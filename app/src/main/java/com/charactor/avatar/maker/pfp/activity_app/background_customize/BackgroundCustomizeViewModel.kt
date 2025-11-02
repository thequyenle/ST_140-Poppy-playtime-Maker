package com.charactor.avatar.maker.pfp.activity_app.background_customize

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.charactor.avatar.maker.pfp.core.helper.AssetHelper
import com.charactor.avatar.maker.pfp.core.helper.BitmapHelper
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.utils.key.AssetsKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.SaveState
import com.charactor.avatar.maker.pfp.data.model.custom.BackgroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BackgroundCustomizeViewModel : ViewModel() {
    private val _backgroundList = MutableStateFlow<ArrayList<BackgroundModel>>(arrayListOf())
    val backgroundList: StateFlow<ArrayList<BackgroundModel>> = _backgroundList.asStateFlow()

    private val _pathInternalTemp = MutableStateFlow<String>("")
    val pathInternalTemp: StateFlow<String> = _pathInternalTemp.asStateFlow()


    fun loadBackground(context: Context) {
        _backgroundList.value.addAll(
            AssetHelper.getSubfoldersAsset(
                context, AssetsKey.BACKGROUND_ASSET
            ).mapIndexed { index, string ->
                if (index == 0) {
                    BackgroundModel(string, true)
                } else {
                    BackgroundModel(string)
                }
            })
    }

    fun changeFocusBackgroundList(position: Int) {
        _backgroundList.value = _backgroundList.value.mapIndexed { index, model ->
            model.copy(isSelected = index == position)
        }.toCollection(ArrayList())
    }

    fun setPathInternalTemp(path: String) {
        _pathInternalTemp.value = path
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorage(context, ValueKey.DOWNLOAD_ALBUM, bitmap)
            .collect { state ->
                emit(state)
            }
    }.flowOn(Dispatchers.IO)
}