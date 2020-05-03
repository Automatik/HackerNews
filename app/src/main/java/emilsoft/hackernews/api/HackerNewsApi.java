package emilsoft.hackernews.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Observable;

public interface HackerNewsApi {

    String BASE_URL = "https://hacker-news.firebaseio.com/v0/";
    String HACKER_NEWS_BASE_URL = "https://news.ycombinator.com/item?id=";

    int MAX_TOP_STORIES_ITEMS = 500;

    @GET("topstories.json")
    Observable<List<Long>> getTopStories();

    @GET("newstories.json")
    Observable<List<Long>> getNewStories();

    @GET("beststories.json")
    Observable<List<Long>> getBestStories();

    @GET("askstories")
    Observable<List<Long>> getAskStories();

    @GET("showstories")
    Observable<List<Long>> getShowStories();

    @GET("jobstories")
    Observable<List<Long>> getJobStories();

    @GET("item/{id}.json")
    Observable<Story> getStory(@Path("id") long id);

    @GET("item/{id}.json")
    Observable<Job> getJob(@Path("id") long id);

    @GET("item/{id}.json")
    Observable<Comment> getComment(@Path("id") long id);

}
