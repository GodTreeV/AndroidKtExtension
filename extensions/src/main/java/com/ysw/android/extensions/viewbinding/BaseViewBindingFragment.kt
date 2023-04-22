package com.ysw.android.extensions.viewbinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseViewBindingFragment<T: ViewBinding>: Fragment(), ViewBindingAbility<T> {
    lateinit var binding: T
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return getViewBinding(inflater).root
    }

    override fun doWithViewBinding(block: (T) -> Unit) {
        with(binding) {
            block(this)
        }
    }
}