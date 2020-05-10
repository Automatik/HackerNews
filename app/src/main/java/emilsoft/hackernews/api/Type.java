package emilsoft.hackernews.api;

import com.google.gson.annotations.SerializedName;

public enum Type {

    @SerializedName("job")
    JOB_TYPE("job"),
    @SerializedName("story")
    STORY_TYPE("story"),
    @SerializedName("comment")
    COMMENT_TYPE("comment"),
    @SerializedName("poll")
    POLL_TYPE("poll"),
    @SerializedName("pollopt")
    POLLOPT_TYPE("pollopt");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Type fromString(String type) {
        switch (type) {
            case "job": return JOB_TYPE;
            case "story": return STORY_TYPE;
            case "comment": return COMMENT_TYPE;
            case "poll": return POLL_TYPE;
            case "pollopt": return POLLOPT_TYPE;
            default: throw new IllegalArgumentException("No Enum specified for this string type");
        }
    }

}
