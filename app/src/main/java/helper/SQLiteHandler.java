package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marmagno on 5/15/2017.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    // Database name
    public static final String DB_NAME = "phpork";
    // Database Tables
    private static final String TABLE_PIG = "pig";
    private static final String TABLE_MOVEMENT = "movement";
    private static final String TABLE_SLAUGHTERPIG = "slaughter_pig";
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
    private static final String KEY_MOVEID = "movement_id";
    private static final String KEY_DATE = "date_moved";
    private static final String KEY_TIME = "time_moved";
    private static final String KEY_SERVERDATE = "server_date";
    private static final String KEY_SERVERTIME = "server_time";
    // Table slaughter pig
    private static final String KEY_SLID = "slaughter_id";
    private static final String KEY_SLSTAT = "slaughter_stat";
    private static final String KEY_TIMESTAMP = "slaughter_timestamp";
    // Table RFID Tags
    private static final String KEY_TAGID = "tag_id";
    private static final String KEY_TAGRFID = "tag_rfid";
    private static final String KEY_LABEL = "label";
    private static final String KEY_TAGSTAT = "status";
    // Table User
    private static final String KEY_USERID = "user_id";
    private static final String KEY_USERNAME = "user_name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERTYPE = "user_type";
    private static final String KEY_SYNCSTAT = "sync_status";
    private static SQLiteHandler mInstance;


    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private SQLiteHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized SQLiteHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new SQLiteHandler(context.getApplicationContext());
        }
        return mInstance;
    }

    public static void initializeDB(Context c) {
        mInstance = new SQLiteHandler(c);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USER = "CREATE TABLE " + TABLE_USER + "("
                + KEY_USERID + " INTEGER PRIMARY KEY,"
                + KEY_USERNAME + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT ,"
                + KEY_USERTYPE + " TEXT" + ")";
        db.execSQL(CREATE_USER);

        String CREATE_PIG = "CREATE TABLE " + TABLE_PIG + "("
                + KEY_PIGID + " INTEGER PRIMARY KEY,"
                + KEY_BOARID + " TEXT,"
                + KEY_SOWID + " TEXT,"
                + KEY_FOSTER + " TEXT,"
                + KEY_WEEKF + " TEXT,"
                + KEY_GENDER + " TEXT,"
                + KEY_FDATE + " DATE,"
                + KEY_PIGSTAT + " TEXT,"
                + KEY_PENID + " INTEGER,"
                + KEY_BREEDID + " INTEGER,"
                + KEY_USER + " TEXT,"
                + KEY_GNAME + " TEXT,"
                + KEY_SYNCSTAT + " TEXT" + ")";
        db.execSQL(CREATE_PIG);

        String CREATE_MOVEMENT = "CREATE TABLE " + TABLE_MOVEMENT + "("
                + KEY_MOVEID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " DATE,"
                + KEY_TIME + " TIME,"
                + KEY_PENID + " INTEGER,"
                + KEY_SERVERDATE + " DATE,"
                + KEY_SERVERTIME + " TIME,"
                + KEY_PIGID + " INTEGER,"
                + KEY_SYNCSTAT + " TEXT,"
                + "FOREIGN KEY(" + KEY_PIGID + ") REFERENCES "
                + TABLE_PIG + "(" + KEY_PIGID + ")"
                + ")";
        db.execSQL(CREATE_MOVEMENT);

        String CREATE_RFIDTAGS = "CREATE TABLE " + TABLE_RFID_TAGS + "("
                + KEY_TAGID + " INTEGER PRIMARY KEY,"
                + KEY_TAGRFID + " TEXT,"
                + KEY_PIGID + " INTEGER,"
                + KEY_LABEL + " TEXT,"
                + KEY_TAGSTAT + " TEXT,"
                + " FOREIGN KEY(" + KEY_PIGID + ") REFERENCES "
                + TABLE_PIG + "(" + KEY_PIGID + ")" + ")";
        db.execSQL(CREATE_RFIDTAGS);

        String CREATE_SLAUGHTERPIG = "CREATE TABLE " + TABLE_SLAUGHTERPIG + "("
                + KEY_SLID + " INTEGER PRIMARY KEY,"
                + KEY_SLSTAT + " TEXT,"
                + KEY_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_PIGID + " INTEGER,"
                + KEY_SYNCSTAT + " TEXT,"
                + " FOREIGN KEY(" + KEY_PIGID + ") REFERENCES "
                + TABLE_PIG + "(" + KEY_PIGID + ")" + ")";
        db.execSQL(CREATE_SLAUGHTERPIG);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop Tables here and recreate again or
        // Use Adam Incremental Method
        onCreate(db);
    }

    public void addMovement(String movement_id, String date_moved, String time_moved,
                            String pen_id, String server_date, String server_time,
                            String pig_id, String sync_stat) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MOVEID, movement_id);
        values.put(KEY_DATE, date_moved);
        values.put(KEY_TIME, time_moved);
        values.put(KEY_PENID, pen_id);
        values.put(KEY_SERVERDATE, server_date);
        values.put(KEY_SERVERTIME, server_time);
        values.put(KEY_PIGID, pig_id);
        values.put(KEY_SYNCSTAT, sync_stat);

        // Inserting Row
        db.insert(TABLE_MOVEMENT, null, values);
        db.close(); // Closing database connection
    }

    public void addSlaughterPigStat(String stat, String pig_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SLSTAT, stat);
        values.put(KEY_PIGID, pig_id);
        values.put(KEY_SYNCSTAT, "new");

        // Inserting Row
        db.insert(TABLE_SLAUGHTERPIG, null, values);
        db.close(); // Closing database connection
    }

    public void removeSlaughterPigStat(String stat, String pig_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Inserting Row
        db.delete(TABLE_SLAUGHTERPIG, KEY_PIGID +"=? AND "+ KEY_SLSTAT +"=?", new String[] {pig_id, stat});
        db.close(); // Closing database connection
    }

    public void updateOnSwipe(String movement_id, String stat, String pig_id) {
//        Log.d("updateOnSwipe", "movementID: "+movement_id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_MOVEMENT +
                " SET " + KEY_SYNCSTAT + "='"+ stat +
                "' WHERE movement_id='" + movement_id + "' AND pig_id = '" + pig_id + "'");
        db.close(); // Closing database connection
    }

    public void updatePigStat(String stat, String pig_id) {
//        Log.d("updateOnSwipe", "pig ID: "+pig_id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PIG +
                " SET " + KEY_PIGSTAT + "='" + stat + "', " + KEY_SYNCSTAT + "='new'" +
                " WHERE pig_id='" + pig_id + "'");
        db.close(); // Closing database connection
    }

    public ArrayList<HashMap<String, String>> getAllPigs() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT pig_id, pig_status, user FROM " + TABLE_PIG + " WHERE " + KEY_SYNCSTAT + "='new'";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_PIGID, cursor.getString(0));
            result.put(KEY_PIGSTAT, cursor.getString(1));;
            result.put(KEY_USER, cursor.getString(2));

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        return list;
    }

    public ArrayList<HashMap<String, String>> getSlaughterStatPig() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SLAUGHTERPIG + " WHERE " + KEY_SYNCSTAT + "='new'";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_SLSTAT, cursor.getString(1));
            result.put(KEY_TIMESTAMP, cursor.getString(2));
            result.put(KEY_PIGID, cursor.getString(3));

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        return list;
    }

    public ArrayList<HashMap<String, String>> getAllRFIDtags() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_RFID_TAGS +";";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_TAGID, cursor.getString(0));
            result.put(KEY_TAGRFID, cursor.getString(1));
            result.put(KEY_PIGID, cursor.getString(2));
            result.put(KEY_LABEL, cursor.getString(3));
            result.put(KEY_TAGSTAT, cursor.getString(4));

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        return list;
    }

    public ArrayList<HashMap<String, String>> getAllUser() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_RFID_TAGS +";";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_USERNAME, cursor.getString(0));
            result.put(KEY_PASSWORD, cursor.getString(1));
            result.put(KEY_USERTYPE, cursor.getString(2));

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        return list;
    }

    public ArrayList<HashMap<String, String>> getPigsHome() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String[] gender = new String[]{
                "Galore",
                "Landrace",
                "Largewhite",
                "Galore-Largewhite",
                "Galore-Landrace",
                "Largewhite-Landrace"};
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT c."
                + KEY_PIGID + ", b."
                + KEY_LABEL + ", c."
                + KEY_GENDER + ", c."
                + KEY_BREEDID + ", a.movement_id FROM "
                + TABLE_MOVEMENT + " a JOIN "
                + TABLE_RFID_TAGS + " b USING(pig_id) JOIN "
                + TABLE_PIG + " c USING(pig_id) WHERE a."
                + KEY_SYNCSTAT + "='new' GROUP BY c." + KEY_PIGID
                + " ORDER BY a." + KEY_SERVERDATE + " ASC, a." + KEY_SERVERTIME + " ASC;";

        Cursor cursor = db.rawQuery(query, null);

        // Move to first row
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_PIGID, cursor.getString(0));
            result.put(KEY_LABEL, cursor.getString(1));
            result.put(KEY_GENDER, (cursor.getString(2).equalsIgnoreCase("F") ? "Female" : "Male"));
            result.put("breed", gender[Integer.parseInt(cursor.getString(3))]);
            result.put(KEY_MOVEID, cursor.getString(4));

            System.out.println(result);
            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        Log.d("query", list.toString());

        if (list.size() == 0) {
            System.out.println("No data on " + TABLE_MOVEMENT);
        }

        return list;
    }

    public ArrayList<HashMap<String, String>> getPigs(String status) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String[] gender = new String[]{
                "Galore",
                "Landrace",
                "Largewhite",
                "Galore-Largewhite",
                "Galore-Landrace",
                "Largewhite-Landrace"};
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT a."
                + KEY_PIGID + ", b."
                + KEY_LABEL + ", a."
                + KEY_GENDER + ", a."
                + KEY_BREEDID + " FROM "
                + TABLE_RFID_TAGS + " b JOIN "
                + TABLE_PIG + " a USING(pig_id) WHERE a."
                + KEY_PIGSTAT + " = '" + status + "';";

        Cursor cursor = db.rawQuery(query, null);

        // Move to first row
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_PIGID, cursor.getString(0));
            result.put(KEY_LABEL, cursor.getString(1));
            result.put(KEY_GENDER, (cursor.getString(2).equalsIgnoreCase("F") ? "Female" : "Male"));
            result.put("breed", gender[Integer.parseInt(cursor.getString(3))]);

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        if (list.size() == 0) {
            System.out.println("No data on " + TABLE_PIG);
        }
        Log.d("GET PIG", list.toString());
        return list;
    }

    public HashMap<String, String> getPigDetails(String pig_label) {

        String[] gender = new String[]{
                "Galore",
                "Landrace",
                "Largewhite",
                "Galore-Largewhite",
                "Galore-Landrace",
                "Largewhite-Landrace"};

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT a."
                + KEY_WEEKF + ", a."
                + KEY_GENDER + ", a."
                + KEY_FDATE + ", a."
                + KEY_BREEDID + ", b."
                + KEY_TAGRFID +", b."
                + KEY_LABEL + " FROM "
                + TABLE_PIG + " a JOIN "
                + TABLE_RFID_TAGS + " b USING("
                + KEY_PIGID +") WHERE "
                + KEY_LABEL+"='"
                + pig_label +"';";

        Cursor cursor = db.rawQuery(query, null);

        // Move to first row
        cursor.moveToFirst();

        HashMap<String, String> result = new HashMap<>();

        result.put(KEY_WEEKF, cursor.getString(0));
        result.put(KEY_GENDER, (cursor.getString(1).equalsIgnoreCase("F") ? "Female" : "Male"));
        result.put(KEY_FDATE, cursor.getString(2));
        result.put("breed", gender[Integer.parseInt(cursor.getString(3))]);
        result.put(KEY_TAGRFID, cursor.getString(4));
        result.put(KEY_LABEL, cursor.getString(5));

        cursor.close();
        db.close();

        return result;
    }

    public ArrayList<HashMap<String, String>> getUsers() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT user_id, user_name, password FROM " + TABLE_USER;

        Cursor cursor = db.rawQuery(query, null);

        // Move to first row
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String, String> result = new HashMap<>();

            result.put(KEY_USERID, cursor.getString(0));
            result.put(KEY_USERNAME, cursor.getString(1));
            result.put(KEY_PASSWORD, cursor.getString(2));

            cursor.moveToNext();
            list.add(result);
        }

        cursor.close();
        db.close();

        return list;
    }

    public String getPassword(String userId, String username) {
        String result = "";
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT password FROM " + TABLE_USER + " WHERE user_id = '" + userId
                + "' AND user_name = '" + username + "'";

        Cursor cursor = db.rawQuery(query, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            String res = cursor.getString(0);
            if (res != null) {
                result = res;
            }
        }

        cursor.close();
        db.close();

        return result;
    }

}
