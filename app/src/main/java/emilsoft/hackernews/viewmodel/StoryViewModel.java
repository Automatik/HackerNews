package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.repository.HackerNewsRepository;

public class StoryViewModel extends ViewModel {

    public LinkedList<Comment> commentsList;
    private HackerNewsRepository repository;
    public long lastCommentsRefreshTime = 0L;
    public boolean commentsFound = false;

    public String mUrl, mUser, mTitle;
    public int mPoints, mNumComments;
    public long mTime, mStoryId;
    public long[] mComments;
    public Story mStory;

    public long start1 = 0L;
    public long stop1 = 0L;
    public long start2=0L, stop2 = 0L;

    public StoryViewModel() {
        repository = HackerNewsRepository.getInstance();
        commentsList = new LinkedList<>();

    }

    public LiveData<Story> getStory() {
        return repository.getStory(mStoryId);
    }

    public LiveData<Comment> getComment(long id) {
        return repository.getComment(id);
    }

    public LiveData<List<Comment>> getComments(List<Long> ids) { return repository.getComments(ids); }

}
