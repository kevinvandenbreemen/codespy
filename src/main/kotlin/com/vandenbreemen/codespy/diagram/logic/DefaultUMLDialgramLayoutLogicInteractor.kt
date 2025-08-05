package com.vandenbreemen.com.vandenbreemen.codespy.diagram.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedRelation
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.PositionedType
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import kotlin.math.*

class DefaultUMLDialgramLayoutLogicInteractor : IUMLDiagramLayoutLogicInteractor {

    override fun computeLayoutModel(
        types: List<Type>,
        overarchingSoftwareSystemModel: Model,
        zoomLevel: Float
    ): UMLDiagramLayoutModel {
        //  This function will organize the types in the model into a layout model
        //  based on the relationships and dependencies between the types.
        //  It will return a UMLDiagramLayoutModel that can be used for rendering.

        val layoutModel = UMLDiagramLayoutModel()

        if (types.isEmpty()) {
            return layoutModel
        }

        // Calculate optimal grid layout based on number of types
        val typeCount = types.size
        val optimalColumns = calculateOptimalColumns(typeCount)

        // Dynamic spacing based on content and relationships
        val baseTypeSpacing = 250f // Increased to accommodate larger boxes
        val baseVerticalSpacing = 150f
        val relationshipPadding = 50f // Extra space for relationship routing

        val typeSpacing =
            baseTypeSpacing + if (overarchingSoftwareSystemModel.relations.isNotEmpty()) relationshipPadding else 0f
        val verticalSpacing =
            baseVerticalSpacing + if (overarchingSoftwareSystemModel.relations.isNotEmpty()) relationshipPadding else 0f

        // Position types in an optimized grid with dynamic sizing
        types.forEachIndexed { index, type ->
            val column = index % optimalColumns
            val row = index / optimalColumns
            val position = Offset(
                x = column * typeSpacing + 50f,
                y = row * verticalSpacing + 50f
            )

            // Calculate dynamic size based on type name and package name
            val dynamicSize = calculateOptimalBoxSize(type)
            layoutModel.addPositionedType(PositionedType(type, position, dynamicSize))
        }

        // Add relationships with collision-avoiding paths
        overarchingSoftwareSystemModel.relations.forEach { relation ->
            val fromType = layoutModel.positionedTypes.find { it.type == relation.from }
            val toType = layoutModel.positionedTypes.find { it.type == relation.to }

            if (fromType != null && toType != null) {
                val pathPoints = calculateCollisionFreePath(fromType, toType, layoutModel.positionedTypes)
                layoutModel.addPositionedRelation(PositionedRelation(relation, pathPoints))
            }
        }

        // Force final dimension calculation after all elements are added
        layoutModel.recalculateDimensions()

        return layoutModel
    }

    /**
     * Calculate optimal number of columns based on type count for better layout
     */
    private fun calculateOptimalColumns(typeCount: Int): Int {
        return when {
            typeCount <= 4 -> 2
            typeCount <= 9 -> 3
            typeCount <= 16 -> 4
            typeCount <= 25 -> 5
            else -> ceil(sqrt(typeCount.toDouble())).toInt()
        }
    }

    /**
     * Calculate a path that avoids colliding with other type boxes
     */
    private fun calculateCollisionFreePath(
        fromType: PositionedType,
        toType: PositionedType,
        allTypes: List<PositionedType>
    ): List<Offset> {
        val fromRect = Rect(fromType.position, fromType.size)
        val toRect = Rect(toType.position, toType.size)

        // Get all obstacle rectangles (excluding source and destination)
        val obstacles = allTypes
            .filter { it != fromType && it != toType }
            .map { Rect(it.position, it.size) }

        return findPathAroundObstacles(fromRect, toRect, obstacles)
    }

    /**
     * Find a path from source to destination that avoids obstacles
     */
    private fun findPathAroundObstacles(
        fromRect: Rect,
        toRect: Rect,
        obstacles: List<Rect>
    ): List<Offset> {
        val fromCenter = Offset(fromRect.center.x, fromRect.center.y)
        val toCenter = Offset(toRect.center.x, toRect.center.y)

        // Determine connection points based on relative positions
        val connectionPoints = calculateOptimalConnectionPoints(fromRect, toRect)
        val startPoint = connectionPoints.first
        val endPoint = connectionPoints.second

        // Try direct path first
        val directPath = listOf(startPoint, endPoint)
        if (!pathIntersectsObstacles(directPath, obstacles)) {
            return directPath
        }

        // If direct path intersects obstacles, find a route around them
        return findRouteAroundObstacles(startPoint, endPoint, fromRect, toRect, obstacles)
    }

    /**
     * Calculate optimal connection points based on the relative positions of the rectangles
     */
    private fun calculateOptimalConnectionPoints(fromRect: Rect, toRect: Rect): Pair<Offset, Offset> {
        val fromCenter = Offset(fromRect.center.x, fromRect.center.y)
        val toCenter = Offset(toRect.center.x, toRect.center.y)

        val dx = toCenter.x - fromCenter.x
        val dy = toCenter.y - fromCenter.y

        return when {
            abs(dx) > abs(dy) -> {
                if (dx > 0) {
                    // fromType is left of toType
                    Offset(fromRect.right, fromCenter.y) to Offset(toRect.left, toCenter.y)
                } else {
                    // fromType is right of toType
                    Offset(fromRect.left, fromCenter.y) to Offset(toRect.right, toCenter.y)
                }
            }
            else -> {
                if (dy > 0) {
                    // fromType is above toType
                    Offset(fromCenter.x, fromRect.bottom) to Offset(toCenter.x, toRect.top)
                } else {
                    // fromType is below toType
                    Offset(fromCenter.x, fromRect.top) to Offset(toCenter.x, toRect.bottom)
                }
            }
        }
    }

    /**
     * Find a route around obstacles using a simple orthogonal pathfinding approach
     */
    private fun findRouteAroundObstacles(
        start: Offset,
        end: Offset,
        fromRect: Rect,
        toRect: Rect,
        obstacles: List<Rect>
    ): List<Offset> {
        // Create expanded obstacles with padding to ensure lines don't get too close
        val padding = 20f
        val expandedObstacles = obstacles.map { obstacle ->
            Rect(
                left = obstacle.left - padding,
                top = obstacle.top - padding,
                right = obstacle.right + padding,
                bottom = obstacle.bottom + padding
            )
        }

        // Try different routing strategies
        val routingStrategies = listOf(
            { createLShapedPath(start, end) },
            { createDetourPath(start, end, expandedObstacles, above = true) },
            { createDetourPath(start, end, expandedObstacles, above = false) },
            { createSidewaysDetourPath(start, end, expandedObstacles, left = true) },
            { createSidewaysDetourPath(start, end, expandedObstacles, left = false) }
        )

        // Try each strategy and return the first one that doesn't intersect obstacles
        for (strategy in routingStrategies) {
            val path = strategy()
            if (!pathIntersectsObstacles(path, expandedObstacles)) {
                return path
            }
        }

        // If all strategies fail, return a basic L-shaped path
        return createLShapedPath(start, end)
    }

    /**
     * Create a simple L-shaped path
     */
    private fun createLShapedPath(start: Offset, end: Offset): List<Offset> {
        val midX = (start.x + end.x) / 2
        return listOf(
            start,
            Offset(midX, start.y),
            Offset(midX, end.y),
            end
        )
    }

    /**
     * Create a detour path that goes above or below obstacles
     */
    private fun createDetourPath(
        start: Offset,
        end: Offset,
        obstacles: List<Rect>,
        above: Boolean
    ): List<Offset> {
        if (obstacles.isEmpty()) return createLShapedPath(start, end)

        // Find the extreme Y coordinate of obstacles in the path area
        val minX = min(start.x, end.x)
        val maxX = max(start.x, end.x)
        val relevantObstacles = obstacles.filter { obstacle ->
            obstacle.right >= minX && obstacle.left <= maxX
        }

        if (relevantObstacles.isEmpty()) return createLShapedPath(start, end)

        val extremeY = if (above) {
            relevantObstacles.minOf { it.top } - 30f
        } else {
            relevantObstacles.maxOf { it.bottom } + 30f
        }

        return listOf(
            start,
            Offset(start.x, extremeY),
            Offset(end.x, extremeY),
            end
        )
    }

    /**
     * Create a detour path that goes to the left or right of obstacles
     */
    private fun createSidewaysDetourPath(
        start: Offset,
        end: Offset,
        obstacles: List<Rect>,
        left: Boolean
    ): List<Offset> {
        if (obstacles.isEmpty()) return createLShapedPath(start, end)

        // Find the extreme X coordinate of obstacles in the path area
        val minY = min(start.y, end.y)
        val maxY = max(start.y, end.y)
        val relevantObstacles = obstacles.filter { obstacle ->
            obstacle.bottom >= minY && obstacle.top <= maxY
        }

        if (relevantObstacles.isEmpty()) return createLShapedPath(start, end)

        val extremeX = if (left) {
            relevantObstacles.minOf { it.left } - 30f
        } else {
            relevantObstacles.maxOf { it.right } + 30f
        }

        return listOf(
            start,
            Offset(extremeX, start.y),
            Offset(extremeX, end.y),
            end
        )
    }

    /**
     * Check if a path intersects with any obstacles
     */
    private fun pathIntersectsObstacles(path: List<Offset>, obstacles: List<Rect>): Boolean {
        if (path.size < 2) return false

        for (i in 0 until path.size - 1) {
            val lineStart = path[i]
            val lineEnd = path[i + 1]

            for (obstacle in obstacles) {
                if (lineIntersectsRect(lineStart, lineEnd, obstacle)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if a line segment intersects with a rectangle
     */
    private fun lineIntersectsRect(lineStart: Offset, lineEnd: Offset, rect: Rect): Boolean {
        // Check if line endpoints are inside the rectangle
        if (rect.contains(lineStart) || rect.contains(lineEnd)) {
            return true
        }

        // Check intersection with each edge of the rectangle
        val rectEdges = listOf(
            rect.topLeft to rect.topRight,
            rect.topRight to rect.bottomRight,
            rect.bottomRight to rect.bottomLeft,
            rect.bottomLeft to rect.topLeft
        )

        return rectEdges.any { (edgeStart, edgeEnd) ->
            linesIntersect(lineStart, lineEnd, edgeStart, edgeEnd)
        }
    }

    /**
     * Check if two line segments intersect
     */
    private fun linesIntersect(
        line1Start: Offset,
        line1End: Offset,
        line2Start: Offset,
        line2End: Offset
    ): Boolean {
        val d1 = direction(line2Start, line2End, line1Start)
        val d2 = direction(line2Start, line2End, line1End)
        val d3 = direction(line1Start, line1End, line2Start)
        val d4 = direction(line1Start, line1End, line2End)

        return (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) ||
                (d1 == 0f && onSegment(line2Start, line1Start, line2End)) ||
                (d2 == 0f && onSegment(line2Start, line1End, line2End)) ||
                (d3 == 0f && onSegment(line1Start, line2Start, line1End)) ||
                (d4 == 0f && onSegment(line1Start, line2End, line1End))
    }

    private fun direction(a: Offset, b: Offset, c: Offset): Float {
        return (c.x - a.x) * (b.y - a.y) - (b.x - a.x) * (c.y - a.y)
    }

    private fun onSegment(p: Offset, q: Offset, r: Offset): Boolean {
        return q.x <= max(p.x, r.x) && q.x >= min(p.x, r.x) &&
                q.y <= max(p.y, r.y) && q.y >= min(p.y, r.y)
    }

    /**
     * Calculate optimal box size based on type name and package name length
     */
    private fun calculateOptimalBoxSize(type: Type): Size {
        val baseWidth = 150f
        val baseHeight = 100f

        // Calculate width based on type name length
        val typeName = type.name
        val typeNameLength = typeName.length

        // Ellipsized package name
        val packageSegments = type.pkg.split(".")
        val ellipsizedPackage = if (packageSegments.size > 4) {
            "...${packageSegments.takeLast(4).joinToString(".")}"
        } else {
            type.pkg
        }

        // Calculate required width based on text content
        // Approximate character width: 7px for title (12sp), 5px for package (8sp)
        val titleWidth = typeNameLength * 7f
        val packageWidth = ellipsizedPackage.length * 5f
        val maxTextWidth = maxOf(titleWidth, packageWidth)

        // Add padding (30px on each side) and ensure minimum width
        val calculatedWidth = maxOf(baseWidth, maxTextWidth + 60f)

        // Height adjustment for very long names that might wrap
        val heightAdjustment = if (typeNameLength > 20) 20f else 0f
        val calculatedHeight = baseHeight + heightAdjustment

        return Size(calculatedWidth, calculatedHeight)
    }
}