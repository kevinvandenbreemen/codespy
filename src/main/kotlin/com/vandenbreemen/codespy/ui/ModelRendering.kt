package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.TypeLayoutLogicViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedType
import com.vandenbreemen.grucd.model.RelationType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModelRendering(typeLayoutLogicViewModel: TypeLayoutLogicViewModel) {
    val layoutModel by typeLayoutLogicViewModel.modelState
    val textMeasurer = rememberTextMeasurer()

    // Trigger layout computation when the composable is first composed
    LaunchedEffect(typeLayoutLogicViewModel) {
        typeLayoutLogicViewModel.computeLayoutForModel()
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Draw relationships first (so they appear behind the boxes)
            layoutModel.positionedRelations.forEach { relation ->
                drawRelation(relation, textMeasurer)
            }

            // Draw type boxes on top
            layoutModel.positionedTypes.forEach { positionedType ->
                drawTypeBox(positionedType, textMeasurer)
            }
        }
    }
}

private fun DrawScope.drawTypeBox(
    positionedType: PositionedType,
    textMeasurer: TextMeasurer
) {
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

private fun DrawScope.drawRelation(
    positionedRelation: PositionedRelation,
    textMeasurer: TextMeasurer
) {
    val start = positionedRelation.startPosition
    val end = positionedRelation.endPosition
    val relationType = positionedRelation.relation.type

    // Draw the main line
    drawLine(
        color = Color.Blue,
        start = start,
        end = end,
        strokeWidth = 2.dp.toPx()
    )

    // Draw arrowhead based on relation type
    when (relationType) {
        RelationType.subclass -> drawInheritanceArrow(start, end)
        RelationType.encapsulates -> drawCompositionArrow(start, end)
        else -> drawSimpleArrow(start, end)
    }

    // Draw relation type label
    val midPoint = Offset(
        (start.x + end.x) / 2,
        (start.y + end.y) / 2
    )

    val labelStyle = TextStyle(
        fontSize = 8.sp,
        color = Color.Blue
    )

    drawText(
        textMeasurer = textMeasurer,
        text = relationType.name,
        style = labelStyle,
        topLeft = Offset(midPoint.x - 20, midPoint.y - 10)
    )
}

private fun DrawScope.drawInheritanceArrow(start: Offset, end: Offset) {
    val arrowSize = 10.dp.toPx()
    val angle = atan2(end.y - start.y, end.x - start.x)

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

    drawPath(
        path = arrowPath,
        color = Color.Blue,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawCompositionArrow(start: Offset, end: Offset) {
    val arrowSize = 8.dp.toPx()
    val angle = atan2(end.y - start.y, end.x - start.x)

    val diamondPath = Path().apply {
        val tipX = end.x - arrowSize * cos(angle)
        val tipY = end.y - arrowSize * sin(angle)

        moveTo(end.x, end.y)
        lineTo(
            tipX - arrowSize / 2 * cos(angle + PI / 2).toFloat(),
            tipY - arrowSize / 2 * sin(angle + PI / 2).toFloat()
        )
        lineTo(
            end.x - 2 * arrowSize * cos(angle),
            end.y - 2 * arrowSize * sin(angle)
        )
        lineTo(
            tipX + arrowSize / 2 * cos(angle + PI / 2).toFloat(),
            tipY + arrowSize / 2 * sin(angle + PI / 2).toFloat()
        )
        close()
    }

    drawPath(
        path = diamondPath,
        color = Color.Blue
    )
}

private fun DrawScope.drawSimpleArrow(start: Offset, end: Offset) {
    val arrowSize = 10.dp.toPx()
    val angle = atan2(end.y - start.y, end.x - start.x)

    drawLine(
        color = Color.Blue,
        start = end,
        end = Offset(
            end.x - arrowSize * cos(angle - PI / 6).toFloat(),
            end.y - arrowSize * sin(angle - PI / 6).toFloat()
        ),
        strokeWidth = 2.dp.toPx()
    )

    drawLine(
        color = Color.Blue,
        start = end,
        end = Offset(
            end.x - arrowSize * cos(angle + PI / 6).toFloat(),
            end.y - arrowSize * sin(angle + PI / 6).toFloat()
        ),
        strokeWidth = 2.dp.toPx()
    )
}
