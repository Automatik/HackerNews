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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.databinding.CommentsListItemBinding;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private HashMap<Long, List<Comment>> collapsedParentComments;
    private HashMap<Long, Long> collapsedChildren;
    private LinkedList<Comment> commentsList;
    private int[] colorCodes;
    private int levelStartMargin;
    private Context context;


    public CommentsAdapter(LinkedList<Comment> commentsList, HashMap<Long, List<Comment>> collapsedParentComments,
                           HashMap<Long, Long> collapsedChildren) {
        if (commentsList == null)
            commentsList = new LinkedList<>();
        this.commentsList = commentsList;
        if (collapsedParentComments == null)
            collapsedParentComments = new HashMap<>();
        this.collapsedParentComments = collapsedParentComments;
        if (collapsedChildren == null)
            collapsedChildren = new HashMap<>();
        this.collapsedChildren = collapsedChildren;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(CommentsListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false),
                collapseCommentsListener,
                false
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mComment = commentsList.get(position);
        holder.mComment.setIsCollapsed(collapsedParentComments.containsKey(holder.mComment.getId()));
//        holder.isCollapsed = holder.mComment.isCollapsed();
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

        if(holder.mComment.isCollapsed()) {
            holder.mCollapseText.setText(R.string.comment_expand_text);
            holder.mCollapseIcon.setImageDrawable(context.getDrawable(R.drawable.ic_expand_more_24dp));
        } else {
            holder.mCollapseText.setText(R.string.comment_collapse_text);
            holder.mCollapseIcon.setImageDrawable(context.getDrawable(R.drawable.ic_expand_less_24dp));
        }
    }

    @Override
    public int getItemCount() {
        if(commentsList != null)
            return commentsList.size();
        return 0;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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

    private CollapseCommentsListener collapseCommentsListener = new CollapseCommentsListener() {
        @Override
        public void onCollapse(Comment comment) {
            final int index = commentsList.indexOf(comment);
            final int level = comment.getLevel();
            if(index == -1) return;
            int i = index + 1;
//            final List<Comment> childs = new LinkedList<>();
//            Observable.fromIterable(commentsList.subList(i, commentsList.size()))
//                    .takeWhile(c -> c.getLevel() > level)
//                    .toList()
//                    .subscribe(comments -> {
//                        childs.addAll(comments);
//                        commentsList.subList(index + 1, index + 1 + childs.size()).clear();
//                        notifyItemRangeRemoved(index + 1, childs.size());
//                        collapsedComments.put(comment.getId(), childs);
//                    })
//                    .dispose();
            //Without using RxJava

            List<Comment> childs = new LinkedList<>();
            Comment temp;
            while (i < commentsList.size() && (temp = commentsList.get(i)).getLevel() > level) {
                childs.add(temp);
                i++;
            }
            commentsList.subList(index + 1, index + 1 + childs.size()).clear();
            notifyItemRangeRemoved(index + 1, childs.size());
            collapsedParentComments.put(comment.getId(), childs);
//            collapsedChildren.put(comment.getId(), comment.getId());
        }

        @Override
        public void onExpand(Comment comment) {
            long id = comment.getId();
            int index = commentsList.indexOf(comment);
            if(index == -1) return;
            List<Comment> childs = collapsedParentComments.get(id);
            if(childs == null) return;
            commentsList.addAll(index + 1, childs);
            notifyItemRangeInserted(index + 1, childs.size());
            collapsedParentComments.remove(id);
            collapsedChildren.remove(id);
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {

        final CollapseCommentsListener listener;
        final CommentsListItemBinding mBinding;
        final TextView mTime;
        final TextView mUser;
        final TextView mText;
        final TextView mCollapseText;
        final View mLevel;
        final ImageView mCollapseIcon;
        Comment mComment;


        ViewHolder(@NonNull CommentsListItemBinding binding, CollapseCommentsListener listener, boolean startCollapsed) {
            super(binding.getRoot());
            mBinding = binding;
            mLevel = binding.commentLevel;
            mTime = binding.commentTime;
            mUser = binding.commentUser;
            mText = binding.commentText;
            mCollapseText = binding.commentExpandText;
            mCollapseIcon = binding.commentExpandImageview;
            this.listener = listener;
            View.OnClickListener collapseIconClickListener = v -> {
                if(listener != null) {
                    if (mComment.isCollapsed()) {
                        listener.onExpand(mComment);
                        mCollapseText.setText(R.string.comment_collapse_text);
                        mCollapseIcon.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_expand_less_24dp));
                    }
                    else {
                        listener.onCollapse(mComment);
                        mCollapseText.setText(R.string.comment_expand_text);
                        mCollapseIcon.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_expand_more_24dp));
                    }
                }
                mComment.setIsCollapsed(!mComment.isCollapsed());
            };
            mCollapseText.setOnClickListener(collapseIconClickListener);
            mCollapseIcon.setOnClickListener(collapseIconClickListener);
//            mComment.setIsCollapsed(startCollapsed);
        }

    }

    private interface CollapseCommentsListener {

        void onCollapse(Comment comment);

        void onExpand(Comment comment);

    }

}
