package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// UserDao.kt
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM user WHERE uid = :userId")
    suspend fun getUserById(userId: Int): User

    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

// PriorityDao.kt
@Dao
interface PriorityDao {
    @Query("SELECT * FROM priority")
    suspend fun getAllPriorities(): List<Priority>

    @Query("SELECT * FROM priority WHERE uid = :priorityId")
    suspend fun getPriorityById(priorityId: Int): Priority

    @Insert
    suspend fun insertPriority(priority: Priority)

    @Update
    suspend fun updatePriority(priority: Priority)

    @Delete
    suspend fun deletePriority(priority: Priority)
}

// RoleDao.kt
@Dao
interface RoleDao {
    @Query("SELECT * FROM role")
    suspend fun getAllRoles(): List<Role>

    @Query("SELECT * FROM role WHERE uid = :roleId")
    suspend fun getRoleById(roleId: Int): Role

    @Insert
    suspend fun insertRole(role: Role)

    @Update
    suspend fun updateRole(role: Role)

    @Delete
    suspend fun deleteRole(role: Role)
}

// TaskStatusDao.kt
@Dao
interface TaskStatusDao {
    @Query("SELECT * FROM taskstatus")
    suspend fun getAllTaskStatuses(): List<TaskStatus>

    @Query("SELECT * FROM taskstatus WHERE uid = :taskStatusId")
    suspend fun getTaskStatusById(taskStatusId: Int): TaskStatus

    @Insert
    suspend fun insertTaskStatus(taskStatus: TaskStatus)

    @Update
    suspend fun updateTaskStatus(taskStatus: TaskStatus)

    @Delete
    suspend fun deleteTaskStatus(taskStatus: TaskStatus)
}
