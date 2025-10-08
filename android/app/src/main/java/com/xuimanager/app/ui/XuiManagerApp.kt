package com.xuimanager.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.xuimanager.app.data.DisplayEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XuiManagerApp(viewModel: XuiViewModel) {
    val state = viewModel.uiState
    val snackbarHostState = rememberSnackbarHostState()

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (state.loginResult != null) {
                TopAppBar(
                    title = { Text(text = "XUI Manager") },
                    actions = {
                        IconButton(onClick = { viewModel.refreshAll() }, enabled = !state.isLoading) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh data")
                        }
                        IconButton(onClick = viewModel::logout) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (state.loginResult == null) {
            LoginScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                form = state.loginForm,
                isLoading = state.isLoading,
                onBaseUrlChanged = viewModel::updateBaseUrl,
                onUsernameChanged = viewModel::updateUsername,
                onLoggingChanged = viewModel::updateLoggingPreference,
                onRememberChanged = viewModel::updateRememberSession,
                onLogin = viewModel::login
            )
        } else {
            DashboardScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                state = state,
                onRenew = viewModel::renewSubscription,
                onStatusChanged = viewModel::updateSubscriptionStatus,
                onChannelFilterChanged = viewModel::updateChannelFilter,
                onCustomAction = viewModel::runCustomAction
            )
        }
    }
}

@Composable
private fun LoginScreen(
    modifier: Modifier,
    form: LoginFormState,
    isLoading: Boolean,
    onBaseUrlChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onLoggingChanged: (Boolean) -> Unit,
    onRememberChanged: (Boolean) -> Unit,
    onLogin: (baseUrl: String, username: String, password: String, enableLogging: Boolean) -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign in to your XUI panel", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = form.baseUrl,
            onValueChange = onBaseUrlChanged,
            label = { Text("Panel URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("https://example.com/") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = form.username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = form.enableLogging,
                onCheckedChange = onLoggingChanged
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Enable request logging")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.rememberSession, onCheckedChange = onRememberChanged)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Remember session on this device")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLogin(form.baseUrl.trim(), form.username.trim(), password, form.enableLogging) },
            enabled = !isLoading && form.baseUrl.isNotBlank() && form.username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text("Login")
        }
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier,
    state: XuiUiState,
    onRenew: (subscriptionId: Int, months: Int) -> Unit,
    onStatusChanged: (String?) -> Unit,
    onChannelFilterChanged: (String?) -> Unit,
    onCustomAction: (Map<String, String>) -> Unit
) {
    var selectedEntry by remember { mutableStateOf<DisplayEntry?>(null) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome ${state.loginResult?.username ?: "reseller"}",
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            FiltersCard(
                status = state.subscriptionStatus.orEmpty(),
                channelId = state.channelFilter.orEmpty(),
                onStatusChanged = onStatusChanged,
                onChannelFilterChanged = onChannelFilterChanged
            )
        }
        item {
            RenewSubscriptionCard(onRenew = onRenew, isProcessing = state.isRenewing)
        }
        item {
            CustomActionCard(onCustomAction = onCustomAction, isProcessing = state.isLoading)
        }
        item {
            DataSection(
                title = "Resellers",
                entries = state.resellers,
                emptyLabel = "No resellers available",
                onEntrySelected = { selectedEntry = it }
            )
        }
        item {
            DataSection(
                title = "Subscriptions",
                entries = state.subscriptions,
                emptyLabel = "No subscriptions found",
                onEntrySelected = { selectedEntry = it }
            )
        }
        item {
            DataSection(
                title = "Online Users",
                entries = state.onlineUsers,
                emptyLabel = "No active viewers",
                onEntrySelected = { selectedEntry = it }
            )
        }
        item {
            DataSection(
                title = "Channel Watchers",
                entries = state.channelWatchers,
                emptyLabel = "No channel watcher data",
                onEntrySelected = { selectedEntry = it }
            )
        }
        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        EntryDetailDialog(entry = entry, onDismiss = { selectedEntry = null })
    }
}

@Composable
private fun FiltersCard(
    status: String,
    channelId: String,
    onStatusChanged: (String?) -> Unit,
    onChannelFilterChanged: (String?) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Filters", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = status,
                onValueChange = { onStatusChanged(it) },
                label = { Text("Subscription status (e.g. active)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = channelId,
                onValueChange = { onChannelFilterChanged(it) },
                label = { Text("Channel ID filter") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun RenewSubscriptionCard(
    onRenew: (subscriptionId: Int, months: Int) -> Unit,
    isProcessing: Boolean
) {
    var subscriptionId by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("1") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Renew subscription", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = subscriptionId,
                onValueChange = { subscriptionId = it },
                label = { Text("Subscription ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = months,
                onValueChange = { months = it },
                label = { Text("Months") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val id = subscriptionId.toIntOrNull()
                    val duration = months.toIntOrNull() ?: 1
                    if (id != null) {
                        onRenew(id, duration)
                    }
                },
                enabled = !isProcessing && subscriptionId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text("Renew")
            }
        }
    }
}

@Composable
private fun CustomActionCard(
    onCustomAction: (Map<String, String>) -> Unit,
    isProcessing: Boolean
) {
    var action by remember { mutableStateOf("") }
    val parameters = remember { mutableStateListOf(CustomParam(id = 0)) }
    var nextId by remember { mutableIntStateOf(1) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Custom API action", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = action,
                onValueChange = { action = it },
                label = { Text("Action name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            parameters.forEachIndexed { index, param ->
                ParameterRow(
                    parameter = param,
                    onChange = { updated -> parameters[index] = updated },
                    onRemove = {
                        if (parameters.size == 1) {
                            parameters[index] = CustomParam(id = param.id)
                        } else {
                            parameters.removeAt(index)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            TextButton(onClick = {
                parameters.add(CustomParam(id = nextId))
                nextId++
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add parameter")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add parameter")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val payload = buildMap {
                            put("action", action.trim())
                            parameters
                                .mapNotNull { param ->
                                    val key = param.key.trim()
                                    val value = param.value.trim()
                                    if (key.isNotEmpty() && value.isNotEmpty()) key to value else null
                                }
                                .forEach { (key, value) -> put(key, value) }
                        }
                        onCustomAction(payload)
                    },
                    enabled = !isProcessing && action.isNotBlank()
                ) {
                    Text("Send")
                }
                TextButton(onClick = {
                    action = ""
                    parameters.clear()
                    parameters.add(CustomParam(id = 0))
                    nextId = 1
                }) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun ParameterRow(
    parameter: CustomParam,
    onChange: (CustomParam) -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = parameter.key,
            onValueChange = { onChange(parameter.copy(key = it)) },
            label = { Text("Key") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = parameter.value,
            onValueChange = { onChange(parameter.copy(value = it)) },
            label = { Text("Value") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRemove) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove parameter")
        }
    }
}

@Composable
private fun DataSection(
    title: String,
    entries: List<DisplayEntry>,
    emptyLabel: String,
    onEntrySelected: (DisplayEntry) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    val filteredEntries = remember(query, entries) {
        if (query.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.title.contains(query, ignoreCase = true) ||
                    entry.attributes.any { (label, value) ->
                        label.contains(query, ignoreCase = true) ||
                            value.contains(query, ignoreCase = true)
                    }
            }
        }
    }
    val displayEntries = filteredEntries.take(50)
    val overflowCount = filteredEntries.size - displayEntries.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search entries")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (displayEntries.isEmpty()) {
                        Text(emptyLabel, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            displayEntries.forEach { entry ->
                                EntryListItem(entry = entry, onClick = onEntrySelected)
                            }
                            if (overflowCount > 0) {
                                Text(
                                    text = "+$overflowCount more – refine your search to narrow the list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryListItem(entry: DisplayEntry, onClick: (DisplayEntry) -> Unit) {
    Card(
        onClick = { onClick(entry) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            entry.attributes.take(3).forEach { (label, value) ->
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (entry.attributes.size > 3) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${entry.attributes.size - 3} more fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EntryDetailDialog(entry: DisplayEntry, onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                val formatted = buildString {
                    appendLine(entry.title)
                    entry.attributes.forEach { (label, value) ->
                        appendLine("$label: $value")
                    }
                }.trimEnd()
                clipboardManager.setText(AnnotatedString(formatted))
            }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy entry")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy data")
            }
        },
        title = { Text(entry.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(scrollState)
            ) {
                entry.attributes.forEach { (label, value) ->
                    Text(text = label, style = MaterialTheme.typography.labelMedium)
                    Text(text = value.ifEmpty { "—" }, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

private data class CustomParam(
    val id: Int,
    val key: String = "",
    val value: String = ""
)
