package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Poll extends Item {

    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {

        @Override
        public Poll createFromParcel(Parcel parcel) {
            return new Poll(parcel);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };

    Poll(){}

    public Poll(Parcel in) {
        super(in);
        this.descendants = in.readInt();
        this.kids = new long[in.readInt()];
        in.readLongArray(kids);
        this.score = in.readInt();
        this.parts = new long[in.readInt()];
        in.readLongArray(parts);
        this.text = in.readString();
        this.title = in.readString();
    }

    /**
     * In the case of stories or polls, the total comment count.
     */
    int descendants;

    /**
     * The ids of the item's comments, in ranked display order.
     */
    long[] kids;

    /**
     * The story's score, or the votes for a pollopt.
     */
    int score;

    /**
     * A list of related pollopts, in display order.
     */
    long[] parts;

    /**
     * The comment, story or poll text. HTML.
     */
    String text;

    /**
     * The title of the story, poll or job.
     */
    String title;

    public int getDescendants() {
        return descendants;
    }

    public long[] getKids() {
        return kids;
    }

    public int getScore() {
        return score;
    }

    public long[] getParts() {
        return parts;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(descendants);
        dest.writeInt(kids.length);
        dest.writeLongArray(kids);
        dest.writeInt(score);
        dest.writeInt(parts.length);
        dest.writeLongArray(parts);
        dest.writeString(text);
        dest.writeString(title);
    }
}
