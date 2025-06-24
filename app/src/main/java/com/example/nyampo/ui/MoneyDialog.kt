package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.example.nyampo.R

object MoneyDialog {
    fun show(context: Context, userMoney: Int) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_money, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val backBtn = view.findViewById<ImageButton>(R.id.imageButton_back)
        val accountInput = view.findViewById<EditText>(R.id.editText_account)
        val amountInput = view.findViewById<EditText>(R.id.editText_amount)
        val transferBtn = view.findViewById<Button>(R.id.transferMoney)
        val moneyText = view.findViewById<TextView>(R.id.Money)

        // 보유 금액 표시
        moneyText.text = "잔액: ${userMoney} 시루"

        backBtn.setOnClickListener {
            dialog.dismiss()
        }

        transferBtn.setOnClickListener {
            val account = accountInput.text.toString().trim()
            val amountText = amountInput.text.toString().trim()

            if (account.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(context, "계좌번호와 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toIntOrNull()
            if (amount == null) {
                Toast.makeText(context, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount > userMoney) {
                Toast.makeText(context, "보유 금액보다 많습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "이체 완료!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
