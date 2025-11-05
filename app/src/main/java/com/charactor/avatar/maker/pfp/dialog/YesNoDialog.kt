package com.charactor.avatar.maker.pfp.dialog

import android.app.Activity
import android.graphics.Color
import com.charactor.avatar.maker.pfp.core.extensions.gone
import com.charactor.avatar.maker.pfp.core.extensions.hideNavigation
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.R
import com.charactor.avatar.maker.pfp.core.base.BaseDialog
import com.charactor.avatar.maker.pfp.core.extensions.dp
import com.charactor.avatar.maker.pfp.core.extensions.strings
import com.charactor.avatar.maker.pfp.databinding.DialogConfirmBinding


class YesNoDialog(
    val context: Activity, val title: Int, val description: Int, val isError: Boolean = false
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        initBottom()
        initText()
        if (isError) {
            binding.flBottom.btnBottomLeft.gone()
        }
        context.hideNavigation()
    }

    override fun initAction() {
        binding.apply {
            flBottom.btnBottomLeft.setOnSingleClick {
                onYesClick.invoke()
            }
            flBottom.btnBottomRight.setOnSingleClick {
                onNoClick.invoke()
            }
            flOutSide.setOnSingleClick {
                onDismissClick.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            tvTitle.text = context.getString(title)
            tvDescription.text = context.getString(description)
        }
    }

    private fun initBottom() {
        binding.flBottom.apply {
            imvBottomLeft.gone()
            imvBottomRight.gone()

            // Yes button (left)
            btnBottomLeft.apply {
                setBackgroundResource(R.drawable.bg_dialog_yes)
                layoutParams = layoutParams.apply { height = 44.dp }
            }
            tvBottomLeft.text = context.strings(R.string.yes)
            tvBottomLeft.setTextColor(Color.parseColor("#A717E0"))

            // No button (right)
            btnBottomRight.apply {
                setBackgroundResource(R.drawable.bg_dialog_no)
                layoutParams = layoutParams.apply { height = 44.dp }
            }
            tvBottomRight.text = context.strings(R.string.no)
            tvBottomRight.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }
}