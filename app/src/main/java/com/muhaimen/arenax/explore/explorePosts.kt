package com.muhaimen.arenax.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R
import com.muhaimen.arenax.userProfile.UserPost
import com.muhaimen.arenax.userProfile.explorePostsAdapter

class explorePosts : Fragment() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: explorePostsAdapter
    private val dummyPostsList = generateDummyPosts()  // Generate dummy data here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_posts, container, false)

        // Initialize RecyclerView and set the adapter
        postsRecyclerView = view.findViewById(R.id.posts_recyclerview)
        postsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        postsAdapter = explorePostsAdapter(dummyPostsList)
        postsRecyclerView.adapter = postsAdapter

        return view
    }

    // Function to generate dummy data for UserPost
    private fun generateDummyPosts(): List<UserPost> {
        return listOf(
            UserPost(
                username = "gamer123",
                profilePictureUrl = "https://example.com/profile1.jpg",
                postContent = "https://example.com/post1.jpg",
                caption = "Just had an amazing game!",
                likes = 150,
                comments = 20,
                shares = 5,
                trimmedAudioUrl = "https://example.com/audio1.mp3",
                createdAt = "2024-10-28"
            ),
            UserPost(
                username = "player456",
                profilePictureUrl = "https://example.com/profile2.jpg",
                postContent = "https://example.com/post2.jpg",
                caption = "New high score!",
                likes = 200,
                comments = 45,
                shares = 12,
                trimmedAudioUrl = "https://example.com/audio2.mp3",
                createdAt = "2024-10-28"
            ),
            UserPost(
                username = "proGamer",
                profilePictureUrl = "https://example.com/profile3.jpg",
                postContent = "https://example.com/post3.jpg",
                caption = "Victory!",
                likes = 320,
                comments = 67,
                shares = 20,
                trimmedAudioUrl = "https://example.com/audio3.mp3",
                createdAt = "2024-10-27"
            ),
            UserPost(
                username = "speedster",
                profilePictureUrl = "https://example.com/profile4.jpg",
                postContent = "https://example.com/post4.jpg",
                caption = "Reached top speed!",
                likes = 180,
                comments = 30,
                shares = 10,
                trimmedAudioUrl = "https://example.com/audio4.mp3",
                createdAt = "2024-10-26"
            ),
            UserPost(
                username = "aceSniper",
                profilePictureUrl = "https://example.com/profile5.jpg",
                postContent = "https://example.com/post5.jpg",
                caption = "Sniped the final boss!",
                likes = 275,
                comments = 50,
                shares = 8,
                trimmedAudioUrl = "https://example.com/audio5.mp3",
                createdAt = "2024-10-25"
            ),
            UserPost(
                username = "ninjaWarrior",
                profilePictureUrl = "https://example.com/profile6.jpg",
                postContent = "https://example.com/post6.jpg",
                caption = "Stealth mode activated!",
                likes = 240,
                comments = 40,
                shares = 15,
                trimmedAudioUrl = "https://example.com/audio6.mp3",
                createdAt = "2024-10-24"
            )
        )
    }

}
