package com.smartpack.packagemanager.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.packagemanager.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on June 03, 2026
 */
public abstract class UninstallerDialog extends BottomSheetDialog {

    public UninstallerDialog(Drawable icon, String appName, String packageName, String dialogText, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_dialog_uninstaller, null);
        AppCompatImageButton appIcon = rootView.findViewById(R.id.app_icon);
        MaterialButton cancel = rootView.findViewById(R.id.cancel);
        MaterialButton yes = rootView.findViewById(R.id.yes);
        MaterialTextView appNameTxt = rootView.findViewById(R.id.app_name);
        MaterialTextView packageNameTxt = rootView.findViewById(R.id.package_name);
        MaterialTextView text = rootView.findViewById(R.id.text);

        String titleTxt = dialogText + "\n\n" + context.getString(R.string.uninstall_warning);
        text.setText(titleTxt);
        appIcon.setImageDrawable(icon);
        appNameTxt.setText(appName);
        packageNameTxt.setText(packageName);

        cancel.setOnClickListener(v -> dismiss());

        yes.setOnClickListener(v -> {
            onUninstall();
            dismiss();
        });

        setContentView(rootView);
        show();
    }

    public abstract void onUninstall();

}