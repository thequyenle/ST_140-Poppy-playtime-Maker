package com.charactor.avatar.maker.pfp.activity_app.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.helper.UnitHelper
import com.charactor.avatar.maker.pfp.data.model.custom.NavigationModel
import com.charactor.avatar.maker.pfp.databinding.ItemBottomNavigationBinding

class BottomNavigationCustomizeAdapter(private val context: Context) : ListAdapter<NavigationModel, BottomNavigationCustomizeAdapter.BottomNavViewHolder>(DiffCallback) {
    var onItemClick: (Int) -> Unit = {}
    inner class BottomNavViewHolder(
        private val binding: ItemBottomNavigationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavigationModel, position: Int) = with(binding) {
            val margin = if (item.isSelected) UnitHelper.pxToDpInt(context, 2) else 0
            val params = cvContent.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(margin, margin, margin, margin)
            cvContent.layoutParams = params

            val cvRadius = if (item.isSelected)
                UnitHelper.pxToDpInt(context, 6)
            else
                UnitHelper.pxToDpInt(context, 8)
            cvContent.radius = cvRadius.toFloat()

            loadImageGlide(root, item.imageNavigation, imvImage)

            root.setOnSingleClick {
                onItemClick.invoke(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavViewHolder {
        val binding = ItemBottomNavigationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BottomNavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomNavViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<NavigationModel>() {
            override fun areItemsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                // Nếu NavigationModel có id riêng thì nên so sánh id, ở đây tạm so sánh hình
                return oldItem.imageNavigation == newItem.imageNavigation
            }

            override fun areContentsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
