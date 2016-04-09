package cz.muni.fi.anglictina;

import android.app.Application;

/**
 * Created by collfi on 11. 3. 2016.
 */
public class App extends Application {
    public static boolean isAppVisible() {
        return appVisible;
    }

    public static void activityResumed() {
        appVisible = true;
    }

    public static void activityPaused() {
        appVisible = false;
    }

    private static boolean appVisible;
}
