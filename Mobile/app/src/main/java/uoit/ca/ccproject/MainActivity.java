package uoit.ca.ccproject;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {

    private TextView txtView;
    private NotificationReceiver nReceiver;
    int hr;
    int min;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            txtView = (TextView) findViewById(R.id.textView);
            nReceiver = new NotificationReceiver();
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            editor = pref.edit();
            IntentFilter filter = new IntentFilter();
            filter.addAction("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
            registerReceiver(nReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }

    public static void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm = ctxt.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(i, 0);

        for (ResolveInfo resolveInfo : matches) {
            Intent explicit = new Intent(i);
            ComponentName cn =
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            explicit.setComponent(cn);
            ctxt.sendBroadcast(explicit);
        }
    }
    public void showHourPicker() {
        final Calendar myCalender = Calendar.getInstance();
        final int hour = myCalender.get(Calendar.HOUR_OF_DAY);
        int minute = myCalender.get(Calendar.MINUTE);


        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);
                    hr = hourOfDay;
                    min = minute;
                    editor.putInt("hour",hr);
                    editor.putInt("minute",minute);
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
        timePickerDialog.setTitle("Choose hour:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }
    public void buttonClicked(View v) {
        try {
            Log.d("test", "button clicked");

            if (v.getId() == R.id.btnCreateNotify) {
                Log.d("test", "notify");

                NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this, "M_CH_ID");
                ncomp.setContentTitle("My Notification");
                ncomp.setContentText("Notification Listener Service Example");
                ncomp.setTicker("Notification Listener Service Example");
                ncomp.setSmallIcon(R.mipmap.ic_launcher);
                ncomp.setAutoCancel(true);
                nManager.notify((int) System.currentTimeMillis(), ncomp.build());
            } else if (v.getId() == R.id.btnClearNotify) {
                Log.d("test", "clear");

                Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
                i.putExtra("command", "clearall");
                //sendImplicitBroadcast(this,i);
                sendBroadcast(i);

            } else if (v.getId() == R.id.btnListNotify) {
                Log.d("test", "list");

                Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
                i.putExtra("command", "list");
                sendBroadcast(i);
                //sendImplicitBroadcast(this,i);
            }
            else if (v.getId() == R.id.btnSetTime) {
               showHourPicker();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("test", "receive broadcast");

            String temp = intent.getStringExtra("notification_event") + "n" + txtView.getText();
            txtView.setText(temp);
        }
    }


}
