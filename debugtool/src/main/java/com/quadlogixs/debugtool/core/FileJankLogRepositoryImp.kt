package com.quadlogixs.debugtool.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class FileJankLogRepositoryImp @Inject constructor(
    @ApplicationContext private val context: Context
) : JankLogRepository {
    private val _junkList: MutableStateFlow<List<JankModel>> = MutableStateFlow(listOf())
    private val junkList: StateFlow<List<JankModel>> = _junkList.asStateFlow()

    override fun saveLog(log: JankModel) {
        _junkList.value += log
    }

    override fun getLogs(): List<JankModel> =
        junkList.value

    override fun clearLogs() {
        _junkList.value = listOf()
    }
}
