package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandenbreemen.grucd.model.Model

@Composable
fun ModelRendering(model: Model?) {
    if (model == null) {
        Text("No model to display.")
    } else {
        // Draw a scrollable canvas to visualize the model`
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Model Rendering", style = TextStyle(fontSize = 24.sp))
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                val width = size.width
                val height = size.height

                // Draw a rectangle for each type in the model
                model.types.forEachIndexed { index, type ->
                    val rectWidth = width / model.types.size
                    val rectHeight = 50f
                    val left = index * rectWidth
                    val top = (height - rectHeight) / 2
                    drawRect(
                        color = Color.LightGray,
                        topLeft = Offset(left, top),
                        size = Size(rectWidth, rectHeight)
                    )

                }
            }
        }
    }
}
