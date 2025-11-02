package com.charactor.avatar.maker.pfp.activity_app.view

import android.content.pm.PackageManager
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.viewModels
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
                btnActionBarNextToRight.setOnSingleClick { viewModel.shareFiles(this@ViewActivity) }
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
                btnActionBarRight.setImageResource(R.drawable.ic_delete_red)
                btnActionBarRight.visible()
            }

            tvSuccess.gone()
            (layoutBottom.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = Gravity.BOTTOM
                setMargins(0, 0, 0, UnitHelper.pxToDpInt(this@ViewActivity, 24))
            }
            layoutBottom.requestLayout()


            binding.bottomBar.apply {
                tvBottomLeft.text = strings(R.string.share)
                tvBottomLeft.select()
                imvBottomLeft.setImageResource(R.drawable.ic_share_white)

                tvBottomRight.text = strings(R.string.download)
                tvBottomRight.select()
                imvBottomRight.setImageResource(R.drawable.ic_download_white)
            }
        }
    }

    private fun setUpSuccessUI() {
        binding.apply {
            actionBar.apply {
                btnActionBarRight.setImageResource(R.drawable.ic_home)
                btnActionBarRight.visible()

                tvCenter.text = strings(R.string.successfully)
                tvCenter.visible()
                tvCenter.select()

                btnActionBarNextToRight.setImageResource(R.drawable.ic_share_suc)
                btnActionBarNextToRight.visible()
            }

            tvSuccess.visible()

            binding.bottomBar.apply {
                tvBottomLeft.text = strings(R.string.my_creation)
                tvBottomLeft.select()
                imvBottomLeft.setImageResource(R.drawable.ic_my_creation)

                tvBottomRight.text = strings(R.string.download)
                tvBottomRight.select()
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
                startIntentRightToLeft(MyCreationActivity::class.java, true)
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
