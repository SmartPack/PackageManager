package com.smartpack.packagemanager.views.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.smartpack.packagemanager.R;
import com.smartpack.packagemanager.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by willi on 17.04.16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnViewChangedListener {
        void viewChanged();
    }

    private final List<RecyclerViewItem> mItems;
    private final Map<RecyclerViewItem, View> mViews = new HashMap<>();
    private OnViewChangedListener mOnViewChangedListener;
    private View mFirstItem;

    public RecyclerViewAdapter(List<RecyclerViewItem> items, OnViewChangedListener onViewChangedListener) {
        mItems = items;
        mOnViewChangedListener = onViewChangedListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecyclerViewItem item = mItems.get(position);
        item.onCreateView(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerViewItem item = mItems.get(viewType);
        View view;
        if (item.cacheable()) {
            if (mViews.containsKey(item)) {
                view = mViews.get(item);
            } else {
                mViews.put(item, view = LayoutInflater.from(parent.getContext())
                        .inflate(item.getLayoutRes(), parent, false));
            }
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(item.getLayoutRes(), parent, false);
        }
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(view);
        }
        if (item.cardCompatible()
                && Utils.getBoolean("forcecards", false, view.getContext())) {
            androidx.cardview.widget.CardView cardView = new androidx.cardview.widget.CardView(view.getContext());
            cardView.setRadius(view.getResources().getDimension(R.dimen.cardview_radius));
            cardView.setCardElevation(view.getResources().getDimension(R.dimen.cardview_elevation));
            cardView.setUseCompatPadding(true);
            cardView.setFocusable(false);
            cardView.addView(view);
            view = cardView;
        }
        if (viewType == item.getLayoutRes()) {
            mFirstItem = view;
        }
        item.setOnViewChangeListener(mOnViewChangedListener);
        item.onCreateHolder(parent, view);

        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public View getFirstItem() {
        return mFirstItem;
    }

}
