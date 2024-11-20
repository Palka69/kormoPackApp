package com.example.kormopack.activityCallbackInterfaces

import android.content.SharedPreferences

interface DrawerCallback {
    fun lockDrawer()
    fun unlockDrawer()
    fun renameDrawerUser(user: String)
    fun getSharPref(): SharedPreferences
}