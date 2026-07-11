package com.quadlogixs.debugtool.core

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName

private var cachedDeviceConfigs: DeviceConfig? = null

/*
fun getCachedDeviceConfig(deviceId: String,onFetchFailed: () -> Unit,onFetchSuccess: (DeviceConfig?) -> Unit) {
    if (cachedDeviceConfigs == null) {
        try {
            val gson = Gson()
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val jsonString = remoteConfig.getString("device_config")
                    Log.d("RemoteConfig", "Fetched JSON: $jsonString")
                    val listType = object : TypeToken<List<Map<String, DeviceConfig>>>() {}.type
                    val list: List<Map<String, DeviceConfig>> = gson.fromJson(jsonString, listType)
                    cachedDeviceConfigs = list.flatMap { it.entries }.associate { it.toPair() }
                    onFetchSuccess(cachedDeviceConfigs?.get("deviceId_${deviceId}"))
                } else {
                    Log.e("RemoteConfig", "Fetch failed", task.exception)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFetchFailed()
        }
    }else{
        onFetchSuccess(cachedDeviceConfigs?.get("deviceId_${deviceId}"))
    }
}
*/


fun getDeviceConfigFromFirestore(
    deviceId: String,
    onFetchFailed: () -> Unit,
    onFetchSuccess: (DeviceConfig?) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val documentId = "licence_2025_${deviceId}"

    // Create default empty config
    val defaultConfig = DeviceConfig(
        pin = "",
        enable = false
    )

    // If cached → return fast
    cachedDeviceConfigs?.let {
        onFetchSuccess(it)
        return
    }

    firestore.collection("debugTool")
        .document(documentId)
        .get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                // Document exists → parse it
                val config = doc.toObject(DeviceConfig::class.java) ?: defaultConfig

                // Cache it
                if (cachedDeviceConfigs == null){
                    cachedDeviceConfigs= config
                }

                onFetchSuccess(config)

            } else {
                // Document does NOT exist → create a default one
                firestore.collection("debugTool")
                    .document(documentId)
                    .set(defaultConfig)
                    .addOnSuccessListener {
                        onFetchFailed()
                        // Cache default config
                        /*if (cachedDeviceConfigs == null) {
                            cachedDeviceConfigs = defaultConfig
                        }
                        onFetchSuccess(defaultConfig)*/
                    }
                    .addOnFailureListener {
                        onFetchFailed()
                    }
            }
        }
        .addOnFailureListener {
            onFetchFailed()
        }
}


data class DeviceConfig(
    @SerializedName("pin")
    val pin: String = "",
    @SerializedName("enable")
    val enable: Boolean = false,
    @SerializedName("haltLevel")
    val haltLevel: Int = 0,
    @SerializedName("canChangeEnvironment")
    val canChangeEnvironment: Boolean = false,
    @SerializedName("localLogs")
    val localLogs: Boolean = false,
    @SerializedName("networkTrace")
    val networkTrace: Boolean = true,
    @SerializedName("apisPerformance")
    val apisPerformance: Boolean = false
)
