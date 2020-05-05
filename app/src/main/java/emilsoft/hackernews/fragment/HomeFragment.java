package emilsoft.hackernews.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.StoriesAdapter;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.databinding.FragmentHomeBinding;
import emilsoft.hackernews.viewmodel.HomeViewModel;
import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Story;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

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
    private StoriesAdapter adapter;
    private HomeViewModel homeViewModel;

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
        //homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        Bundle args = getArguments();
        if(savedInstanceState != null)
            args = savedInstanceState;
        if(args != null) {
            homeViewModel.argViewStories = args.getInt(ARG_VIEW_STORIES);
            homeViewModel.lastIdsRefreshTime = 0;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        swipeRefreshLayout = binding.articlesSwipeRefresh;
        recyclerView = binding.articlesList;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.redA200);
        recyclerView.addOnScrollListener(onScrollListener);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(homeViewModel.stories != null) {
            adapter = new StoriesAdapter(homeViewModel.stories, homeViewModel.argViewStories);
            recyclerView.setAdapter(adapter);
        }
        refreshArticles();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_VIEW_STORIES, homeViewModel.argViewStories);
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
            int numIds = homeViewModel.storiesIds.size();
            if(newState == RecyclerView.SCROLL_STATE_IDLE && scrollY > 0 && numIds > 0) {
                //The RecyclerView is not currently scrolling.
                int startIndex = homeViewModel.lastItemLoadedIndex + 1;
                int i = startIndex;

                //First condition to check if the user finished to load the 500 top stories ids
                while (i < homeViewModel.storiesIds.size() && i < startIndex + NUM_LOAD_ITEMS) {
                    observeItem(homeViewModel.storiesIds.get(i));
                    i++;
                }
//                homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
                homeViewModel.lastItemLoadedIndex += i - startIndex;

            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scrollY = dy;
        }
    };

    @Override
    public void onRefresh() {
        refreshArticles();
    }

    private void refreshArticles() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - homeViewModel.lastIdsRefreshTime > Utils.CACHE_EXPIRATION) {
            homeViewModel.lastIdsRefreshTime = currentTime;
            homeViewModel.getStoriesIds().observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
                @Override
                public void onChanged(List<Long> ids) {
                    homeViewModel.storiesIds.clear();
                    int size = homeViewModel.stories.size();
                    homeViewModel.stories.clear();
                    if (adapter != null)
                        adapter.notifyItemRangeRemoved(0, size);
                    homeViewModel.storiesIds.addAll(ids);
                    homeViewModel.lastItemLoadedIndex = 0;
                    for (int i = 0; i < NUM_LOAD_ITEMS; i++) {
                        observeItem(ids.get(i));
                    }
                    homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void observeItem(long id) {
        homeViewModel.getItem(id).observe(this, (item) -> {
                if(!homeViewModel.stories.contains(item)) {
                    int pos = homeViewModel.stories.size();
                    homeViewModel.stories.add(item);
                    if (adapter != null)
                        adapter.notifyItemInserted(pos);
                }
        });
    }

    public static void navigateToStory(NavController navController, int currentArgViewStory, Bundle args) {
        if(currentArgViewStory < TOP_STORIES_VIEW || currentArgViewStory > JOB_STORIES_VIEW)
            return;
        if(currentArgViewStory == ASK_STORIES_VIEW || currentArgViewStory == JOB_STORIES_VIEW)
            //Shouldn't open StoryFragment but only AskJobFragment
            return;
        int navId = 0;
        switch (currentArgViewStory) {
            case TOP_STORIES_VIEW: navId = R.id.action_nav_topstories_to_nav_story; break;
            case BEST_STORIES_VIEW: navId = R.id.action_nav_beststories_to_nav_story; break;
            case NEW_STORIES_VIEW: navId = R.id.action_nav_newstories_to_nav_story; break;
            case SHOW_STORIES_VIEW: navId = R.id.action_nav_showstories_to_nav_story; break;
        }
        navController.navigate(navId, args);
    }

    public static void navigateToAskJob(NavController navController, int currentArgViewStory, Bundle args) {
        if(currentArgViewStory < TOP_STORIES_VIEW || currentArgViewStory > JOB_STORIES_VIEW)
            return;
        if(currentArgViewStory == SHOW_STORIES_VIEW)
            //Shouldn't open AskJobFragment
            return;
        int navId = 0;
        switch (currentArgViewStory) {
            case TOP_STORIES_VIEW: navId = R.id.action_nav_topstories_to_nav_ask; break;
            case BEST_STORIES_VIEW: navId = R.id.action_nav_beststories_to_nav_ask; break;
            case NEW_STORIES_VIEW: navId = R.id.action_nav_newstories_to_nav_ask; break;
            case ASK_STORIES_VIEW: navId = R.id.action_nav_askstories_to_nav_ask; break;
            case JOB_STORIES_VIEW: navId = R.id.action_nav_jobstories_to_nav_ask; break;
        }
        navController.navigate(navId, args);
    }
}