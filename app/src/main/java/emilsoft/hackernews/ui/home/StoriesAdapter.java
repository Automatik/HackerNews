package emilsoft.hackernews.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Story;

import static emilsoft.hackernews.MainActivity.TAG;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private List<Story> stories;

    public StoriesAdapter(List<Story> stories) {
        if(stories == null)
            this.stories = new ArrayList<>();
        else
            this.stories = stories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_home_articles_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mStory = stories.get(position);
        holder.mTitle.setText(holder.mStory.getTitle());
        holder.mNumComments.setText(holder.mStory.getDescendants() + " C");
        holder.mUrl.setText(holder.mStory.getUrl());
        holder.mPoints.setText(holder.mStory.getScore() + " p");
        holder.mUser.setText(holder.mStory.getUser());
        holder.mTime.setText(Utils.getAbbreviatedTimeSpan(holder.mStory.getTime()));
    }

    @Override
    public int getItemCount() {
        if(stories != null)
            return stories.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        public final TextView mTitle;
        public final TextView mNumComments;
        public final TextView mUrl;
        public final TextView mPoints;
        public final TextView mUser;
        public final TextView mTime;
        public Story mStory;

        public ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.article_title);
            mNumComments = view.findViewById(R.id.article_num_comments);
            mUrl = view.findViewById(R.id.article_url);
            mPoints = view.findViewById(R.id.article_points);
            mUser = view.findViewById(R.id.article_user);
            mTime = view.findViewById(R.id.article_time);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //String url = mStory.getUrl();
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putParcelable(StoryFragment.ARG_STORY, mStory);
//            args.putString(StoryFragment.ARG_URL, url);
//            args.putString(StoryFragment.ARG_TITLE, mStory.getTitle());
//            args.putString(StoryFragment.ARG_USER, mStory.getUser());
//            args.putInt(StoryFragment.ARG_POINTS, mStory.getScore());
//            args.putLong(StoryFragment.ARG_TIME, mStory.getTime());
//            args.putInt(StoryFragment.ARG_NUM_COMMENTS, mStory.getDescendants());
//            args.putLongArray(StoryFragment.ARG_COMMENTS, mStory.getKids());
            navController.navigate(R.id.action_nav_home_to_nav_story, args);
        }
    }
}
