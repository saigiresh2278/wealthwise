package com.example.wealthwiseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ai.RiskAnalyzer
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GlassCard
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.RiskViewModel

@Composable
fun RiskAnalyzerScreen(
    viewModel: RiskViewModel,
    onBack: () -> Unit
) {
    val riskProfile by viewModel.riskProfile.collectAsState()

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateListOf<Int>() }
    var showResults by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Risk Profile Analyzer")

            if (!showResults && currentQuestionIndex < RiskAnalyzer.questions.size) {
                // Quiz Interface
                val question = RiskAnalyzer.questions[currentQuestionIndex]

                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${RiskAnalyzer.questions.size}",
                    fontSize = 12.sp,
                    color = BlueAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1) / RiskAnalyzer.questions.size.toFloat(),
                    color = BlueAccent,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(bottom = 24.dp)
                )

                GlassCard {
                    Text(
                        text = question.question,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    question.options.forEachIndexed { index, option ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    answers.add(index)
                                    if (currentQuestionIndex + 1 < RiskAnalyzer.questions.size) {
                                        currentQuestionIndex++
                                    } else {
                                        showResults = true
                                    }
                                }
                                .border(1.dp, TextWhite.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = option,
                                color = TextWhite,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                if (currentQuestionIndex > 0) {
                    TextButton(
                        onClick = {
                            currentQuestionIndex--
                            answers.removeAt(answers.size - 1)
                        }
                    ) {
                        Text("Back to Previous Question", color = TextGray)
                    }
                }
            } else {
                // Results Screen
                val result = if (answers.size == RiskAnalyzer.questions.size) {
                    RiskAnalyzer.analyzeRisk(answers)
                } else {
                    // Fallback to saved risk profile if available
                    val saved = riskProfile
                    if (saved != null) {
                        RiskAnalyzer.AnalysisResult(
                            score = saved.score,
                            riskClass = saved.riskClass,
                            explanation = "Risk calculated from your previous assessment.",
                            learningPath = when (saved.riskClass) {
                                "Low Risk" -> listOf("Budgeting basics", "Fixed deposit")
                                "Medium Risk" -> listOf("Mutual funds", "SIP")
                                else -> listOf("Stocks basics", "Diversification")
                            }
                        )
                    } else null
                }

                if (result != null) {
                    // Save risk profile inside database
                    LaunchedEffect(result) {
                        viewModel.saveRiskProfile(result.score, result.riskClass)
                    }

                    val color = when (result.riskClass) {
                        "Low Risk" -> GreenAccent
                        "Medium Risk" -> GoldAccent
                        else -> RedAccent
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "YOUR PROFILE CLASSIFICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.riskClass,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score: ${result.score} / 15",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = result.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Recommended Learning Path",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextWhite,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    result.learningPath.forEach { topic ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• $topic",
                                modifier = Modifier.padding(12.dp),
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Retake
                            answers.clear()
                            currentQuestionIndex = 0
                            showResults = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retake Risk Quiz", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Fallback to start a new quiz
                    Button(
                        onClick = {
                            answers.clear()
                            currentQuestionIndex = 0
                            showResults = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Risk Assessment Quiz", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DisclaimerText()

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
