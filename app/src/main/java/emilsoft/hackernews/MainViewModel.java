package emilsoft.hackernews;

import androidx.lifecycle.ViewModel;

import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.RetrofitHelper;

public class MainViewModel extends ViewModel {

    public HackerNewsApi hackerNewsApi;

    public MainViewModel() {
        hackerNewsApi = RetrofitHelper.create(HackerNewsApi.class);
    }

}
