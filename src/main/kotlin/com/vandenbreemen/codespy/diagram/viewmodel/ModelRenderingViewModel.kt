package com.vandenbreemen.com.vandenbreemen.codespy.diagram.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.logic.IUMLDiagramLayoutLogicInteractor
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelRenderingViewModel(private val model: Model) {

    private val layoutInteractor: IUMLDiagramLayoutLogicInteractor = Dependencies.Companion.main.layoutInteractor()

    private val _layoutModelState: MutableState<UMLDiagramLayoutModel> = mutableStateOf(UMLDiagramLayoutModel())
    val modelState: State<UMLDiagramLayoutModel> = _layoutModelState

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun computeLayoutForModel() {
        coroutineScope.launch {
            _layoutModelState.value = layoutInteractor.computeLayoutModel(
                types = model.types,
                overarchingSoftwareSystemModel = model
            )
        }
    }

}