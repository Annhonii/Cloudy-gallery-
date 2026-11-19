package com.annhonii.cloudygallery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhonii.cloudygallery.ui.viewmodel.CloudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CloudViewModel = hiltViewModel()
) {
    // Access encrypted prefs through ViewModel
    var masterPassword by remember { mutableStateOf(viewModel.getMasterPassword()) }
    var confirmPassword by remember { mutableStateOf("") }
    var botToken by remember { mutableStateOf(viewModel.getBotToken()) }
    var chatId by remember { mutableStateOf(viewModel.getChatId()) }
    var showPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("Settings saved")
            saveSuccess = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // === ENCRYPTION ===
            SectionHeader(title = "ENCRYPTION")

            MonoTextField(
                value = masterPassword,
                onValueChange = {
                    masterPassword = it
                    passwordError = null
                },
                label = "Master Password",
                hint = "Used to encrypt/decrypt all cloud photos",
                isPassword = true,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword }
            )

            MonoTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordError = null
                },
                label = "Confirm Password",
                hint = "Re-enter master password",
                isPassword = true,
                showPassword = showPassword,
                onTogglePassword = { showPassword = !showPassword }
            )

            AnimatedVisibility(visible = passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            InfoBox(
                text = "◆ PBKDF2 · 310,000 iter · 256-bit key · AES-256-GCM\n" +
                        "◆ Per-photo unique salt + IV\n" +
                        "◆ Password never leaves your device"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // === TELEGRAM BOT ===
            SectionHeader(title = "TELEGRAM BOT")

            MonoTextField(
                value = botToken,
                onValueChange = { botToken = it },
                label = "Bot Token",
                hint = "From @BotFather (e.g. 1234567890:ABC...)"
            )

            MonoTextField(
                value = chatId,
                onValueChange = { chatId = it },
                label = "Chat ID",
                hint = "Private group/channel ID (e.g. -100123456789)",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))
            InfoBox(
                text = "◆ Create a bot at @BotFather\n" +
                        "◆ Create a private group and add the bot as admin\n" +
                        "◆ Photos are stored as encrypted .bin files in the group"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // === SAVE BUTTON ===
            Button(
                onClick = {
                    // Validate
                    if (masterPassword.isNotEmpty() && masterPassword != confirmPassword && confirmPassword.isNotEmpty()) {
                        passwordError = "Passwords do not match"
                        return@Button
                    }
                    if (masterPassword.length in 1..7) {
                        passwordError = "Password must be at least 8 characters"
                        return@Button
                    }

                    // Save to encrypted prefs via ViewModel's prefs reference
                    // The ViewModel holds a reference; we call a save method
                    // For simplicity, the prefs are written via the ViewModel
                    viewModel.saveSettings(masterPassword, botToken, chatId)
                    saveSuccess = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            ) {
                Text("SAVE SETTINGS", fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        HorizontalDivider(color = Color(0xFF1A1A1A))
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MonoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String = "",
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            },
            visualTransformation = if (isPassword && !showPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Hide" else "Show",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            singleLine = true
        )
        if (hint.isNotEmpty()) {
            Text(
                text = hint,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun InfoBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F0F0F),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A1A1A))
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.Gray,
            lineHeight = 16.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
