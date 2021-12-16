package net.sergeych.zetext

import net.sergeych.utils.Bytes
import net.sergeych.zetext.CrcHeader.Companion.longToBytes
import org.testng.annotations.Test

import org.testng.Assert.*
import java.util.zip.CRC32
import kotlin.experimental.xor

fun assertStarts(what: String, withWhat: String) {
    assertEquals(what.substring(0,withWhat.length), withWhat)
}

class CrcHeaderTest {

    @Test
    fun testLongToBytes() {
        val x = CrcHeader.longToBytes(0x44003300220011)
//        Bytes.dump(x)
        assertStarts(Bytes.toDump(x)[0], "0000 00 44 00 33 00 22 00 11")
        assertEquals(CrcHeader.bytesToLong(x), 0x44003300220011)
    }

    @Test
    fun testCrcHeader() {
        val header = CrcHeader.encode("leprecon")
        Bytes.dump(header)
        val x = CrcHeader.decode(header)
        assertNotNull(x)
        assertTrue(x!!.match("leprecon"))
        assertFalse(x.match("leprecoN"))
        header[3] = header[3] xor 17
        assertNull(CrcHeader.decode(header))
    }
}