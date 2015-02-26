package utility;

import ie.teamchile.smartapp.R;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastAlert{
	private TextView tv;
	private View layout;

	public ToastAlert(Context context, String message) {
		Toast ImageToast = new Toast(context);
	    LinearLayout toastLayout = new LinearLayout(
	            context);
	    toastLayout.setOrientation(LinearLayout.HORIZONTAL);
	    toastLayout.setBackgroundColor(Color.BLUE);
	    ImageView image = new ImageView(context);
	    TextView tv = new TextView(context);
	    tv.setTextColor(Color.WHITE);
	    tv.setText(message);
	    image.setImageResource(R.drawable.ic_launcher);
	    ImageToast.setGravity(Gravity.TOP, 0, 0);
	    toastLayout.addView(tv);
	    toastLayout.addView(image);
	    ImageToast.setView(toastLayout);
	    ImageToast.setDuration(Toast.LENGTH_SHORT);
	    ImageToast.show();
	}
}