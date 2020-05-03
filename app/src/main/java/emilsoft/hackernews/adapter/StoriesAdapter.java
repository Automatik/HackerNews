package emilsoft.hackernews.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import emilsoft.hackernews.fragment.AskJobFragment;
import emilsoft.hackernews.fragment.StoryFragment;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private List<? extends Item> stories;
    private Context context;

    public StoriesAdapter(List<? extends Item> stories) {
        if (stories == null)
            this.stories = new ArrayList<>();
        else
            this.stories = stories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                FragmentHomeArticlesListItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false),
                onStoryClickListener);
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
        public void onStoryClick(String url, long storyId) {
            try {
                WeakReference<Context> ref = new WeakReference<>(context);
//                Utils.openWebUrl(ref, url);
                String hackerNewsUrl = HackerNewsApi.HACKER_NEWS_BASE_URL + storyId;
                CustomTabActivityHelper.openWebUrl(ref, url, hackerNewsUrl);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "No Browser found to open link", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onAskClick(View view, Story askStory) {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putParcelable(AskJobFragment.ARG_ASK_JOB, askStory);
            args.putBoolean(AskJobFragment.ARG_VIEWING_ASK_JOB, true);
            args.putBoolean(AskJobFragment.ARG_IS_ASK_OR_JOB, true);
            navController.navigate(R.id.action_nav_home_to_nav_ask, args);
        }

        @Override
        public void onJobClick(View view, Job job) {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putParcelable(AskJobFragment.ARG_ASK_JOB, job);
            args.putBoolean(AskJobFragment.ARG_VIEWING_ASK_JOB, true);
            args.putBoolean(AskJobFragment.ARG_IS_ASK_OR_JOB, false);
            navController.navigate(R.id.action_nav_home_to_nav_ask, args);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final FragmentHomeArticlesListItemBinding mBinding;
        final ConstraintLayout mCommentsLayout;
        final TextView mTitle;
        final TextView mNumComments;
        final TextView mUrl;
        final TextView mPoints;
        final TextView mUser;
        final TextView mTime;
        final ImageView mJobIcon;

        Item mItem;
        private OnItemClickListener mListener;

        ViewHolder(@NonNull FragmentHomeArticlesListItemBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            mListener = listener;
            mBinding = binding;
            mCommentsLayout = binding.articleCommentsLayout;
            mTitle = binding.articleTitle;
            mNumComments = binding.articleNumComments;
            mCommentsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //String url = mStory.getUrl();
                    NavController navController = Navigation.findNavController(view);
                    Bundle args = new Bundle();
                    if(mItem instanceof Story) {
                        Story mStory = (Story) mItem;
                        if (Story.isAsk(mStory)) {
                            args.putParcelable(AskJobFragment.ARG_ASK_JOB, mStory);
                            args.putBoolean(AskJobFragment.ARG_VIEWING_ASK_JOB, false);
                            args.putBoolean(AskJobFragment.ARG_IS_ASK_OR_JOB, true);
                            navController.navigate(R.id.action_nav_home_to_nav_ask, args);
                        } else {
                            args.putParcelable(StoryFragment.ARG_STORY, mStory);
                            navController.navigate(R.id.action_nav_home_to_nav_story, args);
                        }
                    } else if(mItem instanceof Job) {
                        Job mJob = (Job) mItem;
                        args.putParcelable(AskJobFragment.ARG_ASK_JOB, mJob);
                    }
                }
            });
            mUrl = binding.articleUrl;
            mPoints = binding.articlePoints;
            mUser = binding.articleUser;
            mTime = binding.articleTime;
            mJobIcon = binding.articleJobIcon;
            mBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Handle to click in case of not a story with url
            switch (mItem.getType()) {
                case STORY_TYPE:
                    if(mItem instanceof Story) {
                        Story mStory = (Story) mItem;
                        if(Story.isAsk(mStory))
                            mListener.onAskClick(v, mStory);
                        else
                            mListener.onStoryClick(mStory.getUrl(), mStory.getId());
                    }
                    break;
                case JOB_TYPE:
                    if(mItem instanceof Job) {
                        Job mJob = (Job) mItem;
                        mListener.onJobClick(v, mJob);
                    }
                    break;
                case POLL_TYPE:
                default: break;
            }
        }

    }

    private interface OnItemClickListener{

        void onStoryClick(String url, long storyId);

        void onAskClick(View view, Story askStory);

        void onJobClick(View view, Job job);

    }
}
