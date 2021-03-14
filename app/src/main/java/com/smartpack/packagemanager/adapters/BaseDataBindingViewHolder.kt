package com.smartpack.packagemanager.adapters

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 */
abstract class BaseDataBindingViewHolder<T>(
    binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: T)
}
