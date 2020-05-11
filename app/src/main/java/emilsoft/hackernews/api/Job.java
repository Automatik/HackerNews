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
        super(in);
        this.score = in.readInt();
        this.text = in.readString();
        this.title = in.readString();
        this.url = in.readString();
    }

    /**
     * The story's score, or the votes for a pollopt.
     */
    int score;

    /**
     * The comment, story or poll text. HTML.
     */
    String text;

    /**
     * The title of the story, poll or job.
     */
    String title;

    /**
     * The URL of the story.
     */
    String url;

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
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeString(title);
        dest.writeString(url);
    }

    public boolean hasJobUrl() {
        return url != null && !url.isEmpty();
    }
}
