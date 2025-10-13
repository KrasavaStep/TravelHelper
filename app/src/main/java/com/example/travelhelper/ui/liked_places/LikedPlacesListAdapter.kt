package com.example.travelhelper.ui.liked_places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.R
import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.network.overpass_api.OSMPlace
import com.example.travelhelper.databinding.LikedAttractionsItemBinding

class LikedPlacesListAdapter(
    private val clickListener: ItemClickListener
) : ListAdapter<AttractionModel, LikedPlacesListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LikedAttractionsItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            nameText.text = item.name
            //val nameText = holder.itemView.context.getString(R.string.get_more_info)
            //val typeText = holder.itemView.context.getString(R.string.get_more_info)

            when (item.category) {
                "memorial" -> {
                    itemIcon.setImageResource(R.drawable.memorial)
                    typeText.text = "Мемориал"
                }
                "monument" -> {
                    itemIcon.setImageResource(R.drawable.monument)
                    typeText.text = "Монумент"
                }
                "museum" -> {
                    itemIcon.setImageResource(R.drawable.museum)
                    typeText.text = "Музей"
                }
                "castle" -> {
                    itemIcon.setImageResource(R.drawable.castle)
                    typeText.text = "Замок"
                }
                else -> {
                    itemIcon.setImageResource(R.drawable.map_marker_svg)
                    typeText.text = "Иное"
                }
            }

        }
    }

    inner class ViewHolder(val binding: LikedAttractionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val attractionItem = getItem(adapterPosition)
                    clickListener.onAttractionClicked(attractionItem)
                }
            }
        }
    }

    fun setData(currencies: List<AttractionModel>) {
        submitList(currencies.toMutableList())
    }

    interface ItemClickListener {
        fun onAttractionClicked(item: AttractionModel)
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<AttractionModel> =
            object : DiffUtil.ItemCallback<AttractionModel>() {
                override fun areItemsTheSame(
                    oldItem: AttractionModel,
                    newItem: AttractionModel
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: AttractionModel,
                    newItem: AttractionModel
                ): Boolean {
                    return oldItem == newItem
                }

            }
    }

}