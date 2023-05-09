package com.ysw.android.extensions.databinding

import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.ysw.android.extensions.layoutInflater

abstract class AbsBindingRVAdapter<T : ViewDataBinding, D : Any> :
    RecyclerView.Adapter<AbsBindingRVAdapter.BindingViewHolder<T>>() {

    open class BindingViewHolder<T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)

    abstract val data: MutableList<D>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<T> {
        return generateBindingViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<T>, position: Int) {
        bindDefaultData(holder, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    protected open fun generateBindingViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<T> {
        return BindingViewHolder(
            DataBindingUtil.inflate(
                parent.context.layoutInflater,
                getLayoutRes(),
                parent,
                false
            )
        )
    }

    protected open fun bindDefaultData(holder: BindingViewHolder<T>, position: Int) {
        holder.binding.setVariable(getVariableId(), data[position])
    }

    @LayoutRes
    abstract fun getLayoutRes(): Int

    @IdRes
    abstract fun getVariableId(): Int
}