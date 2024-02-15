package ru.job4j.quartz;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.model.Post;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;
import ru.job4j.utils.Parse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final String SOURCE_LINK = "https://career.habr.com";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (Post post : habrCareerParse.list(SOURCE_LINK)) {
            System.out.println(post);
        }
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements element = document.select(".vacancy-description__text");
        return element.first().text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String fullLink = "%s%s%d%s".formatted(link, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element dateElement = row.select(".vacancy-card__date").first();

                    Element linkElement = titleElement.child(0);
                    Element linkElementDate = dateElement.child(0);

                    String vacancyName = titleElement.text();
                    DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
                    String linkVacancies = String.format("%s%s", link, linkElement.attr("href"));
                    LocalDateTime date = dateTimeParser.parse(linkElementDate.attr("datetime"));
                    try {
                        posts.add(new Post(vacancyName, linkVacancies, retrieveDescription(linkVacancies), date));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

}
