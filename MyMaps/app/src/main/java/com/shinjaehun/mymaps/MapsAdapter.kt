package com.shinjaehun.mymaps

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shinjaehun.mymaps.models.UserMap

private const val TAG = "MapsAdapter"

class MapsAdapter(val context: Context, val userMaps: List<UserMap>, val onClickListener: OnClickListener)
    : RecyclerView.Adapter<MapsAdapter.ViewHolder>() {
//class MapsAdapter(val context: Context, val userMaps: List<UserMap>)
//    : RecyclerView.Adapter<MapsAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_map, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = userMaps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userMap = userMaps[position]

        holder.itemView.setOnClickListener {
            Log.i(TAG, "Tapped on position $position")
            onClickListener.onItemClick(position)
        }

//        val textViewTitle = holder.itemView.findViewById<TextView>(android.R.id.text1)
        val textViewTitle = holder.itemView.findViewById<TextView>(R.id.tvMapTitle)
        textViewTitle.text = userMap.title
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
