/*
 * Copyright (C) 2019-2020 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Smart Flasher, which is a simple app aimed to make flashing
 * recovery zip files much easier. Significant amount of code for this app has been from
 * Kernel Adiutor by Willi Ye <williye97@gmail.com>.
 *
 * Smart Flasher is a free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Smart Flasher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Smart Flasher. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.smartpack.packagemanager.utils.root;

import androidx.annotation.NonNull;

import com.smartpack.packagemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 24, 2019
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class RootFile {

    private final String mFile;
    private RootUtils.SU mSU;

    public RootFile(String file) {
        mFile = file;
        mSU = RootUtils.getSU();
    }

    public RootFile(String file, RootUtils.SU su) {
        mFile = file;
        mSU = su;
    }

    public String getName() {
        return new File(mFile).getName();
    }

    public void mkdir() {
        mSU.runCommand("mkdir -p '" + mFile + "'");
    }

    public List<String> list() {
        List<String> list = new ArrayList<>();
        String files = mSU.runCommand("ls '" + mFile + "/'");
        if (files != null) {
            // Make sure the files exists
            for (String file : files.split("\\r?\\n")) {
                if (file != null && !file.isEmpty() && Utils.existFile(mFile + "/" + file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public boolean exists() {
        String output = mSU.runCommand("[ -e " + mFile + " ] && echo true");
        return output != null && output.equals("true");
    }

    public String readFile() {
        return mSU.runCommand("cat '" + mFile + "'");
    }

    @Override
    @NonNull
    public String toString() {
        return mFile;
    }
}