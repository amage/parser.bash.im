package org.playstat.parsers.bashim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BashParser {
    private static final int THREADS_COUNT = Runtime.getRuntime().availableProcessors();
    private static final Logger log = LoggerFactory.getLogger(BashParser.class);

    private BashParser() {
        // no-op
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final Long startTime = System.currentTimeMillis();

        final Document doc = Jsoup.connect("http://bash.im/").get();
        final int pageCount = getPageCount(doc);
        final LinkedList<String> queue = generateMap(pageCount);
        final Map<String, BashItem> storage = Collections.synchronizedMap(new HashMap<>());
        final ExecutorService executor = new ThreadPoolExecutor(THREADS_COUNT, THREADS_COUNT, 2L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(THREADS_COUNT * 3), new ThreadPoolExecutor.CallerRunsPolicy());

        while (!queue.isEmpty()) {
            executor.execute(new BashPageParser(storage, queue.removeFirst()));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.MINUTES);
        storeToFile(storage);

        final long runTime = System.currentTimeMillis() - startTime;
        final String stat = String.format("Parse time: %s for %s pages (%.2f page/sec)", runTime, pageCount,
                pageCount / ((double) runTime / 1000.0));
        log.info(stat);
    }

    private static LinkedList<String> generateMap(int pageCount) {
        final LinkedList<String> result = new LinkedList<>();
        for (long pageNum = pageCount; pageNum > 0; pageNum--) {
            result.add("http://bash.im/index/" + pageNum);
        }
        return result;
    }

    private static int getPageCount(Document doc) {
        String pageCount = doc.getElementsByClass("pager").get(0).getElementsByTag("input").get(0).attr("max");
        return Integer.parseInt(pageCount);
    }

    private static void storeToFile(Map<String, BashItem> storage) throws IOException {
        try (FileWriter out = new FileWriter(new File("bashes-rated.txt"))) {
            for (Entry<String, BashItem> item : storage.entrySet()) {
                out.append(item.getValue().getRate() + " " + item.getValue().getText());
                out.append("\n");
            }
        }
    }

}
