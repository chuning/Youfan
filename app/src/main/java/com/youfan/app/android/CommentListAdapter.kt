package com.youfan.app.android

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.youfan.app.android.data.Comment
import com.squareup.picasso.Picasso
import com.youfan.app.android.util.AppUtil
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class CommentListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var comments: ArrayList<Comment> = ArrayList()
    val resultsSubject = BehaviorSubject.create<List<Comment>>()
    val addCommentSubject = PublishSubject.create<Comment>()

    init {
        resultsSubject.subscribe {
            comments = ArrayList(it)
            notifyDataSetChanged()
        }

        addCommentSubject.subscribe { newComment ->
            comments.add(newComment)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_cell, parent, false)
        return CommentViewHolder(view as ViewGroup, parent.width)
    }

    override fun getItemCount(): Int {
        return comments.size;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is CommentListAdapter.CommentViewHolder) {
            holder.bind(comments[position])
        }
    }

    inner class CommentViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root) {
        val commenterPictureView = root.findViewById(R.id.comment_user_picture) as ImageView
        val commenterNameView = root.findViewById(R.id.comment_user_name) as TextView
        val commentsInfoView = root.findViewById(R.id.comment_detail) as TextView
        val commentsTimeView = root.findViewById(R.id.comment_time) as TextView

        fun bind(comment: Comment) {
            if (comment.postedBy != null){
                Picasso.with(context)
                        .load(Uri.parse(comment.postedBy?.photoUrl?:""))
                        .placeholder(R.drawable.ic_action_name)
                        .error(R.drawable.ic_action_name)
                        .into(commenterPictureView);
                commenterNameView.text = comment.postedBy.firstName + " " + comment.postedBy.lastName
            } else {
                Picasso.with(context)
                        .load(R.drawable.ic_action_name)
                        .into(commenterPictureView);
                commenterNameView.text = ""
            }
            commentsInfoView.text = comment.content
            if (!comment.createdAt.isNullOrEmpty()) {
                val dateTime = AppUtil.parseToDateTime(comment.createdAt)
                commentsTimeView.text = AppUtil.dateTimeToMMMdhhmma(dateTime)
            } else {
                commentsTimeView.text= ""
            }
        }
    }
}