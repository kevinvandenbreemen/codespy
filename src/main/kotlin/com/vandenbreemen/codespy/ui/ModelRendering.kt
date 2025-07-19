package com.vandenbreemen.codespy.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.TypeLayoutLogicViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.ui.logic.layout.interactor.IDrawingInteractor

@Composable
fun ModelRendering(
    modifier: Modifier = Modifier,
    typeLayoutLogicViewModel: TypeLayoutLogicViewModel,
    drawingInteractor: IDrawingInteractor = Dependencies.main.drawingInteractor()
) {
    val layoutModel by typeLayoutLogicViewModel.modelState
    val textMeasurer = rememberTextMeasurer()

    // Trigger layout computation when the composable is first composed
    LaunchedEffect(typeLayoutLogicViewModel) {
        typeLayoutLogicViewModel.computeLayoutForModel()
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Draw relationships first (so they appear behind the boxes)
            layoutModel.positionedRelations.forEach { relation ->
                drawingInteractor.drawRelation(this, relation, textMeasurer)
            }

            // Draw type boxes on top
            layoutModel.positionedTypes.forEach { positionedType ->
                drawingInteractor.drawTypeBox(this, positionedType, textMeasurer)
            }
        }
    }
}

