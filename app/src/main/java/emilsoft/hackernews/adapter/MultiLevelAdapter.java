package emilsoft.hackernews.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import emilsoft.hackernews.BuildConfig;
import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.MultiLevelData;
import emilsoft.hackernews.api.RecyclerViewItem;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public abstract class MultiLevelAdapter<VH extends MultiLevelAdapter.MultiLevelViewHolder>
        extends RecyclerView.Adapter<VH> {

    private List<RecyclerViewItem> items;
    private HashMap<Long, List<RecyclerViewItem>> collapsedParents;
    private HashMap<Long, Long> collapsedChildren;

    public MultiLevelAdapter(MultiLevelData data) {
        if(data == null)
            data = new MultiLevelData();
        if(data.items == null)
            data.items = new ArrayList<>();
        if(data.collapsedParents == null)
            data.collapsedParents = new HashMap<>();
        if(data.collapsedChildren == null)
            data.collapsedChildren = new HashMap<>();
        this.items = data.items;
        this.collapsedParents = data.collapsedParents;
        this.collapsedChildren = data.collapsedChildren;
    }

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {
        RecyclerViewItem item = items.get(position);
//        boolean isItemCollapsed = collapsedParents.containsKey(item.getId());
        boolean isItemCollapsed = item.isCollapsed();
        onBindViewHolder(holder, position, item, isItemCollapsed);
    }

    public abstract void onBindViewHolder(@NonNull VH holder, int position, RecyclerViewItem item, boolean isItemCollapsed);

    @Override
    public final int getItemCount() {
        if(items == null)
            return 0;
        return items.size();
    }

    protected final CollapseItemsListener collapseCommentListener = new CollapseItemsListener() {

        @SuppressLint("CheckResult")
        @Override
        public void onCollapse(RecyclerViewItem item) {
            final int index = items.indexOf(item);
            final int level = item.getLevel();
            if(index == -1) return;
            int i = index + 1;

            //it's buggy, sometimes it  throws AssertionError("parentParentChildren is null") on line 184
//            Observable.fromIterable(items.subList(i, items.size()))
//                    .takeWhile(c -> c.getLevel() > level)
//                    .toList()
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(children -> {
//                        items.subList(index + 1, index + 1 + children.size()).clear();
//                        notifyItemRangeRemoved(index + 1, children.size());
//                        collapsedParents.put(item.getId(), children);
//                    });

            //Without using RxJava

            List<RecyclerViewItem> children = new ArrayList<>();
            RecyclerViewItem temp;
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
        public void onExpand(RecyclerViewItem item) {
            int index = items.indexOf(item);
            if(index == -1) return;
            if(item.hasChildren()) {
                List<RecyclerViewItem> children = item.getChildren();
                item.setChildren(null);
                // Remove children of collapsed comments
                children.removeIf(c -> c.getParentInstance().hasChildren());

                items.addAll(index + 1, children);
                notifyItemRangeInserted(index + 1, children.size());
            }
        }
    };

    @SuppressLint("CheckResult")
    public final void addItem2(RecyclerViewItem item) {
        long idParent = item.getParent();
        int itemIndex = items.indexOf(item);
        if(itemIndex != -1) {
            //The comment is visible and items contains item
            update(items, item, itemIndex);
            final RecyclerViewItem parent = new Comment(idParent);
            int index = items.indexOf(parent);
            if(index != -1) {
                RecyclerViewItem parentInstance = items.get(index);
                item.setParentInstance(parentInstance);
                addCommentToList(item);
            } else {
                //it's a top-comment
                addCommentToList(item);
            }
        } else {
            //This is a new fresh comment or a collapsed comment
            final RecyclerViewItem parent = new Comment(idParent);
            int index = items.indexOf(parent);
            if(index != -1) {
                RecyclerViewItem parentInstance = items.get(index);
                item.setParentInstance(parentInstance);
                if(parentInstance.isCollapsed()) {
                    // the comment is collapsed but the parent is not
                    if (!parentInstance.hasChildren()) {
                        Log.v(MainActivity.TAG, "parentInstance.hasChildren = false");
                        parentInstance.setChildren(new ArrayList<>());
                    }
                    parentInstance.getChildren().add(item); //or update old item?
                } else {
                    // the comment is not collapsed and it's child of another comment
                    addCommentToList(item);
                }
            } else {
                // it's a fresh new comment or the parent is also collapsed

                //                RecyclerViewItem visibleComment = items.stream()
//                        .filter((visComment -> visComment.hasChildren() && visComment.getChildren().contains(parent)))
//                        .findFirst()
//                        .orElse(null);

//                Observable.fromIterable(items).forEachWhile(visibleComments -> {
//                    int i;
//                    if(visibleComments.hasChildren() && (i = visibleComments.getChildren().indexOf(parent)) != -1) {
//                        RecyclerViewItem parentInstance = visibleComments.getChildren().get(i);
//                        item.setParentInstance(parentInstance);
//                        if(parentInstance.isCollapsed()) {
//                            // parent has collapsed children
//                            if(!parentInstance.hasChildren())
//                                parentInstance.setChildren(new ArrayList<>());
//                            parentInstance.getChildren().add(item);
//                        } else {
//                            // parent has not collapsed children
//                            visibleComments.getChildren().add(i + 1, item);
//                        }
//                        return false;
//                    }
//                    return true;
//                });

                int parentIndex = -1;
                int visibleCommentIndex = 0;
                boolean parentFound = false;
                while(visibleCommentIndex < items.size() && !parentFound) {
                    RecyclerViewItem visibleComment = items.get(visibleCommentIndex);
                    if(visibleComment.hasChildren() && (parentIndex = visibleComment.getChildren().indexOf(parent)) != -1)
                        parentFound = true;
                    visibleCommentIndex++;
                }
                if(parentFound) {
                    RecyclerViewItem visibleComment = items.get(visibleCommentIndex - 1);
                    RecyclerViewItem parentInstance = visibleComment.getChildren().get(parentIndex);
                    item.setParentInstance(parentInstance);
                    if(parentInstance.isCollapsed()) {
                        // parent has collapsed children
                        if(!parentInstance.hasChildren())
                            parentInstance.setChildren(new ArrayList<>());
                        parentInstance.getChildren().add(item);
                    } else {
                        // parent has not collapsed children
                        Log.v(MainActivity.TAG, "parent has not collapsed children but the parent should be collapsed here");
                    }
                    visibleComment.getChildren().add(parentIndex + 1, item); // maintain coherency with onCollapse
                } else {
                    // it's a fresh new top comment (doesn't have a parent and is not contained in items)
                    addCommentToList(item);
                }
            }
        }
    }

    private static void update(List<RecyclerViewItem> items, RecyclerViewItem item) {
        int index = items.indexOf(item);
        if(index == -1) return;
        update(items, item, index);
    }

    private static void update(List<RecyclerViewItem> items, RecyclerViewItem item, int index) {
        RecyclerViewItem oldItem = items.get(index);
        item.setIsCollapsed(oldItem.isCollapsed());
    }

    public final void addItem(RecyclerViewItem item) {
        long idParent = item.getParent();
        List<RecyclerViewItem> parentChildren = collapsedParents.get(idParent);
        if(parentChildren != null) {
            //This item is collapsed
            List<RecyclerViewItem> children = collapsedParents.get(item.getId());
            if(children != null)
                //This item has children collapsed
                children.clear();
            Long idCollapsedParent = collapsedChildren.get(idParent);
            if(idCollapsedParent != null) {
                List<RecyclerViewItem> parentParentChildren = collapsedParents.get(idCollapsedParent);
                if(parentParentChildren != null) {
                    // This item is child of a collapsed parent
                    collapsedChildren.put(item.getId(), idParent); //idCollapsedParent wrong
                    int index = indexOf(parentParentChildren, idParent);
                    if(index > -1) {
                        RecyclerViewItem parent = parentParentChildren.get(index);
                        item.setLevel(parent.getLevel() + 1);
//                            parentChildren.add(index + 1, item); //This produces indexOutOfBoundsException
                        parentChildren.add(item);
                        //append(items, item, index + 1);
                        collapsedParents.put(idParent, parentChildren);
                    } else {
                        index = indexOf(items, idParent);
                        if (BuildConfig.DEBUG && index < 0)
                            throw new AssertionError("addItems/ index is - 1");
                        RecyclerViewItem parent = items.get(index);
                        item.setLevel(parent.getLevel() + 1);
                        parentChildren.add(item);
//                        append(parentChildren, item, index + 1);
                        collapsedParents.put(idParent, parentChildren);
                    }
                } else {
                    // The collapsed parent is now expanded and thus this comment's parent
                    // must be removed from collapsedChildren
                    collapsedChildren.remove(idParent);
                    //if (BuildConfig.DEBUG && !itemViewModel.commentsList.contains(new Comment(idParent)))
//                    if(BuildConfig.DEBUG)
//                        throw new AssertionError("addItems/ collapsed parent is now expanded");
                    Log.v(MainActivity.TAG, "parentParentChildren is null and collapsed parent is now expanded");
                    addCommentToList(item);
                }
            } else {
                if (BuildConfig.DEBUG)
                    throw new AssertionError("idCollapsedParent is null");
            }
        } else {
            // Is this item a collapsed parent ?
            List<RecyclerViewItem> children = collapsedParents.get(item.getId());
            if(children != null) {
                // This item is a collapsed parent
                children.clear();
                //This item's parent is not collapsed
                Long idCollapsedParent = collapsedChildren.get(idParent);
                if(idCollapsedParent != null) {
                    //This item's parent is child of another collapsed parent
                    collapsedChildren.put(item.getId(), idCollapsedParent);
                    List<RecyclerViewItem> parentParentChildren = collapsedParents.get(idCollapsedParent);
                    if(parentParentChildren != null) {
                        int index = indexOf(parentParentChildren, idParent);
                        if(index > -1) {
                            RecyclerViewItem parent = parentParentChildren.get(index);
                            item.setLevel(parent.getLevel() + 1);
                            parentParentChildren.add(index + 1, item);
                            //append(parentParentChildren, item, index + 1);
                        } else {
                            if(BuildConfig.DEBUG) {
                                throw new AssertionError("index is -1, shouldn't be because the parent is child of a collapsed parent");
                            }
                        }
                    } else {
                        collapsedChildren.remove(idParent);
                        Log.v(MainActivity.TAG, "parentParentChildren is null");
                        addCommentToList(item);
//                        if(BuildConfig.DEBUG) {
//                            throw new AssertionError("parentParentChildren is null");
//                        }
                    }
                } else {
                    // This item is collapsed (it's a top parent level 0) but it's not a child of no other collapsed parent
                    collapsedChildren.put(item.getId(), item.getId());
                    addCommentToList(item);
                }
            } else {
                // This item is not a child of a collapsed parent
                addCommentToList(item);
            }
        }
    }

    public final void addItemOld(RecyclerViewItem item) {
        // Is this item a collapsed parent ?
        List<RecyclerViewItem> children = collapsedParents.get(item.getId());
        if(children != null) {
            // This item is a collapsed parent
            children.clear();
            long idParent = item.getParent();
            List<RecyclerViewItem> parentChildren = collapsedParents.get(idParent);
            if(parentChildren != null) {
                // This item is collapsed and is also child of another collapsed parent
                Long idCollapsedParent = collapsedChildren.get(idParent);
                if(idCollapsedParent != null) {
                    List<RecyclerViewItem> parentParentChildren = collapsedParents.get(idCollapsedParent);
                    if(parentParentChildren != null) {
                        collapsedChildren.put(item.getId(), idParent); //idCollapsedParent wrong
                        int index = indexOf(parentParentChildren, idParent);
                        if (index > -1) {
                            RecyclerViewItem parent = parentParentChildren.get(index);
                            item.setLevel(parent.getLevel() + 1);
//                            parentChildren.add(index + 1, item); //This produces indexOutOfBoundsException index: 3, size: 0
                            parentChildren.add(item);
                            collapsedParents.put(idParent, parentChildren);
                        } else {
                            index = indexOf(items, idParent);
                            if(BuildConfig.DEBUG && index < 0) {
                                throw new AssertionError("parent in Items has index -1");
                            }
                            RecyclerViewItem parent = items.get(index);
                            item.setLevel(parent.getLevel() + 1);
                            parentChildren.add(item);
                            collapsedParents.put(idParent, parentChildren);
                        }
                    } else {
                        if(BuildConfig.DEBUG) {
                            throw new AssertionError("parentParentChildren is null");
                        }
                    }
                } else {
                    if(BuildConfig.DEBUG) {
                        throw new AssertionError("Long idCollapsedParent is null");
                    }
                }
            } else {
                //This item's parent is not collapsed
                Long idCollapsedParent = collapsedChildren.get(idParent);
                if(idCollapsedParent != null) {
                    //This item's parent is child of another collapsed parent
                    collapsedChildren.put(item.getId(), idCollapsedParent);
                    List<RecyclerViewItem> parentParentChildren = collapsedParents.get(idCollapsedParent);
                    if(parentParentChildren != null) {
                        int index = indexOf(parentParentChildren, idParent);
                        if(index > -1) {
                            RecyclerViewItem parent = parentParentChildren.get(index);
                            item.setLevel(parent.getLevel() + 1);
                            parentParentChildren.add(index + 1, item);
                        } else {
                            if(BuildConfig.DEBUG) {
                                throw new AssertionError("index is -1, shouldn't be because the parent is child of a collapsed parent");
                            }
                        }
                    } else {
                        if(BuildConfig.DEBUG) {
                            throw new AssertionError("parentParentChildren is null");
                        }
                    }
                } else {
                    // This item is collapsed (it's a top parent level 0) but it's not a child of no other collapsed parent
                    collapsedChildren.put(item.getId(), item.getId());
                    addCommentToList(item);
                }
            }
        } else {
            //This item is not a collapsed parent
            long idParent = item.getParent();
            List<RecyclerViewItem> parentChildren = collapsedParents.get(idParent);
            if(parentChildren != null) {
                Long idCollapsedParent = collapsedChildren.get(idParent);
                if(idCollapsedParent != null) {
                    List<RecyclerViewItem> parentParentChildren = collapsedParents.get(idCollapsedParent);
                    if(parentParentChildren != null) {
                        // This item is child of a collapsed parent
                        collapsedChildren.put(item.getId(), idCollapsedParent);
                        int index = indexOf(parentParentChildren, idParent);
                        if(index > -1) {
                            RecyclerViewItem parent = parentParentChildren.get(index);
                            item.setLevel(parent.getLevel() + 1);
//                            parentChildren.add(index + 1, item); //This produces indexOutOfBoundsException
                            parentChildren.add(item);
//                            collapsedParents.put(idCollapsedParent, parentParentChildren);
                            collapsedParents.put(idParent, parentChildren);
                        } else {
                            index = indexOf(items, idParent);
                            if (BuildConfig.DEBUG && index < 0)
                                throw new AssertionError("addItems/ index is - 1");
                            RecyclerViewItem parent = items.get(index);
                            item.setLevel(parent.getLevel() + 1);
                            parentChildren.add(item);
                            collapsedParents.put(idParent, parentChildren);
                        }
                    } else {
                        // The collapsed parent is now expanded and thus this comment's parent
                        // must be removed from collapsedChildren
                        collapsedChildren.remove(idParent);
                        //if (BuildConfig.DEBUG && !itemViewModel.commentsList.contains(new Comment(idParent)))
                        if(BuildConfig.DEBUG)
                            throw new AssertionError("addItems/ collapsed parent is now expanded");
                        addCommentToList(item);
                    }
                } else {
                    if (BuildConfig.DEBUG)
                        throw new AssertionError("idCollapsedParent is null");
                }
            } else {
                // This item is not a child of a collapsed parent
                addCommentToList(item);
            }
        }
    }

    final private void addCommentToList(RecyclerViewItem item) {
        int pos = items.size();
        long idParent = item.getParent();
        int index = indexOf(items, idParent);
        if(index != -1) {
            RecyclerViewItem parent = items.get(index);
            item.setLevel(parent.getLevel() + 1);
        } else {
            item.setLevel(1);
            index = pos;
        }
        int indexToInsert = index + 1;
        if(!items.contains(item) && indexToInsert > -1) {
//            if(indexToInsert > items.size())
//                items.add(item);
//            else
//                items.add(indexToInsert, item);
            append(items, item, indexToInsert);
            notifyItemInserted(indexToInsert);
        }
    }

    private static int indexOf(List<RecyclerViewItem> items, long id) {
        int index = 0;
        while(index < items.size() && items.get(index).getId() != id)
            index++;
        return (index >= items.size()) ? -1 : index;
    }

    private static void append(List<RecyclerViewItem> items, RecyclerViewItem item, int index) {
        int size = items.size();
        int i = (index < 0 || index > size) ? size : index;
        items.add(i, item);
    }

    public static class MultiLevelViewHolder extends RecyclerView.ViewHolder {

        private CollapseItemsListener listener;

        public MultiLevelViewHolder(@NonNull ViewBinding binding, CollapseItemsListener listener) {
            super(binding.getRoot());
            this.listener = listener;
        }
    }

    protected interface CollapseItemsListener {

        void onCollapse(RecyclerViewItem item);

        void onExpand(RecyclerViewItem item);
    }

}
