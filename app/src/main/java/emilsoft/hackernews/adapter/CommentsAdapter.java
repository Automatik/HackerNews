package emilsoft.hackernews.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.LinkedList;

import emilsoft.hackernews.BuildConfig;
import emilsoft.hackernews.MainActivity;
import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Comment;
import emilsoft.hackernews.databinding.CommentsListItemBinding;

import static emilsoft.hackernews.MainActivity.TAG;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private LinkedList<Comment> commentsList;
    private int[] colorCodes;
    private int levelStartMargin;
    private Context context;


    public CommentsAdapter(LinkedList<Comment> commentsList) {
        this.commentsList = commentsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(CommentsListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mComment = commentsList.get(position);
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

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public final CommentsListItemBinding mBinding;
        public final TextView mTime;
        public final TextView mUser;
        public final TextView mText;
        public final View mLevel;
        public Comment mComment;


        public ViewHolder(@NonNull CommentsListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mLevel = binding.commentLevel;
            mTime = binding.commentTime;
            mUser = binding.commentUser;
            mText = binding.commentText;
        }
    }

}
