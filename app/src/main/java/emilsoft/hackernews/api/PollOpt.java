package emilsoft.hackernews.api;

public class PollOpt extends Item {

    /**
     * The story's score, or the votes for a pollopt.
     */
    private int score;

    /**
     * The comment, story or poll text. HTML.
     */
    private String text;

    /**
     * The pollopt's associated poll.
     */
    private long poll;

}
