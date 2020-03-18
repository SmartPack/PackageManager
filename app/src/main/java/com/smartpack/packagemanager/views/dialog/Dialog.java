package com.smartpack.packagemanager.views.dialog;

import android.content.Context;
import android.database.Cursor;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by willi on 07.11.16.
 */

public class Dialog extends AlertDialog.Builder {

    private DialogInterface.OnDismissListener mOnDismissListener;

    public Dialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public Dialog setTitle(CharSequence title) {
        return (Dialog) super.setTitle(title);
    }

    @Override
    public Dialog setTitle(int titleId) {
        return (Dialog) super.setTitle(titleId);
    }

    @Override
    public Dialog setMessage(CharSequence message) {
        return (Dialog) super.setMessage(message);
    }

    @Override
    public Dialog setMessage(int messageId) {
        return (Dialog) super.setMessage(messageId);
    }

    @Override
    public Dialog setView(int layoutResId) {
        return (Dialog) super.setView(layoutResId);
    }

    @Override
    public Dialog setView(View view) {
        return (Dialog) super.setView(view);
    }

    @Override
    public Dialog setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setItems(items, listener);
    }

    @Override
    public Dialog setItems(int itemsId, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setItems(itemsId, listener);
    }

    @Override
    public Dialog setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setPositiveButton(text, listener);
    }

    @Override
    public Dialog setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setPositiveButton(textId, listener);
    }

    @Override
    public Dialog setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setNegativeButton(text, listener);
    }

    @Override
    public Dialog setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
        return (Dialog) super.setNegativeButton(textId, listener);
    }

    @Override
    public Dialog setMultiChoiceItems(int itemsId, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener){
        return (Dialog) super.setMultiChoiceItems(itemsId, checkedItems, listener);
    }

    @Override
    public Dialog setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, DialogInterface.OnMultiChoiceClickListener listener){
        return (Dialog) super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
    }

    @Override
    public Dialog
    setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener listener){
        return (Dialog) super.setMultiChoiceItems(items, checkedItems, listener);
    }

    public Dialog setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        setOnCancelListener(dialogInterface -> {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss(dialogInterface);
            }
        });
        return this;
    }

    @Override
    public AlertDialog show() {
        try {
            AlertDialog dialog = create();
            dialog.setOnDismissListener(mOnDismissListener);
            dialog.show();
            return dialog;
        } catch (WindowManager.BadTokenException ignored) {
            return create();
        }
    }

}