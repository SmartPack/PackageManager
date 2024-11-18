---
layout: default
title: User Interface
permalink: /ui/
---

<style>
    tab1 { padding-left: 4em; }
</style>

## User Interface

<p style="text-align: justify;"><tab1>The main user interface of Package Manager includes a simple, but elegant list view of applications showing the individual application's <b>icon</b>, <b>name</b> and <b>package id</b>. The individual items also include a check box on the right side and are used to initialize <a href="{{ site.github.url }}/batch/">batch processing</a> (more details are available in a later stage of this article).</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the main UI of Package Manager</p>

<p style="text-align: justify;"><tab1>Other than the main title (app name itself), the top portion of the application includes three buttons and are</tab1></p>

<ol>
    <li><b>Search</b> - Search and categorize applications with a specific name/package id).</li>
    <li><b>Sort</b> - Sort the applications by name or package id. This section also offers an option to reverse the application list order.</li>
    <li><b>Reload</b> - Reload the entire application list.</li>
</ol>

<p style="text-align: justify;"><tab1>The bottom navigation bar of the application provides quick access links to <b>Exported Apps</b>, <b>Uninstalled Apps</b> (requires Root or Shizuku), and the <b>Settings</b> menu.</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the App Info page of Package Manager</p>

<p style="text-align: justify;"><tab1>Upon clicking an individual item on the app list, Package Manager opens a new page having a number of separate tabs (depending on the selected application) that are easily scrollable to each other. The first tab will list the necessary information of the selected application and it includes</tab1></p>

<ol>
    <li><b>Version</b> - Current version</li>
    <li><b>Package id</b> - Package id (the unique application id which looks like a Java package name, such as com.example.app. This id uniquely identifies an application on the device as well as in an app store.)</li>
    <li><b>APK Path</b> - The directory in which the APK file (or split APK's) of the selected application are installed</li>
    <li><b>Data Directory</b>  - The directory in which the data files of the selected application are stored</li>
    <li><b>Native Library</b> - The directory in which the necessity libraries of the selected application are stored</li>
    <li><b>Installation Dates</b> - The dates at which the application is first installed and last updated</li>
    <li><b>Certificate</b> - some details about the app signing certificate</li>
</ol>

<p style="text-align: justify;"><tab1>Moreover, this page also shows a number of buttons scattered throughout (some of them are visible only on Root or Shizuku supported devices) and includes</tab1></p>

<ol>
    <li><b>Open</b> - Open the selected application</li>
    <li><b>Explore</b> - Extract and navigate through the contents of the selected APK file. This feature will allow the users to check out various resources and other important files that decide how the application behaves. It is also possible to export resources of the selected app (e.g. application icon) into the device storage in a few clicks</li>
    <li><b>Disable/Enable</b> - Disable the selected app if it is enabled or vice versa (root-only feature)</li>
    <li><b>Uninstall</b> - Package Manager allows uninstalling any applications including the ones with system privileges when running with root permissions. On non-rooted devices, only user applications are allowed to remove. On the other hand, Package Manager also offers some guidance to remove the system application on non-rooted devices via the Android Debug Bridge (ADB) method</li>
    <li><b>App Info</b> - Open the native settings page of the selected app</li>
    <li><b>Google Play</b> - Open the Google Play page of the app (work only if the selected application is published in Google Play)</li>
    <li><b>Export</b> - Export the individual APK file or split APK's (app bundles) into the device storage. After exporting an APK file/app bundles, Package Manager also provides an option to share the exported APK file/bundle via third-party applications</li>
    <li><b>Reset</b> - Reset the data folder of the selected application (root-only feature)</li>
</ol>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the Split APK's page of Package Manager</p>

<p style="text-align: justify;"><tab1>The second tab on this menu is only applicable for split APK's or app bundles where the app shows a complete list of individual APK's with an option to export them individually into the device storage. This feature is pretty useful as google is nowadays suggesting developers to provide their work as app bundles (not APK's). This feature, therefore, helps users to only backup the necessary APK files instead of the whole bundle. Moreover, it is also possible to <a href="{{ site.github.url }}/sai/">install</a> signed app bundles/split APK's using Package Manager (more details are available in a later stage of this article).</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/10.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the Permissions page of Package Manager</p>

<p style="text-align: justify;"><tab1>The next tab (<b>Permissions</b>) will be visible for any apps that declared at least one permission in its manifest file. On this page, Package Manager lists both the "<b>Granted</b>" as well as the '<b>Denied</b>" permissions of the selected application. This important feature gives users an easy way to check out the permissions enjoying each and every application installed on their device as it is an important privacy/security concern. In future releases, Package Manager likely offers an option to revoke dangerous permissions that are already granted by the user.</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/11.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the Operations page of Package Manager</p>

<p style="text-align: justify;"><tab1>The Operations (<b>AppOps</b>) page of Package Manager offers full (nearly) control over various operations handled by an application. However, note that manipulating some operations will affect normal the functioning of the application. As an example, disabling the "Camera" operation for an application using the camera hardware will permanently destroy its access to that hardware. Hence, these features shouldn't be used unless the user is fully aware of the consequences of his/her action. Also, please be aware that some operations are simply unchangeable.</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/12.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the Activity page of Package Manager</p>

<p style="text-align: justify;"><tab1>The next tab on this page displays all the enabled activities related to the selected application.</tab1></p>

<p style="text-align: center"><img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/13.jpg" alt="" width="250" height="500" /></p>
<p style="text-align: center">Screenshot of the Manifest page of Package Manager</p>

<p style="text-align: justify;"><tab1>Finally, the last tab on this page displays the manifest file (AndroidManifest.xml) of the selected application.</tab1></p>
