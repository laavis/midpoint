package com.nopoint.midpoint

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nopoint.midpoint.models.*
import kotlinx.android.synthetic.main.request_header.view.*
import kotlinx.android.synthetic.main.request_row.view.*

class MeetingRequestsAdapter(
    private val requests: MutableList<MeetingRequestRow>,
    private val context: Context,
    private val respond: (meetingRequest: MeetingRequest) -> Unit,
    private val showOnMap: (meetingRequest: MeetingRequest) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    override fun getItemCount(): Int = requests.size

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
        val request = requests[position]
        if (request.rowType != RowType.HEADER) {
            when(request.type) {
                MeetingType.REJECTED -> holder.userName!!.text = context.getString(R.string.meeting_rejected, request.meetingRequest!!.receiverUsername)
                MeetingType.ACTIVE -> {
                    if (request.rowType == RowType.DELETABLE) {
                        holder.userName!!.text = context.getString(R.string.meeting_active, request.meetingRequest!!.receiverUsername)
                    } else {
                        holder.userName!!.text = context.getString(R.string.meeting_active, "You")
                    }
                    holder.meetBtn!!.text = context.getString(R.string.show_route)
                    holder.meetBtn.setOnClickListener { showOnMap(request.meetingRequest!!) }
                }
                MeetingType.INCOMING -> {
                    holder.userName!!.text = context.getString(R.string.meeting_incoming, request.meetingRequest!!.requesterUsername)
                    holder.meetBtn!!.setOnClickListener { respond(request.meetingRequest) }
                }
                MeetingType.OUTGOING -> {
                    holder.userName!!.text = context.getString(R.string.meeting_outgoing, request.meetingRequest!!.receiverUsername)
                    holder.meetBtn!!.visibility = View.GONE
                }
            }
            holder.timestamp!!.text = android.text.format.DateUtils.getRelativeTimeSpanString(request.meetingRequest?.timestamp?.time ?: 0)
        } else {
            holder.headerTxt!!.text = context.getString(R.string.meeting_heading, request.type.toString())
        }
    }

    override fun getItemViewType(position: Int) =
        requests[position].rowType.ordinal

    fun removeAt(position: Int): MeetingRequestRow {
        val removed = requests.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun addItem(row: MeetingRequestRow, position: Int) {
        requests.add(position, row)
        notifyItemInserted(position)
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val userName: TextView? = view.receiver_txt
    val meetBtn: Button? = view.meet_btn
    val timestamp: TextView? = view.timestamp_txt
    val headerTxt: TextView? = view.header_txt
}