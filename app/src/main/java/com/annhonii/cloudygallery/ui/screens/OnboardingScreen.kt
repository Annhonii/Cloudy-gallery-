package com.annhonii.cloudygallery.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhonii.cloudygallery.ui.viewmodel.CloudViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: CloudViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Step 2: Password
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Step 3: Telegram
    var botToken by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 24.dp else 8.dp, 2.dp)
                            .background(if (selected) Color.White else Color.Gray)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomePage()
                    1 -> OnboardingPasswordPage(
                        password = masterPassword,
                        confirmPassword = confirmPassword,
                        showPassword = showPassword,
                        error = passwordError,
                        onPasswordChange = {
                            masterPassword = it
                            passwordError = null
                        },
                        onConfirmChange = {
                            confirmPassword = it
                            passwordError = null
                        },
                        onToggleVisibility = { showPassword = !showPassword }
                    )
                    2 -> OnboardingTelegramPage(
                        botToken = botToken,
                        chatId = chatId,
                        onBotTokenChange = { botToken = it },
                        onChatIdChange = { chatId = it }
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                    ) {
                        Text("BACK", fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Next / Finish button
                Button(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                            1 -> {
                                // Validate password
                                when {
                                    masterPassword.length < 8 -> {
                                        passwordError = "Min 8 characters required"
                                    }
                                    masterPassword != confirmPassword -> {
                                        passwordError = "Passwords do not match"
                                    }
                                    else -> coroutineScope.launch {
                                        pagerState.animateScrollToPage(2)
                                    }
                                }
                            }
                            2 -> {
                                // Save and complete
                                viewModel.saveSettings(masterPassword, botToken, chatId)
                                viewModel.markOnboardingComplete()
                                onComplete()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == 2) "FINISH" else "NEXT",
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingWelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "◆",
            fontSize = 64.sp,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "CLOUDY GALLERY",
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            color = Color.White,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your photos. Encrypted.\nStored in Telegram.",
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

        OnboardingFeatureRow(icon = Icons.Filled.Lock, text = "AES-256-GCM encryption per photo")
        Spacer(modifier = Modifier.height(12.dp))
        OnboardingFeatureRow(icon = Icons.Filled.Cloud, text = "Telegram as your private cloud")
        Spacer(modifier = Modifier.height(12.dp))
        OnboardingFeatureRow(icon = Icons.Filled.Key, text = "Master password stays on device")
    }
}

@Composable
private fun OnboardingFeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun OnboardingPasswordPage(
    password: String,
    confirmPassword: String,
    showPassword: Boolean,
    error: String?,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SET MASTER PASSWORD",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This password encrypts all your cloud photos.\nStore it somewhere safe — it cannot be recovered.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Master Password", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null, tint = Color.Gray
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = onboardingTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmChange,
            label = { Text("Confirm Password", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = onboardingTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            singleLine = true
        )

        AnimatedVisibility(visible = error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun OnboardingTelegramPage(
    botToken: String,
    chatId: String,
    onBotTokenChange: (String) -> Unit,
    onChatIdChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CONNECT TELEGRAM",
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a bot via @BotFather and add it\nto a private group as admin.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = botToken,
            onValueChange = onBotTokenChange,
            label = { Text("Bot Token", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            placeholder = { Text("1234567890:ABC...", color = Color.Gray, fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            colors = onboardingTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = chatId,
            onValueChange = onChatIdChange,
            label = { Text("Chat ID", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            placeholder = { Text("-100123456789", color = Color.Gray, fontFamily = FontFamily.Monospace) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = onboardingTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "(Optional — you can configure this later in Settings)",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun onboardingTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray,
    cursorColor = Color.White
)
