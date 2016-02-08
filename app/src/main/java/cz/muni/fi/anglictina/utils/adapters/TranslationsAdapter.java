package cz.muni.fi.anglictina.utils.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cz.muni.fi.anglictina.R;

/**
 * Created by collfi on 9. 1. 2016.
 */
public class TranslationsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<String> translations;

    public TranslationsAdapter(Context context, List<String> list) {
        inflater = LayoutInflater.from(context);
        translations = list;

    }

    @Override
    public int getCount() {
        return translations.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_translation, parent, false);
            view.setId(position);
            holder = new ViewHolder();
            holder.translation = (TextView) view.findViewById(R.id.translation);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.translation.setText(translations.get(position));
        return view;
    }

    static class ViewHolder {
        public TextView translation;
    }
}
