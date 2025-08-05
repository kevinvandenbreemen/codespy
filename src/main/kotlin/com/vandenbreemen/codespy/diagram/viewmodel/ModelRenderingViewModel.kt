package com.vandenbreemen.com.vandenbreemen.codespy.diagram.viewmodel

import androidx.compose.runtime.*
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.logic.IUMLDiagramLayoutLogicInteractor
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelRenderingViewModel(
    private val model: Model,
    private val layoutInteractor: IUMLDiagramLayoutLogicInteractor
) {

    companion object {
        const val MAX_ZOOM = 3.0f
        const val MIN_ZOOM = 0.2f
    }

    private val _layoutModelState: MutableState<UMLDiagramLayoutModel> = mutableStateOf(UMLDiagramLayoutModel())
    val modelState: State<UMLDiagramLayoutModel> = _layoutModelState

    // Track focused type for auto-scrolling
    private val _focusedType: MutableState<Type?> = mutableStateOf(null)
    val focusedType: State<Type?> = _focusedType

    // Track scroll position for the focused type
    private val _scrollToPosition: MutableState<Pair<Float, Float>?> = mutableStateOf(null)
    val scrollToPosition: State<Pair<Float, Float>?> = _scrollToPosition

    // Zoom level
    var zoomLevel: Float by mutableStateOf(1.0f)
        private set

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun focusOnType(type: Type) {
        println("Focusing on type (vm): ${type.name}")
        _focusedType.value = type

        // Calculate scroll position to center the type
        val layoutModel = _layoutModelState.value
        val positionedType = layoutModel.positionedTypes.find { it.type == type }

        positionedType?.let { positioned ->
            // Calculate center position of the type box
            val centerX = positioned.position.x + positioned.size.width / 2
            val centerY = positioned.position.y + positioned.size.height / 2

            _scrollToPosition.value = Pair(centerX, centerY)
        }
    }

    fun clearFocus() {
        _focusedType.value = null
        _scrollToPosition.value = null
    }

    fun computeLayoutForModel() {
        coroutineScope.launch {
            _layoutModelState.value = layoutInteractor.computeLayoutModel(
                types = model.types,
                overarchingSoftwareSystemModel = model,
                zoomLevel = zoomLevel
            )
        }
    }

    fun zoomIn() {
        zoomLevel = (zoomLevel * 1.1f).coerceAtMost(MAX_ZOOM)
    }

    fun zoomOut() {
        zoomLevel = (zoomLevel / 1.1f).coerceAtLeast(MIN_ZOOM)
    }

}