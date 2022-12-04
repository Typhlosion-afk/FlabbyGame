package com.example.flabbygame.ui.activity

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.example.flabbygame.R
import com.example.flabbygame.databinding.ActivityStartBinding
import com.example.flabbygame.game_obj.ColorSet
import com.example.flabbygame.ui.activity.StartViewModel.Companion.MUSIC_KEY
import com.example.flabbygame.ui.activity.StartViewModel.Companion.SOUND_KEY
import com.example.flabbygame.ui.core.BaseActivity
import com.example.flabbygame.util.Constant.BEST_SCORE
import com.example.flabbygame.util.LogUtils
import com.example.flabbygame.util.SharedPrefs
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class StartActivity : BaseActivity<ActivityStartBinding>(ActivityStartBinding::inflate) {

    private lateinit var colorSet: ColorSet
    private val viewModel: StartViewModel by viewModels()
    private val colorSets = ArrayList<ColorSet>()
    private var position = 0

    override fun onActivityReady() {
        initColorSet()
        initAction()
        subscribeUi()
        initViewAction()
    }

    override fun onResume() {
        super.onResume()
        if (SharedPrefs[BEST_SCORE, Int::class.java] != 0) {
            binding.bestScoreContainer.visibility = View.VISIBLE
            binding.bestScore.text = SharedPrefs[BEST_SCORE, Int::class.java].toString()
        } else {
            binding.bestScoreContainer.visibility = View.GONE
        }
    }

    private fun initViewAction(){
        binding.toggleMusic.setOnClickListener {
            viewModel.changeMusicState()
        }

        binding.toggleSound.setOnClickListener {
            viewModel.changeSoundState()
        }

        binding.txtPlay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(KEY_COLOR_SET, colorSet)
            intent.putExtra(SOUND_KEY, viewModel.isSoundOff.value!!)
            intent.putExtra(MUSIC_KEY, viewModel.isMusicOff.value!!)

            viewModel.saveSetting()

            LogUtils.d("IS SOUND OFF: ${viewModel.isSoundOff.value!!}")
            LogUtils.d("IS MUSIC OFF: ${viewModel.isMusicOff.value!!}")

            startActivity(intent)
        }
    }

    private fun subscribeUi(){
        viewModel.isMusicOff.observe(this){
            binding.toggleMusic.isChecked = !it
        }

        viewModel.isSoundOff.observe(this){
            binding.toggleSound.isChecked = !it
        }

        viewModel.colorPos.observe(this){
            colorSet = colorSets[it]
            binding.gamePreview.setColor(colorSet)
        }
    }

    private fun initColorSet(){
        if(colorSets.isEmpty()) {
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird1),
                background = getColor(R.color.background1),
                column = getColor(R.color.column1)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird2),
                background = getColor(R.color.background2),
                column = getColor(R.color.column2)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird3),
                background = getColor(R.color.background3),
                column = getColor(R.color.column3)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird4),
                background = getColor(R.color.background4),
                column = getColor(R.color.column4)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird5),
                background = getColor(R.color.background5),
                column = getColor(R.color.column5)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird6),
                background = getColor(R.color.background6),
                column = getColor(R.color.column6)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird7),
                background = getColor(R.color.background7),
                column = getColor(R.color.column7)
            ))
            colorSets.add(ColorSet(
                bird = getColor(R.color.bird8),
                background = getColor(R.color.background8),
                column = getColor(R.color.column8)
            ))
        }
    }

    private fun initAction(){
        if(colorSets.isEmpty()){
            initColorSet()
        }
        binding.btnColor1.setOnClickListener {
            viewModel.setColorPos(0)
        }
        binding.btnColor2.setOnClickListener {
            viewModel.setColorPos(1)
        }
        binding.btnColor3.setOnClickListener {
            viewModel.setColorPos(2)
        }
        binding.btnColor4.setOnClickListener {
            viewModel.setColorPos(3)
        }
        binding.btnColor5.setOnClickListener {
            viewModel.setColorPos(4)
        }
        binding.btnColor6.setOnClickListener {
            viewModel.setColorPos(5)
        }
        binding.btnColor7.setOnClickListener {
            viewModel.setColorPos(6)
        }
        binding.btnColor8.setOnClickListener {
            viewModel.setColorPos(7)
        }

    }

    companion object {
        const val KEY_COLOR_SET = "color_set"

    }
}