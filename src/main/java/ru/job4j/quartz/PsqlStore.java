package ru.job4j.quartz;

import ru.job4j.model.Post;
import ru.job4j.repo.Store;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection connection;

    public PsqlStore(Properties config) throws SQLException {
        try {
            Class.forName(config.getProperty("driver-class-name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection = DriverManager.getConnection(config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password"));
    }

    public static void main(String[] args) {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            Properties config = new Properties();
            config.load(in);
            PsqlStore psqlStore = new PsqlStore(config);
            Post post = new Post("test", "https://abcd.ru", "description", LocalDateTime.now().withNano(0));
            Post post1 = new Post("test", "https://abcd.ru", "description", LocalDateTime.now().withNano(0));
            Post post2 = new Post("test", "https://abcde.ru", "description", LocalDateTime.now().withNano(0));
            psqlStore.save(post);
            psqlStore.save(post1);
            psqlStore.save(post2);
            for (Post post4 : psqlStore.getAll()) {
                System.out.println(post4);
            }
            System.out.println(psqlStore.findById(7));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps =
                connection.prepareStatement("insert into post(name, text, link, created) "
                                              + "values (?, ?, ?, ?) "
                                              + "on conflict(link) "
                                              + "do nothing;", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
            try (ResultSet generatedId = ps.getGeneratedKeys()) {
                if (generatedId.next()) {
                    post.setId(generatedId.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps =
                connection.prepareStatement("select * from post")) {
            ps.execute();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(rs.getInt(1),
                                        rs.getString(2),
                                        rs.getString(3),
                                        rs.getString(4),
                                        rs.getTimestamp(5).toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps =
                connection.prepareStatement("select * from post where id = ?;")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    post = new Post(rs.getInt(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4),
                                    rs.getTimestamp(5).toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
