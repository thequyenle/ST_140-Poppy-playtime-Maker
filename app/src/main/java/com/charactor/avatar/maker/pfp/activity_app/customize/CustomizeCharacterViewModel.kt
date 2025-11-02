package com.charactor.avatar.maker.pfp.activity_app.customize

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.extensions.showToast
import com.charactor.avatar.maker.pfp.core.helper.BitmapHelper
import com.charactor.avatar.maker.pfp.core.helper.InternetHelper
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.utils.key.AssetsKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.HandleState
import com.charactor.avatar.maker.pfp.core.utils.state.SaveState
import com.charactor.avatar.maker.pfp.data.model.custom.CustomizeModel
import com.charactor.avatar.maker.pfp.data.model.custom.ItemColorImageModel
import com.charactor.avatar.maker.pfp.data.model.custom.ItemColorModel
import com.charactor.avatar.maker.pfp.data.model.custom.ItemNavCustomModel
import com.charactor.avatar.maker.pfp.data.model.custom.LayerListModel
import com.charactor.avatar.maker.pfp.data.model.custom.NavigationModel
import com.charactor.avatar.maker.pfp.data.model.custom.SuggestionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CustomizeCharacterViewModel : ViewModel() {
    // Đếm số lần random, chỉ số được chọn
    var countRandom = 0
    var positionSelected = 0

    // Data từ API hay không
    private val _isDataAPI = MutableStateFlow(false)

    // Trạng thái flip
    private val _isFlip = MutableStateFlow(false)
    val isFlip = _isFlip.asStateFlow()

    private val _isHideView = MutableStateFlow(false)
    val isHideView = _isHideView.asStateFlow()

    private val _isCreated = MutableStateFlow(false)
    val isCreated = _isCreated.asStateFlow()

    var statusFrom = ValueKey.CREATE

    var avatarPath = ""

    //----------------------------------------------------------------------------------------------------------------------
    var positionNavSelected = -1
    
    var positionCustom = -1

    // Data gốc
    private val _dataCustomize = MutableStateFlow<CustomizeModel?>(null)
    val dataCustomize = _dataCustomize.asStateFlow()

    // Danh sách Navigation bottom
    private val _bottomNavigationList = MutableStateFlow(arrayListOf<NavigationModel>())
    val bottomNavigationList = _bottomNavigationList.asStateFlow()
    
    val itemNavList = ArrayList<ArrayList<ItemNavCustomModel>>()

    // Danh sách màu
    var colorItemNavList = ArrayList<ArrayList<ItemColorModel>>()

    // Trạng thái chọn item/màu
    var positionColorItemList = ArrayList<Int>()

    val isSelectedItemList = ArrayList<Boolean>()

    val isShowColorList = ArrayList<Boolean>()

    // Key + Path đã chọn
    var keySelectedItemList = ArrayList<String>()

    var pathSelectedList = ArrayList<String>()

    // Danh sách ImageView trên layout
    val imageViewList = ArrayList<ImageView>()

    val colorListMost = ArrayList<String>()

    var suggestionModel = SuggestionModel()

    //----------------------------------------------------------------------------------------------------------------------
    // Base setter
    suspend fun setPositionNavSelected(position: Int) {
        positionNavSelected = position
    }

    suspend fun setPositionCustom(position: Int) {
        positionCustom = position
    }

    fun setDataCustomize(data: CustomizeModel) {
        _dataCustomize.value = data
    }

    fun setIsDataAPI(isAPI: Boolean) {
        _isDataAPI.value = isAPI
    }

    fun setIsFlip() {
        _isFlip.value = !_isFlip.value
    }

    fun setIsHideView() {
        _isHideView.value = !_isHideView.value
    }

    fun setIsCreated(status: Boolean) {
        _isCreated.value = status
    }

    fun updatePositionColorItemList(positionList: ArrayList<Int>) {
        positionColorItemList.clear()
        positionColorItemList.addAll(positionList)
    }

    fun updateIsSelectedItemList(selectedList: ArrayList<Boolean>) {
        isSelectedItemList.clear()
        isSelectedItemList.addAll(selectedList)
    }

    fun updateIsShowColorList(position: Int, status: Boolean) {
        isShowColorList[position] = status
    }

    fun updateIsShowColorList(showList: ArrayList<Boolean>) {
        isShowColorList.clear()
        isShowColorList.addAll(showList)
    }

    fun updateKeySelectedItemList(keyList: ArrayList<String>) {
        keySelectedItemList.clear()
        keySelectedItemList = keyList
    }

    fun updatePathSelectedList(pathList: ArrayList<String>) {
        pathSelectedList.clear()
        pathSelectedList.addAll(pathList)
    }

    fun setColorListMost(colorList: ArrayList<String>) {
        colorListMost.clear()
        colorListMost.addAll(colorList)
    }

    fun updateSuggestionModel(model: SuggestionModel){
        suggestionModel = model
    }

    fun updateAvatarPath(path: String){
        avatarPath = path
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Setter suspend
    suspend fun setPositionColorItem(position: Int, newPosition: Int) {
        positionColorItemList =
            positionColorItemList.mapIndexed { index, oldPosition -> if (index == position) newPosition else oldPosition }
                .toCollection(ArrayList())
    }

    suspend fun setIsSelectedItem(position: Int) {
        isSelectedItemList[position] = true
    }

    suspend fun setKeySelected(position: Int, newKey: String) {
        keySelectedItemList[position] = newKey
    }

    suspend fun setPathSelected(position: Int, newPath: String) {
        pathSelectedList[position] = newPath
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Bottom Navigation
    suspend fun setBottomNavigationList(bottomNavList: ArrayList<NavigationModel>) {
        _bottomNavigationList.value = bottomNavList
    }

    suspend fun setBottomNavigationListDefault() {
        val outputBottomNavigationList = arrayListOf<NavigationModel>()
        _dataCustomize.value!!.layerList.forEach { layerList ->
            outputBottomNavigationList.add(NavigationModel(imageNavigation = layerList.imageNavigation))
        }
        outputBottomNavigationList.first().isSelected = true
        _bottomNavigationList.value = outputBottomNavigationList
    }


    suspend fun setClickBottomNavigation(position: Int) {
        _bottomNavigationList.value = _bottomNavigationList.value
            .mapIndexed { index, model -> model.copy(isSelected = index == position) }
            .toCollection(ArrayList())
    }

    //----------------------------------------------------------------------------------------------------------------------
    //  Item Nav / Layer
    suspend fun addValueToItemNavList() {
        itemNavList.clear()
        _dataCustomize.value!!.layerList.forEachIndexed { index, layer ->
            if (index == 0) {
                itemNavList.add(createListItem(layer, true))
            } else {
                itemNavList.add(createListItem(layer))
            }
        }
    }

    suspend fun setFocusItemNavDefault() {
        for (itemParent in itemNavList) {
            itemParent.forEachIndexed { index, item ->
                item.isSelected = index == 0
            }
        }
        itemNavList.first()[0].isSelected = false
        itemNavList.first()[1].isSelected = true
    }

    fun updateItemNavList(list: ArrayList<ArrayList<ItemNavCustomModel>>){
        itemNavList.clear()
        itemNavList.addAll(list)
    }

    suspend fun setItemNavList(positionNavigation: Int, position: Int) {
        itemNavList[positionNavigation] = itemNavList[positionNavigation]
            .mapIndexed { index, models -> models.copy(isSelected = index == position) }
            .toCollection(ArrayList())
    }

    suspend fun setClickFillLayer(item: ItemNavCustomModel, position: Int): String {
        val path = item.path
        setKeySelected(positionNavSelected, path)
        val pathSelected = if (item.listImageColor.isEmpty()) {
            path
        } else {
            item.listImageColor[positionColorItemList[positionNavSelected]].path
        }
        setIsSelectedItem(positionNavSelected)
        setItemNavList(positionNavSelected, position)
        return pathSelected
    }

    suspend fun setClickRandomLayer(): Pair<String, Boolean> {
        val positionStartLayer = if (positionNavSelected == 0) 1 else 2
        val randomLayer = if (positionNavSelected == 0) {
            if (itemNavList[positionNavSelected].size == 1) {
                1
            } else {
                (positionStartLayer..<itemNavList[positionNavSelected].size).random()
            }
        } else {
            (positionStartLayer..<itemNavList[positionNavSelected].size).random()
        }

        var randomColor: Int? = null

        var isMoreColors = false

        if (itemNavList[positionNavSelected][positionStartLayer].listImageColor.isNotEmpty()) {
            isMoreColors = true
            randomColor =
                (0..<(itemNavList[positionNavSelected][positionStartLayer].listImageColor.size)).random()
        }
        var pathRandom = itemNavList[positionNavSelected][randomLayer].path
        setKeySelected(positionNavSelected, pathRandom)

        if (!isMoreColors) {
            setPositionColorItem(positionCustom, 0)
        } else {
            pathRandom = itemNavList[positionNavSelected][randomLayer].listImageColor[randomColor!!].path
            setPositionColorItem(positionCustom, randomColor)
        }
        setPathSelected(positionCustom, pathRandom)
        setItemNavList(positionNavSelected, randomLayer)
        if (isMoreColors) {
            setColorItemNav(positionNavSelected, randomColor!!)
        }
        return pathRandom to isMoreColors
    }

    suspend fun setClickRandomFullLayer(): Boolean {
//        countRandom++
//        val isOutTurn = if (countRandom == 5) true else false

        val colorCode = if (colorListMost.isNotEmpty()) colorListMost[(0..<colorListMost.size).random()] else "#123456"
        for (i in 0 until _bottomNavigationList.value.size) {
            val minSize = if (i == 0) 1 else 2
            if (itemNavList[i].size <= minSize) {
                continue
            }
            val randomLayer = (minSize..<itemNavList[i].size).random()

            var randomColor: Int = 0

            val isMoreColors = if (itemNavList[i][minSize].listImageColor.isNotEmpty()) {
                randomColor =
                    itemNavList[i][randomLayer].listImageColor.indexOfFirst { it.color == colorCode }
                if (randomColor == -1) {
                    randomColor = (0..<itemNavList[i][minSize].listImageColor.size).random()
                }
                true
            } else {
                false
            }
            keySelectedItemList[i] = itemNavList[i][randomLayer].path

            val pathItem = if (!isMoreColors) {
                positionColorItemList[i] = 0
                itemNavList[i][randomLayer].path
            } else {
                positionColorItemList[i] = randomColor
                itemNavList[i][randomLayer].listImageColor[randomColor].path
            }
            pathSelectedList[_dataCustomize.value!!.layerList[i].positionCustom] = pathItem
            setItemNavList(i, randomLayer)
            if (isMoreColors) {
                setColorItemNav(i, randomColor)
            }
        }
        return false
    }

    suspend fun setClickReset(): String {
        resetDataList()
        _bottomNavigationList.value.forEachIndexed { index, model ->
            val positionSelected = if (index == 0) 1 else 0
            setItemNavList(index, positionSelected)
            setColorItemNav(index, 0)
        }
        val pathDefault = _dataCustomize.value!!.layerList.first().layer.first().image
        pathSelectedList[_dataCustomize.value!!.layerList.first().positionCustom] = pathDefault
        keySelectedItemList[_dataCustomize.value!!.layerList.first().positionNavigation] = pathDefault
        isSelectedItemList[_dataCustomize.value!!.layerList.first().positionNavigation] = true
        return pathDefault
    }


    //----------------------------------------------------------------------------------------------------------------------
// Color
    suspend fun setItemColorDefault() {
        for (i in 0 until _dataCustomize.value!!.layerList.size) {
            // Lấy đối tượng LayerModel đầu tiên trong danh sách con
            val currentLayer = _dataCustomize.value!!.layerList[i].layer.first()
            var firstIndex = true
            // Kiểm tra isMoreColors để thêm màu hoặc danh sách rỗng
            if (currentLayer.isMoreColors) {
                val colorList = ArrayList<ItemColorModel>()
                for (j in 0 until currentLayer.listColor.size) {
                    val color = currentLayer.listColor[j].color
                    if (firstIndex) {
                        colorList.add(ItemColorModel(color, true))
                    } else {
                        colorList.add(ItemColorModel(color))
                    }
                    firstIndex = false
                }
                colorItemNavList.add(colorList)
            } else {
                colorItemNavList.add(arrayListOf())
            }
        }
        val getAllColor = ArrayList<String>()
        itemNavList.forEachIndexed { index, nav ->
            val position = if (index != 0) 2 else 1
            val itemNav = nav[position]
            itemNav.listImageColor.forEach { colorList ->
                getAllColor.add(colorList.color)
            }
        }
        setColorListMost(
            getAllColor
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 3 }.keys
                .toCollection(ArrayList())
        )
    }

    fun updateColorNavList(list: ArrayList<ArrayList<ItemColorModel>>){
        colorItemNavList.clear()
        colorItemNavList.addAll(list)
    }

    suspend fun setColorItemNav(positionNavSelected: Int, position: Int) {
        colorItemNavList[positionNavSelected] = colorItemNavList[positionNavSelected]
            .mapIndexed { index, models -> models.copy(isSelected = index == position) }
            .toCollection(ArrayList())
    }

    suspend fun setClickChangeColor(position: Int): String {
        var pathColor = ""
        positionColorItemList[positionNavSelected] = position
        // Đã chọn hình ảnh chưa
        if (keySelectedItemList[positionNavSelected] != "") {
            // Duyệt qua từng item trong bộ phận
            for (item in _dataCustomize.value!!.layerList[positionNavSelected].layer) {
                if (item.image == keySelectedItemList[positionNavSelected]) {
                    pathColor = item.listColor[position].path
                    pathSelectedList[positionCustom] = pathColor
                }
            }
        }
        setColorItemNav(positionNavSelected, position)
        return pathColor
    }

//----------------------------------------------------------------------------------------------------------------------
// Extension other

    suspend fun setImageViewList(frameLayout: FrameLayout) {
        imageViewList.clear()
        imageViewList.addAll(addImageViewToLayout(_dataCustomize.value!!.layerList.size, frameLayout))
    }

    fun addImageViewToLayout(quantityLayer: Int, frameLayout: FrameLayout): ArrayList<ImageView> {
        val imageViewList = ArrayList<ImageView>()
        for (i in 0 until quantityLayer) {
            val imageView = ImageView(frameLayout.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            frameLayout.addView(imageView)
            imageViewList.add(imageView)
        }
        return imageViewList
    }

    fun createListItem(layers: LayerListModel, isBody: Boolean = false): ArrayList<ItemNavCustomModel> {
        val listItem = arrayListOf<ItemNavCustomModel>()
        val positionCustom = layers.positionCustom
        val positionNavigation = layers.positionNavigation
        if (isBody) {
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.RANDOM_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation
                )
            )
        } else {
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.NONE_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                    isSelected = true
                )
            )
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.RANDOM_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                )
            )
        }
        for (layer in layers.layer) {
            if (!layer.isMoreColors) {
                listItem.add(
                    ItemNavCustomModel(
                        path = layer.image,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation
                    )
                )
            } else {
                val listItemColor = ArrayList<ItemColorImageModel>()

                for (colorModel in layer.listColor) {
                    listItemColor.add(
                        ItemColorImageModel(
                            color = colorModel.color,
                            path = colorModel.path
                        )
                    )
                }
                listItem.add(
                    ItemNavCustomModel(
                        path = layer.image,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation,
                        isSelected = false,
                        listImageColor = listItemColor,
                    )
                )
            }
        }
        return listItem
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorage(context, ValueKey.DOWNLOAD_ALBUM_BACKGROUND, bitmap).collect { state ->
            emit(state)
        }
    }.flowOn(Dispatchers.IO)

    fun checkDataInternet(context: Activity, action: (() -> Unit)) {
        if (!_isDataAPI.value) {
            action.invoke()
            return
        }
        InternetHelper.checkInternet(context) { result ->
            if (result == HandleState.SUCCESS) {
                action.invoke()
            } else {
                context.showToast(R.string.please_check_your_internet)
            }
        }
    }

    suspend fun resetDataList() {
        val quantityLayer = _dataCustomize.value!!.layerList.size
        val positionColorItemList = ArrayList<Int>(quantityLayer)
        val isSelectedItemList = ArrayList<Boolean>(quantityLayer)
        val keySelectedItemList = ArrayList<String>(quantityLayer)
        val isShowColorList = ArrayList<Boolean>(quantityLayer)
        val pathSelectedList = ArrayList<String>(quantityLayer)

        repeat(quantityLayer) {
            positionColorItemList.add(0)
            isSelectedItemList.add(false)
            keySelectedItemList.add("")
            isShowColorList.add(true)
            pathSelectedList.add("")
        }

        updatePositionColorItemList(positionColorItemList)
        updateIsSelectedItemList(isSelectedItemList)
        updateKeySelectedItemList(keySelectedItemList)
        updateIsShowColorList(isShowColorList)
        updatePathSelectedList(pathSelectedList)
    }

    fun getSuggestionList(): SuggestionModel {
        return SuggestionModel(
            avatarPath = avatarPath,
            positionColorItemList = ArrayList(positionColorItemList),
            itemNavList = ArrayList(itemNavList),
            colorItemNavList = ArrayList(colorItemNavList),
            isSelectedItemList = ArrayList(isSelectedItemList),
            keySelectedItemList = ArrayList(keySelectedItemList),
            isShowColorList = ArrayList(isShowColorList),
            pathSelectedList = ArrayList(pathSelectedList)
        )
    }

    fun fillSuggestionToCustomize(){
        updatePositionColorItemList(suggestionModel.positionColorItemList)
        updateItemNavList(suggestionModel.itemNavList)
        updateColorNavList(suggestionModel.colorItemNavList)
        updateIsSelectedItemList(suggestionModel.isSelectedItemList)
        updateKeySelectedItemList(suggestionModel.keySelectedItemList)
        updateIsShowColorList(suggestionModel.isShowColorList)
        updatePathSelectedList(suggestionModel.pathSelectedList)
    }
    //----------------------------------------------------------------------------------------------------------------------

}