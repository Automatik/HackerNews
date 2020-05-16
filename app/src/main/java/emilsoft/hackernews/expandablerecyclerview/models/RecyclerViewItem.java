package emilsoft.hackernews.expandablerecyclerview.models;


import java.util.List;

public interface RecyclerViewItem {

//    List<RecyclerViewItem> children = null;
//
//    int level = 0;
//
//    int position = 0;
//
//    boolean expanded = false;

    boolean isExpanded();

    void setExpanded(boolean expanded);

    int getPosition();

    void setPosition(int position);

    int getLevel();

    void setLevel(int level);

    List<RecyclerViewItem> getChildren();

    void addChild(RecyclerViewItem child);

    void addChildren(List<RecyclerViewItem> children);

    boolean hasChildren();

//    public boolean isExpanded() {
//        return expanded;
//    }
//
//    public void setExpanded(boolean expanded) {
//        this.expanded = expanded;
//    }
//
//    protected RecyclerViewItem(int level){
//        this.level = level;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public void setLevel(int level) {
//        this.level = level;
//    }
//
//    public int getLevel(){
//        return level;
//    }
//
//    public List<RecyclerViewItem> getChildren() {
//        return children;
//    }
//
//    public void addChildren(List<RecyclerViewItem> children) {
//        this.children = children;
//    }
//
//    public boolean hasChildren(){
//        if(children !=null && children.size() > 0){
//            return true;
//        }else{
//            return false;
//        }
//
//    }
}
