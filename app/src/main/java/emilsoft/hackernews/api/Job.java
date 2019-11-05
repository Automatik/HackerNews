package emilsoft.hackernews.api;

public class Job extends Item {

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

}
