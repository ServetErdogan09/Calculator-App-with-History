package com.example.calculatorapp.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import com.example.calculatorapp.R
import com.example.calculatorapp.databinding.FragmentCalculatorBinding
import org.apache.commons.jexl3.*
import java.math.BigDecimal

class CalculatorFragment : Fragment() {

    private lateinit var binding: FragmentCalculatorBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var currentExpression: StringBuilder = StringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argumentlardan gelen veriyi al
        val data = arguments?.getString("ITEM_DATA")
        setupButtons()
        setupSharedPreferences()
        setupMenu()

        // Veriyi hesap makinesi ekranına yerleştirin
        if (data != null) {
            currentExpression.append(data)
            binding.yazdirText.text = data
            calculateResult()
        }
    }

    private fun setupButtons() {
        // Sayı butonları
        binding.button0.setOnClickListener { appendToExpression("0") }
        binding.button1.setOnClickListener { appendToExpression("1") }
        binding.button2.setOnClickListener { appendToExpression("2") }
        binding.button3.setOnClickListener { appendToExpression("3") }
        binding.button4.setOnClickListener { appendToExpression("4") }
        binding.button5.setOnClickListener { appendToExpression("5") }
        binding.button6.setOnClickListener { appendToExpression("6") }
        binding.button7.setOnClickListener { appendToExpression("7") }
        binding.button8.setOnClickListener { appendToExpression("8") }
        binding.button9.setOnClickListener { appendToExpression("9") }

        // İşlem butonları
        binding.buttonToplama.setOnClickListener { appendToExpression("+") }
        binding.buttonEksi.setOnClickListener { appendToExpression("-") }
        binding.buttonCarpma.setOnClickListener { appendToExpression("*") }
        binding.buttonBolme.setOnClickListener { appendToExpression("/") }
        binding.buttonsolparantez.setOnClickListener { appendToExpression("(") }
        binding.buttonsagparantez.setOnClickListener { appendToExpression(")") }
        binding.buttonVirgul.setOnClickListener { appendToExpression(".") }

        // Silme ve sıfırlama
        binding.buttonSil.setOnClickListener { deleteLastCharacter() }
        binding.buttonSifirla.setOnClickListener { resetCalculator() }

        // Sonuç butonu
        binding.buttonEsitir.setOnClickListener {
            binding.yazdirText.textSize = 30F
            binding.sonucText.textSize = 40F
            calculateResult()
        }
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_history, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.history_menu -> {
                        findNavController().navigate(R.id.action_calculatorFragment_to_historyFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun appendToExpression(value: String) {
        if (currentExpression.isNotEmpty() && (currentExpression.last().isDigit() || currentExpression.last() == ')')) {
            if (value == "/") {
                currentExpression.append("/")
            } else {
                currentExpression.append(value)
            }
        } else {
            currentExpression.append(value)
        }
        binding.yazdirText.text = currentExpression.toString()
    }

    private fun deleteLastCharacter() {
        val textDeger = binding.yazdirText.text.toString()
        val yeniDeger = if (textDeger.isNotEmpty()) textDeger.dropLast(1) else ""

        if (yeniDeger.isEmpty()){
            binding.yazdirText.text = "0"
            binding.sonucText.text = ""
            binding.yazdirText.textSize = 40F
            binding.sonucText.textSize = 30F
        }else{
            binding.yazdirText.text = yeniDeger
        }
        currentExpression.clear()
        currentExpression.append(yeniDeger)

        if (yeniDeger.isNotEmpty()) {
            val sonKarakter = yeniDeger.last()
            if (sonKarakter !in listOf('/', '*', '+', '-')) {
                calculateResult()
            }
        } else {
            binding.yazdirText.text = "0"
        }
    }

    private fun resetCalculator() {
        currentExpression.clear()
        binding.yazdirText.text = "0"
        binding.sonucText.text = ""
    }

    private fun calculateResult() {
        try {
            val result = evaluateExpression(currentExpression.toString())
            val deger = result.toDouble()
            val formattedResult = if (deger % 1 == 0.0) {
                String.format("= %.0f", result)
            } else {
                String.format("= %.8f", result)
            }
            binding.sonucText.text = formattedResult
            saveOperation(currentExpression.toString())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            binding.sonucText.text = "Hata"
        }
    }

    private fun evaluateExpression(expression: String): BigDecimal {
        val jexl: JexlEngine = JexlBuilder().create()
        val context: JexlContext = MapContext()
        return try {
            val expressionWithDecimals = expression.replace(Regex("(?<!\\d)\\d+(?!\\d)")) { matchResult ->
                "${matchResult.value}.0"
            }
            val jexlExpression: JexlExpression = jexl.createExpression(expressionWithDecimals)
            when (val result = jexlExpression.evaluate(context)) {
                is BigDecimal -> result
                is Number -> BigDecimal(result.toString())
                else -> throw IllegalArgumentException("Hatalı")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Hatalı")
        }
    }

    private fun saveOperation(operation: String) {
        val operations = sharedPreferences.getStringSet("operations", mutableSetOf())?.toMutableList() ?: mutableListOf()
        if (operations.size >= 10) {
            operations.removeAt(0)
        }
        operations.add(operation)
        sharedPreferences.edit().putStringSet("operations", operations.toSet()).apply()
    }

}
