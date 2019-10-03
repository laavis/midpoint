package com.nopoint.midpoint

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nopoint.midpoint.models.FriendRequest
import kotlinx.android.synthetic.main.row_friend_search_results.view.*



class FriendSearchAdapter(
    private val searchResults: List<String>,
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FriendSearchAdapter.ViewHolder>(){


    override fun getItemCount(): Int {
        return searchResults.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.username.text = searchResults[position]
        holder.bind(itemClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_friend_search_results,parent,false))
    }


    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val username: TextView = v.friends_search_list_username
        val buttonSendFriendRequest: ImageButton = v.friends_button_send_friend_request

        fun bind(clickListener: OnItemClickListener) {
            itemView.setOnClickListener {
                clickListener.onItemClicked(buttonSendFriendRequest)
            }
        }
    }
}

interface OnItemClickListener{
    fun onItemClicked(button: ImageButton)
}