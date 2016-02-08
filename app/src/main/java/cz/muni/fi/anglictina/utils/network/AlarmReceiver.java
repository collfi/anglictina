package cz.muni.fi.anglictina.utils.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;

/**
 * Created by collfi on 7. 2. 2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final long HALF_DAY_MILIS = 43200000;
    public static final long SIX_HOURS_MILIS = 21600000;
    private Context mContext;
    private SharedPreferences sp;
    private SQLiteDatabase db;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        db = new WordDbHelper(context).getWritableDatabase();
        if (isOnline()) {
            new GetFromServer().execute();
            new PostToServer().execute();
        } else {
            Intent i = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + HALF_DAY_MILIS, pendingIntent);
        }

        Log.i("asdf", "fdsa");
        //sync
    }

    //todo -> alarm?/ ak nie je online tak receiver to isonline wifi// pv256 sync manager?
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public class GetFromServer extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... params) {
            try {


                URL url = new URL("http://collfi.pythonanywhere.com/get/" + sp.getInt("last_sync", 0)); //

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                Log.d("GET", "MSG " + connection.getResponseMessage());
                Log.d("GET RES", "" + responseCode);
                if (responseCode != 200) {
                    return responseCode;
                }
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();
                Log.d("get output", responseOutput.toString());

                JSONArray ja = new JSONArray(responseOutput.toString());
                for (int i = 0; i < ja.length(); i++) {
                    //todo check if is learned
                    ContentValues cv = new ContentValues();
                    cv.put(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY,
                            ja.getJSONObject(i).getDouble("difficulty"));
                    cv.put(WordContract.WordEntry.COLUMN_NAME_LEARNED_COUNT,
                            ja.getJSONObject(i).getInt("learned_count"));//++
                    db.update(WordContract.WordEntry.TABLE_NAME, cv, WordContract.WordEntry.COLUMN_NAME_WORD + " = ?",
                            new String[]{ja.getJSONObject(i).getString("english")});
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("settings", "json exception downloading. " + e.getLocalizedMessage());
            } catch (MalformedURLException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            db.close();
            SharedPreferences.Editor ed = sp.edit();
            ed.putInt("last_sync", (int) (System.currentTimeMillis() / 1000));
            ed.apply();
            if (integer.equals(0)) {
                Toast.makeText(mContext, "Sync task 1 succeeded.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Sync 1 failed because of code: " + integer, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class PostToServer extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... params) {
            try {
                SharedPreferences pref = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
                String data = pref.getString("post", "[]");
                Log.i("post output", data);
                URL url = new URL("http://collfi.pythonanywhere.com/post");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(data);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                Log.d("POST", "MSG " + connection.getResponseMessage());
                Log.d("POST RES", "" + responseCode);
                if (responseCode != 200) {
                    return responseCode;
                }
//                final StringBuilder output = new StringBuilder("Request URL " + url);
//                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
//                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String line = "";
//                StringBuilder responseOutput = new StringBuilder();
//                while ((line = br.readLine()) != null) {
//                    responseOutput.append(line);
//                }
//                br.close();
//
//                Log.d("output", responseOutput.toString());
//
//                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());


            } catch (MalformedURLException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("settings", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer.equals(0)) {
                SharedPreferences sp = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.remove("post");
                ed.apply();
                Toast.makeText(mContext, "Sync task 2 succeeded.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Sync 2 failed because of code: " + integer, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
