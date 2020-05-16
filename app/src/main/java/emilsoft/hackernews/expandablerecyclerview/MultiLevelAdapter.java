package emilsoft.hackernews.expandablerecyclerview;


import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.expandablerecyclerview.models.RecyclerViewItem;


public abstract class MultiLevelAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<RecyclerViewItem> recyclerViewItemList = new ArrayList<>();

    public MultiLevelAdapter(List<? extends RecyclerViewItem> recyclerViewItems) {
//        if (recyclerViewItems.get(0) == null) {
//            throw new IllegalArgumentException("Please Add Items Of Class extending RecyclerViewItem");
//        }

//        this.recyclerViewItemList.addAll(recyclerViewItems);
        this.recyclerViewItemList = (List<RecyclerViewItem>) recyclerViewItems;
    }

    void setRecyclerViewItemList(List<RecyclerViewItem> recyclerViewItemList) {
        this.recyclerViewItemList = recyclerViewItemList;
    }

    @NonNull
    @Override
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    @Override
    public int getItemCount() {
        return recyclerViewItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recyclerViewItemList.get(position).getLevel();
    }

    List<RecyclerViewItem> getRecyclerViewItemList() {
        return recyclerViewItemList;
    }
}
