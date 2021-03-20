---
layout: default
title: Split APK Installer
permalink: /sai/
---

<style>
    tab1 { padding-left: 4em; }
</style>

## Split APK Installer

<p style="text-align: justify;"><tab1>Another important feature available in this application is the installation of Split APK's. As the name itself suggests, this feature allows the installation of split APK's (multiple APK's bearing the same package id and signature) as well as app bundles (<b>xapk</b>, <b>apkm</b>, and <b>apks</b> bundles). Since Google promotes the distribution of app bundles than normal APK's via their app store, and not many applications are available to handle the installation of such bundles, this feature will be a good deal for Package Manager users.</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/10.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of a Split APK Installation session</p>

In order to install multiple APK's or app bundles, please follow the guidelines

1.  Click the top menu icon and select "<b>Install Split APK</b>".
2.  Package Manager will now open an in-built file picker to select the installation file(s). It is also possible to use a third-party file manager application for selecting the installation files, although with a little less flexibility (check out "<b>Settings --> File Picker</b>").
3.  In order to install an app bundle (<b>xapk</b>, <b>apkm</b>, or <b>apks</b> file), simply select the respective file and follow on-screen instructions.
4.  To install split APK's, please be sure to follow the additional requirements listed below
    1.   All the necessary split APK's should be placed in a single folder.
    2.   Upon being prompted to select the installation files, please make sure to select all the necessary split APK's to avoid possible installation failure.
    3.   If you're using a third-party file manager to pick split APK's, make sure that there are no unnecessary APK's (with a different package id) present in the above-mentioned folder and just select any one of them and follow on-screen instructions. Package Manager will handle the rest of them.
