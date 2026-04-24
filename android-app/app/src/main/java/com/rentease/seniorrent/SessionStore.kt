package com.rentease.seniorrent

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("senior_rent_session", Context.MODE_PRIVATE)

    fun accessToken(): String? = prefs.getString("access_token", null)
    fun refreshToken(): String? = prefs.getString("refresh_token", null)
    fun role(): String? = prefs.getString("role", null)

    fun save(accessToken: String, refreshToken: String, role: String?) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("role", role)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}