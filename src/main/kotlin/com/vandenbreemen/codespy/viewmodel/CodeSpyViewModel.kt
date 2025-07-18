package com.vandenbreemen.com.vandenbreemen.codespy.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CodeSpyViewModel(private val grucdInteractor: GrucdInteractor) {
    private val _directoryMessage = mutableStateOf("")
    val directoryMessage: State<String> = _directoryMessage
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    fun selectNewDirectory(path: File) {
        viewModelScope.launch {
            val files = grucdInteractor.getSourceCodeFiles(path)
            _directoryMessage.value = if (files.isNotEmpty()) {
                "Found ${files.size} parsable file(s) in the selected directory."
            } else {
                "No parsable files found in the selected directory."
            }
        }
    }

}