/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Package Manager, a simple, yet powerful application
 * to manage other application installed on an android device.
 *
 */

package com.smartpack.packagemanager.utils.root;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on February 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class RootUtils {

    private static SU su;

    public static boolean rootAccess() {
        SU su = getSU();
        su.runCommand("echo /testRoot/");
        return !su.mDenied;
    }

    public static String runCommand(String command) {
        return getSU().runCommand(command);
    }

    public static SU getSU() {
        if (su == null || su.mClosed || su.mDenied) {
            if (su != null && !su.mClosed) {
                su.close();
            }
            su = new SU();
        }
        return su;
    }

    /*
     * Based on AndreiLux's SU code in Synapse
     * https://github.com/AndreiLux/Synapse/blob/master/src/main/java/com/af/synapse/utils/Utils.java#L238
     */
    public static class SU {

        private Process mProcess;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
        private final boolean mRoot;
        private final String mTag;
        private boolean mClosed;
        public boolean mDenied;
        private boolean mFirstTry;

        public SU() {
            this(true, null);
        }

        public SU(boolean root, String tag) {
            mRoot = root;
            mTag = tag;
            try {
                if (mTag != null) {
                    Log.i(mTag, String.format("%s initialized", root ? "SU" : "SH"));
                }
                mFirstTry = true;
                mProcess = Runtime.getRuntime().exec(root ? "su" : "sh");
                mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            } catch (IOException e) {
                if (mTag != null) {
                    Log.e(mTag, root ? "Failed to run shell as su" : "Failed to run shell as sh");
                }
                mDenied = true;
                mClosed = true;
            }
        }

        public synchronized String runCommand(final String command) {
            synchronized (this) {
                try {
                    StringBuilder sb = new StringBuilder();
                    String callback = "/shellCallback/";
                    mWriter.write(command + "\necho " + callback + "\n");
                    mWriter.flush();

                    int i;
                    char[] buffer = new char[256];
                    while (true) {
                        sb.append(buffer, 0, mReader.read(buffer));
                        if ((i = sb.indexOf(callback)) > -1) {
                            sb.delete(i, i + callback.length());
                            break;
                        }
                    }
                    mFirstTry = false;
                    if (mTag != null) {
                        Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                    }

                    return sb.toString().trim();
                } catch (IOException e) {
                    mClosed = true;
                    e.printStackTrace();
                    if (mFirstTry) mDenied = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    mDenied = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    mDenied = true;
                }
                return null;
            }
        }

        public void close() {
            try {
                if (mWriter != null) {
                    mWriter.write("exit\n");
                    mWriter.flush();

                    mWriter.close();
                }
                if (mReader != null) {
                    mReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mProcess != null) {
                try {
                    mProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mProcess.destroy();
                if (mTag != null) {
                    Log.i(mTag, String.format("%s mClosed: %d", mRoot ? "SU" : "SH", mProcess.exitValue()));
                }
            }
            mClosed = true;
        }

    }

}
