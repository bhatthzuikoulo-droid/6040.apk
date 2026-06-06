package com.thaihelpthai.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var calculator: Calculator
    private lateinit var inputAmount: EditText
    private lateinit var txtGovernmentPay: TextView
    private lateinit var txtUserPay: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtRemainingDaily: TextView
    private lateinit var txtRemainingMonthly: TextView
    private lateinit var txtStatus: TextView
    private lateinit var btnCalculate: Button
    private lateinit var btnClear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calculator = Calculator()
        initViews()
        updateQuotaDisplay()
    }

    private fun initViews() {
        inputAmount = findViewById(R.id.input_amount)
        txtGovernmentPay = findViewById(R.id.txt_government_pay)
        txtUserPay = findViewById(R.id.txt_user_pay)
        txtTotal = findViewById(R.id.txt_total)
        txtRemainingDaily = findViewById(R.id.txt_remaining_daily)
        txtRemainingMonthly = findViewById(R.id.txt_remaining_monthly)
        txtStatus = findViewById(R.id.txt_status)
        btnCalculate = findViewById(R.id.btn_calculate)
        btnClear = findViewById(R.id.btn_clear)

        btnCalculate.setOnClickListener { onCalculateClick() }
        btnClear.setOnClickListener { onClearClick() }
    }

    private fun onCalculateClick() {
        val amountText = inputAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            txtStatus.text = "⚠️ กรุณากรอกจำนวนเงิน"
            txtStatus.setTextColor(getColor(R.color.error))
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            txtStatus.text = "⚠️ กรุณากรอกจำนวนเงินที่ถูกต้อง"
            txtStatus.setTextColor(getColor(R.color.error))
            return
        }

        val result = calculator.calculatePayment(amount)

        if (result.success) {
            txtGovernmentPay.text = "รัฐช่วยจ่าย (60%): ${String.format("%.2f", result.governmentPay)} บาท"
            txtUserPay.text = "ผู้ใช้จ่าย (40%): ${String.format("%.2f", result.userPay)} บาท"
            txtTotal.text = "รวมทั้งสิ้น: ${String.format("%.2f", result.amount)} บาท"
            
            txtRemainingDaily.text = "เหลือวันนี้: ${String.format("%.2f", result.remainingDaily)} บาท"
            txtRemainingMonthly.text = "เหลือเดือนนี้: ${String.format("%.2f", result.remainingMonthly)} บาท"

            txtStatus.text = "✅ ${result.error ?: "คำนวณสำเร็จ"}"
            txtStatus.setTextColor(getColor(R.color.success))

            inputAmount.text.clear()
            Toast.makeText(this, "✅ บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
        } else {
            txtStatus.text = "❌ ${result.error}"
            txtStatus.setTextColor(getColor(R.color.error))
            txtGovernmentPay.text = "รัฐช่วยจ่าย (60%): - บาท"
            txtUserPay.text = "ผู้ใช้จ่าย (40%): - บาท"
            txtTotal.text = "รวมทั้งสิ้น: - บาท"
        }
    }

    private fun onClearClick() {
        inputAmount.text.clear()
        txtStatus.text = ""
        updateQuotaDisplay()
    }

    private fun updateQuotaDisplay() {
        val status = calculator.getQuotaStatus()
        txtRemainingDaily.text = "เหลือวันนี้: ${String.format("%.2f", status.remainingDaily)} บาท"
        txtRemainingMonthly.text = "เหลือเดือนนี้: ${String.format("%.2f", status.remainingMonthly)} บาท"
    }
}