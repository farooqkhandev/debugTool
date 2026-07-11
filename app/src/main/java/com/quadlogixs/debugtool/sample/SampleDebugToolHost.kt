package com.quadlogixs.debugtool.sample

import android.content.Context
import android.content.SharedPreferences
import com.quadlogixs.debugtool.api.AssignedTo
import com.quadlogixs.debugtool.api.AzureDevOpsConfig
import com.quadlogixs.debugtool.api.AzurePatProvider
import com.quadlogixs.debugtool.api.DebugEnvironment
import com.quadlogixs.debugtool.api.DebugFeatureFlags
import com.quadlogixs.debugtool.api.DebugToolConfig
import com.quadlogixs.debugtool.api.DebugToolHost
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal [DebugToolHost] for the sample app.
 * Host apps should implement the same contract with their own prefs / env / encryption.
 */
@Singleton
class SampleDebugToolHost @Inject constructor(
    @ApplicationContext private val context: Context,
) : DebugToolHost {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("debugtool_sample_prefs", Context.MODE_PRIVATE)

    private val sampleEnvironments = listOf(
        DebugEnvironment(title = "Dev", url = "https://dev.example.com"),
        DebugEnvironment(title = "QA", url = "https://qa.example.com"),
        DebugEnvironment(title = "Staging", url = "https://staging.example.com"),
    )

    override fun environments(): List<DebugEnvironment> = sampleEnvironments

    override suspend fun currentEnvironment(): String =
        prefs.getString(KEY_ENV, sampleEnvironments.first().url).orEmpty()

    override suspend fun applyEnvironment(url: String) {
        prefs.edit().putString(KEY_ENV, url).apply()
    }

    override fun appVersionName(): String = BuildConfig.VERSION_NAME

    override fun flavorName(): String = "sample"

    override fun isEncryptionEnabled(context: Context): Boolean =
        prefs.getBoolean(KEY_ENC, true)

    override suspend fun setEncryptionEnabled(context: Context, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENC, enabled).apply()
    }

    override fun deviceId(): String =
        prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val id = "sample-${System.currentTimeMillis()}"
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            id
        }

    companion object {
        private const val KEY_ENV = "environment"
        private const val KEY_ENC = "encryption_enabled"
        private const val KEY_DEVICE_ID = "device_id"

        fun createConfig(
            host: SampleDebugToolHost,
            patProvider: AzurePatProvider = AzurePatProvider {
                System.getenv("AZURE_DEVOPS_PAT").orEmpty()
            },
        ): DebugToolConfig = DebugToolConfig(
            host = host,
            features = DebugFeatureFlags(
                shakeEnabled = true,
                chuckerEnabled = true,
                firestoreGateEnabled = false,
                mockApiPersistenceEnabled = true,
            ),
            azure = AzureDevOpsConfig(
                organization = "your-org",
                project = "your-project",
                areaPath = "your-project",
                patProvider = patProvider,
            ),
            assignees = listOf(
                AssignedTo(name = "Sample Dev", emailAddress = "dev@example.com"),
            ),
            azureLabel = "sample/debugTool",
        )
    }
}
