package com.nopoint.midpoint.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nopoint.midpoint.R
import com.nopoint.midpoint.models.UserSearchResponseUser
import kotlinx.android.synthetic.main.row_friend_search_results.view.*



class FriendSearchAdapter(
    private val searchResults: ArrayList<UserSearchResponseUser>,
    private val context: Context,
    private val sendReqBtnClickListener: OnSendFriendReqBtnClickListener
) : RecyclerView.Adapter<FriendSearchAdapter.UserSearchVH>(){


    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: UserSearchVH, position: Int) {
        holder.username.text = searchResults[position].username

        val isRequestSent = searchResults[position].isRequestSent == true
        val isFriend = searchResults[position].isFriend == true

        if (isFriend || isRequestSent) {
            holder.buttonSendFriendRequest.visibility = View.GONE

            if (isFriend) {
                holder.statusIcon.setImageResource(R.drawable.ic_status_accepted)
                holder.statusIcon.visibility = View.VISIBLE
            } else if (!isFriend && isRequestSent) {
                holder.statusIcon.visibility = View.VISIBLE
            }
        }

        holder.bind(sendReqBtnClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchVH {
        return UserSearchVH(LayoutInflater.from(context).inflate(R.layout.row_friend_search_results, parent, false)
        )
    }


    class UserSearchVH(v: View): RecyclerView.ViewHolder(v) {
        val username: TextView = v.friends_search_list_username
        val statusIcon: ImageView = v.friends_req_status
        val buttonSendFriendRequest: ImageButton = v.friends_button_send_friend_request

        fun bind(clickListener: OnSendFriendReqBtnClickListener) {
            buttonSendFriendRequest.setOnClickListener {
                clickListener.onItemClicked(buttonSendFriendRequest, statusIcon, adapterPosition)
            }
        }
    }
}

interface OnSendFriendReqBtnClickListener {
    fun onItemClicked(button: ImageButton, statusIcon: ImageView, position: Int)
}