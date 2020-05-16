package emilsoft.hackernews.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.databinding.CommentsListItemBinding;
import emilsoft.hackernews.expandablerecyclerview.MultiLevelAdapter;
import emilsoft.hackernews.expandablerecyclerview.MultiLevelRecyclerView;
import emilsoft.hackernews.expandablerecyclerview.models.RecyclerViewItem;

public class MultiLevelCommentsAdapter extends MultiLevelAdapter<MultiLevelCommentsAdapter.ViewHolder> {

    private int[] colorCodes;
    private int levelStartMargin;
    private Context context;
    private MultiLevelRecyclerView recyclerView;

    public MultiLevelCommentsAdapter(LinkedList<Comment> commentsList) {
        super(commentsList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(CommentsListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false),
                collapseCommentListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mComment = (Comment) recyclerViewItemList.get(position);

        holder.mTime.setText(Utils.getAbbreviatedTimeSpan(holder.mComment.getTime()));

        if (holder.mComment.isDeleted()) {
            // setup STRIKE_THRU_TEXT_FLAG flag if current flags not contains it
            holder.mTime.setPaintFlags(holder.mTime.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mText.setText("");
            holder.mUser.setText("");
        }
        else {
            // Remove strike through if set
            if((holder.mTime.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == Paint.STRIKE_THRU_TEXT_FLAG)
                holder.mTime.setPaintFlags(holder.mTime.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mUser.setText(holder.mComment.getUser());
            holder.mText.setText(Utils.fromHtml(holder.mComment.getText()));
        }
        if (holder.mComment.isDead()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mUser.setTextAppearance(R.style.TextAppearance_AppCompat_Small_Disable);
                holder.mText.setTextAppearance(R.style.TextAppearance_AppCompat_Body1_Disable);
            } else {
                holder.mUser.setTextAppearance(context, R.style.TextAppearance_AppCompat_Small_Disable);
                holder.mText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Body1_Disable);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mUser.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
                holder.mText.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
            } else {
                holder.mUser.setTextAppearance(context, R.style.TextAppearance_AppCompat_Small);
                holder.mText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Body1);
            }
        }

        int color = getCommentColor(holder.mComment.getLevel() - 1);
        if(color != 0) {
            holder.mLevel.setVisibility(View.VISIBLE);
            holder.mLevel.setBackgroundColor(color);
        } else
            holder.mLevel.setVisibility(View.GONE);
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                holder.mBinding.getRoot().getLayoutParams();
        params.leftMargin = holder.mComment.getLevel() * levelStartMargin;
        holder.mBinding.getRoot().setLayoutParams(params);

        if(holder.mComment.isExpanded()) {
            holder.mCollapseText.setText(R.string.comment_collapse_text);
            holder.mCollapseIcon.setImageDrawable(context.getDrawable(R.drawable.ic_expand_less_24dp));
        } else {
            holder.mCollapseText.setText(R.string.comment_expand_text);
            holder.mCollapseIcon.setImageDrawable(context.getDrawable(R.drawable.ic_expand_more_24dp));
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = (MultiLevelRecyclerView) recyclerView;
        context = recyclerView.getContext();
        colorCodes = context.getResources().getIntArray(R.array.color_codes);
        levelStartMargin = (int) (context.getResources().getDimension(R.dimen.comment_level_start_left_margin)
                / context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        colorCodes = null;
    }

    private int getCommentColor(int level) {
        return (colorCodes != null) ? colorCodes[level % colorCodes.length] : 0;
    }

    private CollapseCommentListener collapseCommentListener = position -> {
        recyclerView.toggleItemsGroup(position);
    };

    public static class ViewHolder extends MultiLevelRecyclerView.ViewHolder {

        final CollapseCommentListener listener;
        final CommentsListItemBinding mBinding;
        final TextView mTime;
        final TextView mUser;
        final TextView mText;
        final TextView mCollapseText;
        final View mLevel;
        final ImageView mCollapseIcon;
        Comment mComment;

        public ViewHolder(@NonNull CommentsListItemBinding binding, CollapseCommentListener listener) {
            super(binding.getRoot());
            this.listener = listener;
            mBinding = binding;
            mLevel = binding.commentLevel;
            mTime = binding.commentTime;
            mUser = binding.commentUser;
            mText = binding.commentText;
            mCollapseText = binding.commentExpandText;
            mCollapseIcon = binding.commentExpandImageview;
            View.OnClickListener onClickListener = v -> {
                if(mComment.isExpanded()) {
                    mCollapseText.setText(R.string.comment_expand_text);
                    mCollapseIcon.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_expand_more_24dp));
                } else {
                    mCollapseText.setText(R.string.comment_collapse_text);
                    mCollapseIcon.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_expand_less_24dp));
                }
                if(listener != null)
                    listener.toggleComment(getAdapterPosition());
            };
            mCollapseText.setOnClickListener(onClickListener);
            mCollapseIcon.setOnClickListener(onClickListener);
        }
    }

    private interface CollapseCommentListener {

        void toggleComment(int position);
    }

}
