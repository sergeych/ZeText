package net.sergeych.unikrypto

import com.icodici.crypto.HashType
import com.icodici.crypto.digest.*

fun HashType.getDigestClass(): Class<out Digest> =
    when(this) {
        HashType.SHA3_384 -> Sha3_384::class.java
        HashType.SHA3_256 -> Sha3_256::class.java
        HashType.SHA256 -> Sha256::class.java
        HashType.SHA512 -> Sha512::class.java
        else ->
            throw IllegalArgumentException("unsupported hash type for PBKDF2: ${this.name}")
    }

fun HashType.createDigest(): Digest = getDigestClass().getConstructor().newInstance()
