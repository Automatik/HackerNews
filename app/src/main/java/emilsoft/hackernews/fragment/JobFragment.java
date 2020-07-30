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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.ItemResponse;
import emilsoft.hackernews.api.Job;
import emilsoft.hackernews.customtabs.CustomTabActivityHelper;
import emilsoft.hackernews.databinding.FragmentItemBinding;

public class JobFragment extends BaseItemFragment {

    public static final String ARG_VIEWING_JOB = "argument_is_job_text_viewed";

    private CardView itemCard;
    private ConstraintLayout itemCommentsLayout;

    private boolean isJobTextViewed;

    public static StoryFragment newInstance(Job job, boolean isJobTextViewed) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, job);
        args.putBoolean(ARG_VIEWING_JOB, isJobTextViewed);
        StoryFragment fragment = new StoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(savedInstanceState != null)
            args = savedInstanceState;
        if(args != null)
            isJobTextViewed = args.getBoolean(ARG_VIEWING_JOB);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        FragmentItemBinding binding = FragmentItemBinding.bind(root);
        itemCard = binding.itemCard;
        itemCommentsLayout = binding.itemCommentsLayout;
        ImageView jobIcon = binding.itemJobIcon;

        itemCard.setOnClickListener((v -> {
            if(!isJobTextViewed) {
                showCommentsView(false);
                isJobTextViewed = true;
            }
        }));

        itemCommentsLayout.setOnClickListener((v -> {
            if(isJobTextViewed) {
                showCommentsView(true);
                isJobTextViewed = false;
                itemViewModel.lastCommentsRefreshTime = 0L;
                observeItem(true);
            }
        }));

        if(itemViewModel.item != null && itemViewModel.item instanceof Job) {
            Job job = (Job) itemViewModel.item;
            titleText.setText(job.getTitle());
            if(!job.hasJobUrl())
                urlText.setText(getString(R.string.item_base_url));
            else {
                Uri uri = Uri.parse(job.getUrl());
                urlText.setText(uri.getHost());
            }
            pointsText.setText(String.valueOf(job.getScore()));
            numCommentsText.setText(String.valueOf(0));
            itemText.setText(Utils.fromHtml(job.getText()));
            jobIcon.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO Move this snippet to createView?
        if(isJobTextViewed)
            showCommentsView(false);
        else {
            showCommentsView(true);
        }
        observeItem(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_VIEWING_JOB, isJobTextViewed);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(itemViewModel.item instanceof Job && !((Job) itemViewModel.item).hasJobUrl()) {
            MenuItem item = menu.findItem(R.id.action_item_menu_article_link);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(!(itemViewModel.item instanceof Job))
            return super.onOptionsItemSelected(item);
        switch(id) {
            case R.id.action_item_menu_article_link:
                Job job = (Job) itemViewModel.item;
                if(job.hasJobUrl()) {
                    WeakReference<Context> ref = new WeakReference<>(getContext());
                    String url = job.getUrl();
                    String hnUrl = Utils.toHackerNewsUrl(job.getId());
                    CustomTabActivityHelper.openWebUrl(ref, url, hnUrl);
                }
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = null;
        if(getActivity() != null && (actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar()) != null)
            actionBar.setTitle("Job");
    }

    @Override
    protected void preFetchUrl() {
        if(launchUrlCallback != null && itemViewModel.item != null && itemViewModel.item instanceof Job) {
            Job job = (Job) itemViewModel.item;
            List<Uri> uris = new ArrayList<>(2);
            uris.add(Uri.parse(job.getUrl()));
            uris.add(Uri.parse(Utils.toHackerNewsUrl(job.getId())));
            launchUrlCallback.onMayLaunchUrl(null, Utils.toCustomTabUriBundle(uris));
        }
    }

    @Override
    protected Observer<ItemResponse<? extends Item>> getItemObserver(boolean refreshComments) {
        return new Observer<ItemResponse<? extends Item>>() {
            @Override
            public void onChanged(ItemResponse<? extends Item> response) {
                if(response.isSuccess()) {
                    Item item = response.getData();
                    if(item instanceof Job) {
                        Job job = (Job) item;
                        itemViewModel.commentsFound = false;
                        showTextNoComments();
                    }
                }
            }
        };
    }

    @Override
    public void onRefresh() {
        observeItem(false);
    }

    private void showCommentsView(boolean showComments) {
        if(showComments) {
            itemText.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.INVISIBLE);
            noCommentsLayout.setVisibility(View.INVISIBLE);
            itemText.setVisibility(View.VISIBLE);
        }
    }
}
