package helpers;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;

public class Epublib_example {
    public static void main(String[] args) {
        try {
            Book book = new Book();
            book.getMetadata().addTitle("Epublib test book 1");
            //book.getMetadata().addAuthor(new Author("Joe", "Tester"));
            // Set cover image
            //book.getMetadata().setCoverImage(new Resource(helpers.Epublib_example.class.getResourceAsStream("/book1/test_cover.png"), "cover.png"));
            // Add Chapter 1
            book.addSection("Introduction", new Resource(Epublib_example.class.getResourceAsStream("/book1/chapter1.html"), "chapter1.html"));
            // Add Chapter 2
            TOCReference chapter2 = book.addSection("Second Chapter", new Resource(Epublib_example.class.getResourceAsStream("/book1/chapter2.html"), "chapter2.html"));
            // Add Chapter2, Section 1
            book.addSection(chapter2, "Chapter 2, section 1", new Resource(Epublib_example.class.getResourceAsStream("/book1/chapter2_1.html"), "chapter2_1.html"));
            // Add Chapter 3
            book.addSection("Conclusion", new Resource(Epublib_example.class.getResourceAsStream("/book1/chapter3.html"), "chapter3.html"));
            // Create EpubWriter
            EpubWriter epubWriter = new EpubWriter();
            // Write the Book as Epub
            epubWriter.write(book, new FileOutputStream("test1_book1.epub"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}