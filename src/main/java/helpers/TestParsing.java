package helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class TestParsing {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.novelupdates.com/extnu/2200767/").timeout(10000).followRedirects(true).get();
        //Element element = doc.select("div.kix-paginateddocumentplugin").first();
        System.out.println(doc.text());
    }
}
