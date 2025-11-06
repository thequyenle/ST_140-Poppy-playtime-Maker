package com.charactor.avatar.maker.pfp.activity_app.background_customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.helper.UnitHelper
import com.charactor.avatar.maker.pfp.data.model.custom.BackgroundModel
import com.charactor.avatar.maker.pfp.databinding.ItemBackgroundBinding


class BackgroundCustomizeAdapter(
    private val context: Context,
    var onItemClick: (String, Int) -> Unit = { _, _ -> }
) : ListAdapter<BackgroundModel, BackgroundCustomizeAdapter.BackgroundViewHolder>(DiffCallback) {

    inner class BackgroundViewHolder(
        private val binding: ItemBackgroundBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BackgroundModel, position: Int) {
            binding.apply {
                loadImageGlide(root, item.image, imvImage)

                val (radius, res, margin) = if (item.isSelected) {
                    Triple(
                        UnitHelper.pxToDpInt(context, 6).toFloat(),
                        R.drawable.img_background_focus,
                        UnitHelper.pxToDpInt(context, 4)
                    )
                } else {
                    Triple(
                        UnitHelper.pxToDpInt(context, 8).toFloat(),
                        android.R.color.transparent,
                        UnitHelper.pxToDpInt(context, 2)
                    )
                }

                cvImage.radius = radius
                (cvImage.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(margin, margin, margin, margin)
                    cvImage.layoutParams = this
                }

                //quyen
                if (item.isSelected) {
                    loadImageGlide(root, R.drawable.img_background_focus, imvFocus)
                } else {
                    imvFocus.setImageDrawable(null)  // ← Xóa viền khi không chọn
                }
                //quyen
                root.setOnSingleClick {
                    onItemClick.invoke(item.image, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        val binding = ItemBackgroundBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BackgroundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackgroundViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<BackgroundModel>() {
            override fun areItemsTheSame(oldItem: BackgroundModel, newItem: BackgroundModel): Boolean {
                // Nếu mỗi item có ID riêng thì dùng ID, ở đây tạm dùng image
                return oldItem.image == newItem.image
            }

            override fun areContentsTheSame(oldItem: BackgroundModel, newItem: BackgroundModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
