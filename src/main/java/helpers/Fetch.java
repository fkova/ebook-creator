package helpers;

//import nl.siegmann.epublib.domain.Book;
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
            status=fetchRedlight(""+url,chapter);
        }else if (url.contains("novelupdates.com")) {
            status=fetchNovelUpdates(url,chapter);
        }else if (url.contains("maxnovel.com")) {
            status=fetchMaxnovel(url,chapter);
        }

        if(status){
            return sb.toString();
        }

        return null;
    }

    private boolean fetchNovelUpdates(String url, int chapter) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Element element = doc.select("#myTable >tbody > tr > td:nth-child(3)").first();
            int lastChapter=Integer.valueOf(element.text().split(" ")[0].replaceAll("c",""));
            if(chapter>lastChapter){
                return false;
            }else if(chapter<=lastChapter){
                int page=getPage(chapter,lastChapter);
                Document doc2 = Jsoup.connect(url+"?pg="+page).userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
                Element a = doc2.select("a[title=c"+chapter+"]").first();
                System.out.println(a.attr("href"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private int getPage(int chapter, int lastChapter) {
        int pagesize=15;
        int start=lastChapter;
        int lastPage=(int)Math.ceil((double)lastChapter/pagesize);
        int page=1;

        while(start-14>chapter){
            start-=15;
            page++;
        }


        return page;
    }

    private boolean fetchRedlight(String url,int chapter){
        try {
            Document doc = Jsoup.connect(url+chapter).userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Element div = doc.select("div.chapter-content3 > div.desc").first();

            if(div==null){
                System.out.println("Error at parsing chapter "+chapter+  " Element not present: div.chapter-content3 > div.desc");
                return false;
            }

            String prevoiustext="";
            String text="";

            for(Node e : div.childNodes()){
                if (e instanceof TextNode) {
                    text=((TextNode) e).text().trim();
                    if(!text.equals(prevoiustext) && text.matches(".*\\w.*")){
                        prevoiustext=text;
                        sb.append(prevoiustext);
                    }else{
                        continue;
                    }

                }else if(e instanceof Element && ((Element) e).tagName().equals("p")){
                    text=((Element) e).text().trim();
                    if(!text.equals(prevoiustext) && text.matches(".*\\w.*")){
                        prevoiustext=text;
                        sb.append(prevoiustext);
                    }else{
                        continue;
                    }
                }else if(e instanceof Element && ((Element) e).tagName().equals("br")){
                    sb.append("\n\n");
                }
            }


            String stripped = sb.toString().replaceAll("[\r\n]+", "\n\n");
            sb=new StringBuilder(stripped);
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

            String prevoiusText="";
            String currentText="";
            for(Element e : elements){
                currentText=e.text();

                if(currentText.trim().length()>1){
                    sb.append(e.text()+"\n\n");
                }
                //if(e.text().contains("Glossary of Common Korean Terms.")) break;
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

    private boolean fetchMaxnovel(String url,int chapter) {
        Document doc;
        try {
            doc = Jsoup.connect(url+chapter).userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Elements elements=doc.select("div[class=text-left] > p");
            for(Element e : elements){
                sb.append(e.text()+"\n\n");
            }
        } catch (HttpStatusException e){
            if(e.getStatusCode()==404){
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
