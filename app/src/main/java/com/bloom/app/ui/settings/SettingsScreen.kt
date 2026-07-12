package com.bloom.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.BloomApplication
import com.bloom.app.BuildConfig
import com.bloom.app.util.ThemePreference

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as BloomApplication
    val userPreferences = application.container.userPreferences
    
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(userPreferences)
    )

    val themePref by viewModel.themePreference.collectAsStateWithLifecycle()
    val dailyReminder by viewModel.dailyReminderEnabled.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ProfileSection(
                userName = userName,
                onNameSaved = { newName ->
                    viewModel.setUserName(newName)
                    scope.launch {
                        snackbarHostState.showSnackbar("✓ Profile updated successfully")
                    }
                }
            )

            AppearanceSection(
                currentTheme = themePref,
                onThemeSelected = viewModel::setThemePreference
            )
            
            AISection()
            
            NotificationsSection(
                isEnabled = dailyReminder,
                onToggle = viewModel::setDailyReminderEnabled
            )
            
            PrivacySection()
            
            AboutSection()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Profile Section ──────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(
    userName: String,
    onNameSaved: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var avatarBounce by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (avatarBounce) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "avatarBounce"
    )

    LaunchedEffect(userName) {
        if (userName.isNotBlank()) {
            avatarBounce = true
            delay(150)
            avatarBounce = false
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (userName.isNotBlank()) {
                    userName.trim().split("\\s+".toRegex())
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                } else "?"

                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (userName.isNotBlank()) userName else "Tap to add your name",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personalize your Bloom experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDialog) {
        EditNameDialog(
            currentName = userName,
            onDismiss = { showDialog = false },
            onSave = { newName ->
                onNameSaved(newName)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    val isNameValid = nameInput.trim().isNotBlank() && nameInput.trim() != currentName && nameInput.length <= 30
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Display Name", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    text = "This name is used to personalize your greetings throughout Bloom.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 30) nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    supportingText = {
                        Text(
                            text = "${nameInput.length}/30",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(nameInput.trim()) },
                enabled = isNameValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun AppearanceSection(
    currentTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit
) {
    SettingsSection(title = "Appearance") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOption(
                label = "System",
                isSelected = currentTheme == ThemePreference.SYSTEM,
                onClick = { onThemeSelected(ThemePreference.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                label = "Light",
                isSelected = currentTheme == ThemePreference.LIGHT,
                onClick = { onThemeSelected(ThemePreference.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                label = "Dark",
                isSelected = currentTheme == ThemePreference.DARK,
                onClick = { onThemeSelected(ThemePreference.DARK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AISection() {
    SettingsSection(title = "AI Assistant") {
        val hasKey = BuildConfig.GROQ_API_KEY.isNotEmpty()
        val statusText = if (hasKey) "✓ Connected" else "⚠ API Key Missing"
        val statusColor = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NotificationsSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingsSection(title = "Notifications") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Reminder",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun PrivacySection() {
    SettingsSection(title = "Privacy") {
        Text(
            text = "Your journal entries are stored locally on your device. Only reflections explicitly sent to the AI assistant leave your device, and are never used to train global models.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun AboutSection() {
    SettingsSection(title = "About") {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Bloom",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}
