package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodyMediumText
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpaceDefault
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.showDebugToast
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.EnvironmentViewModel
@Composable
fun EnvironmentSwitcherDialog(
    onDismiss: () -> Unit,
    restartApp: () -> Unit,
    environmentViewModel: EnvironmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allEnvs by environmentViewModel.environments.collectAsStateWithLifecycle()
    val selectedEnvironment by environmentViewModel.selectedEnvironmentUrl.collectAsStateWithLifecycle()
    var showCustomUrlLayout by remember { mutableStateOf(false) }
    var newEnvUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        environmentViewModel.loadEnvironmentScreen()
    }

    Dialog(onDismissRequest = onDismiss) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TitleMediumText(
                            text = "Environment Selector",
                            overrideColor = MaterialTheme.colorScheme.onPrimary
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable { onDismiss() }
                        )
                    }

                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(8.sdp)

                    // Current Env
                    BodySmallText(
                        text = "Current Environment",
                        overrideColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    LabelMediumText(
                        text = selectedEnvironment,
                        fontSize = 12.textSdp,
                        overrideColor = MaterialTheme.colorScheme.secondary
                    )

                    SpacerHeight(10.sdp)

                    // Environment List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                    ) {
                        itemsIndexed(allEnvs) { _, env ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .safeClickable {
                                        if (env.url != selectedEnvironment) {
                                            environmentViewModel.changeEnvironment(env.url)
                                            context.showDebugToast("Environment changed. Restarting app...")
                                            restartApp()
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (env.url == selectedEnvironment)
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    BodyMediumText(
                                        text = env.title,
                                        overrideColor = if (env.url == selectedEnvironment)
                                            MaterialTheme.colorScheme.onBackground
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    LabelSmallText(
                                        text = env.url,
                                        fontSize = 11.textSdp,
                                        maxLines = 2,
                                        overrideColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }
                    }

                    if(showCustomUrlLayout){
                    SpacerHeight(10.sdp)
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(10.sdp)

                    // Add Custom Environment
                    BodySmallText(
                        text = "Add Custom Environment",
                        overrideColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    SpacerHeight(6.sdp)

                    OutlinedTextField(
                        value = newEnvUrl,
                        onValueChange = { newEnvUrl = it },
                        placeholder = { LabelMediumText(text = "Environment URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    SpacerHeight(8.sdp)

                    BaseButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(32.dp),
                        primary = true,
                        containerColor = MaterialTheme.colorScheme.primary,
                        borderColor = Color.Unspecified,
                        enabled = newEnvUrl.isNotEmpty(),
                        content = {
                            LabelMediumText(
                                text = "Add",
                                fontSize = 12.textSdp,
                                overrideColor = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        onClick = {
                            environmentViewModel.addCustomEnvironment(
                                title = "Custom",
                                url = newEnvUrl
                            )
                            newEnvUrl = ""
                            context.showDebugToast("Custom environment added.")
                        }
                    )

                    SpacerHeight(12.sdp)
                    }
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpacerHeight(10.sdp)

                    // Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {


                        BaseButton(
                            modifier = Modifier.height(30.dp),
                            primary = true,
                            containerColor = Color(0xFFE5E5EA),
                            borderColor = Color.Unspecified,
                            enabled = true,
                            content = {
                                LabelMediumText(
                                    text = "Close",
                                    fontSize = 12.textSdp,
                                    overrideColor = Color(0xFF1C1C1E),
                                )
                            },
                            onClick = { onDismiss() }
                        )
                        SpacerWidth(10.sdp)
                        BaseButton(
                            modifier = Modifier.height(30.dp),
                            primary = true,
                            containerColor = MaterialTheme.colorScheme.primary,
                            borderColor = Color.Unspecified,
                            enabled = true,
                            content = {
                                LabelMediumText(
                                    text = if (!showCustomUrlLayout) "Add Custom" else "Hide Custom",
                                    fontSize = 12.textSdp,
                                    overrideColor = MaterialTheme.colorScheme.onPrimary,
                                )
                            },
                            onClick = {
                                showCustomUrlLayout = !showCustomUrlLayout
                            }
                        )
                    }
                }
            }
        )
    }
}

