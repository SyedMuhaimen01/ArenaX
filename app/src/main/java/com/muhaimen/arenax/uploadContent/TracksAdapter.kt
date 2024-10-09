package com.muhaimen.arenax.uploadContent

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Track
import java.io.IOException

class TracksAdapter(
    private val activity: UploadContent,
    private var tracks: List<Track>,
    private val onClick: (Track) -> Unit
) : RecyclerView.Adapter<TracksAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.track_title)
        val artist: TextView = view.findViewById(R.id.track_artist)
        val albumName: TextView = view.findViewById(R.id.album)
        val albumImage: ImageView = view.findViewById(R.id.albumPhoto)
        val track_duration: TextView = view.findViewById(R.id.track_duration)
    }

    private var mediaPlayer: MediaPlayer? = null

    private var currentTrack: Track? = null // To keep track of the currently playing track
    var duration: Int = 0
    var downloadUrl: String? = null
    var audioUrl: String? = null
    private val handler = Handler(Looper.getMainLooper())
    var selectedTrack:Track? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item_card, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        val minutes = track.duration / 60
        val seconds = track.duration % 60
        holder.title.text = track.title
        holder.artist.text = track.artist
        holder.track_duration.text = String.format("Duration: %02d:%02d", minutes, seconds)
        holder.albumName.text = "Album: ${track.albumName}" // Set album name

        Glide.with(holder.itemView.context) // Load album image using Glide
            .load(track.albumImage)
            .into(holder.albumImage)

        holder.itemView.setOnClickListener {
            // Stop and release any existing media player before starting a new one
            stopAudio()

            // Set the current track and start playing the audio
            activity.startSeekBar.max = track.duration
            activity.endSeekBar.max = track.duration
            selectedTrack=track
            currentTrack = track
            audioUrl = track.audioUrl
            playTrack(track)
            downloadUrl = track.downloadUrl
            duration = track.duration
            setSeekBarLimits(track.duration)

            Log.d("MediaPlayer", "Audio duration: ${track.duration}")
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    // Release the MediaPlayer resources when done
    fun releasePlayer() {
        stopAudio() // Stop audio if playing
    }

    fun pauseAudio() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause() // Pause the audio playback
    }

    fun stopAudio() {
        mediaPlayer?.let {
            it.stop()
            it.reset()
            it.release()
        }
        mediaPlayer = null
        currentTrack = null
    }

    fun playTrack(track: Track) {
        stopAudio() // Stop any existing audio before starting a new one
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(track.audioUrl)
                setOnPreparedListener {
                    // Seek to the start point and start playback
                    val startPoint = activity.startSeekBar.progress * 1000 // Convert to milliseconds
                    val endPoint = activity.endSeekBar.progress * 1000 // Convert to milliseconds

                    seekTo(startPoint)
                    start()
                    Log.d("MediaPlayer", "Audio started playing from $startPoint ms")

                    // Loop playback between startPoint and endPoint
                    val handler = Handler()
                    val runnable = object : Runnable {
                        override fun run() {
                            // Ensure MediaPlayer is valid and not released
                            if (mediaPlayer?.isPlaying == true) {
                                val currentPosition = mediaPlayer?.currentPosition ?: 0
                                if (currentPosition >= endPoint) {
                                    Log.d("MediaPlayer", "Reached end point, restarting from start point...")
                                    seekTo(startPoint) // Seek back to the start point
                                    start() // Restart playback
                                }
                                // Continue checking the current position
                                handler.postDelayed(this, 100) // Check every 100 ms
                            } else {
                                // If not playing, remove callbacks
                                handler.removeCallbacks(this)
                            }
                        }
                    }
                    handler.postDelayed(runnable, 100) // Start checking after 100 ms

                    // Set up completion listener to loop the track
                    setOnCompletionListener {
                        Log.d("MediaPlayer", "Audio playback completed, restarting from start point...")
                        seekTo(startPoint) // Seek back to the start point
                        start() // Restart playback
                    }
                }
                prepareAsync() // Prepare the MediaPlayer asynchronously
            } catch (e: IOException) {
                Log.e("MediaPlayer", "Error setting data source: ${e.message}")
                stopAudio() // Stop the audio in case of an error
            }
        }
    }


    private fun setSeekBarLimits(duration: Int) {
        activity.startSeekBar.max = duration
        activity.endSeekBar.max = duration
        activity.searchLinearLayout.visibility = View.GONE
        activity.trimTrackLayout.visibility = View.VISIBLE
    }


}
