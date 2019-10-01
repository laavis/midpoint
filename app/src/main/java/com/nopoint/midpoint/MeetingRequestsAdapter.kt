package com.nopoint.midpoint

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nopoint.midpoint.models.MeetingRequest
import kotlinx.android.synthetic.main.request_row.view.*

class MeetingRequestsAdapter(
    private val requests: List<MeetingRequest>,
    private val context: Context,
    private val onRespond: (meetingRequest: MeetingRequest) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int = requests.size

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.request_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.userName.text = requests[position].receiver
        holder.meetBtn.setOnClickListener { onRespond(request) }
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val userName: TextView = view.receiver_txt
    val meetBtn: Button = view.meet_btn
}