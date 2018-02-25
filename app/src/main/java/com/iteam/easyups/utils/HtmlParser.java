package com.iteam.easyups.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.FormationGroup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marianna on 23/02/2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Void> {

    private String edtBaseQuery = "option[value~=https://edt.univ-tlse3.fr/]";
    private String edtGroupQuery = "resource[type=group]";
    List<Formation> formations = new ArrayList<>();
    private DatabaseReference database;

    public HtmlParser(DatabaseReference ref){
        database = ref;
    }

    protected Void doInBackground(String... urls) {

        for(String url : urls){
            Log.w("START PARSING  ", url);
            getEdtLinks(url);
        }

        return null ;
    }

    protected void onPostExecute(Void arg) {
         Log.w("DATABASE INSERTION ", "Start database insertion");
         for(Formation formation : formations){
             saveFormation(formation);
         }
        Log.w("DATABASE INSERTION ", "Database insertion completed");
    }

    private void getEdtLinks(String sUrl){
        for (Element link : getHtmlElements(sUrl, edtBaseQuery)) {
            String xmlLink  = link.attr("value").replace(".html", ".xml");
            //Log.e("TEXT : ", "- " + link.text() + " " + xmlLink);
            List<FormationGroup> groups = getEdtGroupsLinks(xmlLink);
            Formation formation = new Formation(link.text(), xmlLink, groups);
            formations.add(formation);
        }

    }

    private List<FormationGroup> getEdtGroupsLinks(String url){
        List<FormationGroup> groups = new ArrayList<>();
        for (Element link : getHtmlElements(url, edtGroupQuery)) {
            if(link.select("name") != null && link.select("link[href]") != null){
                groups.add(new FormationGroup(link.select("name").text(),  link.select("link[href]").attr("href")));
               // Log.e("TEXT : ", "--- " + link.select("name").text() + " " + link.select("link[href]").attr("href"));
            }
        }

        return groups;
    }

    private Elements getHtmlElements(String url, String query){

        Elements elements = new Elements();
        try {
            Document doc = null;
            InputStream input = new URL(url).openStream();
            doc = Jsoup.parse(input, "UTF-8", url);
            doc.outputSettings().escapeMode(Entities.EscapeMode.base);
            doc.outputSettings().charset("UTF-8");
            doc.outputSettings().prettyPrint(false);
            elements = doc.select(query);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return elements;
    }


    public void saveFormation(Formation formation){
        formation.id = database.push().getKey();
        database.child(formation.department).child(formation.level).child(formation.id).setValue(formation);

    }

}
