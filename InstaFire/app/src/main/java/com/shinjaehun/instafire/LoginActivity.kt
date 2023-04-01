package com.shinjaehun.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.shinjaehun.instafire.databinding.ActivityLoginBinding

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase authentication check
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) { // 한번 로그인 성공했으면 다음 앱을 실행할 때는 바로 PostsActivity로...
            goPostsActivity()
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false // 이거 해제해놔야 여려번 로그인 버튼 클릭해서 생기는 문제 방지 가능

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "email/password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                binding.btnLogin.isEnabled = true // 로그인 시도 이후에는 다시 활성화시켜 놓으라고?
                if (task.isSuccessful) {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    goPostsActivity()
                } else {
                    Log.e(TAG, "signInWithEmail failed", task.exception)
                    Toast.makeText(this, "authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goPostsActivity() {
        Log.i(TAG, "goPostsActivity")
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}