package com.quadlogixs.debugtool.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


object ApiSpeedTracker {
    private val _records = MutableStateFlow<List<ApiSpeedRecord>>(emptyList())
    val records = _records.asStateFlow()

    fun addRecord(url: String, method: String, duration: Long, statusCode: Int) {
        val newRecord = ApiSpeedRecord(url, method, duration, statusCode, System.currentTimeMillis())
        _records.update { it + newRecord }
    }

    fun clear() {
        _records.value = emptyList()
    }
}

data class ApiSpeedRecord(
    val url: String,
    val method: String,
    val duration: Long,
    val statusCode: Int,
    val timestamp: Long
)
