import gui.MainForm;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

//    private static String URL="http://www.wuxiaworld.com/wmw-index/wmw-chapter-";
//    private static String URL="http://www.wuxiaworld.com/novel/against-the-gods/atg-chapter-";
//    private static String URL="https://www.wuxiaworld.com/novel/wu-dong-qian-kun/wdqk-chapter-";
//    private static String URL="http://volarenovels.com/release-that-witch/rw-chapter-";
    private static String URL="https://www.readlightnovel.org/release-that-witch/chapter-";
//    private static String URL="https://www.readlightnovel.org/everyone-else-is-a-returnee/chapter-";
//    private static String URL="http://www.wuxiaworld.com/martialworld-index/mw-chapter-";
//    private static String URL= "https://www.wuxiaworld.com/novel/overgeared/og-chapter-";

    private static int startIndex=859;
    private static int endIndex=900; //1195
    private static StringBuilder sb= new StringBuilder();

    public static void main(String[] args){
        MainForm form = new MainForm();

        //ConsolTest();
    }

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

    private static boolean fetchRedlight(int i){
        try {
            Document doc = Jsoup.connect(URL+i+"/").userAgent("Mozilla/5.0").timeout(10000).followRedirects(true).get();
            Element div = doc.select("div.chapter-content3 > div.desc").first();

            for(Node e : div.childNodes()){
                if (e instanceof TextNode) {
                    sb.append(((TextNode)e).text()+"\r\n\r\n");
                }else if(e instanceof Element && ((Element) e).tagName().equals("p")){
                    sb.append(((Element) e).text()+"\r\n\r\n");
                }
            }
            sb.append("\n");
        } catch (HttpStatusException e){
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean fetchVolar(int i) {
        Document doc;
        try {
            doc = Jsoup.connect(URL+i+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();

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

    private static boolean fetchWuxia(int i) {
        Document doc;
        try {
            doc = Jsoup.connect(URL+i+"/").userAgent("Mozilla/5.0").timeout(5000).followRedirects(true).get();
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


    /*
    public static String offlineFetch(int page) throws IOException {
        Document doc = Jsoup.connect("https://www.google.hu/?gfe_rd=cr&ei=7bTnWI-5MPSv8wflq4rgCQ#q=emperor+"+page+"+wuxiaworld").get();

        Elements links = doc.select("a[href]");
        for (Element link : links) {

            String temp = link.attr("href");
            if(temp.startsWith("/url?q=")){
                System.out.println(link.text().toString()+"wtf");
            }

        }

        //Element link = doc.select("li.action-menu-item.ab_dropdownitem > a[href]").first();

        return "pagetext";
    }
    */

}
