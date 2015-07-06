package ie.teamchile.smartapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.teamchile.smartapp.R;
import ie.teamchile.smartapp.model.BaseModel;
import ie.teamchile.smartapp.model.Clinic;
import ie.teamchile.smartapp.model.ClinicTimeRecord;
import ie.teamchile.smartapp.model.PostingData;
import ie.teamchile.smartapp.util.NotKeys;
import ie.teamchile.smartapp.util.SmartApi;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class ClinicTimeRecordActivity extends BaseActivity {
    private List<Integer> clinicStopped = new ArrayList<>();
    private List<Integer> clinicStarted = new ArrayList<>();
    private List<Integer> clinicNotStarted = new ArrayList<>();
    private List<Integer> idList = new ArrayList<>();
    private List<String> clinicNotStartedName = new ArrayList<>();
    private List<String> clinicStartedName = new ArrayList<>();
    private List<String> clinicStoppedName = new ArrayList<>();
    private Map<String, List<Integer>> clinicDayMap = new HashMap<>();
    private Map<Integer, Clinic> clinicIdMap = new HashMap<>();
    private List<ClinicTimeRecord> clinicTimeRecords = new ArrayList<>();
    private ListView lvNotStarted;
    private ListView lvStarted;
    private ListView lvStopped;
    private String todayDay;
    private String todayDate;
    private String now;
    private String then;
    private String date;
    private int clinicStartedId;
    private int clinicNotStartedId;
    private int clinicStoppedId;
    private Button btnStartClinic, btnStartClinicDisable;
    private Button btnStopClinic, btnStopClinicDisable;
    private Button btnResetRecord;
    private ArrayAdapter adapterNotStart;
    private ArrayAdapter adapterStart;
    private ArrayAdapter adapterStop;
    private int recordId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentForNav(R.layout.activity_clinic_time_record);
        c = Calendar.getInstance();
        //todayDay = dfDayLong.format(c.getTime());
        todayDay = "Monday";
        todayDate = dfDateOnly.format(c.getTime());
        Log.d("SMART", "today = " + todayDay);
        lvNotStarted = (ListView) findViewById(R.id.lv_clinics_not_started);
        lvNotStarted.setOnItemClickListener(new ItemClicky());
        lvStarted = (ListView) findViewById(R.id.lv_clinics_started);
        lvStarted.setOnItemClickListener(new ItemClicky());
        lvStopped = (ListView) findViewById(R.id.lv_clinics_stopped);
        lvStopped.setOnItemClickListener(new ItemClicky());
        btnStartClinic = (Button) findViewById(R.id.btn_start_clinic);
        btnStartClinic.setOnClickListener(new ButtonClicky());
        btnStopClinic = (Button) findViewById(R.id.btn_stop_clinic);
        btnStopClinic.setOnClickListener(new ButtonClicky());
        btnStartClinicDisable = (Button) findViewById(R.id.btn_start_clinic_disable);
        btnStopClinicDisable = (Button) findViewById(R.id.btn_stop_clinic_disable);
        btnResetRecord = (Button) findViewById(R.id.btn_reset_clinic_record);
        btnResetRecord.setOnClickListener(new ButtonClicky());

        btnStartClinicDisable.setEnabled(false);
        btnStopClinicDisable.setEnabled(false);

        btnStartClinicDisable.setVisibility(View.GONE);
        btnStopClinicDisable.setVisibility(View.GONE);

        setActionBarTitle("Start/Stop Clinics");

        if (BaseModel.getInstance().getClinicStarted().size() == 0) {
            Log.d("bugs", "time records null");
            getDataFromDb();
        } else {
            Log.d("bugs", "time records not null");
            getDataFromSingle();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("bugs", "setting data to singleton");

        BaseModel.getInstance().setClinicTimeRecords(clinicTimeRecords);
        BaseModel.getInstance().setClinicStopped(clinicStopped);
        BaseModel.getInstance().setClinicStarted(clinicStarted);
        BaseModel.getInstance().setClinicNotStarted(clinicNotStarted);
        BaseModel.getInstance().setClinicMap(clinicIdMap);
        BaseModel.getInstance().setClinicDayMap(clinicDayMap);
    }

    private void getDataFromSingle() {
        Log.d("bugs", "getting data from singleton");

        clinicTimeRecords = BaseModel.getInstance().getClinicTimeRecords();
        clinicStopped = BaseModel.getInstance().getClinicStopped();
        clinicStarted = BaseModel.getInstance().getClinicStarted();
        clinicNotStarted = BaseModel.getInstance().getClinicNotStarted();
        clinicIdMap = BaseModel.getInstance().getClinicMap();
        clinicDayMap = BaseModel.getInstance().getClinicDayMap();

        for (int i = 0; i < clinicNotStarted.size(); i++) {
            getTimeRecords(clinicNotStarted.get(i), todayDate);
        }

        for (int i = 0; i < clinicStarted.size(); i++) {
            getTimeRecords(clinicStarted.get(i), todayDate);
        }

        setNotStartedList();
        setStartedList();
        setStoppedList();
    }

    private void getDataFromDb() {
        Log.d("bugs", "getting data from db");

        List<Clinic> clinics = BaseModel.getInstance().getClinics();

        for (int i = 0; i < clinics.size(); i++) {
            List<String> trueDays = clinics.get(i).getTrueDays();
            for (int j = 0; j < trueDays.size(); j++) {
                Clinic clinic = clinics.get(i);
                int clinicId = clinics.get(i).getId();
                clinicIdMap.put(clinicId, clinic);

                if (clinicDayMap.get(trueDays.get(j)) != null) {
                    idList = new ArrayList<>();
                    idList = clinicDayMap.get(trueDays.get(j));
                    idList.add(clinicId);
                    clinicDayMap.put(trueDays.get(j), idList);
                } else {
                    idList = new ArrayList<>();
                    idList.add(clinicId);
                    clinicDayMap.put(trueDays.get(j), idList);
                }
            }
        }
        Log.d("Clinic", "Record map = " + clinicDayMap);

        idList = clinicDayMap.get(todayDay);

        if (clinicDayMap.containsKey(todayDay)) {
            for (int i = 0; i < idList.size(); i++) {
                getTimeRecords(idList.get(i), todayDate);
            }
        } else {
            setNotStartedList();
            setStartedList();
            setStoppedList();
        }

    }

    private void getTimeRecords(final int clinicId, String date) {
        api.getTimeRecords(
                clinicId,
                date,
                BaseModel.getInstance().getLogin().getToken(),
                NotKeys.API_KEY,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("retro", "get time record success");
                        if (baseModel.getClinicTimeRecords().size() > 0) {
                            clinicTimeRecords.add(baseModel.getClinicTimeRecords().get(0));
                            if (baseModel.getClinicTimeRecords().get(0).getEndTime() == null) {
                                if (!clinicStarted.contains(clinicId))
                                    clinicStarted.add(clinicId);
                            } else if (baseModel.getClinicTimeRecords().get(0).getEndTime() == null &&
                                    baseModel.getClinicTimeRecords().get(0).getStartTime() == null) {
                                clinicNotStarted.add(clinicId);
                            } else {
                                if (!clinicStopped.contains(clinicId))
                                    clinicStopped.add(clinicId);
                            }
                        } else {
                            if (!clinicNotStarted.contains(clinicId)) {
                                clinicNotStarted.add(clinicId);
                            }
                        }

                        setNotStartedList();
                        setStartedList();
                        setStoppedList();

                        Log.d("bugs", "clinicStopped = " + clinicStopped);
                        Log.d("bugs", "clinicStarted = " + clinicStarted);
                        Log.d("bugs", "clinicNotStarted = " + clinicNotStarted);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("retro", "get time record failure = " + error);
                    }
                }
        );
    }

    private void putStartTime(String now, final int clinicId) {
        PostingData timeRecord = new PostingData();
        timeRecord.putStartTimeRecord(
                now,
                clinicId,
                date);

        showProgressDialog(ClinicTimeRecordActivity.this, "Updating Clinic Time Records");

        api.postTimeRecords(
                timeRecord,
                clinicId,
                BaseModel.getInstance().getLogin().getToken(),
                NotKeys.API_KEY,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("SMART", "retro success");
                        clinicTimeRecords.add(baseModel.getClinicTimeRecord());
                        clinicNotStarted.remove(clinicNotStarted.indexOf(clinicId));
                        clinicStarted.add(clinicId);
                        setNotStartedList();
                        setStartedList();
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("SMART", "retro error = " + error.getResponse());
                        pd.dismiss();
                    }
                });
    }

    private void putEndTime(String then, String date, final int clinicId) {
        for (int i = 0; i < clinicTimeRecords.size(); i++) {
            if (clinicTimeRecords.get(i).getClinicId() == clinicId) {
                recordId = clinicTimeRecords.get(i).getId();
            }
        }

        PostingData timeRecord = new PostingData();
        timeRecord.putEndTimeRecord(
                then,
                date,
                clinicId);

        showProgressDialog(ClinicTimeRecordActivity.this, "Updating Clinic Time Records");

        api.putTimeRecords(
                timeRecord,
                clinicId,
                recordId,
                BaseModel.getInstance().getLogin().getToken(),
                NotKeys.API_KEY,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("SMART", "retro success");
                        for (int i = 0; i < clinicTimeRecords.size(); i++) {
                            if (clinicTimeRecords.get(i).getId() == recordId) {
                                Log.d("bugs", "record id = " + clinicTimeRecords.get(i).getId());
                                clinicTimeRecords.remove(i);
                                clinicStarted.remove(clinicStarted.indexOf(clinicId));
                                clinicStopped.add(clinicId);
                                setStartedList();
                                setStoppedList();
                            }
                        }
                        clinicTimeRecords.add(baseModel.getClinicTimeRecord());
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("SMART", "retro error = " + error.getResponse());
                        pd.dismiss();
                    }
                });
    }

    private void resetRecord(final int clinicId) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(NotKeys.BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient())
                .build();

        api = restAdapter.create(SmartApi.class);

        for (int i = 0; i < clinicTimeRecords.size(); i++) {
            if (clinicTimeRecords.get(i).getClinicId() == clinicId) {
                recordId = clinicTimeRecords.get(i).getId();
            }
        }

        PostingData timeRecord = new PostingData();
        timeRecord.resetTimeRecord(
                "",
                "",
                date,
                clinicId);

        showProgressDialog(ClinicTimeRecordActivity.this, "Updating Clinic Time Records");

        api.resetTimeRecordById(
                timeRecord,
                clinicId,
                recordId,
                BaseModel.getInstance().getLogin().getToken(),
                NotKeys.API_KEY,
                new Callback<BaseModel>() {
                    @Override
                    public void success(BaseModel baseModel, Response response) {
                        Log.d("SMART", "retro success");
                        for (int i = 0; i < clinicTimeRecords.size(); i++) {
                            if (clinicTimeRecords.get(i).getId() == recordId) {
                                Log.d("bugs", "record id = " + clinicTimeRecords.get(i).getId());
                                clinicTimeRecords.remove(i);
                                clinicStopped.remove(clinicStopped.indexOf(clinicId));
                                clinicNotStarted.add(clinicId);
                                setStartedList();
                                setStoppedList();
                                setNotStartedList();
                            }
                        }
                        clinicTimeRecords.add(baseModel.getClinicTimeRecord());
                        pd.dismiss();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("SMART", "retro error = " + error.getResponse());
                        pd.dismiss();
                    }
                });
    }


    private void setNotStartedList() {
        String clinicName = new String();
        clinicNotStartedName = new ArrayList<>();
        if (clinicNotStarted.size() == 0) {
            clinicNotStartedName.add("No clinics to be started");
            lvNotStarted.setEnabled(false);
            btnStartClinic.setVisibility(View.GONE);
            btnStartClinicDisable.setVisibility(View.VISIBLE);
        } else {
            lvNotStarted.setEnabled(true);
            btnStartClinic.setVisibility(View.VISIBLE);
            btnStartClinicDisable.setVisibility(View.GONE);
            for (int i = 0; i < clinicNotStarted.size(); i++) {
                clinicName = clinicIdMap.get(clinicNotStarted.get(i)).getName();
                int clinicId = clinicNotStarted.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicNotStartedName.add(clinicName);
            }
        }

        adapterNotStart = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicNotStartedName);

        adapterNotStart.notifyDataSetChanged();
        lvNotStarted.setAdapter(adapterNotStart);
    }

    private void setStartedList() {
        clinicStartedName = new ArrayList<>();
        if (clinicStarted.size() == 0) {
            clinicStartedName.add("No clinics currently started");
            lvStarted.setEnabled(false);
            btnStopClinic.setVisibility(View.GONE);
            btnStopClinicDisable.setVisibility(View.VISIBLE);
        } else {
            lvStarted.setEnabled(true);
            btnStopClinic.setVisibility(View.VISIBLE);
            btnStopClinicDisable.setVisibility(View.GONE);
            for (int i = 0; i < clinicStarted.size(); i++) {
                String clinicName = clinicIdMap.get(clinicStarted.get(i)).getName();
                int clinicId = clinicStarted.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicStartedName.add(clinicName);
            }
        }

        adapterStart = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicStartedName);

        adapterStart.notifyDataSetChanged();
        lvStarted.setAdapter(adapterStart);
    }

    private void setStoppedList() {
        clinicStoppedName = new ArrayList<>();

        if (clinicStopped.size() == 0) {
            clinicStoppedName.add("No clinics currently stopped");
        } else {
            for (int i = 0; i < clinicStopped.size(); i++) {
                String clinicName = clinicIdMap.get(clinicStopped.get(i)).getName();
                int clinicId = clinicStopped.get(i);
                switch (clinicId) {
                    case 6:
                        clinicName += " (Domino)";
                        break;
                    case 10:
                        clinicName += " (Satellite)";
                        break;
                }
                clinicStoppedName.add(clinicName);
            }
        }

        adapterStop = new ArrayAdapter(
                ClinicTimeRecordActivity.this,
                android.R.layout.simple_list_item_1,
                clinicStoppedName);

        adapterStop.notifyDataSetChanged();
        lvStopped.setAdapter(adapterStop);
    }

    private void sortListApha(List<String> badList) {
        Collections.sort(badList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });
    }

    private class ItemClicky implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.lv_clinics_not_started:
                    clinicStartedId = 0;
                    adapterStart.notifyDataSetChanged();
                    lvStarted.setAdapter(adapterStart);
                    Log.d("bugs", "not started clinic list pressed");
                    clinicNotStartedId = clinicNotStarted.get(position);
                    Log.d("bugs", "clinicNotStartedId = " + clinicNotStartedId);
                    break;
                case R.id.lv_clinics_started:
                    clinicNotStartedId = 0;
                    adapterNotStart.notifyDataSetChanged();
                    lvNotStarted.setAdapter(adapterNotStart);
                    Log.d("bugs", "started clinic list pressed");
                    clinicStartedId = clinicStarted.get(position);
                    Log.d("bugs", "clinicStartedId = " + clinicStartedId);
                    break;
                case R.id.lv_clinics_stopped:
                    clinicStoppedId = clinicStopped.get(position);
                    Log.d("timeRecord", "reset clinic id = " + clinicStoppedId);
                    break;
            }
        }
    }

    private class ButtonClicky implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            c = Calendar.getInstance();
            now = dfDateTime.format(c.getTime());
            c.add(Calendar.HOUR, 1);
            then = dfDateTime.format(c.getTime());
            date = dfDateOnly.format(c.getTime());
            switch (v.getId()) {
                case R.id.btn_start_clinic:
                    if (clinicNotStartedId != 0) {
                        Log.d("bugs", "start clinic btn pressed");
                        putStartTime(now, clinicNotStartedId);
                    }
                    break;
                case R.id.btn_stop_clinic:
                    if (clinicStartedId != 0) {
                        putEndTime(then, date, clinicStartedId);
                        Log.d("bugs", "stop clinic btn pressed");
                    }
                    break;
                case R.id.btn_reset_clinic_record:
                    Log.d("timeRecord", "reset clinic id = " + clinicStoppedId);
                    resetRecord(clinicStoppedId);
                    break;
            }
            clinicNotStartedId = 0;
            clinicStartedId = 0;
        }
    }
}
