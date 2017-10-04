package slaughter.phporktraceabilty.farmslaughter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import app.AppConfig;
import app.AppController;
import helper.NetworkUtil;
import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Created by marmagno on 5/16/2017.
 */

public class GetDataFromServer extends Activity {

    private static final String TAG = GetDataFromServer.class.getSimpleName();

    // Tables
    private static final String TABLE_PIG = "pig";
    private static final String TABLE_MOVEMENT = "movement";
    private static final String TABLE_RFID_TAGS = "rfid_tags";
    private static final String TABLE_USER = "user";

    // Table Pig
    private static final String KEY_PIGID = "pig_id";
    private static final String KEY_BOARID = "boar_id";
    private static final String KEY_SOWID = "sow_id";
    private static final String KEY_FOSTER = "foster_sow";
    private static final String KEY_WEEKF = "week_farrowed";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_FDATE = "farrowing_date";
    private static final String KEY_PIGSTAT = "pig_status";
    private static final String KEY_PENID = "pen_id";
    private static final String KEY_BREEDID = "breed_id";
    private static final String KEY_GNAME = "pig_batch";
    private static final String KEY_USER = "user";

    // Table movement
    private static final String KEY_DATE = "date_moved";
    private static final String KEY_TIME = "time_moved";
    private static final String KEY_SERVERDATE = "server_date";
    private static final String KEY_SERVERTIME = "server_time";

    // Table RFID Tags
    private static final String KEY_TAGID = "tag_id";
    private static final String KEY_TAGRFID = "tag_rfid";
    private static final String KEY_LABEL = "label";
    private static final String KEY_TAGSTAT = "status";

    // Table User
    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERTYPE = "user_type";

    private static final String KEY_SYNCSTAT = "sync_status";

    JSONArray pigs = null;
    JSONArray movement = null;
    JSONArray users = null;
    JSONArray tags = null;
    SQLiteHandler db;
    SessionManager session;
    //A ProgressDialog View
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        db = SQLiteHandler.getInstance(this);
//        db.deleteTables();

        session = new SessionManager(getApplicationContext());

        //Create a new progress dialog.
        progressDialog = new ProgressDialog(GetDataFromServer.this);
        //Set the progress dialog to display a horizontal bar .
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Set the dialog title to 'Loading...'.
        progressDialog.setTitle("Setting Things Up.");
        //Set the dialog message to 'Loading application View, please wait...'.
        progressDialog.setMessage("Loading, please wait...");
        //This dialog can't be canceled by pressing the back key.
        progressDialog.setCancelable(false);
        //This dialog is indeterminate.
        progressDialog.setIndeterminate(true);
        //Display the progress dialog.
        progressDialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();

        int status = NetworkUtil.getConnectivityStatus(getApplicationContext());
        if (status == 0) {
            displayAlert("Cannot establish connection to server.");
        } else {
            getAllDataByNet();
        }
    }

    // Called when there is connection to the server via WiFi
    public void getAllDataByNet() {
        final String tag_string_req = "req_alldata";

        // Request a string response from the provided URL.
        final JsonObjectRequest _request = new JsonObjectRequest(AppConfig.URL_GETALLDATA, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject resp) {

                        Log.d(TAG, "Getting tables : " + resp.toString());

                        try {
                            //JSONObject resp = new JSONObject(response);
                            boolean error = resp.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                dataOperations(resp);
                                nextPage();
                            } else {
                                String errorMsg = resp.getString("error_msg");
                                Log.e(TAG, "Error Response: " + errorMsg);
                            }
                        } catch (JSONException e) {
                            // JSON error
                            try {
                                e.printStackTrace();
                                Log.e(TAG, "JSON Error: " + resp.toString(3));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley error: " + error.getMessage());
                displayAlert("Connection failed.");
            }
        });

        _request.setRetryPolicy(new DefaultRetryPolicy(
                2000, //timeout in ms
                0, // no of max retries
                1)); // backoff multiplier

        // Adding request to request queue
        AppController.getInstance(getApplicationContext())
                .addToRequestQueue(_request, tag_string_req);

    }

    public void dataOperations(JSONObject resp) throws JSONException {
        String sync_status = "old";
        String sql;
        SQLiteStatement stmt;
        SQLiteDatabase db2;

        sql = "INSERT OR REPLACE INTO " + TABLE_USER + " VALUES(?,?,?,?)";

        db2 = db.getWritableDatabase();
        db2.beginTransactionNonExclusive();

        users = new JSONArray();
        users = resp.getJSONArray("user");

        stmt = db2.compileStatement(sql);

        for (int i = 0; i < users.length(); i++) {
            JSONObject c = users.getJSONObject(i);

            // Now store the user in SQLite
            String username = c.getString(KEY_USERNAME);
            String password = c.getString(KEY_PASSWORD);
            String account = c.getString(KEY_USERTYPE);

            stmt.bindString(1, String.valueOf(i + 1));
            stmt.bindString(2, username);
            stmt.bindString(3, password);
            stmt.bindString(4, account);

            stmt.execute();
            stmt.clearBindings();
        }

        db2.setTransactionSuccessful();
        db2.endTransaction();

        db2.close();

        sql = "INSERT OR REPLACE INTO " + TABLE_PIG + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        db2 = db.getWritableDatabase();
        db2.beginTransactionNonExclusive();

        pigs = new JSONArray();
        pigs = resp.getJSONArray("pig");

        stmt = db2.compileStatement(sql);

        for (int i = 0; i < pigs.length(); i++) {
            JSONObject c = pigs.getJSONObject(i);

            // Now store the user in SQLite
            String pig_id = c.getString(KEY_PIGID);
            String boar_id = c.getString(KEY_BOARID);
            String sow_id = c.getString(KEY_SOWID);
            String foster_sow = c.getString(KEY_FOSTER);
            String week_farrowed = c.getString(KEY_WEEKF);
            String gender = c.getString(KEY_GENDER);
            String farrowing_date = c.getString(KEY_FDATE);
            String pig_status = c.getString(KEY_PIGSTAT);
            String pen_id = c.getString(KEY_PENID);
            String breed_id = c.getString(KEY_BREEDID);
            String user = c.getString(KEY_USER);
            String group_name = c.getString(KEY_GNAME);

            stmt.bindString(1, pig_id);
            stmt.bindString(2, boar_id);
            stmt.bindString(3, sow_id);
            stmt.bindString(4, foster_sow);
            stmt.bindString(5, week_farrowed);
            stmt.bindString(6, gender);
            stmt.bindString(7, farrowing_date);
            stmt.bindString(8, pig_status);
            stmt.bindString(9, pen_id);
            stmt.bindString(10, breed_id);
            stmt.bindString(11, user);
            stmt.bindString(12, group_name);
            stmt.bindString(13, sync_status);

            stmt.execute();
            stmt.clearBindings();
        }

        db2.setTransactionSuccessful();
        db2.endTransaction();

        db2.close();

//        sql = "INSERT INTO " + TABLE_MOVEMENT + " VALUES(?,?,?,?,?,?,?,?) ";
//        db2 = db.getWritableDatabase();
//        db2.beginTransactionNonExclusive();

        movement = new JSONArray();
        movement = resp.getJSONArray("movement");

//        stmt = db2.compileStatement(sql);

        for (int i = 0; i < movement.length(); i++) {
            JSONObject c = movement.getJSONObject(i);

            // Now store the user in SQLite
            String movement_id = c.getString("movement_id");
            String date_moved = c.getString(KEY_DATE);
            String time_moved = c.getString(KEY_TIME);
            String pen_id = c.getString(KEY_PENID);
            String server_date = c.getString(KEY_SERVERDATE);
            String server_time = c.getString(KEY_SERVERTIME);
            String pig_id = c.getString(KEY_PIGID);

            db.addMovement(movement_id, date_moved, time_moved, pen_id,
                    server_date, server_time, pig_id, "new");
            /*
            stmt.bindString(1, movement_id);
            stmt.bindString(2, date_moved);
            stmt.bindString(3, time_moved);
            stmt.bindString(4, pen_id);
            stmt.bindString(5, server_date);
            stmt.bindString(6, server_time);
            stmt.bindString(7, "new");
            stmt.bindString(8, pig_id);

            stmt.execute();
            stmt.clearBindings();
            */
        }
/*
        db2.setTransactionSuccessful();
        db2.endTransaction();

        db2.close();
*/

        sql = "INSERT OR REPLACE INTO " + TABLE_RFID_TAGS + " VALUES(?,?,?,?,?) ";
        db2 = db.getWritableDatabase();
        db2.beginTransactionNonExclusive();

        tags = new JSONArray();
        tags = resp.getJSONArray("rfid_tags");

        stmt = db2.compileStatement(sql);

        for (int i = 0; i < tags.length(); i++) {
            JSONObject c = tags.getJSONObject(i);

            // Now store the user in SQLite
            String tag_id = c.getString(KEY_TAGID);
            String tag_rfid = c.getString(KEY_TAGRFID);
            String pig_id = c.getString(KEY_PIGID);
            String label = c.getString(KEY_LABEL);
            String status = c.getString(KEY_TAGSTAT);

            stmt.bindString(1, tag_id);
            stmt.bindString(2, tag_rfid);
            stmt.bindString(3, pig_id);
            stmt.bindString(4, label);
            stmt.bindString(5, status);

            stmt.execute();
            stmt.clearBindings();
        }

        db2.setTransactionSuccessful();
        db2.endTransaction();

        db2.close();

    }

    public void nextPage() {
        Intent i = new Intent();
        if (session.isLoggedIn()) {
            i.setClass(this, HomeActivity.class);
        } else {
//            i.setClass(LoadSyncAll.this, IntroSliderActivity.class);
            i.setClass(this, LoginActivity.class);
        }
        startActivity(i);
        finish();
    }

    public void displayAlert(String message) {

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(message)
                .setCancelable(false)
                .setMessage("Try again?")
                /*.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Do intense testing on this part
                        importTables();
                    }
                })*/
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        importTables();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nextPage();
                    }
                }).show();
    }

    public void importTables() {
        String line1;
        String line2;
        int errors = 0;

        try {
            final String dbFileName = "phpork_in.sql";

            File dbFile = new File(Environment.getExternalStorageDirectory(), dbFileName);
            BufferedReader reader = new BufferedReader(new FileReader(dbFile));
            SQLiteDatabase load_db;

//            db.deleteTables();

            load_db = db.getWritableDatabase();

            load_db.execSQL("Delete from pig");
            load_db.execSQL("Delete from movement");
            load_db.execSQL("Delete from user");
            load_db.execSQL("Delete from rfid_tags");
            load_db.execSQL("Delete from slaughter_pig");

            while ((line1 = reader.readLine()) != null) {
                if (line1.startsWith("INSERT")) {
                    while (true) {
                        boolean stop;

                        line2 = reader.readLine();
                        while (line2.startsWith("--")) {
                            line2 = reader.readLine();
                        }

                        stop = line2.endsWith(";");
                        line2 = line1 + line2.substring(0, line2.length() - 1) + ";";

                        try {
                            load_db.execSQL(line2);
                        } catch (Exception ex) {
                            Log.d(TAG, "Importing Offline: " + ex.getMessage());
                            errors++;
                        }

                        if (stop) {
                            break;
                        }
                    }
                }
            }
            load_db.close();

            if (errors > 0) {
                Log.d(TAG, errors + " statements skipped.");
                Toast.makeText(this, "Imported with warnings, check log for details", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Imported with no errors", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Filename to import not found.");
            Toast.makeText(this, "Filename to import not found.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in importing tables, " + e.getMessage(), e);
            Toast.makeText(this, "Something went wrong on importing database", Toast.LENGTH_SHORT).show();
        }

        nextPage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Close all dialogs
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

}
