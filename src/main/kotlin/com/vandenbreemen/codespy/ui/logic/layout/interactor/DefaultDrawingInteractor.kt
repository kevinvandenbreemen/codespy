package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class DefaultDrawingInteractor : IDrawingInteractor {

    override fun drawSimpleArrow(
        drawScope: DrawScope,
        start: Offset, end: Offset
    ) {
        drawScope.apply {
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
    }

}