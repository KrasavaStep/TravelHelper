package com.example.travelhelper.ui
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.data.data_model.AttractionModel

// onItemClicked пока не используется, но нужен для будущего
class SearchAdapter(private val onItemClicked: (AttractionModel) -> Unit) :
    ListAdapter<AttractionModel, SearchAdapter.SearchViewHolder>(AttractionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        // Используем самый простой стандартный макет для одного элемента
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val attraction = getItem(position)
        holder.bind(attraction)
        holder.itemView.setOnClickListener {
            onItemClicked(attraction)
        }
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // В макете simple_list_item_1 есть один TextView с системным id 'text1'
        private val nameTextView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(attraction: AttractionModel) {
            nameTextView.text = attraction.name
        }
    }
}

// Класс для эффективного сравнения элементов списка
class AttractionDiffCallback : DiffUtil.ItemCallback<AttractionModel>() {
    override fun areItemsTheSame(oldItem: AttractionModel, newItem: AttractionModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AttractionModel, newItem: AttractionModel): Boolean {
        return oldItem == newItem
    }
}
