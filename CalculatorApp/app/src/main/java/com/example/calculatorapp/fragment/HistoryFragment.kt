package com.example.calculatorapp.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculatorapp.adapter.Adapter
import com.example.calculatorapp.databinding.FragmentHistoryBinding
import com.example.calculatorapp.model.Sonuclar

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapterHistory: Adapter
    private lateinit var sonuclarList: ArrayList<Sonuclar>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sonuclarList = ArrayList()

        sharedPreferences = requireContext().getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

        val operationsSet = sharedPreferences.getStringSet("operations", setOf()) ?: setOf()
        val operationsList = operationsSet.toList()

        for (operation in operationsList) {
            val sonuclar = Sonuclar(operation)
            sonuclarList.add(sonuclar)
        }

        adapterHistory = Adapter(sonuclarList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapterHistory
    }
}
