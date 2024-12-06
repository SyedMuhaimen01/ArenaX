package com.muhaimen.arenax.userFeed

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class UserFeedPostsAdapter(
    private val posts: List<Post>,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit
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
        private val tvLikes: TextView = itemView.findViewById(R.id.likeCount)
        private val tvCommentsCount: TextView = itemView.findViewById(R.id.commentCount)
        private val btnLike: ImageButton = itemView.findViewById(R.id.likeButton)
        private val btnShare: ImageButton = itemView.findViewById(R.id.shareButton)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val recyclerViewComments: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val newComment:EditText=itemView.findViewById(R.id.writeCommentEditText)
        private val postComment:ImageButton=itemView.findViewById(R.id.postCommentButton)
        private val commentProfilePicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
        private val client = OkHttpClient()
        private var exoPlayer: ExoPlayer? = null

        fun bind(post: Post) {
            // Load profile picture using Glide
            Glide.with(itemView.context)
                .load(post.userProfilePictureUrl)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(profilePicture)

            // Set username, caption, location, likes, and comments count
            tvUserName.text = post.userFullName
            if (post.caption=="null"){
                tvCaption.visibility = View.GONE
                tvCaption.text = ""
            }
            else{
                tvCaption.text = post.caption ?: ""
            }
            locationTextView.text = "${post.city ?: ""}, ${post.country ?: ""}".trimEnd { it == ',' || it.isWhitespace() }
            tvLikes.text = post.likes.toString()
            tvCommentsCount.text = post.comments.toString()

            // Set up the comments RecyclerView
            val comments = post.commentsData ?: emptyList()
            val commentAdapter = commentsAdapter(comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(itemView.context)
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.isNestedScrollingEnabled = false

            // Handle button clicks
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
                .head() // Use HEAD to get headers only
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
                playerView.player = this
            }
        }

        fun releasePlayer() {
            exoPlayer?.release()
        }
    }

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }
}
