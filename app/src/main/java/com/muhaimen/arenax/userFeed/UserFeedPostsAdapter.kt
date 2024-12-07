package com.muhaimen.arenax.userFeed

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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


class UserFeedPostsAdapter(
    private val posts: List<Post>
) : RecyclerView.Adapter<UserFeedPostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.userfeed_posts_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val playerView: PlayerView = itemView.findViewById(R.id.videoPlayerView)
        private val profilePicture: ImageView = itemView.findViewById(R.id.ProfilePicture)
        private val tvUserName: TextView = itemView.findViewById(R.id.usernameTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        private val tvCaption: TextView = itemView.findViewById(R.id.postCaption)
        private val likesCount: TextView = itemView.findViewById(R.id.likeCount)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.commentCount)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val newCommentEditText: EditText = itemView.findViewById(R.id.writeCommentEditText)
        private val postCommentButton: ImageButton = itemView.findViewById(R.id.postCommentButton)
        private var commenterPicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
        private val client = OkHttpClient()
        private var exoPlayer: ExoPlayer? = null
        private lateinit var commenterName: String
        private var likeButton:ImageButton=itemView.findViewById(R.id.likeButton)
        private var alreadyLikedButton:ImageButton=itemView.findViewById(R.id.likeFilledButton)
        @SuppressLint("SetTextI18n")
        fun bind(post: Post) {

            val userId = FirebaseManager.getCurrentUserId()
            if (userId != null) {
                val database = FirebaseDatabase.getInstance()
                val userDataRef: DatabaseReference = database.reference.child("userData").child(userId)

                // Query the Firebase Database for the user profile picture
                userDataRef.get().addOnSuccessListener { dataSnapshot ->
                    // Safely retrieve values from Firebase snapshot
                    val picture = dataSnapshot.child("profilePicture").getValue(String::class.java)
                    val name = dataSnapshot.child("fullname").getValue(String::class.java)

                    Log.d("Firebase", "Profile picture URL: $picture")
                    Log.d("Firebase", "User Name: $name")

                    // Check if a profile picture URL exists
                    if (!picture.isNullOrEmpty()) {
                        Glide.with(commenterPicture.context)
                            .load(picture)
                            .placeholder(R.mipmap.appicon2)
                            .circleCrop()
                            .into(commenterPicture)
                    } else {
                        commenterPicture.setImageResource(R.mipmap.appicon2)
                    }
                    commenterName = name.toString()
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Error fetching user data", exception)
                }
            } else {
                Log.e("Firebase", "User is not logged in.")
            }

            postCommentButton.setOnClickListener {
                val commentText = newCommentEditText.text.toString()

                if (commentText.isNotEmpty()) {
                    val commentId = (0..Int.MAX_VALUE).random()
                    val createdAt = System.currentTimeMillis().toString()

                    val newComment = Comment(
                        commentId = commentId,
                        commentText = commentText,
                        createdAt = createdAt,
                        commenterName = commenterName,
                        commenterProfilePictureUrl = commenterPicture.toString()
                    )

                    // Send only the new comment with the post ID
                    saveCommentToServer(post.postId.toString(), newComment)

                    // Optionally update the local comments list for UI
                    val updatedCommentsList = post.commentsData?.toMutableList() ?: mutableListOf()
                    updatedCommentsList.add(newComment)
                    post.commentsData = updatedCommentsList

                    // Clear the input field
                    newCommentEditText.text.clear()
                }
            }

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
                savePostLikeOnServer(post)
            }

            alreadyLikedButton.setOnClickListener {
                // Update the UI when the user removes the like
                alreadyLikedButton.visibility = View.GONE
                likeButton.visibility = View.VISIBLE

                if (post.likes == 0) {
                    likesCount.text = "0"
                } else {
                    likesCount.text = (post.likes - 1).toString()
                }

                // Remove the like on the server
                deletePostLikeOnServer(post)
                Log.d("already liked clicked", "already liked clicked")
            }



            Glide.with(itemView.context)
                .load(post.userProfilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)

            Log.d("UserFeed post adapter", "Post: $post")

            tvUserName.text = post.userFullName
            if (post.caption == "null") {
                tvCaption.visibility = View.GONE
                tvCaption.text = ""
            } else {
                tvCaption.text = post.caption ?: ""
            }
            locationTextView.text = "${post.city ?: ""}, ${post.country ?: ""}".trimEnd { it == ',' || it.isWhitespace() }
            likesCount.text = post.likes.toString()
            tvCommentsCount.text = post.comments.toString()

            val comments = post.commentsData ?: emptyList()
            val commentAdapter = commentsAdapter(comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(itemView.context)
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.isNestedScrollingEnabled = false



            recyclerViewComments.visibility = View.GONE
            commentButton.setOnClickListener {
                recyclerViewComments.visibility = if (recyclerViewComments.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            loadMedia(post.postContent)
        }

        private fun loadMedia(mediaUrl: String?) {
            if (mediaUrl.isNullOrEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                val mediaType = getMediaType(mediaUrl)

                withContext(Dispatchers.Main) {
                    when {
                        mediaType?.startsWith("image/") == true -> {
                            imageView.visibility = View.VISIBLE
                            playerView.visibility = View.GONE
                            setImage(mediaUrl)
                        }
                        mediaType?.startsWith("video/mp4") == true -> {
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

        private suspend fun getMediaType(mediaUrl: String): String? {
            val request = Request.Builder()
                .url(mediaUrl)
                .head()
                .build()

            return try {
                val response: Response = client.newCall(request).execute()
                response.header("Content-Type").also {
                    Log.d("ViewPost", "Media type retrieved: $it")
                }
            } catch (e: Exception) {
                Log.e("ViewPost", "Error retrieving media type: ${e.message}")
                null
            }
        }

        private fun setImage(imageUrl: String) {
            Glide.with(itemView.context)
                .load(Uri.parse(imageUrl))
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(imageView)
        }

        private fun playVideo(videoPath: String) {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
            exoPlayer = ExoPlayer.Builder(itemView.context).build().apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
            playerView.player = exoPlayer
        }

        private fun saveCommentToServer(postId: String, newComment: Comment) {
            val requestQueue = Volley.newRequestQueue(itemView.context)

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
                    // Handle success
                    val intent = Intent("NEW_COMMENT_ADDED")
                    LocalBroadcastManager.getInstance(itemView.context).sendBroadcast(intent)
                },
                { error ->
                    // Handle error
                }
            )

            requestQueue.add(postRequest)
        }

        private fun savePostLikeOnServer(post: Post)
        {
            val requestQueue = Volley.newRequestQueue(itemView.context)

            // Create a JSON object for the updated post
            val jsonRequest = JSONObject().apply {
                put("postId", post.postId)
                put("userId",FirebaseManager.getCurrentUserId() )

            }

            // Create a POST request
            val postRequest = JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                "${Constants.SERVER_URL}uploads/uploadLike",
                jsonRequest,
                { response ->
                    // Handle the response from the server (success)

                    // Optionally notify other parts of the app (e.g., refresh the UI)
                    val intent = Intent("Like added ")
                    LocalBroadcastManager.getInstance(itemView.context).sendBroadcast(intent)
                },
                { error ->
                    // Handle error (e.g., show a toast or log the error)
                }
            )

            // Add the request to the request queue
            requestQueue.add(postRequest)
        }

        private fun deletePostLikeOnServer(post: Post)
        {
            val requestQueue = Volley.newRequestQueue(itemView.context)

            // Create a JSON object for the updated post
            val jsonRequest = JSONObject().apply {
                put("postId", post.postId)
                put("userId",FirebaseManager.getCurrentUserId() )

            }

            // Create a POST request
            val postRequest = JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                "${Constants.SERVER_URL}uploads/deleteLike",
                jsonRequest,
                { response ->
                    // Handle the response from the server (success)

                    // Optionally notify other parts of the app (e.g., refresh the UI)
                    val intent = Intent("Like Removed ")
                    LocalBroadcastManager.getInstance(itemView.context).sendBroadcast(intent)
                },
                { error ->
                    // Handle error (e.g., show a toast or log the error)
                }
            )

            // Add the request to the request queue
            requestQueue.add(postRequest)
        }
    }

}
