package ie.teamchile.smartapp.activities.AppointmentTypeSpinner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.activities.AppointmentCalendar.AppointmentCalendarActivity;
import ie.teamchile.smartapp.activities.Base.BaseActivity;
import ie.teamchile.smartapp.activities.HomeVisitAppointment.HomeVisitAppointmentActivity;
import ie.teamchile.smartapp.model.Clinic;
import ie.teamchile.smartapp.model.RealmInteger;
import ie.teamchile.smartapp.model.ServiceOption;
import ie.teamchile.smartapp.util.AdapterSpinner;
import ie.teamchile.smartapp.util.AppointmentHelper;
import ie.teamchile.smartapp.util.Constants;
import ie.teamchile.smartapp.util.CustomDialogs;
import ie.teamchile.smartapp.util.GeneralUtils;
import io.realm.Realm;
import timber.log.Timber;

public class AppointmentTypeSpinnerActivity extends BaseActivity implements AppointmentTypeSpinnerView {
    private List<RealmInteger> idList;
    private int clinicSelected;
    private Date daySelected, dayOfWeek;
    private Calendar c = Calendar.getInstance();
    private AppointmentCalendarActivity passOptions = new AppointmentCalendarActivity();
    private int spinnerWarning;
    private Spinner appointmentTypeSpinner, serviceOptionSpinner,
            visitOptionSpinner, visitDaySpinner, clinicSpinner, daySpinner, weekSpinner;
    private TextView tvAppointmentType, tvServiceOption, tvVisit, tvVisitDay, tvClinic, tvDay, tvWeek;
    private AppointmentHelper apptHelp;
    private CountDownTimer timer;
    private ProgressDialog pd;
    private Realm realm;
    private List<ServiceOption> serviceOptionClinicList;
    private List<ServiceOption> serviceOptionVisitList;
    private AppointmentTypeSpinnerPresenter appointmentTypeSpinnerPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_appointment_type_spinner);

        initViews();

        appointmentTypeSpinnerPresenter = new AppointmentTypeSpinnerPresenterImp(
                this, new WeakReference<Activity>(AppointmentTypeSpinnerActivity.this));

        realm = appointmentTypeSpinnerPresenter.getEncryptedRealm();

        apptHelp = new AppointmentHelper(realm);

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        spinnerWarning = ContextCompat.getColor(this, R.color.spinner_warning);

        setAppointmentTypeSpinner();
        setServiceOptionSpinner();
        setVisitSpinner();
        setVisitDaySpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initViews() {
        tvAppointmentType = (TextView) findViewById(R.id.tv_appointment_type);
        tvServiceOption = (TextView) findViewById(R.id.tv_service_option);
        tvVisit = (TextView) findViewById(R.id.tv_visit_option);
        tvVisitDay = (TextView) findViewById(R.id.tv_visit_day);
        tvClinic = (TextView) findViewById(R.id.tv_clinic);
        tvDay = (TextView) findViewById(R.id.tv_day);
        tvWeek = (TextView) findViewById(R.id.tv_week);

        appointmentTypeSpinner = (Spinner) findViewById(R.id.spnr_appointment_type);
        serviceOptionSpinner = (Spinner) findViewById(R.id.spnr_service_option);
        visitOptionSpinner = (Spinner) findViewById(R.id.spnr_visit_option);
        visitDaySpinner = (Spinner) findViewById(R.id.spnr_visit_day);
        clinicSpinner = (Spinner) findViewById(R.id.spnr_clinic);
        daySpinner = (Spinner) findViewById(R.id.spnr_day);
        weekSpinner = (Spinner) findViewById(R.id.spnr_week);

        serviceOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        weekSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        visitOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        tvAppointmentType.setVisibility(View.VISIBLE);
        tvServiceOption.setVisibility(View.GONE);
        tvVisit.setVisibility(View.GONE);
        tvVisitDay.setVisibility(View.GONE);
        tvClinic.setVisibility(View.GONE);
        tvDay.setVisibility(View.GONE);
        tvWeek.setVisibility(View.GONE);

        appointmentTypeSpinner.setVisibility(View.VISIBLE);
        serviceOptionSpinner.setVisibility(View.GONE);
        visitOptionSpinner.setVisibility(View.GONE);
        visitDaySpinner.setVisibility(View.GONE);
        clinicSpinner.setVisibility(View.GONE);
        daySpinner.setVisibility(View.GONE);
        weekSpinner.setVisibility(View.GONE);
    }

    private void setServiceOptionSpinner() {
        serviceOptionClinicList = realm.where(ServiceOption.class).equalTo(Constants.REALM_HOME_VISIT, false).findAll();
        List<String> serviceOptionNameList = new ArrayList<>();
        serviceOptionNameList.add(getString(R.string.select_service_option));

        if (!serviceOptionClinicList.isEmpty()) {
            int mapSize = serviceOptionClinicList.size();
            for (int i = 0; i < mapSize; i++) {
                serviceOptionNameList.add("- " + serviceOptionClinicList.get(i).getName());
            }
        }

        ArrayAdapter<String> serviceOptionAdapter = new AdapterSpinner(this, R.layout.spinner_layout, serviceOptionNameList, R.id.tv_spinner_item);
        serviceOptionAdapter.setDropDownViewResource(R.layout.spinner_layout);
        serviceOptionSpinner.setAdapter(serviceOptionAdapter);
    }

    private void setClinicSpinner(int z) {
        idList = serviceOptionClinicList.get(z - 1).getClinicIds();

        List<String> clinicNames = new ArrayList<>();
        clinicNames.add(getString(R.string.select_clinic));

        if (!idList.isEmpty()) {
            int size = idList.size();
            for (int i = 0; i < size; i++) {
                clinicNames.add("- " + realm.where(Clinic.class).equalTo(Constants.REALM_ID, idList.get(i).getValue()).findFirst().getName());
            }
        }

        clinicSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        ArrayAdapter<String> clinicAdapter = new AdapterSpinner(this, R.layout.spinner_layout, clinicNames, R.id.tv_spinner_item);
        clinicAdapter.setDropDownViewResource(R.layout.spinner_layout);
        clinicSpinner.setAdapter(clinicAdapter);
    }

    private void setVisitSpinner() {
        serviceOptionVisitList = realm.where(ServiceOption.class).equalTo(Constants.REALM_HOME_VISIT, true).findAll();

        List<String> visitClinics = new ArrayList<>();
        visitClinics.add(getString(R.string.select_visit_option));

        if (!serviceOptionVisitList.isEmpty()) {
            int size = serviceOptionVisitList.size();
            for (int i = 0; i < size; i++) {
                visitClinics.add("- " + serviceOptionVisitList.get(i).getName());
            }
        }

        visitOptionSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        ArrayAdapter<String> visitAdapter = new AdapterSpinner(this, R.layout.spinner_layout, visitClinics, R.id.tv_spinner_item);
        visitAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitOptionSpinner.setAdapter(visitAdapter);
    }

    private void setVisitDaySpinner() {
        Calendar cal = Calendar.getInstance();
        List<String> visitDayList = new ArrayList<>();
        visitDayList.add(getString(R.string.select_day));

        for (int i = 0; i < 10; i++) {
            String day = Constants.DF_DAY_SHORT.format(cal.getTime());
            String date = Constants.DF_DATE_W_MONTH_NAME.format(cal.getTime());

            visitDayList.add("- " + day + ", " + date);

            cal.add(Calendar.DATE, 1);
        }

        visitDaySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        ArrayAdapter<String> visitDayAdapter = new AdapterSpinner(this, R.layout.spinner_layout, visitDayList, R.id.tv_spinner_item);
        visitDayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitDaySpinner.setAdapter(visitDayAdapter);
    }

    private void setAppointmentTypeSpinner() {
        appointmentTypeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        ArrayAdapter<String> appointmentAdapter = new AdapterSpinner(this, R.array.appointment_type_list, R.layout.spinner_layout, R.id.tv_spinner_item);
        appointmentAdapter.setDropDownViewResource(R.layout.spinner_layout);
        appointmentTypeSpinner.setAdapter(appointmentAdapter);
    }

    private void setWeekSpinner(Date dayOfWeek) {
        List<String> weeks = new ArrayList<>();
        weeks.add(getString(R.string.select_week));

        for (int i = 1; i <= 10; i++) {
            c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, 7 * i);
            c.set(Calendar.DAY_OF_WEEK, dayOfWeek.getDay() + 1);
            weeks.add("- Week " + i + " (" + Constants.DF_DOW_MONTH_DAY.format(c.getTime()) + ")");
        }

        ArrayAdapter<String> weekAdapter = new AdapterSpinner(this, R.layout.spinner_layout, weeks, R.id.tv_spinner_item);
        weekAdapter.setDropDownViewResource(R.layout.spinner_layout);
        weekSpinner.setAdapter(weekAdapter);
    }

    private void setDaySpinner(List<String> days) {
        if (!days.isEmpty()) {
            int size = days.size();
            for (int i = 0; i < size; i++) {
                String dayFirstLetterUpperCase = Character.toString(days.get(i).charAt(0))
                        .toUpperCase(Locale.getDefault())
                        + days.get(i).substring(1);
                days.set(i, dayFirstLetterUpperCase);
            }
        }
        days.add(0, getString(R.string.select_day));

        daySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());
        ArrayAdapter<String> dayAdapter = new AdapterSpinner(this, R.layout.spinner_layout, days, R.id.tv_spinner_item);
        dayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        daySpinner.setAdapter(dayAdapter);
    }

    private void loopForServiceOptionDay(int visitOption) {
        Calendar c = Calendar.getInstance();
        Date todayDate = c.getTime();
        c.setTime(todayDate);
        c.add(Calendar.DAY_OF_YEAR, 10);
        Date todayPlus10Day = c.getTime();

        while (todayDate.before(todayPlus10Day)) {
            c.setTime(todayDate);
            String date = Constants.DF_DATE_ONLY.format(c.getTime());
            c.add(Calendar.DAY_OF_YEAR, 1);
            todayDate = c.getTime();

            apptHelp.getAppointmentsHomeVisit(date, visitOption);
        }
    }

    private void doneChecker(final Intent intent) {
        pd = new CustomDialogs().showProgressDialog(AppointmentTypeSpinnerActivity.this,
                getString(R.string.fetching_information));
        timer = new CountDownTimer(200, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (BaseActivity.apptDone >= 10) {
                    pd.dismiss();
                    changeActivity(intent);
                } else
                    timer.start();
            }
        }.start();
    }

    public void changeActivity(Intent intent) {
        passOptions.setClinicSelected(clinicSelected);
        passOptions.setDaySelected(daySelected);

        startActivity(intent);
    }

    public void addDayToTime(Date dayOfWeek) {
        c.setTime(dayOfWeek);
        int dayAsInt = c.get(Calendar.DAY_OF_WEEK);
        c.setTime(daySelected);
        c.set(Calendar.DAY_OF_WEEK, dayAsInt);
        daySelected = c.getTime();
    }

    private class MySpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.spnr_appointment_type:
                    switch (position) {
                        case 0:
                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.GONE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            appointmentTypeSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            break;
                        case 1:     //Clinic
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);

                            appointmentTypeSpinner.setBackgroundColor(Color.TRANSPARENT);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setSelection(0);
                            break;
                        case 2:     //Visit
                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.GONE);
                            tvVisit.setVisibility(View.VISIBLE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            appointmentTypeSpinner.setBackgroundColor(Color.TRANSPARENT);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setVisibility(View.VISIBLE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            visitOptionSpinner.setSelection(0);
                            break;
                    }
                    break;
                case R.id.spnr_visit_option:
                    switch (position) {
                        case 0:
                            visitOptionSpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            visitOptionSpinner.setBackgroundColor(Color.TRANSPARENT);
                            visitDaySpinner.setVisibility(View.VISIBLE);
                            tvVisitDay.setVisibility(View.VISIBLE);
                            visitDaySpinner.setSelection(0);
                            int visitOptionSelected = serviceOptionVisitList.get(position - 1).getId();
                            loopForServiceOptionDay(visitOptionSelected);

                            HomeVisitAppointmentActivity.visitOptionSelected = visitOptionSelected;
                            break;
                    }
                    break;
                case R.id.spnr_visit_day:
                    switch (position) {
                        case 0:
                            visitDaySpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            visitDaySpinner.setBackgroundColor(Color.TRANSPARENT);
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, position - 1);
                            daySelected = cal.getTime();

                            HomeVisitAppointmentActivity.daySelected = daySelected;
                            Intent intent = new Intent(AppointmentTypeSpinnerActivity.this, HomeVisitAppointmentActivity.class);
                            doneChecker(intent);
                            break;
                    }
                    break;
                case R.id.spnr_service_option:
                    switch (position) {
                        case 0:
                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.GONE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            serviceOptionSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setVisibility(View.GONE);
                            break;
                        default:
                            tvClinic.setVisibility(View.VISIBLE);

                            serviceOptionSpinner.setBackgroundColor(Color.TRANSPARENT);
                            clinicSpinner.setVisibility(View.VISIBLE);
                            setClinicSpinner(position);
                            clinicSpinner.setSelection(0);
                            break;
                    }
                    break;
                case R.id.spnr_clinic:
                    switch (position) {
                        case 0:
                            clinicSelected = 0;

                            tvAppointmentType.setVisibility(View.VISIBLE);
                            tvServiceOption.setVisibility(View.VISIBLE);
                            tvVisit.setVisibility(View.GONE);
                            tvVisitDay.setVisibility(View.GONE);
                            tvClinic.setVisibility(View.VISIBLE);
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            clinicSpinner.setBackgroundColor(spinnerWarning);
                            appointmentTypeSpinner.setVisibility(View.VISIBLE);
                            serviceOptionSpinner.setVisibility(View.VISIBLE);
                            visitOptionSpinner.setVisibility(View.GONE);
                            visitDaySpinner.setVisibility(View.GONE);
                            clinicSpinner.setVisibility(View.VISIBLE);
                            daySpinner.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            break;
                        default:
                            tvDay.setVisibility(View.GONE);
                            tvWeek.setVisibility(View.GONE);

                            clinicSpinner.setBackgroundColor(Color.TRANSPARENT);
                            daySpinner.setVisibility(View.GONE);
                            daySpinner.setSelection(0);
                            weekSpinner.setVisibility(View.GONE);
                            weekSpinner.setSelection(0);

                            clinicSelected = idList.get(position - 1).getValue();
                            List<String> trueDays = new GeneralUtils().getTrueDays(
                                    realm.where(Clinic.class)
                                            .equalTo(Constants.REALM_ID, clinicSelected)
                                            .findFirst().getDays());

                            if (trueDays.size() > 1) {
                                setDaySpinner(trueDays);
                                tvDay.setVisibility(View.VISIBLE);
                                daySpinner.setVisibility(View.VISIBLE);
                                daySpinner.setSelection(0);
                            } else {
                                try {
                                    tvWeek.setVisibility(View.VISIBLE);
                                    weekSpinner.setVisibility(View.VISIBLE);
                                    weekSpinner.setSelection(0);
                                    dayOfWeek = Constants.DF_DAY_SHORT.parse(trueDays.get(0));
                                    appointmentTypeSpinnerPresenter.getAppointment(clinicSelected, dayOfWeek);
                                    setWeekSpinner(dayOfWeek);
                                } catch (ParseException e) {
                                    Timber.e(Log.getStackTraceString(e));
                                }
                            }
                            break;
                    }
                    break;
                case R.id.spnr_day:
                    switch (position) {
                        case 0:
                            tvWeek.setVisibility(View.GONE);
                            weekSpinner.setVisibility(View.GONE);
                            daySpinner.setBackgroundColor(spinnerWarning);
                            break;
                        default:
                            tvWeek.setVisibility(View.VISIBLE);
                            daySpinner.setBackgroundColor(Color.TRANSPARENT);
                            weekSpinner.setVisibility(View.VISIBLE);
                            weekSpinner.setSelection(0);
                            try {
                                dayOfWeek = Constants.DF_DAY_SHORT.parse(daySpinner.getSelectedItem().toString());
                                appointmentTypeSpinnerPresenter.getAppointment(clinicSelected, dayOfWeek);
                                setWeekSpinner(dayOfWeek);
                            } catch (ParseException e) {
                                Timber.e(Log.getStackTraceString(e));
                            }
                            break;
                    }
                    break;
                case R.id.spnr_week:
                    switch (position) {
                        case 0:
                            weekSpinner.setBackgroundColor(spinnerWarning);
                            c = Calendar.getInstance();
                            break;
                        default:
                            weekSpinner.setBackgroundColor(Color.TRANSPARENT);
                            c = Calendar.getInstance();
                            c.add(Calendar.DAY_OF_YEAR, 7 * position);
                            daySelected = c.getTime();
                            addDayToTime(dayOfWeek);
                            Intent intent = new Intent(AppointmentTypeSpinnerActivity.this, AppointmentCalendarActivity.class);
                            doneChecker(intent);
                            break;
                    }
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }
}