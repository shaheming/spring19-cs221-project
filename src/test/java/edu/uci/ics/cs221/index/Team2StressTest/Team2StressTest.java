package edu.uci.ics.cs221.index.Team2StressTest;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team2StressTest {

    Analyzer analyzer;
    InvertedIndexManager invertedIndexManager;

    /**
     * Instantiate the invertedIndexManager.
     * Import and add external documents to get prepared for the tests.
     */
    @Before
    public void init() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
        invertedIndexManager = InvertedIndexManager.createOrOpen("./index/Team2StressTest/", analyzer);
        String text = "";
        try {
            URL url = new URL("http://www.gutenberg.org/cache/epub/42671/pg42671.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(inputLine);
                stringBuilder.append(System.lineSeparator());
            }

            bufferedReader.close();
            text = stringBuilder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // copy full pride-and-prejudice for 5000 times, every document is about 708KB, 5000 times is about 3.46GB
        for (int i = 0; i < 5000; i++) {
            invertedIndexManager.addDocument(new Document(text));
        }
        // Then, add two small test documents
        invertedIndexManager.addDocument(new Document("qwertyuiop elizabeth"));
        invertedIndexManager.addDocument(new Document("qwertyuiop"));
    }

    /**
     * Using search keywords query. Check whether the number of the documents containing "elizabeth" is 5001,
     * the purpose of doing this is to check whether the system can handle large amounts of data
     * and process it correctly
     */
    @Test
    public void test1(){
        Iterator<Document> result = invertedIndexManager.searchQuery("elizabeth");

        int counter = 0;
        while (result.hasNext()) {
            counter++;
            result.next();
        }
        assertEquals(5001, counter);
    }

    /**
     * Test whether searchAndQuery() works well in large datasets
     */
    @Test
    public void test2(){
        List<String> keywords = Arrays.asList("qwertyuiop", "elizabeth");

        Iterator<Document> result = invertedIndexManager.searchAndQuery(keywords);

        int counter = 0;
        while (result.hasNext()) {
            counter++;
            result.next();
        }
        assertEquals(1, counter);
    }

    /**
     * Test whether searchOrQuery() works well in large datasets
     */
    @Test
    public void test3(){
        List<String> keywords = Arrays.asList("qwertyuiop", "elizabeth");

        Iterator<Document> result = invertedIndexManager.searchOrQuery(keywords);

        int counter = 0;
        while (result.hasNext()) {
            counter++;
            result.next();
        }
        assertEquals(5002, counter);
    }

    /**
     * Delete all the files and empty or non-empty folders in ./index/Team2StressTest/
     * Use recursive delete in case any sub folders is created
     */
    @After
    public void after(){
        String path = "./index/Team2StressTest/";
        if (delAllFile(path)) {
            System.out.println("All files in " + path + " are deleted.");
        } else {
            System.out.println("Deletion of all files in " + path + " failed to complete.");
        }
    }

    private boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Path: " + file.toString() + "is not a valid file.");
            return flag;
        }
        if (!file.isDirectory()) {
            System.out.println("Path: " + file.toString() + "is not a valid directory");
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        if (tempList == null || tempList.length == 0) {
            System.out.println("No files in current folder " + file.toString());
            return true;
        }
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                if (temp.delete()) {
                    System.out.println("File: " + temp + " is successfully deleted.");
                } else {
                    System.out.println("File: " + temp + " failed to be deleted.");
                }
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }

    private void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            java.io.File myFilePath = new java.io.File(folderPath);
            if (myFilePath.delete()) {
                System.out.println(folderPath + " folder is successfully deleted");
            } else {
                System.out.println(folderPath + " folder failed to be deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}