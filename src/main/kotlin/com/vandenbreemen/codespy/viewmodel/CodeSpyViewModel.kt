package com.vandenbreemen.com.vandenbreemen.codespy.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor
import com.vandenbreemen.grucd.model.Model
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CodeSpyViewModel(private val grucdInteractor: GrucdInteractor) {
    private val _directoryMessage = mutableStateOf("")
    val directoryMessage: State<String> = _directoryMessage
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private var currentDirectory: File? = null

    private var model: Model? = null

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
                model = grucdInteractor.getModel(path)
            }


        }
    }

}