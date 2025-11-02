package com.charactor.avatar.maker.pfp.activity_app.random_character

import androidx.lifecycle.ViewModel
import com.charactor.avatar.maker.pfp.data.model.custom.SuggestionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RandomCharacterViewModel : ViewModel() {

    val randomList = ArrayList<SuggestionModel>()
    //-----------------------------------------------------------------------------------------------------------------

    suspend fun updateRandomList(suggestionModel: SuggestionModel){
        randomList.add(suggestionModel)
    }


}