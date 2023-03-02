package com.mycompany.futwizscrape;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FutWizScrape {

    private static final int THREAD_POOL_SIZE = 10;
    private static final int MAX_PAGES = 800;
    private static final AtomicInteger PRINTED_LINE_COUNTER = new AtomicInteger(0);
    private static final AtomicBoolean KEEP_SEARCHING = new AtomicBoolean(true);

    private static class PlayerSearchTask implements Callable<Integer> {

        private final String playerNameInput;
        private final int startPage;
        private final int endPage;
        private final Scanner scanner;

        public PlayerSearchTask(String playerNameInput, int startPage, int endPage, Scanner scanner) {
            this.playerNameInput = playerNameInput;
            this.startPage = startPage;
            this.endPage = endPage;
            this.scanner = scanner;
        }

        @Override
        public Integer call() {
            for (int pageNum = startPage; pageNum <= endPage; pageNum++) {
                String url = "https://www.futwiz.com/en/fifa23/players?page=" + pageNum;
                Document doc;

                try {
                    doc = Jsoup.connect(url).get();
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 500) {
                        System.out.println("Retrying connection...");
                        if (pageNum < MAX_PAGES) {
                            return pageNum;
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (IOException e) {
                    System.out.println("\nError: Failed to connect to server. Please check your internet connection. - Retrying...");
                    return pageNum;
                }

                Elements playerRows = doc.select(".table-row");
                for (Element playerRow : playerRows) {
                    String playerName = playerRow.select(".player a[href*=player] b").text();
                    String playerRating = playerRow.select(".otherversion23-txt").text();
                    String playerPrice = playerRow.select("td:nth-of-type(5)").text();
                    if (playerNameInput.equalsIgnoreCase(playerName)) {
                        if (PRINTED_LINE_COUNTER.get() < 3) {
                            if (playerPrice.isEmpty()) {
                                playerPrice = "sbc/untradable";
                            }
                            System.out.println(playerName + "  Overall: " + playerRating + "  Last Sold For: " + playerPrice);
                            System.out.println(PRINTED_LINE_COUNTER.incrementAndGet());
                        }
                        if (PRINTED_LINE_COUNTER.get() == 3) {
                            System.out.print("\nDo you want to continue? (Y/N): ");
                            String input = scanner.nextLine();
                            if (input.equalsIgnoreCase("n")) {
                                KEEP_SEARCHING.set(false);
                                return null;
                            } else {
                                PRINTED_LINE_COUNTER.set(0);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int pagesPerThread = MAX_PAGES / THREAD_POOL_SIZE;

        while (true) {
            System.out.print("\nENTER PLAYER NAME ('STOP' TO QUIT): \n");
            String playerNameInput = scanner.nextLine();

            if (playerNameInput.equalsIgnoreCase("stop")) {
                executor.shutdown();
                return;
            }

            PRINTED_LINE_COUNTER.set(0);
            KEEP_SEARCHING.set(true);
            for (int i = 0; i < THREAD_POOL_SIZE && KEEP_SEARCHING.get(); i++) {
                int startPage = i * pagesPerThread;
                int endPage = (i + 1) * pagesPerThread;
                if (i == THREAD_POOL_SIZE - 1) {
                    endPage = MAX_PAGES;
                }
                Future<Integer> future = executor.submit(new PlayerSearchTask(playerNameInput, startPage, endPage, scanner));
                Integer failedPageNum = future.get();
            }

            if (KEEP_SEARCHING.get()) {
                System.out.println("Player not found.");
            }
        }
    }
}
