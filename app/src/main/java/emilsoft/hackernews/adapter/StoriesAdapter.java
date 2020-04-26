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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.fragment.StoryFragment;

import static emilsoft.hackernews.MainActivity.TAG;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_home_articles_list_item, parent, false);
        return new ViewHolder(view, onStoryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mStory = stories.get(position);
        holder.mTitle.setText(holder.mStory.getTitle());
        holder.mNumComments.setText(holder.mStory.getDescendants() + " C");
        // Fix for stories that don't have url like AskHN
        if(holder.mStory.getUrl() == null)
            //Url would be news.ycombinator.com
            holder.mUrl.setVisibility(View.GONE); //Handle on Click
        else
            holder.mUrl.setText(holder.mStory.getUrl());
        holder.mPoints.setText(holder.mStory.getScore() + " p");
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
        public void onStoryClick(String url) {
            try {
                WeakReference<Context> ref = new WeakReference<>(context);
//                Utils.openWebUrl(ref, url);

                CustomTabActivityHelper.openWebUrl(ref, url);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "No Browser found to open link", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        public final TextView mTitle;
        public final TextView mNumComments;
        public final TextView mUrl;
        public final TextView mPoints;
        public final TextView mUser;
        public final TextView mTime;
        public Story mStory;
        private OnStoryClickListener mListener;

        public ViewHolder(@NonNull View view, OnStoryClickListener listener) {
            super(view);
            mListener = listener;
            mView = view;
            mTitle = view.findViewById(R.id.article_title);
            mNumComments = view.findViewById(R.id.article_num_comments);
            mNumComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //String url = mStory.getUrl();
                    NavController navController = Navigation.findNavController(view);
                    Bundle args = new Bundle();
                    args.putParcelable(StoryFragment.ARG_STORY, mStory);
                    navController.navigate(R.id.action_nav_home_to_nav_story, args);
                }
            });
            mUrl = view.findViewById(R.id.article_url);
            mPoints = view.findViewById(R.id.article_points);
            mUser = view.findViewById(R.id.article_user);
            mTime = view.findViewById(R.id.article_time);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //TODO Handle to click in case of not a story with url
            mListener.onStoryClick(mUrl.getText().toString());
        }

    }

    public interface OnStoryClickListener{

        void onStoryClick(String url);

    }
}
