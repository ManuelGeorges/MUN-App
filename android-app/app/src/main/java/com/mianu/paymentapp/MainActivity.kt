package com.mianu.paymentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mianu.paymentapp.nfc.NfcHub
import com.mianu.paymentapp.ui.theme.MianuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MianuTheme {
                AppScaffold()
            }
        }
    }

    // Reader mode is tied to the foreground window, not to a screen: the platform only grants it to
    // the resumed activity, and releasing it on pause is what lets other apps read a badge when
    // this one isn't in front.
    override fun onResume() {
        super.onResume()
        NfcHub.bind(this)
    }

    override fun onPause() {
        super.onPause()
        NfcHub.unbind(this)
    }
}
