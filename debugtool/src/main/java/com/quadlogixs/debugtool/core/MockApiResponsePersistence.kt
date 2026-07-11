package com.quadlogixs.debugtool.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quadlogixs.debugtool.core.SavedMockResponse

private const val PREFS_NAME = "debug_api_mocks_secure"
private const val KEY_GLOBAL_ENABLED = "global_enabled"
private const val KEY_MOCKS_JSON = "mocks_json"

data class MockApiResponseSnapshot(
    val globalEnabled: Boolean = true,
    val mocks: List<SavedMockResponse> = emptyList(),
)

object MockApiResponsePersistence {
    private val gson = Gson()
    private val mocksType = object : TypeToken<List<SavedMockResponse>>() {}.type

    fun load(context: Context): MockApiResponseSnapshot {
        return try {
            val prefs = encryptedPrefs(context.applicationContext)
            val globalEnabled = prefs.getBoolean(KEY_GLOBAL_ENABLED, true)
            val json = prefs.getString(KEY_MOCKS_JSON, null)
                ?: return MockApiResponseSnapshot(globalEnabled = globalEnabled)
            val mocks: List<SavedMockResponse> = gson.fromJson(json, mocksType) ?: emptyList()
            MockApiResponseSnapshot(globalEnabled = globalEnabled, mocks = mocks)
        } catch (_: Exception) {
            MockApiResponseSnapshot()
        }
    }

    fun save(context: Context, snapshot: MockApiResponseSnapshot) {
        try {
            encryptedPrefs(context.applicationContext).edit()
                .putBoolean(KEY_GLOBAL_ENABLED, snapshot.globalEnabled)
                .putString(KEY_MOCKS_JSON, gson.toJson(snapshot.mocks))
                .apply()
        } catch (_: Exception) {
            // Debug-only persistence — ignore write failures.
        }
    }

    private fun encryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
