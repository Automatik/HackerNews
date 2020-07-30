package emilsoft.hackernews.fragment;

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

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Item;
import emilsoft.hackernews.api.ItemResponse;
import emilsoft.hackernews.api.Story;
import emilsoft.hackernews.connectivity.ConnectionSnackbar;
import emilsoft.hackernews.databinding.FragmentItemBinding;

public class AskFragment extends BaseItemFragment {

    public static final String ARG_VIEWING_ASK = "argument_is_ask_text_viewed";

    private CardView itemCard;
    private ConstraintLayout itemCommentsLayout;

    private boolean isAskTextViewed;

    public static StoryFragment newInstance(Story askStory, boolean isAskTextViewed) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, askStory);
        args.putBoolean(ARG_VIEWING_ASK, isAskTextViewed);
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
            isAskTextViewed = args.getBoolean(ARG_VIEWING_ASK);
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
            if(!isAskTextViewed) {
                showCommentsView(false);
                isAskTextViewed = true;
            }
        }));

        itemCommentsLayout.setOnClickListener((v -> {
            if(isAskTextViewed) {
                showCommentsView(true);
                isAskTextViewed = false;
                itemViewModel.lastCommentsRefreshTime = 0L;
                observeItem(true);
            }
        }));

        if(itemViewModel.item != null && itemViewModel.item instanceof Story) {
            Story askStory = (Story) itemViewModel.item;
            titleText.setText(askStory.getTitle());
            urlText.setText(getString(R.string.item_base_url));
            pointsText.setText(String.valueOf(askStory.getScore()));
            numCommentsText.setText(String.valueOf(askStory.getDescendants()));
            itemText.setText(Utils.fromHtml(askStory.getText()));
            jobIcon.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO Move this snippet to createView?
        if(isAskTextViewed)
            showCommentsView(false);
        else {
            showCommentsView(true);
            if(itemViewModel.item instanceof Story)
                startObservingComments(((Story) itemViewModel.item).getKids());
        }
        observeItem(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_VIEWING_ASK, isAskTextViewed);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_item_menu_article_link);
        item.setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = null;
        if(getActivity() != null && (actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar()) != null)
            actionBar.setTitle("Ask");
    }

    @Override
    protected void preFetchUrl() {
        if(launchUrlCallback != null && itemViewModel.item != null && itemViewModel.item instanceof Story) {
            Story askStory = (Story) itemViewModel.item;
            launchUrlCallback.onMayLaunchUrl(Uri.parse(Utils.toHackerNewsUrl(askStory.getId())), null);
        }
    }

    @Override
    protected Observer<ItemResponse<? extends Item>> getItemObserver(boolean refreshComments) {
        return new Observer<ItemResponse<? extends Item>>() {
            @Override
            public void onChanged(ItemResponse<? extends Item> response) {
                if(response.isSuccess()) {
                    Item item = response.getData();
                    if(item instanceof Story) {
                        Story askStory = (Story) item;
                        if(askStory.getKids() == null || askStory.getKids().length == 0) {
                            itemViewModel.commentsFound = false;
                            showTextNoComments();
                        }
                        if(refreshComments) {
                            int size = itemViewModel.commentsList.size();
                            itemViewModel.commentsList.clear();
//                          int size = itemViewModel.multiLevelData.itemsSize();
//                          itemViewModel.multiLevelData.clear();
                            if(adapter != null)
                                adapter.notifyItemRangeRemoved(0, size);
                            startObservingComments(askStory.getKids());
                        }
                    }
                } else {
                    String message = Utils.getMessageErrorFromRetrofitException(response.getError());
                    ConnectionSnackbar.showErrorMessageSnackbar(getView(), message);
                }
            }
        };
    }

    @Override
    public void onRefresh() {
        if(isAskTextViewed)
            observeItem(false);
        else
            observeItem(true);
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
