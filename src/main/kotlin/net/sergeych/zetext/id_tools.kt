package net.sergeych.zetext

import java.util.*
import kotlin.random.Random

private val idLetters = "qwertyuiopasdfghjklzxcvbnm"
private val idChars = idLetters + idLetters.uppercase() + "_$1234567890"

fun randomId(length: Int): String {
    val id = StringBuilder()
    for( i in 1..length)
        id.append(idChars[Random.nextInt(0, idChars.length)])
    return id.toString()
}

fun ByteArray.toBase64(): String =
    String(Base64.getEncoder().encode(this))

fun String.decodeBase64(): ByteArray =
    Base64.getDecoder().decode(this)

val EMPTY = mapOf<String,String>()