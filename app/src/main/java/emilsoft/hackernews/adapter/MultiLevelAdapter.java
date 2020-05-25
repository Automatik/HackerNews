package emilsoft.hackernews.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import emilsoft.hackernews.BuildConfig;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.MultiLevelData;
import emilsoft.hackernews.api.RecyclerViewItem;

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
        boolean isItemCollapsed = collapsedParents.containsKey(item.getId());
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

        @Override
        public void onCollapse(RecyclerViewItem item) {
            final int index = items.indexOf(item);
            final int level = item.getLevel();
            if(index == -1) return;
            int i = index + 1;
//            final List<Comment> childs = new LinkedList<>();
//            Observable.fromIterable(commentsList.subList(i, commentsList.size()))
//                    .takeWhile(c -> c.getLevel() > level)
//                    .toList()
//                    .subscribe(comments -> {
//                        childs.addAll(comments);
//                        commentsList.subList(index + 1, index + 1 + childs.size()).clear();
//                        notifyItemRangeRemoved(index + 1, childs.size());
//                        collapsedComments.put(comment.getId(), childs);
//                    })
//                    .dispose();
            //Without using RxJava

            List<RecyclerViewItem> children = new ArrayList<>();
            RecyclerViewItem temp;
            while (i < items.size() && (temp = items.get(i)).getLevel() > level) {
                children.add(temp);
                i++;
            }
            items.subList(index + 1, index + 1 + children.size()).clear();
            notifyItemRangeRemoved(index + 1, children.size());
            collapsedParents.put(item.getId(), children);
//            collapsedChildren.put(item.getId(), item.getId());
        }

        @Override
        public void onExpand(RecyclerViewItem item) {
            long id = item.getId();
            int index = items.indexOf(item);
            if(index == -1) return;
            List<RecyclerViewItem> children = collapsedParents.get(id);
            if(children == null) return;
            items.addAll(index + 1, children);
            notifyItemRangeInserted(index + 1, children.size());
            collapsedParents.remove(id);
            collapsedChildren.remove(id);
        }
    };

    public final void addItem2(RecyclerViewItem item) {
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
            } else {
                // This item is not a child of a collapsed parent
                addCommentToList(item);
            }
        }
    }

    public final void addItem(RecyclerViewItem item) {
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
            if(indexToInsert > items.size())
                items.add(item);
            else
                items.add(indexToInsert, item);
            notifyItemInserted(indexToInsert);
        }
    }

    final private static int indexOf(List<RecyclerViewItem> items, long id) {
        int index = 0;
        while(index < items.size() && items.get(index).getId() != id)
            index++;
        return (index >= items.size()) ? -1 : index;
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
