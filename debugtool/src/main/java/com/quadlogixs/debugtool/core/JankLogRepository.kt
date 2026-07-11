package com.quadlogixs.debugtool.core

interface JankLogRepository {
    fun saveLog(log: JankModel)
    fun getLogs(): List<JankModel>
    fun clearLogs()
}
