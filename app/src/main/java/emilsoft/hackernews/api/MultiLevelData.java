package emilsoft.hackernews.api;

import java.util.HashMap;
import java.util.List;

public class MultiLevelData {

    public List<RecyclerViewItem> items = null;

    public HashMap<Long, List<RecyclerViewItem>> collapsedParents = null;

    public HashMap<Long, Long> collapsedChildren = null;

    public void clear() {
        if(items != null)
            items.clear();
        if(collapsedParents != null)
            collapsedParents.clear();
        if(collapsedChildren != null)
            collapsedChildren.clear();
    }

    public int itemsSize() {
        if(items != null)
            return items.size();
        return 0;
    }

}
