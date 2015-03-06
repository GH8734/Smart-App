package ie.teamchile.smartapp;


import org.json.JSONException;
import org.json.JSONObject;

import utility.ServiceUserSingleton;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AnteNatalActivity extends MenuInheritActivity {
	private TextView ageAnte;
	private TextView nameAntiNatal, gestationAntiNatal, parityAntiNatal
	                ,deliveryTime, bloodGroup, rhesus, obstetricHistory;
	private ImageView userImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ante_natal);
		
		userImage = (ImageView)findViewById(R.id.user_image_anti_natal);
		userImage.setOnClickListener(new AntiNatalOptions());
		
		ageAnte = (TextView)findViewById(R.id.age_ante_natal);
		nameAntiNatal = (TextView)findViewById(R.id.name_anti_natal);
		gestationAntiNatal = (TextView)findViewById(R.id.gestation);
		parityAntiNatal = (TextView)findViewById(R.id.parity);
		deliveryTime = (TextView)findViewById(R.id.deliveryTime);
		bloodGroup = (TextView)findViewById(R.id.blood_group);
		rhesus = (TextView)findViewById(R.id.rhesus);
		obstetricHistory = (TextView)findViewById(R.id.obstetric_history_ante_natal);
	
		String name = ServiceUserSingleton.getInstance().getName();
		parityAntiNatal.setText(ServiceUserSingleton.getInstance().getParity());
		gestationAntiNatal.setText(ServiceUserSingleton.getInstance().getGestation());
		rhesus.setText(ServiceUserSingleton.getInstance().getRhesus());
		bloodGroup.setText(ServiceUserSingleton.getInstance().getBloodGroup());
		deliveryTime.setText(ServiceUserSingleton.getInstance().getEstimatedDeliveryDate());
		obstetricHistory.setText(ServiceUserSingleton.getInstance().getObstreticHistory());

		ServiceUserActivity aa = new ServiceUserActivity();
		String age = ServiceUserSingleton.getInstance().getAge();
		int anteNatalAge = aa.getAge(age);
		String theAge = String.valueOf(anteNatalAge);
		
		ageAnte.setText(theAge);
		nameAntiNatal.setText(name);

	}

	private class AntiNatalOptions implements View.OnClickListener {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.user_image_anti_natal:
				Intent intent = new Intent(AnteNatalActivity.this, ServiceUserActivity.class);
				startActivity(intent);
				break;
			}
		}
	 }
}
