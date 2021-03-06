package emilsoft.hackernews.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.StoriesAdapter;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.ItemResponse;
import emilsoft.hackernews.api.RetrofitException;
import emilsoft.hackernews.api.Type;
import emilsoft.hackernews.connectivity.ConnectionSnackbar;
import emilsoft.hackernews.connectivity.ConnectivityProvider;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentHomeBinding;
import emilsoft.hackernews.viewmodel.HomeViewModel;
import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Story;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        ConnectivityProvider.ConnectivityStateListener {

    public static final int TOP_STORIES_VIEW = 0;
    public static final int BEST_STORIES_VIEW = 1;
    public static final int NEW_STORIES_VIEW = 2;
    public static final int ASK_STORIES_VIEW = 3;
    public static final int SHOW_STORIES_VIEW = 4;
    public static final int JOB_STORIES_VIEW = 5;

    public static final String ARG_VIEW_STORIES = "arg_view_stories";

    private static final int NUM_LOAD_ITEMS = 20;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ConstraintLayout offlineViewLayout;
    private StoriesAdapter adapter;
    private HomeViewModel homeViewModel;
    private CustomTabActivityHelper.LaunchUrlCallback launchUrlCallback;
    private ConnectivityProvider connectivityProvider;
    private boolean isConnected = false;

    public static HomeFragment newInstance(int argViewStories) {
        Bundle args = new Bundle();
        args.putInt(ARG_VIEW_STORIES, argViewStories);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        if(getContext() != null) {
            connectivityProvider = ConnectivityProvider.createProvider(getContext());
            isConnected = ConnectivityProvider.isStateConnected(connectivityProvider.getNetworkState());
        }

        Bundle args = getArguments();
        if(savedInstanceState != null)
            args = savedInstanceState;
        if(args != null) {
            homeViewModel.argViewItems = args.getInt(ARG_VIEW_STORIES);
            homeViewModel.lastIdsRefreshTime = 0;
        }
        if(getActivity() instanceof CustomTabActivityHelper.LaunchUrlCallback)
            launchUrlCallback = (CustomTabActivityHelper.LaunchUrlCallback) getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        swipeRefreshLayout = binding.articlesSwipeRefresh;
        recyclerView = binding.articlesList;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.redA200);
        recyclerView.addOnScrollListener(onScrollListener);
        offlineViewLayout = binding.articlesOfflineViewLayout.offlineViewLayout;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(homeViewModel.items != null) {
            adapter = new StoriesAdapter(homeViewModel.items, homeViewModel.argViewItems);
            recyclerView.setAdapter(adapter);
        }
        refreshArticles();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_VIEW_STORIES, homeViewModel.argViewItems);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_articles_refresh:
                swipeRefreshLayout.setRefreshing(true);
                refreshArticles();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        int scrollY = 0;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            int numIds = homeViewModel.itemsIds.size();
            if(newState == RecyclerView.SCROLL_STATE_IDLE && scrollY > 0 && numIds > 0) {
                //The RecyclerView is not currently scrolling.
                int startIndex = homeViewModel.lastItemLoadedIndex + 1;
//                int i = startIndex;

                //First condition to check if the user finished to load the 500 top stories ids
//                while (i < homeViewModel.itemsIds.size() && i < startIndex + NUM_LOAD_ITEMS) {
//                    observeItem(homeViewModel.itemsIds.get(i));
//                    i++;
//                }
                int endIndex = Math.min(startIndex + NUM_LOAD_ITEMS, homeViewModel.itemsIds.size());
                observeItems(homeViewModel.itemsIds.subList(startIndex, endIndex));

//                homeViewModel.lastItemLoadedIndex += i - startIndex;
                homeViewModel.lastItemLoadedIndex = endIndex - 1;
                preFetchUrls(startIndex, homeViewModel.lastItemLoadedIndex);
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollY = dy;
        }
    };

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
    public void onRefresh() {
        refreshArticles();
    }

    private void refreshArticles() {
        long currentTime = System.currentTimeMillis();
        if(isConnected && currentTime - homeViewModel.lastIdsRefreshTime > Utils.CACHE_EXPIRATION) {
            homeViewModel.lastIdsRefreshTime = currentTime;
            homeViewModel.getItemsIds().observe(getViewLifecycleOwner(), new Observer<ItemResponse<List<Long>>>() {
                @Override
                public void onChanged(ItemResponse<List<Long>> response) {
                    if(response.isSuccess()) {
                        List<Long> ids = response.getData();
                        homeViewModel.itemsIds.clear();
                        int size = homeViewModel.items.size();
                        homeViewModel.items.clear();
                        if (adapter != null)
                            adapter.notifyItemRangeRemoved(0, size);
                        homeViewModel.itemsIds.addAll(ids);
                        homeViewModel.lastItemLoadedIndex = 0;
                        //homeViewModel.start1 = System.nanoTime();
                        //for (int i = 0; i < NUM_LOAD_ITEMS; i++) {
                        //    observeItem(ids.get(i));
                        //}
                        observeItems(ids.subList(0, NUM_LOAD_ITEMS));
                        homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
                        preFetchUrls(0, homeViewModel.lastItemLoadedIndex);
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        String message = Utils.getMessageErrorFromRetrofitException(response.getError());
                        ConnectionSnackbar.showErrorMessageSnackbar(getView(), message);
                    }
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void observeItem(long id) {
        homeViewModel.getItem(id).observe(this, (response) -> {
            if(response.isSuccess()) {
                Item item = response.getData();
                if(!homeViewModel.items.contains(item)) {
                    int pos = homeViewModel.items.size();
                    homeViewModel.items.add(item);
                    if (adapter != null)
                        adapter.notifyItemInserted(pos);
                }
                //homeViewModel.stop1 = System.nanoTime();
                //Log.v(MainActivity.TAG, "getStory: " + (((homeViewModel.stop1-homeViewModel.start1)/(double)1000000))+ " ms");
            } else {
                String message = Utils.getMessageErrorFromRetrofitException(response.getError());
                ConnectionSnackbar.showErrorMessageSnackbar(getView(), message);
            }
        });
    }

    private void observeItems(List<Long> ids) {
        if(isConnected) {
            homeViewModel.getItems(ids).observe(this, (response -> {
                if(response.isSuccess()) {
                    List<? extends Item> items = response.getData();
                    for(Item item : items) {
                        if(!homeViewModel.items.contains(item)) {
                            int pos = homeViewModel.items.size();
                            homeViewModel.items.add(item);
                            if(adapter != null)
                                adapter.notifyItemInserted(pos);
                        }
                    }
                } else {
                    String message = Utils.getMessageErrorFromRetrofitException(response.getError());
                    ConnectionSnackbar.showErrorMessageSnackbar(getView(), message);
                }
            }));
        }
    }

    /**
     * @param endIndex inclusive
     */
    private void preFetchUrls(int startIndex, int endIndex) {
        if(endIndex <= startIndex || startIndex >= homeViewModel.items.size() || endIndex >= homeViewModel.items.size())
            return;
        List<Uri> uris = new ArrayList<>(endIndex - startIndex + 1);
        for(int i = startIndex; i <= endIndex; i++) {
            Item item = homeViewModel.items.get(i);
            if(item.getType() == Type.STORY_TYPE && item instanceof Story) {
                Story story = (Story) item;
                uris.add(Uri.parse(story.getUrl()));
            }
        }
        if(launchUrlCallback != null)
            launchUrlCallback.onMayLaunchUrl(null, Utils.toCustomTabUriBundle(uris));
    }

    public static void navigateToStory(NavController navController, int currentArgViewStory, Bundle args) {
        int navId = 0;
        switch (currentArgViewStory) {
            case TOP_STORIES_VIEW: navId = R.id.action_nav_topstories_to_nav_story; break;
            case BEST_STORIES_VIEW: navId = R.id.action_nav_beststories_to_nav_story; break;
            case NEW_STORIES_VIEW: navId = R.id.action_nav_newstories_to_nav_story; break;
            case SHOW_STORIES_VIEW: navId = R.id.action_nav_showstories_to_nav_story; break;
            default: return;
        }
        navController.navigate(navId, args);
    }

    public static void navigateToAsk(NavController navController, int currentArgViewStory, Bundle args) {
        int navId = 0;
        switch (currentArgViewStory) {
            case TOP_STORIES_VIEW: navId = R.id.action_nav_topstories_to_nav_ask; break;
            case BEST_STORIES_VIEW: navId = R.id.action_nav_beststories_to_nav_ask; break;
            case NEW_STORIES_VIEW: navId = R.id.action_nav_newstories_to_nav_ask; break;
            case ASK_STORIES_VIEW: navId = R.id.action_nav_askstories_to_nav_ask; break;
            default: return;
        }
        navController.navigate(navId, args);
    }

    public static void navigateToJob(NavController navController, int currentArgViewStory, Bundle args) {
        int navId = 0;
        switch (currentArgViewStory) {
            case TOP_STORIES_VIEW: navId = R.id.action_nav_topstories_to_nav_job; break;
            case BEST_STORIES_VIEW: navId = R.id.action_nav_beststories_to_nav_job; break;
            case NEW_STORIES_VIEW: navId = R.id.action_nav_newstories_to_nav_job; break;
            case JOB_STORIES_VIEW: navId = R.id.action_nav_jobstories_to_nav_job; break;
            default: return;
        }
        navController.navigate(navId, args);
    }

    @Override
    public void onStateChange(ConnectivityProvider.NetworkState state) {
        if(connectivityProvider != null) {
            isConnected = ConnectivityProvider.isStateConnected(connectivityProvider.getNetworkState());
            if (homeViewModel.items.isEmpty() && !isConnected) {
                offlineViewLayout.setVisibility(View.VISIBLE);
            }
            if (!homeViewModel.items.isEmpty() && !isConnected) {
                ConnectionSnackbar.showConnectionLostSnackbar(getView());
            }
            if (isConnected)
                offlineViewLayout.setVisibility(View.INVISIBLE);
            if (homeViewModel.items.isEmpty() && isConnected)
                refreshArticles();
        }
    }
}