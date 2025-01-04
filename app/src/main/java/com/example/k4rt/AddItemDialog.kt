package com.example.k4rt

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AddItemDialog(context: Context, private val onSaveListener: OnSaveListener) : Dialog(context) {

    private lateinit var etItemName: EditText
    private lateinit var etItemQuantity: EditText
    private lateinit var etItemPrice: EditText
    private lateinit var btnSave: TextView
    private lateinit var btnCancel:TextView

    interface OnSaveListener {
        fun onSave(item: Item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_item)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        etItemName = findViewById(R.id.etItemName)
        etItemQuantity = findViewById(R.id.etItemQuantity)
        etItemPrice = findViewById(R.id.etItemPrice)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        etItemPrice.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etItemPrice.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                    val formatted = if (cleanString.isNotEmpty()) {
                        formatCurrency(cleanString.toLong())
                    } else {
                        ""
                    }

                    current = formatted
                    etItemPrice.setText(formatted)
                    etItemPrice.setSelection(formatted.length)

                    etItemPrice.addTextChangedListener(this)
                }
            }
        })

        btnSave.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val quantityStr = etItemQuantity.text.toString().trim()
            val priceStr = etItemPrice.text.toString().trim().replace(".", "")

            if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toIntOrNull() ?: 0
            val price = priceStr.toDoubleOrNull() ?: 0.0

            onSaveListener.onSave(Item("0", name, quantity, price))
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    fun prefillData(item: Item) {
        etItemName.setText(item.name)
        etItemQuantity.setText(item.quantity.toString())
        etItemPrice.setText(formatCurrency(item.price.toLong()))
    }

    private fun formatCurrency(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        return decimalFormat.format(amount)
    }
}
