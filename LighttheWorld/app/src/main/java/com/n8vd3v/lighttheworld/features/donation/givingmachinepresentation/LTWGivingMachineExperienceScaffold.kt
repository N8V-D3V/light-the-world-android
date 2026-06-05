package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.n8vd3v.lighttheworld.CrispWhite
import com.n8vd3v.lighttheworld.MidnightBlue
import kotlinx.coroutines.delay

private val MachineHeaderCream = Color(0xFFF4E98C)
private val MachineBlue = Color(0xFF1639B8)
private val MachineHousingRed = Color(0xFFD30E10)
private val MachineHousingRedDark = Color(0xFF9D0608)
private val MachineViewportBlack = Color(0xFF1E1B1C)
private val MachineViewportBlackSoft = Color(0xFF2A2526)
private val MachineCardWhite = Color(0xFFFFFCFA)
private val MachineCardBorder = Color(0xFFD7D0CB)
private val MachineCardText = Color(0xFFB30D12)
private val MachineCardBody = Color(0xFF7E1217)
private val MachineGold = Color(0xFFFFF08B)
private val MachineCream = Color(0xFFF8F1E7)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LTWGivingMachineExperienceScaffold(
    modifier: Modifier = Modifier,
    controller: LTWGivingMachineUiController = remember { LTWGivingMachineUiController() },
    homeContent: @Composable () -> Unit,
) {
    val uiState = controller.uiState
    val destinationState = uiState.presentationState.givingMachineDestinationState
    val isExpandedDestination = destinationState?.sheetState == GivingMachineSheetState.EXPANDED
    val scaffoldState = rememberGivingMachineScaffoldState()

    LaunchedEffect(isExpandedDestination) {
        if (isExpandedDestination) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        when (scaffoldState.bottomSheetState.currentValue) {
            BottomSheetValue.Expanded -> {
                if (!isExpandedDestination) {
                    controller.openMachine(method = GivingMachineEntryMethod.SWIPE_UP)
                }
            }

            BottomSheetValue.Collapsed -> {
                if (isExpandedDestination) {
                    controller.dismissMachine(method = GivingMachineDismissMethod.SWIPE_DOWN)
                }
            }
        }
    }

    LaunchedEffect(uiState.pendingConfirmedAddHandoff) {
        val handoff = uiState.pendingConfirmedAddHandoff ?: return@LaunchedEffect
        delay(140)
        controller.acceptSuccessfulAddResult(handoff)
    }

    LaunchedEffect(uiState.presentationState.dispenseAnimationState?.presentationState) {
        if (uiState.presentationState.dispenseAnimationState?.presentationState ==
            DispenseAnimationPresentationState.PRESENTING
        ) {
            delay(620)
            controller.continuePresentation()
        }
    }

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 94.dp,
        sheetBackgroundColor = Color.Transparent,
        backgroundColor = CrispWhite,
        sheetGesturesEnabled = true,
        sheetElevation = 0.dp,
        sheetContent = {
            LTWGivingMachineSheet(
                uiState = uiState,
                onOpen = { controller.openMachine() },
                onDismiss = { controller.dismissMachine() },
                onBrowsePrevious = { controller.browseMachineWindow(MachineWindowBrowseDirection.PREVIOUS) },
                onBrowseNext = { controller.browseMachineWindow(MachineWindowBrowseDirection.NEXT) },
                onConfirmDetailAdd = controller::confirmGiftFromDetail,
                onOpenCart = controller::openCartOrCheckout,
                onOpenInfo = controller::openInfo,
                onReturnToMachine = {
                    when (uiState.presentationState.givingMachineDestinationState?.visibleContext) {
                        GivingMachineVisibleContext.CART_OR_CHECKOUT -> controller.returnFromCartOrCheckout()
                        GivingMachineVisibleContext.INFO -> controller.returnFromInfo()
                        else -> Unit
                    }
                },
            )
        },
    ) {
        homeContent()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun rememberGivingMachineScaffoldState(): BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed,
    ),
)

@Composable
private fun LTWGivingMachineSheet(
    uiState: LTWGivingMachineUiState,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onBrowsePrevious: () -> Unit,
    onBrowseNext: () -> Unit,
    onConfirmDetailAdd: (String) -> Unit,
    onOpenCart: () -> Unit,
    onOpenInfo: () -> Unit,
    onReturnToMachine: () -> Unit,
) {
    val accessibilityState = uiState.presentationState.accessibilityPresentationState
    val availableActions = accessibilityState?.availableActions.orEmpty()
    val isExpanded = uiState.presentationState.givingMachineDestinationState?.sheetState == GivingMachineSheetState.EXPANDED
    val visibleContext = uiState.presentationState.givingMachineDestinationState?.visibleContext
        ?: GivingMachineVisibleContext.MACHINE_BROWSE
    var detailItemIdentifier by rememberSaveable { mutableStateOf<String?>(null) }
    var detailSlotNumber by rememberSaveable { mutableStateOf<String?>(null) }
    val detailItem = uiState.environment.catalogContext.givingMachineCatalog
        .orEmpty()
        .firstOrNull { it.itemIdentifier == detailItemIdentifier }

    LaunchedEffect(isExpanded, visibleContext) {
        if (!isExpanded || visibleContext != GivingMachineVisibleContext.MACHINE_BROWSE) {
            detailItemIdentifier = null
            detailSlotNumber = null
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MachineHousingRedDark,
        contentColor = CrispWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MachineHousingRed,
                            MachineHousingRedDark,
                        ),
                    ),
                ),
        ) {
            LTWGivingMachinePeekHeader(
                label = accessibilityState?.entryLabel ?: "Giving Machine",
                isExpanded = isExpanded,
                visibleContext = visibleContext,
                canOpen = availableActions.contains(GivingMachineAccessibilityAction.OPEN_MACHINE),
                canDismiss = availableActions.contains(GivingMachineAccessibilityAction.DISMISS_MACHINE),
                onOpen = onOpen,
                onDismiss = onDismiss,
            )

            AnimatedVisibility(visible = isExpanded) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val bodyHeight = (maxHeight - 92.dp).coerceAtLeast(0.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bodyHeight)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        uiState.latestFailureMessage?.let { message ->
                            Text(
                                text = message,
                                color = MachineGold,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                textAlign = TextAlign.Center,
                            )
                        }

                        LTWGivingMachineContextBar(
                            visibleContext = visibleContext,
                            canOpenCart = availableActions.contains(
                                GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT,
                            ),
                            canOpenInfo = availableActions.contains(
                                GivingMachineAccessibilityAction.OPEN_INFO,
                            ),
                            onOpenCart = onOpenCart,
                            onOpenInfo = onOpenInfo,
                            onReturnToMachine = onReturnToMachine,
                        )

                        when (visibleContext) {
                            GivingMachineVisibleContext.MACHINE_BROWSE -> LTWMachineBrowsePane(
                                uiState = uiState,
                                canBrowsePrevious = availableActions.contains(
                                    GivingMachineAccessibilityAction.BROWSE_PREVIOUS,
                                ),
                                canBrowseNext = availableActions.contains(
                                    GivingMachineAccessibilityAction.BROWSE_NEXT,
                                ),
                                canOpenCart = availableActions.contains(
                                    GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT,
                                ),
                                canOpenInfo = availableActions.contains(
                                    GivingMachineAccessibilityAction.OPEN_INFO,
                                ),
                                onBrowsePrevious = onBrowsePrevious,
                                onBrowseNext = onBrowseNext,
                                onOpenItemDetail = { item ->
                                    detailItemIdentifier = item.itemIdentifier
                                    detailSlotNumber = item.slotNumber
                                },
                                onOpenCart = onOpenCart,
                                onOpenInfo = onOpenInfo,
                            )

                            GivingMachineVisibleContext.CART_OR_CHECKOUT -> LTWCartPane(
                                uiState = uiState,
                                onReturnToMachine = onReturnToMachine,
                            )

                            GivingMachineVisibleContext.INFO -> LTWInfoPane(
                                uiState = uiState,
                                onReturnToMachine = onReturnToMachine,
                            )
                        }
                    }
                }
            }
        }
    }

    if (detailItem != null && isExpanded && visibleContext == GivingMachineVisibleContext.MACHINE_BROWSE) {
        LTWGivingMachineItemDetailDialog(
            item = detailItem,
            onDismiss = {
                detailItemIdentifier = null
                detailSlotNumber = null
            },
            onAddToCart = {
                onConfirmDetailAdd(detailSlotNumber ?: detailItem.slotNumber)
                detailItemIdentifier = null
                detailSlotNumber = null
            },
        )
    }
}

@Composable
private fun LTWGivingMachinePeekHeader(
    label: String,
    isExpanded: Boolean,
    visibleContext: GivingMachineVisibleContext,
    canOpen: Boolean,
    canDismiss: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MachineHeaderCream,
                        Color(0xFFEBDC74),
                    ),
                ),
            )
            .clickable(enabled = !isExpanded && canOpen, onClick = onOpen)
            .semantics {
                contentDescription = label
                stateDescription = if (isExpanded) {
                    "Expanded $visibleContext"
                } else {
                    "Collapsed entry surface"
                }
                role = Role.Button
                if (!isExpanded && canOpen) {
                    onClick(label = "Open Giving Machine") {
                        onOpen()
                        true
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = MachineBlue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        if (isExpanded) {
            IconButton(onClick = onDismiss, enabled = canDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Giving Machine",
                    tint = MachineBlue,
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = MachineBlue,
                modifier = Modifier.size(28.dp),
            )
        }
    }
    HorizontalDivider(color = MachineBlue.copy(alpha = 0.22f))
}

@Composable
private fun LTWGivingMachineContextBar(
    visibleContext: GivingMachineVisibleContext,
    canOpenCart: Boolean,
    canOpenInfo: Boolean,
    onOpenCart: () -> Unit,
    onOpenInfo: () -> Unit,
    onReturnToMachine: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (visibleContext != GivingMachineVisibleContext.MACHINE_BROWSE) {
            OutlinedButton(
                onClick = onReturnToMachine,
                border = androidx.compose.foundation.BorderStroke(1.dp, MachineGold.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MachineViewportBlackSoft,
                    contentColor = MachineCream,
                ),
            ) {
                Text("Back to Machine")
            }
        }

        OutlinedButton(
            onClick = onOpenCart,
            enabled = canOpenCart,
            border = androidx.compose.foundation.BorderStroke(1.dp, MachineGold.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MachineViewportBlackSoft,
                contentColor = MachineCream,
            ),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cart")
        }

        OutlinedButton(
            onClick = onOpenInfo,
            enabled = canOpenInfo,
            border = androidx.compose.foundation.BorderStroke(1.dp, MachineGold.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MachineViewportBlackSoft,
                contentColor = MachineCream,
            ),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Info")
        }
    }
}

@Composable
private fun ColumnScope.LTWMachineBrowsePane(
    uiState: LTWGivingMachineUiState,
    canBrowsePrevious: Boolean,
    canBrowseNext: Boolean,
    canOpenCart: Boolean,
    canOpenInfo: Boolean,
    onBrowsePrevious: () -> Unit,
    onBrowseNext: () -> Unit,
    onOpenItemDetail: (PresentedGivingMachineItem) -> Unit,
    onOpenCart: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    val machineWindowState = uiState.presentationState.machineWindowState
    val emptyMachineState = uiState.presentationState.emptyMachineState

    if (emptyMachineState != null) {
        LTWEmptyMachinePane(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            message = emptyMachineState.message,
            canOpenCart = canOpenCart,
            canOpenInfo = canOpenInfo,
            onOpenCart = onOpenCart,
            onOpenInfo = onOpenInfo,
        )
        return
    }

    val visibleItems = machineWindowState?.visibleSlotItems.orEmpty()
    val catalogItems = uiState.environment.catalogContext.givingMachineCatalog.orEmpty()
    val previousPreview = previewRow(
        catalogItems = catalogItems,
        visibleItems = visibleItems,
        peekState = machineWindowState?.peekContinuationState,
        direction = MachineWindowBrowseDirection.PREVIOUS,
    )
    val nextPreview = previewRow(
        catalogItems = catalogItems,
        visibleItems = visibleItems,
        peekState = machineWindowState?.peekContinuationState,
        direction = MachineWindowBrowseDirection.NEXT,
    )

    val dispensingSlotNumber = uiState.presentationState.dispenseAnimationState
        ?.takeIf { it.presentationState == DispenseAnimationPresentationState.PRESENTING }
        ?.slotNumber

    Column(modifier = Modifier.fillMaxSize()) {
        LTWMachineHousing(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            visibleItems = visibleItems,
            dispensingSlotNumber = dispensingSlotNumber,
            previousPreview = previousPreview,
            nextPreview = nextPreview,
            canBrowsePrevious = canBrowsePrevious,
            canBrowseNext = canBrowseNext,
            onBrowsePrevious = onBrowsePrevious,
            onBrowseNext = onBrowseNext,
            onOpenItemDetail = onOpenItemDetail,
        )
    }
}

@Composable
private fun LTWEmptyMachinePane(
    modifier: Modifier,
    message: String,
    canOpenCart: Boolean,
    canOpenInfo: Boolean,
    onOpenCart: () -> Unit,
    onOpenInfo: () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MachineViewportBlack,
            contentColor = MachineCream,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Machine window",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MachineGold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MachineCream,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenInfo,
                    enabled = canOpenInfo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MachineGold.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MachineViewportBlackSoft,
                        contentColor = MachineCream,
                    ),
                ) {
                    Text("Info")
                }
                Button(
                    onClick = onOpenCart,
                    enabled = canOpenCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MachineHeaderCream,
                        contentColor = MachineBlue,
                    ),
                ) {
                    Text("Cart")
                }
            }
        }
    }
}

@Composable
private fun LTWMachineHousing(
    modifier: Modifier,
    visibleItems: List<PresentedGivingMachineItem>,
    dispensingSlotNumber: String?,
    previousPreview: List<PresentedGivingMachineItem>,
    nextPreview: List<PresentedGivingMachineItem>,
    canBrowsePrevious: Boolean,
    canBrowseNext: Boolean,
    onBrowsePrevious: () -> Unit,
    onBrowseNext: () -> Unit,
    onOpenItemDetail: (PresentedGivingMachineItem) -> Unit,
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MachineHousingRed),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(
                            width = 2.dp,
                            color = MachineGold.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(24.dp),
                        ),
                    colors = CardDefaults.cardColors(containerColor = MachineViewportBlack),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                    ) {
                        LTWPeekRow(
                            label = "Browse previous row",
                            previewItems = previousPreview,
                            onBrowse = onBrowsePrevious,
                            visible = previousPreview.isNotEmpty(),
                            enabled = canBrowsePrevious,
                            direction = MachineWindowBrowseDirection.PREVIOUS,
                        )

                        LTWMachineWindow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .draggable(
                                    orientation = Orientation.Vertical,
                                    state = rememberDraggableState { delta ->
                                        dragAccumulator += delta
                                        if (dragAccumulator >= 72f && canBrowsePrevious) {
                                            dragAccumulator = 0f
                                            onBrowsePrevious()
                                        } else if (dragAccumulator <= -72f && canBrowseNext) {
                                            dragAccumulator = 0f
                                            onBrowseNext()
                                        }
                                    },
                                ),
                            visibleItems = visibleItems,
                            dispensingSlotNumber = dispensingSlotNumber,
                            onOpenItemDetail = onOpenItemDetail,
                        )

                        LTWPeekRow(
                            label = "Browse next row",
                            previewItems = nextPreview,
                            onBrowse = onBrowseNext,
                            visible = nextPreview.isNotEmpty(),
                            enabled = canBrowseNext,
                            direction = MachineWindowBrowseDirection.NEXT,
                        )
                    }
                }

                LTWMachineRail(
                    canBrowsePrevious = canBrowsePrevious,
                    canBrowseNext = canBrowseNext,
                    onBrowsePrevious = onBrowsePrevious,
                    onBrowseNext = onBrowseNext,
                )
            }
        }
    }
}

@Composable
private fun LTWMachineWindow(
    modifier: Modifier,
    visibleItems: List<PresentedGivingMachineItem>,
    dispensingSlotNumber: String?,
    onOpenItemDetail: (PresentedGivingMachineItem) -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MachineViewportBlackSoft,
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = CrispWhite.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp),
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MachineViewportBlackSoft,
                            MachineViewportBlack,
                        ),
                    ),
                )
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                visibleItems.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { item ->
                            LTWMachineSlotCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                item = item,
                                isDispensing = dispensingSlotNumber == item.slotNumber,
                                onOpenItemDetail = { onOpenItemDetail(item) },
                            )
                        }
                        repeat((2 - row.size).coerceAtLeast(0)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CrispWhite.copy(alpha = 0.09f),
                                Color.Transparent,
                                CrispWhite.copy(alpha = 0.03f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun LTWMachineRail(
    canBrowsePrevious: Boolean,
    canBrowseNext: Boolean,
    onBrowsePrevious: () -> Unit,
    onBrowseNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .fillMaxSize()
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MachineHousingRed,
                        MachineHousingRedDark,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = MachineGold.copy(alpha = 0.24f),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            )
            .padding(vertical = 20.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LTWRailArrowButton(
            label = "Browse previous row",
            enabled = canBrowsePrevious,
            direction = MachineWindowBrowseDirection.PREVIOUS,
            onClick = onBrowsePrevious,
        )
        Spacer(modifier = Modifier.height(22.dp))
        LTWRailArrowButton(
            label = "Browse next row",
            enabled = canBrowseNext,
            direction = MachineWindowBrowseDirection.NEXT,
            onClick = onBrowseNext,
        )
    }
}

@Composable
private fun LTWRailArrowButton(
    label: String,
    enabled: Boolean,
    direction: MachineWindowBrowseDirection,
    onClick: () -> Unit,
) {
    val icon = when (direction) {
        MachineWindowBrowseDirection.PREVIOUS -> Icons.Default.KeyboardArrowUp
        MachineWindowBrowseDirection.NEXT -> Icons.Default.KeyboardArrowDown
    }
    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 92.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) MachineHousingRedDark.copy(alpha = 0.45f) else Color.Transparent)
            .semantics {
                if (enabled) {
                    contentDescription = label
                    role = Role.Button
                    onClick(label = label) {
                        onClick()
                        true
                    }
                }
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MachineGold else MachineGold.copy(alpha = 0.35f),
            modifier = Modifier.size(46.dp),
        )
    }
}

@Composable
private fun LTWMachineSlotCard(
    modifier: Modifier,
    item: PresentedGivingMachineItem,
    isDispensing: Boolean,
    onOpenItemDetail: () -> Unit,
) {
    val animatedFall by animateFloatAsState(
        targetValue = if (isDispensing) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "dispense_fall",
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDispensing) 0.08f else 1f,
        animationSpec = tween(durationMillis = 420),
        label = "dispense_alpha",
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                translationY = animatedFall
                alpha = animatedAlpha
            }
            .semantics {
                contentDescription = "Gift ${item.title}, slot ${item.slotNumber}"
                stateDescription = "Details available"
                role = Role.Button
                onClick(label = "Open details for ${item.title}") {
                    onOpenItemDetail()
                    true
                }
            }
            .clickable(onClick = onOpenItemDetail),
        colors = CardDefaults.cardColors(
            containerColor = MachineCardWhite,
            contentColor = MachineCardText,
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MachineCardBorder,
                    shape = RoundedCornerShape(22.dp),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MachineCardText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MachineCardBody,
            )
            Spacer(modifier = Modifier.height(10.dp))
            LTWOptionalItemImage(
                imageReference = item.imageReference,
                height = 78.dp,
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
        }
    }
}

@Composable
private fun LTWOptionalItemImage(
    imageReference: String?,
    height: androidx.compose.ui.unit.Dp = 54.dp,
) {
    if (imageReference.isNullOrBlank()) {
        return
    }

    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1E7D9))
            .border(1.dp, MachineCardBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageReference)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                LTWImageFallbackLabel(
                    label = "Loading image",
                )
            },
            error = {
                LTWImageFallbackLabel(
                    label = "Image unavailable",
                )
            },
        )
    }
}

@Composable
private fun LTWImageFallbackLabel(
    label: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1E7D9)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MachineCardBody,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LTWPeekRow(
    label: String,
    previewItems: List<PresentedGivingMachineItem>,
    onBrowse: () -> Unit,
    visible: Boolean,
    enabled: Boolean,
    direction: MachineWindowBrowseDirection,
) {
    val alpha = if (visible) 1f else 0f
    val rowHeight = if (visible) 52.dp else 6.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .alpha(alpha)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (visible) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF322C2C),
                            Color(0xFF282223),
                        ),
                    )
                } else {
                    Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent))
                },
            )
            .semantics(mergeDescendants = true) {
                if (visible && enabled) {
                    contentDescription = label
                    role = Role.Button
                    onClick(label = label) {
                        onBrowse()
                        true
                    }
                }
            }
            .clickable(enabled = visible && enabled, onClick = onBrowse)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            previewItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    Card(
                        modifier = Modifier
                            .align(
                                if (direction == MachineWindowBrowseDirection.PREVIOUS) {
                                    Alignment.BottomCenter
                                } else {
                                    Alignment.TopCenter
                                },
                            )
                            .fillMaxWidth()
                            .height(74.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MachineCardWhite.copy(alpha = 0.94f),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MachineCardText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF443C3C)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (direction == MachineWindowBrowseDirection.PREVIOUS) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null,
                tint = if (enabled) MachineGold else MachineGold.copy(alpha = 0.35f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun LTWGivingMachineItemDetailDialog(
    item: PresentedGivingMachineItem,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 360.dp),
            colors = CardDefaults.cardColors(
                containerColor = MachineCream,
                contentColor = MachineBlue,
            ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gift details",
                            style = MaterialTheme.typography.labelLarge,
                            color = MachineCardText,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MachineBlue,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MachineHeaderCream)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Slot ${item.slotNumber}",
                            color = MachineBlue,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                LTWOptionalItemImage(
                    imageReference = item.imageReference ?: "Gift image coming soon",
                    height = 120.dp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MachineCardBody,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MachineBlue.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CrispWhite,
                            contentColor = MachineBlue,
                        ),
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MachineHousingRed,
                            contentColor = CrispWhite,
                        ),
                    ) {
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}

@Composable
private fun LTWCartPane(
    uiState: LTWGivingMachineUiState,
    onReturnToMachine: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MachineCream,
            contentColor = MachineBlue,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Text(
                text = "Cart and checkout",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MachineBlue,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This calmer presentation keeps the Giving Machine destination intact while the donation business flow remains upstream.",
                style = MaterialTheme.typography.bodyMedium,
                color = MachineCardBody,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.sessionGifts.isEmpty()) {
                Text(
                    text = "No gifts have completed the add handoff yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MachineCardBody,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.sessionGifts.size) { index ->
                        val gift = uiState.sessionGifts[index]
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CrispWhite,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .border(1.dp, MachineCardBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                            ) {
                                Text(
                                    text = "Slot ${gift.slotNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MachineCardText,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = gift.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MachineBlue,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gift.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MachineCardBody,
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MidnightBlue.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReturnToMachine,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MachineHousingRed,
                    contentColor = CrispWhite,
                ),
            ) {
                Text("Return to Machine")
            }
        }
    }
}

@Composable
private fun LTWInfoPane(
    uiState: LTWGivingMachineUiState,
    onReturnToMachine: () -> Unit,
) {
    val infoState = uiState.presentationState.infoPresentationState
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MachineCream,
            contentColor = MachineBlue,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Text(
                text = infoState?.screenTitle ?: "Info",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MachineBlue,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(infoState?.contentSections?.size ?: 0) { index ->
                    val section = infoState!!.contentSections[index]
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CrispWhite,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, MachineCardBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                        ) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MachineBlue,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MachineCardBody,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReturnToMachine,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MachineHousingRed,
                    contentColor = CrispWhite,
                ),
            ) {
                Text("Back to Machine")
            }
        }
    }
}

private fun previewRow(
    catalogItems: List<PresentedGivingMachineItem>,
    visibleItems: List<PresentedGivingMachineItem>,
    peekState: MachineWindowPeekContinuationState?,
    direction: MachineWindowBrowseDirection,
): List<PresentedGivingMachineItem> {
    val previewRowSize = 2
    if (visibleItems.isEmpty()) {
        return emptyList()
    }
    val previewAllowed = when (direction) {
        MachineWindowBrowseDirection.PREVIOUS -> peekState == MachineWindowPeekContinuationState.PEEK_ABOVE_ONLY ||
            peekState == MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW
        MachineWindowBrowseDirection.NEXT -> peekState == MachineWindowPeekContinuationState.PEEK_BELOW_ONLY ||
            peekState == MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW
    }
    if (!previewAllowed) {
        return emptyList()
    }

    val firstVisibleIndex = visibleItems.first().slotNumber.toIntOrNull()?.minus(1) ?: return emptyList()
    val lastVisibleIndex = visibleItems.last().slotNumber.toIntOrNull()?.minus(1) ?: return emptyList()
    return when (direction) {
        MachineWindowBrowseDirection.PREVIOUS -> {
            val start = (firstVisibleIndex - previewRowSize).coerceAtLeast(0)
            catalogItems.drop(start).take(previewRowSize)
        }
        MachineWindowBrowseDirection.NEXT -> {
            catalogItems.drop(lastVisibleIndex + 1).take(previewRowSize)
        }
    }
}
