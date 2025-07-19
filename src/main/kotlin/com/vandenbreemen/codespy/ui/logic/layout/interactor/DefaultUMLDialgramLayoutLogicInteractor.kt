package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedType
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import kotlin.math.abs

class DefaultUMLDialgramLayoutLogicInteractor : IUMLDiagramLayoutLogicInteractor {

    override fun computeLayoutModel(types: List<Type>, overarchingSoftwareSystemModel: Model): UMLDiagramLayoutModel {
        //  This function will organize the types in the model into a layout model
        //  based on the relationships and dependencies between the types.
        //  It will return a UMLDiagramLayoutModel that can be used for rendering.

        val layoutModel = UMLDiagramLayoutModel()

        // Simple grid layout for positioning types
        val gridColumns = 3
        val typeSpacing = 200f
        val verticalSpacing = 150f

        // Position types in a grid
        types.forEachIndexed { index, type ->
            val column = index % gridColumns
            val row = index / gridColumns
            val position = Offset(
                x = column * typeSpacing + 50f,
                y = row * verticalSpacing + 50f
            )
            layoutModel.addPositionedType(PositionedType(type, position))
        }

        // Add relationships
        overarchingSoftwareSystemModel.relations.forEach { relation ->
            val fromType = layoutModel.positionedTypes.find { it.type == relation.from }
            val toType = layoutModel.positionedTypes.find { it.type == relation.to }

            if (fromType != null && toType != null) {
                val (startPos, endPos) = calculateEdgeConnectionPoints(fromType, toType)
                layoutModel.addPositionedRelation(PositionedRelation(relation, startPos, endPos))
            }
        }

        return layoutModel
    }

    /**
     * Calculate connection points on the edges of two rectangles that are closest to each other
     */
    private fun calculateEdgeConnectionPoints(fromType: PositionedType, toType: PositionedType): Pair<Offset, Offset> {
        val fromRect = Rect(fromType.position, fromType.size)
        val toRect = Rect(toType.position, toType.size)

        // Calculate center points for direction determination
        val fromCenter = Offset(fromRect.center.x, fromRect.center.y)
        val toCenter = Offset(toRect.center.x, toRect.center.y)

        // Determine which edges are closest
        val dx = toCenter.x - fromCenter.x
        val dy = toCenter.y - fromCenter.y

        val fromEdgePoint: Offset
        val toEdgePoint: Offset

        when {
            // Horizontal connection (left-right or right-left)
            abs(dx) > abs(dy) -> {
                if (dx > 0) {
                    // fromType is left of toType
                    fromEdgePoint = Offset(fromRect.right, fromCenter.y)
                    toEdgePoint = Offset(toRect.left, toCenter.y)
                } else {
                    // fromType is right of toType
                    fromEdgePoint = Offset(fromRect.left, fromCenter.y)
                    toEdgePoint = Offset(toRect.right, toCenter.y)
                }
            }
            // Vertical connection (top-bottom or bottom-top)
            else -> {
                if (dy > 0) {
                    // fromType is above toType
                    fromEdgePoint = Offset(fromCenter.x, fromRect.bottom)
                    toEdgePoint = Offset(toCenter.x, toRect.top)
                } else {
                    // fromType is below toType
                    fromEdgePoint = Offset(fromCenter.x, fromRect.top)
                    toEdgePoint = Offset(toCenter.x, toRect.bottom)
                }
            }
        }

        return Pair(fromEdgePoint, toEdgePoint)
    }
}