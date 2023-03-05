package com.shinjaehun.yelpclone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class RestaurantsAdapter(val context: Context, val restaurants: List<YelpRestaurant>)
    : RecyclerView.Adapter<RestaurantsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
//        return ViewHolder(view)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount() = restaurants.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvNumReviews: TextView = itemView.findViewById(R.id.tvNumReviews)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)


        fun bind(restaurant: YelpRestaurant) {
            tvName.text = restaurant.name
            ratingBar.rating = restaurant.rating.toFloat()
            tvNumReviews.text = "${restaurant.numReviews} Reviews"
            tvAddress.text = restaurant.location.address
            tvCategory.text = restaurant.categories[0].title
            tvDistance.text = restaurant.displayDistance()
            tvPrice.text = restaurant.price
            Glide.with(context).load(restaurant.imageUrl).apply(
                RequestOptions().transforms(
                CenterCrop(), RoundedCorners(20)
            )).into(imageView)

//            transforms() are deprecated on glide v4 (It may be warning. It works now.) I changed it like this.
//            Glide.with(context).load(restaurant.imageUrl).apply(RequestOptions().transform(MultiTransformation(CenterCrop(), RoundedCorners(20)))).into(itemView.imageView)

        }

    }
}
