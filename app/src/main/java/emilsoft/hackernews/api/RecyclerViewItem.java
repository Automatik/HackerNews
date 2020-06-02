package emilsoft.hackernews.api;

import java.util.ArrayList;
import java.util.List;

public interface RecyclerViewItem<T> {

    int level = 0;

    boolean isCollapsed = false;

    boolean hasChildren();

    void setChildren(List<T> children);

    List<T> getChildren();

    boolean hasParent();

    void setParentInstance(T parent);

    T getParentInstance();

    long getId();

    long getParent();

    void setLevel(int level);

    int getLevel();

    void setIsCollapsed(boolean isCollapsed);

    boolean isCollapsed();

}
