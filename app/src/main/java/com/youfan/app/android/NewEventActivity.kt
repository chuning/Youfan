package com.youfan.app.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.*
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.youfan.app.android.data.EventCreateParams
import com.youfan.app.android.util.AppUtil
import com.youfan.app.android.util.Constants
import java.text.SimpleDateFormat
import java.util.Date

class NewEventActivity : AppCompatActivity() {
    private val mFormatter = SimpleDateFormat("MMM dd, hh:mm aa")
    lateinit var toolbar: Toolbar
    lateinit var setTopicEditText: EditText
    lateinit var setTimeButton: Button
    lateinit var setLocationButton: Button
    lateinit var setPeopleLimit: EditText
    lateinit var overlay: FrameLayout

    var eventCreateParamsBuilder = EventCreateParams.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_new_fan_ju)

        overlay = findViewById(R.id.overlay) as FrameLayout
        setTopicEditText = findViewById(R.id.start_party) as EditText
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setTimeButton = findViewById(R.id.start_time) as Button
        setLocationButton = findViewById(R.id.choose_restaurant) as Button
        setPeopleLimit = findViewById(R.id.enter_people_limit) as EditText

        val doneButton = findViewById(R.id.done_button) as TextView
        doneButton.setOnClickListener {
            if (isLogin()) {
                eventCreateParamsBuilder.topic(setTopicEditText.text.toString())
                eventCreateParamsBuilder.quota(if (setPeopleLimit.text.isNullOrEmpty()) 0  else setPeopleLimit.text.toString().toInt())

                if (eventCreateParamsBuilder.areRequiredParamsFilled()) {
                    Db.setEventCreateParams(eventCreateParamsBuilder.build())
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    val toast = Toast.makeText(applicationContext, resources.getString(R.string.missing_field), Toast.LENGTH_SHORT)
                    toast.show()
                }
            } else {
                startLoginActivity()
            }
        }

        setTimeButton.setOnClickListener {
            SlideDateTimePicker.Builder(supportFragmentManager).setListener(setTimeListener)
                    .setInitialDate(Date())
                    .setMinDate(Date())
                    //.setIs24HourTime(true)
                    //.setTheme(SlideDateTimePicker.HOLO_DARK)
                    //.setIndicatorColor(Color.parseColor("#990000"))
                    .build()
                    .show();
        }

        setLocationButton.setOnClickListener {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this)
                startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: GooglePlayServicesRepairableException) {
            } catch (e: GooglePlayServicesNotAvailableException) {
            }

        }

        overlay.visibility = if (isLogin()) View.GONE else View.VISIBLE
        overlay.setOnClickListener { l ->
            startLoginActivity()
        }
    }

    private val setTimeListener = object : SlideDateTimeListener() {
        override fun onDateTimeSet(date: Date) {
            setTimeButton.text = mFormatter.format(date)
            eventCreateParamsBuilder.start_ts(date.time.toLong()/1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                setLocationButton.text = place.name
                val address = place.name.toString() + ", " + place.address.toString()
                eventCreateParamsBuilder.address(address)
                eventCreateParamsBuilder.latitude(place.latLng.latitude.toFloat())
                eventCreateParamsBuilder.longitude(place.latLng.longitude.toFloat())

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // TODO: Handle autocomplete error.

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == Constants.CHECK_USER_EXIST_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlay.visibility = if (isLogin()) View.GONE else View.VISIBLE
    }

    fun isLogin(): Boolean {
        return AppUtil.isLogin()
    }

    fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, Constants.CHECK_USER_EXIST_REQUEST_CODE)
    }
}