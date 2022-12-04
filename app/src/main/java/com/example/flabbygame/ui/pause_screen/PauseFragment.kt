package com.example.flabbygame.ui.pause_screen

import com.example.flabbygame.databinding.FragmentPauseBinding
import com.example.flabbygame.ui.core.BaseFragment
import com.example.flabbygame.ui.game_over.GameOverFragment

class PauseFragment : BaseFragment<FragmentPauseBinding>(FragmentPauseBinding::inflate){
    private var onClick: PauseScreenEvent? = null
    override fun onViewReady() {
        initAction()
    }

    private fun initAction(){
        binding.btnContinue.setOnClickListener {
            onClick?.onContinueClick()
        }

        binding.btnHome.setOnClickListener {
            onClick?.onHomeClick()
        }

        binding.btnReset.setOnClickListener {
            onClick?.onNewGameClick()
        }
    }

    fun setClickEvent(clickEvent: PauseScreenEvent){
        this.onClick = clickEvent
    }

    interface PauseScreenEvent{
        fun onHomeClick()
        fun onNewGameClick()
        fun onContinueClick()
    }
}