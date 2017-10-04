package app;

import android.app.Application;

import slaughter.phporktraceabilty.farmslaughter.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by marmagno on 9/6/2017.
 */

public class AppFont extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // font from assets: "assets/fonts/Roboto-Regular.ttf
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/BebasNeue.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
