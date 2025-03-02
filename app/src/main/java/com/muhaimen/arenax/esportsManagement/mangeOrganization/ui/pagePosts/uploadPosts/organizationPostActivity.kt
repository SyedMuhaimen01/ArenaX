package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.muhaimen.arenax.R
import com.muhaimen.arenax.uploadContent.UploadContent.Companion.CAMERA_PERMISSION_REQUEST_CODE

class organizationPostActivity : AppCompatActivity() {

    private lateinit var cameraButton: TextView
    private lateinit var galleryButton: TextView
    private lateinit var postButton: FloatingActionButton
    private lateinit var articleButton: TextView
    private lateinit var previewImageView: ImageView
    private lateinit var articleTextView: EditText
    private lateinit var captionEditText: EditText
    private lateinit var backButton: ImageButton
    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_organization_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor=resources.getColor(R.color.primaryColor)

        initializeViews()
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        galleryButton.setOnClickListener {
            openGallery()
        }

        articleButton.setOnClickListener {
            if (articleTextView.visibility == View.VISIBLE) {
                articleTextView.visibility = View.GONE
                previewImageView.visibility = View.VISIBLE
                captionEditText.visibility = View.VISIBLE

            } else {
                articleTextView.visibility = View.VISIBLE
                previewImageView.visibility = View.GONE
                captionEditText.visibility = View.GONE
            }
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
        postButton.setOnClickListener {
            // Upload the post
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaUri = result.data?.data
                previewImageView.setImageURI(mediaUri)
                previewImageView.clipToOutline = true
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
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // Finish the activity only if the user confirms
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun initializeViews(){
        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        articleButton = findViewById(R.id.articleButton)
        postButton = findViewById(R.id.uploadPostButton)
        previewImageView = findViewById(R.id.previewImageView)
        articleTextView = findViewById(R.id.articleEditText)
        captionEditText = findViewById(R.id.captionEditText)
        backButton = findViewById(R.id.backButton)
    }

}