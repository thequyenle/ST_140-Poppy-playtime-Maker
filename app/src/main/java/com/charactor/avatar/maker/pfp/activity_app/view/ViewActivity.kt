package com.charactor.avatar.maker.pfp.activity_app.view

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.checkPermissions
import com.charactor.avatar.maker.pfp.core.extensions.goToSettings
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.handleBackLeftToRight
import com.charactor.avatar.maker.pfp.core.extensions.hideNavigation
import com.charactor.avatar.maker.pfp.core.extensions.loadImageGlide
import com.charactor.avatar.maker.pfp.core.extensions.requestPermission
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.showToast
import com.charactor.avatar.maker.pfp.core.extensions.startIntentWithClearTop
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.helper.LanguageHelper
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.core.utils.key.RequestKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.HandleState
import com.charactor.avatar.maker.pfp.databinding.ActivityViewBinding
import com.charactor.avatar.maker.pfp.dialog.YesNoDialog
import com.charactor.avatar.maker.pfp.activity_app.main.MainActivity
import com.charactor.avatar.maker.pfp.activity_app.my_creation.MyCreationActivity
import com.charactor.avatar.maker.pfp.core.extensions.startIntentRightToLeft
import com.charactor.avatar.maker.pfp.core.extensions.strings
import com.charactor.avatar.maker.pfp.core.helper.UnitHelper
import com.charactor.avatar.maker.pfp.activity_app.permission.PermissionViewModel
import com.charactor.avatar.maker.pfp.core.extensions.setFont
import kotlinx.coroutines.launch

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val viewModel: ViewViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY)!!)
        viewModel.setType(intent.getIntExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW))
    }

    override fun dataObservable() {

//        pathInternal
        lifecycleScope.launch {
            viewModel.pathInternal.collect { path ->
                loadImageGlide(this@ViewActivity, path, binding.imvImage)
            }
        }

//        typeUI
        lifecycleScope.launch {
            viewModel.typeUI.collect { type ->
                if (type != -1) {
                    when (type) {
                        ValueKey.TYPE_VIEW -> setUpViewUI()
                        else -> setUpSuccessUI()
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
                btnActionBarRight.setOnSingleClick { handleActionBarRight() }
                btnActionBarNextToRight.setOnSingleClick { startIntentRightToLeft(MyCreationActivity::class.java, true) }

            }
            bottomBar.apply {
                btnBottomLeft.setOnSingleClick { handleBottomBarLeft() }
                btnBottomRight.setOnSingleClick { handleBottomBarRight() }
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
        }
        binding.bottomBar.apply {
            tvBottomLeft.select()
            tvBottomRight.select()
        }
    }

    private fun setUpViewUI() {
        binding.apply {
            actionBar.apply {
                btnActionBarRight.apply {
                    setImageResource(R.drawable.ic_delete_red)
                    visible()

                    // Set kích thước 40x40 dp
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = (40 * resources.displayMetrics.density).toInt()  // 40dp
                        height = (40 * resources.displayMetrics.density).toInt() // 40dp
                    }
                }

                tvCenter.apply {
                    text = strings(R.string.title_view)  // Thay text bạn muốn
                    setFont(R.font.creepstercaps_regular)  // Hoặc font bạn muốn
                    textSize = 32f  // 16sp
                    setTextColor(Color.parseColor("#FFFFFF"))  // Màu trắng
                    select()
                }
                tvCenter.visible()
            }

            tvSuccess.gone()
            (layoutBottom.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = Gravity.BOTTOM
                setMargins(0, 0, 0, UnitHelper.pxToDpInt(this@ViewActivity, 24))
            }
            layoutBottom.requestLayout()


            binding.bottomBar.apply {
                tvBottomLeft.apply {
                    text = strings(R.string.share)
                    setFont(R.font.creepstercaps_regular)  // Hoặc font bạn muốn
                    textSize = 20f  // 20sp
                    setTextColor(ContextCompat.getColor(this@ViewActivity, R.color.purple))  // Hoặc dùng hex
                    // setTextColor(Color.parseColor("#A717E0"))  // Cách 2: Dùng hex trực tiếp
                    select()
                }

                imvBottomLeft.setImageResource(R.drawable.ic_share_white)

                tvBottomRight.apply {
                    text = strings(R.string.download)
                    setFont(R.font.creepstercaps_regular)  // Hoặc font bạn muốn
                    textSize = 20f  // 20sp
                    setTextColor(ContextCompat.getColor(this@ViewActivity, R.color.purple))  // Hoặc dùng hex
                    // setTextColor(Color.parseColor("#A717E0"))  // Cách 2: Dùng hex trực tiếp
                    select()
                }
                imvBottomRight.setImageResource(R.drawable.ic_download_white)
            }
        }
    }

    private fun setUpSuccessUI() {
        binding.apply {
            actionBar.apply {
                btnActionBarRight.apply {
                    setImageResource(R.drawable.ic_home)
                    visible()

                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = UnitHelper.dpToPx(this@ViewActivity, 40)
                        height = UnitHelper.dpToPx(this@ViewActivity, 40)
                    }
                }
                btnActionBarNextToRight.apply {
                    setImageResource(R.drawable.ic_my_creation)
                    visible()

                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = UnitHelper.dpToPx(this@ViewActivity, 40)
                        height = UnitHelper.dpToPx(this@ViewActivity, 40)
                    }
                }
            }

            tvSuccess.visible()

            binding.bottomBar.apply {
                tvBottomLeft.apply {
                    text = strings(R.string.share)
                    setFont(R.font.creepstercaps_regular)  // Hoặc font bạn muốn
                    textSize = 20f  // 20sp
                    setTextColor(ContextCompat.getColor(this@ViewActivity, R.color.purple))  // Hoặc dùng hex
                    // setTextColor(Color.parseColor("#A717E0"))  // Cách 2: Dùng hex trực tiếp
                    select()
                }
                imvBottomLeft.setImageResource(R.drawable.ic_share_white)

                tvBottomRight.apply {
                    text = strings(R.string.download)
                    setFont(R.font.creepstercaps_regular)  // Hoặc font bạn muốn
                    textSize = 20f  // 20sp
                    setTextColor(ContextCompat.getColor(this@ViewActivity, R.color.purple))  // Hoặc dùng hex
                    // setTextColor(Color.parseColor("#A717E0"))  // Cách 2: Dùng hex trực tiếp
                    select()
                }
                imvBottomRight.setImageResource(R.drawable.ic_download_white)
            }
        }
    }

    private fun handleActionBarRight() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                handleDelete()
            }

            else -> {
                startIntentWithClearTop(MainActivity::class.java)
            }
        }
    }

    private fun handleBottomBarLeft() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                viewModel.shareFiles(this@ViewActivity)
            }

            else -> {
                viewModel.shareFiles(this@ViewActivity)
            }
        }
    }

    private fun handleBottomBarRight() {
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@ViewActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }

                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }

                }
            }
        }

    }

    private fun handleDelete() {
        val dialog = YesNoDialog(this, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onYesClick = {
            dialog.dismiss()
            lifecycleScope.launch {
                viewModel.deleteFile(viewModel.pathInternal.value).collect { state ->
                    when (state) {
                        HandleState.LOADING -> showLoading()
                        HandleState.SUCCESS -> {
                            dismissLoading()
                            finish()
                        }

                        else -> {
                            dismissLoading()
                            showToast(R.string.delete_failed_please_try_again)
                        }
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }
}
