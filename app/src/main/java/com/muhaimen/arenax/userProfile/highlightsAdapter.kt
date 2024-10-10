import android.annotation.SuppressLint
import android.content.ContentResolver
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
import com.muhaimen.arenax.dataClasses.DraggableText
import com.muhaimen.arenax.dataClasses.Story
import com.muhaimen.arenax.uploadStory.viewStory
import java.util.Locale

class highlightsAdapter(private val storiesList: List<Story>) : RecyclerView.Adapter<highlightsAdapter.StoryViewHolder>() {

    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storyImage: ImageView = itemView.findViewById(R.id.highlight_image)
        private val storyTitle: TextView = itemView.findViewById(R.id.highlight_title)

        fun bind(story: Story) {
            Log.d("HighlightsAdapter", "Media URL: ${story.mediaUrl}")

            // Attempt to load the media when binding
            loadMedia(story)

            // Set the story title
            storyTitle.text = "Story Title" // Customize as needed

            // Set the click listener for the entire item view
            itemView.setOnClickListener {
                val gson = Gson()

                // Serialize the draggableTexts list to JSON
                val draggableJson = gson.toJson(story.draggableTexts)

                // Start the ViewStory activity and pass the media URL and other data
                val intent = Intent(itemView.context, viewStory::class.java).apply {
                    putExtra("MEDIA_URL", story.mediaUrl)
                    putExtra("Audio", story.trimmedAudioUrl)
                    putExtra("Texts", draggableJson) // Pass the serialized JSON
                }
                itemView.context.startActivity(intent) // Start the activity
            }


        }

        @SuppressLint("SuspiciousIndentation")
        private fun loadMedia(story: Story) {
            val uri = Uri.parse(story.mediaUrl)

            // Check if the media exists before loading

                Glide.with(itemView.context)
                    .load(uri)
                    .thumbnail(0.1f) // Show a thumbnail while loading
                    .error(R.mipmap.appicon2) // Show a default error image if loading fails
                    .into(storyImage)
        }

        private fun mediaExists(uri: Uri, contentResolver: ContentResolver): Boolean {
            return try {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use { it.count > 0 } ?: false
            } catch (e: Exception) {
                Log.e("HighlightsAdapter", "Error checking media existence: ${e.message}")
                false
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

