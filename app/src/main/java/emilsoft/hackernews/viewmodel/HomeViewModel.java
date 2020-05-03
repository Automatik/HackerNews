package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.fragment.HomeFragment;
import emilsoft.hackernews.repository.HackerNewsRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    public List<Long> storiesIds;
    public List<Item> stories;
    private HackerNewsRepository repository;
    public int argViewStories;
    public int lastItemLoadedIndex;
    public long lastIdsRefreshTime = 0L;

    public HomeViewModel() {
        repository = HackerNewsRepository.getInstance();
        storiesIds = new ArrayList<>();
        stories = new ArrayList<>();
    }

    public LiveData<List<Long>> getStoriesIds() {
        switch (argViewStories) {
            case HomeFragment.BEST_STORIES_VIEW:
                return repository.getBestStoriesIds();
            case HomeFragment.NEW_STORIES_VIEW:
                return repository.getNewStoriesIds();
            case HomeFragment.ASK_STORIES_VIEW:
                return repository.getAskStoriesIds();
            case HomeFragment.SHOW_STORIES_VIEW:
                return repository.getShowStoriesIds();
            case HomeFragment.JOB_STORIES_VIEW:
                return repository.getJobStoriesIds();
            case HomeFragment.TOP_STORIES_VIEW:
            default: return repository.getTopStoriesIds();
        }
    }

    public LiveData<? extends Item> getItem(long id) {
        if(argViewStories == HomeFragment.JOB_STORIES_VIEW)
            return getJob(id);
        else
            return getStory(id);
    }

    private LiveData<Story> getStory(long id) {
        return repository.getStory(id);
    }

    private LiveData<Job> getJob(long id) {
        return repository.getJob(id);
    }

}