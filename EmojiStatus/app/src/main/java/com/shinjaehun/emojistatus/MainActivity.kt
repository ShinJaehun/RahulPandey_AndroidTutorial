package com.shinjaehun.emojistatus

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shinjaehun.emojistatus.databinding.ActivityMainBinding
import kotlin.math.sign

data class User (
    val displayName: String = "",
    val emojis: String = ""
)

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    private companion object {
        private const val TAG = "MainActivity"
//        private const val RC_GOOGLE_SIGN_IN = 4926
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val query = db.collection("users")
        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
            .setLifecycleOwner(this).build()

        val adapter = object: FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(this@MainActivity).inflate(android.R.layout.simple_list_item_2, parent, false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
                val tvEmojis: TextView = holder.itemView.findViewById(android.R.id.text2)
                tvName.text = model.displayName
                tvEmojis.text = model.emojis
            }
        }

        binding.rvUsers.adapter = adapter
        binding.rvUsers.layoutManager = LinearLayoutManager(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miLogout) {
            Log.i(TAG, "Logout")
            auth.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        } else if (item.itemId == R.id.miEdit) {
            Log.i(TAG, "Show alert dialog to edit status")
            showAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog() {
        val editText = EditText(this)
        // restrict input length and only to emojis
        val lengthFilter = InputFilter.LengthFilter(9) // 실제로는 4 이모지로 제한
        val emojiFilter = EmojiFilter()
        editText.filters = arrayOf(lengthFilter, emojiFilter)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Update your  emojis")
            .setView(editText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            Log.i(TAG, "Clicked on positive button!")
            val emojisEntered: String = editText.text.toString()
            if (emojisEntered.isBlank()) {
                Toast.makeText(this, "cannot submit empty text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "no signed in user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // update firestore with the new emojis
            db.collection("users").document(currentUser.uid)
                .update("emojis", emojisEntered)

            dialog.dismiss()
        }
    }


    inner class EmojiFilter : InputFilter{
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence {
            // if the added text is valid, return source
            // if it's invalid, then ""
            if (source == null || source.isBlank()) {
                return ""
            }
            Log.i(TAG, "Added text $source, it has length ${source.length} characters")
            val validCharTypes = listOf(Character.SURROGATE, Character.NON_SPACING_MARK, Character.OTHER_SYMBOL).map {
                // valid emojis character types: 6, 19, 28
                it.toInt()
            }
            // if it's invalid, then ""
            for (inputChar in source) {
                val type = Character.getType(inputChar)
                Log.i(TAG, "character type $type")
                if (!validCharTypes.contains(type)) {
                    Toast.makeText(this@MainActivity, "Only emojis are allowed", Toast.LENGTH_SHORT).show()
                    return ""
                }
            }
            // if the added text is valid, return source
            return source
        }

    }


}