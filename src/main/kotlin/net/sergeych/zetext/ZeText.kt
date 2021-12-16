package net.sergeych.zetext

import com.icodici.crypto.HashType
import com.icodici.crypto.PBKDF2
import com.icodici.crypto.SymmetricKey
import com.icodici.crypto.digest.Digest
import com.icodici.crypto.digest.Sha3_256
import com.icodici.crypto.digest.Sha3_384
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import net.sergeych.boss.Boss
import net.sergeych.boss_serialization.BossDecoder
import net.sergeych.boss_serialization.BossEncoder
import net.sergeych.boss_serialization.deserialize
import net.sergeych.boss_serialization.encode
import net.sergeych.unikrypto.getDigestClass
import java.io.*
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.random.Random

@Serializable
internal data class ZtInfo(
    val rounds: Int = 20000,
    var salt: ByteArray = Random.Default.nextBytes(32),
    var hashType: HashType,
    var keyId: ByteArray,
) {
    fun deriveKey(password: String): SymmetricKey? {
        val generated = PBKDF2.derive(hashType.getDigestClass(), password, salt, rounds, 64)
        val keyId2 = generated.takeLast(32).toByteArray()
        return if (keyId2 contentEquals keyId) SymmetricKey(generated.take(32).toByteArray()) else null;
    }

    companion object {
        fun deriveFromPassword(
            password: String,
            rounds: Int = 20000,
            hashType: HashType = HashType.SHA3_384
        ): Pair<ZtInfo, SymmetricKey> {
            val salt = Random.nextBytes(32)
            val generated = PBKDF2.derive(hashType.getDigestClass(), password, salt, rounds, 64)
            val keyId = generated.takeLast(32).toByteArray()
            return ZtInfo(rounds, salt, hashType, keyId) to SymmetricKey(generated.take(32).toByteArray())
        }
    }
}

class ZeText private constructor(
    private var info: ZtInfo,
    private var key: SymmetricKey,
    private var _plaintext: String
) {

    class Error(text: String, cause: Throwable? = null) : Exception(text, cause)

    var text: String
        get() = _plaintext
        set(value) {
            _plaintext = value
        }

    suspend fun saveToFile(fileName: String) {
        withContext(Dispatchers.IO) {
            Path.of(fileName).outputStream(StandardOpenOption.CREATE).use { saveTo(it) }
        }
    }

    fun saveTo(out: OutputStream) {
        out.write(encodedHeader)
        val bw = Boss.Writer(out)
        bw.encode(info)
        bw.write(key.etaEncrypt(_plaintext.toByteArray()))
    }

    fun toByteArray(): ByteArray =
        ByteArrayOutputStream().also { saveTo(it) }.toByteArray()


    sealed class Result {
        class Success(val value: ZeText) : Result()
        object BadPassword : Result()
        class InvalidData(val reason: String) : Result()
    }

    companion object {

        val headerName = "ZeText.1"
        val encodedHeader = CrcHeader.encode(headerName)

        fun fromByteArray(data: ByteArray,password: String): Result =
            loadFrom(ByteArrayInputStream(data),password)

        fun loadFrom(input: InputStream, password: String): Result {
            return try {
                CrcHeader.decode(input)?.let { header ->
                    // TODO: read and decrypt
                    if (header.match(headerName)) {
                        // header is ok
                        val br = Boss.Reader(input)
                        val info = br.deserialize<ZtInfo>()
                        info.deriveKey(password)?.let { key ->
                            val ciphertext = br.readBinary()
                            val plaintext = key.etaDecrypt(ciphertext)
                            Result.Success(ZeText(info, key, String(plaintext)))
                        } ?: Result.BadPassword
                    } else {
                        Result.InvalidData("wrong header")
                    }
                } ?: Result.InvalidData("no header")
            } catch (e: Exception) {
                Result.InvalidData("unpack error: $e")
            }
        }

        suspend fun loadFrom(fileName: String, password: String): Result = withContext(Dispatchers.IO) {
            Path.of(fileName).inputStream().use { loadFrom(it, password) }
        }

        suspend fun loadFrom(file: File, password: String): Result = withContext(Dispatchers.IO) {
            file.inputStream().use { loadFrom(it, password) }
        }

        suspend fun create(
            text: String,
            password: String,
            rounds: Int = 500_000,
            hashtype: HashType = HashType.SHA3_384
        ): ZeText = withContext(Dispatchers.Unconfined) {
            val (zi, key) = ZtInfo.deriveFromPassword(password, rounds = rounds, hashType = hashtype)
            ZeText(zi, key, text)
        }
    }
}