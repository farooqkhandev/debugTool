package com.quadlogixs.debugtool.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import kotlinx.coroutines.flow.StateFlow

/**
 * Logs recompositions. Uses [LocalCurrentRoute] when the host provides it; otherwise
 * [fallbackRoute] (typically [DebugToolScaffold]'s `routeTrail`) so stats still accumulate.
 */
@Composable
fun RecompositionLogger(fallbackRoute: String = "debug") {
    val navRoute = LocalCurrentRoute.current?.value?.route
    val route = navRoute?.takeIf { it.isNotBlank() } ?: fallbackRoute
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
