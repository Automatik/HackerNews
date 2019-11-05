package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Story extends Item {

    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>(){

        @Override
        public Story createFromParcel(Parcel parcel) {
            return new Story(parcel);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    Story(){}

    public Story(Parcel in) {
        super(in);
        this.descendants = in.readInt();
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.score = in.readInt();
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
        dest.writeString(title);
        dest.writeString(url);

    }
}
