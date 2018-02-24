package com.knosseir.admin.bruinfeed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SimpleHoursAdapter extends RecyclerView.Adapter<SimpleHoursAdapter.SimpleHoursViewHolder> {

    private final Context mContext;
    private List<String> mData;

    /*
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
    */

    public static class SimpleHoursViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;

        public SimpleHoursViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.firstLine);
        }
    }

    public SimpleHoursAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    public SimpleHoursViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.hours_menu_row, parent, false);
        return new SimpleHoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleHoursViewHolder holder, final int position) {
        // get element from dataset at this position, and replace the contents of the view with that element
        if (mData.isEmpty()) return;

        if (position >= mData.size() || mData.get(position) == null) {
            return;
        }

        final String name = mData.get(position);
        holder.title.setText(name);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}