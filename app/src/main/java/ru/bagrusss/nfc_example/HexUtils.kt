package ru.bagrusss.nfc_example

private val HEX_CHAR_TABLE = byteArrayOf(
    '0'.toByte(), '1'.toByte(), '2'.toByte(), '3'.toByte(),
    '4'.toByte(), '5'.toByte(), '6'.toByte(), '7'.toByte(),
    '8'.toByte(), '9'.toByte(), 'A'.toByte(), 'B'.toByte(),
    'C'.toByte(), 'D'.toByte(), 'E'.toByte(), 'F'.toByte()
)

fun ByteArray.toHexString(): String {
    val hex = ByteArray(2 * size)
    var index = 0
    var pos = 0
    for (b in this) {
        if (pos >= size) break
        pos++
        val v: Int = b.toInt() and 0xFF
        hex[index++] = HEX_CHAR_TABLE[v ushr 4]
        hex[index++] = HEX_CHAR_TABLE[v and 0xF]
    }
    return String(hex)
}

fun String.toHexBytes(): ByteArray {
    val data = ByteArray(length / 2)
    var i = 0
    while (i < length) {
        data[i / 2] = ((Character.digit(get(i), 16) shl 4) + Character.digit(get(i + 1), 16)).toByte()
        i += 2
    }
    return data
}