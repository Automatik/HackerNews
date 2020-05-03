package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment extends Item {

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

    public int getLevel() { return level; }

    public void setLevel(int level) {
        this.level = level;
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
