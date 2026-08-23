package com.smartpack.packagemanager.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.adapters.BottomMenuAdapter;
import com.smartpack.packagemanager.utils.Serializables.MenuItems;

import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on August 13, 2025
 */
public abstract class BatchOptionsDialog extends BottomSheetDialog {

    public BatchOptionsDialog(List<MenuItems> menuItems, String currentStatus, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_batch_options, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new BottomMenuAdapter(menuItems, currentStatus, id -> {
            onMenuItemClicked(id);
            dismiss();
        }, currentStatus == null));
        setContentView(rootView);
        show();
    }

    public abstract void onMenuItemClicked(int menuID);

}