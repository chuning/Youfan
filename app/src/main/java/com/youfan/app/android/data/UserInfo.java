package com.youfan.app.android.data;

public class UserInfo {
	public String id;
	public String photoUrl;
	public String firstName;
	public String lastName;
	public String bio;

	public UserInfo(){

	}

	public UserInfo(String photoUrl, String firstName, String lastName) {
		this.photoUrl = photoUrl;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public UserInfo(String photoUrl, String firstName, String lastName, String bio) {
		this.photoUrl = photoUrl;
		this.firstName = firstName;
		this.lastName = lastName;
		this.bio = bio;
	}
}
