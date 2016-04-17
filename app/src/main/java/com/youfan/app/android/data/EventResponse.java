package com.youfan.app.android.data;

import java.util.List;

/**
 * Created by chuluo on 2/27/16.
 */
public class EventResponse {
    public List<Event> results;

    public class Event {
        public String id;
        public String topic;
        public String address;
        public Location location;
        public int quota;

        public transient float distance;

        public String start;
        public String createdAt;

        public UserInfo hostedBy;
    }

    public class Location {
        public String type;
        public double[] coordinates;
    }
}
