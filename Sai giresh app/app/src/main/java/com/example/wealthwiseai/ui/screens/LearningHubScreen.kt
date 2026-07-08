package com.example.wealthwiseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*

data class LearningTopic(
    val title: String,
    val explanation: String,
    val whyItMatters: String,
    val beginnerTips: List<String>,
    val riskNote: String
)

@Composable
fun LearningHubScreen() {
    val topics = listOf(
        LearningTopic(
            title = "Budgeting Basics",
            explanation = "Budgeting is the process of tracking where your money comes from and allocating it intentionally towards expenses, savings, and investments. A popular rule is the 50/30/20 budget (50% needs, 30% wants, 20% savings).",
            whyItMatters = "Without a budget, it is easy to overspend and live paycheck-to-paycheck, making it impossible to accumulate long-term wealth.",
            beginnerTips = listOf(
                "Categorize your fixed costs first (Rent, bills).",
                "Use tracking apps (like WealthWise AI!) daily to stay accountably disciplined.",
                "Review and adjust budget ceilings monthly."
            ),
            riskNote = "Low Risk. Budgeting only requires discipline; there is no capital loss risk."
        ),
        LearningTopic(
            title = "Emergency Fund",
            explanation = "An emergency fund is liquid money set aside to cover unexpected events like medical emergencies, job loss, or car repairs. It typically covers 3 to 6 months of living expenses.",
            whyItMatters = "It acts as a financial shield, preventing you from going into debt or selling investments prematurely when life surprises you.",
            beginnerTips = listOf(
                "Aim for a starter buffer of $1000, then grow it to cover 6 months of expenses.",
                "Keep this money in a high-yield, easily accessible savings account or liquid fund.",
                "Never use this buffer for discretionary desires (shopping, holidays)."
            ),
            riskNote = "Low Risk. Keeping cash in savings or liquid accounts preserves principal value."
        ),
        LearningTopic(
            title = "Savings Habit",
            explanation = "Savings rate is the percentage of income left over after expenses. Cultivating a savings habit means paying yourself first—saving a fixed portion of your income as soon as it arrives, rather than saving whatever happens to be left at the end of the month.",
            whyItMatters = "Consistency is far more powerful than timing. Automating savings turns financial discipline into a background process.",
            beginnerTips = listOf(
                "Set up an automatic monthly transfer on payday.",
                "Start small (even 5-10%) and scale up as your income grows.",
                "Celebrate milestones to reinforce positive behavior."
            ),
            riskNote = "Low Risk. Consistent savings in bank deposits are highly secure."
        ),
        LearningTopic(
            title = "Fixed Deposit",
            explanation = "A Fixed Deposit (FD) is a financial instrument where you lock a lump sum of money with a bank for a fixed tenure at a guaranteed interest rate.",
            whyItMatters = "It offers highly predictable, low-risk returns, making it excellent for short-term targets where capital safety is the absolute priority.",
            beginnerTips = listOf(
                "Use FDs for money you will need within 1 to 3 years.",
                "Check premature withdrawal penalty terms in case you need cash earlier.",
                "Compare interest rates across standard reputable banking institutions."
            ),
            riskNote = "Very Low Risk. The principal and interest are guaranteed by banking regulators up to statutory limits."
        ),
        LearningTopic(
            title = "Mutual Funds",
            explanation = "A mutual fund pools money from many investors to purchase a diversified portfolio of securities (like stocks, bonds, or short-term debt). The fund is managed by professional fund managers.",
            whyItMatters = "It allows beginners to benefit from professional management and diversification without needing large sums of capital or expert stock-picking skills.",
            beginnerTips = listOf(
                "Decide between Equity Mutual Funds (for growth) and Debt Mutual Funds (for stability).",
                "Prefer Direct plans over Regular plans to save on commissions.",
                "Check the expense ratio; lower expense ratios mean higher net returns for you."
            ),
            riskNote = "Moderate to High Risk. Mutual funds are subject to market risks. The value of investments can fluctuate depending on stock/bond performance."
        ),
        LearningTopic(
            title = "SIP (Systematic Investment Plan)",
            explanation = "An SIP is a method of investing in mutual funds where you commit a fixed sum of money at regular intervals (monthly, weekly) instead of making a lump-sum contribution.",
            whyItMatters = "It encourages discipline and utilizes 'Rupee-Cost Averaging'—buying more units when prices are low and fewer units when prices are high, smoothing out volatility.",
            beginnerTips = listOf(
                "Link SIPs directly to your bank account for automated wealth building.",
                "Do not stop SIPs during market downturns; that is when your money buys the most units.",
                "Step up your SIP amounts annually in line with salary increments."
            ),
            riskNote = "Moderate Risk. While SIPs reduce market timing risks, they are still linked to market fluctuations."
        ),
        LearningTopic(
            title = "Stocks Basics",
            explanation = "A stock (or share) represents partial ownership in a corporation. When you buy a stock, you own a tiny slice of that business. Wealth is built through stock price appreciation and dividends.",
            whyItMatters = "Historically, equities have delivered the highest long-term returns, beating inflation and significantly compounding wealth.",
            beginnerTips = listOf(
                "Focus on buying established, stable blue-chip companies as a beginner.",
                "Invest for the long term (5+ years) rather than short-term trading.",
                "Understand the company's business model before buying its shares."
            ),
            riskNote = "High Risk. Single stocks can experience severe price drops or go to zero if the company fails. Capital is not guaranteed."
        ),
        LearningTopic(
            title = "Diversification",
            explanation = "Diversification means spreading your investment capital across different asset classes (equities, debt, gold, real estate) and sectors to manage volatility.",
            whyItMatters = "It ensures that if one sector or asset class performs poorly, gains in other areas can offset the losses, protecting your total portfolio.",
            beginnerTips = listOf(
                "Never invest all your savings in a single stock or sector.",
                "Maintain a mix of equity, debt, and liquid cash according to your risk capacity.",
                "Use diversified index funds as a solid core foundation."
            ),
            riskNote = "Lowers Risk. Diversification reduces portfolio volatility but does not eliminate market risk entirely."
        ),
        LearningTopic(
            title = "Risk Management",
            explanation = "Risk management is the process of identifying, analyzing, and taking steps to reduce exposure to financial losses. It involves insurance, appropriate asset allocation, and matching investment horizons to objectives.",
            whyItMatters = "Protecting what you have is just as important as growing it. Good defense wins championships.",
            beginnerTips = listOf(
                "Ensure you have adequate term life and health insurance coverage.",
                "Match your investment risk to your time horizon; short-term goals should remain low-risk.",
                "Keep clear of speculative bubbles or get-rich-quick opportunities."
            ),
            riskNote = "Lowers Risk. Actively managing risk shields your financial foundation from catastrophic events."
        ),
        LearningTopic(
            title = "Financial Discipline",
            explanation = "Financial discipline is the habit of making choices aligned with your long-term goals, rather than acting on short-term emotional impulses.",
            whyItMatters = "A great investment strategy fails if you consistently overspend. Your habits determine your final financial destination.",
            beginnerTips = listOf(
                "Implement a 24-hour rule before making any major discretionary purchases.",
                "Review your financial statement updates monthly.",
                "Focus on financial security over showing off status."
            ),
            riskNote = "Low Risk. Requires behavioral commitment rather than financial speculation."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "WealthWise AI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = BlueAccent,
                letterSpacing = 1.sp
            )
            SectionHeader("Learning Hub")
            Text(
                text = "Build your financial literacy with simple, bite-sized tutorials on compounding, safety buffers, and asset classes.",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(topics) { topic ->
            ExpandableTopicCard(topic = topic)
        }

        item {
            DisclaimerText(modifier = Modifier.padding(vertical = 12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ExpandableTopicCard(topic: LearningTopic) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topic.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "What is it?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent
                    )
                    Text(
                        text = topic.explanation,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 8.dp),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "Why it matters?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )
                    Text(
                        text = topic.whyItMatters,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 8.dp),
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "Beginner Tips:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    topic.beginnerTips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(vertical = 1.dp),
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Risk Note:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedAccent
                    )
                    Text(
                        text = topic.riskNote,
                        fontSize = 12.sp,
                        color = TextGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
