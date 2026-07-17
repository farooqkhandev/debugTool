package com.quadlogixs.debugtool.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quadlogixs.debugtool.api.DebugEnvironment
import com.quadlogixs.debugtool.api.DebugToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Library-owned persistence for the selected environment URL and custom environments.
 * Host [com.quadlogixs.debugtool.api.DebugToolHost.applyEnvironment] is still invoked so
 * host apps can recreate network clients.
 */
object DebugEnvironmentStore {

    private const val PREFS_NAME = "debugtool_env_prefs"
    private const val KEY_SELECTED_URL = "selected_environment_url"
    private const val KEY_CUSTOM_ENVS = "custom_environments_json"

    private val gson = Gson()
    private val customType = object : TypeToken<List<DebugEnvironment>>() {}.type

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun environments(hostBase: List<DebugEnvironment>): List<DebugEnvironment> {
        val custom = loadCustomEnvironments()
        val seen = linkedSetOf<String>()
        val merged = ArrayList<DebugEnvironment>(hostBase.size + custom.size)
        for (env in hostBase + custom) {
            val key = normalizeUrl(env.url)
            if (key.isBlank() || !seen.add(key)) continue
            merged.add(env)
        }
        return merged
    }

    suspend fun currentUrl(): String = withContext(Dispatchers.IO) {
        val saved = prefs?.getString(KEY_SELECTED_URL, null).orEmpty().trim()
        if (saved.isNotBlank() && !saved.equals("default", ignoreCase = true)) {
            return@withContext saved
        }
        if (!DebugToolRegistry.isInstalled()) return@withContext ""
        val host = DebugToolRegistry.host
        val hostCurrent = runCatching { host.currentEnvironment() }.getOrDefault("").trim()
        if (hostCurrent.isNotBlank() && !hostCurrent.equals("default", ignoreCase = true)) {
            return@withContext hostCurrent
        }
        host.environments().firstOrNull()?.url.orEmpty()
    }

    suspend fun applyEnvironment(url: String) {
        val trimmed = url.trim()
        withContext(Dispatchers.IO) {
            prefs?.edit()?.putString(KEY_SELECTED_URL, trimmed)?.apply()
        }
        if (DebugToolRegistry.isInstalled()) {
            DebugToolRegistry.host.applyEnvironment(trimmed)
        }
    }

    suspend fun addCustomEnvironment(title: String, url: String) {
        val env = DebugEnvironment(title = title.trim().ifBlank { "Custom" }, url = url.trim())
        withContext(Dispatchers.IO) {
            val existing = loadCustomEnvironments().toMutableList()
            val key = normalizeUrl(env.url)
            existing.removeAll { normalizeUrl(it.url) == key }
            existing.add(env)
            prefs?.edit()
                ?.putString(KEY_CUSTOM_ENVS, gson.toJson(existing))
                ?.putString(KEY_SELECTED_URL, env.url)
                ?.apply()
        }
        if (DebugToolRegistry.isInstalled()) {
            DebugToolRegistry.host.applyEnvironment(env.url)
        }
    }

    fun titleForSelectedUrl(selectedUrl: String, hostBase: List<DebugEnvironment>): String? {
        val key = normalizeUrl(selectedUrl)
        if (key.isBlank()) return null
        return environments(hostBase).firstOrNull { normalizeUrl(it.url) == key }?.title
    }

    private fun loadCustomEnvironments(): List<DebugEnvironment> {
        val json = prefs?.getString(KEY_CUSTOM_ENVS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<DebugEnvironment>>(json, customType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun normalizeUrl(url: String): String = url.trim().trimEnd('/')
}
