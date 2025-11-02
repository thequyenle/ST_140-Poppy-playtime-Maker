package com.charactor.avatar.maker.pfp.activity_app.my_creation

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charactor.avatar.maker.pfp.core.extensions.shareImagesPaths
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper.getImageInternal
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.HandleState
import com.charactor.avatar.maker.pfp.data.model.MyAlbumModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyCreationViewModel : ViewModel() {

    private val _albumList = MutableStateFlow<List<MyAlbumModel>>(emptyList())
    val albumList: StateFlow<List<MyAlbumModel>> = _albumList.asStateFlow()

    private val _downloadState = MutableSharedFlow<HandleState>()
    val downloadState: SharedFlow<HandleState> = _downloadState

    private val _shareState = MutableSharedFlow<HandleState>()
    val sharedState: SharedFlow<HandleState> = _shareState

    private val _deleteState = MutableSharedFlow<HandleState>()
    val deleteState: SharedFlow<HandleState> = _deleteState

    private val _isLastItem = MutableStateFlow<Boolean>(false)
    val isLastItem: StateFlow<Boolean> = _isLastItem

    private val _isShowLongCLick = MutableStateFlow<Boolean>(false)
    val isShowLongCLick: StateFlow<Boolean> = _isShowLongCLick.asStateFlow()

    private val _isFromSuccess = MutableStateFlow<Boolean>(false)
    val isFromSuccess: StateFlow<Boolean> = _isFromSuccess

    fun setStatusFrom(status: Boolean){
        _isFromSuccess.value = status
    }
    fun loadAlbum(context: Context) {
        val list = getImageInternal(context, ValueKey.DOWNLOAD_ALBUM).map { MyAlbumModel(it) }
        _albumList.value = list
        checkLastItem()
    }

    fun toggleSelect(position: Int) {
        val list = _albumList.value.toMutableList()
        list[position] = list[position].copy(isSelected = !list[position].isSelected, isShowSelection = true)
        _albumList.value = list
        checkLastItem()
    }

    private fun checkLastItem(){
        _isLastItem.value = _albumList.value.any { !it.isSelected }
    }

    fun showLongClick(positionSelect: Int) {
        setShowLongClick(true)
        _albumList.value = _albumList.value.mapIndexed { position, item ->
            item.copy(isSelected = position == positionSelect, isShowSelection = true)
        }
        checkLastItem()
    }

    fun selectAll(shouldSelect: Boolean) {
        _albumList.value = _albumList.value.map {
            it.copy(isSelected = shouldSelect, isShowSelection = true)
        }
        checkLastItem()
    }

    fun setShowLongClick(status: Boolean){
        _isShowLongCLick.value = status
    }
    fun deleteFiles(onlyPath: String = "") {
        val deleteList = if (onlyPath == "") {
            _albumList.value.filter { it.isSelected }.map { it.path }
        } else {
            arrayListOf(onlyPath)
        }

        viewModelScope.launch {
            if (deleteList.isEmpty()) {
                _deleteState.emit(HandleState.NOT_SELECT)
                return@launch
            }

            withContext(Dispatchers.IO){
                MediaHelper.deleteFileByPath(ArrayList(deleteList)).collect { state ->
                    when (state) {
                        HandleState.LOADING -> _deleteState.emit(HandleState.LOADING)
                        HandleState.SUCCESS -> {
                            _albumList.value = _albumList.value.filterNot { deleteList.contains(it.path) }
                            _deleteState.emit(HandleState.SUCCESS)
                        }

                        else -> _deleteState.emit(HandleState.FAIL)
                    }
                }
            }
        }
    }

    fun downloadFiles(context: Activity, onlyPath: String = "") {
        val downloadList = if (onlyPath == "") {
            _albumList.value.filter { it.isSelected }.map { it.path }
        } else {
            arrayListOf(onlyPath)
        }

        viewModelScope.launch {
            if (downloadList.isEmpty()) {
                _downloadState.emit(HandleState.NOT_SELECT)
                return@launch
            }

            MediaHelper.downloadPartsToExternal(context, downloadList)
                .flowOn(Dispatchers.IO)
                .collect { state ->
                    _downloadState.emit(state)
                }
        }
    }

    fun shareFiles(context: Activity, onlyPath: String = "") {
        val shareList = if (onlyPath == "") {
            _albumList.value.filter { it.isSelected }.map { it.path }
        } else {
            arrayListOf(onlyPath)
        }
        viewModelScope.launch {
            if (shareList.isEmpty()) {
                _shareState.emit(HandleState.NOT_SELECT)
                return@launch
            }
            context.shareImagesPaths(ArrayList(shareList))
        }
    }
}