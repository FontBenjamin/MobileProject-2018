package com.iteam.easyups.model;

/**
 * Created by Marianna on 11/03/2018.
 */

public enum Criticality {

    COMFORT("Confort"),
    PROBLEM("Problème"),
    DANGER("Danger");

    private String label;

    Criticality(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
