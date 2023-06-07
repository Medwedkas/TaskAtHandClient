package com.example.myapplication.feature_messanger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


@Serializable
data class MessageResponse(
    val timestamp: String,
    val message: String,
    val sender_id: Int,
    val receiver_id: Int
)

@Serializable
data class Message(
    val timestamp: String,
    val message: String,
    val sender_id: Int,
    val receiver_id: Int
)

@Serializable
data class User(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: Int
)


fun parseMessageListFromJson(json: String?): List<Message> {
    return try {
        val responseList = Json.decodeFromString<List<Message>>(json ?: "")
        responseList
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun MessageCard(message: Message, currentUserUid: Int) {
    val alignment = if (message.sender_id == currentUserUid) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val sender = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(message.sender_id) {
        fetchUser(message.sender_id)?.let { user ->
            sender.value = user
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF3C3A3F)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                sender.value?.let { user ->
                    Text(
                        text = "От: ${user.name}",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.alpha(0.8f)
                    )
                }

                Text(
                    text = message.message,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Text(
                    text = message.timestamp,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(uid: Int, navController: NavController) {
    val messages = remember { mutableStateListOf<Message>() }
    val coroutineScope = rememberCoroutineScope()
    val inputText = remember { mutableStateOf("") }
    val recipient = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uid) {
        loadMessages(uid, messages)
        recipient.value = fetchUser(uid)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF323034)),
    ) {
        TopAppBar(
            title = {
                recipient.value?.let { user ->
                    Text(text = user.name)
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    currentUserUid = UserManager.user?.uid ?: 0
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color(0xFF3C3A3F))
        ) {
            TextField(
                value = inputText.value,
                onValueChange = { inputText.value = it },
                modifier = Modifier.weight(1f).background(Color(0xFF3C3A3F)),
                placeholder = { Text(text = "Напишите сообщение") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val newMessage = inputText.value
                    val requestBody = JSONObject().apply {
                        put("sender_id", UserManager.user?.uid ?: "")
                        put("receiver_id", uid)
                        put("message", newMessage)
                    }.toString()

                    coroutineScope.launch {
                        sendMessage(requestBody)
                        loadMessages(uid, messages)
                    }

                    inputText.value = ""
                },
                modifier = Modifier.wrapContentWidth().background(Color(0xFF3C3A3F)),
                colors = ButtonDefaults.buttonColors(Color(0xFF323034))
            ) {
                Text(text = "Отправить")
            }
        }
    }
}


private suspend fun loadMessages(uid: Int, messages: MutableList<Message>) {
    val requestBody = JSONObject().apply {
        put("sender_id", UserManager.user?.uid ?: "")
        put("receiver_id", uid)
    }.toString()

    val response = fetchMessages(requestBody)
    response?.let {
        val messageList = parseMessageListFromJson(response)
        messages.clear()
        messages.addAll(messageList)
    }
}

private suspend fun fetchMessages(requestBody: String): String? {
    val url = MainActivity.ApiConfig.BASE_URL + "message/getMessages"

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            val json = response.body?.string()
            json
        } catch (e: IOException) {
            null
        }
    }
}

private suspend fun sendMessage(requestBody: String) {
    val url = MainActivity.ApiConfig.BASE_URL + "message/send"

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            // Handle error
        }
    }
}

private suspend fun fetchUser(uid: Int): User? {
    val url = MainActivity.ApiConfig.BASE_URL + "getUser"

    val client = OkHttpClient()
    val requestBody = JSONObject().apply {
        put("uid", uid)
    }.toString()

    val request = Request.Builder()
        .url(url)
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            val json = response.body?.string()
            Json.decodeFromString<List<User>>(json ?: "").firstOrNull()
        } catch (e: IOException) {
            null
        }
    }
}
