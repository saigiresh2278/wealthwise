package com.example.wealthwiseai.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object ReceiptParser {
    fun parse(text: String): ParsedReceipt {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        val amount = extractAmount(lines)
        val merchant = extractMerchant(lines)
        val date = extractDate(text) ?: System.currentTimeMillis()
        val category = suggestCategory(merchant, text)
        
        return ParsedReceipt(amount, merchant, date, category)
    }

    private fun extractAmount(lines: List<String>): Double {
        val amountPattern = Pattern.compile("(?i)(total|due|paid|net|grand|amount|sum)\\b")
        val numberPattern = Pattern.compile("\\b\\d+[,.]\\d{2}\\b")

        var bestMatch: Double? = null

        // 1. Look for lines with 'total' etc., and find the closest number on that line or next line
        for (i in lines.indices) {
            val line = lines[i]
            if (amountPattern.matcher(line).find()) {
                val match = findDecimalInString(line)
                if (match != null) {
                    bestMatch = match
                    break
                }
                if (i + 1 < lines.size) {
                    val nextLine = lines[i + 1]
                    val nextMatch = findDecimalInString(nextLine)
                    if (nextMatch != null) {
                        bestMatch = nextMatch
                        break
                    }
                }
            }
        }

        if (bestMatch != null) {
            return bestMatch
        }

        // 2. Fallback: find the largest number that looks like a decimal price/amount
        val allAmounts = mutableListOf<Double>()
        for (line in lines) {
            val matcher = numberPattern.matcher(line)
            while (matcher.find()) {
                val cleaned = matcher.group().replace(",", ".")
                val value = cleaned.toDoubleOrNull()
                if (value != null && value < 100000.0) { // Keep below 100k to ignore transaction references
                    allAmounts.add(value)
                }
            }
        }

        if (allAmounts.isNotEmpty()) {
            return allAmounts.maxOrNull() ?: 0.0
        }

        return 0.0
    }

    private fun findDecimalInString(str: String): Double? {
        val numberPattern = Pattern.compile("\\b\\d+[,.]\\d{2}\\b")
        val matcher = numberPattern.matcher(str)
        if (matcher.find()) {
            return matcher.group().replace(",", ".").toDoubleOrNull()
        }
        return null
    }

    private fun extractMerchant(lines: List<String>): String {
        if (lines.isEmpty()) return "Unknown Merchant"
        val datePattern = Pattern.compile("\\d{2,4}[-/.]\\d{2}[-/.]\\d{2,4}")
        val timePattern = Pattern.compile("\\d{2}:\\d{2}")
        
        for (line in lines.take(3)) {
            if (!datePattern.matcher(line).find() && 
                !timePattern.matcher(line).find() && 
                line.length > 2 && 
                !line.contains("receipt", ignoreCase = true) &&
                !line.contains("tax", ignoreCase = true) &&
                !line.contains("invoice", ignoreCase = true)) {
                return line
            }
        }
        return lines.firstOrNull() ?: "Unknown Merchant"
    }

    private fun extractDate(text: String): Long? {
        val dateRegexes = listOf(
            "\\b(\\d{4})[-/.](\\d{2})[-/.](\\d{2})\\b" to "yyyy-MM-dd",
            "\\b(\\d{2})[-/.](\\d{2})[-/.](\\d{4})\\b" to "dd-MM-yyyy",
            "\\b(\\d{2})[-/.](\\d{2})[-/.](\\d{2})\\b" to "dd-MM-yy"
        )

        for ((regex, format) in dateRegexes) {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val dateStr = matcher.group()
                val cleanDateStr = dateStr.replace("/", "-").replace(".", "-")
                val sdf = SimpleDateFormat(format.replace("/", "-").replace(".", "-"), Locale.US)
                try {
                    val date = sdf.parse(cleanDateStr)
                    if (date != null) {
                        return date.time
                    }
                } catch (e: Exception) {
                    if (format == "dd-MM-yyyy") {
                        try {
                            val altSdf = SimpleDateFormat("MM-dd-yyyy", Locale.US)
                            val date = altSdf.parse(cleanDateStr)
                            if (date != null) return date.time
                        } catch (e2: Exception) {}
                    }
                }
            }
        }
        return null
    }

    private fun suggestCategory(merchant: String, text: String): String {
        val combined = "$merchant $text".lowercase()
        return when {
            combined.contains("cafe") || combined.contains("restaurant") || combined.contains("pizza") ||
            combined.contains("burger") || combined.contains("mcdonald") || combined.contains("starbucks") ||
            combined.contains("eat") || combined.contains("food") || combined.contains("canteen") || combined.contains("coffee") ||
            combined.contains("dining") -> "Food"

            combined.contains("uber") || combined.contains("taxi") || combined.contains("flight") ||
            combined.contains("railway") || combined.contains("train") || combined.contains("fuel") ||
            combined.contains("petrol") || combined.contains("metro") || combined.contains("travel") ||
            combined.contains("ola") || combined.contains("airline") || combined.contains("transport") -> "Travel"

            combined.contains("rent") || combined.contains("lease") || combined.contains("housing") -> "Rent"

            combined.contains("school") || combined.contains("college") || combined.contains("course") ||
            combined.contains("udemy") || combined.contains("book") || combined.contains("education") ||
            combined.contains("training") || combined.contains("tuition") -> "Education"

            combined.contains("walmart") || combined.contains("amazon") || combined.contains("target") ||
            combined.contains("mall") || combined.contains("clothing") || combined.contains("shoe") ||
            combined.contains("shopping") || combined.contains("groceries") || combined.contains("flipkart") ||
            combined.contains("store") || combined.contains("market") || combined.contains("supermarket") -> "Shopping"

            combined.contains("electricity") || combined.contains("water") || combined.contains("gas") ||
            combined.contains("internet") || combined.contains("telecom") || combined.contains("phone") ||
            combined.contains("bill") || combined.contains("utility") || combined.contains("recharge") ||
            combined.contains("wifi") || combined.contains("power") || combined.contains("mobile") -> "Bills"

            combined.contains("hospital") || combined.contains("pharmacy") || combined.contains("medical") ||
            combined.contains("doctor") || combined.contains("dentist") || combined.contains("clinic") ||
            combined.contains("medicine") || combined.contains("health") -> "Health"

            combined.contains("investment") || combined.contains("stock") || combined.contains("mutual fund") ||
            combined.contains("crypto") || combined.contains("share") || combined.contains("brokerage") -> "Investment"

            combined.contains("salary") || combined.contains("paycheck") || combined.contains("income") -> "Salary"

            combined.contains("freelance") || combined.contains("upwork") || combined.contains("fiverr") ||
            combined.contains("gigs") -> "Freelance"

            else -> "Others"
        }
    }
}

data class ParsedReceipt(
    val amount: Double,
    val merchant: String,
    val date: Long,
    val category: String
)
