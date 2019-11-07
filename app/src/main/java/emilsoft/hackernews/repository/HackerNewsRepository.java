package emilsoft.hackernews.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.api.Story;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

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
        final MutableLiveData<List<Long>> data = new MutableLiveData<>();
        Call<List<Long>> call = hackerNewsApi.getTopStories();
        call.enqueue(new Callback<List<Long>>() {
            @EverythingIsNonNull
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if(!response.isSuccessful()) {
                    Log.v(TAG, "Code: "+response.code());
                    return;
                }
                List<Long> ids = response.body();
                if(ids == null) {
                    Log.v(TAG, "List of Top Stories ids is null");
                    return;
                }
                data.setValue(ids);
            }

            @EverythingIsNonNull
            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                Log.v(TAG, "onFailure: "+t.getMessage());
            }
        });

        return data;
    }

    public LiveData<Story> getStory(long id) {
        final MutableLiveData<Story> data = new MutableLiveData<>();
        Call<Story> call = hackerNewsApi.getStory(id);
        call.enqueue(new Callback<Story>() {
            @EverythingIsNonNull
            @Override
            public void onResponse(Call<Story> call, Response<Story> response) {
                if(!response.isSuccessful()) {
                    Log.v(TAG, "Code: "+response.code());
                    return;
                }
                data.setValue(response.body());
            }

            @EverythingIsNonNull
            @Override
            public void onFailure(Call<Story> call, Throwable t) {
                Log.v(TAG, "onFailure: "+t.getMessage());
            }
        });

        return data;
    }

    public LiveData<Comment> getComment(long id) {
        final MutableLiveData<Comment> data = new MutableLiveData<>();
        Call<Comment> call = hackerNewsApi.getComment(id);
        call.enqueue(new Callback<Comment>() {
            @EverythingIsNonNull
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if(!response.isSuccessful()) {
                    Log.v(TAG, "Code: "+response.code());
                    return;
                }
                data.setValue(response.body());
            }

            @EverythingIsNonNull
            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Log.v(TAG, "onFailure: "+t.getMessage());
            }
        });

        return data;
    }

}
