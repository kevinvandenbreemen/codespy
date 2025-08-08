package com.vandenbreemen.codespy.diagram.view

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.vandenbreemen.com.vandenbreemen.codespy.di.Dependencies
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.view.renderlogic.IDrawingInteractor
import com.vandenbreemen.com.vandenbreemen.codespy.diagram.viewmodel.ModelRenderingViewModel
import com.vandenbreemen.com.vandenbreemen.codespy.viewmodel.CodeSpyViewModel
import com.vandenbreemen.grucd.model.Type
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ModelRendering(
    modifier: Modifier = Modifier,
    modelRenderingViewModel: ModelRenderingViewModel,
    drawingInteractor: IDrawingInteractor = Dependencies.main.drawingInteractor(),
    onTypeClick: (Type) -> Unit = {},
    codeSpyViewModel: CodeSpyViewModel? = null // Add CodeSpyViewModel parameter
) {
    val layoutModel by modelRenderingViewModel.modelState
    val focusedType by modelRenderingViewModel.focusedType
    val scrollToPosition by modelRenderingViewModel.scrollToPosition
    val textMeasurer = rememberTextMeasurer()

    // Use CodeSpyViewModel's popup menu state if available, otherwise use local state
    val showPopupMenu by (codeSpyViewModel?.showPopupMenu ?: remember { mutableStateOf(false) })
    val popupMenuOffset by (codeSpyViewModel?.popupMenuOffset ?: remember { mutableStateOf(Offset.Zero) })
    val selectedType by (codeSpyViewModel?.selectedTypeForPopup ?: remember { mutableStateOf<Type?>(null) })

    // Create scroll states for horizontal and vertical scrolling
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    // Handle auto-scrolling when a type is focused
    LaunchedEffect(scrollToPosition) {
        scrollToPosition?.let { (targetX, targetY) ->
            println("Auto-scrolling to position: ($targetX, $targetY)")

            // Calculate the scroll offsets to center the target position
            // Get the actual viewport size dynamically
            val viewportWidth = 800f // You might want to make this dynamic
            val viewportHeight = 600f // You might want to make this dynamic

            val horizontalOffset = (targetX - viewportWidth / 2).coerceAtLeast(0f)
            val verticalOffset = (targetY - viewportHeight / 2).coerceAtLeast(0f)

            println("Calculated scroll offsets: horizontal=$horizontalOffset, vertical=$verticalOffset")

            // Animate to the calculated scroll positions
            launch {
                horizontalScrollState.animateScrollTo(horizontalOffset.toInt())
            }
            launch {
                verticalScrollState.animateScrollTo(verticalOffset.toInt())
            }
        }
    }

    // Clear focus after a delay to allow scrolling to complete
    LaunchedEffect(focusedType) {
        focusedType?.let {
            // Clear the scroll position after a delay to allow scrolling animation to complete
            delay(1000) // 1 second delay
            modelRenderingViewModel.clearFocus()
        }
    }

    // Trigger layout computation when the composable is first composed or zoom changes
    LaunchedEffect(modelRenderingViewModel, modelRenderingViewModel.zoomLevel) {
        modelRenderingViewModel.computeLayoutForModel()
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        elevation = 4.dp
    ) {

        Column {
            // --- ZOOM BUTTONS ---
            Row(modifier = Modifier.padding(8.dp)) {
                Button(
                    onClick = { modelRenderingViewModel.zoomOut() },
                ) { Text("-") }
                Text(text = "  Zoom: %.1fx  ".format(modelRenderingViewModel.zoomLevel))
                Button(
                    onClick = { modelRenderingViewModel.zoomIn() },
                ) { Text("+") }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                        .verticalScroll(verticalScrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(
                                width = maxOf(800.dp, layoutModel.width.dp),
                                height = maxOf(600.dp, layoutModel.height.dp)
                            )
                            .background(Color.White)
                            .pointerInput(layoutModel) {
                                detectTapGestures { offset ->
                                    // Find the clicked type box
                                    val clickedType = layoutModel.positionedTypes.find { positionedType ->
                                        val rect = Rect(
                                            offset = positionedType.position,
                                            size = positionedType.size
                                        )
                                        rect.contains(offset)
                                    }

                                    // Show popup menu if a type was clicked
                                    clickedType?.let { positionedType ->
                                        // Calculate the popup position accounting for scroll offset
                                        val adjustedOffset = Offset(
                                            x = offset.x - horizontalScrollState.value,
                                            y = offset.y - verticalScrollState.value
                                        )
                                        // Use CodeSpyViewModel if available, otherwise fallback to callback
                                        codeSpyViewModel?.showPopupMenuForType(positionedType.type, adjustedOffset)
                                            ?: onTypeClick(positionedType.type)
                                    } ?: run {
                                        // Hide popup menu if clicked outside any type
                                        codeSpyViewModel?.hidePopupMenu()
                                    }
                                }
                            }
                    ) {
                        // Draw relationships first (so they appear behind the boxes)
                        layoutModel.positionedRelations.forEach { relation ->
                            drawingInteractor.drawRelation(
                                this,
                                relation,
                                textMeasurer,
                                zoomLevel = modelRenderingViewModel.zoomLevel
                            )
                        }

                        // Draw type boxes on top
                        layoutModel.positionedTypes.forEach { positionedType ->
                            // Highlight focused type
                            val isHighlighted = positionedType.type == focusedType
                            drawingInteractor.drawTypeBox(
                                this,
                                positionedType,
                                textMeasurer,
                                isHighlighted,
                                zoomLevel = modelRenderingViewModel.zoomLevel
                            )
                        }
                    }
                }

                // --- POPUP MENU ---
                selectedType?.let { type ->
                    DropdownMenu(
                        expanded = showPopupMenu,
                        onDismissRequest = { codeSpyViewModel?.hidePopupMenu() },
                        offset = DpOffset(popupMenuOffset.x.dp, popupMenuOffset.y.dp)
                    ) {
                        // Generate menu items based on available actions for this type
                        codeSpyViewModel?.getAvailableActionsForType(type)?.forEach { action ->
                            DropdownMenuItem(onClick = {
                                codeSpyViewModel.executeAction(action)
                            }) {
                                Text(codeSpyViewModel.getActionDisplayText(action))
                            }
                        }
                    }
                }
            }
        }
    }
}
