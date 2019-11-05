package emilsoft.hackernews.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import emilsoft.hackernews.api.Story;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    public List<Long> topStoriesIds;
    public List<Story> topStories;
    public int lastItemLoadedIndex;

    public HomeViewModel() {
        topStoriesIds = new ArrayList<>();
        topStories = new ArrayList<>();
    }

}