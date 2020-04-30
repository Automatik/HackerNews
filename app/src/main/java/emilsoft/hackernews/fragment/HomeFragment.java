package emilsoft.hackernews.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.StoriesAdapter;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.databinding.FragmentHomeBinding;
import emilsoft.hackernews.viewmodel.HomeViewModel;
import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Story;

import static emilsoft.hackernews.MainActivity.TAG;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int NUM_LOAD_ITEMS = 20;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private StoriesAdapter adapter;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
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
        if(homeViewModel.topStories != null) {
            adapter = new StoriesAdapter(homeViewModel.topStories);
            recyclerView.setAdapter(adapter);
        }
        refreshArticles();
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
            int numIds = homeViewModel.topStoriesIds.size();
            if(newState == RecyclerView.SCROLL_STATE_IDLE && scrollY > 0 && numIds > 0) {
                //The RecyclerView is not currently scrolling.
                int startIndex = homeViewModel.lastItemLoadedIndex + 1;
                int i = startIndex;

                //First condition to check if the user finished to load the 500 top stories ids
                while (i < homeViewModel.topStoriesIds.size() && i < startIndex + NUM_LOAD_ITEMS) {
                    observeStory(homeViewModel.topStoriesIds.get(i));
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
            homeViewModel.getTopStoriesIds().observe(this, new Observer<List<Long>>() {
                @Override
                public void onChanged(List<Long> ids) {
                    homeViewModel.topStoriesIds.clear();
                    int size = homeViewModel.topStories.size();
                    homeViewModel.topStories.clear();
                    if (adapter != null)
                        adapter.notifyItemRangeRemoved(0, size);
                    homeViewModel.topStoriesIds.addAll(ids);
                    homeViewModel.lastItemLoadedIndex = 0;
                    for (int i = 0; i < NUM_LOAD_ITEMS; i++) {
                        observeStory(ids.get(i));
                    }
                    homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void observeStory(long id) {
        homeViewModel.getStory(id).observe(this, new Observer<Story>() {
            @Override
            public void onChanged(Story story) {
                if(!homeViewModel.topStories.contains(story)) {
                    int pos = homeViewModel.topStories.size();
                    homeViewModel.topStories.add(story);
                    if (adapter != null)
                        adapter.notifyItemInserted(pos);
                }
            }
        });
    }
}