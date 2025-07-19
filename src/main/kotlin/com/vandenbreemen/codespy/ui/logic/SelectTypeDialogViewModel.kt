package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type

class SelectTypeDialogViewModel(private val model: Model) {


    //  State with the list of visible Types
    private val _visibleTypes = mutableStateOf<List<Type>>(emptyList())
    val visibleTypes: State<List<Type>> = _visibleTypes

    fun onUserInputChange(currentInput: String) {

        //  Filter visible types based on what the user typed
        val matchingTypes = model.types.filter { type -> type.name.contains(currentInput, true) }
        //  Update the state with the matching types

        _visibleTypes.value = matchingTypes

    }

}