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
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FutWizScrape {

    private static JTextField NameTextField;
    private static JTextField RatingTextField;
    private static JTextField TypeTextField;
    private static JTextArea PlayerDetailsTextArea;
    private static final int THREAD_POOL_SIZE = 1;
    private static final int MAX_PAGES = 800;
    private static final AtomicInteger PRINTED_LINE_COUNTER = new AtomicInteger(0);
    protected static final AtomicBoolean KEEP_SEARCHING = new AtomicBoolean(true);

    protected static class PlayerSearchTask implements Callable<Integer> {

        private final String playerNameInput;
        private final String playerRatingInput;
        private final String playerTypeInput;
        private final int startPage;
        private final int endPage;
        private final Scanner scanner;

        public PlayerSearchTask(String playerNameInput, String playerRatingInput, String playerTypeInput, int startPage, int endPage, Scanner scanner) {
            this.playerNameInput = playerNameInput;
            this.playerRatingInput = playerRatingInput;
            this.playerTypeInput = playerTypeInput;
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
                        System.out.println("\nError: Server error. Retrying page...");
                        continue;
                    } else {
                        System.out.println("\nError: Failed to connect to server. Please check your internet connection. Retrying page...");
                        continue;
                    }
                } catch (IOException e) {
                    System.out.println("\nError: Failed to connect to server. Please check your internet connection. Retrying page...");
                    continue;
                }

                Elements playerRows = doc.select(".table-row");
                for (Element playerRow : playerRows) {
                    String playerName = playerRow.select(".player a[href*=player] b").text();
                    Element playerType = playerRow.select("div." + playerTypeInput).first();
                    String playerRating = "";
                    if (playerType != null) {
                        playerRating = playerType.select(".otherversion23-txt").text();
                    }
                    String playerPrice = playerRow.select("td:nth-of-type(5)").text();
                    if (playerType != null && playerType.hasClass(playerTypeInput)) {
                        if (PRINTED_LINE_COUNTER.get() < 1) {
                            if ((playerNameInput.equalsIgnoreCase(playerName)) && (playerRatingInput.equalsIgnoreCase(playerRating)) && (!playerPrice.isEmpty())) {
                                System.out.println("\n-----------------------------------------------");
                                System.out.println(playerName + " | Overall: " + playerRating + "  Last Sold For: " + playerPrice);
                                PRINTED_LINE_COUNTER.incrementAndGet();
                            }
                        } else {
                            PRINTED_LINE_COUNTER.set(0);
                            KEEP_SEARCHING.set(false);
                            return null;
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
        boolean continueSearching = true;
        String playerTypeInputOne = "otherversion23-";

        while (continueSearching) {
            System.out.println("\n------------------------------------");
            System.out.print("| Enter Player Info ('STOP' to quit)  \n");
            System.out.print("| Player Name: \n");
            System.out.print("| Player Rating: \n");
            System.out.print("| Player Type: \n");
            System.out.println("------------------------------------");
            String playerNameInput = scanner.nextLine().toLowerCase();
            String playerRatingInput = scanner.nextLine().toLowerCase();
            String playerTypeInputTwo = scanner.nextLine().toLowerCase();

            if ((playerNameInput.equalsIgnoreCase("stop")) || (playerRatingInput.equalsIgnoreCase("stop"))) {
                executor.shutdown();
                continueSearching = false;
            } else {
                PRINTED_LINE_COUNTER.set(0);
                KEEP_SEARCHING.set(true);
                for (int i = 0; i < THREAD_POOL_SIZE && KEEP_SEARCHING.get(); i++) {
                    int startPage = i * pagesPerThread;
                    int endPage = (i + 1) * pagesPerThread;
                    if (i == THREAD_POOL_SIZE - 1) {
                        endPage = MAX_PAGES;
                    }

                    switch (playerTypeInputTwo.toLowerCase()) {
                        case "toty icon" ->
                            playerTypeInputTwo = "totyicon";
                        case "ucl live" ->
                            playerTypeInputTwo = "ucllive";
                        case "gold inform", "inform gold" ->
                            playerTypeInputTwo = "goldif";
                        case "centurion", "centurions" ->
                            playerTypeInputTwo = "cent";
                        case "winter wildcard" ->
                            playerTypeInputTwo = "wwreward";
                        case "road to world cup", "road to wc" ->
                            playerTypeInputTwo = "roadtowc";
                        case "future stars", "future star" ->
                            playerTypeInputTwo = "ffs";
                        case "toty nominee" ->
                            playerTypeInputTwo = "totynominee";
                        case "wc stories", "world cup stories" ->
                            playerTypeInputTwo = "wcstories";
                        case "wc icon", "world cup icon" ->
                            playerTypeInputTwo = "wcicon";
                        case "shape shifter" ->
                            playerTypeInputTwo = "shapeshifter";
                        case "europa league rttf", "europa league road to the final" ->
                            playerTypeInputTwo = "uelrttf";
                        case "champions league rttf", "champions league road to the final" ->
                            playerTypeInputTwo = "uclrttf";
                        case "wc tott", "world cup tott", "world cup team of the tournament" ->
                            playerTypeInputTwo = "wctott";
                        case "wc phenoms", "world cup phenoms" ->
                            playerTypeInputTwo = "wcphenoms";
                        case "rule breaker", "rulebreakers" ->
                            playerTypeInputTwo = "rulebreakers";
                        case "world cup hero" ->
                            playerTypeInputTwo = "futherowc";
                        case "fut hero", "hero" ->
                            playerTypeInputTwo = "futhero";
                        case "fantasy team 1" ->
                            playerTypeInputTwo = "fantasy1";
                        case "non rare gold" ->
                            playerTypeInputTwo = "gold-nr";
                        default -> {
                        }
                    }

                    String playerTypeInput = playerTypeInputOne + playerTypeInputTwo;

                    if (Integer.parseInt(playerRatingInput) >= 92) {
                        startPage = 0;
                    } else if (Integer.parseInt(playerRatingInput) >= 90) {
                        startPage = 4;
                    } else if (Integer.parseInt(playerRatingInput) >= 88) {
                        startPage = 12;
                    } else if (Integer.parseInt(playerRatingInput) >= 85) {
                        startPage = 26;
                    } else if (Integer.parseInt(playerRatingInput) >= 83) {
                        startPage = 53;
                    } else if (Integer.parseInt(playerRatingInput) >= 82) {
                        startPage = 70;
                    } else if (Integer.parseInt(playerRatingInput) >= 80) {
                        startPage = 76;
                    } else if (Integer.parseInt(playerRatingInput) >= 79) {
                        startPage = 96;
                    } else if (Integer.parseInt(playerRatingInput) >= 77) {
                        startPage = 132;
                    } else {
                        startPage = 76;
                    }

                    System.out.println("Searching...");

                    Future<Integer> future = executor.submit(new PlayerSearchTask(playerNameInput, playerRatingInput, playerTypeInput, startPage, endPage, scanner));
                    Integer failedPageNum = future.get();
                }

                if (KEEP_SEARCHING.get()) {
                    System.out.println("Player not found.");
                }
            }
        }
    }
}
