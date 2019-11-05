package emilsoft.hackernews.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.TreeMap;

import emilsoft.hackernews.MainViewModel;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.RetrofitHelper;
import emilsoft.hackernews.tree.GenericTree;
import emilsoft.hackernews.tree.GenericTreeNode;
import retrofit2.Call;

import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Comment;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static emilsoft.hackernews.MainActivity.TAG;

public class StoryFragment extends Fragment {

    public static final String ARG_URL = "argument_url";
    public static final String ARG_USER = "argument_user";
    public static final String ARG_TITLE = "argument_title";
    public static final String ARG_TIME = "argument_time";
    public static final String ARG_NUM_COMMENTS = "argument_num_comments";
    public static final String ARG_POINTS = "argument_points";
    public static final String ARG_COMMENTS = "argument_comments";

    private WebView mWebView;
    private TextView titleText, userText, urlText, timeText, pointsText, numCommentsText;

    private String mUrl, mUser, mTitle;
    private int mPoints, mNumComments;
    private long mTime;
    private long[] mComments;

    private MainViewModel mainViewModel;
    private HackerNewsApi hackerNewsApi;
    private GenericTree<Comment> commentsTree;

    //private final HackerNewsApi hackerNewsApi = RetrofitHelper.create(HackerNewsApi.class);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        hackerNewsApi = mainViewModel.hackerNewsApi;
        Comment root = new Comment();
        GenericTreeNode<Comment> rootNode = new GenericTreeNode<>(root);
        commentsTree = new GenericTree<>();
        commentsTree.setRoot(rootNode);

        Bundle args = getArguments();
        if(args != null) {
            mUrl = args.getString(ARG_URL);
            mUser = args.getString(ARG_USER);
            mTitle = args.getString(ARG_TITLE);
            mPoints = args.getInt(ARG_POINTS);
            mNumComments = args.getInt(ARG_NUM_COMMENTS);
            mTime = args.getLong(ARG_TIME);
            mComments = args.getLongArray(ARG_COMMENTS);

            if(mComments == null) {
                Log.v(TAG, "Comments are null");
                return;
            }

            getComments();
        } else {
            mUrl = "www.example.com";
            mTitle = "Error Retrieving Story";
            mUser = "";
            mPoints = 0;
            mNumComments = 0;
            mTime = 0;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story, container, false);
        titleText = view.findViewById(R.id.story_article_title);
        urlText = view.findViewById(R.id.story_article_url);
        userText = view.findViewById(R.id.story_article_user);
        timeText = view.findViewById(R.id.story_article_time);
        pointsText = view.findViewById(R.id.story_article_points);
        numCommentsText = view.findViewById(R.id.story_article_num_comments);

        titleText.setText(mTitle);
        urlText.setText(mUrl);
        userText.setText(mUser);
        pointsText.setText(String.valueOf(mPoints));
        timeText.setText(Utils.getAbbreviatedTimeSpan(mTime));
        numCommentsText.setText(String.valueOf(mNumComments));
        //mWebView = view.findViewById(R.id.webview_story);
        //WebSettings webSettings = mWebView.getSettings();
        //webSettings.setJavaScriptEnabled(true);
        //mWebView.loadUrl(mUrl);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void getComments() {
        for(int i = 0; i < mComments.length; i++) {
            Call<Comment> call = hackerNewsApi.getComment(mComments[i]);
            call.enqueue(commentCallback);
        }
    }

    private Callback<Comment> commentCallback = new Callback<Comment>() {

        @Override
        @EverythingIsNonNull
        public void onResponse(Call<Comment> call, Response<Comment> response) {
            if(!response.isSuccessful()) {
                Log.v(TAG, "Code: "+response.code());
                return;
            }
            Comment comment = response.body();
            Log.v(TAG, "Comment: "+comment.getId());
        }

        @Override
        @EverythingIsNonNull
        public void onFailure(Call<Comment> call, Throwable t) {
            Log.v(TAG, "onFailure: "+t.getMessage());
        }
    };


}
