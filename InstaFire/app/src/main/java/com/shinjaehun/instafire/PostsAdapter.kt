package com.shinjaehun.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shinjaehun.instafire.databinding.ItemPostBinding
import com.shinjaehun.instafire.models.Post
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter(private val context: Context, private val posts: List<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
//        return ViewHolder(view)

        // recycler view에서도 binding을 사용해보자!
//        val binding = ActivityPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // 계속 이게 실패했던 이유는 ItemPostBinding이 맞는거기 때문!
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    // 그러니까 이런식으로 view를 사용할 수 는 있는데 binding을 사용하고 싶어요.
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(post: Post) {
//            val tvUsername = itemView.findViewById<TextView>(R.id.tvUsername)
//            tvUsername.text = post.user?.username
//        }
//    }

//    class ViewHolder(binding: ActivityPostsBinding): RecyclerView.ViewHolder(binding.root) {
    inner class ViewHolder(binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root) {
        private val binding = binding
        fun bind(post: Post) {
            val username = post.user?.username as String
            binding.tvUsername.text = post.user?.username
            binding.tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(binding.ivPost)
            Glide.with(context).load(getProfileImageUrl(username)).into(binding.ivProfileImage)
            binding.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }

        private fun getProfileImageUrl(username: String): String {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(username.toByteArray())
            val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hex?d=identicon"
        }
    }
}