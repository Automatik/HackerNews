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
    POLL_TYPE("poll");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
