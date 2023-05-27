package com.example.myapplication.dao

import androidx.room.Entity

// Класс Task
@Entity
data class Task(
    val uid: Int,
    val creator: Int,
    val creatorRole: Int,
    val executor: Int,
    val executorRole: Int,
    val description: String,
    val deadlines: String,
    val status: Int,
    val priority: Int,
    val header: String,
    val completeDate: String?
)

@Entity
data class User(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: Int
)

@Entity
data class Priority(
    val uid: Int,
    val name: String
)

@Entity
data class Role(
    val uid: Int,
    val post: String
)

@Entity
data class TaskStatus(
    val uid: Int,
    val name: String
)
