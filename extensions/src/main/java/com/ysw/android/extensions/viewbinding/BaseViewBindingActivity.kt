package com.ysw.android.extensions.viewbinding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseViewBindingActivity<T: ViewBinding>: AppCompatActivity(), ViewBindingAbility<T> {

    lateinit var binding: T
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding(layoutInflater)
        doBeforeContentViewSet()
        setContentView(binding.root)
    }

    open fun doBeforeContentViewSet() {
    }

    override fun doWithViewBinding(block: (T) -> Unit) {
        with(binding) {
            block(this)
        }
    }
}