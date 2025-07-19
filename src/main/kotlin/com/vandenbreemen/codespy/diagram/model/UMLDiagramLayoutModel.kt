package com.vandenbreemen.com.vandenbreemen.codespy.diagram.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.vandenbreemen.grucd.model.Type
import com.vandenbreemen.grucd.model.TypeRelation

data class PositionedType(
    val type: Type,
    val position: Offset,
    val size: Size = Size(150f, 100f)
)

data class PositionedRelation(
    val relation: TypeRelation,
    val pathPoints: List<Offset>
) {
    // Convenience properties for backward compatibility
    val startPosition: Offset get() = pathPoints.first()
    val endPosition: Offset get() = pathPoints.last()

    // Helper constructor for simple two-point relations
    constructor(relation: TypeRelation, startPosition: Offset, endPosition: Offset) :
            this(relation, listOf(startPosition, endPosition))
}

class UMLDiagramLayoutModel {
    val positionedTypes: MutableList<PositionedType> = mutableListOf()
    val positionedRelations: MutableList<PositionedRelation> = mutableListOf()

    // Diagram dimensions
    var width: Float = 0f
        private set
    var height: Float = 0f
        private set

    fun addPositionedType(positionedType: PositionedType) {
        positionedTypes.add(positionedType)
        updateDimensions()
    }

    fun addPositionedRelation(positionedRelation: PositionedRelation) {
        positionedRelations.add(positionedRelation)
        updateDimensions()
    }

    fun clear() {
        positionedTypes.clear()
        positionedRelations.clear()
        width = 0f
        height = 0f
    }

    /**
     * Calculate optimal diagram dimensions based on positioned elements
     */
    private fun updateDimensions() {
        if (positionedTypes.isEmpty()) {
            width = 0f
            height = 0f
            return
        }

        val padding = 50f // Minimum padding around the diagram

        // Find bounds from positioned types
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        positionedTypes.forEach { positionedType ->
            val left = positionedType.position.x
            val top = positionedType.position.y
            val right = left + positionedType.size.width
            val bottom = top + positionedType.size.height

            minX = minOf(minX, left)
            minY = minOf(minY, top)
            maxX = maxOf(maxX, right)
            maxY = maxOf(maxY, bottom)
        }

        // Also consider relation path points to ensure they fit
        positionedRelations.forEach { relation ->
            relation.pathPoints.forEach { point ->
                minX = minOf(minX, point.x)
                minY = minOf(minY, point.y)
                maxX = maxOf(maxX, point.x)
                maxY = maxOf(maxY, point.y)
            }
        }

        // Calculate dimensions with padding
        width = maxOf(800f, maxX - minX + 2 * padding) // Minimum width of 800dp
        height = maxOf(600f, maxY - minY + 2 * padding) // Minimum height of 600dp
    }

    /**
     * Force recalculation of dimensions (useful after layout changes)
     */
    fun recalculateDimensions() {
        updateDimensions()
    }
}