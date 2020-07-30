package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.ItemResponse;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.fragment.HomeFragment;
import emilsoft.hackernews.repository.HackerNewsRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    public List<Long> itemsIds;
    public List<Item> items;
    private HackerNewsRepository repository;
    public int argViewItems;
    public int lastItemLoadedIndex;
    public long lastIdsRefreshTime = 0L;

    public HomeViewModel() {
        repository = HackerNewsRepository.getInstance();
        itemsIds = new ArrayList<>();
        items = new ArrayList<>();
    }

    public LiveData<ItemResponse<List<Long>>> getItemsIds() {
        switch (argViewItems) {
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

    public LiveData<? extends ItemResponse<? extends Item>> getItem(long id) {
        if(argViewItems == HomeFragment.JOB_STORIES_VIEW)
            return getJob(id);
        else
            return getStory(id);
    }

    public LiveData<ItemResponse<List<? extends Item>>> getItems(List<Long> ids) {
        return repository.getItems(ids);
    }

    private LiveData<ItemResponse<Story>> getStory(long id) {
        return repository.getStory(id);
    }

    private LiveData<ItemResponse<Job>> getJob(long id) {
        return repository.getJob(id);
    }

    public LiveData<ItemResponse<List<Story>>> getStories(List<Long> ids) {
        return repository.getStories(ids);
    }

}