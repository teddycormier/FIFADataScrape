package com.mycompany.futwizscrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class FutWizScrape {
    public static void main(String[] args) {
        String url = "https://www.futwiz.com/en//fifa23/player/erling-haaland/37";
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .get();
            Element priceElement = document.selectFirst(".price-num");
            String priceText = priceElement.text();
            System.out.println("Haalands's last sold price: " + priceText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
