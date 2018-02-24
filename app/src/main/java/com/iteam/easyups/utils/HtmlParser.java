package com.iteam.easyups.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.iteam.easyups.communication.FormationService;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.FormationGroup;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Marianna on 23/02/2018.
 */

public class HtmlParser extends AsyncTask<String, Void, Void> {

    private String edtBaseQuery = "option[value~=https://edt.univ-tlse3.fr/]";
    private String edtGroupQuery = "resource[type=group]";

    protected Void doInBackground(String... urls) {
        for(String sUrl : urls){
            Log.w("START PARSING  ", sUrl);
            String dpt = "";
            try {
                URL url = new URL(sUrl);
                dpt = url.getPath().split("/")[1];
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            getEdtLinks(sUrl, dpt);
        }

        return null ;
    }

    protected void onPostExecute(Void arg) {
         Log.w("END OF PARSING ", "Parsing completed");

    }

    private void getEdtLinks(String sUrl, String dpt){
        FormationService s = new FormationService();
        int i = 0;
        for (Element link : getHtmlElements(sUrl, edtBaseQuery)) {
            String xmlLink  = link.attr("value").replace(".html", ".xml");
            Log.e("TEXT : ", "- " + link.text() + " " + xmlLink);
            List<FormationGroup> groups = getEdtGroupsLinks(xmlLink);
            Formation formation = new Formation(link.text(), xmlLink, groups, dpt);
            s.saveFormation(formation);
        }
    }

    private List<FormationGroup> getEdtGroupsLinks(String url){
        List<FormationGroup> groups = new ArrayList<>();
        for (Element link : getHtmlElements(url, edtGroupQuery)) {
            if(link.select("name") != null && link.select("link[href]") != null){
                groups.add(new FormationGroup(link.select("name").text(),  link.select("link[href]").attr("href")));
                Log.e("TEXT : ", "--- " + link.select("name").text() + " " + link.select("link[href]").attr("href"));
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


}
