package uoit.ca.ccproject;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

public class NLService extends NotificationListenerService {
    public ArrayList<String> playerList;
    private String TAG = "NLService";
    private NLServiceReceiver nlservicereciver;
    String userID;
    int hr;
    int min;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        userID = "1";
        handler = new Handler(getApplicationContext().getMainLooper());

        IntentFilter filter = new IntentFilter();
        filter.addAction("uoit.ca.ccproject.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver, filter);
        musicPlayerList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            Log.i(TAG, "**********  onNotificationPosted");
            Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
            Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
            i.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "n");


            String str = String.format("%02d:%02d", pref.getInt("hour",1), pref.getInt("minute",1));
            //Date time1 = new SimpleDateFormat("HH:mm").parse(str);
            //String current = new SimpleDateFormat("HH:mm").format(new Date());
            Calendar c = Calendar.getInstance();
            int currentHour = c.get(Calendar.HOUR_OF_DAY);
            int currentMinute = c.get(Calendar.MINUTE);
            Log.e(TAG, String.valueOf(currentHour));
            Log.e(TAG, String.valueOf(pref.getInt("hour",1)));

            if(currentHour ==pref.getInt("hour",1) ){
                getMusicAsync gma = new getMusicAsync();
                gma.execute();
            }



            sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR " + e);

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            Log.i(TAG, "********** onNotificationRemoved");
            Log.i(TAG, "ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
            Intent i = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
            i.putExtra("notification_event", "onNotificationRemoved :" + sbn.getPackageName() + "n");

            sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR " + e);

        }
    }

    public void musicPlayerList() {
        playerList = new ArrayList<String>();
        playerList.add("spotify");
        playerList.add("blackplayer");
        playerList.add("music");
        playerList.add("player");

    }

    public class getMusicAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HttpHandlerGet sh = new HttpHandlerGet();
            // Making a request to url and getting response
            //String url = "http://api.onemusicapi.com/20151208/release?title=" + title.replaceAll(" ","+").toLowerCase() + "&artist=" + text.replaceAll(" ","+").toLowerCase() + "&user_key=511f13fd5f3daea12fe39976ef0ba7ca";
            String url = "http://99.79.42.247/cloud/Song/" + userID;
            final String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url GET: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //Log.e(TAG, "Response from url: " + jsonStr);
                    JSONArray results = new JSONArray(jsonStr);
                    String recommendations ="";
                    ArrayList<Spanned> links = new ArrayList<Spanned>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);

                        String videoID = c.getString("videoID");
                        String videoTitle = c.getString("videoTitle");

                        JSONObject s = c.getJSONObject("song");

                        String songNmae = s.getString("songName");
                        String songID = s.getString("songID");
                        String artist = s.getString("artist");
                        String genre = s.getString("genre");
                        recommendations = recommendations + String.valueOf(i+1)+": "+ songNmae+ " "+artist+"\n "+"Genre: "+ genre+"\n";
                        Spanned link = Html.fromHtml("<a href='"+videoID+"'>"+videoTitle+"</a> ");
                        links.add(link);
                    }

                    final String finalRecommendations = recommendations;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    finalRecommendations,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent i3 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                    i3.putExtra("rec", finalRecommendations);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("links",links);
                    i3.putExtra("bundle", bundle);

                    sendBroadcast(i3);

                    Log.e(TAG, "GET successful ");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR GET" + e);

                }
            }
            return null;
        }
    }

    public class sendMusicAsync extends AsyncTask<Void, Void, String> {
        String title;
        String text;

        public sendMusicAsync(String ti, String te) {
            title = ti;
            text = te;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e(TAG, "Title: " + title.replaceAll(" ", "+").toLowerCase());
            Log.e(TAG, "Text: " + text.replaceAll(" ", "+").toLowerCase());

            HttpHandlerPost sh = new HttpHandlerPost();
            // Making a request to url and getting response
            //String url = "http://api.onemusicapi.com/20151208/release?title=" + title.replaceAll(" ","+").toLowerCase() + "&artist=" + text.replaceAll(" ","+").toLowerCase() + "&user_key=511f13fd5f3daea12fe39976ef0ba7ca";
            String url = "http://99.79.42.247/cloud/ListenedTo?userID=" + userID + "&songName=" + title.replaceAll(" ", "%20").toLowerCase() + "&artistName=" + text.replaceAll(" ", "%20").toLowerCase();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url POST: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //Log.e(TAG, "Response from url: " + jsonStr);
                    Log.e(TAG, "POST Successful");

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ERROR POST" + e);

                }
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getMusicAsync gma = new getMusicAsync();
            gma.execute();
        }
    }


    public void findMusic(String packageName, String title, String text) {
        Log.e(TAG, "SEARCHING FOR MUSIC");

        for (String s : playerList) {
            //Log.e(TAG, "player"+s);
            //Log.e(TAG, "package"+packageName);

            if (packageName.toLowerCase().contains(s.toLowerCase())) {
                Log.e(TAG, "FOUND PLAYER");

                sendMusicAsync sma = new sendMusicAsync(title, text);
                sma.execute();
            }
        }
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.d("test", "recieve");
                Log.e(TAG, "RECEIVE");
                String str = String.format("%02d:%02d", pref.getInt("hour",1), pref.getInt("minute",1));
                //Date time1 = new SimpleDateFormat("HH:mm").parse(str);
                //String current = new SimpleDateFormat("HH:mm").format(new Date());
                Calendar c = Calendar.getInstance();
                int currentHour = c.get(Calendar.HOUR_OF_DAY);
                int currentMinute = c.get(Calendar.MINUTE);
                //Log.e(TAG, String.valueOf(currentHour));
                //Log.e(TAG, String.valueOf(pref.getInt("hour",1)));

                if(currentHour ==pref.getInt("hour",1) && currentMinute ==pref.getInt("minute",1) ){
                //if(String.valueOf(currentHour).compareTo(pref.getString("hour","one"))==0){

                getMusicAsync gma = new getMusicAsync();
                    gma.execute();
                }

                if (intent.getStringExtra("command").equals("clearall")) {
                    NLService.this.cancelAllNotifications();
                } else if (intent.getStringExtra("command").equals("list")) {
                    Intent i1 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                    i1.putExtra("notification_event", "=====================");
                    sendBroadcast(i1);
                    int i = 1;
                    for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                        Intent i2 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                        try {
                            i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "n Notification Details: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "n ");
                        }
                        findMusic(sbn.getPackageName(), sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString(), sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                        sendBroadcast(i2);
                        i++;
                    }
                    Intent i3 = new Intent("uoit.ca.ccproject.NOTIFICATION_LISTENER_EXAMPLE");
                    i3.putExtra("notification_event", "===== Notification List ====");
                    sendBroadcast(i3);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR " + e);

            }

        }
    }
   private void runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }
}
