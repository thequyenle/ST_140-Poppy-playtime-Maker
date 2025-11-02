package com.charactor.avatar.maker.pfp.activity_app.customize

import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.dLog
import com.charactor.avatar.maker.pfp.core.extensions.eLog
import com.charactor.avatar.maker.pfp.core.extensions.hideNavigation
import com.charactor.avatar.maker.pfp.core.extensions.invisible
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.showToast
import com.charactor.avatar.maker.pfp.core.extensions.startIntentRightToLeft
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.helper.LanguageHelper
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.SaveState
import com.charactor.avatar.maker.pfp.data.model.custom.ItemNavCustomModel
import com.charactor.avatar.maker.pfp.databinding.ActivityCustomizeBinding
import com.charactor.avatar.maker.pfp.dialog.YesNoDialog
import com.charactor.avatar.maker.pfp.activity_app.background_customize.BackgroundCustomizeActivity
import com.charactor.avatar.maker.pfp.activity_app.main.DataViewModel
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.data.model.custom.SuggestionModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomizeCharacterActivity : BaseActivity<ActivityCustomizeBinding>() {
    private val viewModel: CustomizeCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    val colorLayerCustomizeAdapter by lazy { ColorLayerCustomizeAdapter(this) }
    val layerCustomizeAdapter by lazy { LayerCustomizeAdapter(this) }
    val bottomNavigationCustomizeAdapter by lazy { BottomNavigationCustomizeAdapter(this) }
    val hideList: ArrayList<View> by lazy {
        arrayListOf(
            binding.btnRandom,
            binding.btnColor,
            binding.flColor,
            binding.rcvLayer,
            binding.flBottomNav
        )
    }

    override fun setViewBinding(): ActivityCustomizeBinding {
        return ActivityCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        // allData
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    viewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                    viewModel.statusFrom = intent.getIntExtra(IntentKey.STATUS_FROM_KEY, ValueKey.CREATE)
                    viewModel.setDataCustomize(list[viewModel.positionSelected])
                    viewModel.setIsDataAPI(viewModel.positionSelected >= ValueKey.POSITION_API)
                    initData()
                }
            }
        }

        // isFlip
        lifecycleScope.launch {
            viewModel.isFlip.collect { status ->
                val rotation = if (status) -180f else 0f
                viewModel.imageViewList.forEachIndexed { index, view ->
                    view.rotationY = rotation
                }
            }
        }

//        isHideView
        lifecycleScope.launch {
            viewModel.isHideView.collect { status ->
                if (viewModel.isCreated.value) {
                    if (!status) {
                        hideList.forEach { it.visible() }
                        checkStatusColor()
                    } else {
                        hideList.forEach { it.invisible() }
                    }
                }
            }
        }

        // bottomNavigationList
        lifecycleScope.launch {
            viewModel.bottomNavigationList.collect { bottomNavigationList ->
                if (bottomNavigationList.isNotEmpty()) {
                    bottomNavigationCustomizeAdapter.submitList(bottomNavigationList)
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    if (viewModel.colorItemNavList[viewModel.positionNavSelected].isNotEmpty()) {
                        binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList[viewModel.positionNavSelected].indexOfFirst { it.isSelected })
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.setOnSingleClick { confirmExit() }
                btnActionBarRightText.setOnSingleClick { handleSave() }
            }
            btnRandom.setOnSingleClick { viewModel.checkDataInternet(this@CustomizeCharacterActivity) { handleRandomAllLayer() } }
            btnReset.setOnSingleClick { handleReset() }
            btnFlip.setOnSingleClick { viewModel.setIsFlip() }
            btnColor.setOnSingleClick { handleStatusColor() }
            btnCloseColor.setOnSingleClick { handleStatusColor(true) }
            btnHide.setOnSingleClick { viewModel.setIsHideView() }
        }
        handleRcv()
    }

    override fun initText() {

    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()

            btnActionBarRightText.visible()
            tvRightText.select()

        }
    }

    private fun initRcv() {
        binding.apply {
            rcvLayer.apply {
                adapter = layerCustomizeAdapter
                itemAnimator = null
            }

            rcvColor.apply {
                adapter = colorLayerCustomizeAdapter
                itemAnimator = null
            }

            rcvNavigation.apply {
                adapter = bottomNavigationCustomizeAdapter
                itemAnimator = null
            }
        }
    }

    private fun handleRcv() {
        layerCustomizeAdapter.onItemClick =
            { item, position -> viewModel.checkDataInternet(this) { handleFillLayer(item, position) } }

        layerCustomizeAdapter.onNoneClick =
            { position -> viewModel.checkDataInternet(this) { handleNoneLayer(position) } }

        layerCustomizeAdapter.onRandomClick = { viewModel.checkDataInternet(this) { handleRandomLayer() } }

        colorLayerCustomizeAdapter.onItemClick =
            { position -> viewModel.checkDataInternet(this) { handleChangeColorLayer(position) } }

        bottomNavigationCustomizeAdapter.onItemClick =
            { positionBottomNavigation -> handleClickBottomNavigation(positionBottomNavigation) }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading(true)
                val dialogExit =
                    YesNoDialog(this@CustomizeCharacterActivity, R.string.error, R.string.an_error_occurred)
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation()
                    startIntentRightToLeft(CustomizeCharacterActivity::class.java, viewModel.positionSelected)
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            var pathImageDefault = ""
            // Get data from list
            val deferred1 = async {
                when (viewModel.statusFrom) {
                    ValueKey.CREATE -> {
                        viewModel.resetDataList()
                        viewModel.addValueToItemNavList()
                        viewModel.setItemColorDefault()
                        viewModel.setFocusItemNavDefault()
                    }

                    // Edit
                    else -> {
                        viewModel.updateSuggestionModel(
                            MediaHelper.readListFromFile<SuggestionModel>(
                                this@CustomizeCharacterActivity,
                                ValueKey.SUGGESTION_FILE_INTERNAL
                            ).first()
                        )
                        viewModel.fillSuggestionToCustomize()
                    }
                }

                viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList.first().positionCustom)
                viewModel.setPositionNavSelected(viewModel.dataCustomize.value!!.layerList.first().positionNavigation)
                viewModel.setBottomNavigationListDefault()
                dLog("deferred1")
                return@async true
            }
            // Add custom view in FrameLayout
            val deferred2 = async(Dispatchers.Main) {
                if (deferred1.await()) {
                    viewModel.setImageViewList(binding.layoutCustomLayer)
                    dLog("deferred2")
                }
                return@async true
            }

            // Fill data default
            val deferred3 = async {
                if (deferred1.await() && deferred2.await()) {
                    if (viewModel.statusFrom == ValueKey.CREATE){
                        pathImageDefault = viewModel.dataCustomize.value!!.layerList.first().layer.first().image
                        viewModel.setIsSelectedItem(viewModel.positionCustom)
                        viewModel.setPathSelected(viewModel.positionCustom, pathImageDefault)
                        viewModel.setKeySelected(viewModel.positionNavSelected, pathImageDefault)
                    }
                    dLog("deferred3")
                }
                return@async true
            }

            withContext(Dispatchers.Main) {
                if (deferred1.await() && deferred2.await() && deferred3.await()) {
                    when (viewModel.statusFrom) {
                        ValueKey.CREATE -> {
                            Glide.with(this@CustomizeCharacterActivity).load(pathImageDefault)
                                .into(viewModel.imageViewList[viewModel.positionCustom])
                        }

                        // Edit
                        else -> {
                            viewModel.pathSelectedList.forEachIndexed { index, path ->
                                if (path != ""){
                                    Glide.with(this@CustomizeCharacterActivity).load(path)
                                        .into(viewModel.imageViewList[index])
                                }
                            }
                        }
                    }

                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    checkStatusColor()
                    viewModel.setIsCreated(true)
                    dismissLoading()
                    dLog("main")
                }
            }
        }
    }

    private fun checkStatusColor() {
        if (viewModel.colorItemNavList[viewModel.positionNavSelected].isNotEmpty()) {
            binding.btnColor.visible()
            if (viewModel.isShowColorList[viewModel.positionNavSelected]) {
                binding.flColor.visible()
            } else {
                binding.flColor.invisible()
            }
        } else {
            binding.btnColor.invisible()
            binding.flColor.invisible()
        }
    }

    private fun handleStatusColor(isClose: Boolean = false) {
        if (isClose) {
            binding.flColor.invisible()
            viewModel.updateIsShowColorList(viewModel.positionNavSelected, false)
        } else {
            if (viewModel.isShowColorList[viewModel.positionNavSelected]) {
                binding.flColor.invisible()
            } else {
                binding.flColor.visible()
            }
            viewModel.updateIsShowColorList(
                viewModel.positionNavSelected,
                !viewModel.isShowColorList[viewModel.positionNavSelected]
            )
        }
    }

    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathSelected = viewModel.setClickFillLayer(item, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeCharacterActivity).load(pathSelected)
                    .into(viewModel.imageViewList[viewModel.positionCustom])
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
            }
        }
    }

    private fun handleNoneLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setIsSelectedItem(viewModel.positionCustom)
            viewModel.setPathSelected(viewModel.positionCustom, "")
            viewModel.setKeySelected(viewModel.positionNavSelected, "")
            viewModel.setItemNavList(viewModel.positionNavSelected, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeCharacterActivity).clear(viewModel.imageViewList[viewModel.positionCustom])
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
            }
        }
    }

    private fun handleRandomLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (pathRandom, isMoreColors) = viewModel.setClickRandomLayer()
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeCharacterActivity).load(pathRandom)
                    .into(viewModel.imageViewList[viewModel.positionCustom])
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                if (isMoreColors) {
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList[viewModel.positionNavSelected].indexOfFirst { it.isSelected })
                }
            }
        }
    }

    private fun handleChangeColorLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathColor = viewModel.setClickChangeColor(position)
            withContext(Dispatchers.Main) {
                if (pathColor != "") {
                    Glide.with(this@CustomizeCharacterActivity).load(pathColor)
                        .into(viewModel.imageViewList[viewModel.positionCustom])
                }
                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
            }
        }
    }

    private fun handleClickBottomNavigation(positionBottomNavigation: Int) {
        if (positionBottomNavigation == viewModel.positionNavSelected) return
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setPositionNavSelected(positionBottomNavigation)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[positionBottomNavigation].positionCustom)
            viewModel.setClickBottomNavigation(positionBottomNavigation)
            withContext(Dispatchers.Main) { checkStatusColor() }
        }
    }

    private fun confirmExit() {
        val dialog = YesNoDialog(this, R.string.exit, R.string.haven_t_saved_it_yet_do_you_want_to_exit)
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            dialog.dismiss()
            finish()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }

    private fun handleSave() {
        lifecycleScope.launch {
            viewModel.saveImageFromView(this@CustomizeCharacterActivity, binding.layoutCustomLayer).collect { result ->
                when (result) {
                    is SaveState.Loading -> showLoading()
                    is SaveState.Error -> {
                        dismissLoading(true)
                        showToast(R.string.save_failed_please_try_again)
                    }

                    is SaveState.Success -> {
                        dismissLoading(true)
                        startIntentRightToLeft(BackgroundCustomizeActivity::class.java, result.path)
                    }
                }
            }
        }
    }

    private fun handleReset() {
        val dialog = YesNoDialog(
            this@CustomizeCharacterActivity,
            R.string.reset,
            R.string.change_your_whole_design_are_you_sure
        )
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            viewModel.checkDataInternet(this) {
                dialog.dismiss()
                lifecycleScope.launch(Dispatchers.IO) {
                    val pathDefault = viewModel.setClickReset()
                    withContext(Dispatchers.Main) {
                        viewModel.imageViewList.forEach { imageView ->
                            Glide.with(this@CustomizeCharacterActivity).clear(imageView)
                        }
                        Glide.with(this@CustomizeCharacterActivity).load(pathDefault)
                            .into(viewModel.imageViewList[viewModel.dataCustomize.value!!.layerList.first().positionCustom])
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                        hideNavigation()
                    }
                }
            }
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }

    private fun handleRandomAllLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val timeStart = System.currentTimeMillis()
            val isOutTurn = viewModel.setClickRandomFullLayer()

            withContext(Dispatchers.Main) {
                viewModel.pathSelectedList.forEachIndexed { index, path ->
                    Glide.with(this@CustomizeCharacterActivity)
                        .load(path)
                        .into(viewModel.imageViewList[index])
                }
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                if (isOutTurn) binding.btnRandom.invisible()
                val timeEnd = System.currentTimeMillis()
                dLog("time random all : ${timeEnd - timeStart}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setIsCreated(false)
    }
}