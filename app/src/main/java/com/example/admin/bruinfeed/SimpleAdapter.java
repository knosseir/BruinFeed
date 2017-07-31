package com.example.admin.bruinfeed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {

    private final Context mContext;
    private List<String> mData;

    public void add(String s, int position) {
        mData.add(position, s);
        notifyItemInserted(position);
    }

    public void remove(int position){
        if (position < getItemCount()  ) {
            mData.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;

        public SimpleViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.firstLine);
        }
    }

    public SimpleAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        mData = data;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.menu_row, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        // - get element from dataset at this position
        // - replace the contents of the view with that element
        if (mData.size() == 0) return;

        final String name = mData.get(position);
        holder.title.setText(name);
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}