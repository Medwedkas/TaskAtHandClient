package com.example.myapplication.feature_messanger

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import com.example.myapplication.feature_tasks.NavigationBar
import com.example.myapplication.feature_tasks.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Chat(
    val receiverId: Int,
    val name: String,
    val post: String,
    val timestamp: String,
    val message: String
)

@Serializable
data class Users(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: String
)

val chats = mutableStateListOf<Chat>()
private fun navigateToTasks(navController: NavController) {
    if (UserManager.user?.role == 1) {
        navController.navigate("admin_tasks")

    } else {
        navController.navigate("task")
    }
}

private fun parseChatListFromJson(json: String?): List<Chat> {
    val chatList = mutableListOf<Chat>()
    try {
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val receiverId = jsonObject.getInt("receiver_id")
            val name = jsonObject.getString("name")
            val post = jsonObject.getString("post")
            val timestamp = jsonObject.getString("timestamp")
            val message = jsonObject.getString("message")
            val chat = Chat(receiverId, name, post, timestamp, message)
            chatList.add(chat)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return chatList
}

private suspend fun fetchUsersFromApi(
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

@Composable
fun ChatCard(chat: Chat, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("allMessages/${chat.receiverId}") },
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = chat.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Должность: " + chat.post, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Последние сообщение было: " + chat.timestamp,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    val chatList: MutableState<List<Chat>> = remember { mutableStateOf(emptyList()) }
    val users = remember { mutableStateListOf<User>() }
    val isUsersRequestCompleted = remember { mutableStateOf(false) }
    var userExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        chatList.value = fetchChats()
    }

    LaunchedEffect(Unit) {
        if (!isUsersRequestCompleted.value) {
            com.example.myapplication.feature_tasks.fetchUsersFromApi(
                users,
                isUsersRequestCompleted
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Сообщения") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {

                    IconButton(onClick = { userExpanded = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
                    }


                    DropdownMenu(
                        expanded = userExpanded,
                        onDismissRequest = { userExpanded = false },
                        offset = DpOffset(120.dp, (-350).dp),
                        modifier = Modifier
                            .wrapContentSize()
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                onClick = {
                                    val userUid = user.uid
                                    userExpanded = false
                                    navController.navigate("allMessages/${userUid}")
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
                }

            )
        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF323034)),
                contentPadding = PaddingValues(top = 55.dp)
            ) {
                items(chatList.value) { chat ->
                    ChatCard(chat = chat, navController = navController)
                }
            }
        }
    )
}

@Composable
fun ChatList(chats: List<Chat>, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ChatCard(chat = chat, navController = navController)
        }
    }
}


private suspend fun fetchChats(): List<Chat> = withContext(Dispatchers.IO) {
    val userId = UserManager.user?.uid ?: ""
    val url = MainActivity.ApiConfig.BASE_URL + "message/getChats"

    val requestBody = JSONObject().apply {
        put("uid", userId)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    suspendCoroutine { continuation ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val chatList = parseChatListFromJson(json)
                continuation.resume(chatList)
            }
        })
    }
}