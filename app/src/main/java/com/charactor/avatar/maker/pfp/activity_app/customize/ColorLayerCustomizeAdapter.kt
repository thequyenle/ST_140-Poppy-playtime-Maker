package com.charactor.avatar.maker.pfp.activity_app.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.charactor.avatar.maker.pfp.core.base.BaseAdapter
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.data.model.custom.ItemColorModel
import com.charactor.avatar.maker.pfp.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            vFocus.isVisible = item.isSelected
            root.setOnSingleClick { onItemClick.invoke(position) }
        }
    }
}