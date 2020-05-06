package emilsoft.hackernews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsService;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emilsoft.hackernews.api.HackerNewsApi;

import static emilsoft.hackernews.MainActivity.TAG;

public class Utils {

    public static final long CACHE_EXPIRATION = 1000 * 60 * 5; // 5 minutes

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

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null)
            return null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static String toHackerNewsUrl(long itemId) {
        return HackerNewsApi.HACKER_NEWS_BASE_URL + itemId;
    }

    public static List<Bundle> toCustomTabUriBundle(List<Uri> uris) {
        return toCustomTabUriBundle(uris, 0);
    }

    public static List<Bundle> toCustomTabUriBundle(List<Uri> uris, int startIndex) {
        if(uris == null || uris.size() == 0 || startIndex < 0 || startIndex >= uris.size())
            return Collections.emptyList();
        List<Bundle> uriBundles = new ArrayList<>(uris.size());
        for(int i = startIndex; i < uris.size(); i++) {
            Uri uri = uris.get(i);
            if(uri != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(CustomTabsService.KEY_URL, uri);
                uriBundles.add(bundle);
            }
        }
        return uriBundles;
    }

}
