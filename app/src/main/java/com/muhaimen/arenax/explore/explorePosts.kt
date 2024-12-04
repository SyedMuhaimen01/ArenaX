package com.muhaimen.arenax.explore

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
        postsAdapter = explorePostsAdapter(postsList) // Adapter initialization
        postsRecyclerView.adapter = postsAdapter

        // Fetch posts from the backend
        fetchPosts()

        return view
    }

    private fun fetchPosts() {
        val userId = auth.currentUser?.uid ?: return
        val url = "${Constants.SERVER_URL}explorePosts/user/$userId/fetchPosts" // Replace with your backend API endpoint

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        // Run the network request on a background thread
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

                            // Parse comments
                            val commentsData = mutableListOf<Comment>()
                            val commentsArray = postObject.optJSONArray("comments")
                            commentsArray?.let {
                                for (j in 0 until it.length()) {
                                    val commentObject = it.getJSONObject(j)
                                    val comment = Comment(
                                        commentId = commentObject.optInt("comment_id", 0),  // Use optInt to safely get an integer, defaulting to 0
                                        commentText = commentObject.optString("comment", ""),  // Default to empty string if null
                                        createdAt = commentObject.optString("created_at", ""),
                                        commenterName = commentObject.optString("commenter_name", "Unknown"),  // Default name if missing
                                        commenterProfilePictureUrl = commentObject.optString("commenter_profile_pic", null)
                                    )
                                    commentsData.add(comment)
                                }
                            }

                            // Create the Post object
                            val post = Post(
                                postId = postObject.optInt("post_id", 0),  // Default to 0 if null
                                postContent = postObject.optString("post_content", null),
                                caption = postObject.optString("caption", null),
                                sponsored = postObject.optBoolean("sponsored", false),  // Default to false if not provided
                                likes = postObject.optInt("likes", 0),  // Default to 0 if null
                                comments = postObject.optInt("post_comments", 0),  // Default to 0 if null
                                shares = postObject.optInt("shares", 0),  // Default to 0 if null
                                clicks = postObject.optInt("clicks", 0),  // Default to 0 if null
                                city = postObject.optString("city", null),
                                country = postObject.optString("country", null),
                                trimmedAudioUrl = postObject.optString("trimmed_audio_url", null),  // This can be null, so no issue
                                createdAt = postObject.optString("created_at", ""),
                                userFullName = postObject.optString("full_name", "Unknown User"),  // Default to "Unknown User" if null
                                userProfilePictureUrl = postObject.optString("profile_picture_url", null),
                                commentsData = commentsData
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
