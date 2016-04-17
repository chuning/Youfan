
package com.youfan.app.android.service;

import android.location.Location;

import com.youfan.app.android.data.AddCommentParams;
import com.youfan.app.android.data.AttendEventParams;
import com.youfan.app.android.data.CheckUserExistParams;
import com.youfan.app.android.Db;
import com.youfan.app.android.data.EventCreateParams;
import com.youfan.app.android.data.SignUpParams;
import com.youfan.app.android.data.AttendEventResponse;
import com.youfan.app.android.data.AttendUserResponse;
import com.youfan.app.android.data.CheckUserExistResponse;
import com.youfan.app.android.data.CommentAddedResponse;
import com.youfan.app.android.data.CommentResponse;
import com.youfan.app.android.data.CreateEventResponse;
import com.youfan.app.android.data.EventResponse;
import com.youfan.app.android.data.SignUpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class YouFanServices {
    private static final YouFanServices youFanService = new YouFanServices();
    private final String path = "http://youfan-sql-dev.wx32m6ieaf.us-east-1.elasticbeanstalk.com/";
    public YouFanApi youFanApi;

    private YouFanServices() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(path)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build();

        youFanApi = retrofit.create(YouFanApi.class);
    }

    public static YouFanServices getInstance() {
        return youFanService;
    }

    public Subscription nearbyEvents(final Double longitude, final Double latitude, Observer<EventResponse> makeResultsObserver){
        return youFanApi.nearbyEvents(longitude, latitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<EventResponse>() {
                    @Override
                    public void call(EventResponse eventResponse) {
                        //iterate through Event
                        for (EventResponse.Event event : eventResponse.results) {
                            float[] result = new float[1];
                            Location.distanceBetween(latitude, longitude, event.location.coordinates[0], event.location.coordinates[1], result);
                            event.distance = result[0];
                        }
                    }
                }).subscribe(makeResultsObserver);
    }

    public Subscription checkUserExist(CheckUserExistParams checkUserExistParams, Observer<CheckUserExistResponse> checkUserExistResponseObserver) {
        return youFanApi.checkUserExist(checkUserExistParams.getFacebookId(), checkUserExistParams.getFacebookAccessToken())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(checkUserExistResponseObserver);
    }

    public Subscription signUp(SignUpParams signUpParams, Observer<SignUpResponse> signUpResponseObserver) {
        RequestBody body =
            RequestBody.create(MediaType.parse("multipart/form-data"), signUpParams.getUserPhoto());

        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getFirstName());
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getLastName());
        RequestBody bio = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getBio());
        RequestBody facebookId = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getFacebookId());
        RequestBody facebookAccessToken = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getFacebookAccessToken());

        return youFanApi.signUp(body,firstName, lastName, bio, facebookId, facebookAccessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(signUpResponseObserver);
    }

    public Subscription editProfile(SignUpParams signUpParams, Observer<SignUpResponse> signUpResponseObserver) {
        RequestBody body =
            RequestBody.create(MediaType.parse("multipart/form-data"), signUpParams.getUserPhoto());

        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getFirstName());
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getLastName());
        RequestBody bio = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getBio());
        RequestBody facebookId = RequestBody.create(MediaType.parse("text/plain"), signUpParams.getFacebookId());
        RequestBody token = RequestBody.create(MediaType.parse("text/plain"), Db.getYouFanToken());

        return youFanApi.editProfile(body,firstName, lastName, bio, facebookId, token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(signUpResponseObserver);
    }

    public Subscription createEvent(String youFanToken, EventCreateParams eventCreateParams, Observer<CreateEventResponse> createEventResponseObserver) {
        return youFanApi.createEvent(youFanToken, eventCreateParams.toQueryMap())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createEventResponseObserver);
    }

    public Subscription getAttendUsers(String eventId, Observer<AttendUserResponse> getAttendUserObserver) {
        return youFanApi.getAttendUser(eventId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getAttendUserObserver);
    }

    public Subscription getComment(String eventId, Observer<CommentResponse> getCommentObserver) {
        return youFanApi.getComments(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getCommentObserver);
    }

    public Subscription addComment(String token, AddCommentParams addCommentParams, Observer<CommentAddedResponse> addCommentObserver) {
        return youFanApi.addComment(token, addCommentParams.toQueryMap())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(addCommentObserver);
    }

    public Subscription attendedEvents(String token, String userId, Observer<EventResponse> getAttendedEventObserver) {
        return youFanApi.getAttendedEvents(token, userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getAttendedEventObserver);
    }

    public Subscription hostedEvents(String token, String userId, Observer<EventResponse> getHostedEventsObserver) {
        return youFanApi.getHostedEvents(token, userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getHostedEventsObserver);
    }

    //join event
    public Subscription attendEvent(String token, AttendEventParams attendEventParams, Observer<AttendEventResponse> attendEventResponseObserver) {
        return youFanApi.attendEvent(token, attendEventParams.toQueryMap())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(attendEventResponseObserver);
    }

    //quit event
    public Subscription quitEvent(String token, AttendEventParams attendEventParams, Observer<AttendEventResponse> attendEventResponseObserver) {
        return youFanApi.quitEvent(token, attendEventParams.toQueryMap())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(attendEventResponseObserver);
    }
}
