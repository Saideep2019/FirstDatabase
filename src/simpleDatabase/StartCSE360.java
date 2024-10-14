package simpleDatabase;

import java.sql.SQLException;
import java.util.Scanner;

public class StartCSE360 {

    private static DatabaseHelper databaseHelper;
    private static final Scanner scanner = new Scanner(System.in);
    
    private static final String ADMIN_ROLE = "admin";
    private static final String USER_ROLE = "user";
    private static final String USER_CHOICE_REGISTER = "1";
    private static final String USER_CHOICE_LOGIN = "2";
    private static final String USER_CHOICE_MANAGE_ARTICLES = "3";
    
    private static final String ADMIN_CHOICE = "A";
    private static final String USER_CHOICE = "U";

    public static void main(String[] args) {
        try {
            databaseHelper = new DatabaseHelper();
            databaseHelper.connectToDatabase();

            if (databaseHelper.isDatabaseEmpty()) {
                System.out.println("In-Memory Database is empty");
                setupAdministrator();
            } else {
                System.out.println("If you are an administrator, then select A\nIf you are a user then select U\nEnter your choice:  ");
                String role = scanner.nextLine().trim().toUpperCase();

                switch (role) {
                    case USER_CHOICE:
                        userFlow();
                        break;
                    case ADMIN_CHOICE:
                        adminFlow();
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 'A' or 'U'");
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Good Bye!!");
            databaseHelper.closeConnection();
        }
    }

    private static void setupAdministrator() throws Exception {
        System.out.println("Setting up the Administrator access.");
        String email = promptUser("Enter Admin Email: ");
        String password = promptUser("Enter Admin Password: ");
        databaseHelper.register(email, password, ADMIN_ROLE);
        System.out.println("Administrator setup completed.");
    }

    private static void userFlow() throws Exception {
        System.out.println("User Flow");
        System.out.print("What would you like to do?\n1. Register\n2. Login\n3. Manage Articles\nEnter your choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case USER_CHOICE_REGISTER:
                registerUser();
                break;
            case USER_CHOICE_LOGIN:
                loginUser();
                break;
            case USER_CHOICE_MANAGE_ARTICLES:
                articleFlow();
                break;
            default:
                System.out.println("Invalid choice. Please select a valid option.");
                break;
        }
    }

    private static String promptUser(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static void registerUser() throws Exception {
        String email = promptUser("Enter User Email: ");
        String password = promptUser("Enter User Password: ");
        if (!databaseHelper.doesUserExist(email)) {
            databaseHelper.register(email, password, USER_ROLE);
            System.out.println("User setup completed.");
        } else {
            System.out.println("User already exists.");
        }
    }

    private static void loginUser() throws Exception {
        String email = promptUser("Enter User Email: ");
        String password = promptUser("Enter User Password: ");
        if (databaseHelper.login(email, password, USER_ROLE)) {
            System.out.println("User login successful.");
            // Additional user functionalities can be added here
        } else {
            System.out.println("Invalid user credentials. Try again!!");
        }
    }

    private static void articleFlow() throws Exception {
        while (true) {
            System.out.println("Article Management Menu:");
            System.out.println("1. Create Article");
            System.out.println("2. View Articles");
            System.out.println("3. Delete Article");
            System.out.println("4. Delete All Articles");
            System.out.println("5. Backup Articles");
            System.out.println("6. Restore Articles");
            System.out.println("7. Exit Article Management");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createArticle();
                    break;
                case "2":
                    databaseHelper.displayArticles();
                    break;
                case "3":
                    deleteArticle();
                    break;
                case "4":
                    deleteAllArticles();
                    break;
                case "5":
                    backupArticles();
                    break;
                case "6":
                    restoreArticles();
                    break;
                case "7":
                    return; // Exit article management
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    private static void backupArticles() {
        String backupFilePath = promptUser("Enter backup file path (e.g., C:\\Users\\Saideep\\Documents\\article_backup.txt): ");
        try {
            databaseHelper.backupArticles(backupFilePath);
            System.out.println("Articles backed up successfully to: " + backupFilePath);
        } catch (Exception e) {
            System.out.println("Error backing up articles: " + e.getMessage());
        }
    }

    private static void restoreArticles() {
        String restoreFilePath = promptUser("Enter restore file path (e.g., C:\\Users\\Saideep\\Documents\\article_backup.txt): ");
        try {
            databaseHelper.restoreArticles(restoreFilePath);
            System.out.println("Articles restored successfully from: " + restoreFilePath);
        } catch (Exception e) {
            System.out.println("Error restoring articles: " + e.getMessage());
        }
    }

    private static void deleteAllArticles() throws Exception {
        String confirmation = promptUser("Are you sure you want to delete all articles? (yes/no): ");
        if (confirmation.equalsIgnoreCase("yes")) {
            databaseHelper.deleteAllArticles();
            System.out.println("All articles deleted successfully.");
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private static void createArticle() throws Exception {
        String title = promptUser("Enter Title: ");
        String authors = promptUser("Enter Authors (comma separated): ");
        String abstractText = promptUser("Enter Abstract: ");
        String keywords = promptUser("Enter Keywords (comma separated): ");
        String body = promptUser("Enter Body: ");
        String references = promptUser("Enter References (comma separated): ");

        databaseHelper.createArticle(title, authors, abstractText, keywords, body, references);
        System.out.println("Article created successfully.");
    }

    private static void deleteArticle() throws Exception {
        int articleId = Integer.parseInt(promptUser("Enter Article ID to delete: "));
        String confirm = promptUser("Are you sure you want to delete article with ID " + articleId + "? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            databaseHelper.deleteArticle(articleId);
            System.out.println("Article deleted successfully.");
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private static void adminFlow() throws Exception {
        System.out.println("Admin Flow");
        String email = promptUser("Enter Admin Email: ");
        String password = promptUser("Enter Admin Password: ");
        if (databaseHelper.login(email, password, ADMIN_ROLE)) {
            System.out.println("Admin login successful.");
            databaseHelper.displayUsersByAdmin();
        } else {
            System.out.println("Invalid admin credentials. Try again!!");
        }
    }
}
