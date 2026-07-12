package com.bloom.app.ui.journal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.data.model.JournalEntry
import com.bloom.app.data.model.Mood
import com.bloom.app.ui.components.BloomCard
import com.bloom.app.ui.components.BloomPrimaryButton
import com.bloom.app.ui.components.BloomShimmerBlock
import com.bloom.app.ui.theme.MoodGood
import com.bloom.app.ui.theme.MoodGreat
import com.bloom.app.ui.theme.MoodLow
import com.bloom.app.ui.theme.MoodOkay
import com.bloom.app.ui.theme.MoodRough
import com.bloom.app.util.DateUtils

// ─────────────────────────────────────────────────────────────────────────────
// JournalListScreen
//
// Design philosophy:
//   - This is a personal archive — feel like a journal shelf
//   - Each entry card shows: mood emoji, date, first line, word count
//   - Group by date (Today, Yesterday, Mon July 7…)
//   - Empty state should feel encouraging, not clinical
//   - FAB for new entry — always accessible
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JournalListScreen(
    onNewEntry  : () -> Unit,
    onEntryTap  : (Long) -> Unit,
    viewModel   : JournalViewModel = viewModel(),
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (state.isLoading) {
            JournalShimmerLoading()
        } else if (state.entries.isEmpty()) {
            JournalEmptyState(
                onNewEntry = onNewEntry,
                modifier   = Modifier.align(Alignment.Center),
            )
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(
                    top    = 16.dp,
                    bottom = 100.dp,
                    start  = 20.dp,
                    end    = 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text     = "Your journal",
                        style    = MaterialTheme.typography.headlineMedium,
                        color    = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = 16.dp, bottom = 8.dp),
                    )
                }

                // Group entries by date
                val grouped = state.entries.groupBy {
                    DateUtils.formatEntryDate(it.createdAt)
                }

                grouped.forEach { (dateLabel, entries) ->
                    item(key = dateLabel) {
                        Text(
                            text     = dateLabel,
                            style    = MaterialTheme.typography.titleSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    items(entries, key = { it.id }) { entry ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
                        ) {
                            JournalEntryCard(
                                entry  = entry,
                                onTap  = { onEntryTap(entry.id) },
                                onLongPress = { entryToDelete = entry }
                            )
                        }
                    }
                }
            }
        }

        // FAB — always visible for quick access
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(400, delayMillis = 100)) + fadeIn(animationSpec = tween(400, delayMillis = 100)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            FloatingActionButton(
                onClick          = onNewEntry,
                containerColor   = MaterialTheme.colorScheme.primary,
                contentColor     = MaterialTheme.colorScheme.onPrimary,
                shape            = com.bloom.app.ui.theme.PillShape,
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Add,
                        contentDescription = "New entry",
                    )
                    Text(
                        text  = "New entry",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { 
                Text("Delete Entry") 
            },
            text = { 
                Text("Are you sure you want to delete the selected entry(s)?") 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.deleteEntry(entryToDelete!!)
                        entryToDelete = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun JournalShimmerLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BloomShimmerBlock(modifier = Modifier.height(40.dp).width(150.dp).padding(bottom = 8.dp))
        BloomShimmerBlock(modifier = Modifier.height(20.dp).width(100.dp))
        repeat(4) {
            BloomShimmerBlock(modifier = Modifier.height(100.dp).fillMaxWidth())
        }
    }
}

// ── Entry Card ────────────────────────────────────────────────────────────────

@Composable
private fun JournalEntryCard(
    entry : JournalEntry,
    onTap : () -> Unit,
    onLongPress : () -> Unit,
) {
    val moodColor = when (entry.mood) {
        Mood.GREAT -> MoodGreat
        Mood.GOOD  -> MoodGood
        Mood.OKAY  -> MoodOkay
        Mood.LOW   -> MoodLow
        Mood.ROUGH -> MoodRough
    }

    BloomCard(
        onClick  = onTap,
        onLongClick = onLongPress,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Mood emoji — the visual anchor for each entry
            Text(
                text  = entry.mood.emoji,
                style = MaterialTheme.typography.headlineSmall,
            )

            Column(modifier = Modifier.weight(1f)) {
                // First line of content
                Text(
                    text     = entry.content,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = DateUtils.formatTime(entry.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        if (entry.aiReflection != null) {
                            Text(
                                text  = "✨",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        Text(
                            text  = "${entry.wordCount}w",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun JournalEmptyState(
    onNewEntry : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    Column(
        modifier            = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Text(
            text  = "Your journal is waiting.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text  = "Every reflection starts with a single word.\nYou don't need to write perfectly — just honestly.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BloomPrimaryButton(
            text    = "Write your first entry",
            onClick = onNewEntry,
        )
    }
}
