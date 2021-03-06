package emilsoft.hackernews.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentHomeArticlesListItemBinding;
import emilsoft.hackernews.fragment.AskFragment;
import emilsoft.hackernews.fragment.HomeFragment;
import emilsoft.hackernews.fragment.JobFragment;
import emilsoft.hackernews.fragment.StoryFragment;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private int argViewStories;
    private List<? extends Item> stories;
    private Context context;

    public StoriesAdapter(List<? extends Item> stories, int argViewStories) {
        if (stories == null)
            this.stories = new ArrayList<>();
        else
            this.stories = stories;
        this.argViewStories = argViewStories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                FragmentHomeArticlesListItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false),
                onStoryClickListener, argViewStories);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = stories.get(position);
        String titleText = "", numCommentsText = "", urlText = "", pointsText = "", userText = "", timeText = "";
        if(holder.mItem instanceof Story) {
            Story mStory = (Story) holder.mItem;
            titleText = mStory.getTitle();
            numCommentsText = Integer.toString(mStory.getDescendants());
            if(mStory.getUrl() == null || mStory.getUrl().isEmpty())
                urlText = context.getString(R.string.item_base_url);
            else {
                Uri uri = Uri.parse(mStory.getUrl());
                urlText = uri.getHost();
            }
            pointsText = Integer.toString(mStory.getScore());
            userText = mStory.getUser();
            timeText = Utils.getAbbreviatedTimeSpan(mStory.getTime());
            holder.mJobIcon.setVisibility(View.GONE);
        } else if(holder.mItem instanceof Job) {
            Job mJob = (Job) holder.mItem;
            titleText = mJob.getTitle();
            numCommentsText = Integer.toString(0);
            if(mJob.getUrl() == null || mJob.getUrl().isEmpty())
                urlText = context.getString(R.string.item_base_url);
            else {
                Uri uri = Uri.parse(mJob.getUrl());
                urlText = uri.getHost();
            }
            pointsText = Integer.toString(mJob.getScore());
            userText = mJob.getUser();
            timeText = Utils.getAbbreviatedTimeSpan(mJob.getTime());
            holder.mJobIcon.setVisibility(View.VISIBLE);
        }
        holder.mTitle.setText(titleText);
        holder.mNumComments.setText(numCommentsText);
        holder.mUrl.setText(urlText);
        holder.mPoints.setText(pointsText);
        holder.mUser.setText(userText);
        holder.mTime.setText(timeText);
    }

    @Override
    public int getItemCount() {
        if (stories != null)
            return stories.size();
        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    private OnItemClickListener onStoryClickListener = new OnItemClickListener() {

        @Override
        public void onItemContentClick(View view, Item item) {
            switch (item.getType()) {
                case STORY_TYPE:
                    if(item instanceof Story) {
                        Story mStory = (Story) item;
                        if(Story.isAsk(mStory)) {
                            NavController navController = Navigation.findNavController(view);
                            Bundle args = new Bundle();
                            args.putParcelable(AskFragment.ARG_ITEM, mStory);
                            args.putBoolean(AskFragment.ARG_VIEWING_ASK, true);
                            //navController.navigate(R.id.action_nav_topstories_to_nav_ask, args);
                            HomeFragment.navigateToAsk(navController, argViewStories, args);
                        }
                        else {
                            openWebUrl(mStory.getId(), mStory.getUrl());
                        }
                    }
                    break;
                case JOB_TYPE:
                    if(item instanceof Job) {
                        Job mJob = (Job) item;
                        if(mJob.hasJobUrl()) {
                            openWebUrl(mJob.getId(), mJob.getUrl());
                        } else {
                            NavController navController = Navigation.findNavController(view);
                            Bundle args = new Bundle();
                            args.putParcelable(JobFragment.ARG_ITEM, mJob);
                            args.putBoolean(JobFragment.ARG_VIEWING_JOB, true);
                            //navController.navigate(R.id.action_nav_topstories_to_nav_ask, args);
                            HomeFragment.navigateToJob(navController, argViewStories, args);
                        }
                    }
                    break;
                case POLL_TYPE:
                case POLLOPT_TYPE:
                default: break;
            }
        }

        @Override
        public void onItemCommentsClick(View view, Item item) {
            //String url = mStory.getUrl();
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            if(item instanceof Story) {
                Story mStory = (Story) item;
                if (Story.isAsk(mStory)) {
                    args.putParcelable(AskFragment.ARG_ITEM, mStory);
                    args.putBoolean(AskFragment.ARG_VIEWING_ASK, false);
                    //navController.navigate(R.id.action_nav_topstories_to_nav_ask, args);
                    HomeFragment.navigateToAsk(navController, argViewStories, args);
                } else {
                    args.putParcelable(StoryFragment.ARG_ITEM, mStory);
                    //navController.navigate(R.id.action_nav_topstories_to_nav_story, args);
                    HomeFragment.navigateToStory(navController, argViewStories, args);
                }
            } else if(item instanceof Job) {
                Job mJob = (Job) item;
                args.putParcelable(JobFragment.ARG_ITEM, mJob);
                args.putBoolean(JobFragment.ARG_VIEWING_JOB, false);
                //navController.navigate(R.id.action_nav_newstories_to_nav_ask, args);
                HomeFragment.navigateToJob(navController, argViewStories, args);
            }
        }

        @Override
        public void onItemMenuClick(MenuItem menuItem, View view, Item item) {
            switch (menuItem.getItemId()) {
                case R.id.home_fragment_context_item_menu_hackernews_link:
                    WeakReference<Context> ref = new WeakReference<>(context);
                    String hackerNewsUrl = HackerNewsApi.HACKER_NEWS_BASE_URL + item.getId();
                    CustomTabActivityHelper.openWebUrl(ref, hackerNewsUrl);
                    break;
                case R.id.home_fragment_context_item_menu_article_link:
                    onItemContentClick(view, item);
                    break;
                case R.id.home_fragment_context_item_menu_article_comments:
                    onItemCommentsClick(view, item);
                    break;
                case R.id.home_fragment_context_item_menu_article_share:
                    //TODO implement share
                    break;
            }
        }

    };

    private void openWebUrl(long id, String url) {
        try {
            WeakReference<Context> ref = new WeakReference<>(context);
            // Utils.openWebUrl(ref, url);
            String hackerNewsUrl = Utils.toHackerNewsUrl(id);
            CustomTabActivityHelper.openWebUrl(ref, url, hackerNewsUrl);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "No Browser found to open link", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener, PopupMenu.OnMenuItemClickListener{

        final FragmentHomeArticlesListItemBinding mBinding;
        final ConstraintLayout mCommentsLayout;
        final TextView mTitle;
        final TextView mNumComments;
        final TextView mUrl;
        final TextView mPoints;
        final TextView mUser;
        final TextView mTime;
        final ImageView mJobIcon;
        final View mView;

        int argViewStories;
        Item mItem;
        private OnItemClickListener mListener;

        ViewHolder(@NonNull FragmentHomeArticlesListItemBinding binding, OnItemClickListener listener, int argViewStories) {
            super(binding.getRoot());
            mListener = listener;
            mBinding = binding;
            mView = binding.getRoot();
            this.argViewStories = argViewStories;
            mCommentsLayout = binding.articleCommentsLayout;
            mTitle = binding.articleTitle;
            mNumComments = binding.articleNumComments;
            mCommentsLayout.setOnClickListener(view -> {
                if(mListener != null)
                    mListener.onItemCommentsClick(view, mItem);
            });
            mUrl = binding.articleUrl;
            mPoints = binding.articlePoints;
            mUser = binding.articleUser;
            mTime = binding.articleTime;
            mJobIcon = binding.articleJobIcon;
            mBinding.getRoot().setOnClickListener(this);
            mBinding.getRoot().setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Handle to click in case of not a story with url
            if(mListener != null)
                mListener.onItemContentClick(v, mItem);
        }

        @Override
        public boolean onLongClick(View v) {
            PopupMenu popup = new PopupMenu(v.getContext(), v, GravityCompat.END);
            MenuInflater inflater = popup.getMenuInflater();
            popup.setOnMenuItemClickListener(this);
            inflater.inflate(R.menu.home_fragment_popup_menu, popup.getMenu());
            popup.show();
            return true; //consume event
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener == null)
                return false;
            mListener.onItemMenuClick(item, mView, mItem);
            return true;
        }
    }

    private interface OnItemClickListener {

        void onItemContentClick(View view, Item item);

        void onItemCommentsClick(View view, Item item);

        void onItemMenuClick(MenuItem menuItem, View view, Item item);

    }
}
