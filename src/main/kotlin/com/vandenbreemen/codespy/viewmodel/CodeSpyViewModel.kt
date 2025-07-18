package com.vandenbreemen.com.vandenbreemen.codespy.viewmodel

import com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor
import java.io.File

class CodeSpyViewModel(private val grucdInteractor: GrucdInteractor) {

    fun selectNewDirectory(path: File) {
        grucdInteractor.getSourceCodeFiles(path)
    }

}