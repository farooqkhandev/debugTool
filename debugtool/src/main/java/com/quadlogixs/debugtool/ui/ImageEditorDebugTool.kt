package com.quadlogixs.debugtool.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.ui.components.TextButton
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.TextInputFieldApp
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.SpacerHeight
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.ui.theme.LocalExtraThemeColors
import com.quadlogixs.debugtool.R

sealed class Shape {
    data class Line(val points: MutableList<Offset>, val color: Color = Color.Red) : Shape()
    data class Rect(val topLeft: Offset, val size: Size, val color: Color = Color.Blue) : Shape()
    data class Circle(val center: Offset, val radius: Float, val color: Color = Color.Green) :
        Shape()

    data class TextShape(val text: String, val position: Offset, val color: Color = Color.Black) :
        Shape()
}

enum class ToolType { LINE, RECTANGLE, CIRCLE, TEXT, NONE }


@Composable
fun ImageEditorDebugTool(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
    onResult: (Bitmap) -> Unit
) {
    val imageBitmap = bitmap.asImageBitmap()

    var selectedTool by remember { mutableStateOf(ToolType.NONE) }
    val shapes = remember { mutableStateListOf<Shape>() }
    val undoStack = remember { mutableStateListOf<Shape>() }

    var currentShape by remember { mutableStateOf<Shape?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var editingTextShapeIndex by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }
    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 10.sdp)) {

        // --- Drawing Area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.sdp)
                .weight(1f)
                .background(Color.White)
                .pointerInput(selectedTool) {
                    detectDragGestures(
                        onDragStart = { start ->
                            currentShape = when (selectedTool) {
                                ToolType.LINE -> Shape.Line(mutableListOf(start))
                                ToolType.RECTANGLE -> Shape.Rect(start, Size.Zero)
                                ToolType.CIRCLE -> Shape.Circle(start, 0f)
                                ToolType.TEXT -> Shape.TextShape("Enter Text", start)
                                else -> null
                            }
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            currentShape = when (val shape = currentShape) {
                                is Shape.Line -> shape.copy(points = (shape.points + (shape.points.last() + drag)).toMutableList())
                                is Shape.Rect -> shape.copy(
                                    size = Size(
                                        shape.size.width + drag.x,
                                        shape.size.height + drag.y
                                    )
                                )

                                is Shape.Circle -> shape.copy(
                                    radius = (shape.center - change.position).getDistance()
                                )

                                is Shape.TextShape -> shape.copy(position = shape.position + drag)
                                else -> currentShape
                            }
                        },
                        onDragEnd = {
                            currentShape?.let { shapes.add(it) }
                            currentShape = null
                            selectedTool = ToolType.NONE
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val tappedIndex = shapes.indexOfLast {
                            it is Shape.TextShape &&
                                    Rect(
                                        it.position.x,
                                        it.position.y - 50f,
                                        it.position.x + (it.text.length * 25f),
                                        it.position.y + 10f
                                    ).contains(tapOffset)
                        }
                        if (tappedIndex != -1) {
                            val shape = shapes[tappedIndex] as Shape.TextShape
                            editingTextShapeIndex = tappedIndex
                            editingText = shape.text
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale)
                    rotate(rotation)
                }) {
                    val horizontalOffset = (size.width - imageBitmap.width) / 2f
                    drawImage(
                        imageBitmap,
                        topLeft = Offset(horizontalOffset, 0f)
                    )

                    (shapes + listOfNotNull(currentShape)).forEach { shape ->
                        when (shape) {
                            is Shape.Line -> {
                                for (i in 0 until shape.points.size - 1) {
                                    drawLine(
                                        shape.color,
                                        shape.points[i],
                                        shape.points[i + 1],
                                        strokeWidth = 4f
                                    )
                                }
                            }

                            is Shape.Rect -> drawRect(
                                shape.color,
                                shape.topLeft,
                                shape.size,
                                style = Stroke(4f)
                            )

                            is Shape.Circle -> drawCircle(
                                shape.color,
                                shape.radius,
                                shape.center,
                                style = Stroke(4f)
                            )

                            is Shape.TextShape -> drawContext.canvas.nativeCanvas.drawText(
                                shape.text,
                                shape.position.x,
                                shape.position.y,
                                android.graphics.Paint().apply {
                                    color = shape.color.toArgb()
                                    textSize = 48f
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                }
            }

            if (editingTextShapeIndex != null) {
                TextInputFieldApp(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    value = editingText,
                    singleLine = true,
                    maxLines = 1,
                    onValueChange = {
                        editingText = it
                    },
                    onDoneAction = {
                        editingTextShapeIndex?.let { idx ->
                            val old = shapes[idx] as Shape.TextShape
                            shapes[idx] = old.copy(text = editingText)
                        }
                        editingTextShapeIndex = null
                    },
                    trailing = {
                        if(editingText.isNotEmpty()){
                        ResourceImage(
                            image = R.drawable.ic_tick_circle,
                            modifier = Modifier
                                .size(18.sdp)
                                .safeClickable {
                                    editingTextShapeIndex?.let { idx ->
                                        val old = shapes[idx] as Shape.TextShape
                                        shapes[idx] = old.copy(text = editingText)
                                    }
                                    editingTextShapeIndex = null
                                })
                        }
                    },
                    fieldLabel = "",
                    placeholder = "Enter text here",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified)
                )
            }
        }
        // --- Toolbar ---

        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        selectedTool = ToolType.LINE
                    },
                image = R.drawable.ic_line,
                colorFilter = ColorFilter.tint(color = if (selectedTool == ToolType.LINE) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        selectedTool = ToolType.RECTANGLE
                    },
                image = R.drawable.ic_rectangle,
                colorFilter = ColorFilter.tint(color = if (selectedTool == ToolType.RECTANGLE) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        selectedTool = ToolType.CIRCLE
                    },
                image = R.drawable.ic_circle,
                colorFilter = ColorFilter.tint(color = if (selectedTool == ToolType.CIRCLE) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        selectedTool = ToolType.TEXT
                    },
                image = R.drawable.ic_text,
                colorFilter = ColorFilter.tint(color = if (selectedTool == ToolType.TEXT) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        if (shapes.isNotEmpty()) {
                            undoStack.add(shapes.last())
                            shapes.removeAt(shapes.lastIndex)
                        }
                    },
                image = R.drawable.ic_undo,
                colorFilter = ColorFilter.tint(color = if (shapes.isNotEmpty()) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
            ResourceImage(
                modifier = Modifier
                    .size(18.sdp)
                    .safeClickable {
                        if (undoStack.isNotEmpty()) {
                            shapes.add(undoStack.last())
                            undoStack.removeAt(undoStack.lastIndex)
                        }
                    },
                image = R.drawable.ic_redo,
                colorFilter = ColorFilter.tint(color = if (undoStack.isNotEmpty()) LocalExtraThemeColors.current.tertiarySecondary else LocalExtraThemeColors.current.onDisable)
            )
        }

        TextButton(
            text = "Okay",
            enabled = true,
            modifier = Modifier
                .padding(1.sdp)
                .align(Alignment.CenterHorizontally)
        ) {
            // Create a mutable bitmap the same size as the original
            val resultBitmap = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(resultBitmap)

            // Draw the original bitmap (centered horizontally like in your Canvas)
            val left = (resultBitmap.width - bitmap.width) / 2f
            canvas.drawBitmap(bitmap, left, 0f, null)

            // Draw all shapes on top
            shapes.forEach { shape ->
                when (shape) {
                    is Shape.Line -> {
                        val paint = android.graphics.Paint().apply {
                            color = shape.color.toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 4f
                            isAntiAlias = true
                        }
                        for (i in 0 until shape.points.size - 1) {
                            canvas.drawLine(
                                shape.points[i].x,
                                shape.points[i].y,
                                shape.points[i + 1].x,
                                shape.points[i + 1].y,
                                paint
                            )
                        }
                    }
                    is Shape.Rect -> {
                        val paint = android.graphics.Paint().apply {
                            color = shape.color.toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 4f
                            isAntiAlias = true
                        }
                        canvas.drawRect(
                            shape.topLeft.x,
                            shape.topLeft.y,
                            shape.topLeft.x + shape.size.width,
                            shape.topLeft.y + shape.size.height,
                            paint
                        )
                    }
                    is Shape.Circle -> {
                        val paint = android.graphics.Paint().apply {
                            color = shape.color.toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 4f
                            isAntiAlias = true
                        }
                        canvas.drawCircle(
                            shape.center.x,
                            shape.center.y,
                            shape.radius,
                            paint
                        )
                    }
                    is Shape.TextShape -> {
                        val textPaint = android.graphics.Paint().apply {
                            color = shape.color.toArgb()
                            textSize = 48f
                            isAntiAlias = true
                            style = android.graphics.Paint.Style.FILL
                        }
                        canvas.drawText(shape.text, shape.position.x, shape.position.y, textPaint)
                    }
                }
            }

            // Return the final edited bitmap
            onResult(resultBitmap)
        }
        SpacerHeight(30.sdp)
    }
}
