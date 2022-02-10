package com.kabestin.wearos.dagen;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.alexzaitsev.meternumberpicker.MeterView;
import com.kabestin.wearos.dagen.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity
        implements View.OnClickListener,
                   View.OnLongClickListener,
                   DatePickerDialog.OnDateSetListener {

    private TextView fromDateTV, daysBetween, daysLabel, toDateTV;
    private Switch daySwitch;
    private ImageView fromIcon;
    private Calendar fromDate, toDate;
    private ActivityMainBinding binding;

    private enum Targets {TARGET_FROM, TARGET_TO, TARGET_DAYS}
    private enum DaysMode {WEEKDAYS, WORKDAYS}

    private Targets target;
    private DaysMode daysMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, ''yy");
        String formatted;

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // for testing
        fromIcon = binding.fromImageView;
        fromIcon.setOnClickListener(this);

        fromDateTV = binding.fromDate;
        fromDate = today();
        formatted = sdf.format(fromDate.getTime());
        fromDateTV.setText(formatted);
        fromDateTV.setOnClickListener(this);
        fromDateTV.setOnLongClickListener(this);

        daysBetween = binding.daysBetween;
        daysBetween.setOnClickListener(this);
        daysBetween.setOnLongClickListener(this);

        daysLabel= binding.daysLabel;
        daysLabel.setOnClickListener(this);
        daysLabel.setOnLongClickListener(this);

        toDateTV = binding.toDate;
        toDate = today();
        toDate.add(Calendar.DAY_OF_MONTH, 1);
        formatted = sdf.format(toDate.getTime());
        toDateTV.setText(formatted);
        toDateTV.setOnClickListener(this);
        toDateTV.setOnLongClickListener(this);

        daySwitch = binding.daySwitch;
        daySwitch.setOnClickListener(this);
        daysMode = DaysMode.WEEKDAYS;

        target = Targets.TARGET_DAYS;
    }

    @Override
    public void onClick(View v) {
        int month, day, myear;
        if (v == fromDateTV) {
            if (target != Targets.TARGET_FROM) {
                month = fromDate.get(Calendar.MONTH);
                day = fromDate.get(Calendar.DAY_OF_MONTH);
                myear = fromDate.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) (view, year, monthOfYear, dayOfMonth) -> {
                            }, myear, month, day);
                datePickerDialog.setOnDateSetListener(this);
                datePickerDialog.getDatePicker().setTag(fromDateTV);
                datePickerDialog.show();
            }
        } else if (v == toDateTV) {
            if (target != Targets.TARGET_TO) {
                month = toDate.get(Calendar.MONTH);
                day = toDate.get(Calendar.DAY_OF_MONTH);
                myear = toDate.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) (view, year, monthOfYear, dayOfMonth) -> {
                            }, myear, month, day);
                datePickerDialog.setOnDateSetListener(this);
                datePickerDialog.getDatePicker().setTag(toDateTV);
                datePickerDialog.show();
            }
        } else if (v == daysBetween || v == daysLabel) {
            if (target != Targets.TARGET_DAYS) {
                final Dialog d = new Dialog(MainActivity.this);
                d.setTitle("NumberPicker");
                d.setContentView(R.layout.number_picker);
                Button okButton = (Button) d.findViewById(R.id.okButton);
                Button cancelButton = (Button) d.findViewById(R.id.cancelButton);
                final MeterView numberPicker = (MeterView) d.findViewById(R.id.meterView);
                int daysValue = Integer.valueOf(daysBetween.getText().toString());
                numberPicker.setValue(daysValue);
                okButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                       int daysValue = numberPicker.getValue();
                        daysBetween.setText(""+daysValue);
                        if (target == Targets.TARGET_FROM) {
                            setTarget(daysValue, toDate);
                        } else {
                            setTarget(fromDate, daysValue);
                        }
                        d.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });
                d.show();
            }
        } else if (v == daySwitch) {
            if (daySwitch.isChecked()) {
                daysMode = DaysMode.WORKDAYS;
            } else {
                daysMode = DaysMode.WEEKDAYS;
            }

            if (target == Targets.TARGET_DAYS) {
                setTarget(fromDate, toDate);
            } else if (target == Targets.TARGET_FROM) {
                int daysValue = Integer.valueOf(daysBetween.getText().toString());
                setTarget(daysValue, toDate);
            } else {
                int daysValue = Integer.valueOf(daysBetween.getText().toString());
                setTarget(fromDate, daysValue);
            }

        // Jerry-rigged testing unit
        } else if (v == fromIcon) {
            int days;
            Calendar feb3 = today();
            feb3.set(Calendar.YEAR, 2022);
            feb3.set(Calendar.MONTH, 1);
            feb3.set(Calendar.DAY_OF_MONTH, 3);

            Calendar feb28 = today();
            feb28.set(Calendar.YEAR, 2022);
            feb28.set(Calendar.MONTH, 1);
            feb28.set(Calendar.DAY_OF_MONTH, 28);

            days = getWeekDays(feb3, feb28);
            days = getWeekDays(feb28, feb3);

            Calendar mar28 = today();
            mar28.set(Calendar.YEAR, 2022);
            mar28.set(Calendar.MONTH, 2);
            mar28.set(Calendar.DAY_OF_MONTH, 28);

            days = getWeekDays(feb28, mar28);

            Calendar feb6 = today();
            feb6.set(Calendar.YEAR, 2022);
            feb6.set(Calendar.MONTH, 1);
            feb6.set(Calendar.DAY_OF_MONTH, 6);

            days = getWeekDays(feb3, feb6);

            System.out.println(days);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == fromDateTV) {
            target = Targets.TARGET_FROM;
            fromDateTV.setTextColor(ContextCompat.getColor(this, R.color.sandblue));
            daysBetween.setTextColor(ContextCompat.getColor(this, R.color.sand));
            daysLabel.setTextColor(ContextCompat.getColor(this, R.color.sand));
            toDateTV.setTextColor(ContextCompat.getColor(this, R.color.sand));
        } else if (v == toDateTV) {
            target = Targets.TARGET_TO;
            fromDateTV.setTextColor(ContextCompat.getColor(this, R.color.sand));
            daysBetween.setTextColor(ContextCompat.getColor(this, R.color.sand));
            daysLabel.setTextColor(ContextCompat.getColor(this, R.color.sand));
            toDateTV.setTextColor(ContextCompat.getColor(this, R.color.sandblue));
        } else {
            target = Targets.TARGET_DAYS;
            fromDateTV.setTextColor(ContextCompat.getColor(this, R.color.sand));
            daysBetween.setTextColor(ContextCompat.getColor(this, R.color.sandblue));
            daysLabel.setTextColor(ContextCompat.getColor(this, R.color.sandblue));
            toDateTV.setTextColor(ContextCompat.getColor(this, R.color.sand));
        }

        return true;
    }

    @Override
    public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, ''yy");
        String formatted;
        Calendar mCalendar = today();

        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        formatted = sdf.format(mCalendar.getTime());
        TextView tv = (TextView)picker.getTag();
        tv.setText(formatted);

        if (tv == fromDateTV) {
            fromDate = (Calendar)mCalendar.clone();
            if (fromDate.getTimeInMillis() > toDate.getTimeInMillis()) {
                // swap
                Calendar temp;
                temp = (Calendar)fromDate.clone();
                fromDate = (Calendar)toDate.clone();
                set(fromDateTV, fromDate);
                toDate = (Calendar)temp.clone();
                set(toDateTV, toDate);
            }
            if (target == Targets.TARGET_DAYS) {
                setTarget(fromDate, toDate);
            } else {
                int daysValue = Integer.valueOf(daysBetween.getText().toString());
                setTarget(fromDate, daysValue);
            }
        } else {
            toDate = mCalendar;
            if (fromDate.getTimeInMillis() > toDate.getTimeInMillis()) {
                // swap
                Calendar temp;
                temp = (Calendar)fromDate.clone();
                fromDate = (Calendar)toDate.clone();
                set(fromDateTV, fromDate);
                toDate = (Calendar)temp.clone();
                set(toDateTV, toDate);
            }
            if (target == Targets.TARGET_DAYS) {
                setTarget(fromDate, toDate);
            } else {
                int daysValue = Integer.valueOf(daysBetween.getText().toString());
                setTarget(daysValue, toDate);
            }
        }
    }

    public Calendar today() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        return now;
    }

    public void set(TextView tv, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, ''yy");
        String formatted;

        formatted = sdf.format(date.getTime());
        tv.setText(formatted);
    }

    public int getWeekDays(Calendar startCal, Calendar endCal) {
        long daysBetween = ChronoUnit.DAYS.between(startCal.toInstant(), endCal.toInstant());
        int weekdays = (int)daysBetween;
        return weekdays;
    }

    // Altered from here as starting point:
    // https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java
    public int getWorkingDays(Calendar startCal, Calendar endCal) {
        int workDays = 0;

        //Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return 0;
        }

        Calendar start = (Calendar)startCal.clone();
        Calendar end = (Calendar)endCal.clone();

        if (start.getTimeInMillis() > end.getTimeInMillis()) {
            Calendar temp = (Calendar)start.clone();
            start = (Calendar)end.clone();
            end = (Calendar)temp.clone();
        }

        do {
            //excluding start date
            start.add(Calendar.DAY_OF_MONTH, 1);
            if (start.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                ++workDays;
            }
        } while (start.getTimeInMillis() < end.getTimeInMillis()); //excluding end date

        return workDays;
    }

    public void setTarget(Calendar from, Calendar to) {
        int days;
        if (daysMode == DaysMode.WEEKDAYS) {
            days = getWeekDays(from, to);
        } else {
            days = getWorkingDays(from, to);
        }
        daysBetween.setText("" + days);
    }

    public Calendar addWorkingDays(Calendar to, int days) {
        Calendar start = (Calendar)to.clone();
        int increment = days > 0?1:-1;
        days = days * increment;
        while (days > 0) {
            start.add(Calendar.DAY_OF_MONTH, increment);
            if (start.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                days--;
            }
        }
        return start;
    }

    public void setTarget(Calendar from, int days) {
        Calendar to = (Calendar)from.clone();
        if (daysMode == DaysMode.WEEKDAYS) {
            to.add(Calendar.DATE, days);
        } else {
            to = (Calendar)(addWorkingDays(to, days)).clone();
        }

        toDate = (Calendar)to.clone();
        set(toDateTV, to);
    }

    public void setTarget(int days, Calendar to) {
        Calendar from = (Calendar)to.clone();
        if (daysMode == DaysMode.WEEKDAYS) {
            from.add(Calendar.DATE, -days);
        } else {
            from = (Calendar)(addWorkingDays(from, -days)).clone();
        }

        fromDate = (Calendar)from.clone();
        set(fromDateTV, from);

    }
}