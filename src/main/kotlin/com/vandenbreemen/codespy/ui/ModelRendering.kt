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
        // Reflection to get types and relations
        val types = try {
            val field = model.javaClass.getDeclaredField("types")
            field.isAccessible = true
            field.get(model) as? List<Any> ?: emptyList()
        } catch (e: Exception) {
            emptyList<Any>()
        }
        val relations = try {
            val field = model.javaClass.getDeclaredField("relations")
            field.isAccessible = true
            field.get(model) as? List<Any> ?: emptyList()
        } catch (e: Exception) {
            emptyList<Any>()
        }

        // Get type name for comparison
        fun getTypeName(type: Any?): String {
            return try {
                type?.javaClass?.getDeclaredField("name")?.apply { isAccessible = true }?.get(type) as? String
                    ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }
        // Count incoming relations for each type
        val incomingCounts = types.associateWith { type ->
            val typeName = getTypeName(type)
            relations.count { rel ->
                val toType = try {
                    rel.javaClass.getDeclaredField("to").apply { isAccessible = true }.get(rel)
                } catch (e: Exception) {
                    null
                }
                getTypeName(toType) == typeName
            }
        }
        // Sort types by incoming count descending
        val sortedTypes = incomingCounts.entries.sortedByDescending { it.value }.map { it.key }
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Make the canvas height large enough to fit all boxes
            val canvasHeight = sortedTypes.size * 100 + 80
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight.dp)
            ) {
                val boxWidth = 200f
                val boxHeight = 60f
                val boxSpacing = 40f
                sortedTypes.forEachIndexed { index, type ->
                    val y = index * (boxHeight + boxSpacing) + 40f
                    drawRect(
                        color = Color(0xFF90CAF9),
                        topLeft = Offset(40f, y),
                        size = Size(boxWidth, boxHeight)
                    )
                }
            }
            // Draw labels on top of boxes
            sortedTypes.forEachIndexed { index, type ->
                val name = getTypeName(type)
                Box(
                    modifier = Modifier
                        .offset(x = 40.dp, y = ((index * 100) + 40).dp)
                        .width(200.dp)
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = TextStyle(fontSize = 18.sp, color = Color.Black)
                    )
                }
            }
        }
    }
}
