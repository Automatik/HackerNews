package emilsoft.hackernews.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.LinkedList;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Comment;

import static emilsoft.hackernews.MainActivity.TAG;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private LinkedList<Comment> commentsList;
    private int[] colorCodes;
    private int levelStartMargin;

    public CommentsAdapter(LinkedList<Comment> commentsList) {
        this.commentsList = commentsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comments_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mComment = commentsList.get(position);
        holder.mTime.setText(Utils.getAbbreviatedTimeSpan(holder.mComment.getTime()));
        holder.mUser.setText(holder.mComment.getUser());
        holder.mText.setText(Utils.fromHtml(holder.mComment.getText()));
        int color = getCommentColor(holder.mComment.getLevel() - 1);
        if(color != 0) {
            holder.mLevel.setVisibility(View.VISIBLE);
            holder.mLevel.setBackgroundColor(color);
        } else
            holder.mLevel.setVisibility(View.GONE);
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                holder.mView.getLayoutParams();
        params.leftMargin = holder.mComment.getLevel() * levelStartMargin;
        holder.mView.setLayoutParams(params);
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
        Context context = recyclerView.getContext();
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mTime;
        public final TextView mUser;
        public final TextView mText;
        public final View mLevel;
        public Comment mComment;


        public ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mLevel = view.findViewById(R.id.comment_level);
            mTime = view.findViewById(R.id.comment_time);
            mUser = view.findViewById(R.id.comment_user);
            mText = view.findViewById(R.id.comment_text);
        }

    }

}
