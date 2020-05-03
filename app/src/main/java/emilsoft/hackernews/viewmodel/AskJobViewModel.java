package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedList;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.repository.HackerNewsRepository;

public class AskJobViewModel extends ViewModel {


    public LinkedList<Comment> commentsList;
    private HackerNewsRepository repository;
    public long lastCommentsRefreshTime = 0L;
    public boolean commentsFound = false;
    public boolean isAsk; // or job
    public Story askStory;
    public Job job;

    // True if viewing AskText, False if viewing Comments
    public boolean isAskTextViewed;

    public AskJobViewModel() {
        repository = HackerNewsRepository.getInstance();
        commentsList = new LinkedList<>();
    }

    public LiveData<Story> getAskStory() {
        if(askStory != null)
            return repository.getStory(askStory.getId());
        return null;
    }

    public LiveData<Job> getJob() {
        if(job != null)
            return repository.getJob(job.getId());
        return null;
    }

    public LiveData<Comment> getComment(long id) {
        return repository.getComment(id);
    }

}
