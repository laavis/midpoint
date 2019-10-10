package com.nopoint.midpoint.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.row_friend_request.view.*
import kotlinx.android.synthetic.main.row_friends_list.view.*
import kotlinx.android.synthetic.main.row_friend_sent.view.*


class FriendsListAdapter(
    private val onFriendListActionClickListener: OnFriendListActionClickListener,
    private val rows: ArrayList<IRowFriend>,
    val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface IRowFriend
    class FriendRow(val username: String) : IRowFriend
    class SentFriendRequestRow(val username: String) : IRowFriend
    class ReceivedFriendRequestRow(val username: String) : IRowFriend

    inner class FriendVH(v: View): RecyclerView.ViewHolder(v) {
        val username: TextView = v.friends_list_row_username
        private val buttonAction: ImageButton = v.friends_list_row_action

        fun bind(clickListener: OnFriendListActionClickListener) {

            buttonAction.setOnClickListener {
                val popup = PopupMenu(context, buttonAction)
                val inflater = popup.menuInflater

                inflater.inflate(R.menu.friend_options_menu, popup.menu)
                popup.show()

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.friend_delete -> {
                            clickListener.onDeleteClicked(item, adapterPosition)
                        }

                    }
                    false
                }
            }
        }
    }

    inner class SentFriendRequestVH(v: View) : RecyclerView.ViewHolder(v) {
        val username: TextView = v.row_friend_sent_username
    }

    inner class ReceivedFriendRequestVH(v: View) : RecyclerView.ViewHolder(v) {
        val username: TextView = v.row_friend_request_username
        private val buttonAccept = v.row_friend_request_btn_accept!!
        private val buttonDeny = v.row_friend_request_btn_deny!!

        fun bind(clickListener: OnFriendListActionClickListener) {
            buttonAccept.setOnClickListener {
                clickListener.onAcceptClicked(buttonAccept, adapterPosition)
                removeItem(adapterPosition)
            }
            buttonDeny.setOnClickListener {
                clickListener.onDenyClicked(buttonDeny, adapterPosition)
                removeItem(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    override fun getItemViewType(position: Int): Int =
        when (rows[position]) {
            is FriendRow -> TYPE_FRIEND
            is SentFriendRequestRow -> TYPE_SENT_FRIEND_REQUEST
            is ReceivedFriendRequestRow -> TYPE_RECEIVED_FRIEND_REQUEST
            else -> throw IllegalArgumentException()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_FRIEND -> FriendVH(LayoutInflater.from(context).inflate(R.layout.row_friends_list, parent, false))
        TYPE_SENT_FRIEND_REQUEST -> SentFriendRequestVH(LayoutInflater.from(context).inflate(R.layout.row_friend_sent, parent, false))
        TYPE_RECEIVED_FRIEND_REQUEST -> ReceivedFriendRequestVH(LayoutInflater.from(context).inflate(R.layout.row_friend_request, parent, false))
        else -> throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            TYPE_FRIEND -> {
                onBindFriend(holder, rows[position] as FriendRow)
            }
            TYPE_SENT_FRIEND_REQUEST -> {
                onBindSentFriendRequest(holder, rows[position] as SentFriendRequestRow)
            }
            TYPE_RECEIVED_FRIEND_REQUEST -> {
                onBindReceivedFriendRequest(holder, rows[position] as ReceivedFriendRequestRow)
            }
            else -> throw IllegalArgumentException()

        }

    private fun onBindFriend(holder: RecyclerView.ViewHolder, row: FriendRow) {
        val friendRow = holder as FriendVH
        holder.bind(onFriendListActionClickListener)
        friendRow.username.text = row.username
    }

    private fun onBindSentFriendRequest(holder: RecyclerView.ViewHolder, row: SentFriendRequestRow) {
        val sentFriendRequestRow = holder as SentFriendRequestVH
        sentFriendRequestRow.username.text = row.username
    }

    private fun onBindReceivedFriendRequest(holder: RecyclerView.ViewHolder, row: ReceivedFriendRequestRow) {
        val receivedFriendRequestRow = holder as ReceivedFriendRequestVH
        holder.bind(onFriendListActionClickListener)
        receivedFriendRequestRow.username.text = row.username
    }

    private fun removeItem(position: Int) {
        rows.removeAt(position)
        notifyDataSetChanged()
    }

    companion object {
        private const val TYPE_FRIEND = 0
        private const val TYPE_SENT_FRIEND_REQUEST = 1
        private const val TYPE_RECEIVED_FRIEND_REQUEST = 2
    }
}

interface OnFriendListActionClickListener {
    fun onAcceptClicked(button: MaterialButton, position: Int)
    fun onDenyClicked(button: MaterialButton, position: Int)
    fun onDeleteClicked(menuItem: MenuItem, position: Int)
}


