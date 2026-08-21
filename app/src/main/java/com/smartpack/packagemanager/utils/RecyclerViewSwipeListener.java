/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2026
 */
public abstract class RecyclerViewSwipeListener {

    private final RecyclerView.OnItemTouchListener touchListener;
    private final RecyclerView recyclerView;

    public RecyclerViewSwipeListener(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        this.touchListener = new RecyclerView.SimpleOnItemTouchListener() {
            private float startX = 0f;
            private float startY = 0f;
            private boolean actionTriggered = false;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        actionTriggered = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!actionTriggered) {
                            float deltaX = e.getX() - startX;
                            float deltaY = e.getY() - startY;
                            float absX = Math.abs(deltaX);
                            float absY = Math.abs(deltaY);

                            if (absX > ViewConfiguration.get(recyclerView.getContext()).getScaledTouchSlop() * 2 && absX > absY) {
                                int direction = (deltaX > 0) ? -1 : 1;

                                if (!rv.canScrollHorizontally(direction)) {
                                    actionTriggered = true;
                                    if (deltaX > 0) {
                                        onSwipeLeft();
                                    } else {
                                        onSwipeRight();
                                    }
                                }
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        actionTriggered = false;
                        break;
                }
                return false;
            }
        };

        this.recyclerView.addOnItemTouchListener(touchListener);
    }

    public void detach() {
        if (recyclerView != null && touchListener != null) {
            recyclerView.removeOnItemTouchListener(touchListener);
        }
    }

    public abstract void onSwipeLeft();

    public abstract void onSwipeRight();

}