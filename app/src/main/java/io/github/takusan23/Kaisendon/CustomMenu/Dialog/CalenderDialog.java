package io.github.takusan23.Kaisendon.CustomMenu.Dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

import io.github.takusan23.Kaisendon.Activity.AccountInfoUpdateActivity;

public class CalenderDialog extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dateBuilder = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String month_string = "";
                String day_string = "";
                //1-9月は前に0を入れる
                if (month++ <= 9) {
                    month_string = "0" + String.valueOf(month++);
                } else {
                    month_string = String.valueOf(month++);
                }
                //1-9日も前に0を入れる
                if (dayOfMonth <= 9) {
                    day_string = "0" + String.valueOf(dayOfMonth);
                } else {
                    day_string = String.valueOf(dayOfMonth);
                }
                String date = String.valueOf(year) + "-" + month_string + "-" + day_string;
                String iso8601 = String.valueOf(year)+ month_string+day_string+"+0900";
                //もーど
                if (getArguments().getString("type").equals("birthday")){
                    AccountInfoUpdateActivity.birthday_Button.setText(date);
                }else if (getArguments().getString("type").equals("toot_time")){
                    //Home.mastodon_time_post_TextView.setText(iso8601);
                }
            }
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // dateBulderを返す
        return dateBuilder;
    }
}
