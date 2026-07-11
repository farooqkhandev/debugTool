package com.quadlogixs.debugtool.ui

object RecompositionLogStore {
    private val logs = mutableListOf<RecompositionLoggerModel>()

    fun log(screen: String, component: String) {
        val existing = logs.find { it.screen == screen && it.component == component }
        if (existing != null) {
            existing.count++
        } else {
            logs.add(RecompositionLoggerModel(screen, component, 1))
        }
    }

    fun getLogs(): List<RecompositionLoggerModel> = logs.sortedByDescending { it.count }

    fun clear() {
        logs.clear()
    }
}
data class RecompositionLoggerModel(
    val screen: String,
    val component: String,
    var count: Int
)

