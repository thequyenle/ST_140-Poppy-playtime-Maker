package com.charactor.avatar.maker.pfp.choose_character

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChooseCharacterViewModel : ViewModel() {
    private val _isRandom = MutableStateFlow<Boolean>(false)
    var isRandom = _isRandom.asStateFlow()


    //-----------------------------------------------------------------------------------------------------------------
    fun setIsRandom(status: Boolean) {
        _isRandom.value = status
    }
}