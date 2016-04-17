package com.youfan.app.android.data

import java.util.HashMap

data class AttendEventParams(val userId: String, val eventId: String){
    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("user_id", userId)
        params.put("event_id", eventId)
        return params
    }
}
