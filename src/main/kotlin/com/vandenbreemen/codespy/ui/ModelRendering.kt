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
                val boxLeft = 40f
                // Calculate box positions
                val boxPositions = sortedTypes.mapIndexed { index, type ->
                    val y = index * (boxHeight + boxSpacing) + 40f
                    val name = getTypeName(type)
                    name to Offset(boxLeft, y)
                }.toMap()
                // Draw boxes
                sortedTypes.forEachIndexed { index, type ->
                    val y = index * (boxHeight + boxSpacing) + 40f
                    drawRect(
                        color = Color(0xFF90CAF9),
                        topLeft = Offset(boxLeft, y),
                        size = Size(boxWidth, boxHeight)
                    )
                }
                // Draw arrows for subclass relations
                relations.forEach { rel ->
                    val relType = try {
                        rel.javaClass.getDeclaredField("type").apply { isAccessible = true }.get(rel)
                    } catch (e: Exception) {
                        null
                    }
                    if (relType?.toString() == "subclass") {
                        val fromType = try {
                            rel.javaClass.getDeclaredField("from").apply { isAccessible = true }.get(rel)
                        } catch (e: Exception) {
                            null
                        }
                        val toType = try {
                            rel.javaClass.getDeclaredField("to").apply { isAccessible = true }.get(rel)
                        } catch (e: Exception) {
                            null
                        }
                        val fromName = getTypeName(fromType)
                        val toName = getTypeName(toType)
                        val fromPos = boxPositions[fromName]
                        val toPos = boxPositions[toName]
                        if (fromPos != null && toPos != null) {
                            // Arrow from right side of from box to top side of to box
                            val start = Offset(fromPos.x + boxWidth, fromPos.y + boxHeight / 2)
                            val end = Offset(toPos.x + boxWidth / 2, toPos.y)
                            // Calculate arrowhead base
                            val arrowSize = 12f
                            val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                            val baseX = (end.x - arrowSize * Math.cos(angle)).toFloat()
                            val baseY = (end.y - arrowSize * Math.sin(angle)).toFloat()
                            val base = Offset(baseX, baseY)
                            // Draw line up to base of arrowhead (make thinner)
                            drawLine(
                                color = Color.Black,
                                start = start,
                                end = base,
                                strokeWidth = 2f // thinner line
                            )
                            // Draw empty arrowhead (outline only)
                            val arrowAngle1 = angle - Math.PI / 8
                            val arrowAngle2 = angle + Math.PI / 8
                            val arrowP1 = Offset(
                                (end.x - arrowSize * Math.cos(arrowAngle1)).toFloat(),
                                (end.y - arrowSize * Math.sin(arrowAngle1)).toFloat()
                            )
                            val arrowP2 = Offset(
                                (end.x - arrowSize * Math.cos(arrowAngle2)).toFloat(),
                                (end.y - arrowSize * Math.sin(arrowAngle2)).toFloat()
                            )
                            // Draw outline triangle for arrowhead
                            drawLine(color = Color.Black, start = end, end = arrowP1, strokeWidth = 2f)
                            drawLine(color = Color.Black, start = end, end = arrowP2, strokeWidth = 2f)
                            drawLine(color = Color.Black, start = arrowP1, end = arrowP2, strokeWidth = 2f)
                        }
                    }
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
