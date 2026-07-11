package com.quadlogixs.debugtool.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import kotlinx.coroutines.flow.StateFlow

@Composable
fun RecompositionLogger() {
    val route: String = LocalCurrentRoute.current?.value?.route ?: ""
    val componentName = currentCompositeKeyHash.toString()

    SideEffect {
        if (route.isNotBlank()) {
            RecompositionLogStore.log(route, componentName)
        }
    }
}

val LocalCurrentRoute = compositionLocalOf<StateFlow<NavDestination?>?> { null }

@Composable
fun showToast(message: String) {
    val context = LocalContext.current
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.showDebugToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
