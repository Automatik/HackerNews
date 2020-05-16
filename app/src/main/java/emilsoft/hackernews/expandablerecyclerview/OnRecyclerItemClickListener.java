package emilsoft.hackernews.expandablerecyclerview;

import android.view.View;

import emilsoft.hackernews.expandablerecyclerview.models.RecyclerViewItem;

public interface OnRecyclerItemClickListener {
    void onItemClick(View view, RecyclerViewItem item, int position);
}