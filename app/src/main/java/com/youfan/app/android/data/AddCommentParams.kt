package com.youfan.app.android.data

import java.util.HashMap

data class AddCommentParams(val content: String, val user_id: String, val event_id: String) {
    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("content", content)
        params.put("user_id", user_id)
        params.put("event_id", event_id)
        return params
    }
}
