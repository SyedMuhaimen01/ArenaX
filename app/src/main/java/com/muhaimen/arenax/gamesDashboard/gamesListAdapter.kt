package com.muhaimen.arenax.gamesDashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.muhaimen.arenax.dataClasses.AppInfo
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class gamesListAdapter(
    private val activityContext: Context, // Add this parameter
    private var gamesList: MutableList<AppInfo>,
    private val userId: String,
    private val fetchInstalledApps: () -> Unit
) : RecyclerView.Adapter<gamesListAdapter.GamesViewHolder>() {

    inner class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameIcon: ImageView = itemView.findViewById(R.id.game_icon)
        private val gameName: TextView = itemView.findViewById(R.id.name)
        private val genre: TextView = itemView.findViewById(R.id.genre)
        private val publisher: TextView = itemView.findViewById(R.id.publisher)
        private val addButton: Button = itemView.findViewById(R.id.add_button) // Add Button

        fun bind(game: AppInfo) {
            gameName.text = game.name
            genre.text = game.genre
            publisher.text = game.publisher
            val formattedIcon = formatUrl(game.logoUrl)

            Glide.with(itemView.context)
                .load(formattedIcon)
                .placeholder(R.drawable.circle)
                .error(R.drawable.circle)
                .into(gameIcon)

            addButton.setOnClickListener {
                addGame(game, userId, itemView.context, activityContext)
                GenerateUserInterests(activityContext)
                addButton.isEnabled = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gameslist_game_card, parent, false)
        return GamesViewHolder(view)
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        holder.bind(gamesList[position])
    }

    override fun getItemCount(): Int = gamesList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateGamesList(newGamesList: List<AppInfo>) {
        gamesList = newGamesList.toMutableList()
        notifyDataSetChanged()
    }

    private fun addGame(appInfo: AppInfo, userId: String, itemViewContext: Context, activityContext: Context) {
        val queue = Volley.newRequestQueue(itemViewContext) // Use itemView context for the request queue
        val url = "${Constants.SERVER_URL}installedGame/user/$userId/addGame"

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
                    Toast.makeText(itemViewContext, "Game added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent("NEW_GAME_ADDED")
                    LocalBroadcastManager.getInstance(activityContext).sendBroadcast(intent)
                    Log.e("BroadcastSender", "Sending NEW_GAME_ADDED broadcast")

                    fetchInstalledApps()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun GenerateUserInterests(context: Context) {
        val url = "${Constants.SERVER_URL}userIntertests/user/$userId/generateInterests"
        val stringRequest = StringRequest(
            Request.Method.POST, url,
            { response -> },
            { error ->
                Log.e("VolleyError", "Error: ${error.message}")
            }
        )
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }

    fun formatUrl(url: String?): String {
        return when {
            url.isNullOrEmpty() -> ""
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https:$url"
        }
    }
}