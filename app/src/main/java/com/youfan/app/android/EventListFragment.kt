package com.youfan.app.android

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youfan.app.android.data.EventResponse
import rx.subjects.BehaviorSubject


class EventListFragment : Fragment() {
    val eventResultsObservable = BehaviorSubject.create<List<EventResponse.Event>>()
    val eventSelectedSubject = BehaviorSubject.create<EventResponse.Event>()

    companion object {
        @JvmStatic fun newInstance(): EventListFragment = EventListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.event_result, container, false) as View

        val recyclerView = view.findViewById(R.id.list_view) as RecyclerView
        val adapter = EventListAdapter(context, eventSelectedSubject)
        recyclerView.adapter = adapter
        eventResultsObservable.subscribe(adapter.resultsSubject)
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }
}