package com.charactor.avatar.maker.pfp.activity_app.random_character

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.charactor.avatar.maker.pfp.core.base.BaseAdapter
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.data.model.custom.SuggestionModel
import com.charactor.avatar.maker.pfp.databinding.ItemRandomCharacterBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.invisible
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.visible

class RandomCharacterAdapter(val context: Context) :
    BaseAdapter<SuggestionModel, ItemRandomCharacterBinding>(ItemRandomCharacterBinding::inflate) {
    var onItemClick: ((SuggestionModel) -> Unit) = {}
    override fun onBind(binding: ItemRandomCharacterBinding, item: SuggestionModel, position: Int) {
        binding.apply {
            sflShimmer.visible()
            sflShimmer.startShimmer()
            imvImage.invisible()

            val width_height = ValueKey.WIDTH_HEIGHT_BITMAP
            val listBitmap: ArrayList<Bitmap> = arrayListOf()
            val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                Log.e("nbhieu", "random_character: ${throwable.message}")
            }
            CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
                val job1 = async {
                    item.pathSelectedList.forEach { path ->
                        listBitmap.add(
                            Glide.with(context).asBitmap().load(path).submit(width_height, width_height).get()
                        )
                    }
                    return@async (item.pathSelectedList.size == listBitmap.size)
                }

                withContext(Dispatchers.Main) {
                    if (job1.await()) {
                        val combinedBitmap = createBitmap(width_height, width_height)
                        val canvas = Canvas(combinedBitmap)

                        for (i in 0 until listBitmap.size) {
                            val bitmap = listBitmap[i]
                            val left = (width_height - bitmap.width) / 2f
                            val top = (width_height - bitmap.height) / 2f
                            canvas.drawBitmap(bitmap, left, top, null)
                        }
                        Glide.with(root).load(combinedBitmap).listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?, model: Any?, target: Target<Drawable?>, isFirstResource: Boolean
                            ): Boolean {
                                sflShimmer.stopShimmer()
                                sflShimmer.gone()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable?>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                sflShimmer.stopShimmer()
                                sflShimmer.gone()
                                imvImage.visible()
                                return false
                            }
                        }).into(imvImage)
                    }
                }
            }

            root.setOnSingleClick { onItemClick.invoke(item) }
        }
    }
}