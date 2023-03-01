package com.mycompany.futwizscrape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FutWizScrape {

    private static final int THREAD_POOL_SIZE = 5;

    private static class PlayerSearchTask implements Runnable {

        private final String playerNameInput;
        private final int pageNum;
        private final Scanner scanner;
        private int printedLine = 0;

        public PlayerSearchTask(String playerNameInput, int pageNum, Scanner scanner) {
            this.playerNameInput = playerNameInput;
            this.pageNum = pageNum;
            this.scanner = scanner;
        }

        @Override
        public void run() {
            String url = "https://www.futwiz.com/en/fifa23/players?page=" + pageNum;
            Document doc = null;

            try {
                doc = Jsoup.connect(url).get();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 500) {
                    System.out.println("Retrying connection...");
                    if (pageNum < 800) {
                        new Thread(new PlayerSearchTask(playerNameInput, pageNum, scanner)).start();
                        return;
                    } else {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("\nError: Failed to connect to server. Please check your internet connection. - Retrying...");
                new Thread(new PlayerSearchTask(playerNameInput, pageNum, scanner)).start();
                return;
            }

            Elements playerRows = doc.select(".table-row");
            int printedLine = 0;
            for (Element playerRow : playerRows) {
                String playerName = playerRow.select(".player a[href*=player] b").text();
                String playerRating = playerRow.select(".otherversion23-txt").text();
                String playerPrice = playerRow.select("td:nth-of-type(5)").text();
                if (playerNameInput.equalsIgnoreCase(playerName)) {
                    if (printedLine < 3) {
                        if (playerPrice.equals("")) {
                            playerPrice = "sbc/untradable";
                        }
                        System.out.println(playerName + "  Overall: " + playerRating
                                + "  Last Sold For: " + playerPrice);
                        printedLine++;
                    } else {
                        System.out.println("Would you like me to keep searching? (y/n)");
                        String YNresponse = scanner.nextLine();
                        if (YNresponse.equalsIgnoreCase("y")) {
                            if (playerPrice.equals("")) {
                                playerPrice = "sbc/untradable";
                            }
                            System.out.println(playerName + "  Overall: " + playerRating
                                    + "  Last Sold For: " + playerPrice);
                            printedLine++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        while (true) {
            System.out.print("\nENTER PLAYER NAME ('STOP' TO QUIT): \n");
            String playerNameInput = scanner.nextLine();

            if (playerNameInput.equalsIgnoreCase("stop")) {
                executor.shutdown();
                return;
            }

            List<Callable<Object>> tasks = new ArrayList<>();
            for (int pageNum = 0; pageNum < 800; pageNum++) {
                tasks.add(Executors.callable(new PlayerSearchTask(playerNameInput, 
                                             pageNum, scanner)));
            }
            executor.invokeAll(tasks);
            System.out.println("Player not found.");
        }
    }

}
