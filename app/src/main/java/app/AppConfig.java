package app;

/**
 * Created by marmagno on 5/15/2017.
 */

public class AppConfig {
    // Server user login url

    public static String IP = "10.0.5.65";

    public static String URL_GETALLDATA =
            "http://" + IP + "/phpork/android_connect/getSlaughterData.php";

    public static String URL_SENDUPDATEDDATA =
            "http://" + IP + "/phpork/android_connect/slaughterDataReceiver.php";
}
