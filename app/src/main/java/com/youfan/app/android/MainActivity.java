package com.youfan.app.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.youfan.app.android.data.CreateEventResponse;
import com.youfan.app.android.data.EventResponse;
import com.youfan.app.android.service.YouFanServices;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.youfan.app.android.util.Constants;

import rx.Observer;
import rx.subjects.PublishSubject;


public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    private AboutMeFragment aboutMeFragment;
    private EventListFragment eventListFragment;

    private YouFanServices youFanServices;
    private PublishSubject<Boolean> nearbyEventsObservable = PublishSubject.create();

    protected GoogleApiClient mGoogleApiClient;

    protected Location lastLocation;
    protected Double latitude;
    protected Double longitude;
    private ImageButton eventListButton;
    private ImageButton newEventButton;
    private ImageButton aboutMeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        eventListButton = (ImageButton)findViewById(R.id.main_event_list);
        newEventButton = (ImageButton)findViewById(R.id.main_start_event);
        aboutMeButton = (ImageButton)findViewById(R.id.main_me);

        eventListButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.navigationBarSelect));
        eventListButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), android.R.color.white));

        aboutMeFragment = AboutMeFragment.newInstance();
        eventListFragment = EventListFragment.newInstance();
        eventListFragment.getEventSelectedSubject().subscribe(selectEventObserver());

        if (savedInstanceState == null) {
            Db.loadUserAndYouFanTokenFromDisk(this);
            Db.loadFacebookIdFromDisk(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_layout, eventListFragment, "EventList")
                    .commit();
        } else {
            getSupportFragmentManager()
                    .findFragmentById(R.id.root_layout);
        }

        youFanServices = YouFanServices.getInstance();
        nearbyEventsObservable.subscribe(nearbyEventsObserver());

        //get current location.
        buildGoogleApiClient();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        } else {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        findNearbyEvents();
    }

    protected void findNearbyEvents() {
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }
        nearbyEventsObservable.onNext(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                // If permission is granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }
                findNearbyEvents();
            }
        }
    }

    public Observer<Boolean> nearbyEventsObserver() {
        return new Observer<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.d("error", e.getMessage());
            }

            @Override
            public void onNext(Boolean isSearch) {
                youFanServices.nearbyEvents(longitude, latitude, makeResultsObserver());
            }
        };
    }

    public Observer<EventResponse> makeResultsObserver() {
        return new Observer<EventResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(EventResponse eventResponse) {
                eventListFragment.getEventResultsObservable().onNext(eventResponse.results);
            }
        };
    }

    public Observer<EventResponse.Event> selectEventObserver() {
        return new Observer<EventResponse.Event>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.d("error", e.getMessage());
            }

            @Override
            public void onNext(EventResponse.Event event) {
                Db.setEvent(event);
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra(Constants.EVENT_ID, event.id);
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                youFanServices.createEvent(Db.getYouFanToken(), Db.getEventCreateParams(), new Observer<CreateEventResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("create event:", "error");
                    }

                    @Override
                    public void onNext(CreateEventResponse createEventResponse) {
                        Log.d("create event:", "next");
                        nearbyEventsObservable.onNext(true);
                    }
                });
            }
        }
    }

    public void openEventList(View view) {
        changeNavColor(0);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root_layout, eventListFragment, "EventList")
                .commit();
    }

    public void openNewEvent(View view) {
        changeNavColor(1);
        Intent intent = new Intent(this, NewEventActivity.class);
        startActivityForResult(intent, Constants.CREATE_EVENT_REQUEST_CODE);
    }

    public void openAboutMe(View view) {
        changeNavColor(2);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root_layout, aboutMeFragment, "aboutMe")
                .commit();
    }

    private void changeNavColor(int i) {
        switch (i) {
            case 0:
                clearNavColor();
                eventListButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.navigationBarSelect));
                eventListButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.navigationImageSelect));
                break;
            case 1:
                break;
            case 2:
                clearNavColor();
                aboutMeButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.navigationBarSelect));
                aboutMeButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.navigationImageSelect));
        }
    }

    private void clearNavColor() {
        eventListButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.navigationBar));
        eventListButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.navigationImageUnselect));
        aboutMeButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.navigationBar));
        aboutMeButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.navigationImageUnselect));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
