package io.github.takusan23.Kaisendon.CustomMenu.Dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import io.github.takusan23.Kaisendon.Activity.AccountInfoUpdateActivity
import java.util.*

class CalenderDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

// dateBulderを返す
        return DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            var month = month
            var month_string = ""
            var day_string = ""
            //1-9月は前に0を入れる
            if (month++ <= 9) {
                month_string = "0" + month++.toString()
            } else {
                month_string = month++.toString()
            }
            //1-9日も前に0を入れる
            if (dayOfMonth <= 9) {
                day_string = "0$dayOfMonth"
            } else {
                day_string = dayOfMonth.toString()
            }
            val date = "$year-$month_string-$day_string"
            val iso8601 = "$year$month_string$day_string+0900"
            //もーど
            if (arguments!!.getString("type") == "birthday") {
                AccountInfoUpdateActivity.birthday_Button.text = date
            } else if (arguments!!.getString("type") == "toot_time") {
                //Home.mastodon_time_post_TextView.setText(iso8601);
            }
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}
