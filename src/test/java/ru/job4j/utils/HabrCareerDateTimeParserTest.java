package ru.job4j.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;
class HabrCareerDateTimeParserTest {
    @Test
    void whenDateTimeYYYYMMDDhhmmss () {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String date = "2024-05-06T00:00:00+00:00";
        assertThat(parser.parse(date)).isEqualTo(LocalDateTime.of(2024, 05, 06 , 00, 00, 00));
    }

    @Test
    void whenParseFailed() {
        String date = "2021-11-25T15:25:10";
        DateTimeParser parser = new HabrCareerDateTimeParser();
        assertThatThrownBy(() -> parser.parse(date)).isInstanceOf(DateTimeParseException.class);
    }
}