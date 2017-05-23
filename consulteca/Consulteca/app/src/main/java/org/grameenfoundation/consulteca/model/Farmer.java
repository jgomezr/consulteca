package org.grameenfoundation.consulteca.model;

/**
 * represent a farmer record
 *
 * Copyright (c) 2014 AppLab, Grameen Foundation
 * Created by: David
 */
public class Farmer extends ListObject {
    //private String farmerId;
    private String firstName;
    private String lastName;
    private String creationDate;
    private String subcounty;
    private String village;

    /*public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }*/

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getSubcounty() {
        return subcounty;
    }

    public void setSubcounty(String subcounty) {
        this.subcounty = subcounty;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    @Override
    public String toString(){
        return getFirstName() + " " + getLastName();
    }
}
