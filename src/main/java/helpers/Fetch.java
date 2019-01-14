package helpers;

import nl.siegmann.epublib.domain.Book;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fetch {
    private StringBuilder sb;
    private String title;
    private Map<Integer,String> routingMap;

    public Fetch() {
        routingMap= new HashMap<>();
    }

    public String fetchNovel(String url, int chapter) {
        sb = new StringBuilder();
        boolean status=false;

        if (url.contains("wuxiaworld.com")) {
            status=fetchWuxia(""+url,chapter);
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

    private boolean fetchRedlight(String fullUrl){
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

    private boolean fetchVolar(String fullUrl) {
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

    private boolean fetchWuxia(String url,int chapter) {
        Document doc;
        try {
            doc = Jsoup.connect(route(url,chapter)).userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();

            Element text=doc.select("div[class=fr-view]").first();

            if(text==null){
                System.out.println("Error at parsing chapter "+chapter+  " Element not present: div[class=fr-view]");
                return false;
            }

            Elements elements = text.select("div[class=fr-view] > p");

            title = doc.select("div.caption > div > h4").first().text();
            String next = "https://www.wuxiaworld.com"+doc.select("li.next > a").first().attr("href");
            if(!next.equals(url+(chapter+1))){
                routingMap.put(chapter+1,next);
            }

            sb.append(title+"\r\n\r\n") ;

            for(Element e : elements){
                if(e.text().contains("Glossary of Common Korean Terms.")) break;
                sb.append(e.text()+"\r\n\r\n");

            }
        } catch (HttpStatusException e){
            if(e.getStatusCode()==404){
                //doc = Jsoup.connect(url+(Integer.valueOf(chapter)-1)+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();
                //String next = doc.select("li.next > a").first().attr("href");
                //fetchWuxia("https://www.wuxiaworld.com",next);
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String route(String url,int chapter){
       /*
        if(url.contains("a-will-eternal") && chapter>472){
            routingMap.put(chapter,"https://www.wuxiaworld.com/novel/a-will-eternal/chapter-"+chapter);
        }
*/
        if(routingMap.containsKey(chapter)){
            return routingMap.get(chapter);
        }

        return url+chapter;
    }
}
