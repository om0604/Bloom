package com.bloom.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.ui.components.BloomPrimaryButton
import com.bloom.app.ui.components.BloomTextButton
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// OnboardingScreen
//
// Design philosophy:
//   - One thought per page — no information overload
//   - Large emoji as visual anchor — warm and immediate
//   - Headline uses Lora with intentional line breaks — editorial
//   - Body text is short, personal, honest — not marketing copy
//   - Progress indicators are small and unobtrusive
//   - Name input on final page — feels personal, not clinical
//   - Gentle gradient background — warm cream to soft amber
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete  : () -> Unit,
    viewModel   : OnboardingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.entries.size })
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sync pagerState with viewModel (pager is the source of truth for page index)
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    )
                )
            )
            .systemBarsPadding(),
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Page indicator ────────────────────────────────────────────────
            PageIndicator(
                pageCount    = OnboardingPage.entries.size,
                currentPage  = pagerState.currentPage,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Pager ─────────────────────────────────────────────────────────
            HorizontalPager(
                state             = pagerState,
                modifier          = Modifier.weight(1f),
                userScrollEnabled = true,
                beyondViewportPageCount = 1,
            ) { pageIndex ->
                val page = OnboardingPage.entries[pageIndex]
                OnboardingPageContent(
                    page        = page,
                    userName    = uiState.userName,
                    onNameChange= viewModel::onNameChanged,
                    isLastPage  = pageIndex == OnboardingPage.entries.size - 1,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Actions ───────────────────────────────────────────────────────
            OnboardingActions(
                currentPage     = pagerState.currentPage,
                totalPages      = OnboardingPage.entries.size,
                userName        = uiState.userName,
                onNext          = {
                    keyboardController?.hide()
                    scope.launch {
                        val next = pagerState.currentPage + 1
                        pagerState.animateScrollToPage(next)
                    }
                },
                onComplete      = {
                    keyboardController?.hide()
                    viewModel.completeOnboarding()
                },
                onSkip          = {
                    keyboardController?.hide()
                    viewModel.completeOnboarding()
                },
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ── Page Content ──────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(
    page        : OnboardingPage,
    userName    : String,
    onNameChange: (String) -> Unit,
    isLastPage  : Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isLastPage) {
        if (isLastPage) {
            kotlinx.coroutines.delay(400)
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // ── Emoji ─────────────────────────────────────────────────────────────
        Text(
            text  = page.emoji,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize),
        )

        // ── Headline ──────────────────────────────────────────────────────────
        Text(
            text      = page.headline,
            style     = MaterialTheme.typography.displaySmall,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        // ── Body ──────────────────────────────────────────────────────────────
        Text(
            text      = page.body,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // ── Name Input (last page only) ───────────────────────────────────────
        if (isLastPage) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value           = userName,
                onValueChange   = onNameChange,
                modifier        = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text  = "Your first name",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                textStyle       = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(
                    capitalization  = KeyboardCapitalization.Words,
                    imeAction       = ImeAction.Done,
                ),
                shape  = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

// ── Actions Row ───────────────────────────────────────────────────────────────

@Composable
private fun OnboardingActions(
    currentPage : Int,
    totalPages  : Int,
    userName    : String,
    onNext      : () -> Unit,
    onComplete  : () -> Unit,
    onSkip      : () -> Unit,
) {
    val isLastPage   = currentPage == totalPages - 1
    val canComplete  = !isLastPage || userName.trim().isNotEmpty()

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BloomPrimaryButton(
            text     = if (isLastPage) "Begin" else "Continue",
            onClick  = { if (isLastPage) onComplete() else onNext() },
            enabled  = canComplete,
            modifier = Modifier.fillMaxWidth(),
        )

        AnimatedVisibility(visible = !isLastPage) {
            BloomTextButton(
                text    = "Skip",
                onClick = onSkip,
            )
        }
    }
}

// ── Page Indicator ────────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    pageCount   : Int,
    currentPage : Int,
    modifier    : Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue   = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label         = "indicator_width_$index",
            )
            val color by animateColorAsState(
                targetValue   = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                animationSpec = tween(300),
                label         = "indicator_color_$index",
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
