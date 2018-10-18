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
    /*
    private static void ConsolTest() {
        String [] tmb=URL.split("/");
        ArrayUtils.reverse(tmb);
        String novel_name= tmb[1];

        for(int x=0;x<=endIndex-startIndex;x++){
            System.out.print("*");
        }
        System.out.println();

        int i;
        String next=URL+startIndex+"/";

        for(i=startIndex;i<=endIndex;i++){
            if(URL.contains("wuxiaworld.com")){
                fetchWuxia(i);
            }else if(URL.contains("volarenovels.com")){
                fetchVolar(i);
            }else if(URL.contains("readlightnovel.org")){
                fetchRedlight(i);
            }

            if(!next.equals("")){
                System.out.println(novel_name + " chapter "+i+" fetched");
            }else{
                break;
            }
        }

        File filename = new File("books\\"+novel_name+"_"+startIndex+"-"+(--i)+".txt");
        try{
            filename.getParentFile().mkdirs();
        }catch(Exception e){
            e.printStackTrace();
        }


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
            //.replaceAll("\n+","\n\n").replaceAll("[\r\n]+", "\n\n").replaceAll("Previous Chapter","")
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n"+filename+" created!");
    }
*/
    public static StringBuilder fetchNovel(String url, int chapter) {
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
            return sb;
        }

        return null;
    }



    private static boolean fetchRedlight(String fullUrl){
        try {
            Document doc = Jsoup.connect(fullUrl+"/").userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Element div = doc.select("div.chapter-content3 > div.desc").first();

            for(Node e : div.childNodes()){
                if (e instanceof TextNode) {
                    sb.append(((TextNode)e).text()+"\r\n");
                }else if(e instanceof Element && ((Element) e).tagName().equals("p")){
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
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean fetchWuxia(String fullUrl) {
        Document doc;
        try {
            doc = Jsoup.connect(fullUrl+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();
            Elements div = doc.select("div[class=fr-view] > p");
            String chapter = doc.select("div.caption > div > h4").first().text();

            //div.select("p:lt(3)").remove();
            //div.select("p").last().remove();
            int cnt=1;
            for(Element e : div){
                if(e.text().contains("Glossary of Common Korean Terms.")) break;

                if(cnt==1){
                    if(!e.text().contains(chapter)){
                        sb.append(chapter+"\n") ;
                    }else{
                        sb.append(e.text()+"\n");
                    }
                }else{
                    sb.append(e.text()+"\n");
                }
               cnt--;
            }

            if(!sb.toString().contains(chapter)){
                //TODO: do something if necessary
            }

        } catch (HttpStatusException e){
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
