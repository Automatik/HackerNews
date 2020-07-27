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
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.RetrofitException;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.api.Type;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

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
        return getStoriesIds(hackerNewsApi.getTopStories());
    }

    public LiveData<List<Long>> getNewStoriesIds() {
        return getStoriesIds(hackerNewsApi.getNewStories());
    }

    public LiveData<List<Long>> getBestStoriesIds() {
        return getStoriesIds(hackerNewsApi.getBestStories());
    }

    public LiveData<List<Long>> getAskStoriesIds() {
        return getStoriesIds(hackerNewsApi.getAskStories());
    }

    public LiveData<List<Long>> getShowStoriesIds() {
        return getStoriesIds(hackerNewsApi.getShowStories());
    }

    public LiveData<List<Long>> getJobStoriesIds() {
        return getStoriesIds(hackerNewsApi.getJobStories());
    }

    private LiveData<List<Long>> getStoriesIds(Observable<List<Long>> observable) {
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
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                        RetrofitException error = (RetrofitException) e;
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getItems/ "+ error.getResponse());
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
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
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

    public LiveData<List<Story>> getStories(List<Long> ids) {
        final MutableLiveData<List<Story>> data = new MutableLiveData<>();
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
                        data.setValue(stories);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<? extends Item> getItem(long id) {
        final MutableLiveData<Item> data = new MutableLiveData<>();
        hackerNewsApi.getItem(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Item>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Item item) {
                        data.setValue(item);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return data;
    }

    public LiveData<List<? extends Item>> getItems(List<Long> ids) {
        final MutableLiveData<List<? extends Item>> data = new MutableLiveData<>();
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
                        data.setValue(items);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getItems/ "+ Log.getStackTraceString(e));
                        RetrofitException error = (RetrofitException) e;
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getItems/ "+ error.getResponse());
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

    public LiveData<List<Comment>> getComments(List<Long> ids) {
        final MutableLiveData<List<Comment>> data = new MutableLiveData<>();
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
                        data.setValue(commentList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getComments/ "+ Log.getStackTraceString(e));
                        RetrofitException error = (RetrofitException) e;
                        Log.v(MainActivity.TAG, "HackerNewsRepository/getItems/ "+ error.getResponse());
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
