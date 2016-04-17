
package com.youfan.app.android

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.youfan.app.android.data.EventResponse
import com.youfan.app.android.util.AppUtil
import com.youfan.app.android.util.RoundedTransformation
import rx.subjects.BehaviorSubject
import java.util.ArrayList

/*
  disable mapview
 */
class EventListAdapterV2(val context: Context, val eventSelectedSubject: BehaviorSubject<EventResponse.Event>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var events: List<EventResponse.Event> = emptyList()
    val resultsSubject = BehaviorSubject.create<List<EventResponse.Event>>()

    init {
        resultsSubject.subscribe {
            events = ArrayList(it)
            notifyDataSetChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_cell_v2, parent, false)
        return EventViewHolder(view as ViewGroup, parent.width)
    }

    override fun getItemCount(): Int {
        return events.size;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is EventListAdapterV2.EventViewHolder) {
            holder.bind(events[position])
        }
    }

    inner class EventViewHolder(root: ViewGroup, val width: Int): RecyclerView.ViewHolder(root), View.OnClickListener {
        val profilePictureView = root.findViewById(R.id.profile_picture) as ImageView
        val hostView = root.findViewById(R.id.host_name) as TextView
        val topicView = root.findViewById(R.id.topic) as TextView
        val eventAddressView = root.findViewById(R.id.event_address) as TextView
        val eventDistanceView = root.findViewById(R.id.event_distance) as TextView
//        val quotaView = root.findViewById(R.id.num_of_people) as TextView
        val eventDateView = root.findViewById(R.id.event_date) as TextView
        val eventTimeView = root.findViewById(R.id.event_time) as TextView
        val hostBioView = root.findViewById(R.id.host_bio) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(event: EventResponse.Event) {
            if (event.hostedBy != null) {
                Picasso.with(context)
                        .load(Uri.parse(event.hostedBy.photoUrl))
                        .transform(RoundedTransformation())
                        .resizeDimen(R.dimen.event_cell_profile_image_size, R.dimen.event_cell_profile_image_size)
                        .centerCrop()
                        .into(profilePictureView)
            }

            //local time at that place
            val time = AppUtil.parseToDateTime(event.start)
            eventDateView.text = AppUtil.dateTimeToMMMd(time)
            eventTimeView.text = " · " + AppUtil.dateTimeTohhmmaE(time)
            topicView.text = getShortAddress(event.address) + ": " +event.topic
            hostView.text = event.hostedBy.firstName
            eventAddressView.text = getShortAddress(event.address)
            eventDistanceView.text = String.format("%.1f", event.distance * .6214e-3) + " mile"
//            quotaView.text = "限" + event.quota.toString() + "人"
            hostBioView.text = event.hostedBy.bio
        }

        override fun onClick(p0: View?) {
            val event: EventResponse.Event = events[adapterPosition]
            eventSelectedSubject.onNext(event)
        }

        fun getShortAddress(address: String): String {
            return address.split(",")[0]
        }

    }
}