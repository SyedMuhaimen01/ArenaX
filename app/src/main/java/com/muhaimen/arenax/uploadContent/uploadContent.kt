package com.muhaimen.arenax.uploadContent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import org.json.JSONObject
import java.util.*

class UploadContent : AppCompatActivity() {

    private lateinit var previewImageView: ImageView
    private lateinit var captionEditText: EditText
    private lateinit var galleryButton: ImageButton
    private lateinit var cameraButton: ImageButton
    private lateinit var uploadPostButton: FloatingActionButton
    private lateinit var userData: UserData
    private var mediaUri: Uri? = null
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance() // Firebase Auth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_content)

        previewImageView = findViewById(R.id.previewImageView)
        captionEditText = findViewById(R.id.captionEditText)
        galleryButton = findViewById(R.id.galleryButton)
        cameraButton = findViewById(R.id.cameraButton)
        uploadPostButton = findViewById(R.id.uploadPostButton)

        // Gallery button action
        galleryButton.setOnClickListener {
            openGallery()
        }

        // Camera button action
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Upload post button action
        uploadPostButton.setOnClickListener {
            uploadContent()
        }
    }

    // Function to open gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                previewImageView.setImageURI(mediaUri)
            }
        }

    // Function to open camera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                previewImageView.setImageURI(mediaUri)
            }
        }

    // Function to check camera permission
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to upload content
    private fun uploadContent() {
        var caption = captionEditText.text.toString()
        if (mediaUri != null ) {
            if (caption.isBlank()) {
                caption=""
            }
            val userId = auth.currentUser?.uid // Get the current user's ID
            if (userId != null) {
                uploadToFirebaseStorage(userId, caption)
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select media and write a caption.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Function to upload media to Firebase Storage
    private fun uploadToFirebaseStorage(userId: String, caption: String) {
        val mediaRef = firebaseStorage.reference.child("uploads/${UUID.randomUUID()}")

        mediaUri?.let { uri ->
            val uploadTask = mediaRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Media uploaded successfully, send data to PostgreSQL via Volley
                    savePostDetailsToServer(userId, downloadUri.toString(), caption)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to send post details to the backend server
    private fun savePostDetailsToServer(userId: String, mediaUrl: String, caption: String) {
        userData= UserData(userId = userId)
        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JSONObject().apply {
            put("userId", userData.userId)
            put("content", mediaUrl)
            put("caption", caption)
            put("sponsored", false) // Add this if it's required by your backend
        }

        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            "http://192.168.100.6:3000/uploads/uploadPost",
            jsonRequest,
            { response ->
                // Handle success response
                Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Error uploading post: ${error.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        requestQueue.add(postRequest)
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }
}
