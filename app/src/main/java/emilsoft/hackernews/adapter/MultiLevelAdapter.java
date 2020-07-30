package emilsoft.hackernews.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.api.RecyclerViewItem;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class MultiLevelAdapter<T extends RecyclerViewItem<T>, VH extends MultiLevelAdapter.MultiLevelViewHolder<T>>
        extends RecyclerView.Adapter<VH> {

    private List<T> items;
    CompositeDisposable disposables;

    public MultiLevelAdapter(List<T> recyclerViewItems) {
        if(recyclerViewItems == null)
            recyclerViewItems = new ArrayList<>();
        this.items = recyclerViewItems;
        disposables = new CompositeDisposable();
    }

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {
        T item = items.get(position);
        onBindViewHolder(holder, position, item);
    }

    public abstract void onBindViewHolder(@NonNull VH holder, int position, T item);

    @Override
    public final int getItemCount() {
        if(items == null)
            return 0;
        return items.size();
    }

    protected final CollapseItemsListener<T> collapseCommentListener = new CollapseItemsListener<T>() {

        @SuppressLint("CheckResult")
        @Override
        public void onCollapse(T item) {
            final int index = items.indexOf(item);
            final int level = item.getLevel();
            if(index == -1) return;
            int i = index + 1;

            List<T> children = new ArrayList<>();
            T temp;
            while (i < items.size() && (temp = items.get(i)).getLevel() > level) {
                children.add(temp);
                if(temp.hasChildren()) // already collapsed items, not currently contained in items
                    children.addAll(temp.getChildren());
                i++;
            }
            items.subList(index + 1, i).clear();
            notifyItemRangeRemoved(index + 1, i - (index + 1));
            item.setChildren(children);
        }

        @Override
        public void onExpand(T item) {
            int index = items.indexOf(item);
            if(index == -1) return;
            if(item.hasChildren()) {
                List<T> children = item.getChildren();
                item.setChildren(null);

                // Remove children of collapsed comments
                List<T> expandedChildren = new ArrayList<>();
                for(int i = 0; i < children.size(); i++) {
                    expandedChildren.add(children.get(i));
                    if(children.get(i).hasChildren())
                        i += children.get(i).getChildren().size();
                }

                items.addAll(index + 1, expandedChildren);
                notifyItemRangeInserted(index + 1, expandedChildren.size());
            }
        }
    };

    public void dispose() {
        disposables.dispose();
    }

    @SuppressLint("CheckResult")
    public final void addItem(T item) {
        Single.create((SingleOnSubscribe<Optional<T>>) emitter -> {
            long idParent = item.getParent();
            int itemIndex = items.indexOf(item);

            int index = indexOf(items, idParent);
            if(itemIndex != -1) {
                //The comment is visible and items contains item
                update(items, item, itemIndex);
                if(index != -1) {
                    T parentInstance = items.get(index);
                    item.setParentInstance(parentInstance);
                    emitter.onSuccess(Optional.of(item));
                } else {
                    //it's a top-comment
                    emitter.onSuccess(Optional.of(item));
                }
            } else {
                //This is a new fresh comment or a collapsed comment
                if(index != -1) {
                    T parentInstance = items.get(index);
                    item.setParentInstance(parentInstance);
                    if(parentInstance.isCollapsed()) {
                        // the comment is collapsed but the parent is not
                        if (!parentInstance.hasChildren()) {
                            parentInstance.setChildren(new ArrayList<>());
                        }
                        int i = parentInstance.getChildren().indexOf(item);
                        if(i != -1) {
                            update(parentInstance.getChildren(), item, i);
                            parentInstance.getChildren().set(i, item);
                        } else {
                            item.setLevel(parentInstance.getLevel() + 1);
                            parentInstance.getChildren().add(item);
                        }
                        emitter.onSuccess(Optional.empty());
                    } else {
                        // the comment is not collapsed and it's child of another comment
                        emitter.onSuccess(Optional.of(item));
                    }
                } else {
                    // it's a fresh new comment or the parent is also collapsed
//                        RecyclerViewItem visibleComment = items.stream()
//                                .filter((visComment -> visComment.hasChildren() && visComment.getChildren().contains(parent)))
//                                .findFirst()
//                                .orElse(null);

                    int parentIndex = -1;
                    int visibleCommentIndex = 0;
                    boolean parentFound = false;
                    while(visibleCommentIndex < items.size() && !parentFound) {
                        T visibleComment = items.get(visibleCommentIndex);
                        if(visibleComment.hasChildren() && (parentIndex = indexOf(visibleComment.getChildren(), index)) != -1) //TODO Perché ad indexOf gli passo index? E non un id?
                            parentFound = true;
                        visibleCommentIndex++;
                    }
                    if(parentFound) {
                        T visibleComment = items.get(visibleCommentIndex - 1);
                        T parentInstance = visibleComment.getChildren().get(parentIndex);
                        item.setParentInstance(parentInstance);
                        if(parentInstance.isCollapsed()) {
                            // parent has collapsed children
                            if(!parentInstance.hasChildren()) {
                                parentInstance.setChildren(new ArrayList<>());
                            }
                            int i = parentInstance.getChildren().indexOf(item);
                            if(i != -1) {
                                update(parentInstance.getChildren(), item, i);
                                parentInstance.getChildren().set(i, item);
                            } else {
                                item.setLevel(parentInstance.getLevel() + 1);
                                parentInstance.getChildren().add(item);
                            }
                        } else {
                            // parent has not collapsed children
                            // this else can be removed
                            Log.v(MainActivity.TAG, "parent has not collapsed children but one of the parent's parent is collapsed");
                        }
                        // maintain coherency with onCollapse
                        int i = visibleComment.getChildren().indexOf(item);
                        if(i != -1) {
                            update(visibleComment.getChildren(), item, i);
                            visibleComment.getChildren().set(i, item);
                        } else {
                            item.setLevel(parentInstance.getLevel() + 1);
                            visibleComment.getChildren().add(parentIndex + 1, item);
                        }
                        //the comment is collapsed and so not visible
                        emitter.onSuccess(Optional.empty());
                    } else {
                        // it's a fresh new top comment (doesn't have a parent and is not contained in items)
                        emitter.onSuccess(Optional.of(item));
                    }
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> disposables.add(disposable))
                .subscribe(optionalItem -> {
                    // the item is visible and must be added to items
                    optionalItem.ifPresent(this::addCommentToList);
                });
    }

    private static <T extends RecyclerViewItem<T>> void update(List<T> items, T item, int index) {
        T oldItem = items.get(index);
        item.setLevel(oldItem.getLevel());
        item.setIsCollapsed(oldItem.isCollapsed());
        item.setChildren(oldItem.getChildren());
    }

    private void addCommentToList(T item) {
        int pos = items.size();
        int index;
        T parent = item.getParentInstance();
        if(parent != null) {
            item.setLevel(parent.getLevel() + 1);
            index = items.indexOf(parent);
        } else {
            //it's a top level comment
            item.setLevel(1);
            index = pos;
        }
        int indexToInsert = index + 1;
        if(!items.contains(item)) {
            append(items, item, indexToInsert);
            notifyItemInserted(indexToInsert);
        }
    }

    private static <T extends RecyclerViewItem<T>> int indexOf(List<T> items, long id) {
        int index = 0;
        while(index < items.size() && items.get(index).getId() != id)
            index++;
        return (index >= items.size()) ? -1 : index;
    }

    private static <T extends RecyclerViewItem<T>> void append(List<T> items, T item, int index) {
        int size = items.size();
        int i = (index < 0 || index > size) ? size : index;
        items.add(i, item);
    }

    public static class MultiLevelViewHolder<T extends RecyclerViewItem<T>> extends RecyclerView.ViewHolder {

        private CollapseItemsListener<T> listener;

        public MultiLevelViewHolder(@NonNull ViewBinding binding, CollapseItemsListener<T> listener) {
            super(binding.getRoot());
            this.listener = listener;
        }
    }

    protected interface CollapseItemsListener<T extends RecyclerViewItem<T>> {

        void onCollapse(T item);

        void onExpand(T item);
    }

}
