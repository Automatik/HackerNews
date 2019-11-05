package emilsoft.hackernews.api;

import com.google.gson.annotations.SerializedName;

public class Item {

    /**
     * The item's unique id.
     */
    private long id;

    /**
     * true if the item is deleted.
     */
    @SerializedName("deleted")
    private boolean isDeleted;

    /**
     * The type of item. One of "job", "story", "comment", "poll", or "pollopt".
     */
    private Type type;

    /**
     * The username of the item's author.
     */
    @SerializedName("by")
    private String user;

    /**
     * Creation date of the item, in Unix Time.
     */
    private long time;

    /**
     * true if the item is dead.
     */
    @SerializedName("dead")
    private boolean isDead;

    public long getId(){
        return id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Type getType() {
        return type;
    }

    public String getUser() {
        return user;
    }

    public long getTime() {
        return time;
    }

    public boolean isDead() {
        return isDead;
    }
}
