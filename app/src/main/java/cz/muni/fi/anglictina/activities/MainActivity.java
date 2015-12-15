package cz.muni.fi.anglictina.activities;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract.WordEntry;
import cz.muni.fi.anglictina.db.WordDbHelper;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static int sCorrect = 0;
    public static int sIncorrect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!getDatabasePath("words.db").exists()) {
            Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
            try {
                WordDbHelper helper = new WordDbHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("words.txt")));
                String line = reader.readLine();
                while (line != null) {
                    ContentValues values = new ContentValues();
                    String[] s = line.split("\t");
                    values.put(WordEntry.COLUMN_NAME_WORD, s[0]);
                    values.put(WordEntry.COLUMN_NAME_TRANSLATIONS, s[1]);
                    values.put(WordEntry.COLUMN_NAME_FREQUENCY, s[2]);
                    values.put(WordEntry.COLUMN_NAME_PERCENTIL, s[3]);
                    values.put(WordEntry.COLUMN_NAME_PRONUNCIATION, s[4]);
                    values.put(WordEntry.COLUMN_NAME_DIFFICULTY, ((Float.valueOf(s[3]) * 4f / 100f) - 2f));
                    values.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, 0);
                    values.put(WordEntry.COLUMN_NAME_LEARNED, 0);
                    db.insert(WordEntry.TABLE_NAME, null, values);
                    line = reader.readLine();
                }
                db.close();
                reader.close();
            } catch (IOException ioe) {
                Log.e("Main Activity", "error loading db");
            }
            Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "exists", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences sp = getSharedPreferences("stats", Context.MODE_PRIVATE);
        sCorrect = sp.getInt("correct", 0);
        sIncorrect = sp.getInt("incorrect", 0);

        //////test capacity of Shared Preferences
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("words.txt")));
//            String line = reader.readLine();
//            SharedPreferences s = getSharedPreferences("test", MODE_PRIVATE);
//            int q = 0;
//            while (line != null) {
//                String[] a = line.split("\t");
//                s.edit().putString(a[0], a[0]).commit();
//                line = reader.readLine();
//                Log.i("QQQ", "" + ++q);
//            }
//        } catch (Exception e) {
//            Log.e("QQQ", "KOKOTI " + e.getLocalizedMessage());
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_learn) {
            startActivity(new Intent(this, LearnActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_statistics) {
            DialogFragment dialog = new StatisticsFragment();

            dialog.show(getSupportFragmentManager(), "StatisticsDialogFragment");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.learning_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class StatisticsFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View v = getActivity().getLayoutInflater()
                    .inflate(R.layout.fragment_statistics_dialog, null);
            TextView c = (TextView) v.findViewById(R.id.correct);
            TextView i = (TextView) v.findViewById(R.id.incorrect);
            c.setText(String.valueOf(MainActivity.sCorrect));
            i.setText(String.valueOf(MainActivity.sIncorrect));
            builder.setPositiveButton("Ok", null).setView(v);
            // Create the AlertDialog object and return it
            AlertDialog a = builder.create();
            float density = getResources().getDisplayMetrics().density;
            a.getWindow().setLayout((int) (160 * density),(int) (160 * density));
            return a;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i("qqq", "main activity on Resume");
    }
}
