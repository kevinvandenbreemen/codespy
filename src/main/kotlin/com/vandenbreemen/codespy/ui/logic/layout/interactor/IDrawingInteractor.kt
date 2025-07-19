package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedType

interface IDrawingInteractor {
    fun drawTypeBox(
        drawScope: DrawScope,
        positionedType: PositionedType,
        textMeasurer: TextMeasurer
    )

    fun drawRelation(
        drawScope: DrawScope,
        positionedRelation: PositionedRelation,
        textMeasurer: TextMeasurer
    )

    fun drawInheritanceArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    )

    fun drawCompositionArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    )

    fun drawImplementationArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    )

    fun drawSimpleArrow(
        drawScope: DrawScope,
        start: Offset,
        end: Offset
    )
}