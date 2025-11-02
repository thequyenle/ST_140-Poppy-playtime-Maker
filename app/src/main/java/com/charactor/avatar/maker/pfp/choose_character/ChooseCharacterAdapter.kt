package com.charactor.avatar.maker.pfp.choose_character

import com.charactor.avatar.maker.pfp.core.base.BaseAdapter
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.data.model.custom.CustomizeModel
import com.charactor.avatar.maker.pfp.databinding.ItemMyAlbumBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemMyAlbumBinding>(ItemMyAlbumBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}
    override fun onBind(binding: ItemMyAlbumBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            btnMore.gone()
            loadImageGlide(root, item.avatar, imvImage)
            root.setOnSingleClick { onItemClick.invoke(position) }
        }
    }
}