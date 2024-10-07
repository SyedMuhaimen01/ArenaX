package com.muhaimen.arenax.uploadStory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.UserData
import org.json.JSONObject
import java.util.*

class uploadStory : AppCompatActivity() {

    private lateinit var storyPreviewImageView: ImageView
    private lateinit var storyTextInput: EditText
    private lateinit var drawPenButton: ImageButton
    private lateinit var eraserButton: ImageButton
    private lateinit var storyGalleryButton: ImageButton
    private lateinit var storyCameraButton: ImageButton
    private lateinit var uploadStoryButton: FloatingActionButton
    private lateinit var userData: UserData
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)

        storyPreviewImageView = findViewById(R.id.storyPreviewImageView)
        storyTextInput = findViewById(R.id.storyTextInput)
        drawPenButton = findViewById(R.id.drawPenButton)
        eraserButton = findViewById(R.id.eraserButton)
        storyGalleryButton = findViewById(R.id.storyGalleryButton)
        storyCameraButton = findViewById(R.id.storyCameraButton)
        uploadStoryButton = findViewById(R.id.uploadStoryButton)

        // Button for selecting an image from the gallery
        storyGalleryButton.setOnClickListener {
            openGallery()
        }

        // Button for capturing an image using the camera
        storyCameraButton.setOnClickListener {
            captureImage()
        }

        // Button for uploading the story
        uploadStoryButton.setOnClickListener {

            uploadStory()
        }

        // Add functionality for drawing tools (pen, eraser)
        drawPenButton.setOnClickListener {
            // Code for enabling the pen tool
        }

        eraserButton.setOnClickListener {
            // Code for enabling the eraser tool
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                storyPreviewImageView.setImageURI(selectedImageUri)
            }
        }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                storyPreviewImageView.setImageBitmap(bitmap)
            }
        }

    private fun uploadStory() {
        val storyText = storyTextInput.text.toString()
        if (selectedImageUri != null) {
            val userId = auth.currentUser?.uid
            userData= UserData(userId = userId.toString())

            val mediaUrl = selectedImageUri.toString() // You might want to upload this to a server first
            val duration = 24 * 60 * 60 // Duration in seconds (24 hours)

            // Create JSON object for the story
            val storyJson = JSONObject().apply {
                put("user_id", userData.userId)
                put("media_url", mediaUrl)
                put("caption", storyText)
                put("duration", duration)
                put("created_at", System.currentTimeMillis())
            }

            // Send data to the backend
            saveStoryToServer(storyJson)
        } else {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStoryToServer(storyJson: JSONObject) {
        val requestQueue = Volley.newRequestQueue(this)

        val postRequest = JsonObjectRequest(
            Request.Method.POST,
            "http://192.168.100.6:3000/stories/uploadStory", // Your backend endpoint
            storyJson,
            { response ->
                // Handle success response
                Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // Handle error
                Toast.makeText(this, "Error uploading story: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(postRequest)
    }
}
