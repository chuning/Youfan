package com.youfan.app.android.service;

import com.youfan.app.android.data.AttendEventResponse;
import com.youfan.app.android.data.AttendUserResponse;
import com.youfan.app.android.data.CheckUserExistResponse;
import com.youfan.app.android.data.CommentAddedResponse;
import com.youfan.app.android.data.CommentResponse;
import com.youfan.app.android.data.CreateEventResponse;
import com.youfan.app.android.data.EventResponse;
import com.youfan.app.android.data.SignUpResponse;

import java.util.Map;


import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


public interface YouFanApi {
	@GET("/api/v1/events")
	Observable<EventResponse> nearbyEvents(
		@Query("longitude") Double longitude,
		@Query("latitude") Double latitude
	);

	@FormUrlEncoded
	@POST("/api/v1/events")
	Observable<CreateEventResponse> createEvent(
		@Header("X-Access-Token") String youFanToken,
		@FieldMap Map<String, Object> queryParams
	);

	@Multipart
	@POST("/api/v1/users/signup")
	Observable<SignUpResponse> signUp(
		@Part("userPhoto\"; filename=\"p.png\" ") RequestBody photo,
		@Part("firstName") RequestBody firstName,
		@Part("lastName") RequestBody lastName,
		@Part("bio") RequestBody bio,
		@Part("facebookId") RequestBody facebookId,
		@Part("facebookAccessToken") RequestBody facebookAccessToken
	);

	@Multipart
	@POST("/api/v1/users/signup")
	Observable<SignUpResponse> editProfile(
		@Part("userPhoto\"; filename=\"p.png\" ") RequestBody photo,
		@Part("firstName") RequestBody firstName,
		@Part("lastName") RequestBody lastName,
		@Part("bio") RequestBody bio,
		@Part("facebookId") RequestBody facebookId,
		@Part("token") RequestBody token
		);

	@GET("/api/v1/users/check_is_existing_user")
	Observable<CheckUserExistResponse> checkUserExist(
		@Query("facebookId") String facebookId,
		@Query("facebookAccessToken") String facebookAccessToken
	);

	@GET("api/v1/events/{event}/attend_users")
	Observable<AttendUserResponse> getAttendUser(@Path("event") String eventId);

	@GET("api/v1/events/{event}/comments")
	Observable<CommentResponse> getComments(@Path("event") String eventId);

	@FormUrlEncoded
	@POST("/api/v1/comments")
	Observable<CommentAddedResponse> addComment(
		@Header("X-Access-Token") String youFanToken,
		@FieldMap Map<String, Object> queryParams
	);

	@GET("api/v1/users/{user}/attend_events")
	Observable<EventResponse> getAttendedEvents(
		@Header("X-Access-Token") String youFanToken,
		@Path("user") String userId
	);

	@GET("api/v1/users/{user}/host_events")
	Observable<EventResponse> getHostedEvents(
		@Header("X-Access-Token") String youFanToken,
		@Path("user") String userId
	);

	@FormUrlEncoded
	@POST("/api/v1/attend")
	Observable<AttendEventResponse> attendEvent(
		@Header("X-Access-Token") String youFanToken,
		@FieldMap Map<String, Object> queryParams
	);

	@FormUrlEncoded
	@POST("/api/v1/unattend")
	Observable<AttendEventResponse> quitEvent(
		@Header("X-Access-Token") String youFanToken,
		@FieldMap Map<String, Object> queryParams
	);
}
