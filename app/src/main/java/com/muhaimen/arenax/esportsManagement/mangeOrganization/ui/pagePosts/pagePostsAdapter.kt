package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.pagePosts

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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.pagePost
import com.muhaimen.arenax.userFeed.commentsAdapter
import com.muhaimen.arenax.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.dataClasses.OrganizationData
import org.json.JSONObject

class pagePostsAdapter(
private val recyclerView: RecyclerView,
private val posts: List<pagePost>,
private val organizationName: String // Pass organization name to adapter
) : RecyclerView.Adapter<pagePostsAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, organizationName) // Pass organizationName to bind method
    }

    override fun getItemCount(): Int = posts.size


    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val requestQueue = Volley.newRequestQueue(itemView.context)
        private val imageView: ImageView = itemView.findViewById(R.id.ImageView)
        private val articleTextView: TextView = itemView.findViewById(R.id.articleTextView)
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
        private val commenterPicture: ImageView = itemView.findViewById(R.id.commentProfilePicture)
        private val organizationLogo: ImageView = itemView.findViewById(R.id.organizationLogo)
        private val organizationNameTextView: TextView = itemView.findViewById(R.id.organizationNameTextView)

        private lateinit var commenterName: String
        private var likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private var alreadyLikedButton: ImageButton = itemView.findViewById(R.id.likeFilledButton)

        private val storageReference = FirebaseStorage.getInstance().reference

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
            val comments = post.commentsData?.toMutableList() ?: mutableListOf()
            val commentAdapter = commentsAdapter(comments)
            recyclerViewComments.layoutManager = LinearLayoutManager(itemView.context)
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.visibility = View.GONE

            locationTextView.text = listOfNotNull(post.city, post.country).joinToString(", ")
            tvCaption.text = post.caption.takeIf { it != "null" } ?: ""
            tvCaption.visibility = if (post.caption == "null") View.GONE else View.VISIBLE

            commentButton.setOnClickListener {
                recyclerViewComments.visibility = if (recyclerViewComments.visibility == View.VISIBLE) View.GONE else View.VISIBLE
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

        private fun loadMedia(media: String?) {
            if (media.isNullOrEmpty()) return
            imageView.visibility = View.VISIBLE
            articleTextView.visibility = View.GONE
            Glide.with(itemView.context).load(Uri.parse(media))
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(imageView)
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
