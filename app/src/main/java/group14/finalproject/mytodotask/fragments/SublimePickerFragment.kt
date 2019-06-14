package group14.finalproject.mytodotask.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.appeaser.sublimepickerlibrary.SublimePicker
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import group14.finalproject.mytodotask.R

import java.text.DateFormat
import java.util.Locale
import java.util.TimeZone

class SublimePickerFragment : DialogFragment() {
    // Date & Time formatter used for formatting
    // text on the switcher button
    var mDateFormatter: DateFormat
    var mTimeFormatter: DateFormat

    // Picker
    lateinit var mSublimePicker: SublimePicker

    // Callback to activity
    var mCallback: Callback? = null

    var mListener: SublimeListenerAdapter = object : SublimeListenerAdapter() {
        override fun onCancelled() {
            mCallback?.onCancelled()

            // Should actually be called by activity inside `Callback.onCancelled()`
            dismiss()
        }

        override fun onDateTimeRecurrenceSet(
            sublimeMaterialPicker: SublimePicker?,
            selectedDate: SelectedDate?,
            hourOfDay: Int,
            minute: Int,
            recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?,
            recurrenceRule: String?
        ) {
            mCallback?.onDateTimeRecurrenceSet(
                selectedDate,
                hourOfDay, minute, recurrenceOption, recurrenceRule
            )

            // Should actually be called by activity inside `Callback.onCancelled()`
            dismiss()
        }
        // You can also override 'formatDate(Date)' & 'formatTime(Date)'
        // to supply custom formatters.
    }

    init {
        // Initialize formatters
        mDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
        mTimeFormatter.timeZone = TimeZone.getTimeZone("GMT+0")
    }

    // Set activity callback
    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*try {
            //getActivity().getLayoutInflater()
                    //.inflate(R.layout.sublime_recurrence_picker, new FrameLayout(getActivity()), true);
            getActivity().getLayoutInflater()
                    .inflate(R.layout.sublime_date_picker, new FrameLayout(getActivity()), true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }*/

        mSublimePicker = activity!!
            .layoutInflater.inflate(R.layout.sublime_picker, container) as SublimePicker

        // Retrieve SublimeOptions
        val arguments = arguments
        var options: SublimeOptions? = null

        // Options can be null, in which case, default
        // options are used.
        if (arguments != null) {
            options = arguments.getParcelable("SUBLIME_OPTIONS")
        }

        mSublimePicker.initializePicker(options, mListener)
        return mSublimePicker
    }

    // For communicating with the activity
    interface Callback {
        fun onCancelled()

        fun onDateTimeRecurrenceSet(
            selectedDate: SelectedDate?,
            hourOfDay: Int, minute: Int,
            recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?,
            recurrenceRule: String?
        )
    }
}