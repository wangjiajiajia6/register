package org.example.system_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication

public class System2Application {
    public static void main(String[] args) {
        // Initialize database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Start the server
            UserController controller = new UserController();
            controller.startServer();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Failed to start server");
            e.printStackTrace();
        }
    }
}
