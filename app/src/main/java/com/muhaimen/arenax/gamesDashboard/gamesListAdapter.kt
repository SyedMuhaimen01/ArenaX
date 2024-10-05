package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhaimen.arenax.R

class gamesListAdapter(private var gamesList: List<gamesData>) : RecyclerView.Adapter<gamesListAdapter.GamesListViewHolder>() {

    // ViewHolder class
    inner class GamesListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        val gameName: TextView = itemView.findViewById(R.id.name)
        val genre: TextView = itemView.findViewById(R.id.genre)
        val publisher: TextView = itemView.findViewById(R.id.publisher)

        @SuppressLint("SetTextI18n")
        fun bind(data: gamesData) {
            gameName.text = data.gameName
            val genres = data.genre.joinToString(", ")
            genre.text = "Genre: $genres"
            gameIcon.setImageResource(data.iconResId)
            publisher.text = "Publisher: ${data.publisher}"
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gameslist_game_card, parent, false)
        return GamesListViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: GamesListViewHolder, position: Int) {
        val gamesData = gamesList[position]
        holder.bind(gamesData)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return gamesList.size
    }

    fun updateGamesList(newList: List<gamesData>) {
        gamesList = newList
        notifyDataSetChanged()
    }
}
