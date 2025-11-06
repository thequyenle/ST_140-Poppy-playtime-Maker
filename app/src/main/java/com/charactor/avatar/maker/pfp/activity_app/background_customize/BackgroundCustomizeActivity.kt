package com.charactor.avatar.maker.pfp.activity_app.background_customize

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.handleBackLeftToRight
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.showToast
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.SaveState
import com.charactor.avatar.maker.pfp.databinding.ActivityBackgroundBinding
import com.charactor.avatar.maker.pfp.activity_app.view.ViewActivity
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.setFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackgroundCustomizeActivity : BaseActivity<ActivityBackgroundBinding>() {

    private val viewModel: BackgroundCustomizeViewModel by viewModels()
    private val backgroundCustomizeAdapter by lazy { BackgroundCustomizeAdapter(this) }

    override fun setViewBinding(): ActivityBackgroundBinding {
        return ActivityBackgroundBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.apply {
            binding.rcvBackground.adapter = backgroundCustomizeAdapter
            binding.rcvBackground.itemAnimator = null
            viewModel.loadBackground(this@BackgroundCustomizeActivity)
            viewModel.setPathInternalTemp(intent.getStringExtra(IntentKey.INTENT_KEY)!!)
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.pathInternalTemp.collect { path ->
                loadImageGlide(this@BackgroundCustomizeActivity, path, binding.imvImage, false)
            }
        }

        lifecycleScope.launch {
            viewModel.backgroundList.collect { list ->
                backgroundCustomizeAdapter.submitList(list)
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
                btnActionBarRightText.setOnSingleClick { handleSave() }
            }
            //quyen
            // Callback khi click NONE
            backgroundCustomizeAdapter.onNoneClick = { position ->
                handleNoneBackground(position)
            }

            // Callback khi click RANDOM
            backgroundCustomizeAdapter.onRandomClick = {
                handleRandomBackground()
            }

            // Callback khi click ảnh bình thường
            backgroundCustomizeAdapter.onItemClick = { path, position ->
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        loadImageGlide(this@BackgroundCustomizeActivity, path, imvBackground, false)
                    }

                    // Cập nhật focus
                    viewModel.changeFocusBackgroundList(position)

                    // Submit list để cập nhật UI
                    withContext(Dispatchers.Main) {
                        backgroundCustomizeAdapter.submitList(viewModel.backgroundList.value.toList())
                    }
                }
            }
            //quyen
        }

    }

    //quyen
    private fun handleNoneBackground(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Xóa ảnh background
            withContext(Dispatchers.Main) {
                binding.imvBackground.setImageBitmap(null)
            }

            // Cập nhật focus
            viewModel.changeFocusBackgroundList(position)

            // Submit list để cập nhật UI
            withContext(Dispatchers.Main) {
                backgroundCustomizeAdapter.submitList(viewModel.backgroundList.value.toList())
            }
        }
    }

    private fun handleRandomBackground() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathRandom = viewModel.randomBackground()

            withContext(Dispatchers.Main) {
                loadImageGlide(
                    this@BackgroundCustomizeActivity,
                    pathRandom,
                    binding.imvBackground,
                    false
                )
                // Submit list để cập nhật UI
                backgroundCustomizeAdapter.submitList(viewModel.backgroundList.value.toList())
            }
        }
    }
    //quyen

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()

            tvRightText.select()
            btnActionBarRightText.visible()
            tvRightText.apply {
                text = getString(R.string.save)
                textSize = 16f  // Set size 16sp
                setFont(R.font.creepstercaps_regular)
                setTextColor(ContextCompat.getColor(this@BackgroundCustomizeActivity, R.color.purple))
                gravity = Gravity.CENTER  // Căn giữa text trong button
                select()
            }
            // Set kích thước cho button
            btnActionBarRightText.apply {
                visible()
                // Set background bằng ảnh
                setBackgroundResource(R.drawable.img_btn_custom_next)

                val params = layoutParams as ConstraintLayout.LayoutParams
                params.width = (56 * resources.displayMetrics.density).toInt() // 56dp
                params.height = (40 * resources.displayMetrics.density).toInt() // 40dp
                layoutParams = params

            }

        }
    }

    private fun handleSave() {
        lifecycleScope.launch {
            binding.apply {
                viewModel.saveImageFromView(this@BackgroundCustomizeActivity, layoutExport).collect { result ->
                    when (result) {
                        is SaveState.Loading -> showLoading()
                        is SaveState.Error -> {
                            dismissLoading(true)
                            showToast(R.string.save_failed_please_try_again)
                        }

                        is SaveState.Success -> {
                            val intent = Intent(this@BackgroundCustomizeActivity, ViewActivity::class.java)
                            intent.putExtra(IntentKey.INTENT_KEY, result.path)
                            intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this@BackgroundCustomizeActivity, R.anim.slide_in_right, R.anim.slide_out_left
                            )
                            dismissLoading(true)
                            startActivity(intent, options.toBundle())
                        }
                    }
                }
            }
        }

    }
}