package com.nopoint.midpoint.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.MeetingsSingleton
import com.nopoint.midpoint.models.*
import kotlinx.android.synthetic.main.request_header.view.*
import kotlinx.android.synthetic.main.request_row.view.*
import org.jetbrains.anko.backgroundColor


class MeetingRequestsAdapter(
    private val context: Context,
    private val listener: MeetingRequestViewListener
) : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int = MeetingsSingleton.meetingRequestRows.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val inflatedView: View = when (viewType) {
            RowType.REQUEST.ordinal -> layoutInflater.inflate(R.layout.request_row, parent, false)
            RowType.DELETABLE.ordinal -> layoutInflater.inflate(R.layout.request_row, parent, false)
            else -> layoutInflater.inflate(R.layout.request_header, parent, false)
        }
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0){
            holder.topDivider?.visibility = View.GONE
        }
        val request = MeetingsSingleton.meetingRequestRows[position]
        if (request.rowType != RowType.HEADER) {
            if (request.type == MeetingType.OUTGOING || request.type == MeetingType.INCOMING) {
                holder.itemView.setOnClickListener {
                    // Get the current state of the item
                    val expanded = request.expanded
                    // Change the state
                    request.expanded = !expanded
                    // Notify the adapter that item has changed
                    notifyItemChanged(position)
                }
            } else if (request.type == MeetingType.ACTIVE) {
                holder.itemView.setOnClickListener {
                    listener.showOnMap(request.meetingRequest!!)
                }
            } else {
                holder.itemView.setOnClickListener { }
            }
            holder.icon?.visibility = View.GONE
            holder.secondaryButton?.visibility = View.GONE
            bind(request, holder)
        } else {
            holder.headerTxt!!.text = when(request.type){
                MeetingType.INCOMING -> context.getString(R.string.incoming)
                MeetingType.OUTGOING -> context.getString(R.string.outgoing)
                else -> context.getString(R.string.meeting_heading, request.type.toString())
            }
        }
    }

    private fun bind(request: MeetingRequestRow, holder: ViewHolder) {
        when (request.type) {
            // Currently not used
            MeetingType.REJECTED -> {
                if (request.rowType == RowType.DELETABLE) {
                    holder.userName!!.text = context.getString(
                        R.string.meeting_rejected,
                        request.meetingRequest!!.receiverUsername
                    )
                } else {
                    holder.userName!!.text = context.getString(R.string.meeting_rejected, "You")
                }
            }
            MeetingType.ACTIVE -> {
                if (request.rowType == RowType.DELETABLE) {
                    holder.userName!!.text = context.getString(
                        R.string.meeting_active,
                        request.meetingRequest?.receiverUsername
                    )
                } else {
                    holder.userName!!.text = context.getString(
                        R.string.meeting_active,
                        request.meetingRequest?.requesterUsername
                    )
                }
                holder.card?.setCardBackgroundColor(context.getColor(R.color.color_primary))
                holder.userName.setTextColor(context.getColor(R.color.color_white))
                holder.timestamp!!.setTextColor(context.getColor(R.color.color_white_accent))
            }
            MeetingType.INCOMING -> {
                holder.userName!!.text = context.getString(
                    R.string.meeting_incoming,
                    request.meetingRequest!!.requesterUsername
                )
                holder.primaryButton?.setOnClickListener { listener.acceptRequest(request.meetingRequest) }
                holder.secondaryButton?.setOnClickListener { listener.declineRequest(request.meetingRequest) }
                holder.secondaryButton?.visibility = View.VISIBLE
            }
            MeetingType.OUTGOING -> {
                holder.userName!!.text = context.getString(
                    R.string.meeting_outgoing,
                    request.meetingRequest!!.receiverUsername
                )
                holder.icon?.visibility = View.VISIBLE
                holder.primaryButton?.text = context.getString(R.string.cancel)
                holder.primaryButton?.backgroundColor = context.getColor(R.color.color_warning)
                holder.primaryButton?.setOnClickListener { listener.deleteRequest(request.meetingRequest) }
            }
        }
        holder.timestamp!!.text = android.text.format.DateUtils.getRelativeTimeSpanString(
            request.meetingRequest?.timestamp?.time ?: 0
        )
        if (request.expanded) {
            holder.expandedLayout?.visibility = View.VISIBLE
            holder.card?.elevation = 8.0f
        } else {
            holder.expandedLayout?.visibility = View.GONE
            holder.card?.elevation = 0f
        }
    }

    override fun getItemViewType(position: Int) = MeetingsSingleton.meetingRequestRows[position].rowType.ordinal

    fun removeAt(position: Int): MeetingRequestRow {
        val removed = MeetingsSingleton.meetingRequestRows.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun addItem(row: MeetingRequestRow, position: Int) {
        MeetingsSingleton.meetingRequestRows.add(position, row)
        notifyItemInserted(position)
    }
}

interface MeetingRequestViewListener {
    fun showOnMap(meetingRequest: MeetingRequest)
    fun acceptRequest(meetingRequest: MeetingRequest)
    fun declineRequest(meetingRequest: MeetingRequest)
    fun deleteRequest(meetingRequest: MeetingRequest)
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val userName: TextView? = view.receiver_txt
    val primaryButton: MaterialButton? = view.primary_btn
    val secondaryButton: MaterialButton? = view.secondary_btn
    val expandedLayout: ConstraintLayout? = view.expanded_layout
    val timestamp: TextView? = view.timestamp_txt
    val headerTxt: TextView? = view.header_txt
    val card: CardView? = view.card
    val icon: ImageView? = view.icon_view
    val topDivider: View? = view.header_divider
}