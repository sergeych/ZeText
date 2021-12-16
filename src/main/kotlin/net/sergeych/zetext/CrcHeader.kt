package net.sergeych.zetext

import java.io.InputStream
import java.util.zip.CRC32

/**
 * A binary 8-byte multipurpose header, based ona  string value (name), with integrity check and satisfactory low
 * collision rate with good calculation speed.
 *
 * First 6, named "label" depend on the name bytes, using fast crc32 algorithm, and the
 * last 2 bytes, named checkCode, are the check code, based on the same crc32 algorithm (that makes it very fast
 * on platforms where CRC32 is effectively calculated, like modern i64 architectures).
 *
 * The label is calculated as:
 * ~~~
 * val bytes = name.toByteArray()
 * val s1 = crc32(bytes)
 * val s2 = crc32(s1, bytes)
 * val label =eturn s1 + s2.take(2)
 * ~~~
 * in other words, 4 bytes of CRC32 (LE) of the name bytes in UTF8 encodind, then first 2 bytes if the
 * CRC32 of the CRC32 from the previous step and name bytes again.
 *
 * The last 2 bytes, checkCode, is the last 2 bytes of CRC#2(label).
 *
 * Use [encode] to build binary header, [decode] to decode and check it, [match] to test against expected string
 * names or [on] to perform blocks on match.
 *
 * The primary constructor is private, so use [decode] instead. This protects from errors with improperly
 * decoding the label.
 */
class CrcHeader private constructor(val code: ByteArray) {

    /**
     * Check that header matches the specified name
     */
    fun match(text: String): Boolean = label(text).contentEquals(code)

    /**
     * Execute block if the header matches the name and return from block or null if it does not match.
     */
    fun <T>on(text: String, block: () -> T): T? =
        if (match(text)) block() else null

    companion object {

        /**
         * Convert long to LE bytes
         */
        fun longToBytes(value: Long): ByteArray {
            var l = value
            val result = ByteArray(8)
            for (i in 7 downTo 0) {
                result[i] = (l and 0xFF).toByte()
                l = l shr 8
            }
            return result
        }

        /**
         * Convert 8 bytes to LE long
         */
        fun bytesToLong(b: ByteArray): Long {
            var result: Long = 0
            for (i in 0 until 8) {
                result = result shl 8
                result = result or (b[i].toLong() and 0xFF)
            }
            return result
        }

        /**
         * Calculate CRC32 and return it as LE bytes
         */
        fun crc32(vararg source: ByteArray): ByteArray =
            longToBytes(CRC32().also { for (x in source) it.update(x) }.value).takeLast(4).toByteArray()

        /**
         * Calculate label for a given name
         */
        fun label(name: String): ByteArray {
            val bytes = name.toByteArray()
            val s1 = crc32(bytes)
            val s2 = crc32(s1, bytes)
            return s1 + s2.take(2)
        }

        /**
         * Calculate checkCode for a given data
         */
        private fun checkCode(l: ByteArray) = crc32(l).takeLast(2).toByteArray()

        /**
         * Create a binary 8-byte header for a given name. Use [decode] to test and analyse it
         */
        fun encode(name: String): ByteArray {
            val l = label(name)
            return l + checkCode(l)
        }

        /**
         * decode a binary 8-byte header
         * @return decoded header to proceed with or null if encoded data are invalid (wrong size or check code fails)
         */
        fun decode(encoded: ByteArray): CrcHeader? {
            if (encoded.size != 8) return null
            val label = encoded.take(6).toByteArray()
            val code = encoded.takeLast(2).toByteArray()
            return if (code.contentEquals(checkCode(label))) CrcHeader(label) else null
        }

        /**
         * Read and decode header from the stream, or throws file exception if it fails to read 8 necessary 8 bytes
         * from it.
         * @return header instance on successful decoding or null if it is read but invalid
         * @throws input stream exceptions on failre to read the encoded header
         */
        fun decode(input: InputStream): CrcHeader? =
            decode(input.readNBytes(8))
    }
}