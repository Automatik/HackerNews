package emilsoft.hackernews.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.repository.HackerNewsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeViewModel extends ViewModel {

    public List<Long> topStoriesIds;
    public List<Story> topStories;
    private HackerNewsRepository repository;
    public int lastItemLoadedIndex;
    public long lastIdsRefreshTime = 0L;

    public HomeViewModel() {
        repository = HackerNewsRepository.getInstance();
        topStoriesIds = new ArrayList<>();
        topStories = new ArrayList<>();
    }

    public LiveData<List<Long>> getTopStoriesIds() {
        return repository.getTopStoriesIds();
    }

    public LiveData<Story> getStory(long id) {
        return repository.getStory(id);
    }

}