package com.youfan.app.android

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.youfan.app.android.data.EventResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import com.youfan.app.android.data.Location
import com.youfan.app.android.util.AppUtil
import com.youfan.app.android.util.RoundedTransformation
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class EventListAdapter(val context: Context, val eventSelectedSubject: BehaviorSubject<EventResponse.Event>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var events: List<EventResponse.Event> = emptyList()
    val resultsSubject = BehaviorSubject.create<List<EventResponse.Event>>()

    init {
        resultsSubject.subscribe {
            events = ArrayList(it)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_cell, parent, false)
        return EventViewHolder(view as ViewGroup)
    }

    override fun getItemCount(): Int {
        return events.size;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is EventListAdapter.EventViewHolder) {
            holder.bind(events[position])
        }
    }

    inner class EventViewHolder(root: ViewGroup): RecyclerView.ViewHolder(root), View.OnClickListener, OnMapReadyCallback {
        val profilePictureView = root.findViewById(R.id.profile_picture) as ImageView
        val hostView = root.findViewById(R.id.host_name) as TextView
        val topicView = root.findViewById(R.id.topic) as TextView
        val eventAddressView = root.findViewById(R.id.event_address) as TextView
        val eventDistanceView = root.findViewById(R.id.event_distance) as TextView
        val quotaView = root.findViewById(R.id.num_of_people) as TextView
        val eventDateView = root.findViewById(R.id.event_date) as TextView
        val eventTimeView = root.findViewById(R.id.event_time) as TextView
        val mapView = root.findViewById(R.id.mapview) as MapView
        var googleMap: GoogleMap? = null
        var location: Location? = null

        init {
            itemView.setOnClickListener(this)
            mapView.onCreate(null)
            mapView.getMapAsync(this)
            mapView.isClickable = false
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
            eventTimeView.text = AppUtil.dateTimeTohhmmaE(time)
            topicView.text = event.topic
            hostView.text = event.hostedBy.firstName
            eventAddressView.text = getShortAddress(event.address)
            eventDistanceView.text = String.format("%.1f", event.distance * .6214e-3) + context.resources.getString(R.string.mile)
            quotaView.text = event.quota.toString() + context.resources.getString(R.string.people)
            location = Location(event.location.coordinates[0], event.location.coordinates[1])

            if (googleMap != null ){
                updateMapContents()
            }
        }

        override fun onClick(p0: View?) {
            val event: EventResponse.Event = events[adapterPosition]
            eventSelectedSubject.onNext(event)
        }

        override fun onMapReady(map: GoogleMap?) {
            googleMap = map
            MapsInitializer.initialize(context)
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.setAllGesturesEnabled(false)
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

            if (location != null) {
                updateMapContents()
            }
        }

        fun getShortAddress(address: String): String {
            return address.split(",")[0]
        }

        fun updateMapContents() {
            googleMap?.clear()

            val marker = MarkerOptions().position(LatLng(location!!.lat, location!!.lng))
            val bd = BitmapDescriptorFactory.fromResource(R.drawable.food_bowl)
            marker.icon(bd).alpha(0.5f)
            googleMap?.addMarker(marker)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.lat, location!!.lng), 15f))
        }
    }
}