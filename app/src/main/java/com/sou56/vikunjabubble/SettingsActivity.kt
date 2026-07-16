package com.sou56.vikunjabubble

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etUrl       = findViewById<EditText>(R.id.etUrl)
        val etToken     = findViewById<EditText>(R.id.etToken)
        val etProjectId = findViewById<EditText>(R.id.etProjectId)
        val btnSave     = findViewById<Button>(R.id.btnSave)
        val btnTest     = findViewById<Button>(R.id.btnTest)

        // 保存済みの値をフォームに反映
        etUrl.setText(AppPreferences.getBaseUrl(this))
        etToken.setText(AppPreferences.getApiToken(this))
        etProjectId.setText(AppPreferences.getProjectId(this).toString())

        btnSave.setOnClickListener {
            val url   = etUrl.text.toString().trim()
            val token = etToken.text.toString().trim()
            val pid   = etProjectId.text.toString().toLongOrNull() ?: 1L

            if (url.isEmpty()) {
                etUrl.error = "サーバーURLを入力してください"
                return@setOnClickListener
            }
            if (token.isEmpty()) {
                etToken.error = "APIトークンを入力してください"
                return@setOnClickListener
            }

            AppPreferences.saveSettings(this, url, token, pid)
            Toast.makeText(this, "設定を保存しました", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnTest.setOnClickListener {
            val url   = etUrl.text.toString().trim()
            val token = etToken.text.toString().trim()
            if (url.isEmpty() || token.isEmpty()) {
                Toast.makeText(this, "URLとトークンを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 一時保存してテスト（保存はまだしない）
            Toast.makeText(this, "接続テスト: $url", Toast.LENGTH_LONG).show()
        }
    }
}
