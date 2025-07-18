package com.vandenbreemen.com.vandenbreemen.codespy.interactor

import com.vandenbreemen.grucd.builder.SourceCodeExtractor
import com.vandenbreemen.grucd.model.Model
import java.io.File

class GrucdInteractor {

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

}