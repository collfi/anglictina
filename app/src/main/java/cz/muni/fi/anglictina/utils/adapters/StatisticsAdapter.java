package cz.muni.fi.anglictina.utils.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cz.muni.fi.anglictina.R;
import cz.muni.fi.anglictina.db.WordContract;
import cz.muni.fi.anglictina.db.WordDbHelper;
import cz.muni.fi.anglictina.utils.ColorArray;

/**
 * Created by collfi on 30. 4. 2016.
 */
public class StatisticsAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> words;
    private List<String> translations;
    private List<Integer> chances;
    private List<String> difficulties;

    public StatisticsAdapter(Context context) {
        mContext = context;
        words = new ArrayList<>(20);
        chances = new ArrayList<>(20);
        translations = new ArrayList<>(20);
        difficulties = new ArrayList<>(20);
        SQLiteDatabase db = new WordDbHelper(mContext).getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + WordContract.WordEntry.TABLE_NAME
                + " ORDER BY " + WordContract.WordEntry.COLUMN_NAME_DIFFICULTY + " ASC", null);
        //todo catch exception ak sa nepodari posun cursoru
        for (int i = 0; i < 20; i++) {
            float chance = 0f;
            words.add("");
            translations.add("");
            difficulties.add("");
            String divider = "";
            for (int j = 0; j < 5; j++) {
                int r = new Random(System.nanoTime()).nextInt((317 * (i + 1)) - (317 * (i))) + (317 * (i));
                c.moveToPosition(r);
                String word = c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_WORD));
                String translation = c.getString(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_TRANSLATIONS));
                String diff = String.valueOf(c.getFloat(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY)));
                words.set(i, words.get(i) + divider + word);
                translations.set(i, translations.get(i) + divider + translation.split(";")[0]);
                difficulties.set(i, difficulties.get(i) + divider + diff);
                chance += 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                        - c.getFloat(c.getColumnIndexOrThrow(WordContract.WordEntry.COLUMN_NAME_DIFFICULTY)))));
                divider = ", ";
            }
            chances.add((int) ((chance * 100) / 5));
        }
        c.close();
        db.close();
    }

    static class GroupViewHolder {
        public TextView chance;
        public TextView example;
    }

    static class ChildViewHolder {
        public TextView e1;
        public TextView c1;
        public TextView e2;
        public TextView c2;
        public TextView e3;
        public TextView c3;
        public TextView e4;
        public TextView c4;
        public TextView e5;
        public TextView c5;
        public TextView ch1;
        public TextView ch2;
        public TextView ch3;
        public TextView ch4;
        public TextView ch5;


    }

    @Override
    public int getGroupCount() {
        return 20;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null; //todo
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;//todo
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;
        final GroupViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_statistics, parent, false);
            view.setId(groupPosition);
            holder = new GroupViewHolder();
            holder.chance = (TextView) view.findViewById(R.id.chance);
            holder.example = (TextView) view.findViewById(R.id.example);
            view.setTag(holder);
        } else {
            holder = (GroupViewHolder) view.getTag();
        }
        holder.chance.setText(chances.get(groupPosition) + "%");
        holder.example.setText(words.get(groupPosition));
        String s = Integer.toHexString((int) (chances.get(groupPosition) * 2.55));

//        view.setBackgroundColor(Color.parseColor("#00" + ((s.length() == 1) ? "0" + s : s) + "00"));

//        view.setBackgroundColor(Color.parseColor("#" + ((s.length() == 1) ? "0" + s : s) + ((s.length() == 1) ? "0" + s : s) + ((s.length() == 1) ? "0" + s : s)));
        view.setBackgroundColor(Color.parseColor(ColorArray.colors[chances.get(groupPosition)]));
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChildViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_expansion_item_knowledge_map, parent, false);
            view.setId(groupPosition);
            holder = new ChildViewHolder();
            holder.e1 = (TextView) view.findViewById(R.id.english1);
            holder.c1 = (TextView) view.findViewById(R.id.czech1);
            holder.e2 = (TextView) view.findViewById(R.id.english2);
            holder.c2 = (TextView) view.findViewById(R.id.czech2);
            holder.e3 = (TextView) view.findViewById(R.id.english3);
            holder.c3 = (TextView) view.findViewById(R.id.czech3);
            holder.e4 = (TextView) view.findViewById(R.id.english4);
            holder.c4 = (TextView) view.findViewById(R.id.czech4);
            holder.e5 = (TextView) view.findViewById(R.id.english5);
            holder.c5 = (TextView) view.findViewById(R.id.czech5);
            holder.ch1 = (TextView) view.findViewById(R.id.chance1);
            holder.ch2 = (TextView) view.findViewById(R.id.chance2);
            holder.ch3 = (TextView) view.findViewById(R.id.chance3);
            holder.ch4 = (TextView) view.findViewById(R.id.chance4);
            holder.ch5 = (TextView) view.findViewById(R.id.chance5);
            view.setTag(holder);
        } else {
            holder = (ChildViewHolder) view.getTag();
        }
        String[] cze = translations.get(groupPosition).split(", ");
        String[] eng = words.get(groupPosition).split(", ");
        String[] cha = difficulties.get(groupPosition).split(", ");
        float chance1 = 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                - Float.valueOf(cha[0]))));
        float chance2 = 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                - Float.valueOf(cha[1]))));
        float chance3 = 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                - Float.valueOf(cha[2]))));
        float chance4 = 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                - Float.valueOf(cha[3]))));
        float chance5 = 1 / (1 + (float) Math.exp(-(mContext.getSharedPreferences("stats", Context.MODE_PRIVATE).getFloat("skill", 0)
                - Float.valueOf(cha[4]))));

        holder.e1.setText(eng[0]);
        holder.c1.setText(cze[0]);
        holder.e2.setText(eng[1]);
        holder.c2.setText(cze[1]);
        holder.e3.setText(eng[2]);
        holder.c3.setText(cze[2]);
        holder.e4.setText(eng[3]);
        holder.c4.setText(cze[3]);
        holder.e5.setText(eng[4]);
        holder.c5.setText(cze[4]);
        holder.ch1.setText(String.format(Locale.getDefault(), "%.0f%%", chance1 * 100));
        holder.ch2.setText(String.format(Locale.getDefault(), "%.0f%%", chance2 * 100));
        holder.ch3.setText(String.format(Locale.getDefault(), "%.0f%%", chance3 * 100));
        holder.ch4.setText(String.format(Locale.getDefault(), "%.0f%%", chance4 * 100));
        holder.ch5.setText(String.format(Locale.getDefault(), "%.0f%%", chance5 * 100));
        view.setBackgroundColor(Color.parseColor(ColorArray.colors[chances.get(groupPosition)]));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
