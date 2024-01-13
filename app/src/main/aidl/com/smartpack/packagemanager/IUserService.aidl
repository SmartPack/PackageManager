package com.smartpack.packagemanager;

interface IUserService {

    void destroy() = 16777114; // Destroy method defined by Shizuku server

    String runShellCommand(String command) = 0;
}