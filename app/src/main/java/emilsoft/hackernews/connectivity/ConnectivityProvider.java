package emilsoft.hackernews.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public interface ConnectivityProvider {

    interface ConnectivityStateListener {
//        void isConnected(boolean isConnected);
        void onStateChange(NetworkState state);
    }

    void addListener(ConnectivityStateListener listener);

    void removeListener(ConnectivityStateListener listener);

    NetworkState getNetworkState();

    static ConnectivityProvider createProvider(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return new ConnectivityProviderImpl(connMgr);
        else
            return new ConnectivityProviderLegacyImpl(context, connMgr);

    }

    static boolean isStateConnected(ConnectivityProvider.NetworkState state) {
        if(!(state instanceof ConnectivityProvider.NetworkState.ConnectedState))
            return false;
        ConnectivityProvider.NetworkState.ConnectedState connectedState = (ConnectivityProvider.NetworkState.ConnectedState) state;
        return connectedState.isConnected();
    }

    abstract class NetworkState {

        private NetworkState() {}

        public static final class NotConnectedState extends NetworkState {

            public static final NotConnectedState instance;

            static {
                instance = new NotConnectedState();
            }
        }

        public abstract static class ConnectedState extends NetworkState {
            private final boolean isConnected;

            public final boolean isConnected() {
                return isConnected;
            }

            public ConnectedState(boolean isConnected) {
                this.isConnected = isConnected;
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            public static final class Connected extends ConnectedState {
                @NonNull
                private final NetworkCapabilities capabilities;

                public Connected(@NonNull NetworkCapabilities capabilities) {
                    super(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                    this.capabilities = capabilities;
                }

                @NonNull
                private final NetworkCapabilities getCapabilities() {
                    return capabilities;
                }
            }

            public static final class ConnectedLegacy extends ConnectedState {

                private final NetworkInfo networkInfo;

                public ConnectedLegacy(NetworkInfo networkInfo) {
                    super(networkInfo.isConnectedOrConnecting());
                    this.networkInfo = networkInfo;
                }

                private final NetworkInfo getNetworkInfo() {
                    return networkInfo;
                }

            }

        }

    }

}
