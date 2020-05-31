package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

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

    boolean isCollapsed;

    RecyclerViewItem parentInstance;

    List<RecyclerViewItem> children;

    @Override
    public boolean hasChildren() {
        return children != null;
    }

    @Override
    public void setChildren(List<RecyclerViewItem> children) {
        this.children = children;
    }

    @Override
    public List<RecyclerViewItem> getChildren() {
        return children;
    }

    @Override
    public boolean hasParent() {
        return parentInstance != null;
    }

    @Override
    public void setParentInstance(RecyclerViewItem parent) {
        this.parentInstance = parent;
    }

    @Override
    public RecyclerViewItem getParentInstance() {
        return this.parentInstance;
    }

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
    public int getLevel() { return level; }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    public void setIsCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
    }

    public boolean isCollapsed() {
        return isCollapsed;
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
