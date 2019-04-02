package uoit.ca.ccproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private String TAG = "RegisterActivity";
    public EditText usernameEditText;
    public EditText passwordEditText;
    public String passwordText;
    public String userNameText;
    public boolean signIn = false;

    public EditText username1;
    public EditText username2;
    public EditText password1;
    public EditText password2;
    public String u1;
    public String p1;


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        username1 = findViewById(R.id.user1);
        username2 = findViewById(R.id.user2);
        password1 = findViewById(R.id.password1);
        password2 = findViewById(R.id.password2);
    }


    public void goBack(View v) {
        this.finish();
    }




    public void Register(View v){
        u1 = username1.getText().toString();
        String u2 = username2.getText().toString();
        p1 = password1.getText().toString();
        String p2 = password2.getText().toString();
        registerUser RU = new registerUser();



        if(u1.equals(u2) && u1.length() > 0){
            if(p1.equals(p2) && p1.length() > 0){
                RU.execute();
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messg = "Password Mismatch";
                        Toast.makeText(getApplicationContext(),
                                messg,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String messg = "Username Mismatch";
                    Toast.makeText(getApplicationContext(),
                            messg,
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public class registerUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = null;
            try{
                String TAG = LoginActivity.class.getSimpleName();
                String callURL = "http://99.79.42.247/cloud/User/";
                URL url = new URL(callURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json-patch+json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);




                JSONObject jsonParam = new JSONObject();

                jsonParam.put("userName", u1);
                jsonParam.put("password", p1);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    response = "";// String variable declared global
                    response += line;
                    //Log.i("response_line", response);
                }

                Log.e(TAG, "Response from url GET: " + response);

                if (response != null) {
                    JSONObject obj = new JSONObject(response);
                    String name = obj.getString("userName");
                    String ID = obj.getString("userID");
                    if(u1.equals(name)){
                        signIn = true;
                        editor.putString("userName", name);
                        editor.putString("userID", ID);
                    }else{
                        signIn = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(signIn){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messg = "Registration Successful";
                        Toast.makeText(getApplicationContext(),
                                messg,
                                Toast.LENGTH_LONG).show();
                    }
                });

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);

            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messg = "Registration Unsuccessful";
                        Toast.makeText(getApplicationContext(),
                                messg,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

}
