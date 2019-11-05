package emilsoft.hackernews.api;

public class Story extends Item {

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
}
