package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts.uploadPosts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class organizationPostActivity : AppCompatActivity() {

    private lateinit var cameraButton: TextView
    private lateinit var galleryButton: TextView
    private lateinit var postButton: FloatingActionButton
    private lateinit var articleButton: TextView
    private lateinit var previewImageView: ImageView
    private lateinit var articleTextView: EditText
    private lateinit var captionEditText: EditText
    private lateinit var backButton: ImageButton
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private var mediaUri: Uri? = null
    private lateinit var organizationName: String
    private var organizationLogo: String? = null
    private var city: String? = null
    private var country: String? = null

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
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        initializeViews()

        firebaseDatabase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid.toString()

        organizationName = intent.getStringExtra("organization_name") ?: ""
        Log.d("organizationName", organizationName)
        // Load location data
        loadLocationFromSharedPreferences()

        // Fetch organization details
        fetchOrganizationData()

        cameraButton.setOnClickListener {
            if (checkCameraPermission()) openCamera() else requestCameraPermission()
        }
        galleryButton.setOnClickListener { openGallery() }

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

        backButton.setOnClickListener { onBackPressed() }
        postButton.setOnClickListener { uploadPost() }
    }

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

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun initializeViews() {
        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        articleButton = findViewById(R.id.articleButton)
        postButton = findViewById(R.id.uploadPostButton)
        previewImageView = findViewById(R.id.previewImageView)
        articleTextView = findViewById(R.id.articleEditText)
        captionEditText = findViewById(R.id.captionEditText)
        backButton = findViewById(R.id.backButton)
    }

    private fun uploadPost() {
        val caption = captionEditText.text.toString().trim()
        val article = articleTextView.text.toString().trim()
        val mediaUrl = mediaUri?.toString() ?: ""
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val requestBody = JSONObject().apply {
            put("userId",userId)
            put("postContent", if (mediaUrl.isNotEmpty()) mediaUrl else article)
            put("caption", caption.ifEmpty { "" })
            put("sponsored", false)
            put("likes", 0)
            put("comments", 0)
            put("shares", 0)
            put("clicks", 0)
            put("city", city ?: "Unknown")
            put("country", country ?: "Unknown")
            put("createdAt", createdAt)
            put("organizationName", organizationName)
            put("organizationLogo", organizationLogo ?: "Default_Logo_URL")
            put("commentsData", JSONArray())
            put("isLikedByUser", false)
        }

        val requestQueue = Volley.newRequestQueue(this)
        val url = "${Constants.SERVER_URL}organizationPosts/createPost"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_LONG).show() },
            { error -> Toast.makeText(this, "Failed to upload post!", Toast.LENGTH_LONG).show() }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun fetchOrganizationData() {
        val url = "${Constants.SERVER_URL}registerOrganization/basicOrganizationData"
        val requestQueue = Volley.newRequestQueue(this)

        val requestBody = JSONObject().apply {
            put("organization_name", organizationName)
        }

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                organizationLogo = response.optString("organization_logo", null)
                organizationName = response.optString("organization_name", "")
                val organizationLocation = response.optString("organization_location", null)

            },
            { error ->
                Log.e("VolleyError", "Failed to fetch organization data: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun loadLocationFromSharedPreferences() {
        val sharedPreferences by lazy { getSharedPreferences("UserLocationPrefs", Context.MODE_PRIVATE) }
        city = sharedPreferences.getString("city", "Unknown")
        country = sharedPreferences.getString("country", "Unknown")
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
