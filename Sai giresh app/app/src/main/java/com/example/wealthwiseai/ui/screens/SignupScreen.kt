package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GradientButton
import com.example.wealthwiseai.ui.theme.BlueAccent
import com.example.wealthwiseai.ui.theme.GreenAccent
import com.example.wealthwiseai.ui.theme.TextGray
import com.example.wealthwiseai.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: (String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val signupState by viewModel.signupState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(signupState) {
        if (signupState is AuthViewModel.AuthState.Success) {
            onSignupSuccess((signupState as AuthViewModel.AuthState.Success).email)
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
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Join WealthWise AI to get custom advisory",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // General errors from API
            if (signupState is AuthViewModel.AuthState.Error) {
                Text(
                    text = (signupState as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Name Field
            CustomTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    nameError = if (it.trim().isEmpty()) "Name is required" else ""
                },
                label = "Full Name",
                placeholder = "John Doe",
                isError = nameError.isNotEmpty(),
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field
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

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.isEmpty()) {
                        "Password is required"
                    } else if (it.length < 6) {
                        "Password must be at least 6 characters"
                    } else {
                        ""
                    }
                    if (confirmPassword.isNotEmpty()) {
                        confirmPasswordError = if (it != confirmPassword) "Passwords do not match" else ""
                    }
                },
                label = "Password",
                placeholder = "••••••",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError.isNotEmpty(),
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            CustomTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) {
                        "Passwords do not match"
                    } else {
                        ""
                    }
                },
                label = "Confirm Password",
                placeholder = "••••••",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPasswordError.isNotEmpty(),
                errorMessage = confirmPasswordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            if (signupState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = BlueAccent)
            } else {
                GradientButton(
                    text = "Create Account",
                    onClick = {
                        val isNameValid = fullName.trim().isNotEmpty()
                        val isEmailValid = email.trim().isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
                        val isPassValid = password.isNotEmpty() && password.length >= 6
                        val isConfirmValid = confirmPassword == password

                        if (!isNameValid) nameError = "Please enter your name"
                        if (!isEmailValid) emailError = "Please enter a valid email"
                        if (!isPassValid) passwordError = "Password must be at least 6 characters"
                        if (!isConfirmValid) confirmPasswordError = "Passwords do not match"

                        if (isNameValid && isEmailValid && isPassValid && isConfirmValid) {
                            viewModel.signup(fullName.trim(), email.trim(), password)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation back to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Login",
                    color = BlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DisclaimerText()
        }
    }
}
