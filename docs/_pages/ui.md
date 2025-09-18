---
layout: page
title: User Interface
---

<style>
    tab1 { padding-left: 4em; }
</style>

<h2 style="color: blue">🎨 User Interface</h2>

<p style="text-align: justify;">
  <tab1>
    The main user interface of Package Manager presents a simple yet elegant list view of applications, showing each application's <b>icon</b>, <b>name</b>, and <b>package ID</b>.  
    Each item also includes an icon to open the application (if available).  
    Long-pressing an item initiates <a href="{{ site.github.url }}/batch/">batch processing</a> by enabling a checkbox, instead of using the "open" icon on the right side.  
    (More details are provided later in this article.)
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" alt="Package Manager Main UI" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the main UI of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    Other than the main title (app name), the top portion of the application includes three buttons:
  </tab1>
</p>

<ol>
    <li><b>Search</b> – Search and categorize applications by name or package ID.</li>
    <li><b>Sort</b> – Sort applications by name, package ID, size, or installation/update date. This option also allows reversing the application list order.</li>
    <li><b>Reload</b> – Reload the entire application list.</li>
</ol>

<p style="text-align: justify;">
  <tab1>
    The bottom navigation bar provides quick access to <b>Exported Apps</b>, <b>Uninstalled Apps</b> (requires Root or Shizuku), and the <b>Settings</b> menu.
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" alt="Package Manager App Info Page" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the App Info page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    Upon clicking an individual item in the app list, Package Manager opens a new page with several scrollable tabs (depending on the selected application). The first tab displays essential information about the selected application, including:
  </tab1>
</p>

<ol>
    <li><b>Version</b> – Current version of the app</li>
    <li><b>Package ID</b> – The unique application identifier, resembling a Java package name (e.g., com.example.app). This ID uniquely identifies the application on the device and in app stores.</li>
    <li><b>APK Path</b> – The directory where the APK file(s) of the selected application are installed</li>
    <li><b>Data Directory</b> – The directory containing the app’s data files</li>
    <li><b>Native Library</b> – Directory storing the required native libraries for the app</li>
    <li><b>Installation Dates</b> – Dates when the application was first installed and last updated</li>
    <li><b>Certificate</b> – Details about the app’s signing certificate</li>
</ol>

<p style="text-align: justify;">
  <tab1>
    In addition, this page provides several action buttons (some visible only on Root or Shizuku-supported devices), including:
  </tab1>
</p>

<ol>
    <li><b>Open</b> – Launch the selected application</li>
    <li><b>Explore</b> – Extract and browse the contents of the selected APK. Users can inspect resources and other important files that determine the app’s behavior. It is also possible to export resources (e.g., app icon) to device storage with a few clicks</li>
    <li><b>Disable/Enable</b> – Disable an enabled app or enable a disabled app (Root or Shizuku-dependent feature)</li>
    <li><b>Uninstall</b> – Uninstall applications, including system apps, with root or Shizuku permissions. On devices without root or Shizuku support, only user apps can be removed. For such devices, guidance is provided on removing system apps via ADB.</li>
    <li><b>App Info</b> – Open the native Android settings page for the selected app</li>
    <li><b>Google Play</b> – Open the app’s page on Google Play (works only if the app is published there)</li>
    <li><b>Export</b> – Export individual APK files or app bundles to device storage. Exported files can also be shared via third-party apps</li>
    <li><b>Reset</b> – Reset the selected app’s data folder (root or Shizuku-dependent feature)</li>
</ol>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" alt="Split APKs page of Package Manager" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the Split APKs page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    The second tab is applicable only for split APKs or app bundles. It displays a complete list of individual APKs with an option to export them individually or as a batch to device storage.  
    This feature is especially useful as Google now encourages developers to distribute apps as bundles rather than single APKs. Users can selectively back up only the necessary APK files instead of the entire bundle.  
    Additionally, it is possible to <a href="{{ site.github.url }}/sai/">install</a> app bundles or split APKs using Package Manager (more details are provided later in this article).
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/10.jpg" alt="Split APKs page of Package Manager" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the Permissions page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    The next tab (<b>Permissions</b>) appears for any app that declares at least one permission in its manifest file.  
    Package Manager lists both "<b>Granted</b>" and "<b>Denied</b>" permissions for the selected application.  
    This feature provides an easy way to review the permissions each app has, which is important for privacy and security.  
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/11.jpg" alt="Permissions page of Package Manager" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the Operations page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    The Operations (<b>AppOps</b>) page offers near-complete control over various operations handled by an application.  
    Note that modifying some operations may affect the normal functioning of the app.  
    For example, disabling the "Camera" operation for an app that uses camera hardware will permanently block its access.  
    These features should be used only if the user fully understands the consequences. Some operations may also be unchangeable.
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/12.jpg" alt="Operations page of Package Manager" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the Activity page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    The next tab displays all the enabled activities related to the selected application.
  </tab1>
</p>

<p style="text-align: center">
  <img src="https://raw.githubusercontent.com/SmartPack/PackageManager/master/fastlane/metadata/android/en-US/images/phoneScreenshots/13.jpg" alt="Activity page of Package Manager" width="250" height="500" />
</p>
<p style="text-align: center"><b>Screenshot of the Manifest page of Package Manager</b></p>

<p style="text-align: justify;">
  <tab1>
    The final tab displays the manifest file (AndroidManifest.xml) of the selected application.
  </tab1>
</p>
