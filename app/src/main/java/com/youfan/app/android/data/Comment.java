package com.youfan.app.android.data;

public class Comment {
	public String id;
	public String content;
	public UserInfo postedBy;
	public EventResponse.Event inside;
	public String createdAt;

	public Comment(String content, UserInfo postedBy) {
		this.content = content;
		this.postedBy = postedBy;
	}
}