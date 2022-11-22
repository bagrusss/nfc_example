package ru.bagrusss.nfc_example

enum class NfcTags(val value: String) {
    MIFARE_CLASSIC("android.nfc.tech.MifareClassic"),
    MIFARE_ULTRALIGHT("android.nfc.tech.MifareUltralight"),
    NFC_A("android.nfc.tech.NfcA"),
}