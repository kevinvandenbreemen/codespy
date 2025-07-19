package com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor

import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.UMLDiagramLayoutModel
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type

interface IUMLDiagramLayoutLogicInteractor {

    fun computeLayoutModel(types: List<Type>, overarchingSoftwareSystemModel: Model): UMLDiagramLayoutModel

}