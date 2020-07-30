package emilsoft.hackernews.api;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ItemResponse<T> {

    @Nullable private LiveData<T> data;
    @Nullable private Throwable error;
    private boolean isSuccess;

    public ItemResponse() {
        data = null;
        error = null;
        isSuccess = false;
    }

    public void setIsSuccess(LiveData<T> data) {
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

    public LiveData<T> getData() {
        if(isSuccess)
            return data;
        else {
            MutableLiveData<T> errorData = new MutableLiveData<>();
            errorData.setValue(null);
            return errorData;
        }
    }

    public Throwable getError() {
        return (isSuccess) ? null : error;
    }

}
