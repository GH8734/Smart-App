package ie.teamchile.smartapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.model.ApiRootModel;
import ie.teamchile.smartapp.model.Appointment;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.model.ServiceUser;
import ie.teamchile.smartapp.util.AdapterSpinner;
import ie.teamchile.smartapp.util.SmartApi;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class CreateAppointmentActivity extends BaseActivity {
    private ArrayAdapter<String> visitPriorityAdapter, returnTypeAdapter;
    private String userName, apptDate, time, priority, visitType, clinicName,
            hospitalNumber, email, sms;
    private int userID;
    private Calendar c, myCalendar;
    private Date daySelected;
    private List<Integer> idList = new ArrayList<>();
    private AppointmentCalendarActivity passOptions = new AppointmentCalendarActivity();
    private SharedPreferences prefs;
    private AlertDialog.Builder alertDialog;
    private AlertDialog ad;
    private int clinicID, serviceOptionId;
    private int p = 0;
    private EditText etUserName;
    private Button btnConfirmAppointment;
    private ImageButton btnUserSearch;
    private TextView tvTime, tvTimeTitle, tvDate, tvClinic, tvReturnTitle, tvPriorityTitle;
    private Spinner visitReturnTypeSpinner, visitPrioritySpinner;
    private List<ServiceUser> serviceUserList = new ArrayList<>();
    private String returnType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_create_appointment);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SmartApi.BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient())
                .build();

        api = restAdapter.create(SmartApi.class);

        c = Calendar.getInstance();
        myCalendar = Calendar.getInstance();

        etUserName = (EditText) findViewById(R.id.et_service_user);
        btnConfirmAppointment = (Button) findViewById(R.id.btn_confirm_appointment);
        btnUserSearch = (ImageButton) findViewById(R.id.btn_user_search);
        tvTime = (TextView) findViewById(R.id.tv_visit_time);
        tvTimeTitle = (TextView) findViewById(R.id.tv_visit_time_title);
        tvDate = (TextView) findViewById(R.id.tv_visit_date);
        tvClinic = (TextView) findViewById(R.id.tv_visit_clinic);
        tvReturnTitle = (TextView) findViewById(R.id.tv_return_type_title);
        tvPriorityTitle = (TextView) findViewById(R.id.tv_prioirty_title);

        etUserName.setText("");

        btnConfirmAppointment.setOnClickListener(new ButtonClick());
        btnUserSearch.setOnClickListener(new ButtonClick());

        visitReturnTypeSpinner = (Spinner) findViewById(R.id.spnr_visit_return_type);
        visitReturnTypeSpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        visitPrioritySpinner = (Spinner) findViewById(R.id.spnr_visit_priority);
        visitPrioritySpinner.setOnItemSelectedListener(new MySpinnerOnItemSelectedListener());

        Log.d("postAppointment", "time now: " + c.getTime());

        Log.d("bugs", "name before = " + userName);
        Log.d("bugs", "id before = " + userID);

        getSharedPrefs();

        Log.d("bugs", "name after = " + userName);
        Log.d("bugs", "id after = " + userID);

        setReturnTypeSpinner();
        setPrioritySpinner();
        checkIfEditEmpty();
        checkDirectionOfIntent();
    }

    private void clinicAppt() {
        clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
        daySelected = AppointmentCalendarActivity.daySelected;
        clinicName = ApiRootModel.getInstance().getClinicMap().get(clinicID).getName();
        time = getIntent().getStringExtra("time");
        tvTime.setText(time);
        visitPrioritySpinner.setSelection(1);

        myCalendar.setTime(daySelected);
        tvDate.setText(dfDateMonthNameYear.format(daySelected));

        tvClinic.setText(clinicName);
    }

    private void homeVisitAppt() {
        daySelected = HomeVisitAppointmentActivity.daySelected;
        serviceOptionId = Integer.parseInt(getIntent().getStringExtra("serviceOptionId"));
        clinicName = ApiRootModel.getInstance().getServiceOptionsHomeMap().get(serviceOptionId).getName();
        tvTime.setVisibility(View.GONE);
        tvTimeTitle.setVisibility(View.GONE);
        visitReturnTypeSpinner.setVisibility(View.GONE);
        visitPrioritySpinner.setVisibility(View.GONE);
        tvPriorityTitle.setVisibility(View.GONE);
        tvReturnTitle.setVisibility(View.GONE);

        priority = "home-visit";

        myCalendar.setTime(daySelected);
        tvDate.setText(dfDateMonthNameYear.format(daySelected));

        tvClinic.setText(clinicName);
    }

    private void fromConfirm() {
        etUserName.setText(getIntent().getStringExtra("userName"));
        priority = getIntent().getStringExtra("priority");
        userID = Integer.parseInt(getIntent().getStringExtra("userId"));
        //clinicName = getIntent().getStringExtra("clinicName");
        //clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
        apptDate = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        if (priority.equals("home-visit"))
            homeVisitAppt();
        else if (priority.equals("scheduled"))
            clinicAppt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MYLOG", "In onResume CreateAppointment");
        checkDirectionOfIntent();
        //setReturnTypeSpinner();
        //setPrioritySpinner();
        checkIfEditEmpty();
    }

    private void checkDirectionOfIntent() {
        String intentOrigin = getIntent().getStringExtra("from");
        if (intentOrigin.equals("clinic-appointment")) {
            Log.d("bugs", "intent from clinic");
            clinicAppt();
        } else if (intentOrigin.equals("confirm")) {
            Log.d("bugs", "intent from confirm");
            fromConfirm();
        } else if (intentOrigin.equals("home-visit")) {
            Log.d("bugs", "intent from home visit");
            homeVisitAppt();
        }
    }

    private Boolean checkIfEditEmpty() {
        if (TextUtils.equals(String.valueOf(userID), "") || TextUtils.equals(etUserName.getText(), "")) {
            etUserName.setError("Field Empty");
            return true;
        } else
            return false;
    }

    private Boolean checkIfOkToGo() {
        if (userID != 0 &&
                visitPrioritySpinner.getSelectedItemPosition() != 0 &&
                visitReturnTypeSpinner.getSelectedItemPosition() != 0) {
            return true;
        } else
            return false;
    }

    private void getSharedPrefs() {
        prefs = getSharedPreferences("SMART", MODE_PRIVATE);

        if (prefs != null && prefs.getBoolean("reuse", false)) {
            userName = prefs.getString("name", null);
            userID = Integer.parseInt(prefs.getString("id", ""));
            visitType = prefs.getString("visit_type", null);
            hospitalNumber = prefs.getString("hospitalNumber", "");
            email = prefs.getString("email", "");
            sms = prefs.getString("mobile", "");

            etUserName.setText(userName);
        }

        Log.d("bugs", "name pref = " + prefs.getString("name", null));
        Log.d("bugs", "id pref = " + prefs.getString("id", ""));
    }

/*    private void setSharedPrefs(){
        SharedPreferences.Editor prefs = getSharedPreferences("SMART", MODE_PRIVATE).edit();
		prefs.putString("userName", ApiRootModel.getInstance().getServiceUsers().get(0).getPersonalFields().getName(););
		prefs.putString("id", ApiRootModel.getInstance().getServiceUsers().get(0).getId().toString());
		prefs.putBoolean("reuse", true);
		prefs.commit();
    }*/

    private void setPrioritySpinner() {
        visitPriorityAdapter = new AdapterSpinner(this,
                R.array.visit_priority_list,
                R.layout.spinner_layout,
                R.id.tv_spinner_item);
        visitPriorityAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitPrioritySpinner.setAdapter(visitPriorityAdapter);
    }

    private void setReturnTypeSpinner() {
        returnTypeAdapter = new AdapterSpinner(this,
                R.array.return_type_list,
                R.layout.spinner_layout,
                R.id.tv_spinner_item);
        returnTypeAdapter.setDropDownViewResource(R.layout.spinner_layout);
        visitReturnTypeSpinner.setAdapter(returnTypeAdapter);
    }

    private void showEmptyFieldDialog() {
        showProgressDialog(
                CreateAppointmentActivity.this,
                "Cannot proceed, \nSome fields are empty!");
        new CountDownTimer(2000, 1000) {
            @Override
            public void onFinish() {
                pd.dismiss();
            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    private void searchPatient(String serviceUserName) {
        api.getServiceUserByName(
                serviceUserName,
                ApiRootModel.getInstance().getLogin().getToken(),
                SmartApi.API_KEY,
                new Callback<ApiRootModel>() {
                    @Override
                    public void success(ApiRootModel apiRootModel, Response response) {
                        String name, hospitalNumber, dob;
                        List<String> searchResults = new ArrayList<>();
                        int id;
                        if (apiRootModel.getServiceUsers().size() != 0) {
                            ApiRootModel.getInstance().setServiceUsers(apiRootModel.getServiceUsers());
                            ApiRootModel.getInstance().setPregnancies(apiRootModel.getPregnancies());
                            ApiRootModel.getInstance().setBabies(apiRootModel.getBabies());
                            for (int i = 0; i < apiRootModel.getServiceUsers().size(); i++) {
                                ServiceUser serviceUserItem = apiRootModel.getServiceUsers().get(i);
                                serviceUserList.add(serviceUserItem);
                                name = serviceUserItem.getPersonalFields().getName();
                                hospitalNumber = serviceUserItem.getHospitalNumber();
                                dob = serviceUserItem.getPersonalFields().getDob();
                                id = serviceUserItem.getId();

                                Log.d("Retro", name + "\n" + hospitalNumber + "\n" + dob + "\n" + id);
                                idList.add(id);
                                searchResults.add(name + "\n" + hospitalNumber + "\n" + dob);
                            }
                            //postOrAnte();
                            //getRecentPregnancy();
                            pd.dismiss();
                            buildAlertDialog(searchResults);
                        } else {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "No search results found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retro", "retro failure error = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void buildAlertDialog(List<String> searchResults) {
        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(CreateAppointmentActivity.this);
        View convertView = (View) inflater.inflate(R.layout.list, null);
        ListView list = (ListView) convertView.findViewById(R.id.list_dialog);

        list.setOnItemClickListener(new onItemListener());

        alertDialog.setView(convertView);
        alertDialog.setTitle("Search Results");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                CreateAppointmentActivity.this,
                android.R.layout.simple_list_item_1,
                searchResults);
        list.setAdapter(adapter);
        ad = alertDialog.show();
    }

    private void postOrAnte() {
        visitType = "post-natal";        //TODO: this need to be changed
    }

    private void makeAlertDialog() {
        String dateWords = dfDateMonthNameYear.format(daySelected);
        String dateDay = dfDayShort.format(daySelected);

        Log.d("bugs", "making alertDialog");
        LayoutInflater inflater = getLayoutInflater();
        alertDialog = new AlertDialog.Builder(CreateAppointmentActivity.this);
        View convertView = (View) inflater.inflate(R.layout.dialog_confirm_appointment, null);
        TextView txtUserName = (TextView) convertView.findViewById(R.id.tv_confirm_name);
        TextView txtClinic = (TextView) convertView.findViewById(R.id.tv_confirm_location);
        TextView txtDateTime = (TextView) convertView.findViewById(R.id.tv_confirm_time);
        TextView txtEmailTo = (TextView) convertView.findViewById(R.id.tv_confirm_email);
        TextView txtSmsTo = (TextView) convertView.findViewById(R.id.tv_confirm_sms);

        txtUserName.setText(userName + " (" + hospitalNumber + ")");
        txtClinic.setText(clinicName);
        if (priority.equals("home-visit"))
            txtDateTime.setText(dateDay + ", " + dateWords);
        else if (priority.equals("scheduled"))
            txtDateTime.setText(time + " on " + dateWords);
        txtEmailTo.setText(email);
        txtSmsTo.setText(sms);

        Button btnYes = (Button) convertView.findViewById(R.id.btn_confirm_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("bugs", "yes 	 button clicked");
                showProgressDialog(CreateAppointmentActivity.this,
                        "Booking Appointment");
                postAppointment();
            }
        });
        Button btnNo = (Button) convertView.findViewById(R.id.btn_confirm_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });
        ImageView ivExit = (ImageView) convertView.findViewById(R.id.iv_exit_dialog);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.cancel();
            }
        });

        alertDialog.setView(convertView);
        ad = alertDialog.show();
    }

    private void postAppointment() {
        PostingData appointment = new PostingData();
        if (priority.equals("home-visit")) {
            Log.d("bugs", "homevisit");
            appointment.postAppointment(apptDate, userID, priority, visitType, returnType, serviceOptionId);
        } else if (priority.equals("scheduled")) {
            Log.d("bugs", "scheduled");
            int clinicID = Integer.parseInt(getIntent().getStringExtra("clinicID"));
            appointment.postAppointment(apptDate, time, userID, clinicID, priority, visitType, returnType);
        }

        api.postAppointment(
                appointment,
                ApiRootModel.getInstance().getLogin().getToken(),
                SmartApi.API_KEY,
                new Callback<ApiRootModel>() {
                    @Override
                    public void success(ApiRootModel apiRootModel, Response response) {
                        ApiRootModel.getInstance().addAppointment(apiRootModel.getAppointment());
                        if (returnType.equals("returning"))
                            addNewApptToMaps();
                        else if (returnType.equals("new"))
                            getAppointmentById(apiRootModel.getAppointment().getId());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Retrofit", "retro failure error = " + error);
                        if(error.getResponse().getStatus() == 422){
                            ApiRootModel body = (ApiRootModel) error.getBodyAs(ApiRootModel.class);
                            Toast.makeText(CreateAppointmentActivity.this,
                                    body.getError().getAppointmentTaken(), Toast.LENGTH_LONG).show();
                            ad.cancel();
                        }
                        pd.dismiss();
                    }
                }
        );
    }

    private void getAppointmentById(int apptId) {
        api.getAppointmentById(
                apptId + 1,
                ApiRootModel.getInstance().getLogin().getToken(),
                SmartApi.API_KEY,
                new Callback<ApiRootModel>() {
                    @Override
                    public void success(ApiRootModel apiRootModel, Response response) {
                        Log.d("retro", "getAppointmentById success");
                        ApiRootModel.getInstance().addAppointment(apiRootModel.getAppointment());
                        addNewApptToMaps();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "getAppointmentById failure = " + error);
                        pd.dismiss();
                    }
                }
        );
    }

    private void addNewApptToMaps() {
        List<Integer> clinicApptIdList;
        Map<String, List<Integer>> clinicVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> clinicVisitClinicDateApptIdMap = new HashMap<>();
        Map<Integer, Appointment> clinicVisitIdApptMap = new HashMap<>();

        List<Integer> homeApptIdList;
        Map<String, List<Integer>> homeVisitdateApptIdMap;
        Map<Integer, Map<String, List<Integer>>> homeVisitClinicDateApptIdMap = new HashMap<>();
        Map<Integer, Appointment> homeVisitIdApptMap = new HashMap<>();
        for (int i = 0; i < ApiRootModel.getInstance().getAppointments().size(); i++) {
            clinicApptIdList = new ArrayList<>();
            homeApptIdList = new ArrayList<>();
            clinicVisitdateApptIdMap = new HashMap<>();
            homeVisitdateApptIdMap = new HashMap<>();
            Appointment appt = ApiRootModel.getInstance().getAppointments().get(i);
            String apptDate = appt.getDate();
            int apptId = appt.getId();
            int clinicId = appt.getClinicId();
            int serviceOptionId = 0;
            if (appt.getServiceOptionIds().size() > 0) {
                serviceOptionId = appt.getServiceOptionIds().get(0);
            }

            if (appt.getPriority().equals("home-visit")) {
                Log.d("bugs", " appt ID = " + appt.getId());
                if (homeVisitClinicDateApptIdMap.get(serviceOptionId) != null) {
                    homeVisitdateApptIdMap = homeVisitClinicDateApptIdMap.get(serviceOptionId);
                    if (homeVisitdateApptIdMap.get(apptDate) != null) {
                        homeApptIdList = homeVisitdateApptIdMap.get(apptDate);
                    }
                }
                homeApptIdList.add(apptId);
                homeVisitdateApptIdMap.put(apptDate, homeApptIdList);

                homeVisitClinicDateApptIdMap.put(serviceOptionId, homeVisitdateApptIdMap);
                homeVisitIdApptMap.put(apptId, appt);
            } else {
                if (clinicVisitClinicDateApptIdMap.get(clinicId) != null) {
                    clinicVisitdateApptIdMap = clinicVisitClinicDateApptIdMap.get(clinicId);
                    if (clinicVisitdateApptIdMap.get(apptDate) != null) {
                        clinicApptIdList = clinicVisitdateApptIdMap.get(apptDate);
                    }
                }
                clinicApptIdList.add(apptId);
                clinicVisitdateApptIdMap.put(apptDate, clinicApptIdList);

                clinicVisitClinicDateApptIdMap.put(clinicId, clinicVisitdateApptIdMap);
                clinicVisitIdApptMap.put(apptId, appt);
            }
        }
        ApiRootModel.getInstance().setClinicVisitClinicDateApptIdMap(clinicVisitClinicDateApptIdMap);
        ApiRootModel.getInstance().setClinicVisitIdApptMap(clinicVisitIdApptMap);

        ApiRootModel.getInstance().setHomeVisitOptionDateApptIdMap(homeVisitClinicDateApptIdMap);
        ApiRootModel.getInstance().setHomeVisitIdApptMap(homeVisitIdApptMap);

        Intent intentClinic = new Intent(CreateAppointmentActivity.this, AppointmentCalendarActivity.class);
        Intent intentHome = new Intent(CreateAppointmentActivity.this, HomeVisitAppointmentActivity.class);

        if (priority.equals("home-visit"))
            startActivity(intentHome);
        else if (priority.equals("scheduled"))
            startActivity(intentClinic);
        else if (priority.equals("drop-in"))
            startActivity(intentClinic);

        ad.cancel();
        pd.dismiss();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class ButtonClick implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_confirm_appointment:
                    apptDate = dfDateOnly.format(myCalendar.getTime());
                    passOptions.setDaySelected(myCalendar.getTime());

                    if (priority.equals("home-visit")) {
                        if (!checkIfEditEmpty())
                            makeAlertDialog();
                        else
                            showEmptyFieldDialog();
                    } else if (priority.equals("scheduled")) {
                        if (checkIfOkToGo())
                            makeAlertDialog();
                        else
                            showEmptyFieldDialog();
                    }
                    break;
                case R.id.btn_user_search:
                    if (!checkIfEditEmpty()) {
                        hideKeyboard();
                        userID = 0;
                        userName = etUserName.getText().toString();
                        checkIfEditEmpty();
                        showProgressDialog(CreateAppointmentActivity.this, "Fetching Information");
                        searchPatient(userName);
                    }
                    break;
            }
        }
    }

    private class MySpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.spnr_visit_return_type:
                    switch (position) {
                        case 0:
                            break;
                        case 1:
                            returnType = "returning";
                            break;
                        case 2:
                            returnType = "new";
                            break;
                    }
                    break;
                case R.id.spnr_visit_priority:
                    switch (position) {
                        case 0:
                            //Select Visit Priority
                            break;
                        case 1:
                            priority = "scheduled";
                            //Scheduled
                            break;
                        case 2:
                            priority = "drop-in";
                            //Drop-In
                            break;
                    }
                    break;
                case R.id.list_dialog:
                    Log.d("bugs", "list position is: " + position);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private class onItemListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.list_dialog:
                    hideKeyboard();
                    ServiceUser serviceUser = serviceUserList.get(position);
                    ApiRootModel.getInstance().setServiceUser(serviceUser);
                    userName = serviceUser.getPersonalFields().getName();
                    hospitalNumber = serviceUser.getHospitalNumber();
                    email = serviceUser.getPersonalFields().getEmail();
                    sms = serviceUser.getPersonalFields().getMobilePhone();

                    etUserName.setText(userName);
                    userID = serviceUser.getId();
                    postOrAnte();
                    ad.cancel();
                    break;
            }
        }
    }
}
