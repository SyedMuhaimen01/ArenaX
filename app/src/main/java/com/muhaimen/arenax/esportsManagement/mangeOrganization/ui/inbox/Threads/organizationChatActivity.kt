package com.muhaimen.arenax.esportsManagement.mangeOrganization.ui.inbox.Threads

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.ChatItem
import com.muhaimen.arenax.utils.Constants
import org.json.JSONObject

class organizationChatActivity : AppCompatActivity() {
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var buttonTakePicture: ImageButton
    private lateinit var buttonOpenGallery: ImageButton
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var queue: RequestQueue
    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var textViewGamerTag: TextView

    private lateinit var chatAdapter: organizationChatsAdapter
    private val chatMessages = mutableListOf<ChatItem>()

    private lateinit var database: DatabaseReference
    private val storage = FirebaseStorage.getInstance("gs://i210888.appspot.com")

    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>

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
        Log.d("organizationChatActivity", "onCreate")
        queue = Volley.newRequestQueue(this)
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
        senderId= intent.getStringExtra("organizationId") ?: ""

        Log.d("organizationChatActivity", "receiverId: $receiverId, receiverFullName: $receiverFullName, receiverGamerTag: $receiverGamerTag, receiverProfilePicture: $receiverProfilePicture")
        textViewGamerTag.text = receiverGamerTag
        if (receiverProfilePicture.isNotEmpty()) {
            Glide.with(this)
                .load(receiverProfilePicture)
                .placeholder(R.drawable.game_icon_foreground)
                .error(R.drawable.game_icon_foreground)
                .circleCrop()
                .into(imageViewProfilePicture)
        }

        database = FirebaseDatabase.getInstance().getReference("userData")

        chatAdapter = organizationChatsAdapter(chatMessages,senderId)
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, ChatItem.ContentType.TEXT)
            }
        }

        buttonTakePicture.setOnClickListener { capturePhoto() }
        buttonOpenGallery.setOnClickListener { pickMediaLauncher.launch("image/*") }

        setupCameraActivityResultLauncher()
    }

    private fun loadMessages() {
        val chatId = generateChatId(senderId, receiverId)
        val chatRef = FirebaseDatabase.getInstance().getReference("organizationsData").child(senderId).child("chats").child(chatId)

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear()
                for (messageSnapshot in snapshot.children) {
                    val chatItem = messageSnapshot.getValue(ChatItem::class.java)
                    chatItem?.let { chatMessages.add(it) }
                }
                chatAdapter.notifyDataSetChanged()
                recyclerViewMessages.scrollToPosition(chatMessages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@organizationChatActivity, "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(content: String, contentType: ChatItem.ContentType, attachmentUrl: String? = null) {
        val chatId = generateChatId(senderId, receiverId)
        val messageId =  FirebaseDatabase.getInstance().getReference("organizationsData").child(senderId).child("chats").child(chatId).push().key ?: return
        val timestamp = System.currentTimeMillis()

        val chatItem = ChatItem(
            chatId = messageId,
            senderId = senderId,
            receiverId = receiverId,
            message = content,
            time = timestamp,
            contentType = contentType,
            contentUri = attachmentUrl,
            isRead = false
        )

        val senderChatRef =  FirebaseDatabase.getInstance().getReference("organizationsData").child(senderId).child("chats").child(chatId).child(messageId)
        val receiverChatRef = database.child(receiverId).child("chats").child(chatId).child(messageId)

        senderChatRef.setValue(chatItem).addOnSuccessListener {
            receiverChatRef.setValue(chatItem)
            messageEditText.text.clear()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to send message: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        //Below function not implemented in the current app logic
        //sendNotificationToBackend(senderId, receiverId)
    }

    private fun sendMedia(uri: Uri, type: String) {
        val chatId = generateChatId(senderId, receiverId)
        val mediaRef = storage.reference.child("chat_media/${System.currentTimeMillis()}_${uri.lastPathSegment}")

        mediaRef.putFile(uri).addOnSuccessListener {
            mediaRef.downloadUrl.addOnSuccessListener { downloadUri ->
                sendMessage(downloadUri.toString(), if (type == "image") ChatItem.ContentType.IMAGE else ChatItem.ContentType.VIDEO, downloadUri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload media: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateChatId(senderId: String, receiverId: String): String {
        return if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
    }

    private fun capturePhoto() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        }
        currentPhotoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        }
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    private fun setupCameraActivityResultLauncher() {
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoUri?.let { uri ->
                    sendMedia(uri, "image")
                }
            } else {
                Toast.makeText(this, "Camera operation canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Function to create notifications using the server
    private fun sendNotificationToBackend(senderId: String, receiverId: String) {
        // Create JSON object with the necessary data
        val jsonObject = JSONObject().apply {
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("message", "New Message")
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Constants.SERVER_URL}notify-new-message",
            jsonObject,
            { response ->
                // Successfully sent notification to backend
                Log.d("ChatActivity", "Successfully sent notification to backend: $response")
                // Optionally show a success notification or toast
                Toast.makeText(this@organizationChatActivity, "Notification sent", Toast.LENGTH_SHORT).show()
            },
            { error ->
                // Error while sending notification
                Log.e("ChatActivity", "Error sending notification to backend: ${error.message}")


            }
        )

        // Add the request to the queue (assuming you're using Volley)
        queue.add(request)
    }
}
