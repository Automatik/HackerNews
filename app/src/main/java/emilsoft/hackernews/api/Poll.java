package emilsoft.hackernews.api;

public class Poll extends Item {

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
     * A list of related pollopts, in display order.
     */
    private long[] parts;

    /**
     * The comment, story or poll text. HTML.
     */
    private String text;

    /**
     * The title of the story, poll or job.
     */
    private String title;

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
}
