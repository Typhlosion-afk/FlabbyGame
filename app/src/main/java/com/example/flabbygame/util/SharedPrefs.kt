package com.example.flabbygame.util

import android.content.Context
import android.content.SharedPreferences

@Suppress("UNCHECKED_CAST")
object SharedPrefs {
    private lateinit var mSharedPreferences: SharedPreferences

    fun init(context: Context) {
        mSharedPreferences = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
    }

    operator fun <T> get(key: String?, anonymousClass: Class<T>): T? {
        when (anonymousClass) {
            String::class.java -> {
                return mSharedPreferences.getString(key, "") as T?
            }
            Boolean::class.java -> {
                return java.lang.Boolean.valueOf(mSharedPreferences.getBoolean(key, false)) as T
            }
            Float::class.java -> {
                return java.lang.Float.valueOf(mSharedPreferences.getFloat(key, 0f)) as T
            }
            Int::class.java -> {
                return Integer.valueOf(mSharedPreferences.getInt(key, 0)) as T
            }
            Long::class.java -> {
                return java.lang.Long.valueOf(mSharedPreferences.getLong(key, 0)) as T
            }
            else -> return null
        }
    }

    fun <T> put(key: String?, data: T) {
        val editor = mSharedPreferences.edit()
        when (data) {
            is String -> {
                editor.putString(key, data as String)
            }
            is Boolean -> {
                editor.putBoolean(key, (data as Boolean))
                LogUtils.d("save to sharedPref remember value: $data")
            }
            is Float -> {
                editor.putFloat(key, (data as Float))
            }
            is Int -> {
                editor.putInt(key, (data as Int))
            }
            is Long -> {
                editor.putLong(key, (data as Long))
            }
        }
        editor.apply()
    }

}