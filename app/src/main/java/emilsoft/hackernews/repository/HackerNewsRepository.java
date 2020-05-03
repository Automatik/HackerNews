package emilsoft.hackernews.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

import emilsoft.hackernews.api.Ask;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.api.Story;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


import static emilsoft.hackernews.MainActivity.TAG;

public class HackerNewsRepository {

    private HackerNewsApi hackerNewsApi;
    private static HackerNewsRepository instance;

    private HackerNewsRepository() {
        hackerNewsApi = RetrofitHelper.create(HackerNewsApi.class);
    }

    public synchronized static HackerNewsRepository getInstance() {
        if(instance == null)
            instance = new HackerNewsRepository();
        return instance;
    }

    public LiveData<List<Long>> getTopStoriesIds() {
        return getStories(hackerNewsApi.getTopStories());
    }

    public LiveData<List<Long>> getNewStoriesIds() {
        return getStories(hackerNewsApi.getNewStories());
    }

    public LiveData<List<Long>> getBestStoriesIds() {
        return getStories(hackerNewsApi.getBestStories());
    }

    public LiveData<List<Long>> getAskStoriesIds() {
        return getStories(hackerNewsApi.getAskStories());
    }

    public LiveData<List<Long>> getShowStoriesIds() {
        return getStories(hackerNewsApi.getShowStories());
    }

    public LiveData<List<Long>> getJobStoriesIds() {
        return getStories(hackerNewsApi.getJobStories());
    }

    private LiveData<List<Long>> getStories(Observable<List<Long>> observable) {
        final MutableLiveData<List<Long>> data = new MutableLiveData<>();
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Long>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(List<Long> ids) {
                        if(ids == null)
                            return;
                        data.setValue(ids);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {}
                });
        return data;
    }

    public LiveData<Story> getStory(long id) {
        final MutableLiveData<Story> data = new MutableLiveData<>();
        hackerNewsApi.getStory(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Story>() {

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Story story) {
                        data.setValue(story);
                    }
                });
        return data;
    }

    public LiveData<Job> getJob(long id) {
        final MutableLiveData<Job> data = new MutableLiveData<>();
        hackerNewsApi.getJob(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Job>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Job job) {
                        data.setValue(job);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<Comment> getComment(long id) {
        final MutableLiveData<Comment> data = new MutableLiveData<>();
        hackerNewsApi.getComment(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comment>() {

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Comment comment) {
                        data.setValue(comment);
                    }
                });

        return data;
    }

}
