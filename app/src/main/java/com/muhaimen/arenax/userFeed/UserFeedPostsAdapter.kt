package com.muhaimen.arenax.userFeed

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.muhaimen.arenax.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class DummyPost(
    val profilePictureUrl: String,
    val postContent: String,  // media type (e.g., "image/" or "video/mp4")
    val userName: String,
    val location:String,
    val contentText: String? = null, // for text post
    val comments: MutableList<String> = mutableListOf() // list of comments
)
class UserFeedPostsAdapter(
    private val dummyPosts: List<DummyPost>,
    private val onLikeClick: (DummyPost) -> Unit,
    private val onCommentClick: (DummyPost) -> Unit,
    private val onShareClick: (DummyPost) -> Unit
) : RecyclerView.Adapter<UserFeedPostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.userfeed_posts_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = dummyPosts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = dummyPosts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val playerView: PlayerView = itemView.findViewById(R.id.videoPlayerView)
        private val profilePicture: ImageView = itemView.findViewById(R.id.ProfilePicture)
        private val tvUserName: TextView = itemView.findViewById(R.id.usernameTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        private val tvContentText: TextView = itemView.findViewById(R.id.postCaption)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.commentCount)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val client = OkHttpClient()
        private var exoPlayer: ExoPlayer? = null

        fun bind(post: DummyPost) {
            // Load profile picture using Glide
            Glide.with(itemView.context)
                .load(post.profilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)

            // Set username and content text
            tvUserName.text = post.userName
            tvContentText.text = post.contentText
            locationTextView.text = post.location
            tvCommentsCount.text = post.comments.size.toString()

            // Set up the comments RecyclerView using the CommentsAdapter
            val commentAdapter = CommentsAdapter(post.comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(itemView.context)
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.isNestedScrollingEnabled = false
            // Handle Like and Share button clicks
            btnLike.setOnClickListener { onLikeClick(post) }
            btnShare.setOnClickListener { onShareClick(post) }

            // Toggle comments visibility
            recyclerViewComments.visibility = View.GONE
            commentButton.setOnClickListener {
                recyclerViewComments.visibility = if (recyclerViewComments.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            // Check content type and display accordingly
            loadMedia(post.postContent)
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
                    } else {
                        Log.e("ViewPost", "Failed to retrieve media type.")
                    }
                }
            }
        }

        private suspend fun getMediaType(mediaUrl: String): String? {
            val request = Request.Builder()
                .url(mediaUrl)
                .head() // Use HEAD to get the headers only
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
            try {
                val uri = Uri.parse(imageUrl)
                Glide.with(itemView.context)
                    .load(uri)
                    .thumbnail(0.1f) // Show a thumbnail while loading
                    .error(R.mipmap.appicon2) // Show a default error image if loading fails
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("Post Adapter", "Exception while loading image: ${e.message}")
            }
        }

        private fun playVideo(videoPath: String) {
            try {
                val mediaItem = MediaItem.fromUri(Uri.parse(videoPath))
                exoPlayer = ExoPlayer.Builder(itemView.context).build().apply {
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true // Start playback immediately
                    playerView.player = this
                }

                Log.d("ViewPost", "Attempting to play video from path: $videoPath")
            } catch (e: Exception) {
                Log.e("Post Adapter", "Exception while setting up video: ${e.message}")
            }
        }

        // Release ExoPlayer when the view is destroyed
        fun releasePlayer() {
            exoPlayer?.release()
        }
    }

    // Call releasePlayer in the Activity/Fragment when the view is destroyed
    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }
}

