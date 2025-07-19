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

        // Add relationships with angled paths
        overarchingSoftwareSystemModel.relations.forEach { relation ->
            val fromType = layoutModel.positionedTypes.find { it.type == relation.from }
            val toType = layoutModel.positionedTypes.find { it.type == relation.to }

            if (fromType != null && toType != null) {
                val pathPoints = calculateAngledPath(fromType, toType)
                layoutModel.addPositionedRelation(PositionedRelation(relation, pathPoints))
            }
        }

        return layoutModel
    }

    /**
     * Calculate an angled path between two types that creates sharp turns
     */
    private fun calculateAngledPath(fromType: PositionedType, toType: PositionedType): List<Offset> {
        val fromRect = Rect(fromType.position, fromType.size)
        val toRect = Rect(toType.position, toType.size)

        // Calculate center points for direction determination
        val fromCenter = Offset(fromRect.center.x, fromRect.center.y)
        val toCenter = Offset(toRect.center.x, toRect.center.y)

        // Determine which edges are closest
        val dx = toCenter.x - fromCenter.x
        val dy = toCenter.y - fromCenter.y

        val pathPoints = mutableListOf<Offset>()

        when {
            // Horizontal connection with potential vertical detour
            abs(dx) > abs(dy) -> {
                if (dx > 0) {
                    // fromType is left of toType
                    val startPoint = Offset(fromRect.right, fromCenter.y)
                    val endPoint = Offset(toRect.left, toCenter.y)

                    // Add intermediate points for angled path
                    val midX = (startPoint.x + endPoint.x) / 2
                    val cornerPoint1 = Offset(midX, startPoint.y)
                    val cornerPoint2 = Offset(midX, endPoint.y)

                    pathPoints.addAll(listOf(startPoint, cornerPoint1, cornerPoint2, endPoint))
                } else {
                    // fromType is right of toType
                    val startPoint = Offset(fromRect.left, fromCenter.y)
                    val endPoint = Offset(toRect.right, toCenter.y)

                    val midX = (startPoint.x + endPoint.x) / 2
                    val cornerPoint1 = Offset(midX, startPoint.y)
                    val cornerPoint2 = Offset(midX, endPoint.y)

                    pathPoints.addAll(listOf(startPoint, cornerPoint1, cornerPoint2, endPoint))
                }
            }
            // Vertical connection with potential horizontal detour
            else -> {
                if (dy > 0) {
                    // fromType is above toType
                    val startPoint = Offset(fromCenter.x, fromRect.bottom)
                    val endPoint = Offset(toCenter.x, toRect.top)

                    val midY = (startPoint.y + endPoint.y) / 2
                    val cornerPoint1 = Offset(startPoint.x, midY)
                    val cornerPoint2 = Offset(endPoint.x, midY)

                    pathPoints.addAll(listOf(startPoint, cornerPoint1, cornerPoint2, endPoint))
                } else {
                    // fromType is below toType
                    val startPoint = Offset(fromCenter.x, fromRect.top)
                    val endPoint = Offset(toCenter.x, toRect.bottom)

                    val midY = (startPoint.y + endPoint.y) / 2
                    val cornerPoint1 = Offset(startPoint.x, midY)
                    val cornerPoint2 = Offset(endPoint.x, midY)

                    pathPoints.addAll(listOf(startPoint, cornerPoint1, cornerPoint2, endPoint))
                }
            }
        }

        return pathPoints
    }
}