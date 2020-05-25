package emilsoft.hackernews.api;

public interface RecyclerViewItem {

    int level = 0;

    boolean isCollapsed = false;

    public long getId();

    public long getParent();

    public void setLevel(int level);

    public int getLevel();

    public void setIsCollapsed(boolean isCollapsed);

    public boolean isCollapsed();

}
