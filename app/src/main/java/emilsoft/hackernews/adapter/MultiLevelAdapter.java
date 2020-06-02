package emilsoft.hackernews.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
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
    public final void addItem3(T item) {
        Single.create((SingleOnSubscribe<Optional<T>>) emitter -> {
            long idParent = item.getParent();
            int itemIndex = items.indexOf(item);

//                final RecyclerViewItem parent = new T(idParent);
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
                        if(visibleComment.hasChildren() && (parentIndex = indexOf(visibleComment.getChildren(), index)) != -1)
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

//    @SuppressLint("CheckResult")
//    public final void addItem2(RecyclerViewItem item) {
//        Log.v(MainActivity.TAG, "Thread: " + Thread.currentThread().getName());
//        long idParent = item.getParent();
//        int itemIndex = items.indexOf(item);
//        final RecyclerViewItem parent = new Comment(idParent);
//        int index = items.indexOf(parent);
//        if(itemIndex != -1) {
//            //The comment is visible and items contains item
//            update(items, item, itemIndex);
//            if(index != -1) {
//                RecyclerViewItem parentInstance = items.get(index);
//                item.setParentInstance(parentInstance);
//                addCommentToList(item);
//            } else {
//                //it's a top-comment
//                addCommentToList(item);
//            }
//        } else {
//            //This is a new fresh comment or a collapsed comment
//            if(index != -1) {
//                RecyclerViewItem parentInstance = items.get(index);
//                item.setParentInstance(parentInstance);
//                if(parentInstance.isCollapsed()) {
//                    // the comment is collapsed but the parent is not
//                    if (!parentInstance.hasChildren()) {
//                        parentInstance.setChildren(new ArrayList<>());
//                    }
//                    int i = parentInstance.getChildren().indexOf(item);
//                    if(i != -1) {
//                        update(parentInstance.getChildren(), item, i);
//                        parentInstance.getChildren().set(i, item);
//                    } else {
//                        item.setLevel(parentInstance.getLevel() + 1);
//                        parentInstance.getChildren().add(item);
//                    }
//                } else {
//                    // the comment is not collapsed and it's child of another comment
//                    addCommentToList(item);
//                }
//            } else {
//                // it's a fresh new comment or the parent is also collapsed
////                        RecyclerViewItem visibleComment = items.stream()
////                                .filter((visComment -> visComment.hasChildren() && visComment.getChildren().contains(parent)))
////                                .findFirst()
////                                .orElse(null);
//
//                int parentIndex = -1;
//                int visibleCommentIndex = 0;
//                boolean parentFound = false;
//                while(visibleCommentIndex < items.size() && !parentFound) {
//                    RecyclerViewItem visibleComment = items.get(visibleCommentIndex);
//                    if(visibleComment.hasChildren() && (parentIndex = visibleComment.getChildren().indexOf(parent)) != -1)
//                        parentFound = true;
//                    visibleCommentIndex++;
//                }
//                if(parentFound) {
//                    RecyclerViewItem visibleComment = items.get(visibleCommentIndex - 1);
//                    RecyclerViewItem parentInstance = visibleComment.getChildren().get(parentIndex);
//                    item.setParentInstance(parentInstance);
//                    if(parentInstance.isCollapsed()) {
//                        // parent has collapsed children
//                        if(!parentInstance.hasChildren()) {
//                            parentInstance.setChildren(new ArrayList<>());
//                        }
//                        int i = parentInstance.getChildren().indexOf(item);
//                        if(i != -1) {
//                            update(parentInstance.getChildren(), item, i);
//                            parentInstance.getChildren().set(i, item);
//                        } else {
//                            item.setLevel(parentInstance.getLevel() + 1);
//                            parentInstance.getChildren().add(item);
//                        }
//                    } else {
//                        // parent has not collapsed children
//                        // this else can be removed
//                        Log.v(MainActivity.TAG, "parent has not collapsed children but one of the parent's parent is collapsed");
//                    }
//                    // maintain coherency with onCollapse
//                    int i = visibleComment.getChildren().indexOf(item);
//                    if(i != -1) {
//                        update(visibleComment.getChildren(), item, i);
//                        visibleComment.getChildren().set(i, item);
//                    } else {
//                        item.setLevel(parentInstance.getLevel() + 1);
//                        visibleComment.getChildren().add(parentIndex + 1, item);
//                    }
//                } else {
//                    // it's a fresh new top comment (doesn't have a parent and is not contained in items)
//                    addCommentToList(item); //potrebbe evitare di fare indexOf nel metodo
//                }
//            }
//        }
//    }

    private static <T extends RecyclerViewItem<T>> void update(List<T> items, T item) {
        int index = items.indexOf(item);
        if(index == -1) return;
        update(items, item, index);
    }

    private static <T extends RecyclerViewItem<T>> void update(List<T> items, T item, int index) {
        T oldItem = items.get(index);
        item.setLevel(oldItem.getLevel());
        item.setIsCollapsed(oldItem.isCollapsed());
        item.setChildren(oldItem.getChildren());
    }

    private void addCommentToList(T item) {
        int pos = items.size();
        long idParent = item.getParent();
        int index = indexOf(items, idParent);
        if(index != -1) {
            T parent = items.get(index);
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
