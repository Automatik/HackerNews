package emilsoft.hackernews.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import emilsoft.hackernews.MainActivity;

public class ConnectivityProviderLegacyImpl extends ConnectivityProviderBaseImpl {

    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityReceiver receiver;

    public ConnectivityProviderLegacyImpl(Context context, ConnectivityManager connectivityManager) {
        this.context = context;
        this.connectivityManager = connectivityManager;
        receiver = new ConnectivityReceiver(this);
    }

    @Override
    protected void subscribe() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
    }

    @Override
    protected void unsubscribe() {
        context.unregisterReceiver(receiver);
    }

    @Override
    public NetworkState getNetworkState() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null) {
            return new NetworkState.ConnectedState.ConnectedLegacy(activeNetworkInfo);
        } else {
            return new NetworkState.NotConnectedState();
        }
    }

    private static class ConnectivityReceiver extends BroadcastReceiver {

        private ConnectivityProviderLegacyImpl provider;

        public ConnectivityReceiver(ConnectivityProviderLegacyImpl provider) {
            this.provider = provider;
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(MainActivity.TAG, "onReceive");
            // on some devices ConnectivityManager.getActiveNetworkInfo() does not provide the correct network state
            // https://issuetracker.google.com/issues/37137911
            NetworkInfo networkInfo = provider.connectivityManager.getActiveNetworkInfo();
//            if(networkInfo != null) {
//                //networkInfo.isConnectedOrConnecting()
//                provider.dispatchChange(networkInfo.isConnected());
//            }
            NetworkInfo fallbackNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkState state;
            if(networkInfo != null && networkInfo.isConnectedOrConnecting())
                state = new NetworkState.ConnectedState.ConnectedLegacy(networkInfo);
            else if(networkInfo != null && fallbackNetworkInfo != null && networkInfo.isConnectedOrConnecting() != fallbackNetworkInfo.isConnectedOrConnecting())
                state = new NetworkState.ConnectedState.ConnectedLegacy(fallbackNetworkInfo);
            else {
                NetworkInfo info = (networkInfo == null) ? fallbackNetworkInfo : networkInfo;
                state = (info != null) ? new NetworkState.ConnectedState.ConnectedLegacy(info) : new NetworkState.NotConnectedState();
            }
            provider.dispatchChange(state);
        }
    }
}
