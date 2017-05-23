package org.grameenfoundation.consulteca.model;

/**
 * Represents a menu
 *
 * @author Charles Tumwebaze
 */
public class SearchMenuItem extends ListObject {
    private String label;
    private int position;
    private String content;
    private String parentId;
    private String menuId;
    private String attachmentId;

    /**
     * gets the position of the menu item
     *
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * sets the position of the menu item.
     *
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * gets the content of this menu item
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * sets the content of this menu item.
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * gets the menu item Identifier that is the parent of this menu item.
     *
     * @return
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * sets the menu item identifier that is the parent of this menu item.
     *
     * @param parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * gets the identifier of the menu in which this menu item belongs
     *
     * @return
     */
    public String getMenuId() {
        return menuId;
    }

    /**
     * sets the identifier of the menu in which this menu item belongs.
     *
     * @param menuId
     */
    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    /**
     * gets the identifier of the attachment associated with this menu item
     *
     * @return
     */
    public String getAttachmentId() {
        return attachmentId;
    }

    /**
     * sets the identifier of the attachment associated with this menu item.
     *
     * @param attachmentId
     */
    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    public String getDescription() {
        return this.getContent();
    }
}
