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
     * The title of the story, poll or job.
     */
    String title;

    /**
     * The URL of the story.
     */
    String url;

    /**
     * The text written by the user. Valid only for Ask HN
     */
    String text;

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

    //This is needed in TopStories which returns also jobs
    public static Job castStoryToJob(Story story) {
        Job job = new Job();
        job.id = story.id;
        job.isDeleted = story.isDeleted;
        job.isDead = story.isDead;
        job.type = story.type;
        job.user = story.user;
        job.time = story.time;
        job.score = story.score;
        job.text = story.text;
        job.title = story.title;
        job.url = story.url;
        return job;
    }
}
