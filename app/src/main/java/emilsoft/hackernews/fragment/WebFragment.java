package emilsoft.hackernews.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Story;

import static emilsoft.hackernews.MainActivity.TAG;

public class WebFragment extends Fragment {

    public static final String ARG_STORY = "argument_story";

    private WebView mWebView;
    private String mUrl;

    public static WebFragment newInstance(Story story) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_STORY, story);
        WebFragment fragment = new WebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = view.findViewById(R.id.webview_story);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.v(TAG, getArguments() != null ? "Args not null" : "Args null");
        Bundle args = getArguments();
        if(args != null) {
            Story mStory = args.getParcelable(ARG_STORY);
            if(mStory == null) {
                Log.v(TAG, "Story is null");
                return;
            }
            mUrl = mStory.getUrl();
            Log.v(TAG, "Url: "+mUrl);
        }
        if(mUrl != null)
            mWebView.loadUrl(mUrl);
    }
}
