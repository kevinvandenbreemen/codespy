package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout

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

    fun addPositionedType(positionedType: PositionedType) {
        positionedTypes.add(positionedType)
    }

    fun addPositionedRelation(positionedRelation: PositionedRelation) {
        positionedRelations.add(positionedRelation)
    }

    fun clear() {
        positionedTypes.clear()
        positionedRelations.clear()
    }
}