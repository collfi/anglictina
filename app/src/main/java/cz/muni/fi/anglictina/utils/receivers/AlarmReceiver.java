package cz.muni.fi.anglictina.utils.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.muni.fi.anglictina.App;
import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.activities.LearnActivity;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;

/**
 * Created by collfi on 7. 2. 2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final long HALF_DAY_MILIS = 43200000; //12 hours
    public static final long SIX_HOURS_MILIS = 21600000; //6 hours
    public static final long INTERVAL = 21600000; //6 hours
    public static final String INTENT_UPDATE = "intentUpdate";
    private Context mContext;
    private SharedPreferences sp;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("zxcv", "received alarm " + SystemClock.elapsedRealtime());
        mContext = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (isOnline()) {
            new GetFromServer(context).execute();
//            new PostToServer().execute();
        } else {
            Intent i = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, i, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, System.currentTimeMillis() + INTERVAL, pendingIntent);
            Intent intent2 = new Intent(INTENT_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
            if (App.isAppVisible()) {
                Toast.makeText(context, "Nejste připojen k Internetu.", Toast.LENGTH_SHORT).show();
            }
        }
        if (!App.isAppVisible()) {
            checkRepeating();
        }
    }

    public boolean checkRepeating() {
        SQLiteDatabase db = new WordDbHelper(mContext).getReadableDatabase();
//        Cursor cur = db.rawQuery("SELECT * FROM " + WordContract.LearnedWordEntry.TABLE_NAME + " WHERE " +
//                WordContract.LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + System.currentTimeMillis() / 1000
//                + " ORDER BY " + WordContract.LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " ASC LIMIT 1", null);
        Cursor cur = db.rawQuery("SELECT * FROM " + WordContract.LearnedWordEntry.TABLE_NAME + " WHERE " +
                WordContract.LearnedWordEntry.COLUMN_NAME_TIME_TO_REPEAT + " < " + System.currentTimeMillis() / 1000
                + " ORDER BY " + WordContract.LearnedWordEntry.COLUMN_NAME_DIFFICULTY + " DESC LIMIT 1", null);
        if (cur.moveToFirst()) {
            if (PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getBoolean("pref_notifications", true)) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_translate_white_36dp)
                        .setContentTitle("Máte slova na opakovaní.")
                        .setContentText("Pamatujete si slovo: " + cur.getString(
                                cur.getColumnIndexOrThrow(WordContract.LearnedWordEntry.COLUMN_NAME_WORD)) + "?")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);
                Intent resultIntent = new Intent(mContext, LearnActivity.class);
                resultIntent.putExtra("notification", true);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addParentStack(LearnActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager = (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());

                cur.close();
                db.close();
            }
            return true;
        } else {
            cur.close();
            db.close();
            return false;
        }
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
        private SQLiteDatabase db;

        public GetFromServer(Context context) {
            db = new WordDbHelper(context).getWritableDatabase();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String lastSync = "0";
                try {
                    lastSync = String.valueOf(sp.getLong("last_sync", 0L));
                } catch (ClassCastException e) {
                    lastSync = String.valueOf(sp.getInt("last_sync", 0));
                    sp.edit().putLong("last_sync", Long.valueOf(lastSync)).commit();
                    Log.e("alarmReceiver", "class exception");
                }
                URL url = new URL("http://collfi.pythonanywhere.com/get/" + lastSync);
                Log.i("last_sync", "from: " + sp.getLong("last_sync", 0L));
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
                if (sp.getLong("last_sync", 0L) == 0L) {
                    Cursor c = db.rawQuery("SELECT * FROM " + WordContract.WordEntry.TABLE_NAME, null);
                    JSONObject jo = new JSONObject();
                    while (c.moveToNext()) {
                        String s = c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_FREQUENCY))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_PERCENTIL))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_LEARNED))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_CATEGORIES))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_HUMAN_CATEGORIES))
                                + "," + c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_PRONUNCIATION));
                        jo.put(c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_WORD)), s);
                    }
                    db.delete(WordContract.WordEntry.TABLE_NAME, null, null);

                    String sql = "INSERT INTO " + WordContract.WordEntry.TABLE_NAME + " VALUES (?,?,?,?,?,?,?,?,?,?,?);";
                    SQLiteStatement statement = db.compileStatement(sql);
                    db.beginTransaction();
                    for (int i = 0; i < ja.length(); i++) {
                        String[] word = jo.getString(ja.getJSONObject(i).getString("english")).split(",");

                        statement.clearBindings();
                        statement.bindLong(1, System.nanoTime());
                        statement.bindString(2, ja.getJSONObject(i).getString("english"));
                        statement.bindString(3, word[0]);
                        statement.bindLong(4, Integer.valueOf(word[1]));
                        statement.bindLong(5, Integer.valueOf(word[2]));
                        statement.bindDouble(6, ja.getJSONObject(i).getDouble("difficulty"));
                        statement.bindLong(7, ja.getJSONObject(i).getInt("learned_count"));
                        statement.bindLong(8, Integer.valueOf(word[3]));
                        statement.bindString(9, word[4]);
                        statement.bindString(10, word[5]);
                        statement.bindString(11, word[6]);
                        statement.execute();
                    }

                    db.setTransactionSuccessful();
                    db.endTransaction();
                    c.close();

                } else {
                    String sql = "UPDATE " + WordContract.WordEntry.TABLE_NAME + " SET "
                            + WordContract.WordEntry.COLUMN_NAME_DIFFICULTY + " = ?"
                            + ", "
                            + WordContract.WordEntry.COLUMN_NAME_LEARNED_COUNT + " = ?"
                            + " WHERE " + WordContract.WordEntry.COLUMN_NAME_WORD + " = ?;";
                    SQLiteStatement statement = db.compileStatement(sql);
                    db.beginTransaction();

                    for (int i = 0; i < ja.length(); i++) {
                        //todo check if is learned
                        statement.clearBindings();
                        statement.bindDouble(1, ja.getJSONObject(i).getDouble("difficulty"));
                        statement.bindLong(2, ja.getJSONObject(i).getInt("learned_count"));
                        statement.bindString(3, ja.getJSONObject(i).getString("english"));
                        statement.execute();
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
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
            Intent intent = new Intent(INTENT_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            db.close();
            SharedPreferences.Editor ed = sp.edit();
            ed.putLong("last_sync", System.currentTimeMillis() / 1000);
            ed.apply();
            if (App.isAppVisible()) {
                if (integer.equals(0)) {
                    Toast.makeText(mContext, "Stahování dat úspěšne dokončeno.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Chyba při stahování dat.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

//    public class PostToServer extends AsyncTask<Void, Void, Integer> {
//
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            try {
//                SharedPreferences pref = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
//                String data = pref.getString("post", "[]");
//                Log.i("post output", data);
//                URL url = new URL("http://collfi.pythonanywhere.com/post");
//
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setDoOutput(true);
//                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
//                dStream.writeBytes(data);
//                dStream.flush();
//                dStream.close();
//                int responseCode = connection.getResponseCode();
//                Log.d("POST", "MSG " + connection.getResponseMessage());
//                Log.d("POST RES", "" + responseCode);
//                if (responseCode != 200) {
//                    return responseCode;
//                }
////                final StringBuilder output = new StringBuilder("Request URL " + url);
////                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
////                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
////                String line = "";
////                StringBuilder responseOutput = new StringBuilder();
////                while ((line = br.readLine()) != null) {
////                    responseOutput.append(line);
////                }
////                br.close();
////
////                Log.d("output", responseOutput.toString());
////
////                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());
//
//
//            } catch (MalformedURLException e) {
//                Log.e("settings", e.getLocalizedMessage());
//                e.printStackTrace();
//            } catch (IOException e) {
//                Log.e("settings", e.getLocalizedMessage());
//                e.printStackTrace();
//            }
//            return 0;
//        }
//
//        @Override
//        protected void onPostExecute(Integer integer) {
//            if (integer.equals(0)) {
//                SharedPreferences sp = mContext.getSharedPreferences("post", Context.MODE_PRIVATE);
//                SharedPreferences.Editor ed = sp.edit();
//                ed.remove("post");
//                ed.apply();
//                Toast.makeText(mContext, "Poslání dát na server bylo úspěšné.", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(mContext, "Chyba při posílaní dat na server.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


}
