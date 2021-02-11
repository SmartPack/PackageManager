/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.FilePicker;
import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 09, 2020
 */
public class FilePickerActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, List<String>> mLoader;
    private Handler mHandler = new Handler();
    private List<String> mData = new ArrayList<>();
    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        AppCompatImageButton mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, FilePicker.getSpanCount(this)));
        mRecycleViewAdapter = new RecycleViewAdapter(getData());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mTitle.setText(Utils.mPath.equals(getCacheDir().toString() + "/apk/") ? Utils.mApplicationName : new File(Utils.mPath).getName());

        mBack.setOnClickListener(v -> {
            Utils.delete(getCacheDir().getPath() + "/apk");
            super.onBackPressed();
        });

        mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
            if (FilePicker.isDirectory(mData.get(position))) {
                Utils.mPath = mData.get(position);
                reload();
            } else if (FilePicker.isTextFile(mData.get(position))) {
                Intent textView = new Intent(this, TextViewActivity.class);
                textView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(textView);
            } else if (FilePicker.isImageFile(mData.get(position))) {
                Intent imageView = new Intent(this, ImageViewActivity.class);
                imageView.putExtra(TextViewActivity.PATH_INTENT, mData.get(position));
                startActivity(imageView);
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.open_failed_export_message, new File(mData.get(position)).getName()))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.export), (dialogInterface, i) -> FilePicker
                                .copyToStorage(mData.get(position), this)).show();
            }
        });
    }

    private List<String> getData() {
        // Add directories
        for (File mFile : FilePicker.getFilesList()) {
            if (FilePicker.isDirectory(Utils.mPath + mFile.getName())) {
                mData.add(Utils.mPath + mFile.getName());
            }
        }
        // Add files
        for (File mFile : FilePicker.getFilesList()) {
            if (FilePicker.isFile(Utils.mPath + mFile.getName())) {
                mData.add(Utils.mPath + mFile.getName());
            }
        }
        return mData;
    }

    private void reload() {
        if (mLoader == null) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void run() {
                    mLoader = new AsyncTask<Void, Void, List<String>>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mData.clear();
                            mRecyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            mRecycleViewAdapter = new RecycleViewAdapter(getData());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(List<String> recyclerViewItems) {
                            super.onPostExecute(recyclerViewItems);
                            mRecyclerView.setAdapter(mRecycleViewAdapter);
                            mRecycleViewAdapter.notifyDataSetChanged();
                            mTitle.setText(Utils.mPath.equals(getCacheDir().toString() + "/apk/") ? Utils.mApplicationName
                                    : new File(Utils.mPath).getName());
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mLoader = null;
                        }
                    };
                    mLoader.execute();
                }
            }, 250);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Utils.snackbar(findViewById(android.R.id.content), getString(R.string.file_picker_message));
    }

    @Override
    public void onBackPressed() {
        if (Utils.mPath.equals(getCacheDir().toString() + "/apk/")) {
            Utils.delete(getCacheDir().getPath() + "/apk");
            super.onBackPressed();
        } else {
            Utils.mPath = Objects.requireNonNull(new File(Utils.mPath).getParentFile()).getPath();
            reload();
        }
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static ClickListener clickListener;

        private List<String> data;

        public RecycleViewAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            if (FilePicker.isDirectory(this.data.get(position))) {
                holder.mIcon.setImageDrawable(holder.mTitle.getContext().getResources().getDrawable(R.drawable.ic_folder));
                holder.mIcon.setColorFilter(Utils.getThemeAccentColor(holder.mTitle.getContext()));
            } else {
                holder.mIcon.setImageDrawable(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file));
                holder.mIcon.setColorFilter(Utils.isDarkTheme(holder.mIcon.getContext()) ? holder.mIcon.getContext()
                        .getResources().getColor(R.color.colorWhite) : holder.mIcon.getContext().getResources().getColor(R.color.colorBlack));
            }
            holder.mTitle.setText(new File(this.data.get(position)).getName());
        }

        @Override
        public int getItemCount() {
            return this.data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private AppCompatImageButton mIcon;
            private MaterialTextView mTitle;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.mIcon = view.findViewById(R.id.icon);
                this.mTitle = view.findViewById(R.id.title);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(), view);
            }
        }

        public void setOnItemClickListener(ClickListener clickListener) {
            RecycleViewAdapter.clickListener = clickListener;
        }

        public interface ClickListener {
            void onItemClick(int position, View v);
        }
    }

}