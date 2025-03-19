package com.muhaimen.arenax.explore

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post

import com.muhaimen.arenax.userProfile.explorePostsAdapter
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class explorePosts : Fragment() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: explorePostsAdapter
    private val postsList = mutableListOf<Post>()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_posts, container, false)

        auth = FirebaseAuth.getInstance()
        // Initialize RecyclerView and set the adapter
        postsRecyclerView = view.findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        postsAdapter = explorePostsAdapter(postsList)
        postsRecyclerView.adapter = postsAdapter

        // Fetch posts from the backend
        fetchPosts()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchPosts() {
        val userId = auth.currentUser?.uid ?: return
        val url = "${Constants.SERVER_URL}explorePosts/user/$userId/fetchPosts" // Replace with your backend API endpoint

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Timeout for establishing a connection
            .readTimeout(60, TimeUnit.SECONDS)    // Timeout for reading data from the server
            .writeTimeout(60, TimeUnit.SECONDS)   // Timeout for writing data to the server
            .build()
        val request = Request.Builder()
            .url(url)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Parse the response
                    val responseData = response.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        val jsonArray = JSONArray(responseData)
                        val posts = mutableListOf<Post>()

                        for (i in 0 until jsonArray.length()) {
                            val postObject = jsonArray.getJSONObject(i)
                            val commentsData = mutableListOf<Comment>()
                            val commentsArray = postObject.optJSONArray("comments")
                            commentsArray?.let {
                                for (j in 0 until it.length()) {
                                    val commentObject = it.getJSONObject(j)
                                    val comment = Comment(
                                        commentId = commentObject.optInt("comment_id", 0),
                                        commentText = commentObject.optString("comment", ""),
                                        createdAt = commentObject.optString("created_at", ""),
                                        commenterName = commentObject.optString("commenter_name", "Unknown"),
                                        commenterProfilePictureUrl = commentObject.optString("commenter_profile_pic", null)
                                    )
                                    commentsData.add(comment)
                                }
                            }

                            // Create the Post object
                            val post = Post(
                                postId = postObject.optInt("post_id", 0),
                                postContent = postObject.optString("post_content", null),
                                caption = postObject.optString("caption", null),
                                sponsored = postObject.optBoolean("sponsored", false),
                                likes = postObject.optInt("likes", 0),
                                comments = postObject.optInt("post_comments", 0),
                                shares = postObject.optInt("shares", 0),
                                clicks = postObject.optInt("clicks", 0),
                                city = postObject.optString("city", null),
                                country = postObject.optString("country", null),
                                trimmedAudioUrl = postObject.optString("trimmed_audio_url", null),
                                createdAt = postObject.optString("created_at", ""),
                                userFullName = postObject.optString("full_name", "Unknown User"),
                                userProfilePictureUrl = postObject.optString("profile_picture_url", null),
                                commentsData = commentsData,
                                isLikedByUser = postObject.optBoolean("likedByUser", false)
                            )
                            posts.add(post)
                        }

                        // Update the UI on the main thread
                        withContext(Dispatchers.Main) {
                            postsList.clear()
                            postsList.addAll(posts)
                            postsAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("explorePosts", "Error fetching posts", e)
            }
        }
    }
}
