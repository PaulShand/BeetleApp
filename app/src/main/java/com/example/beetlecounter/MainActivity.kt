package com.example.beetlecounter

import androidx.compose.ui.Alignment
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.beetlecounter.ui.theme.BeetleCounterTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BeetleCounterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImagePickerScreen()
                }
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Permission is denied. Show a message to the user explaining why the permission is needed.
            }
        }
    }
}

@Composable
fun ImagePickerScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var processedImageUri by remember { mutableStateOf<Uri?>(null) }
    var beetleCount by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ImagePicker", "Image picked: $uri")
        imageUri = uri
        uri?.let {
            val imagePath = getRealPathFromURI(context, it)
            imagePath?.let { path ->
                uploadImage(path, context) { processedUri, count ->
                    processedImageUri = processedUri
                    beetleCount = count
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            beetleCount?.let {
                Text(
                    text = "Beetles: $it",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            Button(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Text(text = "Pick Image")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(uri)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        processedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(uri)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        }
    }
}

fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
    val cursor = context.contentResolver.query(contentUri, null, null, null, null)
    return cursor?.let {
        it.moveToFirst()
        val idx = it.getColumnIndex("_data")
        val path = it.getString(idx)
        it.close()
        path
    }
}

fun uploadImage(imagePath: String, context: Context, onSuccess: (Uri?, Int?) -> Unit) {
    val client = OkHttpClient()
    val file = File(imagePath)
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
        .build()

    val request = Request.Builder()
        .url("<SERVER_IP>/upload")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("HTTP Request", "Failed to upload image", e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d("HTTP Request", "Image uploaded successfully. Response: $responseBody")

                responseBody?.let {
                    val jsonResponse = JSONObject(it)
                    val processedFileUrl = jsonResponse.getString("processed_file_url")
                    val fullUrl = "<SERVER_IP>$processedFileUrl"
                    val beetleCount = jsonResponse.getInt("beetle_count")
                    Log.d("HTTP Request", "Processed image URL: $fullUrl")

                    onSuccess(Uri.parse(fullUrl), beetleCount)
                }
            } else {
                Log.d("HTTP Request", "Failed to upload image. Response code: ${response.code}")
                Log.d("HTTP Request", "Response: ${response.body?.string()}")
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun ImagePickerScreenPreview() {
    BeetleCounterTheme {
        ImagePickerScreen()
    }
}
