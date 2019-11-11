package emilsoft.hackernews.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    private static RetrofitHelper instance;
    private Retrofit retrofit;

    private RetrofitHelper() {
        retrofit = new Retrofit.Builder()
                .baseUrl(HackerNewsApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static void newInstance() {
        if(instance == null)
            instance = new RetrofitHelper();
    }

    public static <T> T create(final Class<T> service) {
        if(instance == null)
            newInstance();
        return instance.retrofit.create(service);
    }

}
