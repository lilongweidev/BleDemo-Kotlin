package com.llw.blekotlin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *
 * @description BleApplication
 * @author llw
 * @date 2021/9/28 20:28
 */
open class BleApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}