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
import androidx.recyclerview.widget.LinearLayoutManager
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
import java.io.IOException

class ViewPost : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var postCaption: TextView
    private lateinit var seeMoreButton: Button
    private lateinit var likesCount: TextView
    private lateinit var likeButton: ImageButton
    private lateinit var alreadyLikedButton: ImageButton
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
    private lateinit var commentsList: MutableList<Comment>
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_post)
        imageView = findViewById(R.id.ImageView)
        playerView = findViewById(R.id.videoPlayerView)
        postCaption = findViewById(R.id.postCaption)
        seeMoreButton = findViewById(R.id.seeMoreButton)
        likesCount = findViewById(R.id.likeCount)
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
        likeButton = findViewById(R.id.likeButton)
        alreadyLikedButton = findViewById(R.id.likeFilledButton)
        commentsRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

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
                    Glide.with(this)
                        .load(commenterPicture)
                        .placeholder(R.mipmap.appicon2)
                        .circleCrop()
                        .into(commenterProfilePicture)
                } else {
                    // Setting default image incase of no profile picture
                    commenterProfilePicture.setImageResource(R.mipmap.appicon2)
                }
            }.addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firebase", "Error fetching user data", exception)
            }
        } else {
            Log.e("Firebase", "User is not logged in.")
        }

        postCommentButton.setOnClickListener {
            val commentText = writeCommentEditText.text.toString()
            if (commentText.isNotEmpty()) {
                val commentId = (0..Int.MAX_VALUE).random() // Generating a random comment ID
                val createdAt = System.currentTimeMillis().toString()

                val newComment = Comment(
                    commentId = commentId,
                    commentText = commentText,
                    createdAt = createdAt,
                    commenterName = commenterName,
                    commenterProfilePictureUrl = commenterPicture
                )

                saveCommentToServer(post.postId.toString(), newComment)
                val updatedCommentsList = post.commentsData?.toMutableList() ?: mutableListOf()
                updatedCommentsList.add(newComment)

                val updatedPost = post.copy(
                    commentsData = updatedCommentsList,
                    comments = updatedCommentsList.size
                )
                writeCommentEditText.text.clear()

            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Retrieve the Post object from Intent
        post = intent.getParcelableExtra("POST")!!
        Log.d("Post", "Post data: $post")
        post.let {
            // Set text data to UI components
            likesCount.text = it.likes.toString()
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
            if(post.commentsData!=null){
                commentsList = post.commentsData!!.toMutableList()
                updatePostsUI(commentsList)
            }
            else{
                commentsList = mutableListOf()
            }
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
                Glide.with(this)
                    .load(post.userProfilePictureUrl)
                    .placeholder(R.mipmap.appicon2)
                    .circleCrop()
                    .into(profilePicture)
            } else {
                // default image incase of empty PFP
                profilePicture.setImageResource(R.mipmap.appicon2)
            }

            commentButton.setOnClickListener {
                commentsRecyclerView.visibility = if (commentsRecyclerView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            seeMoreButton.setOnClickListener {
                toggleCaption(post.caption ?: "")
            }

            likesCount.text = it.likes.toString()
            post.postContent?.let { mediaUrl ->
                loadMedia(mediaUrl)
            }

            it.trimmedAudioUrl?.let { audioUrl ->
                playTrimmedAudio(audioUrl)
            }
        } ?: Log.d("ViewPost", "Post data is null.")

        if (post.isLikedByUser) {
            likeButton.visibility = View.GONE
            alreadyLikedButton.visibility = View.VISIBLE
        } else {
            likeButton.visibility = View.VISIBLE
            alreadyLikedButton.visibility = View.GONE
        }

        likeButton.setOnClickListener {
            // Update the UI when the post is liked
            likeButton.visibility = View.GONE
            alreadyLikedButton.visibility = View.VISIBLE
            likesCount.text = (post.likes + 1).toString()

            // Save the like on the server
            savePostLikeOnServer()
        }

        alreadyLikedButton.setOnClickListener {
            alreadyLikedButton.visibility = View.GONE
            likeButton.visibility = View.VISIBLE

            if (post.likes == 0) {
                likesCount.text = "0"
            } else {
                likesCount.text = (post.likes - 1).toString()
            }
            deletePostLikeOnServer()
            Log.d("already liked clicked", "already liked clicked")
        }
    }

    private fun saveCommentToServer(postId: String, newComment: Comment) {
        val requestQueue = Volley.newRequestQueue(this)
        val jsonRequest = JSONObject().apply {
            put("postId", postId)
            put("comment", JSONObject().apply {
                put("comment_id", newComment.commentId)
                put("comment", newComment.commentText)
                put("created_at", newComment.createdAt)
                put("commenter_name", newComment.commenterName)
                put("commenter_profile_pic", newComment.commenterProfilePictureUrl)
            })
        }

        val postRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            "${Constants.SERVER_URL}uploads/uploadComments",
            jsonRequest,
            { response ->
                notifyPostReload()
            },
            { error ->
                Toast.makeText(this, "Failed to upload comment", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(postRequest)
    }

    private fun loadMedia(mediaUrl: String) {
        Log.d("ViewPost", "Loading media from URL: $mediaUrl")
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
    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }

    override fun onBackPressed() {
        exoPlayer?.release()
        super.onBackPressed()
    }

    // Function to fetch the media type from the URL or server
    private fun getMediaType(mediaUrl: String): String? {
        try {
            val request = Request.Builder().url(mediaUrl).build()
            val response: Response = client.newCall(request).execute()
            return response.header("Content-Type")
        } catch (e: IOException) {
            Log.e("ViewPost", "Error fetching media type: ${e.message}")
            return null
        }
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun updatePostsUI(comments: List<Comment>) {
        commentsAdapter = commentsAdapter(comments)
        commentsRecyclerView.adapter = commentsAdapter
        commentCount.setText(commentsAdapter.itemCount.toString())

        // Create a temporary ViewHolder to measure the height of the comment card
        val adapter = commentsAdapter
        val totalHeight = adapter.itemCount.let { count ->
            // Calculate the height dynamically based on the number of items and individual item height
            val itemHeight = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp) // Or calculate dynamically
                itemHeight * count
        }
        commentsRecyclerView.layoutParams.height = totalHeight
        commentsRecyclerView.requestLayout()
    }

    private fun savePostLikeOnServer()
    {
        val requestQueue = Volley.newRequestQueue(this)
        val jsonRequest = JSONObject().apply {
            put("postId", post.postId)
            put("userId",FirebaseManager.getCurrentUserId() )
        }

        val postRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            "${Constants.SERVER_URL}uploads/uploadLike",
            jsonRequest,
            { response ->
                notifyPostReload()
            },
            { error -> }
        )
        requestQueue.add(postRequest)
    }

    private fun deletePostLikeOnServer()
    {
        val requestQueue = Volley.newRequestQueue(this)
        val jsonRequest = JSONObject().apply {
            put("postId", post.postId)
            put("userId",FirebaseManager.getCurrentUserId() )
        }

        val postRequest = JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            "${Constants.SERVER_URL}uploads/deleteLike",
            jsonRequest,
            { response ->
                notifyPostReload()
            },
            { error ->
                Toast.makeText(this, "Failed to update post likes", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(postRequest)
    }


    private fun notifyPostReload(){
        val intent = Intent("NEW_POST_ADDED ")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
