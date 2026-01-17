package com.example.travelhelper.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.databinding.ItemSearchSuggestionBinding

class SearchAdapter(
    private val onItemClick: (AttractionModel) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var items: List<AttractionModel> = emptyList()

    fun submitList(newItems: List<AttractionModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.suggestionTitle.text = item.name
        holder.binding.suggestionCategory.text = item.category
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemSearchSuggestionBinding) : 
        RecyclerView.ViewHolder(binding.root)
}