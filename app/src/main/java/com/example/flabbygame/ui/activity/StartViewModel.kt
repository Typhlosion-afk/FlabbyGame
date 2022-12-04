package com.example.flabbygame.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flabbygame.util.SharedPrefs
import kotlinx.coroutines.launch

class StartViewModel: ViewModel() {
    private val _isSoundOff = MutableLiveData<Boolean>(SharedPrefs[SOUND_KEY, Boolean::class.java])
    val isSoundOff : LiveData<Boolean> get() = _isSoundOff

    private val _isMusicOff = MutableLiveData<Boolean>(SharedPrefs[MUSIC_KEY, Boolean::class.java])
    val isMusicOff : LiveData<Boolean> get() = _isMusicOff

    private val _colorPos = MutableLiveData<Int>(SharedPrefs[KEY_COLOR_CHOSEN, Int::class.java])
    val colorPos : LiveData<Int> get() = _colorPos

    fun changeMusicState(){
        _isMusicOff.value = !_isMusicOff.value!!
    }

    fun changeSoundState(){
        _isSoundOff.value = !isSoundOff.value!!
    }

    fun saveSetting(){
        viewModelScope.launch {
            SharedPrefs.put(SOUND_KEY, _isSoundOff.value!!)
            SharedPrefs.put(MUSIC_KEY, _isMusicOff.value!!)
            SharedPrefs.put(KEY_COLOR_CHOSEN, _colorPos.value!!)
        }
    }

    fun setColorPos(i: Int) {
        _colorPos.value = i
    }

    companion object {
        const val SOUND_KEY = "sound_key"
        const val MUSIC_KEY = "music_key"
        const val KEY_COLOR_CHOSEN = "last_color"
    }
}