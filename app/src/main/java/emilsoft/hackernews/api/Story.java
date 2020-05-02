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

    public Story(){}

    public Story(Parcel in) {
        super(in);
        this.descendants = in.readInt();
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.score = in.readInt();
        this.title = in.readString();
        this.url = in.readString();
        this.text = in.readString();
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

    /**
     * The text written by the user. Valid only for Ask HN
     */
    private String text;

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

    public String getText() {
        return text;
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
        dest.writeString(text);
    }

    public static boolean isAsk(Story story) {
        return story.getUrl() == null && story.getText() != null;
    }
}
