package org.playstat.parsers.bashim;

import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BashPageParser implements Runnable {
    private final Map<String, BashItem> storage;
    private final String url;
    private static final Logger log = LoggerFactory.getLogger(BashPageParser.class);

    public BashPageParser(Map<String, BashItem> storage, String url) {
        this.storage = storage;
        this.url = url;
    }

    @Override
    public void run() {
        log.info("parse: " + url);
        try {
            final Document doc = Jsoup.connect(url).get();
            final Elements quotes = doc.getElementsByClass("quote");
            for (Element e : quotes) {
                if (!e.getElementsByClass("text").isEmpty()) {
                    final String text = e.getElementsByClass("text").text();
                    if (text.length() > 10) {
                        int rating = 0;
                        try {
                            String textRaiting = e.getElementsByClass("rating").first().text();
                            if ("...".equals(textRaiting) || "???".equals(textRaiting)) {
                                continue;
                            }
                            rating = Integer.parseInt(textRaiting);
                        } catch (Exception ex) {
                            log.warn(ex.getMessage(), ex);
                        }
                        final BashItem item = new BashItem(text);
                        item.setText(text);
                        item.setRate(Long.valueOf(rating));
                        storage.put(item.getId(), item);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
