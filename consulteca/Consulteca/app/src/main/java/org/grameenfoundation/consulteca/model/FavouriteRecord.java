package org.grameenfoundation.consulteca.model;

import java.util.Date;

/**
 * represents a favourite menu item.
 */
public class FavouriteRecord {
    private Integer id;
    private String name;
    private String category;
    private String menuItemId;
    private Date dateCreated;

    /**
     * gets the identifier of this favourite record
     *
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * sets the identifier of this favourite record
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * gets the name of this favourite record
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of this favourite record
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * gets the name of the category of this favourite record.
     *
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     * sets the name of the category of this favourite record.
     *
     * @param category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * gets the identifier of the menu item that is marked as favourite.
     *
     * @return
     */
    public String getMenuItemId() {
        return menuItemId;
    }

    /**
     * sets the identifier of the menu item that is marked as favourite.
     *
     * @param menuItemId
     */
    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    /**
     * gets the date when this favourite record is created.
     *
     * @return
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * sets the date when this favourite record is created.
     *
     * @param dateCreated
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
