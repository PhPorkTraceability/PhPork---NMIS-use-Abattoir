package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by marmagno on 5/22/2017.
 */

public class IntroSliderSession {


    // Shared preferences file name
    private static final String PREF_NAME = "IntroLogin";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    // LogCat tag
    private static String TAG = IntroSliderSession.class.getSimpleName();
    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    public IntroSliderSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin() {

        editor.putBoolean(KEY_IS_LOGGEDIN, true);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     **/
    public void checkLogin() {
        if (isLoggedIn()) {
        }
    }

    /**
     * Get stored session data
     **/

    public HashMap<String, String> getUserSession() {
        HashMap<String, String> user = new HashMap<>();

        // return user
        return user;
    }

    /**
     * Clear session details
     **/
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}
