package emilsoft.hackernews.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import emilsoft.hackernews.adapter.CommentsAdapter;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.viewmodel.StoryViewModel;
import retrofit2.Call;

import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Comment;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static emilsoft.hackernews.MainActivity.TAG;

public class StoryFragment extends Fragment {

//    public static final String ARG_URL = "argument_url";
//    public static final String ARG_USER = "argument_user";
//    public static final String ARG_TITLE = "argument_title";
//    public static final String ARG_TIME = "argument_time";
//    public static final String ARG_NUM_COMMENTS = "argument_num_comments";
//    public static final String ARG_POINTS = "argument_points";
//    public static final String ARG_COMMENTS = "argument_comments";
    public static final String ARG_STORY = "argument_story";

    private WebView mWebView;
    private TextView titleText, userText, urlText, timeText, pointsText, numCommentsText;
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;

    private String mUrl, mUser, mTitle;
    private int mPoints, mNumComments;
    private long mTime, mStoryId;
    private long[] mComments;
    private Story mStory;

    private StoryViewModel storyViewModel;

    @SuppressLint("UseSparseArrays")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storyViewModel = ViewModelProviders.of(this).get(StoryViewModel.class);

        Bundle args = getArguments();
        if(args != null) {
            mStory = args.getParcelable(ARG_STORY);
            if(mStory == null) {
                Log.v(TAG, "Story is null");
                return;
            }
            mStoryId = mStory.getId();
            mUrl = mStory.getUrl();
            mTitle = mStory.getTitle();
            mUser = mStory.getUser();
            mPoints = mStory.getScore();
            mNumComments = mStory.getDescendants();
            mTime = mStory.getTime();
            mComments = mStory.getKids();

            if(mComments == null) {
                Log.v(TAG, "Comments are null");
                return;
            }

            for(long idComment : mComments)
                observeComment(idComment);

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
        recyclerView = view.findViewById(R.id.comments_list);
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
        if(storyViewModel.commentsList != null) {
            adapter = new CommentsAdapter(storyViewModel.commentsList);
            recyclerView.setAdapter(adapter);
        }
    }

    private void observeComment(long id) {
        storyViewModel.getComment(id).observe(this, new Observer<Comment>() {
            @Override
            public void onChanged(Comment comment) {
                int pos = storyViewModel.commentsList.size();
                Log.v(TAG, "Comments: "+(pos+1));
                long idParent = comment.getParent();
                if(idParent == mStoryId) {
                    storyViewModel.commentsList.add(comment);
                    if(adapter != null)
                        adapter.notifyItemInserted(pos);
                } else {
                    Comment parent = new Comment(idParent);
                    int index = storyViewModel.commentsList.indexOf(parent) + 1; //+1 after the parent
                    storyViewModel.commentsList.add(index, comment);
                    if(adapter != null)
                        adapter.notifyItemInserted(index);
                }
                long[] kids = comment.getKids();
                if(kids != null)
                    for(long idComment : kids)
                        observeComment(idComment);
            }
        });
    }


}
