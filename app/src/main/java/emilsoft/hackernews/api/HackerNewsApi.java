package emilsoft.hackernews.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Observable;

public interface HackerNewsApi {

    public static final String BASE_URL = "https://hacker-news.firebaseio.com/v0/";
    public static final String HACKER_NEWS_BASE_URL = "https://news.ycombinator.com/item?id=";

    @GET("topstories.json")
    Observable<List<Long>> getTopStories();

    @GET("item/{id}.json")
    Observable<Story> getStory(@Path("id") long id);

    @GET("item/{id}.json")
    Observable<Comment> getComment(@Path("id") long id);

}
