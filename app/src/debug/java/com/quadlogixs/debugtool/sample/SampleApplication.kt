package com.quadlogixs.debugtool.sample

import android.app.Application
import com.quadlogixs.debugtool.bootstrap.DebugToolBootstrap
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SampleApplication : Application() {

    @Inject
    lateinit var sampleHost: SampleDebugToolHost

    override fun onCreate() {
        super.onCreate()
        DebugToolBootstrap.install(
            application = this,
            config = SampleDebugToolHost.createConfig(sampleHost),
        )
    }
}
