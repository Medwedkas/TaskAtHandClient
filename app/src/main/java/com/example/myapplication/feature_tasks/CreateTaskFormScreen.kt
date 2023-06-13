package com.example.myapplication.feature_tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class CreateTaskData(
    val creator: Int,
    val creatorRole: Int,
    val executor: String,
    val description: String,
    val deadlines: String,
    val status: String,
    val header: String,
    val priority: String
)

@Serializable
data class User(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: String
)

@Serializable
data class Priority(
    val uid: Int,
    val name: String
)


@Serializable
data class Status(
    val name: String
)

suspend fun fetchUsersFromApi(
    users: MutableList<User>,
    isRequestCompleted: MutableState<Boolean>
) {
    withContext(Dispatchers.IO) {
        try {
            val url = URL(MainActivity.ApiConfig.BASE_URL + "tasks/getAllUsers")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                bufferedReader.close()

                // Десериализация JSON в список пользователей
                val fetchedUsers = Json.decodeFromString<List<User>>(response.toString())
                users.addAll(fetchedUsers)
                isRequestCompleted.value = true
            } else {
                // Обработка ошибки в запросе
                println("Request Error: $responseCode - ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            // Обработка других ошибок
            println("Error: ${e.message}")
        }
    }
}

private fun sendTaskData(taskData: CreateTaskData) {
    GlobalScope.launch(Dispatchers.IO) {
        val url = URL(MainActivity.ApiConfig.BASE_URL + "tasks/createTask")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val requestBody = Json.encodeToString(taskData) // Сериализация в JSON
        println(requestBody)

        val outputStream = OutputStreamWriter(connection.outputStream)
        outputStream.write(requestBody)
        outputStream.flush()

        val responseCode = connection.responseCode
        outputStream.close()
        connection.disconnect()
    }
}

suspend fun fetchTaskStatusListFromApi(isRequestCompleted: MutableState<Boolean>): MutableList<String> {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(MainActivity.ApiConfig.BASE_URL + "tasks/getAllTasksStatus")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Accept", "application/json")

            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()

            var line: String?
            while (inputStream.readLine().also { line = it } != null) {
                response.append(line)
            }
            inputStream.close()
            connection.disconnect()

            val taskStatuses: List<Status> = Json.decodeFromString(response.toString())
            val statusList: MutableList<String> = taskStatuses.map { it.name }.toMutableList()
            isRequestCompleted.value = true
            statusList
        }
    } catch (e: Exception) {
        isRequestCompleted.value = true
        mutableListOf()
    }
}


suspend fun fetchTaskPriorityListFromApi(isRequestCompleted: MutableState<Boolean>): MutableList<String> {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(MainActivity.ApiConfig.BASE_URL + "tasks/getAllPrioritys")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Accept", "application/json")

            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()

            var line: String?
            while (inputStream.readLine().also { line = it } != null) {
                response.append(line)
            }
            inputStream.close()
            connection.disconnect()

            val priorities: List<Priority> = Json.decodeFromString(response.toString())
            val priorityList: MutableList<String> = priorities.map { it.name }.toMutableList()
            isRequestCompleted.value = true
            priorityList
        }
    } catch (e: Exception) {
        isRequestCompleted.value = true
        mutableListOf()
    }
}


//TaskForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(navController: NavController) {
    var executor by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadlines by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var header by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }

    val users = remember { mutableStateListOf<User>() }
    val isUsersRequestCompleted = remember { mutableStateOf(false) }
    var userExpanded by remember { mutableStateOf(false) }


    var selectedStatus by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("") }

    val isTaskStatusRequestCompleted = remember { mutableStateOf(false) }
    val taskStatusList = remember { mutableStateListOf<String>() }

    val isTaskPriorityRequestCompleted = remember { mutableStateOf(false) }
    val taskPriorityList = remember { mutableStateListOf<String>() }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (!isUsersRequestCompleted.value) {
            fetchUsersFromApi(users, isUsersRequestCompleted)
        }
    }

    LaunchedEffect(Unit) {
        if (!isTaskStatusRequestCompleted.value) {
            val statusList = fetchTaskStatusListFromApi(isTaskStatusRequestCompleted)
            taskStatusList.addAll(statusList)
        }
    }

    LaunchedEffect(Unit) {
        if (!isTaskPriorityRequestCompleted.value) {
            val priorityList = fetchTaskPriorityListFromApi(isTaskPriorityRequestCompleted)
            taskPriorityList.addAll(priorityList)
        }
    }

    Scaffold(
        modifier = Modifier
            .background(color = Color(0xFF29272B))
            .fillMaxHeight()
            .fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Создание задачи")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .background(color = Color(0xFF29272B))
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = executor,
                    onValueChange = { executor = it },
                    label = { Text("Исполнитель") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { userExpanded = true }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open Dropdown")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                DropdownMenu(
                    expanded = userExpanded,
                    onDismissRequest = { userExpanded = false },
                    offset = DpOffset(120.dp, (-350).dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.CenterHorizontally)
                ) {
                    users.forEach { user ->
                        DropdownMenuItem(
                            onClick = {
                                executor = user.name
                                userExpanded = false
                            },
                            text = {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontSize = 16.sp)) {
                                            append(user.name)
                                        }
                                        withStyle(
                                            style = SpanStyle(
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        ) {
                                            append("\n${user.role}")
                                        }
                                    }
                                )
                            },
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = deadlines,
                    onValueChange = { deadlines = it },
                    label = { Text("Сроки") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = { selectedStatus = it },
                    label = { Text("Статус") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { statusExpanded = true }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open Dropdown")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    offset = DpOffset(272.dp, (-463).dp),
                    modifier = Modifier
                        .wrapContentHeight()
                        .width(IntrinsicSize.Max)
                        .align(Alignment.CenterHorizontally)
                ) {
                    taskStatusList.forEach { status ->
                        DropdownMenuItem(
                            onClick = {
                                selectedStatus = status
                                statusExpanded = false
                            },
                            text = { Text(text = status) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = { selectedPriority = it },
                    label = { Text("Приоритет") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF717379), shape = MaterialTheme.shapes.small),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { priorityExpanded = true }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open Dropdown")
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color(0xFF717379),
                        focusedBorderColor = Color(0xFF717379),
                        unfocusedBorderColor = Color(0xFF717379),
                        focusedLabelColor = Color(0xFF717379),
                        cursorColor = Color(0xFF717379)
                    )
                )

                DropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                    offset = DpOffset(272.dp, (-305).dp),
                    modifier = Modifier
                        .wrapContentHeight()
                        .width(IntrinsicSize.Max)
                        .align(Alignment.CenterHorizontally)
                ) {
                    taskPriorityList.forEach { priority ->
                        DropdownMenuItem(
                            onClick = {
                                selectedPriority = priority
                                priorityExpanded = false
                            },
                            text = { Text(priority) }
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val taskData = CreateTaskData(
                            creator = UserManager.user!!.uid,
                            creatorRole = UserManager.user!!.role,
                            executor = executor,
                            description = description,
                            deadlines = deadlines,
                            status = selectedStatus,
                            header = header,
                            priority = selectedPriority
                        )

                        sendTaskData(taskData)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    Text("Создать задачу")
                }
            }
        }
    )
}
