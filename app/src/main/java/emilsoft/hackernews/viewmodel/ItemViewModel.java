package emilsoft.hackernews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedList;
import java.util.List;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.repository.HackerNewsRepository;

public class ItemViewModel extends ViewModel {

    public LinkedList<Comment> commentsList;
    private HackerNewsRepository repository;
    public long lastCommentsRefreshTime = 0L;
    public boolean commentsFound = false;
    public Item item;

    public ItemViewModel() {
        repository = HackerNewsRepository.getInstance();
        commentsList = new LinkedList<>();
    }

    public LiveData<? extends Item> getItem() {
        if(item != null)
            return repository.getItem(item.getId());
        return null;
    }

    public LiveData<List<Comment>> getComments(List<Long> ids) { return repository.getComments(ids); }

}
