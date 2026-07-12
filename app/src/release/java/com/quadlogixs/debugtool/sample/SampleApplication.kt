package com.quadlogixs.debugtool.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Release sample Application — hooks-only classpath (no full debugtool install).
 */
@HiltAndroidApp
class SampleApplication : Application()
