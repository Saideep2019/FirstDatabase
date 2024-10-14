package simpleDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.Base64;

import Encryption.EncryptionHelper;
import Encryption.EncryptionUtils;

class DatabaseHelper {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/firstDatabase";  

    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private Connection connection = null;
    private Statement statement = null;
    private EncryptionHelper encryptionHelper;

    public DatabaseHelper() throws Exception {
        encryptionHelper = new EncryptionHelper();
        connectToDatabase();
    }

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            createTables();  // Create the necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    public Article getArticleById(int articleId) throws Exception {
        Article article = null;
        String query = "SELECT * FROM articles WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, articleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String title = resultSet.getString("title");
                String authors = resultSet.getString("authors");

                // Retrieve the encrypted fields
                byte[] encryptedAbstract = resultSet.getBytes("abstract");
                byte[] encryptedBody = resultSet.getBytes("body");
                byte[] encryptedReferences = resultSet.getBytes("references");

                // Assuming the first 16 bytes are the IV and the rest is the encrypted data
                char[] abstractText = decryptField(encryptedAbstract);
                char[] body = decryptField(encryptedBody);
                char[] references = decryptField(encryptedReferences);

                article = new Article(title, authors, new String(abstractText), 
                                      resultSet.getString("keywords"), 
                                      new String(body), 
                                      new String(references));
            }
        } catch (Exception e) {
            throw new Exception("Error fetching article: " + e.getMessage(), e);
        }

        return article; // Returns null if not found
    }

    private char[] decryptField(byte[] encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.length < 16) {
            System.out.println("Error: Encrypted data is too short.");
            return null;
        }

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
        byte[] actualEncryptedData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

        return encryptionHelper.decryptToCharArray(actualEncryptedData, iv);
    }

    public void updateArticle(int articleId, Article article) throws Exception {
        String query = "UPDATE articles SET title = ?, authors = ?, abstract = ?, keywords = ?, body = ?, references = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, article.getTitle());
            preparedStatement.setString(2, article.getAuthors());
            preparedStatement.setString(3, article.getAbstractText());
            preparedStatement.setString(4, article.getKeywords());
            preparedStatement.setString(5, article.getBody());
            preparedStatement.setString(6, article.getReferences());
            preparedStatement.setInt(7, articleId); // Set the ID of the article to update

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Article updated successfully.");
            } else {
                System.out.println("No article found with the given ID.");
            }
        } catch (Exception e) {
            throw new Exception("Error updating article: " + e.getMessage(), e);
        }
    }

    public void deleteAllArticles() throws SQLException {
        String sql = "DELETE FROM articles";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " articles deleted.");
        } catch (SQLException e) {
            throw new SQLException("Error deleting all articles", e);
        }
    }

    private void createTables() throws SQLException {
        // User table creation
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, " 
                + "email VARCHAR(255) UNIQUE, " 
                + "password VARCHAR(255), " 
                + "role VARCHAR(20))";
        statement.execute(userTable);
        
        // Article table creation
        String articleTable = "CREATE TABLE IF NOT EXISTS articles (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, " 
                + "title VARCHAR(255), " 
                + "authors VARCHAR(255), " 
                + "abstract TEXT, " 
                + "keywords VARCHAR(255), " 
                + "body TEXT, " 
                + "references TEXT)";
        statement.execute(articleTable);
    }

    // Other methods ...

    // Method to close the database connection
    public void closeConnection() {
        try { 
            if (statement != null) statement.close(); 
            if (connection != null) connection.close();
        } catch (SQLException se2) { 
            System.err.println("Error closing resources: " + se2.getMessage());
        }
    }
}
