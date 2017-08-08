package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {

    private final Context mContext;
    private List<MealItem> mData;
    private int mLayout;

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

        public SimpleViewHolder(View view, int titleId, int descriptionId) {
            super(view);
            title = (TextView) view.findViewById(titleId);
            description = (TextView) view.findViewById(descriptionId);
        }
    }

    public SimpleAdapter(Context context, List<MealItem> data) {
        mContext = context;
        mData = data;
        mLayout = 0;
    }

    public SimpleAdapter(Context context, List<MealItem> data, int layoutId) {
        mContext = context;
        mData = data;
        mLayout = layoutId;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayout == 0) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.menu_row, parent, false);
            return new SimpleViewHolder(view);
        }
        else {
            final View view = LayoutInflater.from(mContext).inflate(mLayout, parent, false);
            return new SimpleViewHolder(view, R.id.favorites_name, R.id.favorites_description);
        }
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        // - get element from dataset at this position
        // - replace the contents of the view with that element
        if (mData.size() == 0) return;

        final String name = mData.get(position).getName();
        final String description = mData.get(position).getDescription();
        final String url = mData.get(position).getUrl();

        holder.title.setText(name);
        holder.description.setText(description);

        View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mealItemIntent = new Intent(mContext, MealItemActivity.class);
                mealItemIntent.putExtra("Name", name);
                mealItemIntent.putExtra("Description", description);
                mealItemIntent.putExtra("url", url);

                mContext.startActivity(mealItemIntent);
            }
        };

        ((View)holder.title.getParent()).setOnClickListener(menuItemOnClickListener);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}