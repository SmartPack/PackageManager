package com.smartpack.packagemanager.utils

import java.io.Serializable

/*
 * Created by Lennoard <lennoardrai@gmail.com> on Mar 14, 2021
 */
data class AppOperation( // Classes are cheap!
    val name: String,
    val description: String,
    val writable: Boolean,
    var enabled: Boolean
): Serializable
