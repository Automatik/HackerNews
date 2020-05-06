package emilsoft.hackernews.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.adapter.CommentsAdapter;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentAskstoryBinding;
import emilsoft.hackernews.viewmodel.AskJobViewModel;

public class AskJobFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static final String ARG_ASK_JOB = "argument_ask_job";
    public static final String ARG_VIEWING_ASK_JOB = "argument_is_ask_job_text_viewed";
    public static final String ARG_IS_ASK_OR_JOB = "argument_is_ask_or_job";

    private CardView askJobStoryCard;
    private ConstraintLayout commentsLayout;
    private LinearLayout noCommentsLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView askJobText;
    private CommentsAdapter adapter;
    private AskJobViewModel askJobViewModel;
    private CustomTabActivityHelper.LaunchUrlCallback launchUrlCallback;

    public static AskJobFragment newInstance(Item item, boolean isAskTextViewed, boolean isAsk) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ASK_JOB, item);
        args.putBoolean(ARG_VIEWING_ASK_JOB, isAskTextViewed);
        args.putBoolean(ARG_IS_ASK_OR_JOB, isAsk);
        AskJobFragment fragment = new AskJobFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        askJobViewModel = new ViewModelProvider(this).get(AskJobViewModel.class);

        Bundle args = getArguments();
        if(savedInstanceState != null)
            args = savedInstanceState;
        if(args != null) {
            askJobViewModel.isAsk = args.getBoolean(ARG_IS_ASK_OR_JOB);
            Item item = args.getParcelable(ARG_ASK_JOB);
            if(askJobViewModel.isAsk && item instanceof Story) {
                askJobViewModel.askStory = args.getParcelable(ARG_ASK_JOB);
                askJobViewModel.job = null;
            } else if(!askJobViewModel.isAsk && item instanceof Job) {
                askJobViewModel.job = args.getParcelable(ARG_ASK_JOB);
                askJobViewModel.askStory = null;
            }
            askJobViewModel.isAskTextViewed = args.getBoolean(ARG_VIEWING_ASK_JOB);
        }
        if(getActivity() instanceof CustomTabActivityHelper.LaunchUrlCallback)
            launchUrlCallback = (CustomTabActivityHelper.LaunchUrlCallback) getActivity();
        if(launchUrlCallback != null && askJobViewModel.isAsk && askJobViewModel.askStory != null) {
                launchUrlCallback.onMayLaunchUrl(Uri.parse(Utils.toHackerNewsUrl(askJobViewModel.askStory.getId())), null);
        }
        if(launchUrlCallback != null && !askJobViewModel.isAsk && askJobViewModel.job != null) {
            List<Uri> uris = new ArrayList<>(2);
            uris.add(Uri.parse(askJobViewModel.job.getUrl()));
            uris.add(Uri.parse(Utils.toHackerNewsUrl(askJobViewModel.job.getId())));
            launchUrlCallback.onMayLaunchUrl(null, Utils.toCustomTabUriBundle(uris));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAskstoryBinding binding = FragmentAskstoryBinding.inflate(inflater, container, false);
        TextView titleText, userText, urlText, timeText, pointsText, numCommentsText;
        askJobStoryCard = binding.storyAskJobCard;
        commentsLayout = binding.storyAskJobCommentsLayout;
        noCommentsLayout = binding.storyAskJobNocommentsLayout;
        swipeRefreshLayout = binding.storyAskJobSwipeRefresh;
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = binding.storyAskJobCommentsList;
        titleText = binding.storyAskJobTitle;
        urlText = binding.storyAskJobUrl;
        userText = binding.storyAskJobUser;
        timeText = binding.storyAskJobTime;
        pointsText = binding.storyAskJobPoints;
        numCommentsText = binding.storyAskJobNumComments;
        askJobText = binding.storyAskJobText;
        ImageView jobIcon = binding.storyAskJobIcon;

        askJobStoryCard.setOnClickListener((v) -> {
            if(!askJobViewModel.isAskTextViewed) {
                showCommentsView(false);
                askJobViewModel.isAskTextViewed = true;
            }
        });

        commentsLayout.setOnClickListener((v) -> {
            if(askJobViewModel.isAskTextViewed) {
                showCommentsView(true);
                askJobViewModel.isAskTextViewed = false;
                askJobViewModel.lastCommentsRefreshTime = 0L;
                observeItem(true);
            }
        });

        if(askJobViewModel.isAsk && askJobViewModel.askStory != null) {
            titleText.setText(askJobViewModel.askStory.getTitle());
            urlText.setText(getString(R.string.item_base_url));
            userText.setText(askJobViewModel.askStory.getUser());
            timeText.setText(Utils.getAbbreviatedTimeSpan(askJobViewModel.askStory.getTime()));
            pointsText.setText(String.valueOf(askJobViewModel.askStory.getScore()));
            numCommentsText.setText(String.valueOf(askJobViewModel.askStory.getDescendants()));
            askJobText.setText(Utils.fromHtml(askJobViewModel.askStory.getText()));
            jobIcon.setVisibility(View.GONE);
        }
        if(!askJobViewModel.isAsk && askJobViewModel.job != null) {
            titleText.setText(askJobViewModel.job.getTitle());
            if(askJobViewModel.job.getUrl() == null || askJobViewModel.job.getUrl().isEmpty())
                urlText.setText(getString(R.string.item_base_url));
            else {
                Uri uri = Uri.parse(askJobViewModel.job.getUrl());
                urlText.setText(uri.getHost());
            }
            userText.setText(askJobViewModel.job.getUser());
            timeText.setText(Utils.getAbbreviatedTimeSpan(askJobViewModel.job.getTime()));
            pointsText.setText(String.valueOf(askJobViewModel.job.getScore()));
            numCommentsText.setText(String.valueOf(0));
            askJobText.setText(Utils.fromHtml(askJobViewModel.job.getText()));
            jobIcon.setVisibility(View.VISIBLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(askJobViewModel.commentsList != null) {
            adapter = new CommentsAdapter(askJobViewModel.commentsList);
            recyclerView.setAdapter(adapter);
        }
        if(!askJobViewModel.isAskTextViewed) {
            showCommentsView(true);
            if(askJobViewModel.isAsk)
                startObservingComments(askJobViewModel.askStory);
        } else
            showCommentsView(false);
        observeItem(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(askJobViewModel.isAsk)
            outState.putParcelable(ARG_ASK_JOB, askJobViewModel.askStory);
        else
            outState.putParcelable(ARG_ASK_JOB, askJobViewModel.job);
        outState.putBoolean(ARG_IS_ASK_OR_JOB, askJobViewModel.isAsk);
        outState.putBoolean(ARG_VIEWING_ASK_JOB, askJobViewModel.isAskTextViewed);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item_menu, menu);
        if(askJobViewModel.isAsk) {
            MenuItem item = menu.findItem(R.id.action_item_menu_article_link);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        WeakReference<Context> ref = new WeakReference<>(getContext());
        if(askJobViewModel.isAsk && askJobViewModel.askStory == null)
            return super.onOptionsItemSelected(item);
        if(!askJobViewModel.isAsk && askJobViewModel.job == null)
            return super.onOptionsItemSelected(item);
        String hnUrl;
        if(askJobViewModel.isAsk)
            hnUrl = Utils.toHackerNewsUrl(askJobViewModel.askStory.getId());
        else
            hnUrl = Utils.toHackerNewsUrl(askJobViewModel.job.getId());
        switch(id) {
            case R.id.action_item_menu_hackernews_link:
                CustomTabActivityHelper.openWebUrl(ref, hnUrl);
                return true;
            case R.id.action_item_menu_article_link:
                if(!askJobViewModel.isAsk) {
                    String url = askJobViewModel.job.getUrl();
                    CustomTabActivityHelper.openWebUrl(ref, url, hnUrl);
                }
                return true;
            case R.id.action_item_menu_share:
                return true;
            case R.id.action_articles_refresh:
                swipeRefreshLayout.setRefreshing(true);
                observeItem(true);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = null;
        if(getActivity() != null && (actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar()) != null) {
            if (askJobViewModel.isAsk)
                actionBar.setTitle("Ask");
            else
                actionBar.setTitle("Job");
        }
    }

    @Override
    public void onRefresh() {
        if(askJobViewModel.isAskTextViewed)
            observeItem(false);
        else
            observeItem(true);
    }

    private void observeItem(final boolean refreshComments) {
        if(askJobViewModel.isAsk)
            observeAskStory(refreshComments);
        else
            observeJob();
    }

    private void observeJob() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - askJobViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
            askJobViewModel.commentsFound = true;
            showTextNoComments();
            askJobViewModel.lastCommentsRefreshTime = currentTime;
            askJobViewModel.getJob().observe(getViewLifecycleOwner(), (job) -> {
                askJobViewModel.job = job;
                askJobViewModel.commentsFound = false;
                showTextNoComments();
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void observeAskStory(final boolean refreshComments) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - askJobViewModel.lastCommentsRefreshTime > Utils.CACHE_EXPIRATION) {
            askJobViewModel.commentsFound = true;
            showTextNoComments();
            askJobViewModel.lastCommentsRefreshTime = currentTime;
            askJobViewModel.getAskStory().observe(getViewLifecycleOwner(), new Observer<Story>() {
                @Override
                public void onChanged(Story story) {
                    askJobViewModel.askStory = story;
                    if(story.getKids() == null || story.getKids().length == 0) {
                        askJobViewModel.commentsFound = false;
                        showTextNoComments();
                    }
                    if(refreshComments) {
                        int size = askJobViewModel.commentsList.size();
                        askJobViewModel.commentsList.clear();
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
        askJobViewModel.getComment(id).observe(getViewLifecycleOwner(), new Observer<Comment>() {
            @Override
            public void onChanged(Comment comment) {
                // Avoid inserting duplicates
                if(!askJobViewModel.commentsList.contains(comment)) {
                    int pos = askJobViewModel.commentsList.size();
                    long idParent = comment.getParent();
                    if (idParent == askJobViewModel.askStory.getId()) {
                        comment.setLevel(1);
                        askJobViewModel.commentsList.add(comment);
                        if (adapter != null)
                            adapter.notifyItemInserted(pos);
                    } else {
                        Comment parent = new Comment(idParent);
                        int index = askJobViewModel.commentsList.indexOf(parent);
                        parent = askJobViewModel.commentsList.get(index);
                        comment.setLevel(parent.getLevel() + 1);
                        index += 1; //+1 after the parent
                        askJobViewModel.commentsList.add(index, comment);
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
            askJobText.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.INVISIBLE);
            noCommentsLayout.setVisibility(View.INVISIBLE);
            askJobText.setVisibility(View.VISIBLE);
        }
    }

    private void showTextNoComments() {
        if(!askJobViewModel.commentsFound)
            noCommentsLayout.setVisibility(View.VISIBLE);
        else
            noCommentsLayout.setVisibility(View.INVISIBLE);
    }
}
