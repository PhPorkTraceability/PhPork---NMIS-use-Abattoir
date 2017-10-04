package app;

/**
 * Created by marmagno on 5/15/2017.
 */

public class AppConfig {
    // Server user login url

    //public static String IP;

    //public static String IP = "10.0.4.225";
//    public static String IP = "192.168.0.173";
//    public static String IP = "192.168.137.1";
    public static String IP = "192.168.1.4";


    public static String URL_GETALLDATA =
            "http://" + IP + "/phpork/android_connect/getSlaughterData.php";

    public static String URL_SENDUPDATEDDATA =
            "http://" + IP + "/phpork/android_connect/updateTablesData.php";
}
