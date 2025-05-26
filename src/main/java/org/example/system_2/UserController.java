package org.example.system_2;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/register", new RegisterHandler());

        // 添加全局的OPTIONS请求处理
        server.createContext("/", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            exchange.sendResponseHeaders(404, 0);
            exchange.getResponseBody().close();
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // 设置CORS头
                setCorsHeaders(exchange);

                // 处理OPTIONS预检请求
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                    return;
                }

                // 读取请求体
                InputStream requestBody = exchange.getRequestBody();
                String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Received request: " + body);

                // 解析JSON
                User user;
                try {
                    user = parseUserFromJson(body);
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid JSON format\"}");
                    return;
                }

                // 验证用户输入
                if (user.getUsername() == null || user.getUsername().isEmpty() ||
                        user.getPassword() == null || user.getPassword().isEmpty()) {
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Username and password cannot be empty\"}");
                    return;
                }

                // 处理注册逻辑
                String response;
                int statusCode;

                if (userService.isUserExists(user.getUsername())) {
                    response = "{\"success\":false,\"message\":\"Username already exists\"}";
                    statusCode = 400;
                } else if (userService.createUser(user)) {
                    response = "{\"success\":true,\"message\":\"User registered successfully\"}";
                    statusCode = 201;
                } else {
                    response = "{\"success\":false,\"message\":\"Failed to register user\"}";
                    statusCode = 500;
                }

                sendResponse(exchange, statusCode, response);

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"success\":false,\"message\":\"Internal server error\"}");
            }
        }

        private User parseUserFromJson(String json) throws IllegalArgumentException {
            // 改进的JSON解析
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new IllegalArgumentException("Invalid JSON format");
            }

            String username = extractValue(json, "username");
            String password = extractValue(json, "password");

            if (username == null || password == null) {
                throw new IllegalArgumentException("Missing required fields");
            }

            return new User(username, password);
        }

        private String extractValue(String json, String key) {
            String keyPattern = "\"" + key + "\":";
            int keyIndex = json.indexOf(keyPattern);
            if (keyIndex == -1) return null;

            int valueStart = json.indexOf("\"", keyIndex + keyPattern.length());
            if (valueStart == -1) return null;

            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;

            return json.substring(valueStart + 1, valueEnd);
        }
    }

    // 设置CORS头
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");
    }

    // 发送响应工具方法
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}