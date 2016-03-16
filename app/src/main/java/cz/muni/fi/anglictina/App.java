package cz.muni.fi.anglictina;

import android.app.Application;

/**
 * Created by collfi on 11. 3. 2016.
 */
public class App extends Application {
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
