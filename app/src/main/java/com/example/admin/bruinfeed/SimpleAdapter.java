package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {

    private final Context mContext;
    private List<MealItem> mData;

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

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView description;

        public SimpleViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.firstLine);
            description = (TextView) view.findViewById(R.id.secondLine);
        }
    }

    public SimpleAdapter(Context context, List<MealItem> data) {
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

        final MealItem temp = mData.get(position);

        holder.title.setText(temp.getName());
        holder.description.setText(temp.getDescription());

        View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mealItemIntent = new Intent(mContext, MealItemActivity.class);
                mealItemIntent.putExtra("MealItem", temp);
                mContext.startActivity(mealItemIntent);
            }
        };

        ((View) holder.title.getParent()).setOnClickListener(menuItemOnClickListener);
        ((View) holder.description.getParent()).setOnClickListener(menuItemOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}