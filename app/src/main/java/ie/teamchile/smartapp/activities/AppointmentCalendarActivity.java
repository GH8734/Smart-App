package ie.teamchile.smartapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
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
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.activities.Base.BaseActivity;
import ie.teamchile.smartapp.api.SmartApiClient;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.Clinic;
import ie.teamchile.smartapp.model.Login;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.util.Constants;
import ie.teamchile.smartapp.util.CustomDialogs;
import io.realm.Realm;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class AppointmentCalendarActivity extends BaseActivity {
    protected static Date daySelected;
    private static int serviceOptionSelected, weekSelected, clinicSelected, visitOptionSelected;
    private final int sdkVersion = Build.VERSION.SDK_INT;
    private Date openingAsDate, closingAsDate;
    private String clinicOpening, clinicClosing, closingMinusInterval,
            dateSelectedStr, timeBefore, timeAfter, nameOfClinic;
    private int appointmentInterval, dayOfWeek;
    private List<String> timeSingle;
    //private List<Integer> listOfApptId = new ArrayList<>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_appointment_calendar);

        realm = Realm.getInstance(this);

        Timber.d("clinicSelected = " + clinicSelected);

        dateInList = (Button) findViewById(R.id.btn_date);
        listView = (ListView) findViewById(R.id.lv_appointment_list);
        btnPrevWeek = (Button) findViewById(R.id.btn_prev);
        btnPrevWeek.setOnClickListener(new ButtonClick());
        btnNextWeek = (Button) findViewById(R.id.btn_next);
        btnNextWeek.setOnClickListener(new ButtonClick());

        serviceOptionId = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getServiceOptionIds().get(0).getValue();
        clinicOpening = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getOpeningTime();
        clinicClosing = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getClosingTime();
        appointmentInterval = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getAppointmentInterval();
        try {
            openingAsDate = dfTimeOnly.parse(String.valueOf(clinicOpening));
            closingAsDate = dfTimeOnly.parse(String.valueOf(clinicClosing));
        } catch (ParseException e) {
            Timber.e(Log.getStackTraceString(e));
        }

        myCalendar.setTime(closingAsDate);
        myCalendar.add(Calendar.MINUTE, (-appointmentInterval));
        closingMinusInterval = dfTimeOnly.format(myCalendar.getTime());

        c.setTime(daySelected);
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        idList = new ArrayList<>();
        adapter = new ListElementAdapter();
        listView.setAdapter(adapter);

        newSetToList(daySelected);
        createDatePicker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (realm != null)
            realm.close();
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
        newSetToList(daySelected);
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
                        dfDateOnly.format(myCalendar.getTime());
                if (myCalendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                    dateInList.setText(dfDateWMonthName.format(myCalendar.getTime()));
                    newSetToList(myCalendar.getTime());
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

    private void newSetToList(Date dateSelected) {
        timeSingle = new ArrayList<>();

        timeList = new ArrayList<>();
        idList = new ArrayList<>();

        Date apptTime = openingAsDate;
        daySelected = dateSelected;

        dateSelectedStr = dfDateOnly.format(dateSelected);
        dateInList.setText(dfDateWMonthName.format(dateSelected));
        nameOfClinic = realm.where(Clinic.class).equalTo(Constants.REALM_ID, clinicSelected).findFirst().getName();
        setActionBarTitle(nameOfClinic);

        while (!closingAsDate.before(apptTime)) {
            timeList.add(dfTimeOnly.format(apptTime));
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
            for (Appointment appointment : appointmentList) {
                String timeOfAppt = "";
                try {
                    timeOfAppt = dfTimeOnly.format(dfTimeWSec.parse(appointment.getTime()));
                } catch (ParseException e) {
                    Timber.e(Log.getStackTraceString(e));
                }
                if(timeList.contains(timeOfAppt)){
                    idList.set(timeList.indexOf(timeOfAppt), appointment.getId());
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void changeAttendStatus(Boolean status, int position) {
        pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this, "Changing Attended Status");

        PostingData attendedStatus = new PostingData();
        attendedStatus.putAppointmentStatus(
                status,
                clinicSelected,
                realm.where(Login.class).findFirst().getId(),
                realm.where(Appointment.class).equalTo(Constants.REALM_ID, idList.get(position)).findFirst().getServiceUserId());

        SmartApiClient.getAuthorizedApiClient(this).putAppointmentStatus(
                attendedStatus,
                idList.get(position),
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Toast.makeText(AppointmentCalendarActivity.this,
                                "Status changed", Toast.LENGTH_LONG).show();

                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(baseModel.getAppointment());
                        realm.commitTransaction();

                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Timber.d("retro error = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void searchServiceUser(int serviceUserId, final Intent intent) {
        SmartApiClient.getAuthorizedApiClient(this).getServiceUserById(serviceUserId,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(baseModel.getServiceUsers());
                        realm.copyToRealmOrUpdate(baseModel.getBabies());
                        realm.copyToRealmOrUpdate(baseModel.getPregnancies());
                        realm.commitTransaction();
                        startActivity(intent);
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pd.dismiss();
                        Toast.makeText(
                                AppointmentCalendarActivity.this,
                                "Error Search Patient: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setServiceOptionSelected(int serviceOptionSelected) {
        AppointmentCalendarActivity.serviceOptionSelected = serviceOptionSelected;
    }

    public void setClinicSelected(int clinicSelected) {
        AppointmentCalendarActivity.clinicSelected = clinicSelected;
    }

    public void setWeekSelected(int weekSelected) {
        AppointmentCalendarActivity.weekSelected = weekSelected;
    }

    public void setDaySelected(Date daySelected) {
        AppointmentCalendarActivity.daySelected = daySelected;
    }

    public void setVisitOption(int visitOptionSelected) {
        AppointmentCalendarActivity.visitOptionSelected = visitOptionSelected;
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
                    newSetToList(c.getTime());
                    pauseButton();
                    break;
                case R.id.btn_next:
                    c.setTime(daySelected);
                    c.add(Calendar.DAY_OF_YEAR, 7);
                    daySelected = c.getTime();
                    myCalendar.setTime(daySelected);
                    createDatePicker();
                    newSetToList(c.getTime());
                    pauseButton();
                    break;
            }
        }
    }

    private class ListElementAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return idList.size();
        }

        @Override
        public Integer getItem(int position) {
            return idList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return idList.get(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final Appointment appointment = realm.where(Appointment.class).equalTo(Constants.REALM_ID, getItem(position)).findFirst();
            final Boolean attended;

            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(AppointmentCalendarActivity.this);
                convertView = layoutInflater.inflate(R.layout.list_layout_appointment, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            holder.timeText.setText(timeList.get(position));

            if(getItem(position) == 0){
                holder.nameText.setText("Free Slot");
                holder.gestText.setText("");
                attended = false;
                holder.swipeLayout.setSwipeEnabled(false);
                holder.nameText.setTextColor(getResources().getColor(R.color.free_slot));
                holder.nameText.setTypeface(null, Typeface.ITALIC);
            } else {
                holder.nameText.setText(appointment.getServiceUser().getName());
                holder.gestText.setText(appointment.getServiceUser().getGestation());
                attended = appointment.isAttended();
                holder.swipeLayout.setSwipeEnabled(true);
                holder.nameText.setTextColor(holder.gestText.getTextColors().getDefaultColor());
                holder.nameText.setTypeface(null, Typeface.NORMAL);
            }

            if (attended) {
                holder.ivAttend.setBackgroundResource(R.color.attended);
                holder.btnChangeStatus.setText("No");
            } else {
                holder.ivAttend.setBackgroundResource(R.color.unattended);
                holder.btnChangeStatus.setText("Yes");
            }

            holder.llApptListItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getItem(position).equals(0)) {
                        intent = new Intent(AppointmentCalendarActivity.this, CreateAppointmentActivity.class);
                        intent.putExtra("from", "clinic-appointment");
                        intent.putExtra("time", timeList.get(position));
                        intent.putExtra("clinicID", String.valueOf(clinicSelected));
                        intent.putExtra("serviceOptionId", String.valueOf(serviceOptionId));
                        startActivity(intent);
                    } else {
                        int serviceUserId = appointment.getServiceUserId();
                        Timber.d("db string: " + "service_users" + "/" + serviceUserId);
                        pd = new CustomDialogs().showProgressDialog(AppointmentCalendarActivity.this,
                                "Fetching Information");
                        intent = new Intent(AppointmentCalendarActivity.this, ServiceUserActivity.class);
                        searchServiceUser(serviceUserId, intent);
                    }
                    return true;
                }
            });

            holder.btnChangeStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.swipeLayout.close();

                    realm.beginTransaction();
                    appointment.setAttended(!attended);
                    realm.commitTransaction();
                    changeAttendStatus(!attended, position);
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