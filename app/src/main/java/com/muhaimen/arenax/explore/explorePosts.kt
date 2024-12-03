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
import com.muhaimen.arenax.userProfile.UserPost
import com.muhaimen.arenax.userProfile.explorePostsAdapter
import com.muhaimen.arenax.utils.Constants
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray

class explorePosts : Fragment() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: explorePostsAdapter
    private val postsList = mutableListOf<UserPost>()
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
                        val posts = mutableListOf<UserPost>()

                        for (i in 0 until jsonArray.length()) {
                            val postObject = jsonArray.getJSONObject(i)
                            val post = UserPost(
                                username = "someUsername", // You can get the username from a separate request or data source
                                profilePictureUrl = "https://example.com/profile.jpg", // You can get the profile picture from the backend or another source
                                postContent = postObject.getString("post_content"),
                                caption = postObject.getString("caption"),
                                likes = postObject.getInt("likes"),
                                comments = postObject.getInt("post_comments"),
                                shares = postObject.getInt("shares"),
                                trimmedAudioUrl = postObject.getString("trimmed_audio_url"),
                                createdAt = postObject.getString("created_at")
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
