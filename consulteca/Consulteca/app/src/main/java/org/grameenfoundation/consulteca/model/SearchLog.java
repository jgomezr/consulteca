package org.grameenfoundation.consulteca.model;

import org.grameenfoundation.consulteca.storage.DatabaseHelperConstants;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class SearchLog implements Serializable {
    private Integer id;
    private String menuItemId;
    private Date dateCreated;
    private String clientId;
    private String gpsLocation;
    private String content;
    private String category;
    private boolean testLog;
    private String submissionLocation;

    public SearchLog() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        if (getClientId() != null && getClientId().trim().length() > 0)
            builder.append("Client ID:").append(getClientId());

        builder.append(" Date: ").append(DatabaseHelperConstants.DEFAULT_DATE_FORMAT.format(getDateCreated()));
        return builder.toString();
    }

    public boolean isTestLog() {
        return testLog;
    }

    public void setTestLog(boolean testLog) {
        this.testLog = testLog;
    }

    public String getSubmissionLocation() {
        return submissionLocation;
    }

    public void setSubmissionLocation(String submissionLocation) {
        this.submissionLocation = submissionLocation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
