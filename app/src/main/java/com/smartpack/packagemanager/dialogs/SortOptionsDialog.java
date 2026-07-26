package com.smartpack.packagemanager.dialogs;

import android.content.Context;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.PackageData;

import in.sunilpaulmathew.sCommon.CommonUtils.sCommonUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 26, 2026
 */
public abstract class SortOptionsDialog extends BottomSheetDialog {

    public SortOptionsDialog(Context context) {
        super(context);

        View view = View.inflate(context, R.layout.layout_sort_options, null);

        ChipGroup chipGroupShow = view.findViewById(R.id.chipSortApps);
        SwitchMaterial switchReverse = view.findViewById(R.id.switchReverseOrder);

        chipGroupShow.setSingleSelection(true);
        chipGroupShow.setSelectionRequired(true);

        int currentSort = PackageData.getSortingType(context);
        if (currentSort == 4) chipGroupShow.check(R.id.size);
        else if (currentSort == 3) chipGroupShow.check(R.id.timeUpdated);
        else if (currentSort == 2) chipGroupShow.check(R.id.timeInstalled);
        else if (currentSort == 1) chipGroupShow.check(R.id.packageName);
        else if (currentSort == 0) chipGroupShow.check(R.id.appName);

        switchReverse.setChecked(sCommonUtils.getBoolean("reverse_order", false, context));

        chipGroupShow.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                PackageData.setSortingType(getTargetSort(checkedIds.get(0)), context);
                onItemClicked();
            }
        });

        switchReverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sCommonUtils.saveBoolean("reverse_order", isChecked, context);
            onItemClicked();
        });

        setContentView(view);
        show();
    }

    private int getTargetSort(int checkedId) {
        if (checkedId == R.id.size) return 4;
        if (checkedId == R.id.timeUpdated) return 3;
        if (checkedId == R.id.timeInstalled) return 2;
        if (checkedId == R.id.packageName) return 1;
        return 0;
    }

    public abstract void onItemClicked();
}