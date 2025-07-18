package com.vandenbreemen.codespy.ui

import androidx.compose.runtime.Composable
import com.vandenbreemen.grucd.model.Model
import androidx.compose.material.Text

@Composable
fun ModelRendering(model: Model?) {
    if (model == null) {
        Text("No model to display.")
    } else {
        // Replace with actual rendering logic for your model
        Text("Model loaded: ${model.javaClass.simpleName}")
    }
}

