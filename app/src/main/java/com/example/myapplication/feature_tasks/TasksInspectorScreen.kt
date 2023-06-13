package com.example.myapplication.feature_tasks

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private suspend fun fetchTasksFromServer(
    tasks: SnapshotStateList<Task>,
    isRequestCompleted: MutableState<Boolean>
) {
    val uid = UserManager.user?.uid // Замените на фактический UID пользователя
    val json = JSONObject().apply {
        put("uid", uid)
    }
    val requestBody =
        json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url(MainActivity.ApiConfig.BASE_URL + "tasks/get")
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

private fun navigateToTaskDescription(navController: NavController, taskId: Int) {
    navController.navigate("taskDescription/$taskId")
}

private fun navigateToChats(navController: NavController) {
    navController.navigate("chats")
}

private fun navigateToWorkers(navController: NavController) {
    navController.navigate("workers_list")
}

@Composable
fun NavigationInspectorBar(
    navController: NavController,
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
    onButton3Click: () -> Unit,
    onButton4Click: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onButton1Click) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Кнопка 1")
        }
        IconButton(onClick = onButton2Click) {
            Icon(
                painter = painterResource(R.drawable.taskicon),
                contentDescription = "Кнопка 2"
            )
        }
        IconButton(onClick = onButton3Click) {
            Icon(
                painter = painterResource(R.drawable.messageicon),
                contentDescription = "Кнопка 3"
            )
        }
        IconButton(onClick = onButton4Click) {
            Icon(
                painter = painterResource(R.drawable.stats),
                contentDescription = "Кнопка 4"
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksInspectorScreen(navController: NavController) {
    val tasks = remember { mutableStateListOf<Task>() }
    val isRequestCompleted = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isRequestCompleted.value) {
            fetchTasksFromServer(tasks, isRequestCompleted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Задачи") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF3C3A3F),
            ) {
                NavigationInspectorBar(
                    navController = navController,
                    onButton1Click = {},
                    onButton2Click = { /* Handle button 2 click */ },
                    onButton3Click = { navigateToChats(navController) },
                    onButton4Click = { navigateToWorkers(navController) }

                )
            }
        },
        content = { paddingValues ->
            Surface(color = Color(0xFF323034)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF29272B))
                    ) {
                        items(tasks) { task ->
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color(0xFF29272B)),
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    navigateToTaskDescription(navController, task.uid)
                                }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = task.header,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Приоритет: ${task.priority}",
                                        color = PriorityColor(task.priority)
                                    )
                                    Text(
                                        text = "Создатель: ${task.creator}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Роль создателя: ${task.creatorRole}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Исполнитель: ${task.executor}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Роль исполнителя: ${task.executorRole}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Описание: ${task.description}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Дедлайн: ${task.deadlines}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Статус: ${task.status}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}