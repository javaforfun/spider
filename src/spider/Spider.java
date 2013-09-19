/*
 * File : Spider.java
 * Author : Shuai Li <lishuaihenu@gmail.com>
 * Purpose : spider to capture JD.com, computer channel
 * Created : 2013-09-19 by Shuai Li <lishuaihenu@gmail.com>
 */
package spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 * @author gladiator
 */
public class Spider {

    private int id = 0;
    static private DB db;
    private WebClient webClient;
    static String TARGET_URL = "http://www.jd.com/allSort.aspx";

    public Spider() {
        webClient = new WebClient();
        // JD use JavaScript to display price, so we need enable js
        webClient.setJavaScriptEnabled(true);
        webClient.setCssEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setTimeout(5000);
        webClient.setThrowExceptionOnScriptError(false);
    }

    /**
     * first get item, then collect product in item
     */
    void get_item() throws IOException, SQLException {
        // capture "Computer"(channel 670-671) in http://www.jd.com/allSort.aspx
        String regex = "(http:\/\/list\.jd\.com\/670-671.*\.html)\">(.*)<\/a>";
        Pattern p = Pattern.compile(regex);

        URL url = new URL(TARGET_URL);
        BufferedReader buff = new BufferedReader(
                new InputStreamReader(url.openStream(), "gb2312"));
        String s;
        while ((s = buff.readLine()) != null) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                int num = get_max_page(m.group(1));
                get_product(m.group(1), m.group(2), 1, num);
            }
        }
    }

    /**
     * in each item, there will be different pages
     */
    int get_max_page(String URL) throws MalformedURLException,
            UnsupportedEncodingException, IOException {
        String regex = "pagin-m\'><span\\sclass=\'text\'><i>1<\/i>\/(\\d+)<\/span>";
        Pattern p = Pattern.compile(regex);

        URL url = new URL(URL);
        BufferedReader buff = new BufferedReader(
                new InputStreamReader(url.openStream(), "gb2312"));
        String s;

        StringBuffer sb = new StringBuffer("");
        while ((s = buff.readLine()) != null) {
            sb.append(s);
        }
        Matcher m = p.matcher(sb);
        if (m.find()) {
            int num = Integer.parseInt(m.group(1));
            return num;
        }
        return 1;
    }

    void get_product(String URL, String category, int index, int max)
            throws IOException, SQLException {

        System.out.println("category: " + category + ", index is: " + index + ", max is: " + max);
        String name = null, description = null;
        int price = 0;

        HtmlPage page = webClient.getPage(URL);

        String strList = page.getHtmlElementById("plist").asText();
        String textStr[] = strList.split("\n");
        System.out.println(strList);
        System.out.println("**************************************************");
        System.out.println("**************************************************");

        Pattern p1 = Pattern.compile("^(\\S*\\s\\S*)\\s(.*)");
        // String or integer
        Pattern p2 = Pattern.compile("\\d+");
        // Pattern p2 = Pattern.compile("[0-9]+(.[0-9]+)?");

        for (int i = 0; i < textStr.length; i += 4) {
            Matcher m1 = p1.matcher(textStr[i]);
            if (m1.find()) {
                // use top 2 words for name, others for description
                name = m1.group(1);
                description = m1.group(2);
            } else if (textStr[i].contains(" ")) {
                // use first word for name, last for description
                String token[] = textStr[i].split("\\s");
                name = token[0];
                description = token[1];
            } else {
                // use this word for both name and description
                name = textStr[i];
                description = textStr[i];
            }

            Matcher m2 = p2.matcher(textStr[i + 1]);
            if (m2.find()) {
                price = Integer.parseInt(m2.group());
            }
            db.insert(id, name, category, price, description);
            id++;
        }

        if (index < max) {
            String nextPage = get_next_page(URL, index);
            get_product(nextPage, category, index + 1, max);
        }
    }

    String get_next_page(String URL, int index) {
        String str;
        if (index == 1) {
            str = URL.replaceFirst("\.html", "-0-0-0-0-0-0-0-1-1-2-1.html");
        } else {
            str = URL.replaceFirst("\\d+-.\.html", (index + 1) + "-1.html");
        }
        return str;
    }

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, SQLException {

        db = new DB();

        Spider spider = new Spider();

        System.out.println("start scan...");
        spider.get_item();
        System.out.println("scan finished.");

        db.close();
    }
}
