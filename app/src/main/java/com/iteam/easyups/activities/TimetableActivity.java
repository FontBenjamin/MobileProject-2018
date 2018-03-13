package com.iteam.easyups.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.adapter.DataSnapshotSpinnerAdapter;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.FormationGroup;
import com.iteam.easyups.utils.AlertMessage;
import com.iteam.easyups.utils.HtmlParser;
import com.iteam.easyups.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Marianna on 23/02/2018.
 */

public class TimetableActivity extends AppCompatActivity {

    private String[] edtMainPages = {"https://edt.univ-tlse3.fr/FSI/2017_2018/index.html", "https://edt.univ-tlse3.fr/F2SMH/2017_2018/index.html"};
    private String timeTableUrl = "";
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private Context mContext;
    private TabHost tabHost;
    private ProgressBar progress;
    private  TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edt_layout);
        mContext = this;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(Util.isNetworkAvailable(this)){
            saveAllFormation();
        }else{
            Util.displayErrorAlert(AlertMessage.ERROR_TYPE, AlertMessage.NETWORK_ERROR, this);
        }

        initSearchFormation();
    }


    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private void initSearchFormation(){
        text = findViewById(R.id.textViewSpinners);
        progress = findViewById(R.id.progressBarSpinner);
        tabHost = (TabHost)findViewById(R.id.tab_host);
        tabHost.setup();
        getFormationByLevel();
    }


    /**
     * Create the tab for each component (FSI,F2SMH)
     * and retrieve formation from db
     */
    private void getFormationByLevel(){
        database.getReference().child(BDDRoutes.FORMATION_PATH).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (final DataSnapshot data : dataSnapshot.getChildren()) {
                            //Create tab for each department
                            TabHost.TabSpec formationSpec = tabHost.newTabSpec(data.getKey());
                            formationSpec.setIndicator(data.getKey());
                            formationSpec.setContent(new TabHost.TabContentFactory() {

                                public View createTabContent(String tag) {
                                    return createTabView(data);
                                }
                            });
                            tabHost.addTab(formationSpec);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    /**
     * Create the main view of a tab composed of level (L1...M2), formation title and group
     * @param data the component (FSI, F2SMH)
     * @return the new view
     */
    @SuppressLint("ResourceAsColor")
    private View createTabView(DataSnapshot data){
        LinearLayout linearLayout = initTabLayout();
        Spinner spinnerLevel = initLevelSpinner(data);
        final Spinner spinnerFormation = new Spinner(mContext);
        final Spinner spinnerGroup = new Spinner(mContext);

        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //When a group is selected, we update the timetable link
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(parentView.getItemAtPosition(position) instanceof FormationGroup) {
                    FormationGroup group = (FormationGroup) parentView.getItemAtPosition(position);
                    int index = timeTableUrl.lastIndexOf('/');
                    timeTableUrl = timeTableUrl.substring(0, index);
                    timeTableUrl += "/" + group.timeTableLink;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });


        spinnerFormation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //When an item of a spinner formation is selected, we update the groups according to the selected formation
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Formation formation = (Formation)parentView.getItemAtPosition(position);
                timeTableUrl = formation.timeTableLink;
                updateGroupSpinner(spinnerGroup, formation);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //When an item of a spinner level (L1...M2) is selected, we update the formation available for the selected level
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DataSnapshot data = (DataSnapshot)parentView.getItemAtPosition(position);
                updateFormationSpinner(spinnerFormation, data);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });


        Button confirmButton = new Button(mContext);
        confirmButton.setText("Go !");
        initButtonStyle(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TimetableActivity.this, TimetableWebViewActivity.class);
                i.putExtra("timeTableUrl", timeTableUrl);
                startActivity(i);

            }
        });

        TextView txtLevel = new TextView(mContext);
        txtLevel.setText("Niveau : ");
        TextView txtTitle = new TextView(mContext);
        txtTitle.setText("Intitulé : ");
        TextView txtGroup = new TextView(mContext);
        txtGroup.setText("Groupe : ");

        // Add level text and spinner
        linearLayout.addView(txtLevel);
        linearLayout.addView(spinnerLevel);

        // Add formation information text and spinner
        linearLayout.addView(txtTitle);
        linearLayout.addView(spinnerFormation);

        // Add groups text and spinner
        linearLayout.addView(txtGroup);
        linearLayout.addView(spinnerGroup);

        linearLayout.addView(confirmButton);

        progress.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        return linearLayout;
    }

    private void initButtonStyle(Button button){
        button.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        button.setBackgroundColor(Color.argb(255,192,11,18));
        button.setGravity(Gravity.BOTTOM);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Create the spinner to display level (L1...M2) from db
     * @param data the component (FSI, F2SMH)
     * @return the spinner created
     */
    private Spinner initLevelSpinner(DataSnapshot data){
        Spinner spinner = new Spinner(mContext);
        List<DataSnapshot> levels =  new ArrayList<>();
        for (DataSnapshot dataChild : data.getChildren()) {
            levels.add(dataChild);
        }
        DataSnapshotSpinnerAdapter adapter = new DataSnapshotSpinnerAdapter(mContext,
                android.R.layout.simple_spinner_item, levels);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return spinner;
    }

    /**
     * Update the information in the formation title spinner according to the selected level
     * @param spinnerFormationTitle the spinner to update
     * @param data the selected level (L1...M2)
     */
    private void updateFormationSpinner(final Spinner spinnerFormationTitle, DataSnapshot data){
        List<Formation> formations =  new ArrayList<>();
        for(DataSnapshot dataFormation : data.getChildren()){
            formations.add(dataFormation.getValue(Formation.class));
        }

        ArrayAdapter<Formation> adapter =  new ArrayAdapter<Formation> (mContext,
                android.R.layout.simple_spinner_item, formations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFormationTitle.setAdapter(adapter);
        spinnerFormationTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Update the information in the group spinner according to the selected formation
     * @param spinnerGroup The spinner to update
     * @param formation The selected formation
     */
    private void updateGroupSpinner(final Spinner spinnerGroup, Formation formation){
        List<FormationGroup> formationGroup;
        ArrayAdapter<?> adapter;
        if(formation.groupsList == null){
           adapter =  new ArrayAdapter<String> (mContext,
                    android.R.layout.simple_spinner_item, Arrays.asList("Aucun groupe pour cette formation"));
        }else{
            formationGroup  =  formation.groupsList;
            adapter =  new ArrayAdapter<FormationGroup> (mContext,
                    android.R.layout.simple_spinner_item, formationGroup);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerGroup.setAdapter(adapter);
        spinnerGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private LinearLayout initTabLayout(){
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setGravity(Gravity.TOP);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(8, 8, 0, 0);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }


    /**
     * Populate formation in database if they don't are stored yet
     */
    private void saveAllFormation(){
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(BDDRoutes.FORMATION_PATH).exists()) {
                    new HtmlParser( database.getReference().child(BDDRoutes.FORMATION_PATH)).execute(edtMainPages);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
