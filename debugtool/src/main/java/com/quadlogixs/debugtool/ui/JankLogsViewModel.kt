package com.quadlogixs.debugtool.ui

import androidx.lifecycle.ViewModel
import com.quadlogixs.debugtool.core.JankLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JankLogsViewModel @Inject constructor(
    private val logRepository: JankLogRepository
) : ViewModel() {

    fun getLogs() = logRepository.getLogs()

    fun clearLogs() = logRepository.clearLogs()
}
