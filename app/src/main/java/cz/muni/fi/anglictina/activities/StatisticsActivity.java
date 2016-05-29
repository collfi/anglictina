package cz.muni.fi.anglictina.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import cz.muni.fi.anglictina.App;
import cz.muni.fi.anglictina.R;

/**
 * Created by collfi on 30. 4. 2016.
 */
public class StatisticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        setContentView(R.layout.activity_statistics);


        final SharedPreferences mapPref = getSharedPreferences("showMap", Context.MODE_PRIVATE);
        if (mapPref.getBoolean("show", true)) {
            View v = LayoutInflater.from(this).inflate(R.layout.dialog_knowledge_map, null);
            final CheckBox checkBox = (CheckBox)  v.findViewById(R.id.checkbox);
            setTitle("Mapa znalostí");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Mapa znalostí")
                    .setMessage("Slovní zásoba je zde uspořádaná podle aktuální obtížnosti a rozdělena na 20 částí." +
                            " Jsou zde uvedeny příklady z každé dvacetiny a vpravo je přibližná předpokladaná " +
                            "šance na správnou odpověď na slova z dané části. Po kliknutí se zobrazí detail" +
                            " dané sekce.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checkBox.isChecked()) {
                                mapPref.edit().putBoolean("show", false).apply();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setView(v)
                    .create()
                    .show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }
}
