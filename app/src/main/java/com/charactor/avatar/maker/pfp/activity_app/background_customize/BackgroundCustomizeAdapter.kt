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
//quyen
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.extensions.gone
//quyen
import com.charactor.avatar.maker.pfp.core.helper.UnitHelper
import com.charactor.avatar.maker.pfp.core.utils.key.AssetsKey
import com.charactor.avatar.maker.pfp.data.model.custom.BackgroundModel
import com.charactor.avatar.maker.pfp.databinding.ItemBackgroundBinding


class BackgroundCustomizeAdapter(
    private val context: Context,
    var onItemClick: (String, Int) -> Unit = { _, _ -> },
    //quyen
    var onNoneClick: (Int) -> Unit = {},
    var onRandomClick: () -> Unit = {}
    //quyen
) : ListAdapter<BackgroundModel, BackgroundCustomizeAdapter.BackgroundViewHolder>(DiffCallback) {

    inner class BackgroundViewHolder(
        private val binding: ItemBackgroundBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BackgroundModel, position: Int) {
            binding.apply {

                //quyen
                when (item.image) {
                    AssetsKey.NONE_LAYER -> {
                        // Hiển thị button NONE
                        btnNone.visible()
                        btnRandom.gone()
                        imvImage.gone()
                    }
                    AssetsKey.RANDOM_LAYER -> {
                        // Hiển thị button RANDOM
                        btnNone.gone()
                        btnRandom.visible()
                        imvImage.gone()
                    }
                    else -> {
                        // Hiển thị ảnh bình thường
                        btnNone.gone()
                        btnRandom.gone()
                        imvImage.visible()
                        loadImageGlide(root, item.image, imvImage)
                    }
                }
                //quyen

                //quyen
                val (radius, res, margin) = if (item.isSelected) {
                    Triple(
                        UnitHelper.pxToDpInt(context, 8).toFloat(), // 10dp (viền ngoài) - 1dp (margin) = 9dp
                        R.drawable.img_background_focus,
                        UnitHelper.pxToDpInt(context, 2)
                    )
                } else {
                    Triple(
                        UnitHelper.pxToDpInt(context, 10).toFloat(), // Bằng với viền ngoài để khớp góc
                        android.R.color.transparent,
                        UnitHelper.pxToDpInt(context, 0)
                    )
                }

                cvImage.radius = radius
                (cvImage.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(margin, margin, margin, margin)
                    cvImage.layoutParams = this
                }
                //quyen

                //quyen
                if (item.isSelected) {
                    loadImageGlide(root, R.drawable.img_background_focus, imvFocus)
                } else {
                    imvFocus.setImageDrawable(null)  // ← Xóa viền khi không chọn
                }

                // Set click listener riêng cho từng view (giống LayerCustomizeAdapter)
                imvImage.setOnSingleClick { onItemClick.invoke(item.image, position) }
                btnRandom.setOnSingleClick { onRandomClick.invoke() }
                btnNone.setOnSingleClick { onNoneClick.invoke(position) }
                //quyen
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
