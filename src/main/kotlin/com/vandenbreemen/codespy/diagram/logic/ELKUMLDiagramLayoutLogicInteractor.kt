package com.vandenbreemen.com.vandenbreemen.codespy.diagram.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedType
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import org.eclipse.elk.core.RecursiveGraphLayoutEngine
import org.eclipse.elk.core.options.CoreOptions
import org.eclipse.elk.core.options.Direction
import org.eclipse.elk.core.util.NullElkProgressMonitor
import org.eclipse.elk.graph.ElkNode
import org.eclipse.elk.graph.util.ElkGraphUtil

/**
 * UML Diagram Layout Logic Interactor that uses the ELK layout engine
 */
class ELKUMLDiagramLayoutLogicInteractor : IUMLDiagramLayoutLogicInteractor {
    override fun computeLayoutModel(
        types: List<Type>,
        overarchingSoftwareSystemModel: Model
    ): UMLDiagramLayoutModel {

        if (types.isEmpty()) {
            return UMLDiagramLayoutModel()
        }

        //  Step 1: Create ELK graph from types
        val root = ElkGraphUtil.createGraph()

        // Configure layout options
        root.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered")
        root.setProperty(CoreOptions.DIRECTION, Direction.DOWN)
        root.setProperty(CoreOptions.SPACING_NODE_NODE, 80.0)
        root.setProperty(CoreOptions.SPACING_EDGE_EDGE, 20.0)
        root.setProperty(CoreOptions.SPACING_EDGE_NODE, 30.0)

        // Map to keep track of types to nodes
        val typeToNodeMap = mutableMapOf<String, ElkNode>()

        // Step 2: Create nodes for each type
        types.forEach { type ->
            val node = ElkGraphUtil.createNode(root)
            node.identifier = type.name
            typeToNodeMap[type.name] = node

            // Calculate node size based on content
            val nodeSize = calculateNodeSize(type)
            node.width = nodeSize.width.toDouble()
            node.height = nodeSize.height.toDouble()

            // Add labels for fields and methods (these will be rendered by the drawing logic)
            type.fields.forEach { field ->
                val label = ElkGraphUtil.createLabel(node)
                label.text = "${field.name}: ${field.typeName}"
            }
        }

        // Step 3: Create edges for relationships
        overarchingSoftwareSystemModel.relations.forEach { relation ->
            val sourceNode = typeToNodeMap[relation.from.name]
            val targetNode = typeToNodeMap[relation.to.name]

            if (sourceNode != null && targetNode != null) {
                val edge = ElkGraphUtil.createEdge(root)
                edge.sources.add(sourceNode)
                edge.targets.add(targetNode)

                // Store relation type in edge identifier for later retrieval
                edge.identifier = "${relation.from.name}-${relation.to.name}-${relation.type.name}"
            }
        }

        // Step 4: Run ELK layout algorithm
        val layoutEngine = RecursiveGraphLayoutEngine()
        layoutEngine.layout(root, NullElkProgressMonitor())

        // Step 5: Convert ELK graph back to UMLDiagramLayoutModel
        return translateElkGraphToLayoutModel(root, types, overarchingSoftwareSystemModel, typeToNodeMap)
    }

    /**
     * Calculate optimal node size based on type content
     */
    private fun calculateNodeSize(type: Type): Size {
        val baseWidth = 180f
        val baseHeight = 60f

        // Calculate width based on longest text
        val typeNameLength = type.name.length
        val packageNameLength = type.pkg.length
        val maxFieldLength = type.fields.maxOfOrNull { "${it.name}: ${it.typeName}".length } ?: 0

        val maxTextLength = maxOf(typeNameLength, packageNameLength, maxFieldLength)
        val dynamicWidth = maxOf(baseWidth, maxTextLength * 8f) // Approximate character width

        // Calculate height based on number of fields
        val fieldCount = type.fields.size
        val dynamicHeight = baseHeight + (fieldCount * 20f) // 20px per field

        return Size(dynamicWidth, dynamicHeight)
    }

    /**
     * Translate the laid out ELK graph back to UMLDiagramLayoutModel
     */
    private fun translateElkGraphToLayoutModel(
        elkGraph: ElkNode,
        originalTypes: List<Type>,
        model: Model,
        typeToNodeMap: Map<String, ElkNode>
    ): UMLDiagramLayoutModel {
        val layoutModel = UMLDiagramLayoutModel()

        // Convert ELK nodes to PositionedTypes
        elkGraph.children.forEach { elkNode ->
            val typeName = elkNode.identifier
            val originalType = originalTypes.find { it.name == typeName }

            if (originalType != null) {
                val position = Offset(
                    elkNode.x.toFloat(),
                    elkNode.y.toFloat()
                )
                val size = Size(
                    elkNode.width.toFloat(),
                    elkNode.height.toFloat()
                )

                val positionedType = PositionedType(originalType, position, size)
                layoutModel.addPositionedType(positionedType)
            }
        }

        // Convert ELK edges to PositionedRelations
        elkGraph.containedEdges.forEach { elkEdge ->
            val edgeId = elkEdge.identifier ?: ""
            val parts = edgeId.split("-")

            if (parts.size >= 3) {
                val fromTypeName = parts[0]
                val toTypeName = parts[1]
                val relationType = parts[2]

                // Find the original relation
                val originalRelation = model.relations.find {
                    it.from.name == fromTypeName && it.to.name == toTypeName && it.type.name == relationType
                }

                if (originalRelation != null) {
                    val sourceNode: ElkNode? = typeToNodeMap[fromTypeName]
                    val targetNode: ElkNode? = typeToNodeMap[toTypeName]

                    if (sourceNode != null && targetNode != null) {
                        // Create path points based on ELK edge sections
                        val pathPoints = mutableListOf<Offset>()

                        // Start point (center of source node)
                        val startPoint = Offset(
                            sourceNode.x.toFloat() + sourceNode.width.toFloat() / 2,
                            sourceNode.y.toFloat() + sourceNode.height.toFloat() / 2
                        )
                        pathPoints.add(startPoint)

                        // Add bend points if any (ELK may create routing points)
                        elkEdge.sections.forEach { section ->
                            section.bendPoints.forEach { bendPoint ->
                                pathPoints.add(Offset(bendPoint.x.toFloat(), bendPoint.y.toFloat()))
                            }
                        }

                        // End point (center of target node)
                        val endPoint = Offset(
                            targetNode.x.toFloat() + targetNode.width.toFloat() / 2,
                            targetNode.y.toFloat() + targetNode.height.toFloat() / 2
                        )
                        pathPoints.add(endPoint)

                        val positionedRelation = PositionedRelation(originalRelation, pathPoints)
                        layoutModel.addPositionedRelation(positionedRelation)
                    }
                }
            }
        }

        return layoutModel
    }
}