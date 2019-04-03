package uoit.ca.ccproject;

import android.app.Activity;
import android.app.NotificationChannel;
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
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtView;
    private TextView txtRec;
    private TextView txtLinks;
    private NotificationReceiver nReceiver;
    TimePickerDialog.OnTimeSetListener myTimeListener;
    TimePickerDialog timePickerDialog;
    Calendar myCalender;


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
            txtRec = findViewById(R.id.txtRec);
            txtLinks = findViewById(R.id.txtLinks);

            nReceiver = new NotificationReceiver();
            pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            editor = pref.edit();
            IntentFilter filter = new IntentFilter();
            filter.addAction("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
            registerReceiver(nReceiver, filter);

            showHourPicker();

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
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //if (view.isShown()) {
                final Calendar c = Calendar.getInstance();
                c.set(mYear,mMonth,mDay,hour,minute);
                myCalender = c;

                myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);

                    hr = hourOfDay;
                    min = minute;

                    Log.e("picker", String.valueOf(hourOfDay));
                    Log.e("picker",String.valueOf(minute));
                editor.putInt("hour",hourOfDay);
                editor.putInt("minute",minute);
                editor.apply();//I added this line
                editor.commit();

                Log.e("editor", String.valueOf(pref.getInt("hour",1)));
                Log.e("editor",String.valueOf(pref.getInt("minute",1)));

                //}
            }

        };

        timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, myTimeListener, hour, minute, true);
        timePickerDialog.setTitle("Choose Time:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
        //editor.putInt("hour",hr);
        //editor.putInt("minute",min);
        //myCalender.get(Calendar.HOUR_OF_DAY);
        //editor.putInt("hour",myCalender.get(Calendar.HOUR_OF_DAY));
        //editor.putInt("minute",myCalender.get(Calendar.MINUTE));
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("test", "receive broadcast");

            String temp = intent.getStringExtra("notification_event") + "n" + txtView.getText();
            txtView.setText(temp);
            if(intent.getStringExtra("rec")!=null){
                txtRec.setText(intent.getStringExtra("rec"));
            }
            if(intent.getBundleExtra("bundle")!=null){
                try {
                    Bundle bundle = intent.getBundleExtra("bundle");
                    ArrayList<Spanned> links = (ArrayList<Spanned>) bundle.getSerializable("links");
                    assert links != null;
                    txtLinks.setText("");
                    for(int i=0; i<links.size();i++){
                        txtLinks.append(links.get(i));
                    }
                    txtLinks.setMovementMethod(LinkMovementMethod.getInstance());
                    int notifyID = 1;
                    String CHANNEL_ID = "my_channel_01";// The id of the channel.
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "recChannel", importance);

                    NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        nManager.createNotificationChannel(mChannel);
                    }
                    NotificationCompat.Builder ncomp = new NotificationCompat.Builder(getApplicationContext(), "M_CH_ID");
                    ncomp.setContentTitle("New Recommendations");
                    //ncomp.setContentText("Notification Listener Service Example");
                    ncomp.setContentText(txtLinks.getText());
                    ncomp.setTicker("Notification Listener Service Example");
                    ncomp.setSmallIcon(R.mipmap.ic_launcher);
                    ncomp.setAutoCancel(true);
                    ncomp.setChannelId(CHANNEL_ID).build();
                    nManager.notify((int) System.currentTimeMillis(), ncomp.build());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
