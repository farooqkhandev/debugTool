package com.quadlogixs.debugtool.core

import android.util.Log
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.StateInfo
import javax.inject.Inject

class JankFrameProviderImpl @Inject constructor(
    private val logRepository: JankLogRepository
) : FrameDataProvider {

    override fun onFrame(frameData: FrameData) {
        if (frameData.isJank) {
            val junk=JankModel(duration = frameData.frameDurationUiNanos,durationInSeconds = "${frameData.frameDurationUiNanos / 1_000_000}ms",state = frameData.states)
         /*   val log = "Jank detected! Duration: ${frameData.frameDurationUiNanos / 1_000_000}ms, States: ${frameData.states}"
            Log.d("JunkDetection",log)*/
            logRepository.saveLog(junk)
        }
    }
}

data class JankModel(
    val duration:Long,
    val durationInSeconds:String,
    val state:List<StateInfo>,
)
