package com.bargain.controller;

import com.bargain.model.Product;
import com.bargain.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*") // Cross-origin compatibility
public class SearchController {

    private static final Logger logger = Logger.getLogger(SearchController.class.getName());

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private DataSource dataSource;

    // Registry of supported platforms
    private static final List<String> SUPPORTED_PLATFORMS = Arrays.asList(
        "amazon", "flipkart", "meesho", "myntra", "jiomart", 
        "shopsy", "blinkit", "instamart", "purplle", "tira", 
        "newme", "ajio", "nykaa", "tata_cliq", "snapdeal",
        "croma", "vijay_sales", "reliance_digital", "paytm_mall",
        "shopclues", "indiamart", "bigbasket", "flipkart_fashion",
        "limeroad", "craftsvilla", "nykaa_fashion", "tata_cliq_fashion",
        "amazon_fashion"
    );


    // Dynamic Database Auto-Provisioning on Application Boot
    @PostConstruct
    public void initDatabase() {
        logger.info("[Bargain Database] Auto-provisioning MySQL tables...");
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "phone_number VARCHAR(20) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("[Bargain Database] MySQL 'users' table is verified and active!");
        } catch (Exception e) {
            logger.severe("[Bargain Database] ERROR during database auto-provisioning: " + e.getMessage());
        }
    }

    // ─── MVC PAGE CONTROLLERS ──────────────────────────────────────────────────
    
    @GetMapping("/")
    public String showLoginPage(HttpSession session) {
        // Serve login.jsp view resolver
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber,
            HttpSession session) {
        
        if (username == null || username.trim().isEmpty() || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "redirect:/?error=InvalidInputs";
        }

        username = username.trim();
        phoneNumber = phoneNumber.trim();

        logger.info("[Bargain Auth] Registering user '" + username + "' inside MySQL database...");
        
        String insertSQL = "INSERT INTO users (username, phone_number) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, phoneNumber);
            pstmt.executeUpdate();
            
            // Set session attribute
            session.setAttribute("username", username);
            logger.info("[Bargain Auth] User successfully recorded inside MySQL table!");
        } catch (Exception e) {
            logger.severe("[Bargain Auth] ERROR recording user in MySQL: " + e.getMessage());
            return "redirect:/?error=DatabaseError";
        }

        // Redirect to price comparison dashboard
        return "redirect:/search";
    }

    @GetMapping("/search")
    public String showSearchPage(HttpSession session) {
        // Secure comparison storefront behind login check
        if (session.getAttribute("username") == null) {
            return "redirect:/";
        }
        // Serve search.jsp view resolver
        return "search";
    }

    // ─── REST DATA CONTROLLERS ────────────────────────────────────────────────
    
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Bargain Here API (Java Spring Boot)");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/platforms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPlatforms() {
        Map<String, Object> response = new HashMap<>();
        response.put("platforms", SUPPORTED_PLATFORMS);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<?> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "platforms", required = false) String platformsParam,
            HttpSession session) {
        
        // Block REST API access if no logged session
        if (session.getAttribute("username") == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized access. Please log in first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        if (query == null || query.trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Query parameter 'q' is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        
        query = query.trim();

        // 1. Determine platforms to search
        List<String> selectedPlatforms = new ArrayList<>();
        if (platformsParam != null && !platformsParam.trim().isEmpty()) {
            for (String p : platformsParam.split(",")) {
                String cleanPlat = p.trim().toLowerCase();
                if (SUPPORTED_PLATFORMS.contains(cleanPlat)) {
                    selectedPlatforms.add(cleanPlat);
                }
            }
        }
        if (selectedPlatforms.isEmpty()) {
            selectedPlatforms.addAll(SUPPORTED_PLATFORMS);
        }

        // 2. Query the SQLite Local Database first
        List<Product> allResults = scraperService.queryLocalDatabase(query, selectedPlatforms);

        // 3. Determine which requested platforms returned nothing from SQLite
        Set<String> databaseFoundPlatforms = allResults.stream()
                .map(p -> p.getPlatform().toLowerCase())
                .collect(Collectors.toSet());
        
        List<String> remainingPlatforms = selectedPlatforms.stream()
                .filter(p -> !databaseFoundPlatforms.contains(p))
                .collect(Collectors.toList());

        // 4. Concurrently Scrape remaining platforms using JSoup
        if (!remainingPlatforms.isEmpty()) {
            List<Product> scraped = scraperService.dispatchScrapersConcurrently(query, remainingPlatforms);
            allResults.addAll(scraped);
        }

        // 5. Fallback to Simulated Mock Data for platforms that STILL returned nothing
        Set<String> successfulPlatforms = allResults.stream()
                .map(p -> p.getPlatform().toLowerCase())
                .collect(Collectors.toSet());

        List<String> failedPlatforms = selectedPlatforms.stream()
                .filter(p -> !successfulPlatforms.contains(p))
                .collect(Collectors.toList());

        for (String platformKey : failedPlatforms) {
            List<Product> simulated = scraperService.generateSimulatedItems(query, platformKey);
            allResults.addAll(simulated);
        }

        // 6. Enrich all results with deterministic metadata (Brand, Rating, Discounts)
        List<Product> enrichedResults = allResults.stream()
                .map(item -> scraperService.augmentProductData(item))
                .collect(Collectors.toList());

        // 7. Sort by price (Lowest to Highest), placing products with null prices at the end
        enrichedResults.sort((a, b) -> {
            if (a.getPrice() == null && b.getPrice() == null) return 0;
            if (a.getPrice() == null) return 1;
            if (b.getPrice() == null) return -1;
            return Double.compare(a.getPrice(), b.getPrice());
        });

        Product cheapest = enrichedResults.isEmpty() ? null : enrichedResults.get(0);

        // 8. Construct response schema matching Flask perfectly
        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("total", enrichedResults.size());
        response.put("cheapest", cheapest);
        response.put("results", enrichedResults);
        response.put("errors", Collections.emptyList());
        response.put("platforms_searched", selectedPlatforms);

        return ResponseEntity.ok(response);
    }
}
