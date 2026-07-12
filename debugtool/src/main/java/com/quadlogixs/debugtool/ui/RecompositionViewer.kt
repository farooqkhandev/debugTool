package com.quadlogixs.debugtool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import com.quadlogixs.debugtool.ui.components.HorizontalLineDivider
import com.quadlogixs.debugtool.R
import com.quadlogixs.debugtool.ui.components.NoRecentFoundItem
import com.quadlogixs.debugtool.ui.RecompositionLogStore
import com.quadlogixs.debugtool.ui.theme.DebugColors
import com.quadlogixs.debugtool.ui.theme.DebugToolTheme

@Composable
fun RecompositionViewer(onDismissRequest: () -> Unit) {
    DebugToolTheme {
        RecompositionViewerBody(onDismissRequest = onDismissRequest)
    }
}

@Composable
private fun RecompositionViewerBody(onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    val logs = remember { RecompositionLogStore.getLogs() }
    Dialog(onDismissRequest = onDismissRequest) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = DebugColors.Background,
            borderColor = DebugColors.Border,
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .height(350.sdp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    SpacerHeight(5.sdp)
                    Row {
                        TitleMediumText(
                            text = "Recomposition Stats",
                            overrideColor = DebugColors.AccentBlue
                        )
                        SpacerWeight(1f)
                        ResourceImage(
                            image = com.quadlogixs.debugtool.R.drawable.ic_close_receipt,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable {
                                    onDismissRequest()
                                })
                    }
                    SpaceDefault()
                    HorizontalLineDivider(horizontalPadding = 0.dp)
                    SpaceDefault()
                    if (logs.isNotEmpty()) {
                        logs.forEach { item ->
                            Row {
                                LabelMediumText(
                                    text = "Screen:",
                                    overrideColor = MaterialTheme.colorScheme.onPrimary
                                )
                                LabelSmallText(text = item.screen, maxLines = 1)
                            }
                            SpacerHeight(2.sdp)
                            Row(verticalAlignment = Alignment.Bottom) {
                                LabelSmallText(text = "Recomposition in ${item.component} -> count: ${item.count}")
                                SpacerWidth(10.sdp)
                            }
                            HorizontalLineDivider(horizontalPadding = 0.dp)
                            SpaceDefault()
                        }
                    } else {
                        SpacerHeight(20.sdp)
                        NoRecentFoundItem(
                            title = "You Don't have any recomposition stats",
                            icon = R.drawable.ic_no_history
                        )
                    }
                }

            })
    }
}
