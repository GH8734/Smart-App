package ie.teamchile.smartapp.activities.AppointmentCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.activities.Base.BaseActivity;
import ie.teamchile.smartapp.activities.CreateAppointment.CreateAppointmentActivity;
import ie.teamchile.smartapp.activities.ServiceUser.ServiceUserActivity;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.Clinic;
import ie.teamchile.smartapp.util.Constants;
import ie.teamchile.smartapp.util.CustomDialogs;
import io.realm.Realm;
import timber.log.Timber;

public class AppointmentCalendarActivity extends BaseActivity implements AppointmentCalendarView {
    public static Date daySelected;
    public static int clinicSelected;
    private Date clinicOpening, clinicClosing;
    private int appointmentInterval, dayOfWeek;
    private Calendar c = Calendar.getInstance(), myCalendar = Calendar.getInstance();
    private Intent intent;
    private List<String> timeList = new ArrayList<>();
    private List<Integer> idList = new ArrayList<>();
    private int serviceOptionId;
    private Button dateInList, btnPrevWeek, btnNextWeek;
    private BaseAdapter adapter;
    private ListView listView;
    private ProgressDialog pd;
    private Realm realm;
    private AppointmentCalendarPresenter appointmentCalendarPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_appointment_calendar);

        initViews();

        appointmentCalendarPresenter = new AppointmentCalendarPresenterImp(this, new WeakReference<Activity>(AppointmentCalendarActivity.this));

        realm = appointmentCalendarPresenter.getEncryptedRealm();

        serviceOptionId = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getServiceOptionIds().get(0).getValue();

        clinicOpening = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getOpeningTime();

        clinicClosing = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getClosingTime();

        appointmentInterval = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getAppointmentInterval();

        myCalendar.setTime(clinicClosing);
        myCalendar.add(Calendar.MINUTE, (-appointmentInterval));

        c.setTime(daySelected);
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        idList = new ArrayList<>();
        adapter = new AppointmentListAdapter();
        listView.setAdapter(adapter);

        getAppointmentListForDay(daySelected);
        createDatePicker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("in onResume");
        Timber.d("daySelected: " + daySelected);
        idList = new ArrayList<>();
        adapter.notifyDataSetChanged();
        getAppointmentListForDay(daySelected);
    }


    @Override
    public void initViews() {
        dateInList = (Button) findViewById(R.id.btn_date);
        listView = (ListView) findViewById(R.id.lv_appointment_list);
        btnPrevWeek = (Button) findViewById(R.id.btn_prev);
        btnPrevWeek.setOnClickListener(new ButtonClick());
        btnNextWeek = (Button) findViewById(R.id.btn_next);
        btnNextWeek.setOnClickListener(new ButtonClick());
    }

    public void pauseButton() {
        btnNextWeek.setEnabled(false);
        btnPrevWeek.setEnabled(false);
        CountDownTimer nextTimer = new CountDownTimer(250, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                btnNextWeek.setEnabled(true);
                btnPrevWeek.setEnabled(true);
            }
        };
        nextTimer.start();
    }

    public List<Integer> removeZeros(List<Integer> badList) {
        if (badList != null)
            for (int i = 0; i < badList.size(); i++)
                if (badList.get(i).equals(0))
                    badList.remove(i);
        return badList;
    }

    private void createDatePicker() {
        myCalendar.setTime(daySelected);
        final DatePickerDialog.OnDateSetListener pickerDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (pd != null) {
                    pd.dismiss();
                }
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Constants.DF_DATE_ONLY.format(myCalendar.getTime());

                if (myCalendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                    dateInList.setText(Constants.DF_DATE_W_MONTH_NAME.format(myCalendar.getTime()));
                    getAppointmentListForDay(myCalendar.getTime());
                } else {
                    pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this,
                            "Invalid day selected\nPlease choose another");
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            pd.dismiss();
                        }
                    }, 2000);
                }
            }
        };
        dateInList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AppointmentCalendarActivity.this, pickerDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    public void getAppointmentListForDay(Date dateSelected) {
        timeList = new ArrayList<>();
        idList = new ArrayList<>();

        Date apptTime = clinicOpening;
        daySelected = dateSelected;

        String dateSelectedStr = Constants.DF_DATE_ONLY.format(dateSelected);
        dateInList.setText(Constants.DF_DATE_W_MONTH_NAME.format(dateSelected));
        String nameOfClinic = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getName();
        setActionBarTitle(nameOfClinic);

        while (!clinicClosing.before(apptTime)) {
            timeList.add(Constants.DF_TIME_ONLY.format(apptTime));
            idList.add(0);
            c.setTime(apptTime);
            c.add(Calendar.MINUTE, appointmentInterval);
            apptTime = c.getTime();
        }

        List<Appointment> appointmentList = realm.where(Appointment.class)
                .equalTo(Constants.REALM_CLINIC_ID, clinicSelected)
                .equalTo(Constants.REALM_DATE, dateSelectedStr)
                .findAll();

        if (!appointmentList.isEmpty()) {
            int size = appointmentList.size();
            Appointment appointment;
            String timeOfAppt;
            for (int i = 0; i < size; i++) {
                appointment = appointmentList.get(i);
                timeOfAppt = Constants.DF_DATE_ONLY.format(appointment.getTime());
                if (timeList.contains(timeOfAppt)) {
                    idList.set(timeList.indexOf(timeOfAppt), appointment.getId());
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void setClinicSelected(int clinicSelected) {
        AppointmentCalendarActivity.clinicSelected = clinicSelected;
    }

    public void setDaySelected(Date daySelected) {
        AppointmentCalendarActivity.daySelected = daySelected;
    }

    @Override
    public void gotoServiceUserActivity() {
        startActivity(new Intent(AppointmentCalendarActivity.this, ServiceUserActivity.class));
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_prev:
                    c.setTime(daySelected);
                    c.add(Calendar.DAY_OF_YEAR, -7);
                    daySelected = c.getTime();
                    myCalendar.setTime(daySelected);
                    createDatePicker();
                    getAppointmentListForDay(c.getTime());
                    pauseButton();
                    break;
                case R.id.btn_next:
                    c.setTime(daySelected);
                    c.add(Calendar.DAY_OF_YEAR, 7);
                    daySelected = c.getTime();
                    myCalendar.setTime(daySelected);
                    createDatePicker();
                    getAppointmentListForDay(c.getTime());
                    pauseButton();
                    break;
            }
        }
    }

    private class AppointmentListAdapter extends BaseAdapter {
        private boolean attended;
        private ViewHolder holder;

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Integer getItem(int position) {
            return idList.get(position);
        }

        public Appointment getAppointment(int position) {
            return realm.where(Appointment.class).equalTo(Constants.REALM_ID, getItem(position)).findFirst();
        }

        @Override
        public long getItemId(int position) {
            return idList.get(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(AppointmentCalendarActivity.this);
                convertView = layoutInflater.inflate(R.layout.list_layout_appointment, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.timeText.setText(timeList.get(position));

            if (getItem(position) == 0) {
                holder.nameText.setText(getString(R.string.free_slot));
                holder.gestText.setText(null);
                attended = false;
                holder.swipeLayout.setSwipeEnabled(false);
                holder.nameText.setTextColor(getResources().getColor(R.color.free_slot));
                holder.nameText.setTypeface(null, Typeface.ITALIC);
            } else {
                holder.nameText.setText(getAppointment(position).getServiceUser().getName());
                holder.gestText.setText(getAppointment(position).getServiceUser().getGestation());
                attended = getAppointment(position).isAttended();
                holder.swipeLayout.setSwipeEnabled(true);
                holder.nameText.setTextColor(holder.gestText.getTextColors().getDefaultColor());
                holder.nameText.setTypeface(null, Typeface.NORMAL);
            }

            if (attended) {
                holder.ivAttend.setBackgroundResource(R.color.attended);
                holder.btnChangeStatus.setText(getString(R.string.no));
            } else {
                holder.ivAttend.setBackgroundResource(R.color.unattended);
                holder.btnChangeStatus.setText(getString(R.string.yes));
            }

            holder.llApptListItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getItem(position).equals(0)) {
                        intent = new Intent(getApplicationContext(), CreateAppointmentActivity.class);
                        intent.putExtra(Constants.ARGS_FROM, Constants.ARGS_CLINIC_APPOINTMENT);
                        intent.putExtra(Constants.ARGS_TIME, timeList.get(position));
                        intent.putExtra(Constants.ARGS_CLINIC_ID, String.valueOf(clinicSelected));
                        intent.putExtra(Constants.ARGS_SERVICE_OPTION_ID, String.valueOf(serviceOptionId));
                        startActivity(intent);
                    } else {
                        appointmentCalendarPresenter.searchServiceUser(getAppointment(position).getServiceUserId());
                    }
                    return true;
                }
            });

            holder.btnChangeStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.swipeLayout.close();

                    realm.beginTransaction();
                    getAppointment(position).setAttended(!attended);
                    realm.commitTransaction();
                    appointmentCalendarPresenter.changeAttendStatus(
                            !attended,
                            position,
                            clinicSelected,
                            realm.where(Appointment.class)
                                    .equalTo(Constants.REALM_ID,
                                            idList.get(position)).findFirst()
                                    .getServiceUserId(),
                            idList.get(position));
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            private TextView timeText;
            private TextView nameText;
            private TextView gestText;
            private Button btnChangeStatus;
            private ImageView ivAttend;
            private SwipeLayout swipeLayout;
            private LinearLayout llApptListItem;

            public ViewHolder(View view) {
                timeText = (TextView) view.findViewById(R.id.tv_time);
                nameText = (TextView) view.findViewById(R.id.tv_name);
                gestText = (TextView) view.findViewById(R.id.tv_gestation);
                btnChangeStatus = (Button) view.findViewById(R.id.btn_change_status);
                ivAttend = (ImageView) view.findViewById(R.id.img_attended);
                swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_appt_list);
                llApptListItem = (LinearLayout) view.findViewById(R.id.ll_appt_list_item);
            }
        }
    }
}