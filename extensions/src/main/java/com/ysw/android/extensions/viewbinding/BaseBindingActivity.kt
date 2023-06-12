package com.ysw.android.extensions.viewbinding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseBindingActivity<T : ViewBinding> : AppCompatActivity() {

    lateinit var viewBinding: T
        private set

    lateinit var activityLaunchHelper: ActivityLaunchHelper
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = generateViewBinding()
        setContentView(viewBinding.root)
        activityLaunchHelper = generateActivityLauncherHelper()
    }

    abstract fun generateViewBinding(): T

    protected open fun generateActivityLauncherHelper(): ActivityLaunchHelper {
        return ActivityLaunchHelper(this, this, this)
    }
}