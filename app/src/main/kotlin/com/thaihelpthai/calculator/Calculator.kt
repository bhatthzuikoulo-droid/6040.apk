package com.thaihelpthai.calculator

import java.time.LocalDate

data class TransactionRecord(
    val date: LocalDate,
    val amount: Double,
    val governmentPay: Double,
    val userPay: Double
)

class Calculator {
    companion object {
        private const val GOVERNMENT_RATE = 0.60
        private const val USER_RATE = 0.40
        private const val DAILY_LIMIT = 200.0
        private const val MONTHLY_LIMIT = 1000.0
        private const val TOTAL_QUOTA = 4000.0
        private const val MONTHLY_ALLOCATION = 1000.0
    }

    private val transactions = mutableListOf<TransactionRecord>()
    private var currentMonth = LocalDate.now().monthValue
    private var currentYear = LocalDate.now().year

    fun calculatePayment(amount: Double): CalculationResult {
        val today = LocalDate.now()

        // ตรวจสอบว่าเปลี่ยนเดือนหรือไม่
        if (today.monthValue != currentMonth || today.year != currentYear) {
            resetMonthlyData()
            currentMonth = today.monthValue
            currentYear = today.year
        }

        // ตรวจสอบจำนวนเงินที่ถูกต้อง
        if (amount <= 0) {
            return CalculationResult(
                success = false,
                error = "กรุณากรอกจำนวนเงินที่ถูกต้อง",
                amount = 0.0,
                governmentPay = 0.0,
                userPay = 0.0
            )
        }

        // ตรวจสอบเกินวงเงินรายวัน
        val dailyUsed = transactions
            .filter { it.date == today }
            .sumOf { it.amount }

        if (dailyUsed + amount > DAILY_LIMIT) {
            return CalculationResult(
                success = false,
                error = "เกินวงเงินรายวัน 200 บาท (ใช้ไปแล้ว: ${dailyUsed} บาท)",
                amount = 0.0,
                governmentPay = 0.0,
                userPay = 0.0
            )
        }

        // ตรวจสอบเกินวงเงินรายเดือน
        val monthlyUsed = transactions
            .filter { it.date.monthValue == today.monthValue && it.date.year == today.year }
            .sumOf { it.amount }

        if (monthlyUsed + amount > MONTHLY_LIMIT) {
            return CalculationResult(
                success = false,
                error = "เกินวงเงินรายเดือน 1,000 บาท (ใช้ไปแล้ว: ${monthlyUsed} บาท)",
                amount = 0.0,
                governmentPay = 0.0,
                userPay = 0.0
            )
        }

        // คำนวณค่าจ่าย
        val governmentPay = (amount * GOVERNMENT_RATE).roundToTwoDecimals()
        val userPay = (amount * USER_RATE).roundToTwoDecimals()

        // บันทึกธุรกรรม
        transactions.add(
            TransactionRecord(
                date = today,
                amount = amount,
                governmentPay = governmentPay,
                userPay = userPay
            )
        )

        return CalculationResult(
            success = true,
            error = null,
            amount = amount,
            governmentPay = governmentPay,
            userPay = userPay,
            remainingDaily = DAILY_LIMIT - (dailyUsed + amount),
            remainingMonthly = MONTHLY_LIMIT - (monthlyUsed + amount),
            usedMonthly = monthlyUsed + amount
        )
    }

    fun getQuotaStatus(): QuotaStatus {
        val today = LocalDate.now()
        val dailyUsed = transactions
            .filter { it.date == today }
            .sumOf { it.amount }

        val monthlyUsed = transactions
            .filter { it.date.monthValue == today.monthValue && it.date.year == today.year }
            .sumOf { it.amount }

        val totalUsed = transactions.sumOf { it.amount }

        return QuotaStatus(
            dailyUsed = dailyUsed,
            remainingDaily = DAILY_LIMIT - dailyUsed,
            monthlyUsed = monthlyUsed,
            remainingMonthly = MONTHLY_LIMIT - monthlyUsed,
            totalUsed = totalUsed,
            remainingTotal = TOTAL_QUOTA - totalUsed,
            totalQuota = TOTAL_QUOTA
        )
    }

    fun getTransactions(): List<TransactionRecord> = transactions.toList()

    fun clearHistory() {
        transactions.clear()
    }

    private fun resetMonthlyData() {
        // เก็บธุรกรรมเก่าไว้แต่เริ่มนับใหม่เดือนใหม่
    }

    private fun Double.roundToTwoDecimals(): Double {
        return kotlin.math.round(this * 100) / 100
    }
}

data class CalculationResult(
    val success: Boolean,
    val error: String? = null,
    val amount: Double,
    val governmentPay: Double,
    val userPay: Double,
    val remainingDaily: Double = 0.0,
    val remainingMonthly: Double = 0.0,
    val usedMonthly: Double = 0.0
)

data class QuotaStatus(
    val dailyUsed: Double,
    val remainingDaily: Double,
    val monthlyUsed: Double,
    val remainingMonthly: Double,
    val totalUsed: Double,
    val remainingTotal: Double,
    val totalQuota: Double
)