package com.quadlogixs.debugtool.ui.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean =
    permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

fun Context.arePermissionsGranted(permissions: List<String>): Boolean =
    arePermissionsGranted(this, permissions.toTypedArray())
