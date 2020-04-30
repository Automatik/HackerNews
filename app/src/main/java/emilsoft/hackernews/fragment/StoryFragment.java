package emilsoft.hackernews.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import emilsoft.hackernews.adapter.CommentsAdapter;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.databinding.FragmentStoryBinding;
import emilsoft.hackernews.viewmodel.StoryViewModel;

import emilsoft.hackernews.R;
import emilsoft.hackernews.api.Comment;

import static emilsoft.hackernews.MainActivity.TAG;

public class StoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String ARG_STORY = "argument_story";

    private TextView titleText, userText, urlText, timeText, pointsText, numCommentsText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;

    private StoryViewModel storyViewModel;

    public static StoryFragment newInstance(Story story) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_STORY, story);
        StoryFragment fragment = new StoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storyViewModel = ViewModelProviders.of(this).get(StoryViewModel.class);

        Bundle args = getArguments();
        if(args != null) {
            Story mStory = args.getParcelable(ARG_STORY);
            storyViewModel.mStory = mStory;
            if(storyViewModel.mStory == null) {
                Log.v(TAG, "Story is null");
                return;
            }

            startObservingComments(mStory);

        } else {
            storyViewModel.mUrl = "www.example.com";
            storyViewModel.mTitle = "Error Retrieving Story";
            storyViewModel.mUser = "";
            storyViewModel.mPoints = 0;
            storyViewModel.mNumComments = 0;
            storyViewModel.mTime = 0;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentStoryBinding binding = FragmentStoryBinding.inflate(inflater, container, false);
        swipeRefreshLayout = binding.commentsSwipeRefresh;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.redA200);
        recyclerView = binding.commentsList;
        titleText = binding.storyArticleTitle;
        urlText = binding.storyArticleUrl;
        userText = binding.storyArticleUser;
        timeText = binding.storyArticleTime;
        pointsText = binding.storyArticlePoints;
        numCommentsText = binding.storyArticleNumComments;

        titleText.setText(storyViewModel.mTitle);
        urlText.setText(storyViewModel.mUrl);
        userText.setText(storyViewModel.mUser);
        pointsText.setText(String.valueOf(storyViewModel.mPoints));
        timeText.setText(Utils.getAbbreviatedTimeSpan(storyViewModel.mTime));
        numCommentsText.setText(String.valueOf(storyViewModel.mNumComments));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(storyViewModel.commentsList != null) {
            adapter = new CommentsAdapter(storyViewModel.commentsList);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_item_menu_refresh:
                swipeRefreshLayout.setRefreshing(true);
                observeStory();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onRefresh() {
        observeStory();
    }

    private void observeStory() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - storyViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
            storyViewModel.lastCommentsRefreshTime = currentTime;
            storyViewModel.getStory().observe(this, new Observer<Story>() {
                @Override
                public void onChanged(Story story) {
                    storyViewModel.mStory = story;
                    int size = storyViewModel.commentsList.size();
                    storyViewModel.commentsList.clear();
                    if (adapter != null)
                        adapter.notifyItemRangeRemoved(0, size);
                    startObservingComments(story);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void startObservingComments(Story story) {
        storyViewModel.mStoryId = story.getId();
        storyViewModel.mUrl = story.getUrl();
        storyViewModel.mTitle = story.getTitle();
        storyViewModel.mUser = story.getUser();
        storyViewModel.mPoints = story.getScore();
        storyViewModel.mNumComments = story.getDescendants();
        storyViewModel.mTime = story.getTime();
        storyViewModel.mComments = story.getKids();
        if(storyViewModel.mComments == null)
            storyViewModel.mComments = new long[0];
        for(long idComment : storyViewModel.mComments) {
            observeComment(idComment);
        }
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    private void observeComment(long id) {
        storyViewModel.getComment(id).observe(this, new Observer<Comment>() {
            @Override
            public void onChanged(Comment comment) {
                // Avoid inserting duplicates
                if(!storyViewModel.commentsList.contains(comment)) {
                    int pos = storyViewModel.commentsList.size();
                    long idParent = comment.getParent();
                    if (idParent == storyViewModel.mStoryId) {
                        comment.setLevel(1);
                        storyViewModel.commentsList.add(comment);
                        if (adapter != null)
                            adapter.notifyItemInserted(pos);
                    } else {
                        Comment parent = new Comment(idParent);
                        int index = storyViewModel.commentsList.indexOf(parent);
                        parent = storyViewModel.commentsList.get(index);
                        comment.setLevel(parent.getLevel() + 1);
                        index += 1; //+1 after the parent
                        storyViewModel.commentsList.add(index, comment);
                        if (adapter != null)
                            adapter.notifyItemInserted(index);
                    }
                }
                long[] kids = comment.getKids();
                if(kids != null)
                    for(long idComment : kids)
                        observeComment(idComment);
            }
        });
    }

}
