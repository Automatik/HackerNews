package emilsoft.hackernews.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.ItemResponse;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.RetrofitException;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.api.Story;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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

    public LiveData<ItemResponse<List<Long>>> getTopStoriesIds() {
        return getItemsIds(hackerNewsApi.getTopStories());
    }

    public LiveData<ItemResponse<List<Long>>> getNewStoriesIds() {
        return getItemsIds(hackerNewsApi.getNewStories());
    }

    public LiveData<ItemResponse<List<Long>>> getBestStoriesIds() {
        return getItemsIds(hackerNewsApi.getBestStories());
    }

    public LiveData<ItemResponse<List<Long>>> getAskStoriesIds() {
        return getItemsIds(hackerNewsApi.getAskStories());
    }

    public LiveData<ItemResponse<List<Long>>> getShowStoriesIds() {
        return getItemsIds(hackerNewsApi.getShowStories());
    }

    public LiveData<ItemResponse<List<Long>>> getJobStoriesIds() {
        return getItemsIds(hackerNewsApi.getJobStories());
    }

    private LiveData<ItemResponse<List<Long>>> getItemsIds(Observable<List<Long>> observable) {
        final MutableLiveData<ItemResponse<List<Long>>> data = new MutableLiveData<>();
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Long>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Long> ids) {
                        if(ids == null)
                            return;
                        ItemResponse<List<Long>> response = new ItemResponse<>();
                        response.setIsSuccess(ids);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                        ItemResponse<List<Long>> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<ItemResponse<Story>> getStory(long id) {
        final MutableLiveData<ItemResponse<Story>> data = new MutableLiveData<>();
        hackerNewsApi.getStory(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Story>() {

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                        ItemResponse<Story> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Story story) {
                        ItemResponse<Story> response = new ItemResponse<>();
                        response.setIsSuccess(story);
                        data.setValue(response);
                    }
                });
        return data;
    }

    public LiveData<ItemResponse<List<Story>>> getStories(List<Long> ids) {
        final MutableLiveData<ItemResponse<List<Story>>> data = new MutableLiveData<>();
        List<Observable<Story>> observables = new ArrayList<>(ids.size());
        for (Long id : ids)
//            observables.add(Observable.defer(() -> hackerNewsApi.getStory(id)));
            observables.add(hackerNewsApi.getStory(id).subscribeOn(Schedulers.io()));
        Observable<List<Story>> observable = Observable.zip(observables, new Function<Object[], List<Story>>() {
            @Override
            public List<Story> apply(Object[] stories) throws Exception {
                List<Story> list = new ArrayList<>(stories.length);
                for(Object s : stories)
                    list.add((Story) s);
                return list;
            }
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Story>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Story> stories) {
                        ItemResponse<List<Story>> response = new ItemResponse<>();
                        response.setIsSuccess(stories);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ItemResponse<List<Story>> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<ItemResponse<? extends Item>> getItem(long id) {
        final MutableLiveData<ItemResponse<? extends Item>> data = new MutableLiveData<>();
        hackerNewsApi.getItem(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Item>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Item item) {
                        ItemResponse<Item> response = new ItemResponse<>();
                        response.setIsSuccess(item);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                        ItemResponse<Item> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<ItemResponse<List<? extends Item>>> getItems(List<Long> ids) {
        final MutableLiveData<ItemResponse<List<? extends Item>>> data = new MutableLiveData<>();
        Observable.fromIterable(ids)
                .flatMap((id) -> hackerNewsApi.getItem(id).subscribeOn(Schedulers.io()))
                .map(item -> {
                    switch (item.getType()) {
                        case JOB_TYPE: return (Job) item;
                        default: return item;
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Item>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Item> items) {
                        ItemResponse<List<? extends Item>> response = new ItemResponse<>();
                        response.setIsSuccess(items);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getItems/ "+ Log.getStackTraceString(e));
                        ItemResponse<List<? extends Item>> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }
                });

//        List<Observable<? extends Item>> observables = new ArrayList<>(ids.size());
//        for (Long id : ids) {
//            observables.add(hackerNewsApi.getItem(id).subscribeOn(Schedulers.io()));
//        }
//        Observable<List<? extends Item>> observable = Observable.zip(observables, items -> {
//            List<Item> list = new ArrayList<>(items.length);
//            for(Object i : items) {
//                Item item = (Item) i;
//                switch (item.getType()) {
//                    case JOB_TYPE: list.add((Job) i); break;
//                    case STORY_TYPE:
//                    default: list.add((Story) i);
//                }
//            }
//            return list;
//        });
//        observable.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<? extends Item>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(List<? extends Item> items) {
//                        data.setValue(items);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
        return data;
    }

    public LiveData<ItemResponse<Job>> getJob(long id) {
        final MutableLiveData<ItemResponse<Job>> data = new MutableLiveData<>();
        hackerNewsApi.getJob(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Job>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Job job) {
                        ItemResponse<Job> response = new ItemResponse<>();
                        response.setIsSuccess(job);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getJob/ "+ Log.getStackTraceString(e));
                        ItemResponse<Job> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<ItemResponse<Comment>> getComment(long id) {
        final MutableLiveData<ItemResponse<Comment>> data = new MutableLiveData<>();
        hackerNewsApi.getComment(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comment>() {

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getComment/ "+ Log.getStackTraceString(e));
                        ItemResponse<Comment> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Comment comment) {
                        ItemResponse<Comment> response = new ItemResponse<>();
                        response.setIsSuccess(comment);
                        data.setValue(response);
                    }
                });

        return data;
    }

    public LiveData<ItemResponse<List<Comment>>> getComments(List<Long> ids) {
        final MutableLiveData<ItemResponse<List<Comment>>> data = new MutableLiveData<>();
        Observable.fromIterable(ids)
                // use concatMap if we want to preserve order instead
                .flatMap((id) -> hackerNewsApi.getComment(id).subscribeOn(Schedulers.io()))
                .subscribeOn(Schedulers.io())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Comment>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Comment> commentList) {
                        ItemResponse<List<Comment>> response = new ItemResponse<>();
                        response.setIsSuccess(commentList);
                        data.setValue(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getComments/ "+ Log.getStackTraceString(e));
                        ItemResponse<List<Comment>> response = new ItemResponse<>();
                        response.setIsFailed(e);
                        data.setValue(response);
                    }
                });

//        List<Observable<Comment>> observables = new ArrayList<>(ids.size());
//        for (Long id : ids) {
//            observables.add(hackerNewsApi.getComment(id).subscribeOn(Schedulers.io()));
//        }
//        Observable<List<Comment>> observable = Observable.zip(observables, new Function<Object[], List<Comment>>() {
//            @Override
//            public List<Comment> apply(Object[] objects) throws Exception {
//                Log.v(MainActivity.TAG, "getComments/ apply; objects' size: " + objects.length);
//                List<Comment> list = new ArrayList<>(objects.length);
//                for(Object o : objects)
//                    list.add((Comment) o);
//                return list;
//            }
//        });
//        observable.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<Comment>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(List<Comment> comments) {
//                        data.setValue(comments);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.v(MainActivity.TAG, Log.getStackTraceString(e));
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
        return data;
    }

}
