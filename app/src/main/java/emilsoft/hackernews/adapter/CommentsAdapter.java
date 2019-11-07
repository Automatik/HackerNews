package emilsoft.hackernews.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.LinkedList;

import emilsoft.hackernews.R;
import emilsoft.hackernews.Utils;
import emilsoft.hackernews.api.Comment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private LinkedList<Comment> commentsList;

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
    }

    @Override
    public int getItemCount() {
        if(commentsList != null)
            return commentsList.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mTime;
        public final TextView mUser;
        public final TextView mText;
        public Comment mComment;


        public ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mTime = view.findViewById(R.id.comment_time);
            mUser = view.findViewById(R.id.comment_user);
            mText = view.findViewById(R.id.comment_text);
        }

    }

}
