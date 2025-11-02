package com.charactor.avatar.maker.pfp.activity_app.my_creation

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.checkPermissions
import com.charactor.avatar.maker.pfp.core.extensions.goToSettings
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.handleBackLeftToRight
import com.charactor.avatar.maker.pfp.core.extensions.hideNavigation
import com.charactor.avatar.maker.pfp.core.extensions.invisible
import com.charactor.avatar.maker.pfp.core.extensions.requestPermission
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.setTextContent
import com.charactor.avatar.maker.pfp.core.extensions.showToast
import com.charactor.avatar.maker.pfp.core.extensions.startIntentWithClearTop
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.helper.UnitHelper
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.core.utils.key.RequestKey
import com.charactor.avatar.maker.pfp.core.utils.key.ValueKey
import com.charactor.avatar.maker.pfp.core.utils.state.HandleState
import com.charactor.avatar.maker.pfp.databinding.ActivityAlbumBinding
import com.charactor.avatar.maker.pfp.dialog.YesNoDialog
import com.charactor.avatar.maker.pfp.activity_app.main.MainActivity
import com.charactor.avatar.maker.pfp.activity_app.view.ViewActivity
import com.charactor.avatar.maker.pfp.databinding.PopupMyAlbumBinding
import com.charactor.avatar.maker.pfp.core.extensions.strings
import com.charactor.avatar.maker.pfp.activity_app.permission.PermissionViewModel
import kotlinx.coroutines.launch

class MyCreationActivity : BaseActivity<ActivityAlbumBinding>() {
    private val viewModel: MyCreationViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val adapterMyAlbum by lazy { MyCreationAdapter(this) }

    private val selectList by lazy {
        arrayListOf(
            binding.actionBar.btnActionBarRight,
            binding.actionBar.btnActionBarNextToRight,
            binding.layoutBottom,
        )
    }

    override fun setViewBinding(): ActivityAlbumBinding {
        return ActivityAlbumBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        resetData()
        viewModel.setStatusFrom(intent.getBooleanExtra(IntentKey.FROM_SAVE, false))
    }

    override fun dataObservable() {
//        albumList
        lifecycleScope.launch {
            viewModel.albumList.collect { list ->
                adapterMyAlbum.submitList(list)

                if (list.isEmpty()) {
                    binding.layoutNoItem.visible()
                    handleSelectList(true)
                } else {
                    binding.layoutNoItem.gone()
                }

            }
        }

//        downloadState
        lifecycleScope.launch {
            viewModel.downloadState.collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        showToast(R.string.download_success)
                        dismissLoading(true)
                        resetData()
                    }

                    HandleState.FAIL -> {
                        showToast(R.string.download_failed_please_try_again_later)
                        dismissLoading(true)
                    }

                    HandleState.NOT_SELECT -> {
                        showToast(R.string.please_select_an_image)
                        dismissLoading(true)
                    }
                }
            }
        }

//        sharedState
        lifecycleScope.launch {
            viewModel.sharedState.collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        resetData()
                    }

                    HandleState.FAIL -> {}
                    HandleState.NOT_SELECT -> {
                        showToast(R.string.please_select_an_image)
                        dismissLoading(true)
                    }
                }
            }
        }

//        deleteState
        lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading(true)
                        resetData()
                    }

                    HandleState.FAIL -> {
                        dismissLoading(true)
                    }

                    HandleState.NOT_SELECT -> {
                        showToast(R.string.please_select_an_image)
                        dismissLoading(true)
                    }
                }
            }
        }

//        isLastItem
        lifecycleScope.launch {
            viewModel.isLastItem.collect { selectStatus ->
                changeImageActionBarRight(selectStatus)
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.setOnSingleClick {
                    if (viewModel.isShowLongCLick.value) {
                        resetData()
                    } else {
                        if (viewModel.isFromSuccess.value) {
                            startIntentWithClearTop(MainActivity::class.java)
                        } else {
                            handleBackLeftToRight()
                        }
                    }
                }

                btnActionBarRight.setOnSingleClick { handleSelectAll() }
                btnActionBarNextToRight.setOnSingleClick { confirmDeleteItem() }
            }

            bottomBar.apply {
                btnBottomLeft.setOnSingleClick { viewModel.shareFiles(this@MyCreationActivity) }
                btnBottomRight.setOnSingleClick { handleBottomRight() }
            }

            rcvMyAlbum.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
                    return when {
                        motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                            motionEvent.x, motionEvent.y
                        ) != null -> false

                        else -> {
                            resetData()
                            true
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            })
        }

        adapterMyAlbum.apply {
            onItemClick = { path -> handleItemClick(path) }
            onMoreClick = { path, position, view -> handleMoreMyAlbum(path, position, view) }
            onLongClick = { position -> handleLongClick(position) }
            onItemTick = { position -> viewModel.toggleSelect(position) }
        }

    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()

            tvCenter.setTextContent(this@MyCreationActivity, R.string.my_creation)
            tvCenter.visible()

            btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
            btnActionBarNextToRight.setImageResource(R.drawable.ic_delete_red)
        }

        binding.bottomBar.apply {
            tvBottomLeft.text = strings(R.string.share)
            tvBottomLeft.select()
            imvBottomLeft.setImageResource(R.drawable.ic_share_white)

            tvBottomRight.text = strings(R.string.download)
            tvBottomRight.select()
            imvBottomRight.setImageResource(R.drawable.ic_download_white)
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvMyAlbum.adapter = adapterMyAlbum
            rcvMyAlbum.itemAnimator = null
        }
    }

    private fun handleSelectList(isHide: Boolean) {
        if (isHide) {
            selectList.forEachIndexed { index, view ->
                if (index == 0) view.invisible() else view.gone()
            }
            viewModel.setShowLongClick(false)
        } else {
            selectList.forEach { it.visible() }
        }
    }

    private fun handleItemClick(path: String) {
        val intent = Intent(this, ViewActivity::class.java)
        intent.putExtra(IntentKey.INTENT_KEY, path)
        intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
        startActivity(intent, options.toBundle())
    }

    private fun handleLongClick(position: Int) {
        viewModel.showLongClick(position)
        handleSelectList(false)
    }

    private fun resetData() {
        viewModel.loadAlbum(this@MyCreationActivity)
        handleSelectList(true)
        changeImageActionBarRight(true)
    }

    private fun changeImageActionBarRight(isReset: Boolean) {
        val res = if (isReset) R.drawable.ic_not_select_all else R.drawable.ic_select_all
        binding.actionBar.btnActionBarRight.setImageResource(res)
    }

    private fun handleSelectAll() {
        val shouldSelectAll = viewModel.albumList.value.any { !it.isSelected }
        changeImageActionBarRight(!shouldSelectAll)
        viewModel.selectAll(shouldSelectAll)
    }

    override fun onRestart() {
        super.onRestart()
        resetData()
    }

    private fun handleBottomRight(onlyPath: String = "") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (onlyPath == "") {
                viewModel.downloadFiles(this)
            } else {
                viewModel.downloadFiles(this, onlyPath)
            }
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                if (onlyPath != "") {
                    viewModel.downloadFiles(this)
                } else {
                    viewModel.downloadFiles(this, onlyPath)
                }
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
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
                showToast(R.string.granted_storage)
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    private fun handleMoreMyAlbum(path: String, position: Int, view: View) {
        val popupBinding = PopupMyAlbumBinding.inflate(LayoutInflater.from(this))
        val popupWindow = PopupWindow(popupBinding.root, WRAP_CONTENT, WRAP_CONTENT, true).apply {
            elevation = 10f
            setOnDismissListener { hideNavigation() }
        }

        with(popupBinding) {
            tvDownload.select()
            tvShare.select()
            tvDelete.select()

            mnuDelete.setOnSingleClick {
                popupWindow.dismiss()
                confirmDeleteItem(path)
            }
            mnuShare.setOnSingleClick {
                popupWindow.dismiss()
                viewModel.shareFiles(this@MyCreationActivity, path)
            }
            mnuDownload.setOnSingleClick {
                popupWindow.dismiss()
                handleBottomRight(path)
            }
        }

        val location = IntArray(2).apply { view.getLocationOnScreen(this) }
        val distanceToBottom = resources.displayMetrics.heightPixels - location[1] - view.height
        val yOffset = if (distanceToBottom >= UnitHelper.dpToPx(this, 180)) UnitHelper.dpToPx(
            this, 6
        ) else UnitHelper.dpToPx(this, -165)

        popupWindow.showAsDropDown(view, UnitHelper.dpToPx(this, -135), yOffset)
    }

    private fun confirmDeleteItem(onlyPath: String = "") {
        val dialog =
            YesNoDialog(this, R.string.delete_your_customize, R.string.are_you_sure_you_want_to_delete_the_image)
        dialog.show()
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onYesClick = {
            dialog.dismiss()
            hideNavigation()

            if (onlyPath == "") {
                viewModel.deleteFiles()
            } else {
                viewModel.deleteFiles(onlyPath)
            }
        }
    }
}