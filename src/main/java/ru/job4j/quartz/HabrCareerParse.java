package ru.job4j.quartz;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;

public class HabrCareerParse {
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final String SOURCE_LINK = "https://career.habr.com";

    public static void main(String[] args) throws IOException {

        HabrCareerParse habrCareerParse = new HabrCareerParse();

        for (int i = 1; i <= 5; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dateElement = row.select(".vacancy-card__date").first();

                Element linkElement = titleElement.child(0);
                Element linkElementDate = dateElement.child(0);

                String vacancyName = titleElement.text();
                DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = String.format("дата создания ваканции : %s", dateTimeParser.parse(linkElementDate.attr("datetime")));
                System.out.printf("%s%s%s%n", vacancyName, link, date);
            });
        }
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements element = document.select(".vacancy-description__text");
        return element.first().text();
    }
}
