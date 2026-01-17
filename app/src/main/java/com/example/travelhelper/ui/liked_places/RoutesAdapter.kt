package com.example.travelhelper.ui.liked_places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travelhelper.data.data_model.RouteModel
import com.example.travelhelper.databinding.ImbededRoutesItemBinding

class RoutesAdapter (
    private val clickListener: ItemClickListener
) : ListAdapter<RouteModel, RoutesAdapter.ViewHolder>(DIFF_CALLBACK) {


    // ДОБАВЛЕНО: структура для группировки
    sealed class RecyclerItem {
        data class Header(val category: String) : RecyclerItem()
        data class Divider(val category: String) : RecyclerItem()
        data class Item(val routeItem: RouteModel) : RecyclerItem()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ImbededRoutesItemBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {

            routeName.text = item.routeName
            val routeDuration = item.duration.substring(0, item.duration.length - 1)
            routeType.text = "${item.type} ${formatDistance(item.distanceMeters)} ${formatDuration(routeDuration.toInt())}"

            routeDesc.text = item.description

            }
    }

    inner class ViewHolder(val binding: ImbededRoutesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val routeItem = getItem(adapterPosition)
                    clickListener.onRouteClicked(routeItem)
                }
            }
        }
    }

    fun setData(currencies: List<RouteModel>) {
        submitList(currencies.toMutableList())
    }

    interface ItemClickListener {
        fun onRouteClicked(item: RouteModel)
    }

    private fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}ч ${minutes}м"
            else -> "${minutes}м"
        }
    }

    private fun formatDistance(meters: Int): String {
        return when {
            meters >= 1000 -> "${"%.1f".format(meters / 1000.0)} км"
            else -> "$meters м"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<RouteModel> =
            object : DiffUtil.ItemCallback<RouteModel>() {
                override fun areItemsTheSame(
                    oldItem: RouteModel,
                    newItem: RouteModel
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: RouteModel,
                    newItem: RouteModel
                ): Boolean {
                    return oldItem == newItem
                }

            }
    }

}