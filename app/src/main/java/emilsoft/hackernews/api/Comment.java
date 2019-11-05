package emilsoft.hackernews.api;

public class Comment extends Item {

    /**
     * The ids of the item's comments, in ranked display order.
     */
    private long[] kids;

    /**
     * The comment's parent: either another comment or the relevant story.
     */
    private long parent;

    /**
     * The comment, story or poll text. HTML.
     */
    private String text;

    /**
     * The URL of the story.
     */
    private String url;

}
