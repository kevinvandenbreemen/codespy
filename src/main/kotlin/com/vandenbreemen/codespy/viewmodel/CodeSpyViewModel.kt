package com.vandenbreemen.com.vandenbreemen.codespy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.model.UMLDiagramAction
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.viewmodel.ModelRenderingViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor
import com.vandenbreemen.com.vandenbreemen.codespy.interactor.UserPreferenceInteractor
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CodeSpyViewModel(
    private val grucdInteractor: GrucdInteractor,
    private val userPreferenceInteractor: UserPreferenceInteractor
) {
    private val _directoryMessage = mutableStateOf("")
    val directoryMessage: State<String> = _directoryMessage
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private var currentDirectory: File? = null

    private var model: Model? = null
    private val _modelState = mutableStateOf<Model?>(null)
    val modelState: State<Model?> = _modelState

    private val _isLoadingState = mutableStateOf(false)
    val isLoadingState: State<Boolean> = _isLoadingState

    private var modelRenderingViewModel: ModelRenderingViewModel? = null
    private val _renderingViewModelState = mutableStateOf<ModelRenderingViewModel?>(null)
    val renderingViewModelState: State<ModelRenderingViewModel?> = _renderingViewModelState

    // Popup menu state
    private val _showPopupMenu = mutableStateOf(false)
    val showPopupMenu: State<Boolean> = _showPopupMenu

    private val _popupMenuOffset = mutableStateOf(Offset.Zero)
    val popupMenuOffset: State<Offset> = _popupMenuOffset

    private val _selectedTypeForPopup = mutableStateOf<Type?>(null)
    val selectedTypeForPopup: State<Type?> = _selectedTypeForPopup

    fun selectNewDirectory(path: File) {
        viewModelScope.launch {
            val files = grucdInteractor.getSourceCodeFiles(path)
            _directoryMessage.value = if (files.isNotEmpty()) {
                "Software System in directory: ${path.absolutePath}"
            } else {
                "No parsable files found in the selected directory."
            }

            if(files.isNotEmpty()) {    //  Only update current directory if we found files

                _isLoadingState.value = true

                currentDirectory = path

                // Store the parent directory for future use
                userPreferenceInteractor.storeLastParentDirectory(path)

                //  Now build model
                val generatedModel = grucdInteractor.getModel(path)
                model = generatedModel
                _modelState.value = generatedModel

                //  Set up rendering view model
                modelRenderingViewModel = ModelRenderingViewModel(generatedModel, Dependencies.main.layoutInteractor())
                _renderingViewModelState.value = modelRenderingViewModel

                _isLoadingState.value = false
            } else {
                _modelState.value = null
            }


        }
    }

    fun onUserSelectedType(type: Type, onScreenOffset: Offset? = null) {

        //  There should never be a case where this is null, so force unwrapping here
        //  to make it easier to spot a bug!
        modelRenderingViewModel!!.focusOnType(type)

    }

    fun onUserSelectedTypeWithLevels(type: Type, levels: Int) {
        model?.let { currentModel ->
            val surroundingTypesModel = grucdInteractor.getSurroundingTypesFor(currentModel, type, levels)

            // Update the model state with the new surrounding types model
            _modelState.value = surroundingTypesModel
            model = surroundingTypesModel

            // Set up rendering view model for the new model
            modelRenderingViewModel =
                ModelRenderingViewModel(surroundingTypesModel, Dependencies.main.layoutInteractor())
            _renderingViewModelState.value = modelRenderingViewModel

            // Focus on the selected type in the new model
            modelRenderingViewModel!!.focusOnType(type)
        }
    }

    /**
     * Get the last parent directory the user worked with, useful for setting default locations
     * in file dialogs
     */
    fun getLastParentDirectory(): File? {
        return userPreferenceInteractor.getLastParentDirectory()
    }

    fun hasParentModel(): Boolean {
        return grucdInteractor.hasParentModel()
    }

    fun navigateToParentModel() {
        if (grucdInteractor.hasParentModel()) {
            val parentModel = grucdInteractor.getParentModel()

            // Update the model state with the parent model
            model = parentModel
            _modelState.value = parentModel

            // Set up rendering view model for the parent model
            modelRenderingViewModel = ModelRenderingViewModel(parentModel, Dependencies.main.layoutInteractor())
            _renderingViewModelState.value = modelRenderingViewModel
        }
    }

    // Popup menu functions
    fun showPopupMenuForType(type: Type, offset: Offset) {
        _selectedTypeForPopup.value = type
        _popupMenuOffset.value = offset
        _showPopupMenu.value = true
        // Also trigger the existing focus behavior
        onUserSelectedType(type)
    }

    fun hidePopupMenu() {
        _showPopupMenu.value = false
        _selectedTypeForPopup.value = null
    }

    /**
     * Get available actions for a type in the diagram
     */
    fun getAvailableActionsForType(type: Type): List<UMLDiagramAction> {
        // Determine which actions are available based on the type and current state
        val actions = mutableListOf<UMLDiagramAction>()

        // Focus is always available
        actions.add(UMLDiagramAction.FocusOnType)

        // Show surrounding types if we have relationships
        model?.let { currentModel ->
            if (currentModel.types.size > 1) { // Only show if there are other types to show relationships with
                actions.add(UMLDiagramAction.ShowSurroundingTypes)
            }
        }

        return actions
    }

    /**
     * Execute a diagram action for the selected type
     */
    fun executeAction(action: UMLDiagramAction) {
        selectedTypeForPopup.value?.let { type ->
            when (action) {
                UMLDiagramAction.FocusOnType -> {
                    // Focus on the type (already done in showPopupMenuForType, but we can do it again for clarity)
                    onUserSelectedType(type)
                    println("Focusing on type: ${type.name}")
                }

                UMLDiagramAction.ShowSurroundingTypes -> {
                    // Show surrounding types with 1 level depth
                    onUserSelectedTypeWithLevels(type, 1)
                    println("Showing surrounding types for: ${type.name}")
                }
            }
        }
        hidePopupMenu()
    }

    /**
     * Get display text for an action
     */
    fun getActionDisplayText(action: UMLDiagramAction): String {
        return when (action) {
            UMLDiagramAction.FocusOnType -> "Focus on Type"
            UMLDiagramAction.ShowSurroundingTypes -> "Show Surrounding Types"
        }
    }
}
