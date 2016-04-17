package com.youfan.app.android

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.youfan.app.android.data.AddCommentParams
import com.youfan.app.android.data.Comment
import com.youfan.app.android.data.CommentAddedResponse
import com.youfan.app.android.service.YouFanServices
import com.youfan.app.android.util.AppUtil
import rx.Observer
import rx.subjects.BehaviorSubject

class EventCommentFragment : Fragment() {
    val commentResultsObservable = BehaviorSubject.create<List<Comment>>()
    val addCommentObservable = BehaviorSubject.create<Comment>()
    lateinit var noCommentView: TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.event_discuss_fragment, container, false) as View
        val recyclerView = view.findViewById(R.id.list_view) as RecyclerView
        val commentTextView = view.findViewById(R.id.add_comment_text) as EditText
        val sendButton = view.findViewById(R.id.send_comment) as Button
        noCommentView = view.findViewById(R.id.no_comment_text_view) as TextView

        val adapter = CommentListAdapter(context)
        recyclerView.adapter = adapter
        commentResultsObservable.subscribe(adapter.resultsSubject)
        commentResultsObservable.subscribe(noCommentObserver)
        addCommentObservable.subscribe(adapter.addCommentSubject)
        recyclerView.layoutManager = LinearLayoutManager(context)

        sendButton.setOnClickListener {
            if (!commentTextView.text.isNullOrEmpty()){
                val token = Db.getYouFanToken()
                val comment = commentTextView.text.toString()
                val addCommentParams = AddCommentParams(content = comment, user_id = Db.getUserId(), event_id = Db.getEvent().id)
                YouFanServices.getInstance().addComment(token, addCommentParams, object : Observer<CommentAddedResponse> {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, resources.getString(R.string.cannot_add), Toast.LENGTH_LONG)
                    }

                    override fun onNext(commentAddedResponse: CommentAddedResponse) {
                        commentTextView.text.clear()
                        AppUtil.hideKeyboard(activity)
                        val poster = Db.getUser().userInfo
                        val newComment = Comment(addCommentParams.content, poster)
                        addCommentObservable.onNext(newComment)
                        noCommentView.visibility = View.GONE
                    }
                })
            }
        }

        return view
    }

    val noCommentObserver: Observer<List<Comment>> = object : Observer<List<Comment>> {
        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            Log.d("error", e.message)
        }

        override fun onNext(comments: List<Comment>) {
            noCommentView.visibility = if (comments.size == 0) View.VISIBLE else View.GONE
        }
    }
}