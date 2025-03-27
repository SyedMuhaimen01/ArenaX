import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.muhaimen.arenax.R
import com.muhaimen.arenax.dataClasses.Comment
import com.muhaimen.arenax.dataClasses.Post
import com.muhaimen.arenax.userFeed.commentsAdapter
import com.muhaimen.arenax.utils.FirebaseManager

class BottomSheetDialogFragment(
    private val post: Post,
    private var commentsList: List<Comment>,
    private val onCommentSubmitted: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var commentsAdapter: commentsAdapter
    private lateinit var commenterName: String
    private lateinit var commenterPicture: String
    private var isEditTextFocused = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = true // Allow dismissing by clicking outside or pressing back
        Log.d("CommentsBottomSheet", "commentsList: $commentsList")
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        commentsAdapter = commentsAdapter(commentsList)
        recyclerView.adapter = commentsAdapter

        // Handle Send Button Click
        val sendButton = view.findViewById<ImageButton>(R.id.sendCommentButton)
        val commentInput = view.findViewById<EditText>(R.id.writeCommentEditText)
        val commenterProfilePicture = view.findViewById<ImageView>(R.id.commentProfilePicture)

        // Fetch user profile picture and name from Firebase
        val userId = FirebaseManager.getCurrentUserId()
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userDataRef: DatabaseReference = database.reference.child("userData").child(userId)

            userDataRef.get().addOnSuccessListener { dataSnapshot ->
                commenterPicture = dataSnapshot.child("profilePicture").getValue(String::class.java).toString()
                commenterName = dataSnapshot.child("fullname").getValue(String::class.java).toString()
                Log.d("Firebase", "Profile picture URL: $commenterPicture")

                // Load profile picture using Glide
                Glide.with(this)
                    .load(commenterPicture)
                    .placeholder(R.mipmap.appicon2)
                    .circleCrop()
                    .into(commenterProfilePicture)
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching user data", exception)
            }
        } else {
            Log.e("Firebase", "User is not logged in.")
        }

        // Handle Comment Submission
        sendButton.setOnClickListener {
            val commentText = commentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                onCommentSubmitted(commentText)
                commentInput.setText("") // Clear the input field after submission
            }
        }

        // Enable/Disable Send Button Based on Input
        commentInput.doOnTextChanged { text, _, _, _ ->
            sendButton.isEnabled = !text.isNullOrBlank()
        }

        // Scroll to Bottom When Focused
        val rootView = view.rootView
        // Add a global layout listener to detect keyboard state changes
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // Check if the keyboard is visible
            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open
                val bottomSheet = view.parent as? View
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            } else {
                // Keyboard is closed
                val bottomSheet = view.parent as? View
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    commentInput.clearFocus()
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        // Handle Focus Changes
        commentInput.setOnFocusChangeListener { _, hasFocus ->
            isEditTextFocused=hasFocus
            val bottomSheet = view.parent as? View
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                if (hasFocus) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    // Function to update the comments list dynamically
    fun updateComments(newCommentsList: List<Comment>) {
        commentsList = newCommentsList
        commentsAdapter.updateData(newCommentsList) // Notify adapter of data changes
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent) // Transparent background
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                behavior.isFitToContents = true // Allow scrolling if content exceeds screen height
                behavior.halfExpandedRatio = 0.6f // Optional: Set a custom ratio for half-expanded state
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED // Start in collapsed state
                behavior.skipCollapsed = false // Do not skip the collapsed state
                behavior.isDraggable = true // Allow dragging

                // Add a callback to handle state changes
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> {
                                // Dismiss the Bottom Sheet when it is hidden
                                dismiss()
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                // Ensure the sheet stays fully expanded
                                if (!isEditTextFocused) {
                                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                }
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                // Ensure the sheet stays fully collapsed
                                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }
                            else -> {
                                // Handle other states if needed
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // Handle slide animations if needed
                    }
                })
            }
        }

        return dialog
    }


}