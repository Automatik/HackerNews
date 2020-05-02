package emilsoft.hackernews.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.api.Type;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentHomeArticlesListItemBinding;
import emilsoft.hackernews.fragment.AskFragment;
import emilsoft.hackernews.fragment.StoryFragment;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private List<Story> stories;
    private Context context;

    public StoriesAdapter(List<Story> stories) {
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
        holder.mStory = stories.get(position);
        holder.mTitle.setText(holder.mStory.getTitle());
        holder.mNumComments.setText(Integer.toString(holder.mStory.getDescendants()));
        // Fix for stories that don't have url like AskHN
        if(holder.mStory.getUrl() == null || holder.mStory.getUrl().isEmpty())
            //Handle on Click
            //Url would be news.ycombinator.com
//            holder.mUrl.setVisibility(View.GONE);
            holder.mUrl.setText(R.string.item_base_url);
        else {

            Uri uri = Uri.parse(holder.mStory.getUrl());
            holder.mUrl.setText(uri.getHost());
        }
        holder.mPoints.setText(Integer.toString(holder.mStory.getScore()));
        holder.mUser.setText(holder.mStory.getUser());
        holder.mTime.setText(Utils.getAbbreviatedTimeSpan(holder.mStory.getTime()));
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
            args.putParcelable(AskFragment.ARG_ASK, askStory);
            args.putBoolean(AskFragment.ARG_VIEWING_ASK, true);
            navController.navigate(R.id.action_nav_home_to_nav_ask, args);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final FragmentHomeArticlesListItemBinding mBinding;
        public final ConstraintLayout mCommentsLayout;
        public final TextView mTitle;
        public final TextView mNumComments;
        public final TextView mUrl;
        public final TextView mPoints;
        public final TextView mUser;
        public final TextView mTime;
        public Story mStory;
        private OnItemClickListener mListener;

        public ViewHolder(@NonNull FragmentHomeArticlesListItemBinding binding, OnItemClickListener listener) {
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
                    if(Story.isAsk(mStory)) {
                        args.putParcelable(AskFragment.ARG_ASK, mStory);
                        args.putBoolean(AskFragment.ARG_VIEWING_ASK, false);
                        navController.navigate(R.id.action_nav_home_to_nav_ask, args);
                    } else {
                        args.putParcelable(StoryFragment.ARG_STORY, mStory);
                        navController.navigate(R.id.action_nav_home_to_nav_story, args);
                    }
                }
            });
            mUrl = binding.articleUrl;
            mPoints = binding.articlePoints;
            mUser = binding.articleUser;
            mTime = binding.articleTime;
            mBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Handle to click in case of not a story with url
            switch (mStory.getType()) {
                case STORY_TYPE:
                    if(Story.isAsk(mStory))
                        mListener.onAskClick(v, mStory);
                    else
                        mListener.onStoryClick(mStory.getUrl(), mStory.getId());
                    break;
                case JOB_TYPE:
                case POLL_TYPE:
                default: break;
            }
        }

    }

    private interface OnItemClickListener{

        void onStoryClick(String url, long storyId);

        void onAskClick(View view, Story askStory);

    }
}
