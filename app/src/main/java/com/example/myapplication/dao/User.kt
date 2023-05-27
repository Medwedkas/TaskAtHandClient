package com.example.myapplication

data class User(
    val uid: Int,
    val name: String,
    var role: Int,
)

class UserManager {
    companion object {
        var user: User? = null
    }
}
