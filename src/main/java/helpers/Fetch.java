package helpers;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;

public class Fetch {
    private static StringBuilder sb;

    public static String fetchNovel(String url, int chapter) {
        sb = new StringBuilder();
        boolean status=false;

        if (url.contains("wuxiaworld.com")) {
            status=fetchWuxia(""+url+chapter);
        } else if (url.contains("volarenovels.com")) {
            status=fetchVolar(""+url+chapter);
        } else if (url.contains("readlightnovel.org")) {
            status=fetchRedlight(""+url+chapter);
        }

        if(status){
            return sb.toString();
        }

        return null;
    }

    private static boolean fetchRedlight(String fullUrl){
        try {
            Document doc = Jsoup.connect(fullUrl+"/").userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Element div = doc.select("div.chapter-content3 > div.desc").first();


            for(Node e : div.childNodes()){
                if (e instanceof TextNode) {
                    if(((TextNode) e).text().contains("Chapter") && ((TextNode) e).text().trim().length()<15){
                        continue;
                    }else if(((TextNode) e).text().startsWith("Translator:")){
                        continue;
                    }else{
                        sb.append(((TextNode)e).text());
                    }
                }else if(e instanceof Element && ((Element) e).tagName().equals("p")){
                    sb.append(((Element) e).text()+"\r\n");
                }else if(e instanceof Element && ((Element) e).tagName().equals("br")){
                    sb.append(((Element) e).text()+"\r\n");
                }
            }
            sb.append("\n");
        } catch (HttpStatusException e){
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean fetchVolar(String fullUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(fullUrl+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();

            Elements div = doc.select("div.entry-content");
            div.select("p:lt(2)").remove();
            div.select("p").last().remove();
            sb.append("\n");
            for(Element e : div.select("p")){
                sb.append(e.text()+"\n");
            }
        } catch (HttpStatusException e){
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean fetchWuxia(String fullUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(fullUrl+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();
            Elements div = doc.select("div[class=fr-view] > p");
            String chapter = doc.select("div.caption > div > h4").first().text();

            int cnt=1;
            for(Element e : div){
                if(e.text().contains("Glossary of Common Korean Terms.")) break;

                if(cnt==1){
                    if(!e.text().contains(chapter)){
                        sb.append(chapter+"\r\n\r\n") ;
                    }else{
                        sb.append(e.text()+"\r\n\r\n");
                    }
                }else{
                    sb.append(e.text()+"\r\n\r\n");
                }
               cnt--;
            }
        } catch (HttpStatusException e){
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
