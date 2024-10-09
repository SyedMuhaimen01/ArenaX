package com.muhaimen.arenax.gamesDashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.dataClasses.AppInfo
import org.json.JSONObject

class gamesListAdapter(
    private var gamesList: MutableList<AppInfo>,
    private val userId: String
) : RecyclerView.Adapter<gamesListAdapter.GamesViewHolder>() {

    // ViewHolder class to hold the views for each card
    inner class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        private val gameName: TextView = itemView.findViewById(R.id.name)
        private val genre: TextView = itemView.findViewById(R.id.genre)
        private val publisher: TextView = itemView.findViewById(R.id.publisher)
        private val addButton: Button = itemView.findViewById(R.id.add_button) // Add Button

        fun bind(game: AppInfo) {
            // Bind game data to views
            gameName.text = game.name
            genre.text = game.genre
            publisher.text = game.publisher

            val formattedIcon = formatUrl(game.logoUrl)

            Glide.with(itemView.context)
                .load(formattedIcon) // Assuming logoUrl is the URL of the image
                .placeholder(R.drawable.circle) // Add a placeholder image
                .error(R.drawable.circle) // Add an error image if loading fails
                .into(gameIcon)

            // Set up add button click listener
            addButton.setOnClickListener {
                addGame(game, userId, itemView.context) // Pass context and userId
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gameslist_game_card, parent, false)
        return GamesViewHolder(view)
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        holder.bind(gamesList[position]) // Bind game data to the ViewHolder
    }

    override fun getItemCount(): Int = gamesList.size

    // Method to update the games list and notify changes
    fun updateGamesList(newGamesList: List<AppInfo>) {
        gamesList = newGamesList.toMutableList()
        notifyDataSetChanged()
    }

    // Method to send game data to the backend using Volley
    private fun addGame(appInfo: AppInfo, userId: String, context: Context) {
        val queue = Volley.newRequestQueue(context) // Pass the context here
        val url = "http://192.168.100.6:3000/installedGame/user/$userId/addGame"

        val jsonBody = JSONObject().apply {
            put("userId", userId)
            put("game_name", appInfo.name)
            put("package_name", appInfo.packageName)
            put("logo_url", appInfo.logoUrl)
            put("genre", appInfo.genre)
            put("publisher", appInfo.publisher)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                if (response.getString("message") == "Game added successfully") {
                    // Success: show a Toast or handle response accordingly
                    Toast.makeText(context, "Game added successfully", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                // Handle error response
                Toast.makeText(context, "Error adding game", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    fun formatUrl(url: String?): String {
        return when {
            url.isNullOrEmpty() -> "" // Return empty string for null or empty input
            url.startsWith("http://") || url.startsWith("https://") -> url // Return the URL as is
            else -> "https:$url" // Prepend with https if it starts with //
        }
    }
}
