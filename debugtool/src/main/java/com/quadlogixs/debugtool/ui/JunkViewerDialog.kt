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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
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

@Composable
fun JunkViewerDialog(onDismissRequest: () -> Unit,viewModel:JankLogsViewModel = hiltViewModel()) {
    val context = LocalContext.current

    var logs by remember { mutableStateOf(viewModel.getLogs()) }
    Dialog(onDismissRequest = onDismissRequest) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(10.sdp)
                        .height(350.sdp)
                        .fillMaxWidth()
                ) {
                    SpacerHeight(5.sdp)
                    Row {
                        TitleMediumText(
                            text = "Junk Viewer",
                            overrideColor = MaterialTheme.colorScheme.onPrimary
                        )
                        SpacerWeight(1f)
                        if (logs.isNotEmpty()) {
                            ResourceImage(
                                image = R.drawable.ic_clear,
                                modifier = Modifier
                                    .size(18.sdp)
                                    .safeClickable {
                                        logs = listOf()
                                        viewModel.clearLogs()
                                    })
                            SpacerWidth(10.sdp)
                        }
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
                    Column(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())) {

                        if (logs.isNotEmpty()) {
                            logs.reversed().forEach { item ->
                                Row {
                                    item.state.forEach {childItem->
                                        LabelMediumText(
                                            text = "${childItem.key}:",
                                            maxLines = 1,
                                            overrideColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                        LabelSmallText(text = "${childItem.value} ,", maxLines = 1)
                                        SpacerWidth(2.sdp)
                                    }
                                }
                                SpacerHeight(2.sdp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    LabelSmallText(text = "Jank detected! Duration: ${item.durationInSeconds}")
                                    SpacerWidth(10.sdp)
                                }
                                HorizontalLineDivider(horizontalPadding = 0.dp)
                                SpaceDefault()
                            }
                        } else {
                            SpacerHeight(20.sdp)
                            NoRecentFoundItem(
                                title = "You Don't have any jank stats",
                                icon = R.drawable.ic_no_history
                            )
                        }
                    }
                }

            })
    }
}
