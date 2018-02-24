package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by Marianna on 24/02/2018.
 */
@IgnoreExtraProperties
public class Formation extends FormationElement {


    public List<FormationGroup> groupsList;
    public String department;



    public Formation (){

    }


    public Formation (String name, String edtLink, List<FormationGroup> groups, String department){

        super(name, edtLink);
        this.groupsList = groups;
        this.department = department;

    }


}
