package ie.teamchile.smartapp.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/*
"clinics": [
            {
                "address": "Leopardstown Shopping Centre, Unit 12, Ballyogan Road, Dublin 18", 
                "announcement_ids": [], 
                "appointment_interval": 15, 
                "closing_time": "15:00:00", 
                "days": {
                    "friday": false, 
                    "monday": false, 
                    "saturday": false, 
                    "sunday": false, 
                    "thursday": false, 
                    "tuesday": true, 
                    "wednesday": false
                }, 
                "id": 2, 
                "links": {
                    "announcements": "announcements", 
                    "service_options": "/service_options"
                }, 
                "name": "Leopardstown", 
                "opening_time": "09:00:00", 
                "recurrence": "weekly", 
                "service_option_ids": [
                    1
                ], 
                "type": "booking"
            }
        ]
*/

public class ClinicSingleton {	
	private static ClinicSingleton singleInstance;
	private DateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
	private DateFormat sdfHHmm = new SimpleDateFormat("HH:mm", Locale.getDefault());
	private HashMap<String, JSONObject> idHash;
	private ArrayList<JSONObject> jsonValues;
	private JsonParseHelper help = new JsonParseHelper();
	private String days[] = { "monday", "tuesday", "wednesday", "thursday",
			"friday", "saturday", "sunday" };

	private ClinicSingleton() {
	}	
	
	public static synchronized ClinicSingleton getInstance() {
		if(singleInstance == null) {
			singleInstance = new ClinicSingleton();
		}
		return singleInstance;
	}	
	
	public void setHashMapofIdClinic(JSONArray clinicArray) {
		jsonValues = new ArrayList<JSONObject>();
		idHash = new HashMap<String, JSONObject>();
		String id; // key
		JSONObject clinic; // value
		
		try {
			/**
			 * iterates through input JSONArray
			 * parses JSONObjects from JSONArray
			 * puts them into an ArrayList of JSONObjects
			 */
			for (int i = 0; i < clinicArray.length(); i++) {		 
				jsonValues.add(clinicArray.getJSONObject(i));		
			}	
			/**
			 * iterates through ArrayList
			 * parses out id and clinic String
			 * sets to a HashMap of ID as Key and Clinic String as value
			 */
			for (int i = 0; i < jsonValues.size(); i++) {
				id = String.valueOf((jsonValues.get(i).getInt("id")));
				clinic = jsonValues.get(i);			
				idHash.put(id, clinic);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("singleton", "idhash of clinics: " + idHash);
	}
	
	public HashMap<String, JSONObject> getHashMapofIdClinic(){
		return idHash;
	}

	public String getIDFromName(String name) {
		JSONObject json;
		String nameFromDB = "";
		for (int i = 1; i <= idHash.size(); i++) {
			try {
				json = idHash.get(String.valueOf(i));
				nameFromDB = json.get("name").toString();

				if (nameFromDB.equals(name)) {
					return String.valueOf(i);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String getAddress(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "address");
	}
	
	public String getAnnouncementID(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "announcement_ids");
	}
	
	public String getAppointmentInterval(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "appointment_interval");
	}

	public String getClosingTime(String id){
		JSONObject json = idHash.get(id);
		return removeSeconds(help.jsonParseHelper(json, "clinics", "closing_time"));
	}
	
	public List<String> getDays(String id){
		JSONObject json = idHash.get(id);

		try {
			json.get("days");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> getTrueDays(String id){
		JSONObject json = idHash.get(id);
		JSONObject otherJson = new JSONObject();
		List<String> daysTrue = new ArrayList<String>();
		try {
			otherJson = json.getJSONObject("days");

			for (int j = 0; j < days.length; j++) {
				if (otherJson.getBoolean(days[j]) == true) {
					daysTrue.add(days[j].toLowerCase(Locale.getDefault()));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return daysTrue;
	}
	
	public String getClinicID(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "id");
	}
	
	/**
	 * 
	 * TODO: do something with links
	 *  "links": {
     *           "announcements": "announcements", 
     *           "service_options": "/service_options"
     *       }, 
     *       
	 */
	public String getLinks(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "links");
	}
	
	public String getClinicName(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "name");
	}
	
	public String getOpeningTime(String id){
		JSONObject json = idHash.get(id);
		return removeSeconds(help.jsonParseHelper(json, "clinics", "opening_time"));
	}
	
	public String getRecurrence(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "recurrence");
	}
	
	public String getServiceOptionIDs(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "service_option_ids");
	}

	public String getClinicType(String id){
		JSONObject json = idHash.get(id);
		return help.jsonParseHelper(json, "clinics", "type");
	}
	
	public String removeSeconds(String time){
		Date oldTime = null;
		try {
			oldTime = sdfTime.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdfHHmm.format(oldTime);
	}
}