package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Ask extends Item {

    public static final Parcelable.Creator<Ask> CREATOR = new Parcelable.Creator<Ask>() {

        @Override
        public Ask createFromParcel(Parcel parcel) {
            return new Ask(parcel);
        }

        @Override
        public Ask[] newArray(int size) {
            return new Ask[size];
        }
    };

    Ask(){}

    public Ask(Parcel in) {
        super(in);
        this.descendants = in.readInt();
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.score = in.readInt();
        this.text = in.readString();
        this.title = in.readString();
        this.url = in.readString();
    }

    /**
     * In the case of stories or polls, the total comment count.
     */
    private int descendants;

    /**
     * The ids of the item's comments, in ranked display order.
     */
    private long[] kids;

    /**
     * The story's score, or the votes for a pollopt.
     */
    private int score;

    /**
     * The comment, story or poll text. HTML.
     */
    private String text;

    /**
     * The title of the story, poll or job.
     */
    private String title;

    /**
     * The URL of the story.
     */
    private String url;

    public int getDescendants() {
        return descendants;
    }

    public long[] getKids() {
        return kids;
    }

    public int getScore() {
        return score;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(descendants);
        dest.writeInt(kids.length);
        dest.writeLongArray(kids);
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeString(title);
        dest.writeString(url);

    }
}
