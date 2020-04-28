package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.LinkedList;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.repository.HackerNewsRepository;

public class StoryViewModel extends ViewModel {



    public LinkedList<Comment> commentsList;
    private HackerNewsRepository repository;

    public StoryViewModel() {
        repository = HackerNewsRepository.getInstance();
        commentsList = new LinkedList<>();

    }

    public LiveData<Comment> getComment(long id) {
        return repository.getComment(id);
    }

}
