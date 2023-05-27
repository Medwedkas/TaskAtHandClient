package com.example.myapplication.dao

import com.example.myapplication.dao.TaskStatus
import com.example.myapplication.dao.User
import com.example.myapplication.dao.Priority
import com.example.myapplication.dao.Role
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class BackendApi {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getAllTaskStatuses(): List<TaskStatus> {
        val request = Request.Builder()
            .url("http://192.168.1.47:8080/tasks/getAllTasksStatus")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to retrieve task statuses: ${response.code}")
            }

            val responseBody = response.body?.string()
            return gson.fromJson(responseBody, Array<TaskStatus>::class.java).toList()
        }
    }

    fun getAllUsers(): List<User> {
        val request = Request.Builder()
            .url("http://192.168.1.47:8080/tasks/getAllUsers")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to retrieve users: ${response.code}")
            }

            val responseBody = response.body?.string()
            return gson.fromJson(responseBody, Array<User>::class.java).toList()
        }
    }

    fun getAllPriorities(): List<Priority> {
        val request = Request.Builder()
            .url("http://192.168.1.47:8080/tasks/getAllPrioritys")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to retrieve priorities: ${response.code}")
            }

            val responseBody = response.body?.string()
            return gson.fromJson(responseBody, Array<Priority>::class.java).toList()
        }
    }

    fun getAllRoles(): List<Role> {
        val request = Request.Builder()
            .url("http://192.168.1.47:8080/tasks/getAllRole")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to retrieve roles: ${response.code}")
            }

            val responseBody = response.body?.string()
            return gson.fromJson(responseBody, Array<Role>::class.java).toList()
        }
    }
}
