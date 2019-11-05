package emilsoft.hackernews.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.MainViewModel;
import emilsoft.hackernews.R;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.api.Story;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static emilsoft.hackernews.MainActivity.TAG;

public class HomeFragment extends Fragment {

    private static final int NUM_LOAD_ITEMS = 20;

    private RecyclerView recyclerView;
    private StoriesAdapter adapter;
    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private HackerNewsApi hackerNewsApi;
    //private final HackerNewsApi hackerNewsApi = RetrofitHelper.create(HackerNewsApi.class);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
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
        hackerNewsApi = mainViewModel.hackerNewsApi;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(homeViewModel.topStories != null) {
            adapter = new StoriesAdapter(homeViewModel.topStories);
            recyclerView.setAdapter(adapter);
        }
        getTopStoriesIds();
    }

    public void getTopStoriesIds() {
        Call<List<Long>> call = hackerNewsApi.getTopStories();
        call.enqueue(topStoriesIdsCallback);
    }

    public void getTopStory(long id) {
        Call<Story> call = hackerNewsApi.getStory(id);
        call.enqueue(topStoriesCallback);
    }

    private Callback<List<Long>> topStoriesIdsCallback = new Callback<List<Long>>() {

        @Override
        @EverythingIsNonNull
        public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
            if(!response.isSuccessful()) {
                Log.v(TAG, "Code: "+response.code());
                return;
            }
            List<Long> ids = response.body();
            if(ids == null) {
                Log.v(TAG, "List of Top Stories ids is null");
                return;
            }
            //Log.v(TAG, "IDS's size: " + ids.size() + ", "+ ids.toString());
            homeViewModel.topStoriesIds.clear();
            homeViewModel.topStoriesIds.addAll(ids);
            homeViewModel.lastItemLoadedIndex = 0;

            for(int i = 0; i < NUM_LOAD_ITEMS; i++) {
                getTopStory(ids.get(i));
            }
            homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
            Log.v(TAG, "Finished loading first batch");
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<List<Long>> call, Throwable t) {
            Log.v(TAG, "onFailure: "+t.getMessage());
        }
    };

    private Callback<Story> topStoriesCallback = new Callback<Story>() {

        @Override
        @EverythingIsNonNull
        public void onResponse(Call<Story> call, Response<Story> response) {
            if(!response.isSuccessful()) {
                Log.v(TAG, "Code: "+response.code());
                return;
            }
            int pos = homeViewModel.topStories.size();
            homeViewModel.topStories.add(response.body());
            if(adapter != null)
                adapter.notifyItemInserted(pos);
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<Story> call, Throwable t) {
            Log.v(TAG, "onFailure: "+t.getMessage());
        }
    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                Log.v(TAG, "Loading more items");
                //The RecyclerView is not currently scrolling.
                //TODO Add an if condition for checking if the first load has finished
                int startIndex = homeViewModel.lastItemLoadedIndex + 1;
                for(int i = startIndex; i < startIndex + NUM_LOAD_ITEMS; i++)
                    getTopStory(homeViewModel.topStoriesIds.get(i));
                homeViewModel.lastItemLoadedIndex += NUM_LOAD_ITEMS - 1;
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
}