package emilsoft.hackernews.api;

import androidx.annotation.Nullable;

public class ItemResponse<T> {

    private T data;
    private Throwable error;
    private boolean isSuccess;

    public ItemResponse() {
        data = null;
        error = null;
        isSuccess = false;
    }

    public void setIsSuccess(T data) {
        this.data = data;
        error = null;
        isSuccess = true;
    }

    public void setIsFailed(Throwable error) {
        this.error = error;
        data = null;
        isSuccess = false;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public T getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

}
