package com.muhaimen.arenax.Threads

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.utils.FirebaseManager

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var buttonTakePicture: ImageButton
    private lateinit var buttonOpenGallery: ImageButton
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton

    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var textViewGamerTag: TextView

    private lateinit var chatAdapter: ChatsAdapter
    private val chatMessages = mutableListOf<ChatItem>()

    private lateinit var database: DatabaseReference
    private val storage = FirebaseStorage.getInstance()

    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>

    // Modify pickMediaLauncher to accept both image and video MIME types
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { sendMedia(it, if (it.toString().contains("video")) "video" else "image") }
    }

    private lateinit var senderId: String
    private lateinit var receiverId: String
    private lateinit var receiverFullName: String
    private lateinit var receiverGamerTag: String
    private lateinit var receiverProfilePicture: String

    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        window.statusBarColor = resources.getColor(R.color.primaryColor)
        window.navigationBarColor = resources.getColor(R.color.primaryColor)

        recyclerViewMessages = findViewById(R.id.chatsRecyclerView)
        buttonTakePicture = findViewById(R.id.cameraButton)
        buttonOpenGallery = findViewById(R.id.addButton)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        imageViewProfilePicture = findViewById(R.id.profilePicture)
        textViewGamerTag = findViewById(R.id.gamerTag)

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        val intent = intent
        receiverId = intent.getStringExtra("userId") ?: ""
        receiverFullName = intent.getStringExtra("fullname") ?: ""
        receiverGamerTag = intent.getStringExtra("gamerTag") ?: ""
        receiverProfilePicture = intent.getStringExtra("profilePicture") ?: ""

        textViewGamerTag.text = receiverGamerTag
        if (receiverProfilePicture.isNotEmpty()) {
            Glide.with(this)
                .load(receiverProfilePicture)
                .placeholder(R.drawable.game_icon_foreground)
                .error(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(imageViewProfilePicture)
        }

        senderId = FirebaseManager.getCurrentUserId().toString()
        database = FirebaseManager.getDatabseInstance().getReference("chats")

        chatAdapter = ChatsAdapter(chatMessages)
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoUri?.let { uri ->
                    sendMedia(uri, if (uri.toString().contains("video")) "video" else "image")
                }
            }
        }

        buttonTakePicture.setOnClickListener { openCamera() }
        buttonOpenGallery.setOnClickListener { pickMediaLauncher.launch("*/*") } // This now accepts all media types

        sendButton.setOnClickListener { sendMessage() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadChatMessages()
    }

    private fun openCamera() {
        val options = arrayOf("Take Photo", "Record Video")
        AlertDialog.Builder(this).apply {
            setTitle("Select Media")
            setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        currentPhotoUri = createImageUri()
                        currentPhotoUri?.let { uri ->
                            val imageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            }
                            cameraActivityResultLauncher.launch(imageIntent)
                        }
                    }
                    1 -> {
                        currentPhotoUri = createVideoUri()
                        currentPhotoUri?.let { uri ->
                            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            }
                            cameraActivityResultLauncher.launch(videoIntent)
                        }
                    }
                }
            }
        }.show()
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: Uri.EMPTY
    }

    private fun createVideoUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.TITLE, "New Video")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: Uri.EMPTY
    }

    private fun sendMedia(uri: Uri, type: String) {
        val mediaRef = storage.reference.child("chat_media/${System.currentTimeMillis()}.$type")
        mediaRef.putFile(uri).addOnSuccessListener {
            mediaRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val chatItem = ChatItem(
                    senderId = senderId,
                    receiverId = receiverId,
                    contentUri = downloadUrl.toString(),
                    contentType = if (type == "image") ChatItem.ContentType.IMAGE else ChatItem.ContentType.VIDEO
                )
                database.push().setValue(chatItem).addOnSuccessListener { loadChatMessages() }
                    .addOnFailureListener { e -> Toast.makeText(this, "Error sending media: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }.addOnFailureListener { e -> Toast.makeText(this, "Error uploading media: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val chatItem = ChatItem(senderId = senderId, receiverId = receiverId, message = messageText)
            database.push().setValue(chatItem).addOnSuccessListener {
                messageEditText.text.clear()
                loadChatMessages()
            }.addOnFailureListener { e -> Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun loadChatMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear()
                for (data in snapshot.children) {
                    val chatItem = data.getValue(ChatItem::class.java)
                    chatItem?.let {
                        if ((it.senderId == senderId && it.receiverId == receiverId) ||
                            (it.senderId == receiverId && it.receiverId == senderId)) {
                            chatMessages.add(it)
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged()
                recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Error loading messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
