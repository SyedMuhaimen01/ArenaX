package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
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
import org.json.JSONObject


class pagePostsAdapter(private val recyclerView: RecyclerView,
                           private val posts: List<pagePost>
) : RecyclerView.Adapter<pagePostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)


    }

    override fun getItemCount(): Int = posts.size


    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val articleTextView: TextView = itemView.findViewById(R.id.articleTextView)
        private val OrganizationLogo: ImageView = itemView.findViewById(R.id.organizationLogo)
        private val OrganizationName: TextView = itemView.findViewById(R.id.organizationNameTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.organizationLocationTextView)
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
        private lateinit var commenterName: String
        private var likeButton:ImageButton=itemView.findViewById(R.id.likeButton)
        private var alreadyLikedButton:ImageButton=itemView.findViewById(R.id.likeFilledButton)


        @SuppressLint("SetTextI18n")
        fun bind(post: pagePost) {

            val userId = FirebaseManager.getCurrentUserId()


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
                    saveCommentToServer(post.postId.toString(), newComment)

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
                likeButton.visibility = View.GONE
                alreadyLikedButton.visibility = View.VISIBLE
                likesCount.text = (post.likes + 1).toString()

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


                Log.d("already liked clicked", "already liked clicked")
            }

            Glide.with(itemView.context)
                .load(post.organizationLogo)
                .placeholder(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(OrganizationLogo)


            if (post.caption == "null") {
                tvCaption.visibility = View.GONE
                tvCaption.text = ""
            } else {
                tvCaption.text = post.caption ?: ""
            }
            locationTextView.text = "${post.city ?: ""}, ${post.country ?: ""}".trimEnd { it == ',' || it.isWhitespace() }
            likesCount.text = post.likes.toString()
            tvCommentsCount.text = post.comments.toString()
            OrganizationName.text = post.organizationName

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



        private fun loadMedia(media: String?) {
            if (media.isNullOrEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                val mediaType = getMediaType(media)

                withContext(Dispatchers.Main) {
                    when {
                        mediaType?.startsWith("image/") == true -> {
                            imageView.visibility = View.VISIBLE
                            articleTextView.visibility = View.GONE
                            setImage(media)
                        }

                        mediaType == null -> { // Assume it's plain text
                            imageView.visibility = View.GONE
                            articleTextView.visibility = View.VISIBLE
                            articleTextView.text = media
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
                    notifyPostReload()
                },
                { error -> }
            )
            requestQueue.add(postRequest)
        }


        private fun notifyPostReload(){
            val intent = Intent("NEW_POST_ADDED ")
            LocalBroadcastManager.getInstance(itemView.context).sendBroadcast(intent)
        }
    }
    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
    }

}
