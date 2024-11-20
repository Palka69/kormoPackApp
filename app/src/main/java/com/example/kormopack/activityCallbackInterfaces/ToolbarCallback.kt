package com.example.kormopack.activityCallbackInterfaces

interface ToolbarCallback {
    fun showToolbar()
    fun hideToolbar()
    fun renameToolbar(string: String)
    fun changeToolbarColor(num: Int)
}