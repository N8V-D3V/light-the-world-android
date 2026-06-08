package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.n8vd3v.lighttheworld.CrispWhite
import com.n8vd3v.lighttheworld.DeepGold
import com.n8vd3v.lighttheworld.MidnightBlue
import com.n8vd3v.lighttheworld.R
import com.n8vd3v.lighttheworld.RichRed
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeFlow
import com.n8vd3v.lighttheworld.features.dailychallenge.share.SharePayload
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyChallengeCardScreen(
    flow: DailyServiceChallengeFlow,
    currentLocalDateProvider: () -> LocalDate,
    campaignWindow: CampaignWindow,
    appLink: String,
    onSharePayload: (SharePayload) -> Unit,
    coordinator: ChallengeCardPresentationCoordinator = remember { ChallengeCardPresentationCoordinator() },
) {
    val emptyMessage = stringResource(R.string.no_challenges_available)
    val runtimeController = remember(flow, currentLocalDateProvider, campaignWindow, appLink) {
        DailyChallengeCardRuntimeController(
            flow = flow,
            currentLocalDateProvider = currentLocalDateProvider,
            campaignWindow = campaignWindow,
            appLink = appLink,
        )
    }
    val runtimeState = runtimeController.uiState
    val browseResponse = runtimeState.browseResponse
    val currentLocalDate = runtimeState.currentLocalDate
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, runtimeController) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                runtimeController.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val loadState = remember(browseResponse, currentLocalDate, coordinator) {
        coordinator.resolveLoadState(
            browseResponse = browseResponse,
            currentLocalDate = currentLocalDate,
            emptyMessage = emptyMessage,
        )
    }

    when (loadState) {
        is ChallengeCardPresentationLoadState.Failure -> {
            ChallengeCardFailureState(message = loadState.failure.message)
            return
        }
        is ChallengeCardPresentationLoadState.Empty -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = loadState.message, color = MidnightBlue)
            }
            return
        }
        is ChallengeCardPresentationLoadState.Content -> Unit
    }

    val presentedCards = loadState.cards
    val initialRenderState = remember(presentedCards, currentLocalDate, coordinator) {
        coordinator.resolveRenderState(
            cards = presentedCards,
            currentDate = currentLocalDate,
            currentActiveCardIdentifier = null,
            requestedActiveCardIdentifier = null,
            browseDirection = null,
            browsePhase = ChallengeCardBrowsePhase.ENDED,
            faceState = null,
        )
    }
    if (initialRenderState is ChallengeCardPresentationRenderState.Failure) {
        ChallengeCardFailureState(message = initialRenderState.failure.message)
        return
    }
    val initialPresentation = initialRenderState as ChallengeCardPresentationRenderState.Content
    val initialPage = remember(initialPresentation, presentedCards) {
        presentedCards.indexOfFirst { it.cardIdentifier == initialPresentation.activeCardIdentifier }
            .takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { presentedCards.size },
    )
    val coroutineScope = rememberCoroutineScope()

    var settledPage by remember(presentedCards) { mutableIntStateOf(initialPage) }
    var faceState by remember(presentedCards) {
        mutableStateOf(
            initialPresentation.activeCardIdentifier?.let { activeIdentifier ->
                ChallengeCardFaceState(
                    cardIdentifier = activeIdentifier,
                    visibleFace = ChallengeCardVisibleFace.FRONT,
                )
            },
        )
    }
    var interactionFailure by remember(presentedCards) { mutableStateOf<ChallengeCardPresentationFailure?>(null) }

    val currentPageCardIdentifier = presentedCards.getOrNull(pagerState.currentPage)?.cardIdentifier
    val browseDirection = when {
        pagerState.currentPage > settledPage -> ChallengeCardBrowseDirection.NEXT
        pagerState.currentPage < settledPage -> ChallengeCardBrowseDirection.PREVIOUS
        else -> null
    }
    val browsePhase = if (pagerState.isScrollInProgress) {
        ChallengeCardBrowsePhase.IN_PROGRESS
    } else {
        ChallengeCardBrowsePhase.ENDED
    }

    val renderState = remember(
        presentedCards,
        currentLocalDate,
        currentPageCardIdentifier,
        browseDirection,
        browsePhase,
        faceState,
        coordinator,
    ) {
        coordinator.resolveRenderState(
            cards = presentedCards,
            currentDate = currentLocalDate,
            currentActiveCardIdentifier = currentPageCardIdentifier,
            requestedActiveCardIdentifier = currentPageCardIdentifier,
            browseDirection = browseDirection,
            browsePhase = browsePhase,
            faceState = faceState,
        )
    }
    if (renderState is ChallengeCardPresentationRenderState.Failure) {
        ChallengeCardFailureState(message = renderState.failure.message)
        return
    }
    val contentState = renderState as ChallengeCardPresentationRenderState.Content
    val activeCardIdentifier = contentState.activeCardIdentifier ?: currentPageCardIdentifier
    val activeCard = contentState.railResponse.challengeCardRail.firstOrNull {
        it.cardIdentifier == activeCardIdentifier
    } ?: presentedCards.getOrNull(pagerState.currentPage)
    val actionState = runtimeController.actionStateFor(activeCard?.challengeDate)
    val activeFaceState = contentState.accessibilityResponse.cardFaceState
        ?: activeCardIdentifier?.let { identifier ->
            ChallengeCardFaceState(
                cardIdentifier = identifier,
                visibleFace = ChallengeCardVisibleFace.FRONT,
            )
        }

    LaunchedEffect(activeCardIdentifier) {
        if (activeCardIdentifier != null && faceState?.cardIdentifier != activeCardIdentifier) {
            val faceResponse = coordinator.resolveActiveCardChange(
                cards = presentedCards,
                previousActiveCardIdentifier = faceState?.cardIdentifier ?: activeCardIdentifier,
                requestedActiveCardIdentifier = activeCardIdentifier,
            )
            val failure = coordinator.toFaceFailure(
                response = faceResponse,
                stage = ChallengeCardPresentationFailureStage.FACE,
            )
            if (failure == null) {
                faceState = faceResponse.cardFaceState
                interactionFailure = null
            } else {
                faceState = faceResponse.cardFaceState ?: faceState
                interactionFailure = failure
            }
        }
    }

    LaunchedEffect(pagerState.settledPage, presentedCards) {
        settledPage = pagerState.settledPage
    }

    fun requestFace(face: ChallengeCardVisibleFace) {
        val response = coordinator.resolveFaceRequest(
            cards = presentedCards,
            activeCardIdentifier = activeCardIdentifier,
            requestedFace = face,
        )
        val failure = coordinator.toFaceFailure(
            response = response,
            stage = ChallengeCardPresentationFailureStage.FACE,
        )
        if (failure == null) {
            faceState = response.cardFaceState
            interactionFailure = null
        } else {
            faceState = response.cardFaceState ?: faceState
            interactionFailure = failure
        }
    }

    fun toggleActiveFace() {
        val response = coordinator.resolveFaceToggle(
            cards = presentedCards,
            activeCardIdentifier = activeCardIdentifier,
            currentFaceState = activeFaceState,
        )
        val failure = coordinator.toFaceFailure(
            response = response,
            stage = ChallengeCardPresentationFailureStage.FACE,
        )
        if (failure == null) {
            faceState = response.cardFaceState
            interactionFailure = null
        } else {
            faceState = response.cardFaceState ?: faceState
            interactionFailure = failure
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MidnightBlue,
        )

        if (interactionFailure != null) {
            Text(
                text = interactionFailure!!.message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = RichRed,
            )
        }

        if (runtimeState.latestFailureMessage != null) {
            Text(
                text = runtimeState.latestFailureMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = RichRed,
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 0.dp,
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            val card = contentState.railResponse.challengeCardRail[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = lerp(start = 0.85f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(start = 0.6f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            val isActive = card.cardIdentifier == contentState.railResponse.activeCardState?.cardIdentifier
            val visibleFace = if (isActive) {
                activeFaceState?.takeIf { it.cardIdentifier == card.cardIdentifier }?.visibleFace
                    ?: ChallengeCardVisibleFace.FRONT
            } else {
                ChallengeCardVisibleFace.FRONT
            }

            val customActions = if (isActive) {
                buildList {
                    val accessibleState = contentState.accessibilityResponse.accessibilityPresentationState
                    val actions = accessibleState?.availableActions.orEmpty()
                    if (actions.contains(ChallengeCardAccessibilityAction.BROWSE_PREVIOUS)) {
                        add(
                            CustomAccessibilityAction(label = "Previous card") {
                                val targetPage = (page - 1).coerceAtLeast(0)
                                if (targetPage != page) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
                                }
                                true
                            },
                        )
                    }
                    if (actions.contains(ChallengeCardAccessibilityAction.BROWSE_NEXT)) {
                        add(
                            CustomAccessibilityAction(label = "Next card") {
                                val targetPage = (page + 1).coerceAtMost(presentedCards.lastIndex)
                                if (targetPage != page) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
                                }
                                true
                            },
                        )
                    }
                    if (actions.contains(ChallengeCardAccessibilityAction.SHOW_BACK)) {
                        add(
                            CustomAccessibilityAction(label = "Show back") {
                                requestFace(ChallengeCardVisibleFace.BACK)
                                true
                            },
                        )
                    }
                    if (actions.contains(ChallengeCardAccessibilityAction.SHOW_FRONT)) {
                        add(
                            CustomAccessibilityAction(label = "Show front") {
                                requestFace(ChallengeCardVisibleFace.FRONT)
                                true
                            },
                        )
                    }
                }
            } else {
                emptyList()
            }

            ChallengeCard(
                card = card,
                isActive = isActive,
                visibleFace = visibleFace,
                semanticsDescription = cardSemanticsDescription(
                    card = card,
                    activeState = contentState.railResponse.activeCardState,
                    face = visibleFace,
                ),
                semanticsStateDescription = cardSemanticsStateDescription(
                    accessibilityResponse = contentState.accessibilityResponse,
                    face = visibleFace,
                    isActive = isActive,
                ),
                accessibilityActions = customActions,
                onToggleFace = { toggleActiveFace() },
                onBackFaceRead = {
                    val response = coordinator.resolveBackFaceReading(
                        cards = presentedCards,
                        activeCardIdentifier = activeCardIdentifier,
                    )
                    val failure = coordinator.toFaceFailure(
                        response = response,
                        stage = ChallengeCardPresentationFailureStage.BACK_FACE_READING,
                    )
                    if (failure == null) {
                        faceState = response.cardFaceState ?: faceState
                        interactionFailure = null
                    } else {
                        faceState = response.cardFaceState ?: faceState
                        interactionFailure = failure
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .aspectRatio(0.72f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
            )
        }

        if (actionState.canMarkComplete || actionState.canRequestReversal || actionState.canShare) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (actionState.canMarkComplete) {
                    Button(
                        onClick = { runtimeController.markComplete(activeCard?.challengeDate) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.challenge_mark_complete))
                    }
                }
                if (actionState.canRequestReversal) {
                    if (actionState.canMarkComplete) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    OutlinedButton(
                        onClick = { runtimeController.requestCompletionReversal(activeCard?.challengeDate) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.challenge_mark_incomplete))
                    }
                }
                if (actionState.canShare) {
                    if (actionState.canMarkComplete || actionState.canRequestReversal) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Button(
                        onClick = {
                            runtimeController.shareCompletedChallenge(
                                challengeDate = activeCard?.challengeDate,
                                onSharePayloadReady = onSharePayload,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.challenge_share))
                    }
                }
            }
        }

        val position = contentState.railResponse.activeCardState?.sequencePosition
        Text(
            text = if (position != null) {
                "Day ${position.oneBasedIndex} of ${position.totalCards}"
            } else {
                "Day 0 of ${presentedCards.size}"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
        )
    }

    runtimeState.pendingReversalPrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = { runtimeController.dismissCompletionReversalPrompt() },
            title = { Text(text = stringResource(R.string.challenge_reversal_title)) },
            text = { Text(text = prompt.message) },
            confirmButton = {
                TextButton(onClick = { runtimeController.confirmCompletionReversal() }) {
                    Text(text = stringResource(R.string.challenge_mark_incomplete))
                }
            },
            dismissButton = {
                TextButton(onClick = { runtimeController.dismissCompletionReversalPrompt() }) {
                    Text(text = stringResource(R.string.challenge_keep_complete))
                }
            },
        )
    }
}

@Composable
private fun ChallengeCardFailureState(
    message: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = RichRed,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun ChallengeCard(
    card: PresentedChallengeCard,
    isActive: Boolean,
    visibleFace: ChallengeCardVisibleFace,
    semanticsDescription: String,
    semanticsStateDescription: String,
    accessibilityActions: List<CustomAccessibilityAction>,
    onToggleFace: () -> Unit,
    onBackFaceRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (visibleFace == ChallengeCardVisibleFace.BACK) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cardFlip",
    )

    Card(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = semanticsDescription
                stateDescription = semanticsStateDescription
                role = Role.Button
                if (isActive) {
                    onClick(
                        label = if (visibleFace == ChallengeCardVisibleFace.BACK) {
                            "Show front"
                        } else {
                            "Show back"
                        },
                    ) {
                        onToggleFace()
                        true
                    }
                    customActions = accessibilityActions
                }
            }
            .clickable(enabled = isActive) { onToggleFace() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(48.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(48.dp)),
        ) {
            if (rotation <= 90f) {
                CardFront(card)
            } else {
                Box(Modifier.graphicsLayer { rotationY = 180f }) {
                    CardBack(card = card, onBackFaceRead = onBackFaceRead)
                }
            }
        }
    }
}

@Composable
private fun CardFront(card: PresentedChallengeCard) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM. dd") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrispWhite),
    ) {
        Box(
            modifier = Modifier
                .weight(6f)
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                AutoResizingText(
                    text = card.shortSummary,
                    baseFontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = card.challengeDate.format(dateFormatter),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }

        Box(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
                .background(RichRed)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = card.detailDescription,
                fontSize = 18.sp,
                color = CrispWhite,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.alpha(0.95f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            if (card.completionState == ChallengeCardCompletionPresentationState.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.completed_content_description),
                    tint = CrispWhite,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun AutoResizingText(
    text: String,
    baseFontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    val minimumFontSize = 14f
    var fontSizeValue by remember(text, baseFontSize) { mutableStateOf(baseFontSize.value) }

    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSizeValue.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        lineHeight = (fontSizeValue * 1.1f).sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && fontSizeValue > minimumFontSize) {
                fontSizeValue = (fontSizeValue * 0.9f).coerceAtLeast(minimumFontSize)
            }
        },
    )
}

@Composable
private fun CardBack(
    card: PresentedChallengeCard,
    onBackFaceRead: () -> Unit,
) {
    LaunchedEffect(card.cardIdentifier) {
        onBackFaceRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = card.detailDescription,
            fontSize = 21.sp,
            color = CrispWhite,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .background(DeepGold),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.suggestions_label).uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DeepGold,
            letterSpacing = 2.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        card.suggestions.forEach { suggestion ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = "• ", color = DeepGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = suggestion, fontSize = 17.sp, color = CrispWhite, lineHeight = 24.sp)
            }
        }
    }
}

private fun cardSemanticsDescription(
    card: PresentedChallengeCard,
    activeState: ChallengeCardActiveState?,
    face: ChallengeCardVisibleFace,
): String {
    val date = card.challengeDate.format(DateTimeFormatter.ofPattern("MMMM d"))
    val body = if (face == ChallengeCardVisibleFace.FRONT) {
        card.shortSummary
    } else {
        card.detailDescription
    }
    val position = activeState?.sequencePosition
    val positionText = if (position != null && activeState.cardIdentifier == card.cardIdentifier) {
        "Card ${position.oneBasedIndex} of ${position.totalCards}. "
    } else {
        ""
    }
    return "$positionText$date. $body"
}

private fun cardSemanticsStateDescription(
    accessibilityResponse: com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityResponse,
    face: ChallengeCardVisibleFace,
    isActive: Boolean,
): String {
    if (!isActive) {
        return "Adjacent card preview"
    }

    val actions = accessibilityResponse.accessibilityPresentationState?.availableActions.orEmpty()
    val availableActionsDescription = buildList {
        if (actions.contains(ChallengeCardAccessibilityAction.BROWSE_PREVIOUS)) {
            add("previous card")
        }
        if (actions.contains(ChallengeCardAccessibilityAction.BROWSE_NEXT)) {
            add("next card")
        }
        if (actions.contains(ChallengeCardAccessibilityAction.SHOW_FRONT)) {
            add("show front")
        }
        if (actions.contains(ChallengeCardAccessibilityAction.SHOW_BACK)) {
            add("show back")
        }
    }.joinToString()

    val faceDescription = if (face == ChallengeCardVisibleFace.BACK) "Back face" else "Front face"
    return if (availableActionsDescription.isBlank()) {
        faceDescription
    } else {
        "$faceDescription. Available actions: $availableActionsDescription"
    }
}
