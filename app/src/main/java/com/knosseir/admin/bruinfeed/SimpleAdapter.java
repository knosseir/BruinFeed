package com.knosseir.admin.bruinfeed;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        public final ImageButton favorite;

        public SimpleViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.firstLine);
            description = view.findViewById(R.id.secondLine);
            favorite = view.findViewById(R.id.favorite_indicator);
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
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
        // get element from dataset at this position, and replace the contents of the view with that element
        if (mData.isEmpty()) return;

        if (position >= mData.size() || mData.get(position) == null) {
            return;
        }

        final MealItem selectedMealItem = mData.get(position);
        holder.title.setText(selectedMealItem.getName());

        String description = selectedMealItem.getDescription();
        String descriptors = selectedMealItem.getDescriptors();

        final boolean isFavorite = selectedMealItem.getFavorite(mContext);

        holder.favorite.setImageResource(R.drawable.ic_star_border_black_24dp);

        if (isFavorite) {
            holder.favorite.setImageResource(R.drawable.ic_star_black_24dp);
        }

        // if meal item does not have a description, then display other useful information
        if (description.equals("No description available") && !descriptors.equals("")) {
            holder.description.setText(descriptors);
        } else {
            holder.description.setText(description);
        }

        // create OnClickListener to launch MealItemActivity for the MealItem that was selected
        View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mealItemIntent = new Intent(mContext, MealItemActivity.class);
                mealItemIntent.putExtra("MealItem", selectedMealItem);
                mealItemIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(mealItemIntent);
            }
        };

        // create OnCLickListener to favorite a MealItem
        View.OnClickListener favoriteOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMealItem.setFavorite(mContext, !isFavorite);
                if (selectedMealItem.getFavorite(mContext)) {
                    Snackbar.make(v, R.string.favorites_add, Snackbar.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                } else {
                    Snackbar.make(v, R.string.favorites_remove, Snackbar.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
            }
        };

        // set the appropriate OnClickListeners
        ((View) holder.title.getParent()).setOnClickListener(menuItemOnClickListener);
        ((View) holder.description.getParent()).setOnClickListener(menuItemOnClickListener);
        holder.favorite.setOnClickListener(favoriteOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}