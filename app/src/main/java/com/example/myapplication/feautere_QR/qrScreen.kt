import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            UserManager.user?.let { user ->
                val bitmap = generateQRCode(user.uid)
                qrCodeBitmap = bitmap
            }
        }
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(title = { Text(text = "QR вход") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                })

            qrCodeBitmap?.let { QRCodeImage(it) } ?: Text(text = "QR Code not generated")
            GenerateButton(onClick = {
                UserManager.user?.let { user ->
                    coroutineScope.launch {
                        val bitmap = generateQRCode(user.uid)
                        qrCodeBitmap = bitmap
                    }
                }
            })
        }
    }
}

@Composable
fun QRCodeImage(qrCodeBitmap: ImageBitmap) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(1f)
    ) {
        Image(
            bitmap = qrCodeBitmap,
            contentDescription = "QR Code",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun GenerateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Generate QR Code")
    }
}

private suspend fun generateQRCode(userUid: Int): ImageBitmap? = withContext(Dispatchers.Default) {
    val client = OkHttpClient()
    val mediaType = "application/json".toMediaType()
    val requestBody = """
        {
            "userUid": $userUid
        }
    """.trimIndent().toRequestBody(mediaType)

    val request = Request.Builder()
        .url(MainActivity.ApiConfig.BASE_URL + "qrLogin")
        .post(requestBody)
        .build()

    return@withContext suspendCancellableCoroutine<ImageBitmap?> { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                continuation.resume(null, onCancellation = {})
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val uid = extractUidFromResponse(responseBody)
                    if (uid != null) {
                        val qrCodeUrl = "${MainActivity.ApiConfig.BASE_URL}qrResp?uid=$uid"
                        val bitmap = runBlocking { generateQRCodeImage(qrCodeUrl) }
                        continuation.resume(bitmap, onCancellation = {})
                    } else {
                        // Handle error: Unable to extract UID from the response
                        continuation.resume(null, onCancellation = {})
                    }
                } ?: continuation.resume(null, onCancellation = {})
            }
        })
    }
}


private fun extractUidFromResponse(responseBody: String): String? {
    // Your logic to extract UID from responseBody
    // Modify the following code according to your expected format and UID representation

    // Example logic assuming responseBody contains a JSON with a "uid" field
    try {
        val json = JSONObject(responseBody)
        val uid = json.optString("uid")
        return if (uid.isNotEmpty()) {
            uid // Return the extracted UID
        } else {
            null // Return null if UID is empty or not found
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        return null // Return null in case of JSON parsing error
    }
}


private fun generateQRCodeImage(qrCodeUrl: String): ImageBitmap? {
    try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            qrCodeUrl,
            BarcodeFormat.QR_CODE,
            300, // QR code width and height
            300, // Adjust this value as per your requirement
            null
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap.asImageBitmap()
    } catch (e: WriterException) {
        e.printStackTrace()
    }
    return null
}
