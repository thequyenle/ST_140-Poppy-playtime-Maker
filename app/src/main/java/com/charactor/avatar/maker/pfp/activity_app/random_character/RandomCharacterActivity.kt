package com.charactor.avatar.maker.pfp.activity_app.random_character

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.activity_app.customize.CustomizeCharacterActivity
import com.charactor.avatar.maker.pfp.activity_app.customize.CustomizeCharacterViewModel
import com.charactor.avatar.maker.pfp.activity_app.main.DataViewModel
import com.charactor.avatar.maker.pfp.choose_character.ChooseCharacterViewModel
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.dLog
import com.charactor.avatar.maker.pfp.core.extensions.eLog
import com.charactor.avatar.maker.pfp.core.extensions.handleBackLeftToRight
import com.charactor.avatar.maker.pfp.core.extensions.hideNavigation
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.startIntentRightToLeft
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.data.model.custom.CustomizeModel
import com.charactor.avatar.maker.pfp.data.model.custom.SuggestionModel
import com.charactor.avatar.maker.pfp.databinding.ActivityRandomCharacterBinding
import com.charactor.avatar.maker.pfp.dialog.YesNoDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class RandomCharacterActivity : BaseActivity<ActivityRandomCharacterBinding>() {
    private val viewModel: RandomCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val customizeCharacterViewModel: CustomizeCharacterViewModel by viewModels()
    private val randomCharacterAdapter by lazy { RandomCharacterAdapter(this) }

    override fun setViewBinding(): ActivityRandomCharacterBinding {
        return ActivityRandomCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { data ->
                if (data.isNotEmpty()) {
                    customizeCharacterViewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                    customizeCharacterViewModel.setDataCustomize(data[customizeCharacterViewModel.positionSelected])
                    customizeCharacterViewModel.setIsDataAPI(customizeCharacterViewModel.positionSelected >= ValueKey.POSITION_API)
                    customizeCharacterViewModel.updateAvatarPath(customizeCharacterViewModel.dataCustomize.value!!.avatar)
                    initData()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
        }

        randomCharacterAdapter.onItemClick = { model -> handleItemClick(model)}

    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
        }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading(true)
                val dialogExit = YesNoDialog(this@RandomCharacterActivity, R.string.error, R.string.an_error_occurred)
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation()
                    startIntentRightToLeft(
                        RandomCharacterActivity::class.java, customizeCharacterViewModel.positionSelected
                    )
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            showLoading()
            // Get data from list
            val deferred1 = async {
                val timeStart1 = System.currentTimeMillis()
                customizeCharacterViewModel.resetDataList()
                customizeCharacterViewModel.addValueToItemNavList()
                customizeCharacterViewModel.setItemColorDefault()
                customizeCharacterViewModel.setBottomNavigationListDefault()
                dLog("deferred1: ${System.currentTimeMillis() - timeStart1}")
                return@async true
            }
            // Random
            val deferred2 = async {
                if (deferred1.await()) {
                    val timeStart2 = System.currentTimeMillis()
                    for (i in 0 until ValueKey.RANDOM_QUANTITY) {
                        customizeCharacterViewModel.setClickRandomFullLayer()
                        viewModel.updateRandomList(customizeCharacterViewModel.getSuggestionList())
                    }
                    dLog("deferred2: ${System.currentTimeMillis() - timeStart2}")

                }
                return@async true
            }
            // Init Rcv
            withContext(Dispatchers.Main) {
                if (deferred1.await() && deferred2.await()) {
                    dismissLoading()
                    initRcv()
                }
            }
        }
    }

    private fun initRcv() {
        binding.rcvRandomCharacter.apply {
            adapter = randomCharacterAdapter
            itemAnimator = null
        }
        randomCharacterAdapter.submitList(viewModel.randomList)
    }

    private fun handleItemClick(model: SuggestionModel){
        customizeCharacterViewModel.checkDataInternet(this){
            lifecycleScope.launch {
                showLoading()
                withContext(Dispatchers.IO){
                    MediaHelper.writeListToFile(this@RandomCharacterActivity, ValueKey.SUGGESTION_FILE_INTERNAL, arrayListOf(model))
                }
                val intent = Intent(this@RandomCharacterActivity, CustomizeCharacterActivity::class.java)
                intent.putExtra(IntentKey.INTENT_KEY, customizeCharacterViewModel.positionSelected)
                intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.SUGGESTION)
                val option = ActivityOptions.makeCustomAnimation(this@RandomCharacterActivity, R.anim.slide_out_left, R.anim.slide_in_right)
                dismissLoading()
                startActivity(intent, option.toBundle())
            }
        }
    }
}