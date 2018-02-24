package com.iteam.easyups.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

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
        for(String url : urls){
            Log.w("START PARSING  ", url);
           getEdtLinks(url);
        }

        return null ;
    }

    protected void onPostExecute(Void arg) {
         Log.w("END OF PARSING ", "Parsing completed");

    }

    private void getEdtLinks(String url){
        for (Element link : getHtmlElements(url, edtBaseQuery)) {
            Log.e("TEXT : ", "- " + link.text() + " " + link.attr("value"));
            getEdtGroupsLinks(link.attr("value").replace(".html", ".xml"));
        }
    }

    private void getEdtGroupsLinks(String url){
        for (Element link : getHtmlElements(url, edtGroupQuery)) {
            if(link.select("name") != null && link.select("link[href]") != null){
                Log.e("TEXT : ", "--- " + link.select("name").text() + " " + link.select("link[href]").attr("href"));
            }

        }
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
