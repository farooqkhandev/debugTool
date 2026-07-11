package com.quadlogixs.debugtool.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quadlogixs.debugtool.core.ApiSpeedTracker
import com.quadlogixs.debugtool.core.JankLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class ApiPerformanceViewModel @Inject constructor(
    private val logRepository: JankLogRepository
) : ViewModel() {
    val records = ApiSpeedTracker.records
    fun clear() = ApiSpeedTracker.clear()
    fun testApiSpeed(url: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                val client = OkHttpClient()
                val req = Request.Builder().url(url).build()
                val res = client.newCall(req).execute()
                val duration = System.currentTimeMillis() - start
                withContext(Dispatchers.Main) {
                    onResult("Response ${res.code} in ${duration} ms")
                }
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - start
                withContext(Dispatchers.Main) {
                    onResult("Failed after ${duration} ms: ${e.message}")
                }
            }
        }
    }
}
