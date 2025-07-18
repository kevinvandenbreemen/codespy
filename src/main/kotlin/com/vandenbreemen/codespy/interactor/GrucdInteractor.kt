package com.vandenbreemen.com.vandenbreemen.codespy.interactor

import com.vandenbreemen.grucd.builder.SourceCodeExtractor
import java.io.File

class GrucdInteractor {

    //  TODO Need Cache of source code extractors for the past 3 requests


    fun getSourceCodeFiles(path: File): List<String>  {

        val extractor = SourceCodeExtractor().detectFileDeltas()
        return extractor.getFilenamesToVisit(null, path.absolutePath)

    }

}