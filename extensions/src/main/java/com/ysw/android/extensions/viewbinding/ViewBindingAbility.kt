package com.ysw.android.extensions.viewbinding

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

interface ViewBindingAbility<T: ViewBinding> {
    fun getViewBinding(layoutInflater: LayoutInflater): T
    fun doWithViewBinding(block: (T) -> Unit)
}