package cz.muni.fi.anglictina.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import cz.muni.fi.anglictina.R;

/**
 * Created by collfi on 29. 5. 2016.
 */
public class AboutFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        final TextView textView = (TextView) getDialog().findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        //Do something!
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String version = "1.0";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setTitle("O aplikaci");
        SpannableString s =
                new SpannableString("Aplikace na adaptabilní učení anglické slovní zásoby. Přizpůsobuje se " +
                        "uživatelově úrovni. Implementuje techniku opakování se zpožděním. Tato aplikace vznikla " +
                        "jako diplomová práce na Fakultě Informatiky Masarykovy Univerzity. Všechna data" +
                        " jsou zpracovávána anonymně.\n\nAutor: Boris " +
                        "Valentovič \nRok: 2016 \nVerze: " + version + "\ngithub: " +
                        getString(R.string.about));
        Linkify.addLinks(s, Linkify.ALL);
        builder.setMessage(s);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });


       return builder.create();
    }
}
