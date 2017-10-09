package slaughter.phporktraceabilty.farmslaughter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.NetworkUtil;
import helper.SQLiteHandler;
import helper.SessionManager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    SessionManager session;
    NavigationView navigationView = null;
    Toolbar toolbar = null;
    TextView mTextView;
    DrawerLayout drawer;
    SQLiteHandler db;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SQLiteHandler.initializeDB(this);
        HashMap<String, String> user;

        //creating a new folder for the database to backup to
        File direct = new File(Environment.getExternalStorageDirectory() + "/FarmSlaughterData");

        if (!direct.exists()) {
            direct.getParentFile().mkdir();
        }

        session = new SessionManager(getApplicationContext());
        user = session.getUserSession();
        String username = user.get(SessionManager.KEY_USERNAME);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        //display username to nav drawer
        mTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.uname);
        mTextView.setText(username);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Close App")
                    .setMessage("Do you want to close the app?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            dialogInterface.dismiss();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            alertBuilder.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == R.id.action_logout) {
            session.logoutUser();
            finish();
            return true;
        } else if (id == R.id.action_help) {
            Intent i = new Intent(this, Help.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (id == R.id.nav_home) {
            HomeFragment fragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_accepted) {
            AcceptedFragment fragment = new AcceptedFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_rejected) {
            RejectedFragment fragment = new RejectedFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_onHold) {
            OnHoldFragment fragment = new OnHoldFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_syncData) {
            int status = NetworkUtil.getConnectivityStatus(getApplicationContext());
            if (status == 0) {
                displayAlert("Cannot establish connection to server.");
            } else {
                syncData();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void syncData() {
        final String tag_string_req = "post_alldata";

        db = SQLiteHandler.getInstance(this);

        JSONArray pig_array = new JSONArray();
        JSONArray slaughter_data = new JSONArray();
        Map<String, JSONArray> params = new HashMap<>();
        try {
            ArrayList<HashMap<String, String>> pig = db.getAllPigs();

            for(int i = 0;i < pig.size();i++) {
                HashMap<String, String> d = pig.get(i);
                JSONObject j = new JSONObject();
                j.put("pig_id", d.get("pig_id"));
                j.put("pig_status", d.get("pig_status"));
                j.put("user", d.get("user"));
                pig_array.put(j);
            }

            ArrayList<HashMap<String, String>> slaughter_pig = db.getSlaughterStatPig();

            for(int i = 0;i < slaughter_pig.size();i++) {
                HashMap<String, String> d = slaughter_pig.get(i);
                JSONObject j = new JSONObject();
                j.put("slaughter_stat", d.get("slaughter_stat"));
                j.put("slaughter_timestamp", d.get("slaughter_timestamp"));
                j.put("pig_id", d.get("pig_id"));
                slaughter_data.put(j);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put("pig", pig_array);
        params.put("slaughter_pig", slaughter_data);

        JSONObject jsonObj = new JSONObject(params);

        String url = AppConfig.URL_SENDUPDATEDDATA;

        // Request a string response from the provided URL.
        final JsonObjectRequest _request = new JsonObjectRequest(Request.Method.POST, url, jsonObj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject resp) {
                        Log.d(TAG, "Volley error: " + resp.toString());
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error: " + error.getMessage());
                        displayAlert("Connection failed.");
                    }
                }
        );

        _request.setRetryPolicy(new DefaultRetryPolicy(
                2000, //timeout in ms
                0, // no of max retries
                1)); // backoff multiplier

        // Adding request to request queue
        AppController.getInstance(getApplicationContext())
                .addToRequestQueue(_request, tag_string_req);

    }

    public void displayAlert(String message) {

        new AlertDialog.Builder(this)
                .setTitle(message)
                .setCancelable(false)
                .setMessage("Try again?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        syncData();
                        exportDB();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    //exporting database
    public void exportDB() {

        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                String backupDBPath = "/FarmSlaughterData";
                File currentDB = this.getDatabasePath(SQLiteHandler.DB_NAME);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Log.d("Export", "export successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
