package com.example.calculatorapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.calculatorapp.databinding.GecmisTasarimBinding
import com.example.calculatorapp.model.Sonuclar

class Adapter(
    private val list: List<Sonuclar>,
    private val fragment: Fragment
) : RecyclerView.Adapter<Adapter.IslemViewHolder>() {

    class IslemViewHolder(val binding: GecmisTasarimBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IslemViewHolder {
        val binding = GecmisTasarimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IslemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: IslemViewHolder, position: Int) {
        val islem = list[position].islem
        holder.binding.islemText.text = islem

        holder.binding.islemText.setOnClickListener {
            val action = HistoryFragmentDirections.actionHistoryFragmentToCalculatorFragment(islem)
            fragment.findNavController().navigate(action)
        }
    }
}
