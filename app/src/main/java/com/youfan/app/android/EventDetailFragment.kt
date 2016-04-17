package com.youfan.app.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.youfan.app.android.data.Comment
import com.youfan.app.android.data.EventResponse
import com.youfan.app.android.data.UserInfo
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.squareup.picasso.Picasso
import com.youfan.app.android.util.AppUtil
import com.youfan.app.android.util.RoundedTransformation
import rx.Observer
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class EventDetailFragment : Fragment(), OnMapReadyCallback {
    lateinit var profilePictureView: ImageView
    lateinit var hostName: TextView
    lateinit var hostBio: TextView
    lateinit var startTime: TextView
    lateinit var address: TextView
    lateinit var topic: TextView
    lateinit var attendantLayout: GridLayout
    lateinit var event: EventResponse.Event
    private var googleMap: GoogleMap? = null
    lateinit var peopleAttendingTextView: TextView

    val attendUserObservable = BehaviorSubject.create<List<UserInfo>>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.event_detail_fragment, container, false) as View

        profilePictureView = view.findViewById(R.id.profile_picture) as ImageView
        hostName = view.findViewById(R.id.host_name) as TextView
        hostBio = view.findViewById(R.id.host_bio) as TextView
        startTime = view.findViewById(R.id.detail_start_time) as TextView
        address = view.findViewById(R.id.detail_address) as TextView
        topic = view.findViewById(R.id.detail_topic) as TextView
        attendantLayout = view.findViewById(R.id.attendant_container) as GridLayout
        peopleAttendingTextView = view.findViewById(R.id.show_people_attending) as TextView

        bind()
        attendUserObservable.subscribe(attendUserObserver)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    fun bind() {
        event = Db.getEvent()
        Picasso.with(context)
                .load(Uri.parse(event.hostedBy.photoUrl))
                .transform(RoundedTransformation())
                .resizeDimen(R.dimen.event_detail_profile_image_size, R.dimen.event_detail_profile_image_size)
                .centerCrop()
                .into(profilePictureView)
        hostName.text = event.hostedBy.firstName + " " + event.hostedBy.lastName
        hostBio.text = event.hostedBy.bio
        val dateTime = AppUtil.parseToDateTime(event.start)
        startTime.text = AppUtil.dateTimeToMMMdhhmmaE(dateTime)
        address.text = event.address
        topic.text = event.topic
    }

    override fun onMapReady(map: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = map
        googleMap?.addMarker(MarkerOptions().position(LatLng(event.location.coordinates[0], event.location.coordinates[1])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.setAllGesturesEnabled(false)
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(event.location.coordinates[0], event.location.coordinates[1]), 17f))

        googleMap?.setOnMapClickListener {
            val intent = Intent(this.activity, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    fun addAttendant(viewGroup: ViewGroup, attendantList: List<UserInfo>) {
        viewGroup.removeAllViews()
        val host = event.hostedBy
        var hostAndAttendUserList = ArrayList<UserInfo>()
        hostAndAttendUserList.add(host)
        hostAndAttendUserList.addAll(attendantList)

        for (attendant in hostAndAttendUserList) {
            val attendantLayout = LayoutInflater.from(viewGroup.context).inflate(R.layout.event_attendant_cell, null) as EventAttendantView
            attendantLayout.bind(attendant)
            val columnNum = 5
            attendantLayout.layoutParams = ViewGroup.LayoutParams(viewGroup.getContext().getResources().getDisplayMetrics().widthPixels / columnNum, ViewGroup.LayoutParams.WRAP_CONTENT)
            viewGroup.addView(attendantLayout)
        }

        if (hostAndAttendUserList.size >= Db.getEvent().quota) {
            peopleAttendingTextView.text = resources.getString(R.string.attended_people_full)
        } else {
            peopleAttendingTextView.text = resources.getString(R.string.attended_people_template, hostAndAttendUserList.size)
        }
    }

    val attendUserObserver: Observer<List<UserInfo>> = object : Observer<List<UserInfo>> {
        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
        }

        override fun onNext(attendants: List<UserInfo>) {
            addAttendant(attendantLayout, attendants)
        }
    }
}