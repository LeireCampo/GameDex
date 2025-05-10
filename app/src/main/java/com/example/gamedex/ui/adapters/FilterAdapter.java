package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gamedex.R;
import com.example.gamedex.data.remote.model.GenreListResponse;
import com.example.gamedex.data.remote.model.PlatformListResponse;

import java.util.List;

public class FilterAdapter<T> extends ArrayAdapter<T> {
    private final LayoutInflater inflater;

    public FilterAdapter(@NonNull Context context, @NonNull List<T> objects) {
        super(context, R.layout.item_filter, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_filter, parent, false);
        }

        TextView textView = view.findViewById(R.id.text_filter_name);
        T item = getItem(position);

        if (item instanceof PlatformListResponse.Platform) {
            textView.setText(((PlatformListResponse.Platform) item).getName());
        } else if (item instanceof GenreListResponse.Genre) {
            textView.setText(((GenreListResponse.Genre) item).getName());
        } else {
            textView.setText(item.toString());
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}