package com.shinjaehun.instafire

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shinjaehun.instafire.databinding.ActivityCreateBinding
import com.shinjaehun.instafire.models.Post
import com.shinjaehun.instafire.models.User

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234
class CreateActivity : AppCompatActivity() {
    private var photoUri: Uri? = null

    private var signedInUser: User? = null
    private lateinit var binding: ActivityCreateBinding
//    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference = FirebaseStorage.getInstance().reference

        firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }

        var getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri = result.data?.data
                Log.i(TAG, "photoUri : $photoUri")
                binding.imageView.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
            }
        }

        // 이건 좀 결이 다른...
//        var getResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            if (uri != null) {
//                Log.i(TAG, "uri: $uri")
//                binding.imageView.setImageURI(uri)
//            } else {
//                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
//            }
//        }

        binding.btnPickImage.setOnClickListener {
            Log.i(TAG, "open up image picker on device")

            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "*/*" // 이게 문제였다... 이걸 "image/*"로 했었는데
            getResult.launch(imagePickerIntent)

            // startActivityForResult() deprecated
//            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT).
//            imagePickerIntent.type = "image/*"
//            if (imagePickerIntent.resolveActivity(packageManager) != null) {
//                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
//            }

            // 이건 좀 결이 다른...
//            getResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null) {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etDescription.text.isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedInUser == null) {
            Toast.makeText(this, "no signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri // 이게 null check 때문에...
        // 이게 중요하지
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        // upload photo to firebase storage
//        photoReference.putFile(photoUri)
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                // retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // create a post object with the image url and add that to the posts collection
                val post = Post(
                    binding.etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                binding.btnSubmit.isEnabled = true

                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception during firebase operation", postCreationTask.exception)
                    Toast.makeText(this, "failed to save post", Toast.LENGTH_SHORT).show()
                }
                binding.etDescription.text.clear()
                binding.imageView.setImageResource(0)
                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)

                finish()
            }
    }

    // onActivityResult() deprecated
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_PHOTO_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                photoUri = data?.data
//                Log.i(TAG, "photoUri $photoUri")
//                binding.imageView.setImageURI(photoUri)
//            } else {
//                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}