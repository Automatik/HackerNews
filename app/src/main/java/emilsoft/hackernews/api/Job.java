package emilsoft.hackernews.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Job extends Item {

    public static final Parcelable.Creator<Job> CREATOR = new Parcelable.Creator<Job>() {

        @Override
        public Job createFromParcel(Parcel parcel) {
            return new Job(parcel);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    Job(){}

    public Job(Parcel in) {
        this.score = in.readInt();
        this.text = in.readString();
        this.title = in.readString();
        this.url = in.readString();
    }

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeString(title);
        dest.writeString(url);
    }
}
