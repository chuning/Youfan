package com.youfan.app.android.data

import com.youfan.app.android.Db
import java.util.HashMap

data class EventCreateParams(val topic: String?, val start_ts: Long, val latitude: Float, val longitude: Float, val address: String?, val quota: Int, val userId: String?) {

    class Builder() {
        private var topic: String? = null
        private var start_ts: Long = 0
        private var latitude: Float = 0f
        private var longitude: Float = 0f
        private var address: String? = null
        private var quota: Int = 1

        fun topic(topic: String?): Builder {
            this.topic = topic
            return this
        }

        fun start_ts(start_ts: Long): Builder {
            this.start_ts = start_ts
            return this
        }

        fun longitude(longitude: Float): Builder {
            this.longitude = longitude
            return this
        }

        fun latitude(latitude: Float): Builder {
            this.latitude = latitude
            return this
        }

        fun address(address: String?): Builder {
            this.address = address
            return this
        }

        fun quota(quota: Int): Builder {
            this.quota = quota
            return this
        }

        fun build(): EventCreateParams {
            val topic = topic ?: throw IllegalArgumentException()
            val start_ts = start_ts
            val latitude = latitude
            val longitude = longitude
            val address = address ?: throw IllegalArgumentException()
            val quota = quota

            //YouFan userId
            val userId = Db.getUserId()
            return EventCreateParams(topic, start_ts, latitude, longitude, address, quota, userId)
        }

        fun areRequiredParamsFilled(): Boolean {
            return isTopicFilled() && isDateAndLocationFilled() && quota > 1
        }

        fun isTopicFilled(): Boolean {
            return !topic.isNullOrEmpty()
        }

        fun isDateAndLocationFilled() : Boolean {
            return start_ts > 0 && address != null
        }

    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("topic", topic)
        params.put("start_ts", start_ts)
        params.put("latitude", latitude)
        params.put("longitude", longitude)
        params.put("address", address)
        params.put("quota", quota)
        params.put("user_id", userId)
        return params
    }
}
