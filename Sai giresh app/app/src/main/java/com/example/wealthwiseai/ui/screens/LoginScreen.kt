package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GradientButton
import com.example.wealthwiseai.ui.theme.BlueAccent
import com.example.wealthwiseai.ui.theme.GreenAccent
import com.example.wealthwiseai.ui.theme.TextGray
import com.example.wealthwiseai.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    val scrollState = rememberScrollState()

    // Trigger onLoginSuccess callback on successful login state
    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.AuthState.Success) {
            onLoginSuccess((loginState as AuthViewModel.AuthState.Success).email)
            viewModel.resetStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(BlueAccent, GreenAccent)),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "W",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WealthWise AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Your Personal AI Financial Advisor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Form Errors
            if (loginState is AuthViewModel.AuthState.Error) {
                Text(
                    text = (loginState as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Email input
            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (it.trim().isEmpty()) {
                        "Email is required"
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it.trim()).matches()) {
                        "Invalid email format"
                    } else {
                        ""
                    }
                },
                label = "Email Address",
                placeholder = "yourname@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.isNotEmpty(),
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input (visual transformation handles masking)
            var passwordVisibility by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.isEmpty()) "Password is required" else ""
                },
                label = { Text("Password") },
                placeholder = { Text("••••••") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError.isNotEmpty(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    focusedLabelColor = BlueAccent,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )
            if (passwordError.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            if (loginState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = BlueAccent)
            } else {
                GradientButton(
                    text = "Login",
                    onClick = {
                        val isEmailValid = email.trim().isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                        val isPassValid = password.isNotEmpty()

                        if (!isEmailValid) emailError = "Please enter a valid email"
                        if (!isPassValid) passwordError = "Please enter your password"

                        if (isEmailValid && isPassValid) {
                            viewModel.login(email.trim(), password)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation to Signup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Create New Account",
                    color = BlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DisclaimerText()
        }
    }
}
