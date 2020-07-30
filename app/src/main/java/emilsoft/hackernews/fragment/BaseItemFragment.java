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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

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
import emilsoft.hackernews.adapter.MultiCommentsAdapter;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.connectivity.ConnectionSnackbar;
import emilsoft.hackernews.connectivity.ConnectivityProvider;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentItemBinding;
import emilsoft.hackernews.viewmodel.ItemViewModel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public abstract class BaseItemFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        ConnectivityProvider.ConnectivityStateListener {

    public static final String ARG_ITEM = "argument_item";

    private TextView userText, timeText;
    protected TextView titleText, urlText, pointsText, numCommentsText, itemText;
    protected LinearLayout noCommentsLayout;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected RecyclerView recyclerView;
    private ConstraintLayout offlineViewLayout;
//    protected CommentsAdapter adapter;
    protected MultiCommentsAdapter adapter;
    protected ItemViewModel itemViewModel;
    protected CustomTabActivityHelper.LaunchUrlCallback launchUrlCallback;
    private ConnectivityProvider connectivityProvider;
    private boolean isConnected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        if(getContext() != null) {
            connectivityProvider = ConnectivityProvider.createProvider(getContext());
            isConnected = ConnectivityProvider.isStateConnected(connectivityProvider.getNetworkState());
        }

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
        offlineViewLayout = binding.itemOfflineViewLayout.offlineViewLayout;

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
//        adapter = new CommentsAdapter(itemViewModel.commentsList,
//                itemViewModel.collapsedParentComments, itemViewModel.collapsedChildren);
        adapter = new MultiCommentsAdapter(itemViewModel.commentsList);
//        adapter = new MultiCommentsAdapter(itemViewModel.multiLevelData);
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

    @Override
    public void onResume() {
        super.onResume();
        if(connectivityProvider != null)
            connectivityProvider.addListener(this);
    }

    @Override
    public void onPause() {
        if(connectivityProvider != null)
            connectivityProvider.removeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adapter.dispose();
        super.onDestroy();
    }

    protected abstract void preFetchUrl();

    protected void observeItem(final boolean refreshComments) {
        long currentTime = System.currentTimeMillis();
        if(isConnected && currentTime - itemViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
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
        observeComments(LongStream.of(kids).boxed().collect(Collectors.toList()));
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    protected void observeComments(List<Long> ids) {
        if(isConnected) {
            itemViewModel.getComments(ids).observe(getViewLifecycleOwner(), comments -> {
                List<Long> newKidsIds = new ArrayList<>();
                for (Comment comment : comments) {

                    // this null check should prevent the following error when rotating the phone
                    // while downloading comments
                    // Attempt to invoke virtual method 'long[] emilsoft.hackernews.api.Comment.getKids()' on a null object reference
                    if (comment == null)
                        return;

                    if (adapter != null)
                        adapter.addItem(comment);
                    long[] kids = comment.getKids();
                    if (kids != null) {
                        newKidsIds.addAll(LongStream.of(kids).boxed().collect(Collectors.toList()));
                    }
                }
                if (newKidsIds.size() > 0)
                    observeComments(newKidsIds);
            });
        }
    }

    protected void showTextNoComments() {
        if(!itemViewModel.commentsFound)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else
            noCommentsLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStateChange(ConnectivityProvider.NetworkState state) {
        if(connectivityProvider != null) {
            isConnected = ConnectivityProvider.isStateConnected(connectivityProvider.getNetworkState());
            if(itemViewModel.commentsList.isEmpty() && !isConnected)
                offlineViewLayout.setVisibility(View.VISIBLE);
            if(!itemViewModel.commentsList.isEmpty() && !isConnected)
                ConnectionSnackbar.showConnectionLostSnackbar(getView());
            if(isConnected)
                offlineViewLayout.setVisibility(View.INVISIBLE);
            if(itemViewModel.commentsList.isEmpty() && isConnected)
                getItemObserver(true);
        }
    }
}
