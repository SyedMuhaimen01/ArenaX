package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

import BottomSheetDialogFragment
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.userFeed.commentsAdapter
import com.muhaimen.arenax.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.dataClasses.OrganizationData
import com.muhaimen.arenax.userFeed.UserFeedPostsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject

class pagePostsAdapter(
    private val fragmentManager: FragmentManager,
    private val recyclerView: RecyclerView,
    private val posts: List<pagePost>,
    private val organizationName: String // Pass organization name to adapter
) : RecyclerView.Adapter<pagePostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_post_item, parent, false)
        return PostViewHolder(view)
    }
    override fun onViewRecycled(holder: PostViewHolder) {
        holder.releaseContent()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, organizationName) // Pass organizationName to bind method
    }

    override fun getItemCount(): Int = posts.size

    private val playerList = mutableListOf<ExoPlayer>()

    fun addPlayer(player: ExoPlayer) {
        playerList.add(player)
    }

    fun releaseAllPlayers() {
        playerList.forEach { player ->
            player.stop()
            player.release()
        }
        playerList.clear()
    }

    fun handlePlayerVisibility() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        if (layoutManager != null) {
            val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

            for (i in 0 until itemCount) {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? pagePostsAdapter.PostViewHolder
                if (viewHolder != null) {
                    if (i in firstVisible..lastVisible) {
                        viewHolder.playContent() // Start playing if completely visible
                    } else {
                        viewHolder.stopContent() // Stop playing if not completely visible
                    }
                }
            }
        }
    }



    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val requestQueue = Volley.newRequestQueue(itemView.context)
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val playerView: PlayerView = itemView.findViewById(R.id.videoPlayerView)
        private val locationTextView: TextView = itemView.findViewById(R.id.organizationLocationTextView)
        private val tvCaption: TextView = itemView.findViewById(R.id.postCaption)
        private val likesCount: TextView = itemView.findViewById(R.id.likeCount)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.commentCount)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val newCommentEditText: EditText = itemView.findViewById(R.id.writeCommentEditText)
        private val postCommentButton: ImageButton = itemView.findViewById(R.id.postCommentButton)
        private val commenterPicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
        private val organizationLogo: ImageView = itemView.findViewById(R.id.organizationLogo)
        private val organizationNameTextView: TextView = itemView.findViewById(R.id.organizationNameTextView)
        private val client = OkHttpClient()
        private var commentsBottomSheet: BottomSheetDialogFragment? = null
        var exoPlayer: ExoPlayer? = null
        private lateinit var commenterName: String
        private var likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private var alreadyLikedButton: ImageButton = itemView.findViewById(R.id.likeFilledButton)

        private val storageReference = FirebaseStorage.getInstance().reference

        fun playContent() {
            if (exoPlayer == null) {
                Log.e("PlayContent", "ExoPlayer is not initialized")
                return
            }
            exoPlayer?.playWhenReady = true
            addPlayer(exoPlayer!!)
        }

        fun releaseContent() {
            exoPlayer?.release()
            exoPlayer = null
        }

        fun stopContent() {
            exoPlayer?.playWhenReady = false
        }

        private suspend fun getMediaType(mediaUrl: String): String? {
            val request = okhttp3.Request.Builder()
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
            addPlayer(exoPlayer!!)
        }

        private fun playTrimmedAudio(outputPath: String) {
            val audioUri = Uri.parse(outputPath)
            val audioMediaItem = MediaItem.fromUri(audioUri)
            exoPlayer = ExoPlayer.Builder(itemView.context).build().apply {
                setMediaItem(audioMediaItem)
                prepare()
                playWhenReady = true
            }
            addPlayer(exoPlayer!!)
        }

        fun stopTrimmedAudio() {
            exoPlayer?.let { player ->
                player.stop()
                player.release()
                exoPlayer = null
            }
        }
        // Bind data to the views in the ViewHolder
        fun bind(post: pagePost, organizationName: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val isOrganization = post.organizationName ?: ""
            val commenterId = if (isOrganization != "") organizationName else userId
            val commenterType = if (isOrganization != "") "organization" else "user"

            organizationNameTextView.text = post.organizationName
            Glide.with(itemView.context)
                .load(post.organizationLogo)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(organizationLogo)

            locationTextView.text = listOfNotNull(post.city, post.country).joinToString(", ")
            tvCaption.text = post.caption.takeIf { it != "null" } ?: ""
            tvCaption.visibility = if (post.caption == "null") View.GONE else View.VISIBLE

            likesCount.text = post.likes.toString()
            updateLikeButtonState(post.isLikedByUser)

            tvCommentsCount.text = post.comments.toString()
            var comments = post.commentsData?.toMutableList() ?: mutableListOf()
            val commentAdapter = commentsAdapter(comments)

            locationTextView.text = listOfNotNull(post.city, post.country).joinToString(", ")
            tvCaption.text = post.caption.takeIf { it != "null" } ?: ""
            tvCaption.visibility = if (post.caption == "null") View.GONE else View.VISIBLE

            commentButton.setOnClickListener {
                // Initialize the CommentsBottomSheetFragment

                commentsBottomSheet = BottomSheetDialogFragment( comments) { newCommentText ->
                    // Handle new comment submission
                    val commentId = (0..Int.MAX_VALUE).random() // Generating a random comment ID
                    val createdAt = System.currentTimeMillis().toString()
                    val newComment = Comment(
                        commentId = commentId,
                        commentText = newCommentText,
                        createdAt = createdAt,
                        commenterName = commenterName,
                        commenterProfilePictureUrl = commenterPicture.toString()
                    )

                    // Save the new comment to the server
                    saveCommentToServer(post.postId, commenterId, commenterType, newComment, comments, commentAdapter)

                    // Update the local comments list
                    val updatedCommentsList = post.commentsData?.toMutableList() ?: mutableListOf()
                    updatedCommentsList.add(newComment)
                    comments = updatedCommentsList

                    // Update the UI (e.g., comment count)
                    tvCommentsCount.text = comments.size.toString()

                    // Notify the bottom sheet to refresh its data
                    commentsBottomSheet?.updateComments(comments)
                }

                // Show the bottom sheet
                commentsBottomSheet!!.show(fragmentManager, "CommentsBottomSheet")
            }

            likeButton.setOnClickListener {
                savePostLikeOnServer(post.postId, userId, commenterType)
            }

            alreadyLikedButton.setOnClickListener {
                deletePostLikeOnServer(post.postId, userId, commenterType)
            }

            postCommentButton.setOnClickListener {
                val commentText = newCommentEditText.text.toString().trim()
                var profilePicture = ""

                if (isOrganization.isEmpty()) {
                    profilePicture = storageReference.child("profileImages/$userId").toString()
                } else {
                    val databaseRef = FirebaseDatabase.getInstance().getReference("organizationsData")
                    val query = databaseRef.orderByChild("organizationName").equalTo(organizationName)
                    query.get().addOnSuccessListener { snapshot ->
                        for (data in snapshot.children) {
                            val organization = data.getValue(OrganizationData::class.java)
                            organization?.organizationId?.let {
                                profilePicture = storageReference.child("organizationContent/$it/organizationProfilePictures").toString()
                            }
                        }
                    }
                }

                if (commentText.isNotEmpty()) {
                    val newComment = Comment(
                        commentId = (0..Int.MAX_VALUE).random(),
                        commentText = commentText,
                        createdAt = System.currentTimeMillis().toString(),
                        commenterName = commenterId.toString(),
                        commenterProfilePictureUrl = profilePicture
                    )
                    saveCommentToServer(post.postId, commenterId, commenterType, newComment, comments, commentAdapter)
                    newCommentEditText.text.clear()
                }
            }

            loadMedia(post.postContent)
        }

        private fun savePostLikeOnServer(postId: Int, userId: String, userType: String) {
            val jsonRequest = JSONObject().apply {
                put("postId", postId)
                put("userId", userId)
                put("userType", userType)
            }

            val postRequest = JsonObjectRequest(
                Request.Method.POST,
                "${Constants.SERVER_URL}organizationPosts/uploadLike",
                jsonRequest,
                {
                    likesCount.text = (likesCount.text.toString().toInt() + 1).toString()
                    updateLikeButtonState(true)
                },
                { error ->
                    Log.e("LikeError", "Error liking post: ${error.message}")
                }
            )
            requestQueue.add(postRequest)
        }

        private fun deletePostLikeOnServer(postId: Int, userId: String, userType: String) {
            val jsonRequest = JSONObject().apply {
                put("postId", postId)
                put("userId", userId)
                put("userType", userType)
            }

            val postRequest = JsonObjectRequest(
                Request.Method.POST,
                "${Constants.SERVER_URL}organizationPosts/deleteLike",
                jsonRequest,
                {
                    likesCount.text = (likesCount.text.toString().toInt() - 1).toString()
                    updateLikeButtonState(false)
                },
                { error ->
                    Log.e("UnlikeError", "Error unliking post: ${error.message}")
                }
            )
            requestQueue.add(postRequest)
        }

        private fun updateLikeButtonState(isLiked: Boolean) {
            likeButton.visibility = if (isLiked) View.GONE else View.VISIBLE
            alreadyLikedButton.visibility = if (isLiked) View.VISIBLE else View.GONE
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



        private fun saveCommentToServer(
            postId: Int,
            commenterId: String,
            commenterType: String,
            newComment: Comment,
            comments: MutableList<Comment>,
            adapter: commentsAdapter
        ) {
            val jsonRequest = JSONObject().apply {
                put("postId", postId)
                put("commenterId", commenterId)
                put("commenterType", commenterType)
                put("comment", newComment.commentText)
            }

            val commentRequest = JsonObjectRequest(
                Request.Method.POST,
                "${Constants.SERVER_URL}organizationPosts/comment",
                jsonRequest,
                {
                    comments.add(newComment)
                    adapter.notifyItemInserted(comments.size - 1)
                },
                { error ->
                    Log.e("CommentError", "Error commenting: ${error.message}")
                }
            )
            requestQueue.add(commentRequest)
        }
    }
}
