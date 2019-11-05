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

    public Comment(Parcel in) {
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.parent = in.readLong();
        this.text = in.readString();
        this.url = in.readString();
    }

    /**
     * The ids of the item's comments, in ranked display order.
     */
    private long[] kids;

    /**
     * The comment's parent: either another comment or the relevant story.
     */
    private long parent;

    /**
     * The comment, story or poll text. HTML.
     */
    private String text;

    /**
     * The URL of the story.
     */
    private String url;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(kids.length);
        dest.writeLongArray(kids);
        dest.writeLong(parent);
        dest.writeString(text);
        dest.writeString(url);
    }
}
