package emilsoft.hackernews.connectivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import emilsoft.hackernews.MainActivity;

@RequiresApi(Build.VERSION_CODES.N)
public class ConnectivityProviderImpl extends ConnectivityProviderBaseImpl{

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public ConnectivityProviderImpl(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
        networkCallback = new ConnectivityCallback(this);
    }

    @Override
    protected void subscribe() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    protected void unsubscribe() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public NetworkState getNetworkState() {
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if(capabilities != null) {
            return new NetworkState.ConnectedState.Connected(capabilities);
        } else {
            return new NetworkState.NotConnectedState();
        }
    }

    private static class ConnectivityCallback extends ConnectivityManager.NetworkCallback {

        private ConnectivityProviderImpl provider;

        public ConnectivityCallback(ConnectivityProviderImpl provider) {
            this.provider = provider;
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
//            provider.dispatchChange(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
            provider.dispatchChange(new NetworkState.ConnectedState.Connected(networkCapabilities));
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            NetworkCapabilities capabilities = provider.connectivityManager.getNetworkCapabilities(network);
            if(capabilities != null)
//                provider.dispatchChange(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                provider.dispatchChange(new NetworkState.ConnectedState.Connected(capabilities));
        }

        @Override
        public void onLost(@NonNull Network network) {
//            provider.dispatchChange(false);
            provider.dispatchChange(new NetworkState.NotConnectedState());
        }
    }


}
