package emilsoft.hackernews.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.CommentsAdapter;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.databinding.FragmentAskstoryBinding;
import emilsoft.hackernews.viewmodel.AskViewModel;

public class AskFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String ARG_ASK = "argument_ask";
    public static final String ARG_VIEWING_ASK = "argument_is_ask_text_viewed";

    private CardView askStoryCard;
    private ConstraintLayout commentsLayout;
    private LinearLayout noCommentsLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView askText;
    private CommentsAdapter adapter;
    private AskViewModel askViewModel;

    public static AskFragment newInstance(Story askStory, boolean isAskTextViewed) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ASK, askStory);
        args.putBoolean(ARG_VIEWING_ASK, isAskTextViewed);
        AskFragment fragment = new AskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askViewModel = ViewModelProviders.of(this).get(AskViewModel.class);

        Bundle args = getArguments();
        if(args != null) {
            askViewModel.askStory = args.getParcelable(ARG_ASK);
            askViewModel.isAskTextViewed = args.getBoolean(ARG_VIEWING_ASK);
            if(askViewModel.askStory == null)
                askViewModel.askStory = new Story(); //to avoid errors in onCreateView
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAskstoryBinding binding = FragmentAskstoryBinding.inflate(inflater, container, false);
        TextView titleText, userText, urlText, timeText, pointsText, numCommentsText;
        askStoryCard = binding.storyAskCard;
        commentsLayout = binding.storyAskCommentsLayout;
        noCommentsLayout = binding.storyAskNocommentsLayout;
        swipeRefreshLayout = binding.storyAskSwipeRefresh;
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = binding.storyAskcommentsList;
        titleText = binding.storyAskTitle;
        urlText = binding.storyAskUrl;
        userText = binding.storyAskUser;
        timeText = binding.storyAskTime;
        pointsText = binding.storyAskPoints;
        numCommentsText = binding.storyAskNumComments;
        askText = binding.storyAskText;

        askStoryCard.setOnClickListener((v) -> {
            if(!askViewModel.isAskTextViewed) {
                showCommentsView(false);
                askViewModel.isAskTextViewed = true;
            }
        });

        commentsLayout.setOnClickListener((v) -> {
            if(askViewModel.isAskTextViewed) {
                showCommentsView(true);
                askViewModel.isAskTextViewed = false;
                askViewModel.lastCommentsRefreshTime = 0L;
                observeAskStory(true);
            }
        });

        titleText.setText(askViewModel.askStory.getTitle());
        urlText.setText(getString(R.string.item_base_url));
        userText.setText(askViewModel.askStory.getUser());
        timeText.setText(Utils.getAbbreviatedTimeSpan(askViewModel.askStory.getTime()));
        pointsText.setText(String.valueOf(askViewModel.askStory.getScore()));
        numCommentsText.setText(String.valueOf(askViewModel.askStory.getDescendants()));
        askText.setText(Utils.fromHtml(askViewModel.askStory.getText()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(askViewModel.commentsList != null) {
            adapter = new CommentsAdapter(askViewModel.commentsList);
            recyclerView.setAdapter(adapter);
        }
        if(!askViewModel.isAskTextViewed) {
            showCommentsView(true);
            startObservingComments(askViewModel.askStory);
        } else
            showCommentsView(false);
        observeAskStory(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_item_menu_refresh:
                swipeRefreshLayout.setRefreshing(true);
                observeAskStory(true);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        if(askViewModel.isAskTextViewed)
            observeAskStory(false);
        else
            observeAskStory(true);
    }

    private void observeAskStory(final boolean refreshComments) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - askViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
            askViewModel.commentsFound = true;
            showTextNoComments();
            askViewModel.lastCommentsRefreshTime = currentTime;
            askViewModel.getStory().observe(this, new Observer<Story>() {
                @Override
                public void onChanged(Story story) {
                    askViewModel.askStory = story;
                    if(story.getKids() == null || story.getKids().length == 0) {
                        askViewModel.commentsFound = false;
                        showTextNoComments();
                    }
                    if(refreshComments) {
                        int size = askViewModel.commentsList.size();
                        askViewModel.commentsList.clear();
                        if (adapter != null)
                            adapter.notifyItemRangeRemoved(0, size);
                        startObservingComments(story);
                    }
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void startObservingComments(Story story) {
        if(story.getKids() == null)
            return;
        for(long idComment : story.getKids())
            observeComment(idComment);
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    private void observeComment(long id) {
        askViewModel.getComment(id).observe(this, new Observer<Comment>() {
            @Override
            public void onChanged(Comment comment) {
                // Avoid inserting duplicates
                if(!askViewModel.commentsList.contains(comment)) {
                    int pos = askViewModel.commentsList.size();
                    long idParent = comment.getParent();
                    if (idParent == askViewModel.askStory.getId()) {
                        comment.setLevel(1);
                        askViewModel.commentsList.add(comment);
                        if (adapter != null)
                            adapter.notifyItemInserted(pos);
                    } else {
                        Comment parent = new Comment(idParent);
                        int index = askViewModel.commentsList.indexOf(parent);
                        parent = askViewModel.commentsList.get(index);
                        comment.setLevel(parent.getLevel() + 1);
                        index += 1; //+1 after the parent
                        askViewModel.commentsList.add(index, comment);
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

    private void showCommentsView(boolean showComments) {
        if(showComments) {
            askText.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.INVISIBLE);
            noCommentsLayout.setVisibility(View.INVISIBLE);
            askText.setVisibility(View.VISIBLE);
        }
    }

    private void showTextNoComments() {
        if(!askViewModel.commentsFound)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else
            noCommentsLayout.setVisibility(View.INVISIBLE);
    }
}
