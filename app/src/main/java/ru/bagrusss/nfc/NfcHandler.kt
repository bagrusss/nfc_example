package ru.bagrusss.nfc

import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import android.nfc.tech.TagTechnology
import android.util.Log
import ru.bagrusss.nfc_example.NfcTags
import ru.bagrusss.nfc_example.toHexBytes
import ru.bagrusss.nfc_example.toHexString
import java.io.IOException

class NfcHandler {

    private val keys = setOf(
        byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
    )
    private val dataToWrite = "000102030405060708090A0B0C0D0E0F".toHexBytes()

    fun handleTag(tag: Tag) {
        tag.techList.forEach { tech ->
            when (tech) {
                NfcTags.MIFARE_ULTRALIGHT.value -> handleMifareUltralight(MifareUltralight.get(tag))
                NfcTags.MIFARE_CLASSIC.value -> handleMifareClassic(MifareClassic.get(tag))
                NfcTags.NFC_A.value -> handleNfcA(NfcA.get(tag))
            }
        }
    }

    private fun handleMifareUltralight(ultralight: MifareUltralight) {
        ultralight.safeHandle { tag ->
            readUltralight(tag)
        }
    }

    private fun readUltralight(tag: MifareUltralight) {
        Log.d("Ultralight", "read Ultralight")
        for (i in 0 until 16 step 4) {
            val data = tag.readPages(i)
            val pageBuffer = ByteArray(4)
            for (page in 0 until 4) {
                System.arraycopy(data, page * 4, pageBuffer, 0, 4)
                Log.d("Ultralight", pageBuffer.toHexString())
            }
        }
    }

    private fun writeUltralight(tag: MifareUltralight, pageOffset: Int, data: ByteArray) {
        tag.writePage(pageOffset, data)
    }

    private fun handleMifareClassic(classic: MifareClassic) {
        classic.safeHandle { tag ->
            readClassic(tag, keys)
        }
    }

    private fun <T : TagTechnology> T.safeHandle(block: (T) -> Unit) = use { tag ->
        try {
            tag.connect()
            block(this)
        } catch (e: TagLostException) {

        } catch (e: IOException) {

        }
    }

    private fun readClassic(tag: MifareClassic, keys: Set<ByteArray>) {
        for (i in 0 until tag.sectorCount) {
            val key = keys.firstOrNull { tag.authenticateSectorWithKeyA(i, it) }
            //val key = keys.firstOrNull { tag.authenticateSectorWithKeyB(i, it) }
            if (key != null) {
                Log.d("Classic", "Sector: $i key = ${key.toHexString()}")
                val blocksCount = tag.getBlockCountInSector(i)
                for (block in 0 until blocksCount) {
                    val blockData = tag.readBlock(i * blocksCount + block).toHexString()
                    Log.d("Classic", blockData)
                }
            }
        }
    }

    private fun writeClassic(tag: MifareClassic, keys: Set<ByteArray>, sector: Int, data: ByteArray) {
        val key = keys.firstOrNull { tag.authenticateSectorWithKeyA(sector, it) }
        //val key = keys.firstOrNull { tag.authenticateSectorWithKeyB(sector, it) }
        if (key != null) {
            val blocksCount = tag.getBlockCountInSector(sector)
            val blocksToWrite = data.size / 16
            if (blocksToWrite > blocksCount - 1 || sector == 0) {
                throw IllegalArgumentException("data length should be less than ${blocksCount * 16} || zero sector was chosen")
            } else {
                val buffer = ByteArray(16)
                for (block in 0 until blocksToWrite) {
                    System.arraycopy(data, 0, buffer, block * 16, 16)
                    tag.writeBlock(sector * blocksCount + block, buffer)
                }
                Log.d("Classic", "write finished")
            }
        }
    }

    private fun handleNfcA(nfcA: NfcA) {
        if (nfcA.atqa.toHexString() == "4400" && nfcA.sak == 0.toShort()) {
            //Ultralight EV1 20 pages
            Log.d("Ultralight", "read EV1")
            nfcA.safeHandle {
                for (i in 0 until 20 step 4) {
                    val data = it.ultralightReadCmd(i)
                    val pageBuffer = ByteArray(4)
                    for (page in 0 until 4) {
                        System.arraycopy(data, page * 4, pageBuffer, 0, 4)
                        Log.d("Ultralight", pageBuffer.toHexString())
                    }
                }
            }
        }
    }

    // MifareUltralight read 4 Page
    private fun NfcA.ultralightReadCmd(pageOffset: Int) : ByteArray {
        val cmd = byteArrayOf(0x30, pageOffset.toByte())
        return transceive(cmd)
    }

    // MifareUltralight write one page
    private fun NfcA.writePage(page: Int, data: ByteArray) {
        val cmd = ByteArray(data.size + 2)
        cmd[0] = 0xA2.toByte()
        cmd[1] = page.toByte()
        System.arraycopy(data, 0, cmd, 2, data.size)

        transceive(cmd)
    }

}