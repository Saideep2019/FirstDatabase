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
import java.util.List;

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
        
    }
    
    
    
    
    
    
    
    public Article getArticleById(int articleId) throws Exception {
        Article article = null;
        String query = "SELECT * FROM articles WHERE id = ?"; // Adjust the table name as per your schema

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, articleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String title = resultSet.getString("title");
                String authors = resultSet.getString("authors");
                String abstractText = resultSet.getString("abstract");
                String keywords = resultSet.getString("keywords");
                String body = resultSet.getString("body");
                String references = resultSet.getString("references");

                article = new Article(title, authors, abstractText, keywords, body, references);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error fetching article: " + e.getMessage());
        }

        return article; // Returns null if not found
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
            e.printStackTrace();
            throw new Exception("Error updating article: " + e.getMessage());
        }
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
    
    
    
    public void deleteAllArticles() throws SQLException {
        String sql = "DELETE FROM articles"; // Replace "articles" with your actual table name
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " articles deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
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

    // Method to register a user
    public void register(String email, String password, String role) throws Exception {
        byte[] iv = EncryptionUtils.getInitializationVector(email.toCharArray());
        String encryptedPassword = Base64.getEncoder().encodeToString(
            encryptionHelper.encrypt(password.getBytes(), iv)
        );
        
        String insertUser = "INSERT INTO cse360users (email, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, email);
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        }
    }

    // Method to log in a user
    public boolean login(String email, String password, String role) throws Exception {
        byte[] iv = EncryptionUtils.getInitializationVector(email.toCharArray());
        String encryptedPassword = Base64.getEncoder().encodeToString(
            encryptionHelper.encrypt(password.getBytes(), iv)
        );  
        
        String query = "SELECT * FROM cse360users WHERE email = ? AND password = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Method to display all users
    public void displayUsersByAdmin() throws Exception {
        String sql = "SELECT * FROM cse360users"; 
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql); 

        while(rs.next()) { 
            int id = rs.getInt("id"); 
            String email = rs.getString("email"); 
            String role = rs.getString("role");  
            String encryptedPassword = rs.getString("password"); 

            byte[] decodedPassword = Base64.getDecoder().decode(encryptedPassword);
            char[] decryptedPassword = EncryptionUtils.toCharArray(
                encryptionHelper.decrypt(decodedPassword, EncryptionUtils.getInitializationVector(email.toCharArray()))
            );

            System.out.print("ID: " + id); 
            System.out.print(", Email: " + email); 
            System.out.print(", Password: "); 
            EncryptionUtils.printCharArray(decryptedPassword);
            System.out.println(", Role: " + role); 
            
            Arrays.fill(decryptedPassword, '0'); // Clear sensitive data
        } 
    }
    
    
    
    

    // Method to create an article
    public void createArticle(String title, String authors, String abstractText, String keywords, String body, String references) throws SQLException {
        String insertArticle = "INSERT INTO articles (title, authors, abstract, keywords, body, references) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
            pstmt.setString(1, title);
            pstmt.setString(2, authors);
            pstmt.setString(3, abstractText);
            pstmt.setString(4, keywords);
            pstmt.setString(5, body);
            pstmt.setString(6, references);
            pstmt.executeUpdate();
        }
    }

    // Method to display all articles
    public void displayArticles() throws SQLException {
        String sql = "SELECT * FROM articles";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql); 

        while (rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String authors = rs.getString("authors");
            String abstractText = rs.getString("abstract");
            String keywords = rs.getString("keywords");
            String body = rs.getString("body");
            String references = rs.getString("references");

            System.out.println("ID: " + id);
            System.out.println("Title: " + title);
            System.out.println("Authors: " + authors);
            System.out.println("Abstract: " + abstractText);
            System.out.println("Keywords: " + keywords);
            System.out.println("Body: " + body);
            System.out.println("References: " + references);
            System.out.println("---------------------------------");
        }
    }

    // Method to delete an article by ID
    public void deleteArticle(int articleId) throws SQLException {
        String deleteArticle = "DELETE FROM articles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
            pstmt.setInt(1, articleId);
            pstmt.executeUpdate();
        }
    }
    
    
    
    
 // Method to restore all articles from a CSV file
    public void restoreArticles(String backupFilePath) throws SQLException {
        try (BufferedReader br = new BufferedReader(new FileReader(backupFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(","); // Assuming fields are separated by commas
                if (fields.length < 6) continue; // Skip invalid lines

                String title = fields[0];
                String authors = fields[1];
                String abstractText = fields[2];
                String keywords = fields[3];
                String body = fields[4];
                String references = fields[5];

                // Check if the article already exists
                if (!articleExists(title)) { // You need to implement this method
                    createArticle(title, authors, abstractText, keywords, body, references);
                } else {
                    System.out.println("Article with title \"" + title + "\" already exists. Skipping...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SQLException("Error restoring articles: " + e.getMessage());
        }
    }
    
    public void backupArticles(String backupFilePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(backupFilePath))) {
            String sql = "SELECT title, authors, abstract, keywords, body, references FROM articles";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    String authors = rs.getString("authors");
                    String abstractText = rs.getString("abstract");
                    String keywords = rs.getString("keywords");
                    String body = rs.getString("body");
                    String references = rs.getString("references");

                    // Write to the backup file, omitting the ID
                    bw.write(title + "," + authors + "," + abstractText + "," + keywords + "," + body + "," + references);
                    bw.newLine();
                }
            } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error backing up articles: " + e.getMessage());
        }
    }

    

    
    private boolean articleExists(String title) throws SQLException {
        String query = "SELECT COUNT(*) FROM articles WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if count is greater than 0
            }
        }
        return false; // Default to false if any exception occurs
    }
    


    // Method to close the database connection
    public void closeConnection() {
        try { 
            if (statement != null) statement.close(); 
        } catch (SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if (connection != null) connection.close(); 
        } catch (SQLException se) { 
            se.printStackTrace(); 
        } 
    }

    // Method to check if a user exists
    public boolean doesUserExist(String email) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // returns true if a user with the email exists
        }
    }

    // Method to check if the database is empty
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) FROM cse360users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) == 0; // true if count is 0
            }
        }
        return true; // Default to true if any exception occurs
    }
}
