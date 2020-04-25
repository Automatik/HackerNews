package emilsoft.hackernews.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import emilsoft.hackernews.adapter.StoriesAdapter;
import emilsoft.hackernews.viewmodel.HomeViewModel;
import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Story;

import static emilsoft.hackernews.MainActivity.TAG;

public class HomeFragment extends Fragment {

    private static final int NUM_LOAD_ITEMS = 20;

    private RecyclerView recyclerView;
    private StoriesAdapter adapter;
    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.articles_list);
        recyclerView.addOnScrollListener(onScrollListener);
        //final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(homeViewModel.topStories != null) {
            adapter = new StoriesAdapter(homeViewModel.topStories);
            recyclerView.setAdapter(adapter);
        }
        homeViewModel.getTopStoriesIds().observe(this, new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> ids) {
                Log.v(TAG, "Top Stories on Main Thread");
                homeViewModel.topStoriesIds.clear();
                homeViewModel.topStoriesIds.addAll(ids);
                homeViewModel.lastItemLoadedIndex = 0;
                for(int i = 0; i < NUM_LOAD_ITEMS; i++)
                    observeStory(ids.get(i));
                homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
                Log.v(TAG, "Finished calling first batch");
            }
        });
//        getTopStoriesIds();
    }

    private void observeStory(long id) {
        homeViewModel.getStory(id).observe(this, new Observer<Story>() {
            @Override
            public void onChanged(Story story) {
                int pos = homeViewModel.topStories.size();
                homeViewModel.topStories.add(story);
                if(adapter != null)
                    adapter.notifyItemInserted(pos);
            }
        });
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                Log.v(TAG, "Loading more items");
                //The RecyclerView is not currently scrolling.
                //TODO Add an if condition for checking if the first load has finished
                int startIndex = homeViewModel.lastItemLoadedIndex + 1;
                int i = startIndex;

                //First condition to check if the user finished to load the 500 top stories ids
                while(i < homeViewModel.topStoriesIds.size() && i < startIndex + NUM_LOAD_ITEMS) {
                    observeStory(homeViewModel.topStoriesIds.get(i));
                    i++;
                }
//                for(int i = startIndex; i < startIndex + NUM_LOAD_ITEMS; i++)
//                    observeStory(homeViewModel.topStoriesIds.get(i));
                homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
}