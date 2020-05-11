package emilsoft.hackernews.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import emilsoft.hackernews.R;
import emilsoft.hackernews.adapter.FragmentAdapter;
import emilsoft.hackernews.api.HackerNewsApi;
import emilsoft.hackernews.api.Story;

import static emilsoft.hackernews.MainActivity.TAG;

public class ItemFragment extends Fragment {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private Story story;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        if(args != null)
            story = args.getParcelable(StoryFragment.ARG_ITEM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_item, container, false);
        viewPager = view.findViewById(R.id.viewpager);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        addFragments();
        viewPager.setCurrentItem(0);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
//        viewPager.setUserInputEnabled(false); //disable swipe. Could help avoiding lag in webview
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_item_menu_hackernews_link: openWebUrl(HackerNewsApi.HACKER_NEWS_BASE_URL+story.getId()); return true;
            case R.id.action_item_menu_article_link: openWebUrl(story.getUrl()); return true;
            case R.id.action_item_menu_share: return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void addFragments() {
        FragmentAdapter adapter = new FragmentAdapter(this, story);
        Fragment webFragment = adapter.createFragment(0);
        //webFragment.setArguments(args);
        Fragment storyFragment = adapter.createFragment(1);
        //storyFragment.setArguments(args);
        viewPager.setAdapter(adapter);
    }

    private void openWebUrl(String url) {
        if(getActivity() == null)
            return;
        Uri uri = Uri.parse(url);
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        //Set desired colors
        intentBuilder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(getActivity(), uri);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.viewpager_bottom_nav_article:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.viewpager_bottom_nav_comments:
                    viewPager.setCurrentItem(1);
                    return true;
                default: return false;
            }
        }
    };

    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    bottomNavigationView.setSelectedItemId(R.id.viewpager_bottom_nav_article);
                    break;
                case 1:
                    bottomNavigationView.setSelectedItemId(R.id.viewpager_bottom_nav_comments);
                    break;
                default: super.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };
}
