package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

import androidx.compose.ui.geometry.Offset
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.PositionedType
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type

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
                val startPos = Offset(
                    fromType.position.x + fromType.size.width / 2,
                    fromType.position.y + fromType.size.height / 2
                )
                val endPos = Offset(
                    toType.position.x + toType.size.width / 2,
                    toType.position.y + toType.size.height / 2
                )
                layoutModel.addPositionedRelation(PositionedRelation(relation, startPos, endPos))
            }
        }

        return layoutModel
    }

}