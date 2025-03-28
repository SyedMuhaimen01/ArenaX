package com.muhaimen.arenax.userFeed
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.uploadStory.viewStory

class UserFeedHighlightsAdapter(private val storiesList: List<Story>) : RecyclerView.Adapter<UserFeedHighlightsAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: ImageView = itemView.findViewById(R.id.highlight_image)
        private val storyTitle: TextView = itemView.findViewById(R.id.highlight_title)

        @SuppressLint("SetTextI18n")
        fun bind(story: Story) {
            storyTitle.visibility=View.VISIBLE
            storyTitle.text = story.userName
            val uri = Uri.parse(story.mediaUrl)
            Glide.with(itemView.context)
                .load(uri)
                .thumbnail(0.1f)
                .error(R.mipmap.appicon2)
                .into(storyImage)

            itemView.setOnClickListener {
                val gson=Gson()
                val draggableJson=gson.toJson(story.draggableTexts)

                val intent = Intent(itemView.context, viewStory::class.java).apply {
                    Log.d("StoryAdapter", story.toString())
                    Log.d("StoryAdapter", draggableJson)
                    // Use the appropriate types for each key-value pair
                    putExtra("intentFrom","Adapter")
                    putExtra("id", story.id)  // Int
                    putExtra("mediaUrl", story.mediaUrl)  // String (URL)
                    putExtra("duration", story.duration)  // Int
                    putExtra("trimmedAudioUrl", story.trimmedAudioUrl)  // String (file path)
                    putExtra("draggableTexts", draggableJson)  // String (JSONArray as String)
                    putExtra("uploadedAt", story.uploadedAt.toString())  // String (Date as String)
                    putExtra("userName", story.userName)  // String
                    putExtra("userProfilePicture", story.userProfilePicture)  // String (URL)
                    putExtra("city", story.city)  // String
                    putExtra("country", story.country)  // String
                    putExtra("latitude", story.latitude)  // Double
                    putExtra("longitude", story.longitude)  // Double
                }

                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.highlights_card, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storiesList[position]
        holder.bind(story)
    }

    override fun getItemCount(): Int = storiesList.size
}
