package emilsoft.hackernews.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentHomeArticlesListItemBinding;
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
        if(holder.mStory.getUrl() == null)
            //Handle on Click
            //Url would be news.ycombinator.com
//            holder.mUrl.setVisibility(View.GONE);
            holder.mUrl.setText("news.ycombinator.com");
        else
            holder.mUrl.setText(holder.mStory.getUrl());
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

    private OnStoryClickListener onStoryClickListener = new OnStoryClickListener() {
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
        private OnStoryClickListener mListener;

        public ViewHolder(@NonNull FragmentHomeArticlesListItemBinding binding, OnStoryClickListener listener) {
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
                    args.putParcelable(StoryFragment.ARG_STORY, mStory);
                    navController.navigate(R.id.action_nav_home_to_nav_story, args);
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
            //TODO Handle to click in case of not a story with url
            mListener.onStoryClick(mUrl.getText().toString(), mStory.getId());
        }

    }

    private interface OnStoryClickListener{

        void onStoryClick(String url, long storyId);

    }
}
