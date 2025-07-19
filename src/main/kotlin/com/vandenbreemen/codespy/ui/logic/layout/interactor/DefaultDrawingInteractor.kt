package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

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
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedType
import com.vandenbreemen.grucd.model.RelationType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class DefaultDrawingInteractor : IDrawingInteractor {


    override fun drawTypeBox(
        drawScope: DrawScope,
        positionedType: PositionedType,
        textMeasurer: TextMeasurer
    ) {
        with(drawScope) {
            val rect = Rect(
                offset = positionedType.position,
                size = positionedType.size
            )

            // Draw box background
            drawRect(
                color = Color.LightGray,
                topLeft = rect.topLeft,
                size = rect.size
            )

            // Draw box border
            drawRect(
                color = Color.Black,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw type name
            val titleStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            val packageStyle = TextStyle(
                fontSize = 10.sp,
                color = Color.DarkGray
            )

            // Measure and draw title
            val titleResult = textMeasurer.measure(
                text = positionedType.type.name,
                style = titleStyle
            )

            val titleX = rect.left + (rect.width - titleResult.size.width) / 2
            val titleY = rect.top + 15.dp.toPx()

            drawText(
                textMeasurer = textMeasurer,
                text = positionedType.type.name,
                style = titleStyle,
                topLeft = Offset(titleX, titleY)
            )

            // Draw package name
            val packageResult = textMeasurer.measure(
                text = positionedType.type.pkg,
                style = packageStyle
            )

            val packageX = rect.left + (rect.width - packageResult.size.width) / 2
            val packageY = titleY + titleResult.size.height + 5.dp.toPx()

            drawText(
                textMeasurer = textMeasurer,
                text = positionedType.type.pkg,
                style = packageStyle,
                topLeft = Offset(packageX, packageY)
            )
        }
    }

    override fun drawRelation(
        drawScope: DrawScope,
        positionedRelation: PositionedRelation,
        textMeasurer: TextMeasurer
    ) {
        with(drawScope) {
            val start = positionedRelation.startPosition
            val end = positionedRelation.endPosition
            val relationType = positionedRelation.relation.type

            // Draw the main line based on relation type
            when (relationType) {
                RelationType.subclass -> {
                    // Solid line for inheritance
                    drawLine(
                        color = Color.Black,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                    drawInheritanceArrow(this, start, end)
                }

                RelationType.encapsulates -> {
                    // Solid line for composition
                    drawLine(
                        color = Color.Black,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCompositionArrow(this, start, end)
                }

                RelationType.implementation -> {
                    // Dashed line for implementation
                    drawLine(
                        color = Color.Black,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                    drawImplementationArrow(this, start, end)
                }

                else -> {
                    // Default solid line
                    drawLine(
                        color = Color.Black,
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                    drawSimpleArrow(this, start, end)
                }
            }

            // Draw relation type label
            val midPoint = Offset(
                (start.x + end.x) / 2,
                (start.y + end.y) / 2
            )

            val labelStyle = TextStyle(
                fontSize = 8.sp,
                color = Color.DarkGray
            )

            drawText(
                textMeasurer = textMeasurer,
                text = relationType.name,
                style = labelStyle,
                topLeft = Offset(midPoint.x - 20, midPoint.y - 10)
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
                style = Stroke(width = 2.dp.toPx())
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

            // Create filled diamond for composition
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

            // Draw filled black diamond
            drawPath(
                path = diamondPath,
                color = Color.Black
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
                style = Stroke(width = 2.dp.toPx())
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
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = Color.Black,
                start = end,
                end = Offset(
                    end.x - arrowSize * cos(angle + PI / 6).toFloat(),
                    end.y - arrowSize * sin(angle + PI / 6).toFloat()
                ),
                strokeWidth = 2.dp.toPx()
            )
        }
    }


}
