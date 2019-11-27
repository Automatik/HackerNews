package emilsoft.hackernews.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.fragment.StoryFragment;
import emilsoft.hackernews.fragment.WebFragment;

public class FragmentAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;
    private Story mStory;

    public FragmentAdapter(Fragment fragment, Story story) {
        super(fragment); //The Fragment Activity
        mStory = story;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return WebFragment.newInstance(mStory);
            case 1: return StoryFragment.newInstance(mStory);
            default: return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
