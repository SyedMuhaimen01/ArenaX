package com.muhaimen.arenax.gamesDashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.gamesData

class gamesListAdapter(private var gamesList: List<gamesData>) : RecyclerView.Adapter<gamesListAdapter.GamesViewHolder>() {

    // ViewHolder class to hold the views for each card
    inner class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        private val gameName: TextView = itemView.findViewById(R.id.name)
        private val genre: TextView = itemView.findViewById(R.id.genre)
        private val publisher: TextView = itemView.findViewById(R.id.publisher)

        fun bind(game: gamesData) {
            // Bind game data to views
            gameName.text = game.gameName
            genre.text = game.genre.joinToString(", ") // Join genres as a string
            publisher.text = game.publisher

            // Load the game icon using Glide
            // Set a placeholder image while loading
            Glide.with(itemView.context)
                .load(game.iconUrl) // Assuming iconUrl is the URL of the image
                .placeholder(R.drawable.circle) // Add a placeholder image
                .error(R.drawable.circle) // Add an error image if loading fails
                .into(gameIcon)
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
    fun updateGamesList(newGamesList: List<gamesData>) {
        gamesList = newGamesList
        notifyDataSetChanged() // Notify the adapter to refresh the views
    }
}
