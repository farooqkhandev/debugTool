package com.quadlogixs.debugtool.ui

/**
 * Handle for the optional shake detector created via
 * [com.quadlogixs.debugtool.DebugTool.createShakeDetector].
 */
interface ShakeHandle {
    fun start()
    fun stop()
    fun onShakeDetected()
}
