package com.sou56.vikunjabubble

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME   = "vikunja_prefs"
    private const val KEY_BASE_URL  = "base_url"
    private const val KEY_API_TOKEN = "api_token"
    private const val KEY_PROJECT_ID = "project_id"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBaseUrl(context: Context): String =
        prefs(context).getString(KEY_BASE_URL, "") ?: ""

    fun getApiToken(context: Context): String =
        prefs(context).getString(KEY_API_TOKEN, "") ?: ""

    fun getProjectId(context: Context): Long =
        prefs(context).getLong(KEY_PROJECT_ID, 1L)

    fun saveSettings(
        context: Context,
        url: String,
        token: String,
        projectId: Long
    ) {
        prefs(context).edit()
            .putString(KEY_BASE_URL, url.trimEnd('/'))
            .putString(KEY_API_TOKEN, token.trim())
            .putLong(KEY_PROJECT_ID, projectId)
            .apply()
    }

    fun isConfigured(context: Context): Boolean =
        getBaseUrl(context).isNotEmpty() && getApiToken(context).isNotEmpty()
}
