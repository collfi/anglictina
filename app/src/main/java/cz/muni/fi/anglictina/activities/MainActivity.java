package cz.muni.fi.anglictina.activities;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
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
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.db.model.Word;
import cz.muni.fi.anglictina.fragments.SettingsFragment;
import cz.muni.fi.anglictina.utils.network.AlarmReceiver;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static int sCorrect;
    public static int sIncorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
//        test();
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("words3.txt")));


                String sql = "INSERT INTO " + WordContract.WordEntry.TABLE_NAME + " VALUES (?,?,?,?,?,?,?,?,?, ?);";
                SQLiteStatement statement = db.compileStatement(sql);
                db.beginTransaction();
                //todo bulk insert
                String line = reader.readLine();
                while (line != null) {
//                    ContentValues values = new ContentValues();
                    String[] s = line.split("\t");
//                    values.put(WordEntry.COLUMN_NAME_WORD, s[0]);
//                    values.put(WordEntry.COLUMN_NAME_TRANSLATIONS, s[1]);
//                    values.put(WordEntry.COLUMN_NAME_FREQUENCY, s[2]);
//                    values.put(WordEntry.COLUMN_NAME_PERCENTIL, s[3]);
//                    values.put(WordEntry.COLUMN_NAME_PRONUNCIATION, s[4]);
//                    values.put(WordEntry.COLUMN_NAME_DIFFICULTY, ((Float.valueOf(s[3]) * 4f / 100f) - 2f));
//                    values.put(WordEntry.COLUMN_NAME_LEARNED_COUNT, 0);
//                    values.put(WordEntry.COLUMN_NAME_LEARNED, 0);
//                    values.put(WordEntry.COLUMN_NAME_CATEGORIES, s[5]);
//                    db.insert(WordEntry.TABLE_NAME, null, values);


                    statement.clearBindings();
                    statement.bindLong(1, System.nanoTime());
                    statement.bindString(2, s[0]);
                    statement.bindString(3, s[1]);
                    statement.bindLong(4, Integer.valueOf(s[2]));
                    statement.bindLong(5, Integer.valueOf(s[3]));
                    statement.bindDouble(6, ((Float.valueOf(s[3]) * 4f / 100f) - 2f));
                    statement.bindLong(7, 0);
                    statement.bindLong(8, 0);
                    statement.bindString(9, s[5]);
                    statement.bindString(10, s[4]);
                    statement.execute();

                    line = reader.readLine();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
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

//        firstTime();
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
        } else if (id == R.id.nav_revise) {
            startActivity(new Intent(this, LearnedWordsActivity.class));
        } else if (id == R.id.nav_feedback) {
            SettingsFragment.FeedbackDialog dialog = new SettingsFragment.FeedbackDialog();
            dialog.show(getSupportFragmentManager(), "feedback");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
            a.getWindow().setLayout((int) (160 * density), (int) (160 * density));
            return a;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i("qqq", "main activity on Resume");
    }

    public void firstTime() {
        SharedPreferences sp = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
        if (sp.getBoolean("run", true)) {
            sp.edit().putBoolean("run", false).apply();
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME, 5000, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void deleteDb(View v) {
        WordDbHelper helper = new WordDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(WordContract.LearnedWordEntry.TABLE_NAME, null, null);
        db.close();
//        new TestTask2().execute();

    }

    public class Levenshtein extends AsyncTask<Void, Pair<Integer, String>, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String a[] = new String[6353];
            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("onlywords.txt")));
                String line = reader.readLine();
                int i = 0;
                while (line != null) {
                    a[i] = line;
                    i++;
                    line = reader.readLine();
                }

                reader.close();
            } catch (IOException ioe) {
                Log.e("Main Activity", "error loading db");
            }
            publishProgress(new Pair<Integer, String>(1, "done loading"));
            for (int i = 0; i < 1; i++) {


                for (int j = 0; j < a.length; j++) {
                    Log.i("leven", "" + i + "-" + j + ". " + a[i] + " = " + levenshteinDistance(a[i], a[j]));
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Pair<Integer, String>... values) {
            Toast.makeText(MainActivity.this, values[0].first + ". " + values[0].second, Toast.LENGTH_SHORT).show();
        }

        public int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
            int len0 = lhs.length() + 1;
            int len1 = rhs.length() + 1;

            // the array of distances
            int[] cost = new int[len0];
            int[] newcost = new int[len0];

            // initial cost of skipping prefix in String s0
            for (int i = 0; i < len0; i++) cost[i] = i;

            // dynamically computing the array of distances

            // transformation cost for each letter in s1
            for (int j = 1; j < len1; j++) {
                // initial cost of skipping prefix in String s1
                newcost[0] = j;

                // transformation cost for each letter in s0
                for (int i = 1; i < len0; i++) {
                    // matching current letters in both strings
                    int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                    // computing cost for each transformation
                    int cost_replace = cost[i - 1] + match;
                    int cost_insert = cost[i] + 1;
                    int cost_delete = newcost[i - 1] + 1;

                    // keep minimum cost
                    newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
                }

                // swap cost/newcost arrays
                int[] swap = cost;
                cost = newcost;
                newcost = swap;
            }

            // the distance is the cost for transforming all letters in both strings
            return cost[len0 - 1];
        }
    }

    public void test() {
        Word w = new Word();
        Word r = new Word();
        Log.i("word", w.equals(r) + "");
        w.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setWord("asdf");
        Log.i("word", w.equals(r) + "");
        r.setTranslations(new String[1]);
        Log.i("word", w.equals(r) + "");
        w.setTranslations(new String[1]);
        Log.i("word", w.getTranslations().length + " " + r.getTranslations().length);
        Log.i("word", w.equals(r) + "");
        w.getTranslations()[0] = "a";
        Log.i("word", w.equals(r) + "");
        r.getTranslations()[0] = "a";
        Log.i("word", w.equals(r) + "");
        r.getTranslations()[0] = "b";
        Log.i("word", w.equals(r) + "");
    }
}
