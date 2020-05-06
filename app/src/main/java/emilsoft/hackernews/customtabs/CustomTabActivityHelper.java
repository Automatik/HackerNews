package emilsoft.hackernews.customtabs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;

public class CustomTabActivityHelper implements ServiceConnectionCallback {

    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;


    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity The host activity.
     * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
     * @param uri the Uri to be opened.
     */
    public static void openCustomTab(Activity activity,
                                     CustomTabsIntent customTabsIntent,
                                     Uri uri) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        //If we cant find a package name, it means theres no browser that supports
        //Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            //Call external browser (I could instead use my own webview)
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(browserIntent);
        } else {

            // This set to only use chrome
//            customTabsIntent.intent.setPackage(packageName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                // Add your app as the referrer
                //It's usually very important for websites to track where their traffic is coming from.
                //Make sure you let them know you are sending them users by setting the referrer when launching your Custom Tab
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,
                        Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + activity.getPackageName()));
            }
            customTabsIntent.launchUrl(activity, uri);
        }
    }

    public static void openWebUrl(WeakReference<Context> context, String url) {
        openWebUrl(context, url, null);
    }

    public static void openWebUrl(WeakReference<Context> context, String articleUrl, String hackerNewsUrl) {
        Context ctx = context.get();
        if(ctx == null || articleUrl == null) return;

        Uri uri = Uri.parse(articleUrl);
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        //Set desired colors
        intentBuilder.setToolbarColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(ctx, R.color.colorPrimaryDark));

        // Set custom back button
        // Can't use bitmapfactory with vector drawable
//        intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_arrow_back_24dp));
        Bitmap icon = Utils.getBitmapFromVectorDrawable(ctx, R.drawable.ic_arrow_back_24dp);
        if (icon != null)
            intentBuilder.setCloseButtonIcon(icon);

        if (hackerNewsUrl != null) {
            // Open new Chrome Tab (not Custom)
            Uri hackerNewsUri = Uri.parse(hackerNewsUrl);
            Intent hackerNewsIntent = new Intent(Intent.ACTION_VIEW, hackerNewsUri);
            PendingIntent hackerNewsPendingIntent = PendingIntent.getActivity(ctx,
                    0,
                    hackerNewsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            intentBuilder.addMenuItem("HackerNews Link", hackerNewsPendingIntent);
        }

        CustomTabsIntent customTabsIntent = intentBuilder.build();
//        customTabsIntent.launchUrl(ctx, uri);
        openCustomTab((Activity) ctx, customTabsIntent, uri);
    }

    /**
     * @see {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)}.
     * @return true if call to mayLaunchUrl was accepted.
     */
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        if (mClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        return session.mayLaunchUrl(uri, extras, otherLikelyBundles);
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    public CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(null); //new NavigationCallback()
        }
        return mCustomTabsSession;
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     * @param connectionCallback
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     * @param activity the activity to be binded to the service.
     */
    public void bindCustomTabsService(Activity activity) {
        if (mClient != null) return;

        String packageName = CustomTabsHelper.getPackageNameToUse(activity);
        if (packageName == null) return;

        mConnection = new ServiceConnection(this);
        CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
    }

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     * @param activity the activity that is connected to the service.
     */
    public void unbindCustomTabsService(Activity activity) {
        if (mConnection == null) return;
        activity.unbindService(mConnection);
        mClient = null;
        mCustomTabsSession = null;
        mConnection = null;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        mClient = client;
        mClient.warmup(0L);
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsConnected();
    }

    @Override
    public void onServiceDisconnected() {
        mClient = null;
        mCustomTabsSession = null;
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsDisconnected();
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    public interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        void onCustomTabsConnected();

        /**
         * Called when the service is disconnected.
         */
        void onCustomTabsDisconnected();
    }

}
