package com.sou56.vikunjabubble

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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

        // 接続テスト: 実際に GET /api/v1/projects を呼んでプロジェクト一覧をダイアログで表示
        btnTest.setOnClickListener {
            val url   = etUrl.text.toString().trim()
            val token = etToken.text.toString().trim()

            if (url.isEmpty() || token.isEmpty()) {
                Toast.makeText(this, "URLとトークンを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnTest.isEnabled = false
            btnTest.text = "接続中…"

            lifecycleScope.launch {
                val result = VikunjaApi.fetchProjects(url, token)
                btnTest.isEnabled = true
                btnTest.text = "接続テスト"

                result.fold(
                    onSuccess = { projects ->
                        val message = if (projects.isEmpty()) {
                            "接続成功！プロジェクトが見つかりませんでした。"
                        } else {
                            "接続成功！以下のプロジェクトが利用できます。\n上の「ID」をプロジェクトID欄に入力してください。\n\n" +
                            projects.joinToString("\n")
                        }
                        AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("プロジェクト一覧")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show()
                    },
                    onFailure = { e ->
                        AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("接続失敗")
                            .setMessage(e.message ?: "不明なエラー")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                )
            }
        }
    }
}
