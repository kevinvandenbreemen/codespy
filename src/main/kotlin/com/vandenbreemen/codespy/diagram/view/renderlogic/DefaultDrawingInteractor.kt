package com.vandenbreemen.com.vandenbreemen.codespy.diagram.view.renderlogic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedType
import com.vandenbreemen.grucd.model.RelationType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class DefaultDrawingInteractor : IDrawingInteractor {


    override fun drawTypeBox(
        drawScope: DrawScope,
        positionedType: PositionedType,
        textMeasurer: TextMeasurer,
        isHighlighted: Boolean,
        zoomLevel: Float
    ) {
        with(drawScope) {
            val rect = Rect(
                offset = positionedType.position,
                size = positionedType.size
            )

            // Choose colors based on highlight state
            val backgroundColor =
                if (isHighlighted) Color(0xFFE3F2FD) else Color.LightGray // Light blue for highlighted
            val borderColor = if (isHighlighted) Color(0xFF1976D2) else Color.Black // Blue border for highlighted
            val borderWidth = if (isHighlighted) 3.dp.toPx() else 1.dp.toPx() // Thicker border for highlighted

            // Draw box background
            drawRect(
                color = backgroundColor,
                topLeft = rect.topLeft,
                size = rect.size
            )

            // Draw box border
            drawRect(
                color = borderColor,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = borderWidth)
            )

            // Smaller font sizes for better readability
            val titleStyle = TextStyle(
                fontSize = (12 * zoomLevel).sp, // Reduced from 14sp
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            val packageStyle = TextStyle(
                fontSize = (8 * zoomLevel).sp, // Reduced from 10sp
                color = Color.DarkGray
            )

            // Ellipsize package name if it has more than 4 segments
            val ellipsizedPackage = ellipsizePackageName(positionedType.type.pkg)

            // Measure and draw title with safe positioning
            val titleResult = textMeasurer.measure(
                text = positionedType.type.name,
                style = titleStyle
            )

            val titleX = maxOf(0f, rect.left + (rect.width - titleResult.size.width) / 2)
            val titleY = maxOf(0f, rect.top + 15.dp.toPx())

            // Only draw title if it fits within bounds
            if (titleX >= 0f && titleY >= 0f &&
                titleX + titleResult.size.width <= size.width &&
                titleY + titleResult.size.height <= size.height
            ) {

                drawText(
                    textLayoutResult = titleResult,
                    topLeft = Offset(titleX, titleY)
                )
            }

            // Measure and draw package name with safe positioning
            val packageResult = textMeasurer.measure(
                text = ellipsizedPackage,
                style = packageStyle
            )

            val packageX = maxOf(0f, rect.left + (rect.width - packageResult.size.width) / 2)
            val packageY = maxOf(0f, titleY + titleResult.size.height + 5.dp.toPx())

            // Only draw package if it fits within bounds
            if (packageX >= 0f && packageY >= 0f &&
                packageX + packageResult.size.width <= size.width &&
                packageY + packageResult.size.height <= size.height
            ) {

                drawText(
                    textLayoutResult = packageResult,
                    topLeft = Offset(packageX, packageY)
                )
            }

            // Draw separator line between header and fields
            val separatorY = packageY + packageResult.size.height + 8.dp.toPx()
            if (separatorY < rect.bottom - 10.dp.toPx() && positionedType.type.fields.isNotEmpty()) {
                drawLine(
                    color = borderColor,
                    start = Offset(rect.left + 5.dp.toPx(), separatorY),
                    end = Offset(rect.right - 5.dp.toPx(), separatorY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw fields
            drawFields(
                rect = rect,
                fields = positionedType.type.fields,
                textMeasurer = textMeasurer,
                startY = separatorY + 5.dp.toPx(),
                maxWidth = rect.width - 10.dp.toPx(), // Leave padding on sides
                zoomLevel = zoomLevel
            )
        }
    }

    /**
     * Draw fields within the type box
     */
    private fun DrawScope.drawFields(
        rect: Rect,
        fields: List<com.vandenbreemen.grucd.model.Field>,
        textMeasurer: TextMeasurer,
        startY: Float,
        maxWidth: Float,
        zoomLevel: Float
    ) {
        if (fields.isEmpty()) return

        val fieldStyle = TextStyle(
            fontSize = (10 * zoomLevel).sp,
            color = Color.Black
        )

        var currentY = startY
        val fieldSpacing = 16.dp.toPx() * zoomLevel // Space between fields, scaled
        val leftMargin = rect.left + 5.dp.toPx() * zoomLevel // Left margin for fields, scaled

        fields.forEach { field ->

            val visibilityPrefix = when (field.visibility) {
                com.vandenbreemen.grucd.model.Visibility.Public -> "+"
                com.vandenbreemen.grucd.model.Visibility.Private -> "-"
                com.vandenbreemen.grucd.model.Visibility.Internal -> "~"
            }

            val fieldText = "$visibilityPrefix ${field.name}: ${field.typeName}"
            val fieldResult = textMeasurer.measure(
                text = fieldText,
                style = fieldStyle
            )

            // Check if we have enough space to draw this field
            if (currentY + fieldResult.size.height <= rect.bottom - 5.dp.toPx() * zoomLevel) {
                // Truncate field text if it's too wide
                val truncatedText = if (fieldResult.size.width > maxWidth) {
                    truncateFieldText(fieldText, textMeasurer, fieldStyle, maxWidth)
                } else {
                    fieldText
                }

                val finalResult = textMeasurer.measure(
                    text = truncatedText,
                    style = fieldStyle
                )

                // Only draw if it fits within bounds
                if (leftMargin >= 0f && currentY >= 0f &&
                    leftMargin + finalResult.size.width <= size.width &&
                    currentY + finalResult.size.height <= size.height
                ) {
                    drawText(
                        textLayoutResult = finalResult,
                        topLeft = Offset(leftMargin, currentY)
                    )
                }

                currentY += fieldSpacing
            } else {
                // Not enough space for more fields, draw "..." to indicate truncation
                if (currentY + fieldSpacing <= rect.bottom - 5.dp.toPx() * zoomLevel) {
                    val ellipsisResult = textMeasurer.measure("...", fieldStyle)
                    if (leftMargin + ellipsisResult.size.width <= size.width &&
                        currentY + ellipsisResult.size.height <= size.height
                    ) {
                        drawText(
                            textLayoutResult = ellipsisResult,
                            topLeft = Offset(leftMargin, currentY)
                        )
                    }
                }
                return // Stop drawing fields
            }
        }
    }

    /**
     * Truncate field text to fit within the specified width
     */
    private fun truncateFieldText(
        text: String,
        textMeasurer: TextMeasurer,
        style: TextStyle,
        maxWidth: Float
    ): String {
        if (text.length <= 3) return text

        // Try progressively shorter versions until it fits
        for (length in text.length - 1 downTo 1) {
            val truncated = text.take(length) + "..."
            val measuredWidth = textMeasurer.measure(truncated, style).size.width
            if (measuredWidth <= maxWidth) {
                return truncated
            }
        }

        return "..." // Fallback if even "..." doesn't fit
    }

    /**
     * Ellipsize package name to show only the last 4 segments if it's longer
     */
    private fun ellipsizePackageName(packageName: String): String {
        val segments = packageName.split(".")
        return if (segments.size > 4) {
            "...${segments.takeLast(4).joinToString(".")}"
        } else {
            packageName
        }
    }

    override fun drawRelation(
        drawScope: DrawScope,
        positionedRelation: PositionedRelation,
        textMeasurer: TextMeasurer,
        zoomLevel: Float
    ) {
        with(drawScope) {
            val pathPoints = positionedRelation.pathPoints
            val relationType = positionedRelation.relation.type

            // Safety check - need at least 2 points
            if (pathPoints.size < 2) return

            // Draw the path based on relation type
            when (relationType) {
                RelationType.subclass -> {
                    // Solid line for inheritance
                    drawPath(pathPoints, Color.Black, 1.dp.toPx())
                    val startPoint = if (pathPoints.size > 1) pathPoints[pathPoints.size - 2] else pathPoints.first()
                    val endPoint = pathPoints.last()
                    drawInheritanceArrow(this, startPoint, endPoint)
                }

                RelationType.encapsulates -> {
                    // Solid line for composition
                    drawPath(pathPoints, Color.Black, 1.dp.toPx())
                    val startPoint = pathPoints.first()
                    val endPoint = if (pathPoints.size > 1) pathPoints[1] else pathPoints.last()
                    drawCompositionArrow(this, startPoint, endPoint)
                }

                RelationType.implementation -> {
                    // Dashed line for implementation
                    drawPath(pathPoints, Color.Black, 1.dp.toPx(), PathEffect.dashPathEffect(floatArrayOf(10f, 5f)))
                    val startPoint = if (pathPoints.size > 1) pathPoints[pathPoints.size - 2] else pathPoints.first()
                    val endPoint = pathPoints.last()
                    drawImplementationArrow(this, startPoint, endPoint)
                }

                else -> {
                    // Default solid line
                    drawPath(pathPoints, Color.Black, 1.dp.toPx())
                    val startPoint = if (pathPoints.size > 1) pathPoints[pathPoints.size - 2] else pathPoints.first()
                    val endPoint = pathPoints.last()
                    drawSimpleArrow(this, startPoint, endPoint)
                }
            }

            // Draw relation type label at the midpoint of the path
            val midPointIndex = pathPoints.size / 2
            val labelPosition = if (pathPoints.size > 2) {
                pathPoints[midPointIndex]
            } else {
                Offset(
                    (pathPoints.first().x + pathPoints.last().x) / 2,
                    (pathPoints.first().y + pathPoints.last().y) / 2
                )
            }

            val labelStyle = TextStyle(
                fontSize = (8 * zoomLevel).sp,
                color = Color.DarkGray
            )

            // Measure text first to ensure proper positioning
            val textResult = textMeasurer.measure(
                text = relationType.name,
                style = labelStyle
            )

            // Calculate safe position for the text label
            val textWidth = textResult.size.width.toFloat()
            val textHeight = textResult.size.height.toFloat()

            val safeX = maxOf(0f, labelPosition.x - textWidth / 2)
            val safeY = maxOf(0f, labelPosition.y - textHeight / 2)

            // Only draw text if we have valid positive dimensions and safe positioning
            if (textWidth > 0 && textHeight > 0 &&
                safeX >= 0 && safeY >= 0 &&
                safeX + textWidth <= size.width &&
                safeY + textHeight <= size.height
            ) {

                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(safeX, safeY)
                )
            }
        }
    }

    /**
     * Helper method to draw a path through multiple points
     */
    private fun DrawScope.drawPath(
        points: List<Offset>,
        color: Color,
        strokeWidth: Float,
        pathEffect: PathEffect? = null
    ) {
        if (points.size < 2) return

        for (i in 0 until points.size - 1) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = strokeWidth,
                pathEffect = pathEffect
            )
        }
    }

    override fun drawInheritanceArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    ) {
        with(drawScope) {
            val arrowSize = 12.dp.toPx()
            val angle = atan2(end.y - start.y, end.x - start.x)

            // Draw open triangle (hollow arrowhead) for inheritance
            val arrowPath = Path().apply {
                moveTo(end.x, end.y)
                lineTo(
                    end.x - arrowSize * cos(angle - PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle - PI / 6).toFloat()
                )
                lineTo(
                    end.x - arrowSize * cos(angle + PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle + PI / 6).toFloat()
                )
                close()
            }

            // Draw hollow triangle with white fill and black stroke
            drawPath(
                path = arrowPath,
                color = Color.White
            )
            drawPath(
                path = arrowPath,
                color = Color.Black,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }

    override fun drawCompositionArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    ) {
        with(drawScope) {
            val diamondSize = 10.dp.toPx()
            val angle = atan2(end.y - start.y, end.x - start.x)

            // Create open diamond for aggregation/encapsulation
            val diamondPath = Path().apply {
                val centerX = start.x + diamondSize * cos(angle)
                val centerY = start.y + diamondSize * sin(angle)

                moveTo(start.x, start.y)
                lineTo(
                    centerX - diamondSize / 2 * cos(angle + PI / 2).toFloat(),
                    centerY - diamondSize / 2 * sin(angle + PI / 2).toFloat()
                )
                lineTo(
                    start.x + 2 * diamondSize * cos(angle),
                    start.y + 2 * diamondSize * sin(angle)
                )
                lineTo(
                    centerX + diamondSize / 2 * cos(angle + PI / 2).toFloat(),
                    centerY + diamondSize / 2 * sin(angle + PI / 2).toFloat()
                )
                close()
            }

            // Draw hollow diamond with white fill and black stroke
            drawPath(
                path = diamondPath,
                color = Color.White
            )
            drawPath(
                path = diamondPath,
                color = Color.Black,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }

    // Add new method for implementation arrows
    override fun drawImplementationArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    ) {
        with(drawScope) {
            val arrowSize = 12.dp.toPx()
            val angle = atan2(end.y - start.y, end.x - start.x)

            // Draw open triangle (hollow arrowhead) for implementation - same as inheritance
            val arrowPath = Path().apply {
                moveTo(end.x, end.y)
                lineTo(
                    end.x - arrowSize * cos(angle - PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle - PI / 6).toFloat()
                )
                lineTo(
                    end.x - arrowSize * cos(angle + PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle + PI / 6).toFloat()
                )
                close()
            }

            // Draw hollow triangle with white fill and black stroke
            drawPath(
                path = arrowPath,
                color = Color.White
            )
            drawPath(
                path = arrowPath,
                color = Color.Black,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }

    override fun drawSimpleArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    ) {
        with(drawScope) {
            val arrowSize = 10.dp.toPx()
            val angle = atan2(end.y - start.y, end.x - start.x)

            // Draw simple open arrowhead
            drawLine(
                color = Color.Black,
                start = end,
                end = Offset(
                    end.x - arrowSize * cos(angle - PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle - PI / 6).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )

            drawLine(
                color = Color.Black,
                start = end,
                end = Offset(
                    end.x - arrowSize * cos(angle + PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle + PI / 6).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )
        }
    }


}
