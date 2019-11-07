package emilsoft.hackernews.viewmodel;

import android.annotation.SuppressLint;
import android.util.LongSparseArray;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.LinkedList;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.repository.HackerNewsRepository;

public class StoryViewModel extends ViewModel {

//    public HashMap<Long, Comment> commentsMap;
//    public LongSparseArray<Integer> kidsNumberLookup;
    public LinkedList<Comment> commentsList;
    private HackerNewsRepository repository;

    @SuppressLint("UseSparseArrays")
    public StoryViewModel() {
        repository = HackerNewsRepository.getInstance();
        commentsList = new LinkedList<>();
//        commentsMap = new HashMap<>();
//        kidsNumberLookup = new LongSparseArray<>();
    }

    public LiveData<Comment> getComment(long id) {
        return repository.getComment(id);
    }

}
