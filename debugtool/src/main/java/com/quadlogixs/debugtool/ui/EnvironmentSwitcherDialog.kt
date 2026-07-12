package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.BaseButton
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.CardContainer
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.SpacerWidth
import com.quadlogixs.debugtool.ui.components.TitleMediumText
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.components.textSdp
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

@Composable
fun EnvironmentSwitcherDialog(
    onDismiss: () -> Unit,
    restartApp: () -> Unit,
    environmentViewModel: EnvironmentViewModel = hiltViewModel(),
) {
    DebugToolTheme {
        EnvironmentSwitcherDialogContent(
            onDismiss = onDismiss,
            restartApp = restartApp,
            environmentViewModel = environmentViewModel,
        )
    }
}

@Composable
private fun EnvironmentSwitcherDialogContent(
    onDismiss: () -> Unit,
    restartApp: () -> Unit,
    environmentViewModel: EnvironmentViewModel,
) {
    val context = LocalContext.current
    val allEnvs by environmentViewModel.environments.collectAsStateWithLifecycle()
    val selectedEnvironment by environmentViewModel.selectedEnvironmentUrl.collectAsStateWithLifecycle()
    var pendingSelection by remember(selectedEnvironment) { mutableStateOf(selectedEnvironment) }
    var showCustomUrlLayout by remember { mutableStateOf(false) }
    var newEnvUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        environmentViewModel.loadEnvironmentScreen()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CardContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.sdp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ResourceImage(
                        image = R.drawable.ic_back,
                        modifier = Modifier
                            .size(20.sdp)
                            .safeClickable { onDismiss() },
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(DebugColors.TextPrimary),
                    )
                    SpacerWidth(8.sdp)
                    Column(modifier = Modifier.weight(1f)) {
                        TitleMediumText(
                            text = "Change Environment",
                            overrideColor = DebugColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        BodySmallText(
                            text = "Select the environment to use for the application.",
                            overrideColor = DebugColors.TextSecondary,
                        )
                    }
                }

                SpacerHeight(12.sdp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(allEnvs) { _, env ->
                        val selected = env.url == pendingSelection
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .safeClickable { pendingSelection = env.url },
                            shape = RoundedCornerShape(12.dp),
                            color = DebugColors.Surface,
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (selected) DebugColors.AccentBlue else DebugColors.Border,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { pendingSelection = env.url },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DebugColors.AccentBlue,
                                        unselectedColor = DebugColors.TextSecondary,
                                    ),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    LabelMediumText(
                                        text = env.title,
                                        fontWeight = FontWeight.SemiBold,
                                        overrideColor = DebugColors.TextPrimary,
                                    )
                                    LabelSmallText(
                                        text = env.url,
                                        fontSize = 11.textSdp,
                                        maxLines = 2,
                                        overrideColor = DebugColors.TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }

                if (showCustomUrlLayout) {
                    SpacerHeight(10.sdp)
                    BodySmallText(
                        text = "Add Custom Environment",
                        overrideColor = DebugColors.TextSecondary,
                    )
                    SpacerHeight(6.sdp)
                    OutlinedTextField(
                        value = newEnvUrl,
                        onValueChange = { newEnvUrl = it },
                        placeholder = {
                            LabelMediumText(
                                text = "Environment URL",
                                overrideColor = DebugColors.TextSecondary,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DebugColors.TextPrimary,
                            unfocusedTextColor = DebugColors.TextPrimary,
                            focusedBorderColor = DebugColors.AccentTeal,
                            unfocusedBorderColor = DebugColors.Border,
                            focusedContainerColor = DebugColors.SurfaceElevated,
                            unfocusedContainerColor = DebugColors.SurfaceElevated,
                            cursorColor = DebugColors.AccentTeal,
                        ),
                    )
                    SpacerHeight(8.sdp)
                    BaseButton(
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(36.dp),
                        enabled = newEnvUrl.isNotEmpty(),
                        onClick = {
                            environmentViewModel.addCustomEnvironment(
                                title = "Custom",
                                url = newEnvUrl,
                            )
                            newEnvUrl = ""
                            context.showDebugToast("Custom environment added.")
                        },
                    ) {
                        LabelMediumText(
                            text = "Add",
                            fontSize = 12.textSdp,
                            overrideColor = DebugColors.OnAccent,
                        )
                    }
                }

                SpacerHeight(12.sdp)

                BaseButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    containerColor = DebugColors.AccentTeal,
                    enabled = pendingSelection.isNotBlank(),
                    onClick = {
                        if (pendingSelection != selectedEnvironment) {
                            environmentViewModel.changeEnvironment(pendingSelection)
                            context.showDebugToast("Environment changed. Restarting app...")
                            restartApp()
                        } else {
                            onDismiss()
                        }
                    },
                ) {
                    LabelMediumText(
                        text = "APPLY & RESTART",
                        fontWeight = FontWeight.Bold,
                        overrideColor = DebugColors.OnAccent,
                    )
                }

                SpacerHeight(8.sdp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    BaseButton(
                        modifier = Modifier.height(36.dp),
                        primary = false,
                        containerColor = DebugColors.SurfaceElevated,
                        onClick = { showCustomUrlLayout = !showCustomUrlLayout },
                    ) {
                        LabelMediumText(
                            text = if (!showCustomUrlLayout) "Add Custom" else "Hide Custom",
                            fontSize = 12.textSdp,
                            overrideColor = DebugColors.TextPrimary,
                        )
                    }
                }
            }
        }
    }
}
