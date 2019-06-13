package group14.finalproject.mytodotask.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import group14.finalproject.mytodotask.PREFERENCES_NAME

object SharedPreferencesHelper {
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences


    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE)
    }


    private fun isHasKey(key: String): Boolean {
        return preferences.contains(key)
    }


    fun saveInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }


    fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }


    fun readString(key: String): String {
        return if (isHasKey(key)) preferences.getString(key, "NO_VALUE")!!
        else "NO_CONTAIN_KEY"
    }

    fun readInt(key: String): Int {
        return if (isHasKey(key)) preferences.getInt(key, -99)
        else -88
    }

    fun readInt(key: String, defaultValue: Int): Int {
        return if (isHasKey(key)) preferences.getInt(key, defaultValue)
        else defaultValue
    }


}