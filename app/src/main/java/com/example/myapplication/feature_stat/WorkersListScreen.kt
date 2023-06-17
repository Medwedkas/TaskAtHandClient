package com.example.myapplication.feature_stat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import com.example.myapplication.feature_messanger.Chat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class UserListItem(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: String
)

private fun navigateToStat(uid: Int, navController: NavController) {
    navController.navigate("user_stat/$uid")
}


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersListPage(navController: NavController) {

    var users by remember { mutableStateOf<List<UserListItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val userList = fetchUsers()
        users = userList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF323034))
    ) {
        TopAppBar(
            title = {
                Text(text = "Список сотрудников")
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display users as a list of cards
        Column(modifier = Modifier.padding(16.dp)) {
            for (user in users) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF323034))
                        .padding(bottom = 16.dp)
                        .clickable {
                            navigateToStat(user.uid, navController)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = user.name, color = Color(0xFF323034))
                        Text(text = user.role, color = Color(0xFF323034))
                        Text(text = user.status, color = Color(0xFF323034))
                    }
                }
            }
        }
    }
}

suspend fun fetchUsers(): List<UserListItem> = withContext(Dispatchers.IO) {
    try {
        val url = URL(MainActivity.ApiConfig.BASE_URL + "tasks/getAllUsers")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseJson = connection.inputStream.bufferedReader().use { it.readText() }
            val users = Json.decodeFromString<List<UserListItem>>(responseJson)
            users
        } else {
            // Handle unsuccessful response here
            emptyList()
        }
    } catch (e: Exception) {
        // Handle exceptions here
        emptyList()
    }
}