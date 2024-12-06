package com.muhaimen.arenax.uploadContent

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.dataClasses.UserData
import com.muhaimen.arenax.userFeed.commentsAdapter
import com.muhaimen.arenax.utils.Constants
import com.muhaimen.arenax.utils.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.Url
import java.io.IOException

class ViewPost : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var postCaption: TextView
    private lateinit var seeMoreButton: Button
    private lateinit var likeCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var shareCount: TextView
    private lateinit var playerView: PlayerView
    private lateinit var commentButton: ImageButton
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentsAdapter: commentsAdapter
    private lateinit var writeCommentEditText:EditText
    private lateinit var postCommentButton:ImageButton
    private lateinit var commenterProfilePicture:ImageView
    private lateinit var profilePicture:ImageView
    private lateinit var username:TextView
    private lateinit var location:TextView
    private lateinit var commenterName:String
    private lateinit var commenterPicture:String
    private var exoPlayer: ExoPlayer? = null
    private var captionText:String = ""
    private val client = OkHttpClient()
    private var isExpanded: Boolean = false
    private lateinit var post: Post
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_post)

        // Setup UI elements
        imageView = findViewById(R.id.ImageView)
        playerView = findViewById(R.id.videoPlayerView)
        postCaption = findViewById(R.id.postCaption)
        seeMoreButton = findViewById(R.id.seeMoreButton)
        likeCount = findViewById(R.id.likeCount)
        commentCount = findViewById(R.id.commentCount)
        shareCount = findViewById(R.id.shareCount)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentsRecyclerView.visibility = View.GONE
        commentButton = findViewById(R.id.commentButton)
        commenterProfilePicture = findViewById(R.id.commentProfilePicture)
        writeCommentEditText = findViewById(R.id.writeCommentEditText)
        postCommentButton = findViewById(R.id.postCommentButton)
        location = findViewById(R.id.locationTextView)
        username = findViewById(R.id.usernameTextView)
        profilePicture = findViewById(R.id.ProfilePicture)

        commentButton.setOnClickListener {
            commentsRecyclerView.visibility = if (commentsRecyclerView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val userId=FirebaseManager.getCurrentUserId()
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userDataRef: DatabaseReference = database.reference.child("userData").child(userId)

            // Query the Firebase Database for the user profile picture
            userDataRef.get().addOnSuccessListener { dataSnapshot ->
                commenterPicture = dataSnapshot.child("profilePicture").getValue(String::class.java).toString()
                commenterName = dataSnapshot.child("fullname").getValue(String::class.java).toString()
                Log.d("Firebase", "Profile picture URL: $commenterPicture")
                // Check if a profile picture URL exists
                if (commenterPicture != null) {
                    // Use Glide (or Picasso) to load the profile picture into the ImageView
                    Glide.with(this)
                        .load(commenterPicture) // Load the image from URL
                        .placeholder(R.mipmap.appicon2) // Optional: placeholder while loading
                        .circleCrop()
                        .into(commenterProfilePicture) // ImageView where you want to set the image
                } else {
                    // If no profile picture is found, set a default image
                    commenterProfilePicture.setImageResource(R.mipmap.appicon2)
                }
            }.addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firebase", "Error fetching user data", exception)
            }
        } else {
            // Handle the case where the user is not logged in
            Log.e("Firebase", "User is not logged in.")
        }
        postCommentButton.setOnClickListener {
            // Get the text from the EditText
            val commentText = writeCommentEditText.text.toString()

            // Check if the comment is not empty
            if (commentText.isNotEmpty()) {
                // Generate a random commentId (you can also use a better strategy for ID generation)
                val commentId = (0..Int.MAX_VALUE).random() // Generating a random comment ID

                // Get the current timestamp as a string
                val createdAt = System.currentTimeMillis().toString()

                // Get the user's name and profile picture URL (these would come from your user profile)


                // Create a new comment object
                val newComment = Comment(
                    commentId = commentId,
                    commentText = commentText,
                    createdAt = createdAt,
                    commenterName = commenterName,
                    commenterProfilePictureUrl = commenterPicture
                )

                // Add the new comment to the post's comments list
                val updatedCommentsList = post.commentsData?.toMutableList() ?: mutableListOf()
                updatedCommentsList.add(newComment)

                // Update the post's comment count (number of comments)
                val updatedPost = post.copy(
                    commentsData = updatedCommentsList,
                    comments = updatedCommentsList.size // Updated comments count
                )

                // Save the updated post details to the server
                savePostDetailsToServer(updatedPost)

                // Optionally clear the EditText after posting the comment
                writeCommentEditText.text.clear()

            } else {
                // Optionally show a message if the comment is empty
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }





        // Retrieve the Post object from Intent
        post = intent.getParcelableExtra("POST")!!
        post?.let {
            // Set text data to UI components
            likeCount.text = it.likes.toString()
            commentCount.text = it.comments.toString()
            shareCount.text = it.shares.toString()

            captionText = it.caption.toString()

            if (post.caption=="null"){
                postCaption.visibility = View.GONE
                postCaption.text = ""
            }
            else{
                postCaption.text = post.caption ?: ""
            }
            // Handle caption display
            if (it.caption.isNullOrEmpty()) {
                postCaption.visibility = View.GONE
                seeMoreButton.visibility = View.GONE
            } else {
                postCaption.text = if (it.caption.length > 50) {
                    it.caption.take(50) + "..."
                } else {
                    it.caption
                }
                seeMoreButton.visibility = if (it.caption.length > 50) View.VISIBLE else View.GONE
            }
            username.text = it.userFullName
            location.text = it.city + ", " + it.country

            if (post.userProfilePictureUrl != null) {
                // Use Glide (or Picasso) to load the profile picture into the ImageView
                Glide.with(this)
                    .load(post.userProfilePictureUrl) // Load the image from URL
                    .placeholder(R.mipmap.appicon2) // Optional: placeholder while loading
                    .circleCrop()
                    .into(profilePicture) // ImageView where you want to set the image
            } else {
                // If no profile picture is found, set a default image
                profilePicture.setImageResource(R.mipmap.appicon2)
            }



            seeMoreButton.setOnClickListener {
                toggleCaption(post.caption ?: "")
            }

            post.postContent?.let { mediaUrl ->
                loadMedia(mediaUrl)
            }

            it.trimmedAudioUrl?.let { audioUrl ->
                playTrimmedAudio(audioUrl)
            }
        } ?: Log.d("ViewPost", "Post data is null.")
    }

    private fun savePostDetailsToServer(post: Post) {
        val requestQueue = Volley.newRequestQueue(this)

        // Create a JSON object for the updated post
        val jsonRequest = JSONObject().apply {
            put("postId", post.postId)
            put("content", post.postContent)
            put("caption", post.caption)
            put("sponsored", post.sponsored)
            put("likes", post.likes)
            put("comments", post.comments)  // Updated comment count
            put("shares", post.shares)
            put("clicks", post.clicks)
            put("city", post.city) // City from shared preferences
            put("country", post.country) // Country from shared preferences
            put("created_at", post.createdAt)
            put("trimmed_audio_url", post.trimmedAudioUrl)

            // Add comments data (convert it to JSON array or handle as necessary)
            val commentsArray = JSONArray()
            post.commentsData?.forEach { comment ->
                val commentJson = JSONObject().apply {
                    put("comment_id", comment.commentId)
                    put("comment", comment.commentText)
                    put("created_at", comment.createdAt)
                    put("commenter_name", comment.commenterName)
                    put("commenter_profile_pic", comment.commenterProfilePictureUrl)
                }
                commentsArray.put(commentJson)
            }
            put("commentsData", commentsArray)  // Add the comments data to the request
        }

        // Create a POST request
        val postRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            "${Constants.SERVER_URL}uploads/uploadPost",
            jsonRequest,
            { response ->
                // Handle the response from the server (success)
                Toast.makeText(this, "Post and comment uploaded successfully", Toast.LENGTH_SHORT).show()

                // Optionally notify other parts of the app (e.g., refresh the UI)
                val intent = Intent("NEW_POST_ADDED")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            },
            { error ->
                // Handle error (e.g., show a toast or log the error)
                Toast.makeText(this, "Failed to upload post", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the request queue
        requestQueue.add(postRequest)
    }

    private fun loadMedia(mediaUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mediaType = getMediaType(mediaUrl)
            withContext(Dispatchers.Main) {
                if (mediaType != null) {
                    when {
                        mediaType.startsWith("image/") -> {
                            imageView.visibility = View.VISIBLE
                            playerView.visibility = View.GONE
                            setImage(mediaUrl)
                        }
                        mediaType.startsWith("video/mp4") -> {
                            imageView.visibility = View.GONE
                            playerView.visibility = View.VISIBLE
                            playVideo(mediaUrl)
                        }
                        else -> {
                            Log.e("ViewPost", "Unsupported media type: $mediaType")
                        }
                    }
                }
            }
        }
    }

    private fun toggleCaption(caption: String) {
        isExpanded = !isExpanded
        postCaption.text = if (isExpanded) {
            caption
        } else {
            caption.take(50) + "..."
        }
        seeMoreButton.text = if (isExpanded) "See Less" else "See More"
    }

    private fun setImage(imageUrl: String) {
        try {
            val uri = Uri.parse(imageUrl)
            Glide.with(this)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(imageView)
        } catch (e: Exception) {
            Log.e("ViewPost", "Exception while loading image: ${e.message}")
        }
    }

    private fun playVideo(videoPath: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            playerView.player = this
        }
    }

    private fun playTrimmedAudio(outputPath: String) {
        val audioUri = Uri.parse(outputPath)
        val audioMediaItem = MediaItem.fromUri(audioUri)
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(audioMediaItem)
            prepare()
            playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onBackPressed() {
        exoPlayer?.release()
        super.onBackPressed()
    }

    private fun getMediaType(mediaUrl: String): String? {
        // Function to fetch the media type from the URL or server
        try {
            val request = Request.Builder().url(mediaUrl).build()
            val response: Response = client.newCall(request).execute()
            return response.header("Content-Type")
        } catch (e: IOException) {
            Log.e("ViewPost", "Error fetching media type: ${e.message}")
            return null
        }
    }
}
