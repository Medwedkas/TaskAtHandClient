package com.example.myapplication.feature_tasks

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class Task(
    val uid: Int,
    val header: String,
    val priority: String,
    val creator: String,
    val creatorRole: String,
    val executor: String,
    val executorRole: String,
    val description: String,
    val deadlines: String,
    val status: String
)

@Composable
fun PriorityColor(priority: String): Color {
    return when (priority) {
        "Высокий" -> Color.Red
        "Средний" -> Color.Yellow
        "Низкий" -> Color.Green
        else -> Color.Black
    }
}

private fun navigateToTaskDescription(navController: NavController, taskId: Int) {
    navController.navigate("taskDescription/$taskId")
}

enum class TaskStatus {
    COMPLETED, IN_PROGRESS, PENDING
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksPage(navController: NavController) {
    // Состояния фильтров
    val statusFilter = mutableStateOf<TaskStatus?>(null)
    val deadlineFilter = mutableStateOf<String?>(null)
    var expanded by remember { mutableStateOf(false) }

    val tasks = remember { mutableStateListOf<Task>() }
    val isRequestCompleted = remember { mutableStateOf(false) } // Флаг выполнения запроса

    LaunchedEffect(Unit) {
        if (!isRequestCompleted.value) {
            fetchTasksFromServer(tasks, isRequestCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Мои задачи") },
                modifier = Modifier.background(color = Color(0xFF71AEB2))
            )
        },
        content = { paddingValues ->
            Surface(color = Color(0xFFC7F1E8)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF71AEB2))
                        ) {
                            items(tasks.filter { task ->
                                val statusFilterValue = statusFilter.value
                                val deadlineFilterValue = deadlineFilter.value

                                val matchesStatus = statusFilterValue?.let {
                                    task.status.equals(it.name, ignoreCase = true)
                                } ?: true

                                val matchesDeadline = deadlineFilterValue?.let {
                                    task.deadlines.toLowerCase().contains(it.toLowerCase())
                                } ?: true

                                matchesStatus && matchesDeadline
                            }) { task ->
                                Card(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(Color(0xFF71AEB2)),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        navigateToTaskDescription(navController, task.uid)
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Заголовок: ${task.header}")
                                        Text(
                                            text = "Приоритет: ${task.priority}",
                                            color = PriorityColor(task.priority)
                                        )
                                        Text(text = "Создатель: ${task.creator}")
                                        Text(text = "Роль создателя: ${task.creatorRole}")
                                        Text(text = "Исполнитель: ${task.executor}")
                                        Text(text = "Роль исполнителя: ${task.executorRole}")
                                        Text(text = "Описание: ${task.description}")
                                        Text(text = "Дедлайн: ${task.deadlines}")
                                        Text(text = "Статус: ${task.status}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private suspend fun fetchTasksFromServer(tasks: SnapshotStateList<Task>, isRequestCompleted: MutableState<Boolean>) {
    val uid = UserManager.user?.uid // Замените на фактический UID пользователя
    val json = JSONObject().apply {
        put("uid", uid)
    }
    val requestBody =
        json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("http://192.168.1.47:8080/tasks/get")
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    val response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }

    if (response.isSuccessful) {
        val responseBody = response.body?.string()
        val jsonArray = JSONArray(responseBody)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val task = Task(
                header = jsonObject.getString("header"),
                priority = jsonObject.getString("priority"),
                creator = jsonObject.getString("creator"),
                creatorRole = jsonObject.getString("creatorRole"),
                executor = jsonObject.getString("executor"),
                executorRole = jsonObject.getString("executorRole"),
                description = jsonObject.getString("description"),
                deadlines = jsonObject.getString("deadlines"),
                status = jsonObject.getString("status"),
                uid = jsonObject.getInt("uid")
            )
            tasks.add(task)
        }

        isRequestCompleted.value = true // Пометить запрос как выполненный
    } else {
        // Ошибка при получении задач
        // Здесь вы можете обновить UI, чтобы показать уведомление
    }
}