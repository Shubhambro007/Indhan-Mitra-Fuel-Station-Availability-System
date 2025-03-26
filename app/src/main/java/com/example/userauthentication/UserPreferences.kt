package com.example.userauthentication

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Save user credentials
    fun saveUser_Credentials(email: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    // Retrieve user credentials
    fun getUser_Credentials(): Pair<String?, String?> {
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)
        return Pair(email, password)
    }

    // Clear user credentials
    fun clearUser_Credentials() {
        with(sharedPreferences.edit()) {
            remove("email")
            remove("password")
            apply()
        }
    }
}