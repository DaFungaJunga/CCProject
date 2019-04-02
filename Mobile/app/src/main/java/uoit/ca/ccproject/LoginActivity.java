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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public EditText usernameEditText;
    public EditText passwordEditText;
    public String passwordText;
    public String userNameText;
    public boolean signIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();



        usernameEditText = (EditText) findViewById(R.id.user);
        passwordEditText = (EditText) findViewById(R.id.password);
    }


    public void toRegister(View v){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }


    public void Login(View v){
        passwordText = passwordEditText.getText().toString();
        userNameText = usernameEditText.getText().toString();

        getUsers e = new getUsers();
        e.execute();
    }





    public class getUsers extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = null;
            try{
                String TAG = LoginActivity.class.getSimpleName();
                String callURL = "http://99.79.42.247/cloud/User/" + userNameText;
                URL url = new URL(callURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json-patch+json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                //jsonParam.put("userID", 0);
                jsonParam.put("userName", userNameText);
                jsonParam.put("password", passwordText);

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
                    if(userNameText.equals(name)){
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
                        String messg = "Login Successful";
                        Toast.makeText(getApplicationContext(),
                                messg,
                                Toast.LENGTH_LONG).show();
                    }
                });

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messg = "Login Unsuccessful";
                        Toast.makeText(getApplicationContext(),
                                messg,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }
}
