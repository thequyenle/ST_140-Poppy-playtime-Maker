package com.charactor.avatar.maker.pfp.activity_app.view

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charactor.avatar.maker.pfp.core.extensions.shareImagesPaths
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.utils.state.HandleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ViewViewModel : ViewModel() {
    private val _pathInternal = MutableStateFlow<String>("")
    val pathInternal: StateFlow<String> = _pathInternal.asStateFlow()

    private val _typeUI = MutableStateFlow<Int>(-1)
    val typeUI: StateFlow<Int> = _typeUI.asStateFlow()

    fun setPath(path: String) {
        _pathInternal.value = path
    }

    fun setType(type: Int) {
        _typeUI.value = type
    }

    fun deleteFile(path: String): Flow<HandleState> = flow {
            emitAll(MediaHelper.deleteFileByPath(arrayListOf(path)))
    }

    fun shareFiles(context: Activity) {
        viewModelScope.launch {
            context.shareImagesPaths(arrayListOf(_pathInternal.value))
        }
    }

    fun downloadFiles(context: Activity): Flow<HandleState> = flow {
        emitAll(
            MediaHelper.downloadPartsToExternal(
                context, arrayListOf(_pathInternal.value)
            )
        )
    }
}