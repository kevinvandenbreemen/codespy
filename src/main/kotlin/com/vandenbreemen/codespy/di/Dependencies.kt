package com.vandenbreemen.com.vandenbreemen.codespy.di

import com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor
import com.vandenbreemen.com.vandenbreemen.codespy.viewmodel.CodeSpyViewModel

/**
 * Not gonna worry about trying to import some new DI framework.  Just gonna write my own simple
 * stuff for now
 */
class Dependencies {

    companion object {
        val main = Dependencies()
    }

    private val grucdInteractor: GrucdInteractor by lazy {
        GrucdInteractor()
    }

    fun grucdInteractor() = grucdInteractor
    fun codeSpyViewModel() = CodeSpyViewModel(grucdInteractor())

}