package com.llw.blekotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.llw.blekotlin.BleApplication

/**
 * SharedPreferences工具类  扩展函数
 *
 * @author llw
 * @date 2021/4/14 16:16
 */
const val NAME = "config"

@SuppressLint("StaticFieldLeak")
val context = BleApplication.context

val sp: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

fun Boolean.putBoolean(key: String) = sp.edit().putBoolean(key, this).apply()

fun getBoolean(key: String, result: Boolean = false): Boolean = sp.getBoolean(key, result)

fun String?.putString(key: String) = sp.edit().putString(key, this).apply()

fun getString(key: String, result: String? = null): String? = sp.getString(key, result)

fun Int.putInt(key: String) = sp.edit().putInt(key, this).apply()

fun getInt(key: String, result: Int = 0): Int = sp.getInt(key, result)

fun Long.putLong(key: String) = sp.edit().putLong(key, this).apply()

fun getLong(key: String, result: Long = 0): Long = sp.getLong(key, result)