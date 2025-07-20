package com.vandenbreemen.com.vandenbreemen.codespy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
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

    private var modelRenderingViewModel: ModelRenderingViewModel? = null
    private val _renderingViewModelState = mutableStateOf<ModelRenderingViewModel?>(null)
    val renderingViewModelState: State<ModelRenderingViewModel?> = _renderingViewModelState

    fun selectNewDirectory(path: File) {
        viewModelScope.launch {
            val files = grucdInteractor.getSourceCodeFiles(path)
            _directoryMessage.value = if (files.isNotEmpty()) {
                "Software System in directory: ${path.absolutePath}"
            } else {
                "No parsable files found in the selected directory."
            }

            if(files.isNotEmpty()) {    //  Only update current directory if we found files
                currentDirectory = path

                //  Now build model
                val generatedModel = grucdInteractor.getModel(path)
                model = generatedModel
                _modelState.value = generatedModel

                //  Set up rendering view model
                modelRenderingViewModel = ModelRenderingViewModel(generatedModel, Dependencies.main.layoutInteractor())
                _renderingViewModelState.value = modelRenderingViewModel
            } else {
                _modelState.value = null
            }


        }
    }

    fun onUserSelectedType(type: Type) {

        //  There should never be a case where this is null, so force unwrapping here
        //  to make it easier to spot a bug!
        modelRenderingViewModel!!.focusOnType(type)

    }

}