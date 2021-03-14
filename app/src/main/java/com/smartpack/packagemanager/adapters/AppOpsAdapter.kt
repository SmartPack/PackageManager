package com.smartpack.packagemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.textview.MaterialTextView
import com.smartpack.packagemanager.databinding.ListItemOpBinding
import com.smartpack.packagemanager.utils.AppOperation

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 */
class AppOpsAdapter(
    private val listener: OnAppOperationClickedListener
) : ListAdapter<AppOperation, BaseDataBindingViewHolder<*>>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppOpViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemOpBinding.inflate(
            inflater, parent, false
        )
        return AppOpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseDataBindingViewHolder<*>, position: Int) {
        if (holder is AppOpViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun updateData(newList: List<AppOperation>) {
        submitList(newList)
    }

    inner class AppOpViewHolder(
        val binding: ListItemOpBinding
    ) : BaseDataBindingViewHolder<AppOperation>(binding) {
        override fun bind(item: AppOperation) {
            binding.operation = item
            binding.executePendingBindings()

            if (!item.writable) {
                binding.operationItemLayout.forEach { child ->
                    child.isEnabled = false
                }
            } else {
                binding.onAppOperationClickedListener = listener
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppOperation>() {
        override fun areItemsTheSame(oldItem: AppOperation, newItem: AppOperation): Boolean {
            return oldItem.name == newItem.name // Name pretty much functions as identifier here 
        }

        override fun areContentsTheSame(oldItem: AppOperation, newItem: AppOperation): Boolean {
            return oldItem == newItem
        }
    }
}

@BindingAdapter("binding:determineVisibility")
fun MaterialTextView.determineVisibility(description: String) {
    visibility = if (description.trim().isEmpty()) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

interface OnAppOperationClickedListener {
    fun onAppOperationClicked(operation: AppOperation)
}