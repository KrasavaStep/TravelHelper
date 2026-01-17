package com.example.travelhelper.ui.liked_places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.databinding.ImbededRoutesItemBinding
import com.example.travelhelper.databinding.ItemRouteHeaderBinding

class RoutesAdapter(
    private val clickListener: ItemClickListener
) : ListAdapter<RoutesAdapter.RecyclerItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    sealed class RecyclerItem {
        data class Header(val category: String) : RecyclerItem()
        data class Item(val routeItem: RouteModel) : RecyclerItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecyclerItem.Header -> TYPE_HEADER
            is RecyclerItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemRouteHeaderBinding.inflate(layoutInflater, parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ImbededRoutesItemBinding.inflate(layoutInflater, parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind((item as RecyclerItem.Header).category)
            is ItemViewHolder -> holder.bind((item as RecyclerItem.Item).routeItem)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemRouteHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            binding.headerTitle.text = category
        }
    }

    inner class ItemViewHolder(private val binding: ImbededRoutesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RouteModel) {
            binding.apply {
                routeName.text = item.routeName
                val durationValue = try {
                    item.duration.substring(0, item.duration.length - 1).toInt()
                } catch (e: Exception) { 0 }
                routeType.text = "${item.type} ${formatDistance(item.distanceMeters)} ${formatDuration(durationValue)}"
                routeDesc.text = item.description
                root.setOnClickListener { clickListener.onRouteClicked(item) }
            }
        }
    }

    fun setData(routes: List<RouteModel>) {
        val items = mutableListOf<RecyclerItem>()
        val grouped = routes.groupBy { it.routeCategory }
        grouped.forEach { (category, routesInCategory) ->
            items.add(RecyclerItem.Header(category))
            items.addAll(routesInCategory.map { RecyclerItem.Item(it) })
        }
        submitList(items)
    }

    interface ItemClickListener {
        fun onRouteClicked(item: RouteModel)
    }

    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) "${hours}ч ${minutes}м" else "${minutes}м"
    }

    private fun formatDistance(meters: Int): String {
        return if (meters >= 1000) "%.1f км".format(meters / 1000.0) else "$meters м"
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecyclerItem>() {
            override fun areItemsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean {
                return when {
                    oldItem is RecyclerItem.Header && newItem is RecyclerItem.Header -> oldItem.category == newItem.category
                    oldItem is RecyclerItem.Item && newItem is RecyclerItem.Item -> oldItem.routeItem.id == newItem.routeItem.id
                    else -> false
                }
            }
            override fun areContentsTheSame(oldItem: RecyclerItem, newItem: RecyclerItem): Boolean = oldItem == newItem
        }
    }
}
