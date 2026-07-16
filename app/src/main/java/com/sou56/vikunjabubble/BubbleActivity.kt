package com.sou56.vikunjabubble

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class BubbleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editInput: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textEmpty: TextView
    private lateinit var adapter: ChatAdapter
    private var updateListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble)

        recyclerView = findViewById(R.id.recyclerChat)
        editInput    = findViewById(R.id.editInput)
        btnSend      = findViewById(R.id.btnSend)
        progressBar  = findViewById(R.id.progressBar)
        textEmpty    = findViewById(R.id.textEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        refreshList()

        // 未設定の場合は設定画面へ自動遷移
        if (!AppPreferences.isConfigured(this)) {
            ChatMessage.add(ChatMessage("⚙️ 最初に Vikunja の接続設定をしてください", isUser = false))
            refreshList()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnSend.setOnClickListener {
            val text = editInput.text.toString().trim()
            if (text.isNotEmpty()) sendTask(text)
        }

        editInput.setOnEditorActionListener { _, _, _ ->
            val text = editInput.text.toString().trim()
            if (text.isNotEmpty()) sendTask(text)
            true
        }

        updateListener = { runOnUiThread { refreshList() } }
        updateListener?.let { ChatMessage.addListener(it) }
    }

    private fun sendTask(text: String) {
        // 送信前に設定チェック
        if (!AppPreferences.isConfigured(this)) {
            Toast.makeText(this, "設定が未完了です。設定画面を開いてください", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }

        ChatMessage.add(ChatMessage(text, isUser = true))
        editInput.text.clear()
        progressBar.visibility = View.VISIBLE
        btnSend.isEnabled = false

        lifecycleScope.launch {
            val result = VikunjaApi.createTask(this@BubbleActivity, text)
            progressBar.visibility = View.GONE
            btnSend.isEnabled = true
            result
                .onSuccess { msg -> ChatMessage.add(ChatMessage(msg, isUser = false)) }
                .onFailure { e  -> ChatMessage.add(ChatMessage("❌ エラー: ${e.message}", isUser = false)) }
            refreshList()
        }
    }

    private fun refreshList() {
        val history = ChatMessage.getHistory()
        adapter = ChatAdapter(history)
        recyclerView.adapter = adapter
        textEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        if (history.isNotEmpty()) recyclerView.scrollToPosition(history.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateListener?.let { ChatMessage.removeListener(it) }
    }
}
