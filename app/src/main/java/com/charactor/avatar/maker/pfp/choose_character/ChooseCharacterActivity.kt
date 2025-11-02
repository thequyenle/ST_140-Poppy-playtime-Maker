package com.charactor.avatar.maker.pfp.choose_character

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
import com.charactor.avatar.maker.pfp.activity_app.main.DataViewModel
import com.charactor.avatar.maker.pfp.activity_app.random_character.RandomCharacterActivity
import com.charactor.avatar.maker.pfp.core.base.BaseActivity
import com.charactor.avatar.maker.pfp.core.extensions.handleBackLeftToRight
import com.charactor.avatar.maker.pfp.core.extensions.select
import com.charactor.avatar.maker.pfp.core.extensions.setOnSingleClick
import com.charactor.avatar.maker.pfp.core.extensions.startIntentRightToLeft
import com.charactor.avatar.maker.pfp.core.extensions.strings
import com.charactor.avatar.maker.pfp.core.extensions.visible
import com.charactor.avatar.maker.pfp.core.utils.key.IntentKey
import com.charactor.avatar.maker.pfp.databinding.ActivityChooseCharacterBinding
import kotlinx.coroutines.launch

class ChooseCharacterActivity : BaseActivity<ActivityChooseCharacterBinding>() {
    private val viewModel: ChooseCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val chooseCharacterAdapter by lazy { ChooseCharacterAdapter() }
    override fun setViewBinding(): ActivityChooseCharacterBinding {
        return ActivityChooseCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        viewModel.setIsRandom(intent.getBooleanExtra(IntentKey.INTENT_KEY, false))
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { data ->
                if (data.isNotEmpty()) {
                    chooseCharacterAdapter.submitList(data)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
        }
        chooseCharacterAdapter.onItemClick = { position ->
            if (!viewModel.isRandom.value){
                startIntentRightToLeft(CustomizeCharacterActivity::class.java, position)
            }else{
                startIntentRightToLeft(RandomCharacterActivity::class.java, position)

            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
        }
    }

    private fun initRcv(){
        binding.rcvCharacter.apply {
            adapter = chooseCharacterAdapter
            itemAnimator = null
        }
    }

}