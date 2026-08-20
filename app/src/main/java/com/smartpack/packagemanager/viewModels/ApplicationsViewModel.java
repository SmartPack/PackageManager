/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on July 13, 2026
 */
public class ApplicationsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> searchTrigger = new MutableLiveData<>(false);
    private final MutableLiveData<Void> reloadTrigger = new MutableLiveData<>();
    private final MutableLiveData<Void> sortTrigger = new MutableLiveData<>();

    public LiveData<Boolean> getSearchTrigger() { return searchTrigger; }

    public LiveData<Void> getReloadTrigger() {
        return reloadTrigger;
    }

    public LiveData<Void> getSortTrigger() {
        return sortTrigger;
    }

    public void triggerSearch(boolean enable) {
        searchTrigger.setValue(enable);
    }

    public void triggerReload() {
        reloadTrigger.setValue(null);
    }

    public void triggerSort() {
        sortTrigger.setValue(null);
    }

}