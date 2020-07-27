package emilsoft.hackernews.api;

import java.io.IOException;
import java.lang.annotation.Annotation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Type;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {

    private final RxJava2CallAdapterFactory original;

    private RxErrorHandlingCallAdapterFactory() {
        original = RxJava2CallAdapterFactory.create();
    }

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    @Override
    public @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper<>(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Object> {

        private final Retrofit retrofit;
        private final CallAdapter<R, ?> wrapped;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, ?> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @NonNull
        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @NonNull
        @Override
        public Object adapt(Call<R> call) {
            Object result = wrapped.adapt(call);
            if (result instanceof Single) {
                return ((Single<Response<R>>) result).onErrorResumeNext((e) -> Single.error(asRetrofitException(e)));
            }

            if (result instanceof Observable) {
                return ((Observable<Response<R>>) result).onErrorResumeNext(e -> {
                    return Observable.error(asRetrofitException(e));
                });
            }

            if (result instanceof Completable) {
                return ((Completable) result).onErrorResumeNext((e) -> Completable.error(asRetrofitException(e)));
            }

            return result;
        }

        private RetrofitException asRetrofitException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                Response<?> response = httpException.response();
                if(response != null)
                    return RetrofitException.httpError(response.raw().request().url().toString(), response, retrofit);
            }
            // A network error happened
            if (throwable instanceof IOException) {
                return RetrofitException.networkError((IOException) throwable);
            }

            // We don't know what happened. We need to simply convert to an unknown error

            return RetrofitException.unexpectedError(throwable);
        }
    }
}
