package com.charactor.avatar.maker.pfp.activity_app.main

import android.annotation.SuppressLint
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.rateApp
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.startIntentRightToLeft
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.helper.LanguageHelper
import com.charactor.avatar.maker.pfp.core.helper.MediaHelper
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.RateState
import com.charactor.avatar.maker.pfp.databinding.ActivityHomeBinding
import com.charactor.avatar.maker.pfp.activity_app.SettingsActivity
import com.charactor.avatar.maker.pfp.activity_app.my_creation.MyCreationActivity
import com.charactor.avatar.maker.pfp.activity_app.customize.CustomizeCharacterActivity
import com.charactor.avatar.maker.pfp.choose_character.ChooseCharacterActivity
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        deleteTempFolder()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.setOnSingleClick { startIntentRightToLeft(SettingsActivity::class.java) }
            btnCreate.setOnSingleClick { startIntentRightToLeft(ChooseCharacterActivity::class.java) }
            btnMyCreation.setOnSingleClick { startIntentRightToLeft(MyCreationActivity::class.java) }
            btnRandom.setOnSingleClick { startIntentRightToLeft(ChooseCharacterActivity::class.java, true) }
        }
    }

    override fun initText() {
        super.initText()
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            cvLogo.gone()
            tvCenter.text = strings(R.string.character_maker)
            tvCenter.select()
            tvCenter.gone()
            btnActionBarRight.setImageResource(R.drawable.ic_settings)
            btnActionBarRight.visible()

            var widthHeightInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                40f,
                resources.displayMetrics
            )
            btnActionBarRight.layoutParams = btnActionBarRight.layoutParams.apply {
                width = widthHeightInPx.toInt()
                height = widthHeightInPx.toInt()
            }
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        Log.d("MainActivity", "onBackPressed called")

        if (!sharePreference.getIsRate(this)) {
            // ✅ Lấy counter hiện tại
            val currentCount = sharePreference.getCountBack()
            Log.d("MainActivity", "Current count BEFORE increment: $currentCount")

            // ✅ Tăng counter và LƯU NGAY
            val newCount = currentCount + 1
            sharePreference.setCountBack(newCount)

            // ✅ Verify đã lưu thành công
            val verifyCount = sharePreference.getCountBack()
            Log.d("MainActivity", "Count AFTER save: $verifyCount")
            Log.d("MainActivity", "getIsRate: ${sharePreference.getIsRate(this)}")
            Log.d("MainActivity", "check % 2: ${newCount % 2}")

            if (newCount % 2 == 0) {
                // Lần chẵn - hiện dialog
                Log.d("MainActivity", "Even count ($newCount) - Showing rate dialog")
                rateApp(sharePreference) { state ->
                    Log.d("MainActivity", "Rate callback: $state")

                    when (state) {
                        RateState.LESS3 -> {
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(500)
                                finishAffinity() // ✅ Dùng finishAffinity() thay vì exitProcess(0)
                            }
                        }

                        RateState.GREATER3 -> {
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(500)
                                finishAffinity()
                            }
                        }

                        RateState.CANCEL -> {
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(500)
                                finishAffinity()
                            }
                        }
                    }
                }
            } else {
                // Lần lẻ - thoát luôn
                Log.d("MainActivity", "Odd count ($newCount) - exit without dialog")
                finishAffinity() // ✅ Dùng finishAffinity() thay vì exitProcess(0)
            }
        } else {
            // Đã rate rồi thì thoát luôn
            Log.d("MainActivity", "Already rated - exit")
            finishAffinity() // ✅ Dùng finishAffinity() thay vì exitProcess(0)
        }
    }
    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataTemp = MediaHelper.getImageInternal(this@MainActivity, ValueKey.DOWNLOAD_ALBUM_BACKGROUND)
            if (dataTemp.isNotEmpty()) {
                dataTemp.forEach {
                    val file = File(it)
                    file.delete()
                }
            }
        }
    }

    private fun updateText() {
        binding.apply {
            tv1.text = strings(R.string.character_maker)
            tv2.text = strings(R.string.my_creation)
        }
    }

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        updateText()
    }
}