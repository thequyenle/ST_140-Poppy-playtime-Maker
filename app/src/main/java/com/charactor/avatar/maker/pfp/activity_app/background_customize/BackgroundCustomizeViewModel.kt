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
        //quyen
        val list = arrayListOf<BackgroundModel>()

        // 1. Thêm NONE vào đầu tiên
        list.add(
            BackgroundModel(
                image = AssetsKey.NONE_LAYER,
                isSelected = false
            )
        )

        // 2. Thêm RANDOM vào thứ hai
        list.add(
            BackgroundModel(
                image = AssetsKey.RANDOM_LAYER,
                isSelected = false
            )
        )

        // 3. Thêm ảnh từ assets
        list.addAll(
            AssetHelper.getSubfoldersAsset(
                context, AssetsKey.BACKGROUND_ASSET
            ).map { string ->
                BackgroundModel(string, false)
            }
        )

        // 4. Chọn ảnh đầu tiên từ assets (index 2, vì 0 là NONE, 1 là RANDOM)
        if (list.size > 2) {
            list[2] = list[2].copy(isSelected = true)
        }

        _backgroundList.value = list
        //quyen
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

    //quyen
    fun randomBackground(): String {
        // Bỏ qua position 0 (NONE) và position 1 (RANDOM), chỉ random từ vị trí 2 trở đi
        val startPosition = 2
        val endPosition = _backgroundList.value.size

        if (endPosition <= startPosition) {
            return ""  // Không có ảnh để random
        }

        // Random index
        val randomIndex = (startPosition until endPosition).random()

        // Lấy path random
        val pathRandom = _backgroundList.value[randomIndex].image

        // Cập nhật focus
        changeFocusBackgroundList(randomIndex)

        return pathRandom
    }
    //quyen
}