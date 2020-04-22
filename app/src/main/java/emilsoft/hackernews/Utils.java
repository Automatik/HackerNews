package emilsoft.hackernews;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import static emilsoft.hackernews.MainActivity.TAG;

public class Utils {

    private static final String ABBR_YEAR = "y";
    private static final String ABBR_WEEK = "w";
    private static final String ABBR_DAY = "d";
    private static final String ABBR_HOUR = "h";
    private static final String ABBR_MINUTE = "m";

    public static String getAbbreviatedTimeSpan(long time) {
        //Multiply for 1000 because HackerNews provide time in seconds instead of milliseconds
        time *= 1000;
        long span = Math.max(System.currentTimeMillis() - time, 0);
        if (span >= DateUtils.YEAR_IN_MILLIS) {
            return (span / DateUtils.YEAR_IN_MILLIS) + ABBR_YEAR;
        }
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return (span / DateUtils.WEEK_IN_MILLIS) + ABBR_WEEK;
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) + ABBR_DAY;
        }
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            return (span / DateUtils.HOUR_IN_MILLIS) + ABBR_HOUR;
        }
        return (span / DateUtils.MINUTE_IN_MILLIS) + ABBR_MINUTE;
    }

    public static CharSequence fromHtml(String htmlText) {
        return fromHtml(htmlText, false);
    }

    public static CharSequence fromHtml(String htmlText, boolean compact) {
        if (TextUtils.isEmpty(htmlText)) {
            return null;
        }
        CharSequence spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //noinspection InlinedApi
            spanned = Html.fromHtml(htmlText, compact ?
                    Html.FROM_HTML_MODE_COMPACT : Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            spanned = Html.fromHtml(htmlText);
        }
        return trim(spanned);
    }

    private static CharSequence trim(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            return charSequence;
        }
        int end = charSequence.length() - 1;
        while (Character.isWhitespace(charSequence.charAt(end))) {
            end--;
        }
        return charSequence.subSequence(0, end + 1);
    }

    public static void openWebUrl(WeakReference<Activity> activity, String url) {
        Activity act = activity.get();
        if(act == null)
            return;
        Uri uri = Uri.parse(url);
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        //Set desired colors
        intentBuilder.setToolbarColor(ContextCompat.getColor(act, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(act, R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(act, uri);
    }

}
