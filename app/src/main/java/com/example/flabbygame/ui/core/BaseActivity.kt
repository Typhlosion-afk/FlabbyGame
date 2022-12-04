package com.example.flabbygame.ui.core

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding


abstract class BaseActivity<VB : ViewBinding>(private val bindingInflater: (layoutInflater: LayoutInflater) -> VB) :
    AppCompatActivity() {
    private var _binding: VB? = null
    open val binding get() = _binding!!

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)

        onActivityReady()
    }

    abstract fun onActivityReady()

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}