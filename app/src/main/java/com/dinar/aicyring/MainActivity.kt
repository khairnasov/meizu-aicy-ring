package com.dinar.aicyring

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(AicyBridge(), "AndroidAicyBridge")
        webView.loadUrl("file:///android_asset/index.html")

        playStartupRingSequence()
    }

    private fun playStartupRingSequence() {
        val startupColors = listOf("#4285F4", "#9B72CB", "#D96570", "#34A853")
        startupColors.forEachIndexed { index, hex ->
            handler.postDelayed({
                AicyNotificationService.triggerAicyRingPulse(
                    this,
                    "gemini_startup",
                    Color.parseColor(hex),
                    500,
                    200
                )
            }, index * 650L)
        }
    }

    inner class AicyBridge {
        @JavascriptInterface
        fun openNotificationListenerSettings() {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        @JavascriptInterface
        fun saveAppColor(pkg: String, hexColor: String) {
            getSharedPreferences("aicy_prefs", MODE_PRIVATE)
                .edit()
                .putString("color_$pkg", hexColor)
                .apply()
        }

        @JavascriptInterface
        fun testColorPulse(hexColor: String) {
            val color = try { Color.parseColor(hexColor) } catch (e: Exception) { Color.BLUE }
            AicyNotificationService.triggerAicyRingPulse(this@MainActivity, "manual_test", color, 700, 300)
        }

        @JavascriptInterface
        fun playGeminiCascade() {
            playStartupRingSequence()
        }
    }
}
