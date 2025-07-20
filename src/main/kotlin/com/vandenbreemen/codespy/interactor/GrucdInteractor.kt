package com.vandenbreemen.com.vandenbreemen.codespy.interactor

import com.vandenbreemen.grucd.builder.SourceCodeExtractor
import com.vandenbreemen.grucd.model.Model
import com.vandenbreemen.grucd.model.Type
import java.io.File
import java.util.*

class GrucdInteractor {

    // Stack of models
    private val modelStack: Stack<Model> = Stack()

    private var extractor: SourceCodeExtractor? = null

    fun getSourceCodeFiles(path: File): List<String>  {

        val extractor = SourceCodeExtractor().detectFileDeltas()
        return extractor.getFilenamesToVisit(null, path.absolutePath).also {
            if(it.isNotEmpty()) {
                this.extractor = extractor
            }
        }

    }

    fun getModel(path: File): Model {
        return extractor?.updateModelWithFileChanges(path.absolutePath)
            ?: throw IllegalStateException("Extractor is not initialized. Call getSourceCodeFiles() first.")
    }

    fun getSurroundingTypesFor(model: Model, type: Type, numLevels: Int = 3): Model {

        model.getTypesReferencingOrReferencedBy(type, numLevels).let { resultingModel ->
            modelStack.push(model)

            return resultingModel
        }

    }

    fun hasParentModel(): Boolean {
        return modelStack.isNotEmpty()
    }

    fun getParentModel(): Model {
        return modelStack.pop()
            ?: throw IllegalStateException("No parent model available in stack - please call hasParentModel() first")
    }

}