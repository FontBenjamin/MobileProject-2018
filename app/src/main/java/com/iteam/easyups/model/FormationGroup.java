package com.iteam.easyups.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Marianna on 24/02/2018.
 */
@IgnoreExtraProperties
public class FormationGroup extends FormationElement {


    public FormationGroup(){

    }


    public FormationGroup(String name, String edtLink){

        super(name, edtLink);

    }


}
