package emilsoft.hackernews.api;

public enum Type {

    JOB_TYPE("job"),
    STORY_TYPE("story"),
    COMMENT_TYPE("comment"),
    POLL_TYPE("poll");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
