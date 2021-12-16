package net.sergeych.zetext

import kotlinx.coroutines.runBlocking
import net.sergeych.utils.Bytes
import org.testng.annotations.Test

import org.testng.Assert.*
import kotlin.test.assertIs

class ZeTextTest {

    @Test
    fun testSaveToFile() {
        runBlocking {
            val zt = ZeText.create("fooBar", "barFoo", rounds = 500_000)
            val x = zt.toByteArray()
            Bytes.dump(x)
            val y = ZeText.fromByteArray(x, "barFoo")
            assertIs<ZeText.Result.Success>(y)
            y as ZeText.Result.Success
            assertEquals(y.value.text, "fooBar")
        }

    }
}