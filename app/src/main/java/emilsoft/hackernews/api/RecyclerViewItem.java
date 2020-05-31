package emilsoft.hackernews.api;

import java.util.ArrayList;
import java.util.List;

public interface RecyclerViewItem {

    int level = 0;

    boolean isCollapsed = false;

//    List<RecyclerViewItem> children = new ArrayList<>();

//    RecyclerViewItem parentInstance = null;

    public boolean hasChildren();

    public void setChildren(List<RecyclerViewItem> children);

    public List<RecyclerViewItem> getChildren();

    public boolean hasParent();

    public void setParentInstance(RecyclerViewItem parent);

    public RecyclerViewItem getParentInstance();

    public long getId();

    public long getParent();

    public void setLevel(int level);

    public int getLevel();

    public void setIsCollapsed(boolean isCollapsed);

    public boolean isCollapsed();

}
