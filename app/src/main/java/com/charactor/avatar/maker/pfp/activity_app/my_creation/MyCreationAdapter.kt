package com.charactor.avatar.maker.pfp.activity_app.my_creation

import android.content.Context
import android.view.View
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseAdapter
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.data.model.MyAlbumModel
import com.charactor.avatar.maker.pfp.databinding.ItemMyAlbumBinding

class MyCreationAdapter(val context: Context) : BaseAdapter<MyAlbumModel, ItemMyAlbumBinding>(
    ItemMyAlbumBinding::inflate
) {
    var onItemClick: ((String) -> Unit) = {}
    var onMoreClick: ((String, Int, View) -> Unit) = { _, _, _ -> }
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    override fun onBind(
        binding: ItemMyAlbumBinding,
        item: MyAlbumModel,
        position: Int
    ) {
        binding.apply {
            loadImageGlide(root, item.path, imvImage)

            binding.btnMore.gone()
            if (item.isShowSelection) {
                binding.btnSelect.visible()
                binding.btnMore.gone()
            } else {
                binding.btnSelect.gone()
                binding.btnMore.gone()
            }

            if (item.isSelected) {
                binding.btnSelect.setImageResource(R.drawable.ic_selected)
            } else {
                binding.btnSelect.setImageResource(R.drawable.ic_not_select)
            }

            binding.root.setOnSingleClick { onItemClick.invoke(item.path) }
            binding.btnMore.setOnSingleClick {
                onMoreClick.invoke(
                    item.path,
                    position,
                    it
                )
            }
            binding.root.setOnLongClickListener {
                if (items.any { album -> album.isShowSelection }) {
                    return@setOnLongClickListener false
                } else {
                    onLongClick.invoke(position)
                    return@setOnLongClickListener true

                }
            }
            binding.btnSelect.setOnSingleClick { onItemTick.invoke(position) }
        }
    }
}