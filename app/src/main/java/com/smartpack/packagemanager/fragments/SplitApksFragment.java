/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.fragments;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.SplitAPKsAdapter;
import com.smartpack.packagemanager.dialogs.ExportSuccessDialog;
import com.smartpack.packagemanager.dialogs.ProgressDialog;
import com.smartpack.packagemanager.utils.Common;
import com.smartpack.packagemanager.utils.PackageData;
import com.smartpack.packagemanager.utils.SplitAPKInstaller;

import java.io.File;

import in.sunilpaulmathew.sCommon.CommonUtils.sExecutor;
import in.sunilpaulmathew.sCommon.FileUtils.sFileUtils;
import in.sunilpaulmathew.sCommon.PackageUtils.sPackageUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 16, 2021
 */
public class SplitApksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_split_apks, container, false);

        MaterialButton mBatch = mRootView.findViewById(R.id.batch);
        RecyclerView mRecyclerView = mRootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
        SplitAPKsAdapter mRecycleViewAdapter = new SplitAPKsAdapter(SplitAPKInstaller.splitApks(sPackageUtils.getParentDir(Common.getApplicationID(), requireActivity())), requireActivity());
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mBatch.setOnClickListener(v ->
                new sExecutor() {
                    private File mParentFile;
                    private ProgressDialog mProgressDialog;
                    @SuppressLint("StringFormatInvalid")
                    @Override
                    public void onPreExecute() {
                        mProgressDialog = new ProgressDialog(v.getContext());
                        mProgressDialog.setIcon(R.mipmap.ic_launcher);
                        mProgressDialog.setTitle(getString(R.string.exporting, "..."));
                        mProgressDialog.show();
                    }

                    @Override
                    public void doInBackground() {
                        mProgressDialog.setMax(Common.getRestoreList().size());
                        PackageData.makePackageFolder(v.getContext());
                        mParentFile = new File(PackageData.getPackageDir(v.getContext()), Common.getApplicationID());
                        if (!mParentFile.exists()) {
                            sFileUtils.mkdir(mParentFile);
                        }

                        for (String apkNAme : Common.getRestoreList()) {
                            sFileUtils.copy(new File(sPackageUtils.getParentDir(Common.getApplicationID(), v.getContext()), apkNAme), new File(mParentFile, apkNAme));
                            mProgressDialog.updateProgress(1);
                        }
                    }

                    @Override
                    public void onPostExecute() {
                        mProgressDialog.dismiss();
                        mBatch.setVisibility(GONE);
                        mRecycleViewAdapter.notifyItemRangeChanged(0, mRecycleViewAdapter.getItemCount());
                        new ExportSuccessDialog(mParentFile.getAbsolutePath(), requireActivity());
                    }
                }.execute()
        );

        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.getRestoreList().clear();
    }

}