package com.quadlogixs.debugtool.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.hooks.rememberDebugDynamicTypeScale

@Composable
fun SampleContent(modifier: Modifier = Modifier) {
    val scale = rememberDebugDynamicTypeScale()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "debugTool Sample",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Dynamic type scale (hooks): $scale",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Tap the floating bug icon to open the debug menu (debug builds).",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
