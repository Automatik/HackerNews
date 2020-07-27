package emilsoft.hackernews.connectivity;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ConnectivityProviderBaseImpl implements ConnectivityProvider {
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<ConnectivityStateListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private boolean subscribed = false;

    @Override
    public synchronized void addListener(ConnectivityStateListener listener) {
        listeners.add(listener);

        /* NOTE: If the default network is not available when you register a listener,
        then you don’t receive any callback. For this reason, you have to always propagate
        the initial state to all consumers manually. */
        NetworkState state = getNetworkState();
//        listener.isConnected(false);
        listener.onStateChange(state); //propagate an initial state
        verifySubscription();
    }

    @Override
    public synchronized void removeListener(ConnectivityStateListener listener) {
        listeners.remove(listener);
        verifySubscription();
    }

    private void verifySubscription() {
        if(!subscribed && !listeners.isEmpty()) {
            subscribe();
            subscribed = true;
        } else if(subscribed && listeners.isEmpty()) {
            unsubscribe();
            subscribed = false;
        }
    }

//    protected void dispatchChange(final boolean isConnected) {
//        handler.post(() -> {
//            for(ConnectivityStateListener listener : listeners)
//                listener.isConnected(isConnected);
//        });
//    }

    protected void dispatchChange(final NetworkState state) {
        handler.post(() -> {
            for(ConnectivityStateListener listener : listeners)
                listener.onStateChange(state);
        });
    }

    protected abstract void subscribe();
    protected abstract void unsubscribe();
}
