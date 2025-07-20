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
        root.setProperty(CoreOptions.SPACING_NODE_NODE, 150.0) // Increased for more space
        root.setProperty(CoreOptions.SPACING_EDGE_EDGE, 40.0)  // Increased for more space
        root.setProperty(CoreOptions.SPACING_EDGE_NODE, 60.0)  // Increased for more space

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

            // Create ports for better edge connections
            val topPort = ElkGraphUtil.createPort(node)
            topPort.identifier = "${type.name}_top"
            topPort.x = nodeSize.width.toDouble() / 2 - 5.0
            topPort.y = 0.0
            topPort.width = 5.0
            topPort.height = 5.0

            val bottomPort = ElkGraphUtil.createPort(node)
            bottomPort.identifier = "${type.name}_bottom"
            bottomPort.x = nodeSize.width.toDouble() / 2 - 5.0
            bottomPort.y = nodeSize.height.toDouble() - 10.0
            bottomPort.width = 5.0
            bottomPort.height = 5.0

            val leftPort = ElkGraphUtil.createPort(node)
            leftPort.identifier = "${type.name}_left"
            leftPort.x = 0.0
            leftPort.y = nodeSize.height.toDouble() / 2 - 5.0
            leftPort.width = 5.0
            leftPort.height = 5.0

            val rightPort = ElkGraphUtil.createPort(node)
            rightPort.identifier = "${type.name}_right"
            rightPort.x = nodeSize.width.toDouble() - 10.0
            rightPort.y = nodeSize.height.toDouble() / 2 - 5.0
            rightPort.width = 5.0
            rightPort.height = 5.0

            // Add labels for fields and methods (these will be rendered by the drawing logic)
            type.fields.forEach { field ->
                val label = ElkGraphUtil.createLabel(node)
                label.text = "${field.name}: ${field.typeName}"
            }
        }

        // Step 3: Create edges for relationships with port connections
        overarchingSoftwareSystemModel.relations.forEach { relation ->
            val sourceNode = typeToNodeMap[relation.from.name]
            val targetNode = typeToNodeMap[relation.to.name]

            if (sourceNode != null && targetNode != null) {
                val edge = ElkGraphUtil.createEdge(root)

                // Determine appropriate ports based on relation type and layout direction
                val (sourcePortId, targetPortId) = determinePortsForRelation(relation, sourceNode, targetNode)

                // Find and connect to specific ports
                val sourcePort = sourceNode.ports.find { it.identifier == sourcePortId }
                val targetPort = targetNode.ports.find { it.identifier == targetPortId }

                if (sourcePort != null && targetPort != null) {
                    edge.sources.add(sourcePort)
                    edge.targets.add(targetPort)
                } else {
                    // Fallback to node-level connection if ports not found
                    edge.sources.add(sourceNode)
                    edge.targets.add(targetNode)
                }

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
                        // Create path points using ELK's port-based routing
                        val pathPoints = mutableListOf<Offset>()

                        // Try to use ELK's edge sections with port information
                        if (elkEdge.sections.isNotEmpty()) {
                            // ELK has calculated routing - use it
                            elkEdge.sections.forEach { section ->
                                // Add start point from section
                                pathPoints.add(Offset(section.startX.toFloat(), section.startY.toFloat()))

                                // Add bend points
                                section.bendPoints.forEach { bendPoint ->
                                    pathPoints.add(Offset(bendPoint.x.toFloat(), bendPoint.y.toFloat()))
                                }

                                // Add end point from section
                                pathPoints.add(Offset(section.endX.toFloat(), section.endY.toFloat()))
                            }
                        } else {
                            // Fallback to port-based connections if ELK sections are empty
                            val (sourcePortId, targetPortId) = determinePortsForRelation(
                                originalRelation,
                                sourceNode,
                                targetNode
                            )

                            val sourcePort = sourceNode.ports.find { it.identifier == sourcePortId }
                            val targetPort = targetNode.ports.find { it.identifier == targetPortId }

                            if (sourcePort != null && targetPort != null) {
                                // Use actual port positions
                                val sourcePortPos = Offset(
                                    (sourceNode.x + sourcePort.x + sourcePort.width / 2).toFloat(),
                                    (sourceNode.y + sourcePort.y + sourcePort.height / 2).toFloat()
                                )
                                val targetPortPos = Offset(
                                    (targetNode.x + targetPort.x + targetPort.width / 2).toFloat(),
                                    (targetNode.y + targetPort.y + targetPort.height / 2).toFloat()
                                )

                                pathPoints.add(sourcePortPos)
                                pathPoints.add(targetPortPos)
                            } else {
                                // Final fallback to edge calculation
                                val sourceCenter = Offset(
                                    sourceNode.x.toFloat() + sourceNode.width.toFloat() / 2,
                                    sourceNode.y.toFloat() + sourceNode.height.toFloat() / 2
                                )
                                val targetCenter = Offset(
                                    targetNode.x.toFloat() + targetNode.width.toFloat() / 2,
                                    targetNode.y.toFloat() + targetNode.height.toFloat() / 2
                                )

                                val sourceEdgePoint = calculateEdgeConnectionPoint(
                                    sourceNode.x.toFloat(), sourceNode.y.toFloat(),
                                    sourceNode.width.toFloat(), sourceNode.height.toFloat(),
                                    sourceCenter, targetCenter
                                )

                                val targetEdgePoint = calculateEdgeConnectionPoint(
                                    targetNode.x.toFloat(), targetNode.y.toFloat(),
                                    targetNode.width.toFloat(), targetNode.height.toFloat(),
                                    targetCenter, sourceCenter
                                )

                                pathPoints.add(sourceEdgePoint)
                                pathPoints.add(targetEdgePoint)
                            }
                        }

                        val positionedRelation = PositionedRelation(originalRelation, pathPoints)
                        layoutModel.addPositionedRelation(positionedRelation)
                    }
                }
            }
        }

        return layoutModel
    }

    /**
     * Calculate the connection point on the edge of a box (node) for routing the line
     */
    private fun calculateEdgeConnectionPoint(
        nodeX: Float,
        nodeY: Float,
        nodeWidth: Float,
        nodeHeight: Float,
        sourceCenter: Offset,
        targetCenter: Offset
    ): Offset {
        // Calculate the center points of the source and target
        val sourceCenterX = sourceCenter.x
        val sourceCenterY = sourceCenter.y
        val targetCenterX = targetCenter.x
        val targetCenterY = targetCenter.y

        // Calculate the difference in position
        val deltaX = targetCenterX - sourceCenterX
        val deltaY = targetCenterY - sourceCenterY

        // Calculate the angle of the line
        val angle = Math.atan2(deltaY.toDouble(), deltaX.toDouble())

        // Calculate the edge connection point on the source node
        val connectionX = nodeX + nodeWidth / 2 + Math.cos(angle) * (nodeWidth / 2)
        val connectionY = nodeY + nodeHeight / 2 + Math.sin(angle) * (nodeHeight / 2)

        return Offset(connectionX.toFloat(), connectionY.toFloat())
    }

    /**
     * Determine appropriate source and target ports based on relation type and node positions
     */
    private fun determinePortsForRelation(
        relation: com.vandenbreemen.grucd.model.TypeRelation,
        sourceNode: ElkNode,
        targetNode: ElkNode
    ): Pair<String, String> {
        val sourceTypeName = relation.from.name
        val targetTypeName = relation.to.name

        // Calculate relative positions to determine best port selection
        val sourceCenterY = sourceNode.y + sourceNode.height / 2
        val targetCenterY = targetNode.y + targetNode.height / 2
        val sourceCenterX = sourceNode.x + sourceNode.width / 2
        val targetCenterX = targetNode.x + targetNode.width / 2

        // For hierarchical relationships (inheritance), prefer vertical connections
        return when (relation.type.name) {
            "subclass" -> {
                // Inheritance: child (source) connects from top, parent (target) from bottom
                if (sourceCenterY > targetCenterY) {
                    // Source is below target - source connects from top, target from bottom
                    Pair("${sourceTypeName}_top", "${targetTypeName}_bottom")
                } else {
                    // Source is above target - source connects from bottom, target from top
                    Pair("${sourceTypeName}_bottom", "${targetTypeName}_top")
                }
            }

            "encapsulates", "aggregates" -> {
                // Composition/Aggregation: prefer horizontal or best available connection
                when {
                    kotlin.math.abs(sourceCenterX - targetCenterX) > kotlin.math.abs(sourceCenterY - targetCenterY) -> {
                        // Horizontal relationship is stronger
                        if (sourceCenterX < targetCenterX) {
                            Pair("${sourceTypeName}_right", "${targetTypeName}_left")
                        } else {
                            Pair("${sourceTypeName}_left", "${targetTypeName}_right")
                        }
                    }

                    sourceCenterY > targetCenterY -> {
                        // Vertical relationship - source below target
                        Pair("${sourceTypeName}_top", "${targetTypeName}_bottom")
                    }

                    else -> {
                        // Vertical relationship - source above target
                        Pair("${sourceTypeName}_bottom", "${targetTypeName}_top")
                    }
                }
            }

            "implementation" -> {
                // Implementation: similar to inheritance but can be more flexible
                if (sourceCenterY > targetCenterY) {
                    Pair("${sourceTypeName}_top", "${targetTypeName}_bottom")
                } else {
                    Pair("${sourceTypeName}_bottom", "${targetTypeName}_top")
                }
            }

            else -> {
                // Default: choose based on relative position
                when {
                    kotlin.math.abs(sourceCenterX - targetCenterX) > kotlin.math.abs(sourceCenterY - targetCenterY) -> {
                        if (sourceCenterX < targetCenterX) {
                            Pair("${sourceTypeName}_right", "${targetTypeName}_left")
                        } else {
                            Pair("${sourceTypeName}_left", "${targetTypeName}_right")
                        }
                    }

                    sourceCenterY > targetCenterY -> {
                        Pair("${sourceTypeName}_top", "${targetTypeName}_bottom")
                    }

                    else -> {
                        Pair("${sourceTypeName}_bottom", "${targetTypeName}_top")
                    }
                }
            }
        }
    }
}
