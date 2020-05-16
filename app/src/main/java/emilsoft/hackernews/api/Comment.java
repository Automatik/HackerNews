package emilsoft.hackernews.api;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import emilsoft.hackernews.expandablerecyclerview.models.RecyclerViewItem;

public class Comment extends Item implements RecyclerViewItem {

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {

        @Override
        public Comment createFromParcel(Parcel parcel) {
            return new Comment(parcel);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public Comment(){}

    //Necessary for searching the parent of a comment
    public Comment(long id) {
        super(id);
        level = 0;
    }

    public Comment(Parcel in) {
        super(in);
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.parent = in.readLong();
        this.text = in.readString();
        this.url = in.readString();
        this.level = in.readInt();
    }

    /**
     * The ids of the item's comments, in ranked display order.
     */
    long[] kids;

    /**
     * The comment's parent: either another comment or the relevant story.
     */
    long parent;

    /**
     * The comment, story or poll text. HTML.
     */
    String text;

    /**
     * The URL of the story.
     */
    String url;

    int level;

    // Add after implementing Expandable RecyclerView

    int position;

    boolean isExpanded;

    List<RecyclerViewItem> children;

    public long[] getKids() {
        return kids;
    }

    public long getParent() {
        return parent;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int getLevel() { return level; }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public List<RecyclerViewItem> getChildren() {
        return children;
    }

    @Override
    public void addChild(RecyclerViewItem child) {
        if(children == null)
            children = new ArrayList<>();
        children.add(child);
    }

    @Override
    public void addChildren(List<RecyclerViewItem> children) {
        this.children = children;
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(kids.length);
        dest.writeLongArray(kids);
        dest.writeLong(parent);
        dest.writeString(text);
        dest.writeString(url);
        dest.writeInt(level);
    }
}
