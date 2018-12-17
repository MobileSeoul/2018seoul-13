package com.rollcake.indi.indiplace.utils

import android.content.Context

class AppkeyManager {

    companion object {


        fun setKet(context: Context, key: String, value: Boolean) {
            val pref = context.getSharedPreferences("AppKey", 0)
            val edit = pref.edit()
            edit.putBoolean(key, value)
            edit.commit()
        }

        fun setKet(context: Context, key: String, value: Float) {
            val pref = context.getSharedPreferences("AppKey", 0)
            val edit = pref.edit()
            edit.putFloat(key, value)
            edit.commit()
        }

        fun setKet(context: Context, key: String, value: Int) {
            val pref = context.getSharedPreferences("AppKey", 0)
            val edit = pref.edit()
            edit.putInt(key, value)
            edit.commit()
        }

        fun setKet(context: Context, key: String, value: String) {
            val pref = context.getSharedPreferences("AppKey", 0)
            val edit = pref.edit()
            edit.putString(key, value)
            edit.commit()
        }

         fun getKey(context: Context, key: String, defaultValue: String): String {
            val pref = context.getSharedPreferences("AppKey", 0)
            return pref.getString(key, defaultValue)
        }

         fun getKey(context: Context, key: String, defaultValue: Int): Int {
            val pref = context.getSharedPreferences("AppKey", 0)
            return pref.getInt(key, defaultValue)
        }

         fun getKey(context: Context, key: String, defaultValue: Float): Float {
            val pref = context.getSharedPreferences("AppKey", 0)
            return pref.getFloat(key, defaultValue)
        }

        fun getKey(context: Context, key: String, defaultValue: Boolean): Boolean{
            val pref = context.getSharedPreferences("AppKey", 0)
            return pref.getBoolean(key, defaultValue)
        }

    }

}