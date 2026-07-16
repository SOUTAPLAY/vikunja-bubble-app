package com.sou56.vikunjabubble

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object VikunjaApi {

    private val client = OkHttpClient()
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun createTask(context: Context, title: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val baseUrl   = AppPreferences.getBaseUrl(context)
                val token     = AppPreferences.getApiToken(context)
                val projectId = AppPreferences.getProjectId(context)

                if (baseUrl.isEmpty() || token.isEmpty()) {
                    error("設定が未完了です。アプリを開いて設定してください。")
                }

                val body = JSONObject().apply {
                    put("title", title)
                    put("project_id", projectId)
                }.toString().toRequestBody(JSON_TYPE)

                val request = Request.Builder()
                    .url("$baseUrl/api/v1/projects/$projectId/tasks")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    error("APIエラー: ${response.code} ${response.message}")
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val taskTitle = json.optString("title", title)
                "✅ タスク「$taskTitle」を作成しました"
            }
        }
}
