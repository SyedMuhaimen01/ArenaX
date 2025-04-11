package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.sponsoredPosts

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.pagePost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response

class SponsoredPostsAdapter(
    private val recyclerView: RecyclerView,
    private val posts: List<pagePost>,
    private val organizationName: String // Pass organization name to adapter
) : RecyclerView.Adapter<SponsoredPostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sponsor_post_item, parent, false)
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
        layoutManager?.let {
            // Find the first and last completely visible item positions
            val firstVisible = it.findFirstCompletelyVisibleItemPosition()
            val lastVisible = it.findLastCompletelyVisibleItemPosition()

            // Iterate through all items in the adapter
            for (i in 0 until itemCount) {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? SponsoredPostsAdapter.PostViewHolder
                viewHolder?.let { holder ->
                    if (i in firstVisible..lastVisible) {
                        holder.playContent() // Start playing if the item is completely visible
                    } else {
                        holder.stopContent() // Stop playing if the item is not completely visible
                    }
                }
            }
        }
    }



    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val requestQueue = Volley.newRequestQueue(itemView.context)
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val playerView: PlayerView = itemView.findViewById(R.id.videoPlayerView)
        private val articleTextView: TextView = itemView.findViewById(R.id.articleTextView)
        private val sponsorButton: Button = itemView.findViewById(R.id.sponsorButton)
        private val client = OkHttpClient()
        var exoPlayer: ExoPlayer? = null
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

    }
}
