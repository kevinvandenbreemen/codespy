package com.vandenbreemen.com.vandenbreemen.codespy.di

/**
 * Not gonna worry about trying to import some new DI framework.  Just gonna write my own simple
 * stuff for now
 */
class Dependencies {

    companion object {
        val main = Dependencies()
    }

    private val grucdInteractor: com.vandenbreemen.com.vandenbreemen.codespy.interactor.GrucdInteractor by lazy {
        grucdInteractor()
    }

    fun grucdInteractor() = grucdInteractor
    fun codeSpyViewModel() = com.vandenbreemen.com.vandenbreemen.codespy.viewmodel.CodeSpyViewModel(grucdInteractor)

}