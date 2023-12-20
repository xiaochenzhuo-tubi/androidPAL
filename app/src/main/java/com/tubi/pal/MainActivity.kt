package com.tubi.pal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tubi.pal.ui.theme.PALDemoTheme

class MainActivity : ComponentActivity() {
    private var allowStorage = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val tvNonce = findViewById<TextView>(R.id.tv_nonce)
        tvNonce.movementMethod = ScrollingMovementMethod()
        val tvAllowStorage = findViewById<TextView>(R.id.tv_allow_storage)
        setAllowStorage(tvAllowStorage)
        val btnSwitch = findViewById<Button>(R.id.btn_switch)
        btnSwitch.setOnClickListener {
            allowStorage = !allowStorage
            setAllowStorage(tvAllowStorage)
        }

        val nonceGenerator = NonceGenerator(this, object : ConsentStorageInterface {
            override fun getConsentToStorage(): Boolean {
                return allowStorage
            }
        })

        findViewById<Button>(R.id.btn_gen_nonce).setOnClickListener {
            // 生成
            nonceGenerator.generateNonce(object : NonceCallback {
                override fun onSuccess(value: String?) {
                    tvNonce.text = value
                }

                override fun onFailure(exception: Exception?) {
                    tvNonce.text = "Generate failed: ${exception?.message}"
                }
            })
        }

        val btnCopy = findViewById<Button>(R.id.btn_copy)
        btnCopy.setOnClickListener {
            if (tvNonce.text.isNullOrEmpty()) {
                Toast.makeText(this, "no nonce", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = ClipData.newPlainText("nonce", tvNonce.text)
            clipboard.setPrimaryClip(text)
            Toast.makeText(this, "copy success", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setAllowStorage(view: TextView) {
        view.text = "AllowStorage: $allowStorage"
    }
}