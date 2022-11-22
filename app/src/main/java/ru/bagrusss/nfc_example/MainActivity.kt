package ru.bagrusss.nfc_example

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.bagrusss.nfc.NfcHandler

class MainActivity : AppCompatActivity() {

    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val nfcHandler = NfcHandler()
    private val nfcCallback = NfcAdapter.ReaderCallback { onNewTag(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        maybeHandleTag(intent)
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter?.let { adapter ->
            if (!adapter.isEnabled) {
                val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                Snackbar.make(root, "Enable NFC module", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK") { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
                    .show()
            } else {
                /*adapter.enableForegroundDispatch(
                    this,
                    PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE),
                    null,
                    null
                )*/
                adapter.enableReaderMode(
                    this,
                    nfcCallback,
                    NfcAdapter.FLAG_READER_NFC_A,
                       // or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    //or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    null
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()

        nfcAdapter?.run {
            //disableForegroundDispatch(this@MainActivity)
            disableReaderMode(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        maybeHandleTag(intent)
    }

    private fun maybeHandleTag(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let(::onNewTag)
        }
    }

    private fun onNewTag(tag: Tag) {
        nfcHandler.handleTag(tag)
        runOnUiThread {
            Toast.makeText(this, "new tag ${tag.id.toHexString()}", Toast.LENGTH_SHORT)
                .show()
        }
    }

}