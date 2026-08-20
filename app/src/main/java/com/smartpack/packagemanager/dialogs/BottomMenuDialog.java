package com.smartpack.packagemanager.dialogs;

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
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 25, 2025
 */
public abstract class BottomMenuDialog extends BottomSheetDialog {

    public BottomMenuDialog(List<MenuItems> menuItems, Drawable headerIcon, String currentStatus, String headerTitle, String headerDescription, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_bottom_menu, null);
        AppCompatImageButton icon = rootView.findViewById(R.id.icon);
        MaterialTextView title = rootView.findViewById(R.id.title);
        MaterialTextView description = rootView.findViewById(R.id.description);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        title.setText(headerTitle);
        description.setText(headerDescription);
        icon.setImageDrawable(headerIcon);

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