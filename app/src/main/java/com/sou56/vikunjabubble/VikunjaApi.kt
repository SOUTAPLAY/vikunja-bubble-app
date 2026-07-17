package com.sou56.vikunjabubble

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object VikunjaApi {

    private val client = OkHttpClient()
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * タスクを作成する。
     * Vikunja API: PUT /api/v1/projects/{id}/tasks
     */
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
                    .put(body)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    error("APIエラー: ${response.code} — プロジェクトIDが間違っている可能性があります。設定画面の「接続テスト」でプロジェクト一覧を確認してください。")
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val taskTitle = json.optString("title", title)
                "✅ タスク「$taskTitle」を作成しました"
            }
        }

    /**
     * プロジェクト一覧を取得する。
     * 成功時は "ID: 5 — My Project" 形式のリストを返す。
     * Vikunja API: GET /api/v1/projects
     */
    suspend fun fetchProjects(baseUrl: String, token: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("$baseUrl/api/v1/projects")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    error("接続失敗: ${response.code} — URLまたはトークンを確認してください。")
                }

                val body = response.body?.string() ?: "[]"
                val arr = JSONArray(body)
                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    val id    = obj.optLong("id", -1)
                    val title = obj.optString("title", "(no title)")
                    "ID: $id — $title"
                }
            }
        }
}
