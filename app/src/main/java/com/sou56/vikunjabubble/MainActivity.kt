package com.sou56.vikunjabubble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editUrl: EditText
    private lateinit var editToken: EditText
    private lateinit var editProject: EditText
    private lateinit var btnStart: Button

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            BubbleNotificationHelper.showBubble(this, "バブルが有効になりました")
        } else {
            Toast.makeText(this, "通知権限がないとバブルが表示できません", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editUrl = findViewById(R.id.editUrl)
        editToken = findViewById(R.id.editToken)
        editProject = findViewById(R.id.editProject)
        btnStart = findViewById(R.id.btnStart)

        editUrl.setText(PrefsManager.getUrl(this))
        editToken.setText(PrefsManager.getToken(this))
        val pid = PrefsManager.getProjectId(this)
        if (pid != 0L) editProject.setText(pid.toString())

        BubbleNotificationHelper.createChannel(this)

        btnStart.setOnClickListener {
            val url = editUrl.text.toString().trim()
            val token = editToken.text.toString().trim()
            val projectId = editProject.text.toString().toLongOrNull() ?: 0L

            if (token.isEmpty()) {
                Toast.makeText(this, "API Tokenを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (projectId == 0L) {
                Toast.makeText(this, "Project IDを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsManager.setUrl(this, url)
            PrefsManager.setToken(this, token)
            PrefsManager.setProjectId(this, projectId)

            Toast.makeText(this, "設定を保存しました", Toast.LENGTH_SHORT).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                BubbleNotificationHelper.showBubble(this, "予定を入力できます")
            }
        }
    }
}
