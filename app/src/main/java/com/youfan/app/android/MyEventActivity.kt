package com.youfan.app.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.youfan.app.android.data.EventResponse
import com.youfan.app.android.service.YouFanServices
import com.youfan.app.android.util.Constants
import rx.Observer
import rx.subjects.BehaviorSubject

class MyEventActivity: Activity() {
    val eventResultsObservable = BehaviorSubject.create<List<EventResponse.Event>>()
    val eventSelectedSubject = BehaviorSubject.create<EventResponse.Event>()
    var requestType: Int = 0
    lateinit var noEventView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_event_list)

        val toolbarTitle =  findViewById(R.id.toolbar_title) as TextView
        noEventView = findViewById(R.id.no_event_text_view) as TextView

        val extras = intent.extras
        if (extras != null) {
            requestType = extras.getInt(Constants.MY_EVENT)
        }

        val token = Db.getYouFanToken()
        val userId = Db.getUserId()
        if (requestType == Constants.EVENT_CREATED_REQUEST_CODE) {
            toolbarTitle.setText(R.string.party_initiated)
            YouFanServices.getInstance().hostedEvents(token, userId, makeResultsObserver())

        } else if (requestType == Constants.EVENT_JOINED_REQUEST_CODE) {
            toolbarTitle.setText(R.string.party_joined)
            YouFanServices.getInstance().attendedEvents(token, userId, makeResultsObserver())
        }

        val recyclerView = findViewById(R.id.list_view) as RecyclerView
        val adapter = EventListAdapter(this, eventSelectedSubject)
        recyclerView.adapter = adapter
        eventResultsObservable.subscribe(adapter.resultsSubject)
        eventResultsObservable.subscribe(noEventObserver)
        eventSelectedSubject.subscribe(selectEventObserver)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun makeResultsObserver(): Observer<EventResponse> {
        return object : Observer<EventResponse> {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable) {
            }

            override fun onNext(eventResponse: EventResponse) {
                eventResultsObservable.onNext(eventResponse.results)
            }
        }
    }

    val noEventObserver: Observer<List<EventResponse.Event>> = object : Observer<List<EventResponse.Event>> {
        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            Log.d("error", e.message)
        }

        override fun onNext(events: List<EventResponse.Event>) {
            noEventView.visibility = if (events.size == 0) View.VISIBLE else View.GONE
        }
    }

    val selectEventObserver: Observer<EventResponse.Event> = object: Observer<EventResponse.Event>{
            override fun onCompleted() {
            }

            override fun onError(e: Throwable) {
                Log.d("error", e.message)
            }

            override fun onNext(event: EventResponse.Event) {
                Db.setEvent(event)
                val intent = Intent(this@MyEventActivity, EventDetailActivity::class.java)
                intent.putExtra(Constants.EVENT_ID, event.id)
                startActivity(intent)
            }

    }
}

