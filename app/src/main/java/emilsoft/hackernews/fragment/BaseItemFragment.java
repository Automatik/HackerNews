package emilsoft.hackernews.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import emilsoft.hackernews.BuildConfig;
import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.CommentsAdapter;
import emilsoft.hackernews.adapter.MultiLevelCommentsAdapter;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentItemBinding;
import emilsoft.hackernews.expandablerecyclerview.MultiLevelRecyclerView;
import emilsoft.hackernews.viewmodel.ItemViewModel;

public abstract class BaseItemFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String ARG_ITEM = "argument_item";

    private TextView userText, timeText;
    protected TextView titleText, urlText, pointsText, numCommentsText, itemText;
    protected LinearLayout noCommentsLayout;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    protected CommentsAdapter adapter;
//    protected MultiLevelRecyclerView recyclerView;
//    protected MultiLevelCommentsAdapter adapter;
    protected ItemViewModel itemViewModel;
    protected CustomTabActivityHelper.LaunchUrlCallback launchUrlCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        Bundle args = getArguments();
        if(savedInstanceState != null)
            args = savedInstanceState;
        if(args != null) {
            itemViewModel.item = args.getParcelable(ARG_ITEM);
        }

        if(getActivity() instanceof CustomTabActivityHelper.LaunchUrlCallback)
            launchUrlCallback = (CustomTabActivityHelper.LaunchUrlCallback) getActivity();

        if(itemViewModel.item != null)
            preFetchUrl();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentItemBinding binding = FragmentItemBinding.inflate(inflater, container, false);
        swipeRefreshLayout = binding.itemSwipeRefresh;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.redA200);
        recyclerView = binding.itemCommentsList;
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); //TODO vedere se serve
//        recyclerView.removeItemClickListeners();
//        recyclerView.setToggleItemOnClick(false);

        itemText = binding.itemText;
        titleText = binding.itemTitle;
        urlText = binding.itemUrl;
        userText = binding.itemUser;
        timeText = binding.itemTime;
        pointsText = binding.itemPoints;
        numCommentsText = binding.itemNumComments;
        noCommentsLayout = binding.itemNocommentsLayout;

        userText.setText(itemViewModel.item.getUser());
        timeText.setText(Utils.getAbbreviatedTimeSpan(itemViewModel.item.getTime()));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new CommentsAdapter(itemViewModel.commentsList,
                itemViewModel.collapsedParentComments, itemViewModel.collapsedChildren);
//        adapter = new MultiLevelCommentsAdapter(itemViewModel.commentsList);
        recyclerView.setAdapter(adapter);
        observeItem(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_ITEM, itemViewModel.item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        WeakReference<Context> ref = new WeakReference<Context>(getContext());
        if(itemViewModel.item == null)
            return super.onOptionsItemSelected(item);
        String hnUrl = Utils.toHackerNewsUrl(itemViewModel.item.getId());
        switch (id) {
            case R.id.action_item_menu_hackernews_link:
                CustomTabActivityHelper.openWebUrl(ref, hnUrl);
                return true;
            case R.id.action_item_menu_share:
                //TODO Implement share
                return true;
            case R.id.action_articles_refresh:
                swipeRefreshLayout.setRefreshing(true);
                observeItem(true);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    protected abstract void preFetchUrl();

    protected void observeItem(final boolean refreshComments) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - itemViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
            itemViewModel.commentsFound = true;
            showTextNoComments();
            itemViewModel.lastCommentsRefreshTime = currentTime;
            itemViewModel.getItem().observe(getViewLifecycleOwner(), getItemObserver(refreshComments));
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    protected abstract Observer<Item> getItemObserver(final boolean refreshComments);

    protected void startObservingComments(long[] kids) {
        if(kids == null || kids.length == 0)
            return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            observeComments(LongStream.of(kids).boxed().collect(Collectors.toList()));
        else {
            List<Long> ids = new ArrayList<>(kids.length);
            for(long id : kids)
                ids.add(id);
            observeComments(ids);
        }
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    protected void observeComments(List<Long> ids) {
        itemViewModel.getComments(ids).observe(getViewLifecycleOwner(), comments -> {
            List<Long> newKidsIds = new ArrayList<>();
            for(Comment comment : comments) {
                // Is this comment a collapsed parent ?
                List<Comment> children = itemViewModel.collapsedParentComments.get(comment.getId());
                if(children != null) {
                    // This comment is a collapsed parent
                    children.clear();
//                    itemViewModel.collapsedChildren.put(comment.getId(), comment.getId());

                    long idParent = comment.getParent();
                    List<Comment> parentChildren = itemViewModel.collapsedParentComments.get(idParent);
                    if(parentChildren != null) {
                        // This comment is collapsed and is also child of another collapsed parent
                        Long idCollapsedParent = itemViewModel.collapsedChildren.get(idParent);
                        if(idCollapsedParent != null) {
                            List<Comment> parentParentChildren = itemViewModel.collapsedParentComments.get(idCollapsedParent);
                            if(parentParentChildren != null) {
                                itemViewModel.collapsedChildren.put(comment.getId(), idCollapsedParent);
                                Comment parent = new Comment(idParent);
                                int index = parentParentChildren.indexOf(parent);
                                if(index > -1) {
                                    parent = parentParentChildren.get(index);
                                    comment.setLevel(parent.getLevel() + 1);
                                    parentChildren.add(comment);
                                    itemViewModel.collapsedParentComments.put(idParent, parentChildren);
                                } else {
                                    index = itemViewModel.commentsList.indexOf(parent);
                                    if(BuildConfig.DEBUG && index < 0) {
                                        throw new AssertionError("parent in CommentList has index -1");
                                    }
                                    parent = itemViewModel.commentsList.get(index);
                                    comment.setLevel(parent.getLevel() + 1);
                                    parentChildren.add(comment);
                                    itemViewModel.collapsedParentComments.put(idParent, parentChildren);
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
                        // This comment is collapsed (parent) but it's not a child of another collapsed parent
                        itemViewModel.collapsedChildren.put(comment.getId(), comment.getId());
                        addCommentToList(comment);
                    }
                } else {
                    // This comment is not a collapsed parent
                    long idParent = comment.getParent();
                    List<Comment> parentChildren = itemViewModel.collapsedParentComments.get(idParent);
                    if(parentChildren == null) {
                        // This comment is not a child of a collapsed parent
                        addCommentToList(comment);
                    }
                    Long idCollapsedParent = itemViewModel.collapsedChildren.get(idParent);
                    if(idCollapsedParent != null) {
                        List<Comment> parentParentChildren = itemViewModel.collapsedParentComments.get(idCollapsedParent);
                        if(parentParentChildren == null) {
                            // The collapsed parent is now expanded and thus this comment's parent
                            // must be removed from collapsedChildren
                            Log.v(MainActivity.TAG, "observeComments/ collapsed parent is now expanded");
                            itemViewModel.collapsedChildren.remove(idParent);
                            if (BuildConfig.DEBUG && !itemViewModel.commentsList.contains(new Comment(idParent)))
                                throw new AssertionError();
                            addCommentToList(comment);
                        } else {
                            // This comment is child of a collapsed parent
                            itemViewModel.collapsedChildren.put(comment.getId(), idCollapsedParent);
                            Comment parent = new Comment(idParent);
                            int index = parentParentChildren.indexOf(parent);
                            if(index > -1) { //Should never be negative
                                parent = parentParentChildren.get(index);
                                comment.setLevel(parent.getLevel() + 1);
//                                parentChildren.add(index + 1, comment);
                                parentChildren.add(comment);
//                                itemViewModel.collapsedParentComments.put(idCollapsedParent, parentParentChildren);
                                itemViewModel.collapsedParentComments.put(idParent, parentChildren);
                            } else {
                                index = itemViewModel.commentsList.indexOf(parent);
                                if(BuildConfig.DEBUG && index < 0)
                                    throw new AssertionError("observeComments/ index is - 1");
                                parent = itemViewModel.commentsList.get(index);
                                comment.setLevel(parent.getLevel() + 1);
//                                parentParentChildren.add(comment);
                                parentChildren.add(comment);
//                                itemViewModel.collapsedParentComments.put(idCollapsedParent, parentParentChildren);
                                itemViewModel.collapsedParentComments.put(idParent, parentChildren);
//                                Log.v(MainActivity.TAG, "observeComments/ index is -1");

                            }
                        }
                    } else {
                        // This comment is not a child of a collapsed parent
                        addCommentToList(comment);
                    }
                }
                long[] kids = comment.getKids();
                if(kids != null) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        newKidsIds.addAll(LongStream.of(kids).boxed().collect(Collectors.toList()));
                    else {
                        List<Long> temp = new ArrayList<>(kids.length);
                        for(long id : kids)
                            temp.add(id);
                        newKidsIds.addAll(temp);
                    }
                }
            }
            if(newKidsIds.size() > 0)
                observeComments(newKidsIds);
        });
    }

    private void addCommentToList(Comment comment) {
        int pos = itemViewModel.commentsList.size();
        long idParent = comment.getParent();
        int indexToInsert;
        if(idParent == itemViewModel.item.getId()) {
            comment.setLevel(1);
            indexToInsert = pos;
        } else {
            Comment parent = new Comment(idParent);
            int index = itemViewModel.commentsList.indexOf(parent);
            if(index != -1) {
                parent = itemViewModel.commentsList.get(index);
                // just a check but should never be null when this method is called
                comment.setLevel(parent.getLevel() + 1);
                indexToInsert = index + 1; // after the parent
            } else {
                indexToInsert = -1;
                Log.v(MainActivity.TAG, "addCommentToList/ index is -1");
            }
        }
        if(!itemViewModel.commentsList.contains(comment) && indexToInsert > -1) {
            itemViewModel.commentsList.add(indexToInsert, comment);
            if(adapter != null)
                adapter.notifyItemInserted(indexToInsert);
        }
    }

    private void addCommentToListOld(Comment comment) {
        if(!itemViewModel.commentsList.contains(comment)) {
            int pos = itemViewModel.commentsList.size();
            long idParent = comment.getParent();
            if(idParent == itemViewModel.item.getId()) {
                comment.setLevel(1);
                itemViewModel.commentsList.add(comment);
                if(adapter != null)
                    adapter.notifyItemInserted(pos);
            } else {
                Comment parent = new Comment(idParent);
                int index = itemViewModel.commentsList.indexOf(parent);
                parent = itemViewModel.commentsList.get(index);
                comment.setLevel(parent.getLevel() + 1);
                index += 1; //+1 after the parent
                itemViewModel.commentsList.add(index, comment);
                if(adapter != null)
                    adapter.notifyItemInserted(index);
            }
        }
    }

    protected void observeCommentsOld(List<Long> ids) {
        itemViewModel.getComments(ids).observe(getViewLifecycleOwner(), comments -> {
            List<Long> newKidsIds = new ArrayList<>();
            for(Comment comment : comments) {
                if(!itemViewModel.commentsList.contains(comment)) {
                    int pos = itemViewModel.commentsList.size();
                    long idParent = comment.getParent();
                    if(idParent == itemViewModel.item.getId()) {
                        comment.setLevel(1);
                        itemViewModel.commentsList.add(comment);
                        if(adapter != null)
                            adapter.notifyItemInserted(pos);
                    } else {
                        Comment parent = new Comment(idParent);
                        int index = itemViewModel.commentsList.indexOf(parent);
                        parent = itemViewModel.commentsList.get(index);
                        comment.setLevel(parent.getLevel() + 1);
                        parent.addChild(comment);
                        index += 1; //+1 after the parent
//                        itemViewModel.commentsList.add(index, comment);
                        if(adapter != null)
                            adapter.notifyItemInserted(index);
                    }
                }
                long[] kids = comment.getKids();
                if(kids != null) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        newKidsIds.addAll(LongStream.of(kids).boxed().collect(Collectors.toList()));
                    else {
                        List<Long> temp = new ArrayList<>(kids.length);
                        for(long id : kids)
                            temp.add(id);
                        newKidsIds.addAll(temp);
                    }
                }
            }
            if(newKidsIds.size() > 0)
                observeComments(newKidsIds);
        });
    }

    protected void showTextNoComments() {
        if(!itemViewModel.commentsFound)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else
            noCommentsLayout.setVisibility(View.INVISIBLE);
    }


}
