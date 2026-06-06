package com.bargain.service;

import com.bargain.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ScraperService {
    private static final Logger logger = Logger.getLogger(ScraperService.class.getName());
    
    private final ExecutorService executor = Executors.newFixedThreadPool(15);
    
    // User Agents for rotation
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    };

    private static final Map<String, String[]> UNSPLASH_CATEGORIES = new HashMap<>();
    private static final Map<String, PlatformDetail> PLATFORM_DETAILS = new HashMap<>();
    private static final Map<String, Double> PLATFORM_MULTIPLIERS = new HashMap<>();
    private static final Map<String, MockTemplate> MOCK_DATA_TEMPLATES = new HashMap<>();
    private static final Map<String, String> CATEGORY_IMAGE_MAP = new HashMap<>();
    private static final Map<String, String> KEYWORD_TO_CATEGORY = new HashMap<>();
    private static final List<Map.Entry<String, String>> KEYWORDS_SORTED = new ArrayList<>();

    static {
        // Platform Details Mapping
        PLATFORM_DETAILS.put("amazon", new PlatformDetail("Amazon", "#FF9900", "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg", "https://www.amazon.in"));
        PLATFORM_DETAILS.put("flipkart", new PlatformDetail("Flipkart", "#2874F0", "https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg", "https://www.flipkart.com"));
        PLATFORM_DETAILS.put("meesho", new PlatformDetail("Meesho", "#F43397", "https://images.meesho.com/images/marketing/1655285313128.png", "https://www.meesho.com"));
        PLATFORM_DETAILS.put("myntra", new PlatformDetail("Myntra", "#FF3F6C", "https://constant.myntassets.com/web/assets/img/logo_myntra2x.png", "https://www.myntra.com"));
        PLATFORM_DETAILS.put("jiomart", new PlatformDetail("JioMart", "#0060A9", "https://upload.wikimedia.org/wikipedia/commons/e/e5/JioMart_logo.svg", "https://www.jiomart.com"));
        PLATFORM_DETAILS.put("shopsy", new PlatformDetail("Shopsy", "#F7A400", "https://shopsy.in/favicon.ico", "https://shopsy.in"));
        PLATFORM_DETAILS.put("blinkit", new PlatformDetail("Blinkit", "#0C831F", "https://blinkit.com/favicon.png", "https://blinkit.com"));
        PLATFORM_DETAILS.put("instamart", new PlatformDetail("Instamart", "#FC8019", "https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto/portal/m/swiggy_logo.png", "https://www.swiggy.com/instamart"));
        PLATFORM_DETAILS.put("purplle", new PlatformDetail("Purplle", "#8B5CF6", "https://www.purplle.com/images/purplle-logo.png", "https://www.purplle.com"));
        PLATFORM_DETAILS.put("tira", new PlatformDetail("Tira", "#E91E8C", "https://www.tirabeauty.com/favicon.ico", "https://www.tirabeauty.com"));
        PLATFORM_DETAILS.put("newme", new PlatformDetail("NewMe", "#1a1a1a", "https://www.newme.asia/favicon.ico", "https://www.newme.asia"));
        PLATFORM_DETAILS.put("ajio", new PlatformDetail("Ajio", "#2C3E50", "https://www.ajio.com/favicon.ico", "https://www.ajio.com"));
        PLATFORM_DETAILS.put("nykaa", new PlatformDetail("Nykaa", "#FC2779", "https://www.nykaa.com/favicon.ico", "https://www.nykaa.com"));
        PLATFORM_DETAILS.put("tata_cliq", new PlatformDetail("Tata Cliq", "#000000", "https://www.tatacliq.com/favicon.ico", "https://www.tatacliq.com"));
        PLATFORM_DETAILS.put("snapdeal", new PlatformDetail("Snapdeal", "#E40046", "https://www.snapdeal.com/favicon.ico", "https://www.snapdeal.com"));
        PLATFORM_DETAILS.put("croma", new PlatformDetail("Croma", "#00E9C0", "https://www.croma.com/favicon.ico", "https://www.croma.com"));
        PLATFORM_DETAILS.put("vijay_sales", new PlatformDetail("Vijay Sales", "#D12229", "https://www.vijaysales.com/favicon.ico", "https://www.vijaysales.com"));
        PLATFORM_DETAILS.put("reliance_digital", new PlatformDetail("Reliance Digital", "#E42526", "https://www.reliancedigital.in/favicon.ico", "https://www.reliancedigital.in"));
        PLATFORM_DETAILS.put("paytm_mall", new PlatformDetail("Paytm Mall", "#00B9F1", "https://paytmmall.com/favicon.ico", "https://paytmmall.com"));
        PLATFORM_DETAILS.put("shopclues", new PlatformDetail("ShopClues", "#24A3B5", "https://www.shopclues.com/favicon.ico", "https://www.shopclues.com"));
        PLATFORM_DETAILS.put("indiamart", new PlatformDetail("IndiaMart", "#005999", "https://www.indiamart.com/favicon.ico", "https://www.indiamart.com"));
        PLATFORM_DETAILS.put("bigbasket", new PlatformDetail("BigBasket", "#84c225", "https://www.bigbasket.com/favicon.ico", "https://www.bigbasket.com"));
        PLATFORM_DETAILS.put("flipkart_fashion", new PlatformDetail("Flipkart Fashion", "#2874F0", "https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg", "https://www.flipkart.com"));
        PLATFORM_DETAILS.put("limeroad", new PlatformDetail("Limeroad", "#99CC33", "https://www.limeroad.com/favicon.ico", "https://www.limeroad.com"));
        PLATFORM_DETAILS.put("craftsvilla", new PlatformDetail("Craftsvilla", "#9C27B0", "https://www.craftsvilla.com/favicon.ico", "https://www.craftsvilla.com"));
        PLATFORM_DETAILS.put("nykaa_fashion", new PlatformDetail("Nykaa Fashion", "#FC2779", "https://www.nykaa.com/favicon.ico", "https://www.nykaa.com"));
        PLATFORM_DETAILS.put("tata_cliq_fashion", new PlatformDetail("Tata Cliq Fashion", "#000000", "https://www.tatacliq.com/favicon.ico", "https://www.tatacliq.com"));
        PLATFORM_DETAILS.put("amazon_fashion", new PlatformDetail("Amazon Fashion", "#FF9900", "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg", "https://www.amazon.in"));


        // Platform Price Multipliers
        PLATFORM_MULTIPLIERS.put("amazon", 1.00);
        PLATFORM_MULTIPLIERS.put("flipkart", 0.98);
        PLATFORM_MULTIPLIERS.put("meesho", 0.88);
        PLATFORM_MULTIPLIERS.put("myntra", 1.02);
        PLATFORM_MULTIPLIERS.put("jiomart", 0.94);
        PLATFORM_MULTIPLIERS.put("shopsy", 0.86);
        PLATFORM_MULTIPLIERS.put("blinkit", 1.05);
        PLATFORM_MULTIPLIERS.put("instamart", 1.04);
        PLATFORM_MULTIPLIERS.put("purplle", 0.96);
        PLATFORM_MULTIPLIERS.put("tira", 1.01);
        PLATFORM_MULTIPLIERS.put("newme", 0.90);
        PLATFORM_MULTIPLIERS.put("ajio", 0.93);
        PLATFORM_MULTIPLIERS.put("nykaa", 0.97);
        PLATFORM_MULTIPLIERS.put("tata_cliq", 0.97);
        PLATFORM_MULTIPLIERS.put("snapdeal", 0.90);
        PLATFORM_MULTIPLIERS.put("croma", 1.01);
        PLATFORM_MULTIPLIERS.put("vijay_sales", 0.99);
        PLATFORM_MULTIPLIERS.put("reliance_digital", 1.00);
        PLATFORM_MULTIPLIERS.put("paytm_mall", 0.95);
        PLATFORM_MULTIPLIERS.put("shopclues", 0.90);
        PLATFORM_MULTIPLIERS.put("indiamart", 0.92);
        PLATFORM_MULTIPLIERS.put("bigbasket", 0.98);
        PLATFORM_MULTIPLIERS.put("flipkart_fashion", 0.95);
        PLATFORM_MULTIPLIERS.put("limeroad", 0.89);
        PLATFORM_MULTIPLIERS.put("craftsvilla", 0.91);
        PLATFORM_MULTIPLIERS.put("nykaa_fashion", 1.03);
        PLATFORM_MULTIPLIERS.put("tata_cliq_fashion", 1.00);
        PLATFORM_MULTIPLIERS.put("amazon_fashion", 0.97);


        // Unsplash Categories (keep electronics / phone / generic as fallbacks if needed)
        UNSPLASH_CATEGORIES.put("electronics", new String[]{
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&auto=format&fit=crop&q=80"
        });
        UNSPLASH_CATEGORIES.put("generic", new String[]{
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&auto=format&fit=crop&q=80"
        });

        // Mock Templates
        MOCK_DATA_TEMPLATES.put("tablet", new MockTemplate("Apple iPad Pro ({specs})", 69900.0, 
            "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&auto=format&fit=crop&q=80", 
            new String[]{"11-inch Display, 128GB", "12.9-inch Display, 256GB"}));
        
        MOCK_DATA_TEMPLATES.put("laptop", new MockTemplate("Lenovo ThinkPad Laptop ({specs})", 54999.0, 
            "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&auto=format&fit=crop&q=80", 
            new String[]{"Intel i5, 8GB RAM, 512GB SSD", "Intel i7, 16GB RAM, 1TB SSD"}));
        
        MOCK_DATA_TEMPLATES.put("camera", new MockTemplate("Sony Alpha Mirrorless Camera ({specs})", 45000.0, 
            "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400&auto=format&fit=crop&q=80", 
            new String[]{"24.2MP, 16-50mm Lens", "45.7MP, Body Only"}));

        MOCK_DATA_TEMPLATES.put("appliance", new MockTemplate("Samsung Smart Refrigerator ({specs})", 24990.0, 
            "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=400&auto=format&fit=crop&q=80", 
            new String[]{"3-Star Energy Rating, 253L", "5-Star Energy Rating, 324L"}));

        MOCK_DATA_TEMPLATES.put("tv", new MockTemplate("LG Ultra HD Smart TV ({specs})", 32999.0, 
            "https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=400&auto=format&fit=crop&q=80", 
            new String[]{"43-inch 4K Smart", "55-inch 4K OLED"}));

        MOCK_DATA_TEMPLATES.put("headphones", new MockTemplate("Sony WH-1000XM5 ANC Headphones ({specs})", 29990.0, 
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&auto=format&fit=crop&q=80", 
            new String[]{"Black Color", "Silver Color"}));

        MOCK_DATA_TEMPLATES.put("phone", new MockTemplate("OnePlus Nord 5G Smartphone ({specs})", 19999.0, 
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&auto=format&fit=crop&q=80", 
            new String[]{"8GB RAM, 128GB Storage", "12GB RAM, 256GB Storage"}));

        MOCK_DATA_TEMPLATES.put("watch", new MockTemplate("Noise Fit ColorFit Smartwatch ({specs})", 2499.0, 
            "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&auto=format&fit=crop&q=80", 
            new String[]{"1.8\" AMOLED screen", "1.69\" display"}));

        // Category Image Map
        CATEGORY_IMAGE_MAP.put("tablets", "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("laptops", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("cameras", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("home_appliances", "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("televisions", "https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("headphones", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("smartphones", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("smartwatches", "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("kurtis_suits", "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("dresses", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("jewellery", "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("accessories", "https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("footwear", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("sarees", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("watches", "https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?w=400&auto=format&fit=crop&q=80");
        CATEGORY_IMAGE_MAP.put("bags_handbags", "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=400&auto=format&fit=crop&q=80");

        // Keywords Mapping
        KEYWORD_TO_CATEGORY.put("tablet", "tablets");
        KEYWORD_TO_CATEGORY.put("ipad", "tablets");
        KEYWORD_TO_CATEGORY.put("pad", "tablets");
        KEYWORD_TO_CATEGORY.put("laptop", "laptops");
        KEYWORD_TO_CATEGORY.put("notebook", "laptops");
        KEYWORD_TO_CATEGORY.put("chromebook", "laptops");
        KEYWORD_TO_CATEGORY.put("macbook", "laptops");
        KEYWORD_TO_CATEGORY.put("camera", "cameras");
        KEYWORD_TO_CATEGORY.put("dslr", "cameras");
        KEYWORD_TO_CATEGORY.put("gopro", "cameras");
        KEYWORD_TO_CATEGORY.put("cam", "cameras");
        KEYWORD_TO_CATEGORY.put("fridge", "home_appliances");
        KEYWORD_TO_CATEGORY.put("refrigerator", "home_appliances");
        KEYWORD_TO_CATEGORY.put("washing machine", "home_appliances");
        KEYWORD_TO_CATEGORY.put("dryer", "home_appliances");
        KEYWORD_TO_CATEGORY.put("washer", "home_appliances");
        KEYWORD_TO_CATEGORY.put("microwave", "home_appliances");
        KEYWORD_TO_CATEGORY.put("oven", "home_appliances");
        KEYWORD_TO_CATEGORY.put("appliance", "home_appliances");
        KEYWORD_TO_CATEGORY.put("ac", "home_appliances");
        KEYWORD_TO_CATEGORY.put("vacuum", "home_appliances");
        KEYWORD_TO_CATEGORY.put("tv", "televisions");
        KEYWORD_TO_CATEGORY.put("television", "televisions");
        KEYWORD_TO_CATEGORY.put("led", "televisions");
        KEYWORD_TO_CATEGORY.put("oled", "televisions");
        KEYWORD_TO_CATEGORY.put("headphones", "headphones");
        KEYWORD_TO_CATEGORY.put("earphones", "headphones");
        KEYWORD_TO_CATEGORY.put("earbuds", "headphones");
        KEYWORD_TO_CATEGORY.put("pods", "headphones");
        KEYWORD_TO_CATEGORY.put("airpods", "headphones");
        KEYWORD_TO_CATEGORY.put("headset", "headphones");
        KEYWORD_TO_CATEGORY.put("phone", "smartphones");
        KEYWORD_TO_CATEGORY.put("smartphone", "smartphones");
        KEYWORD_TO_CATEGORY.put("mobile", "smartphones");
        KEYWORD_TO_CATEGORY.put("smartwatch", "smartwatches");
        KEYWORD_TO_CATEGORY.put("watch", "watches");
        KEYWORD_TO_CATEGORY.put("band", "smartwatches");
        KEYWORD_TO_CATEGORY.put("kurti", "kurtis_suits");
        KEYWORD_TO_CATEGORY.put("suit", "kurtis_suits");
        KEYWORD_TO_CATEGORY.put("patiala", "kurtis_suits");
        KEYWORD_TO_CATEGORY.put("dress", "dresses");
        KEYWORD_TO_CATEGORY.put("frock", "dresses");
        KEYWORD_TO_CATEGORY.put("gown", "dresses");
        KEYWORD_TO_CATEGORY.put("midi", "dresses");
        KEYWORD_TO_CATEGORY.put("bodycon", "dresses");
        KEYWORD_TO_CATEGORY.put("jewel", "jewellery");
        KEYWORD_TO_CATEGORY.put("necklace", "jewellery");
        KEYWORD_TO_CATEGORY.put("ring", "jewellery");
        KEYWORD_TO_CATEGORY.put("anklet", "jewellery");
        KEYWORD_TO_CATEGORY.put("bangle", "jewellery");
        KEYWORD_TO_CATEGORY.put("earring", "jewellery");
        KEYWORD_TO_CATEGORY.put("choker", "jewellery");
        KEYWORD_TO_CATEGORY.put("stud", "jewellery");
        KEYWORD_TO_CATEGORY.put("accessories", "accessories");
        KEYWORD_TO_CATEGORY.put("scarf", "accessories");
        KEYWORD_TO_CATEGORY.put("belt", "accessories");
        KEYWORD_TO_CATEGORY.put("brooch", "accessories");
        KEYWORD_TO_CATEGORY.put("clip", "accessories");
        KEYWORD_TO_CATEGORY.put("hairband", "accessories");
        KEYWORD_TO_CATEGORY.put("sunglasses", "accessories");
        KEYWORD_TO_CATEGORY.put("footwear", "footwear");
        KEYWORD_TO_CATEGORY.put("shoes", "footwear");
        KEYWORD_TO_CATEGORY.put("sandal", "footwear");
        KEYWORD_TO_CATEGORY.put("heel", "footwear");
        KEYWORD_TO_CATEGORY.put("slipper", "footwear");
        KEYWORD_TO_CATEGORY.put("loafer", "footwear");
        KEYWORD_TO_CATEGORY.put("espadrille", "footwear");
        KEYWORD_TO_CATEGORY.put("flat", "footwear");
        KEYWORD_TO_CATEGORY.put("stiletto", "footwear");
        KEYWORD_TO_CATEGORY.put("sneaker", "footwear");
        KEYWORD_TO_CATEGORY.put("mule", "footwear");
        KEYWORD_TO_CATEGORY.put("saree", "sarees");
        KEYWORD_TO_CATEGORY.put("sari", "sarees");
        KEYWORD_TO_CATEGORY.put("bag", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("handbag", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("satchel", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("purse", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("tote", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("hobo", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("sling", "bags_handbags");
        KEYWORD_TO_CATEGORY.put("backpack", "bags_handbags");


        KEYWORDS_SORTED.addAll(KEYWORD_TO_CATEGORY.entrySet());
        KEYWORDS_SORTED.sort((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()));
    }

    public static class PlatformDetail {
        public final String label;
        public final String color;
        public final String logo;
        public final String url;

        public PlatformDetail(String label, String color, String logo, String url) {
            this.label = label;
            this.color = color;
            this.logo = logo;
            this.url = url;
        }
    }

    public static class MockTemplate {
        public final String titleTemplate;
        public final Double basePrice;
        public final String image;
        public final String[] specs;

        public MockTemplate(String titleTemplate, Double basePrice, String image, String[] specs) {
            this.titleTemplate = titleTemplate;
            this.basePrice = basePrice;
            this.image = image;
            this.specs = specs;
        }
    }

    private String getHeadersUserAgent() {
        return USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
    }

    // ─── UTILS ──────────────────────────────────────────────────────────────────
    private Double cleanPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return null;
        String cleaned = priceStr.replace(",", "").replaceAll("[^\\d.]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCategory(String q) {
        if (q == null) return "generic";
        String qLow = q.toLowerCase().trim();
        for (Map.Entry<String, String> entry : KEYWORDS_SORTED) {
            if (qLow.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "generic";
    }

    private boolean matchesAny(String source, String... keys) {
        for (String k : keys) {
            if (source.contains(k)) return true;
        }
        return false;
    }

    private int getConsistentHash(String q) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(q.getBytes("UTF-8"));
            BigInteger no = new BigInteger(1, digest);
            return no.intValue() & 0xfffffff; // Ensure positive integer bounds
        } catch (Exception e) {
            return Math.abs(q.hashCode());
        }
    }

    // ─── DATA ENRICHMENT (MD5 DETERMINISTIC SEEDING) ──────────────────────────────────
    public Product augmentProductData(Product item) {
        String title = item.getTitle() != null ? item.getTitle() : "";
        Double price = item.getPrice() != null ? item.getPrice() : 0.0;
        String platform = item.getPlatform() != null ? item.getPlatform() : "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // 1. Brand Seeding
            String[] knownBrands = {
                "Nike", "Adidas", "Apple", "Sony", "Samsung", "OnePlus", "Tata", "Nescafe", "Fortune",
                "Daawat", "Levi's", "L'Oreal", "Clinique", "Maybelline", "Micromax", "Bosch", "Fastrack",
                "Huawei", "Canon", "Microsoft", "Bose", "Haier", "Intex", "Fujifilm", "Philips", "boAt",
                "Videocon", "Hisense", "Fitbit", "Nikon", "Godrej", "Daikin", "Noise", "Panasonic",
                "Google", "TCL", "Whirlpool", "Hitachi", "JBL", "Lava", "Voltas", "Titan", "Garmin", "Sennheiser",
                "Realme", "Vivo", "Oppo", "Xiaomi", "Motorola", "Wishful", "Anouk", "Aurelia", "Joyalukkas",
                "Zara", "H&M", "Biba", "W", "Giva", "Tanishq", "Bata", "Metro", "Caprese", "Lavie", "Baggit",
                "Crunchy Fashion", "AND", "ONLY", "Hidesign", "Fabindia", "Puma", "Sukkhi", "YouBella", "Mimosa",
                "Malabar Gold", "Voylla", "Lenskart", "Nalli", "Sabyasachi", "Sonata", "Fossil", "Mochi",
                "Zaveri Pearls", "Vero Moda", "Casio", "Lino Perros", "Vardhaman", "Forever 21", "Crocs", "Daniel Wellington"
            };

            String brand = "Generic";
            for (String b : knownBrands) {
                if (title.toLowerCase().contains(b.toLowerCase())) {
                    brand = b;
                    break;
                }
            }
            if ("Generic".equals(brand)) {
                byte[] dBrand = md.digest(title.getBytes("UTF-8"));
                int hB = new BigInteger(1, dBrand).intValue() & 0xfffffff;
                String[] fallbackBrands = {"EcoBrand", "FlexStyle", "AuraBeauty", "NovaTech", "PureChoice"};
                brand = fallbackBrands[hB % fallbackBrands.length];
            }
            item.setBrand(brand);

            // 2. Rating Seeding (3.5 - 4.9 stars)
            byte[] dRating = md.digest((title + platform).getBytes("UTF-8"));
            int hR = new BigInteger(1, dRating).intValue() & 0xfffffff;
            double rating = 3.5 + (hR % 15) / 10.0;
            item.setRating(Math.round(rating * 10.0) / 10.0);

            // 3. Discount Seeding (10% - 55%)
            byte[] dDiscount = md.digest((title + platform + "discount").getBytes("UTF-8"));
            int hD = new BigInteger(1, dDiscount).intValue() & 0xfffffff;
            int discountPct = 10 + (hD % 46);
            item.setDiscount_pct(discountPct);

            // 4. Original Price Seeding
            if (price > 0) {
                double originalPrice = Math.round((price * 100.0) / (100 - discountPct));
                item.setOriginal_price(originalPrice);
                item.setOriginal_price_str("₹" + String.format("%,d", (int) originalPrice));
            } else {
                item.setOriginal_price(0.0);
                item.setOriginal_price_str("");
            }
            
            // Ensure category
            if (item.getCategory() == null || item.getCategory().isEmpty()) {
                item.setCategory(getCategory(title));
            }

        } catch (Exception e) {
            logger.warning("Error augmenting product data: " + e.getMessage());
        }

        return item;
    }

    private String randDigits(Random rand, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

    private String randAlnum(Random rand, int n) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String makeDirectUrl(String platform, String title, int rowId) {
        String slug = title.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "product-" + rowId;
        }
        String plat = platform.toLowerCase().trim();
        Random rand = new Random(rowId);

        if (plat.contains("amazon")) {
            String asin = "B0" + randAlnum(rand, 8).toUpperCase();
            return "https://www.amazon.in/" + slug + "/dp/" + asin;
        } else if (plat.contains("flipkart")) {
            String fsid = randAlnum(rand, 16).toLowerCase();
            return "https://www.flipkart.com/" + slug + "/p/" + fsid;
        } else if (plat.contains("meesho")) {
            return "https://www.meesho.com/" + slug + "/p/" + randDigits(rand, 9);
        } else if (plat.contains("myntra")) {
            return "https://www.myntra.com/" + randDigits(rand, 7);
        } else if (plat.contains("ajio")) {
            return "https://www.ajio.com/" + slug + "/p/" + randDigits(rand, 12);
        } else if (plat.contains("tata cliq") || plat.contains("tatacliq")) {
            return "https://www.tatacliq.com/" + slug + "/p-mp" + randDigits(rand, 12);
        } else if (plat.contains("snapdeal")) {
            return "https://www.snapdeal.com/product/" + slug + "/" + randDigits(rand, 12);
        } else if (plat.contains("croma")) {
            return "https://www.croma.com/" + slug + "/p/" + randDigits(rand, 10);
        } else if (plat.contains("vijay sales") || plat.contains("vijaysales")) {
            return "https://www.vijaysales.com/" + slug + "/p/" + randDigits(rand, 8);
        } else if (plat.contains("reliance digital") || plat.contains("reliancedigital")) {
            return "https://www.reliancedigital.in/p/" + randDigits(rand, 12);
        } else if (plat.contains("paytm")) {
            return "https://paytmmall.com/" + slug + "/p/" + randDigits(rand, 10);
        } else if (plat.contains("shopclues")) {
            return "https://www.shopclues.com/" + slug + "/p/" + randDigits(rand, 9);
        } else if (plat.contains("indiamart")) {
            return "https://www.indiamart.com/proddetail/" + slug + "-" + randDigits(rand, 10) + ".html";
        } else if (plat.contains("bigbasket")) {
            return "https://www.bigbasket.com/pd/" + randDigits(rand, 8) + "/" + slug;
        } else if (plat.contains("nykaa")) {
            return "https://www.nykaa.com/" + slug + "/p/" + randDigits(rand, 7);
        } else if (plat.contains("limeroad")) {
            return "https://www.limeroad.com/" + slug + "/p/" + randDigits(rand, 8);
        } else if (plat.contains("craftsvilla")) {
            return "https://www.craftsvilla.com/" + slug + "/p/" + randDigits(rand, 8);
        } else if (plat.contains("blinkit")) {
            return "https://blinkit.com/prn/" + slug + "/prid/" + randDigits(rand, 6);
        } else if (plat.contains("instamart")) {
            return "https://www.swiggy.com/instamart/item/" + randDigits(rand, 8);
        } else if (plat.contains("purplle")) {
            return "https://www.purplle.com/r/p/" + randDigits(rand, 6);
        } else if (plat.contains("tira")) {
            return "https://www.tirabeauty.com/p/" + randDigits(rand, 7);
        } else if (plat.contains("newme")) {
            return "https://www.newme.asia/products/" + slug + "-" + randAlnum(rand, 10);
        } else {
            return "https://www." + plat.replace(" ", "") + ".com/product/" + slug;
        }
    }

    // ─── SIMULATION DATA ENGINE ──────────────────────────────────────────────────────
    public List<Product> generateSimulatedItems(String query, String platformKey) {
        List<Product> results = new ArrayList<>();
        String qLow = query.toLowerCase().trim();
        int h = getConsistentHash(qLow);

        String templateKey = null;
        for (String k : MOCK_DATA_TEMPLATES.keySet()) {
            if (qLow.contains(k)) {
                templateKey = k;
                break;
            }
        }

        String title;
        double basePrice;
        String image;

        if (templateKey != null) {
            MockTemplate tpl = MOCK_DATA_TEMPLATES.get(templateKey);
            basePrice = tpl.basePrice;
            image = tpl.image;
            if (tpl.specs != null && tpl.specs.length > 0) {
                String spec = tpl.specs[h % tpl.specs.length];
                title = tpl.titleTemplate.replace("{specs}", spec);
            } else {
                title = tpl.titleTemplate;
            }
        } else {
            String category = getCategory(qLow);
            image = CATEGORY_IMAGE_MAP.getOrDefault(category, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&auto=format&fit=crop&q=80");

            basePrice = 20.0 + (h % 7980); // Price baseline between 20 and 7999
            String[] prefixes = {"Premium", "Original", "Luxury", "Smart", "Eco-friendly", "Classic", "Deluxe"};
            String[] suffixes = {"Pro", "Max", "Combo Pack", "Special Edition", "Premium Series", "Essentials"};

            String prefix = prefixes[h % prefixes.length];
            String suffix = suffixes[(h / 2) % suffixes.length];
            
            // Format Query Title Case
            String formattedQuery = Arrays.stream(query.split("\\s+"))
                    .map(word -> word.isEmpty() ? "" : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            
            title = prefix + " " + formattedQuery + " " + suffix;
        }

        // Apply Platform Multipliers and Noise
        double mult = PLATFORM_MULTIPLIERS.getOrDefault(platformKey, 1.0);
        int noiseSeed = getConsistentHash(qLow + platformKey);
        double noise = 0.98 + ((noiseSeed % 40) / 1000.0); // 0.98 to 1.02

        double price = Math.round(basePrice * mult * noise);
        if (price > 20) {
            price = (price % 10 < 5) ? ((price / 10) * 10 - 1) : ((price / 10) * 10 + 9);
        }
        if (price < 5) {
            price = 5.0;
        }

        PlatformDetail details = PLATFORM_DETAILS.getOrDefault(platformKey, new PlatformDetail(platformKey, "#666", "", "https://www.google.com"));

        Product p = new Product();
        p.setPlatform(details.label);
        p.setTitle(title);
        p.setPrice(price);
        p.setPrice_str("₹" + String.format("%,d", (int) price));
        p.setImage(image);
        p.setUrl(makeDirectUrl(platformKey, title, noiseSeed));
        p.setLogo(details.logo);
        p.setColor(details.color);
        p.setCategory(getCategory(title));

        results.add(p);
        return results;
    }

    // ─── SQLITE DATA LAYER ──────────────────────────────────────────────────────────
    public List<Product> queryLocalDatabase(String query, List<String> platformKeys) {
        List<Product> results = new ArrayList<>();
        
        // Dynamic path checking
        String dbPath = "choukasi_products.db";
        File localFile = new File(dbPath);
        if (!localFile.exists()) {
            File parentFile = new File("../choukasi_products.db");
            if (parentFile.exists()) {
                dbPath = "../choukasi_products.db";
            }
        }
        
        String url = "jdbc:sqlite:" + dbPath;
        if (query == null || query.trim().isEmpty()) return results;

        String category = getCategory(query);
        boolean isCategorySearch = category != null && !category.equals("generic");

        try (Connection conn = DriverManager.getConnection(url)) {
            List<Product> tempResults = new ArrayList<>();

            // Step 1: Exact category match
            if (isCategorySearch) {
                String sql = "SELECT * FROM products WHERE category = ? ORDER BY price ASC";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, category);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String plat = rs.getString("platform");
                            if (platformKeys.contains(plat.toLowerCase())) {
                                Product p = new Product();
                                p.setPlatform(plat);
                                p.setTitle(rs.getString("title"));
                                p.setPrice(rs.getDouble("price"));
                                p.setPrice_str(rs.getString("price_str"));
                                p.setImage(rs.getString("image"));
                                p.setUrl(rs.getString("url"));
                                p.setLogo(rs.getString("logo"));
                                p.setColor(rs.getString("color"));
                                p.setSpecs(rs.getString("specs"));
                                p.setCategory(rs.getString("category"));
                                tempResults.add(p);
                            }
                        }
                    }
                }
            }

            // Step 2: Fallback to title keyword match if category query returned nothing
            if (tempResults.isEmpty()) {
                String sql = "SELECT * FROM products WHERE LOWER(title) LIKE ? ORDER BY price ASC";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, "%" + query.toLowerCase() + "%");
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String plat = rs.getString("platform");
                            if (platformKeys.contains(plat.toLowerCase())) {
                                Product p = new Product();
                                p.setPlatform(plat);
                                p.setTitle(rs.getString("title"));
                                p.setPrice(rs.getDouble("price"));
                                p.setPrice_str(rs.getString("price_str"));
                                p.setImage(rs.getString("image"));
                                p.setUrl(rs.getString("url"));
                                p.setLogo(rs.getString("logo"));
                                p.setColor(rs.getString("color"));
                                p.setSpecs(rs.getString("specs"));
                                p.setCategory(rs.getString("category"));
                                tempResults.add(p);
                            }
                        }
                    }
                }
            }

            results.addAll(tempResults);
        } catch (Exception e) {
            logger.warning("Local SQLite database query failed: " + e.getMessage());
        }
        return results;
    }

    // ─── SCRAPER CALLS (HTML JSoup Parsers) ─────────────────────────────────────────
    private List<Product> scrapeAmazon(String query) {
        List<Product> results = new ArrayList<>();
        try {
            String url = "https://www.amazon.in/s?k=" + URLEncoder.encode(query, "UTF-8");
            Document doc = Jsoup.connect(url)
                    .userAgent(getHeadersUserAgent())
                    .timeout(2000)
                    .get();
            
            Elements items = doc.select("div[data-component-type='s-search-result']");
            int count = 0;
            for (Element item : items) {
                if (count >= 5) break;
                Element titleEl = item.selectFirst("h2 a span");
                Element priceEl = item.selectFirst("span.a-price-whole");
                Element imgEl = item.selectFirst("img.s-image");
                Element linkEl = item.selectFirst("h2 a");

                if (titleEl != null && priceEl != null) {
                    Double price = cleanPrice(priceEl.text());
                    if (price != null) {
                        Product p = new Product();
                        p.setPlatform("Amazon");
                        p.setTitle(titleEl.text());
                        p.setPrice(price);
                        p.setPrice_str("₹" + String.format("%,d", price.intValue()));
                        p.setImage(imgEl != null ? imgEl.attr("src") : "");
                        p.setUrl(linkEl != null ? "https://www.amazon.in" + linkEl.attr("href") : url);
                        p.setLogo("https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg");
                        p.setColor("#FF9900");
                        results.add(p);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Amazon JSoup parsing error: " + e.getMessage());
        }
        return results;
    }

    private List<Product> scrapeFlipkart(String query) {
        List<Product> results = new ArrayList<>();
        try {
            String url = "https://www.flipkart.com/search?q=" + URLEncoder.encode(query, "UTF-8");
            Document doc = Jsoup.connect(url)
                    .userAgent(getHeadersUserAgent())
                    .timeout(2000)
                    .get();
            
            Elements items = doc.select("div._1AtVbE, div._2kHMtA, div.cPHDOP");
            int count = 0;
            for (Element item : items) {
                if (count >= 5) break;
                Element titleEl = item.selectFirst("div._4rR01T, a.s1Q9rs, div.KzDlHZ");
                Element priceEl = item.selectFirst("div._30jeq3, div.Nx9bqj");
                Element imgEl = item.selectFirst("img._396cs4, img.DByuf4");
                Element linkEl = item.selectFirst("a._1fQZEK, a.s1Q9rs, a.CGtC98");

                if (titleEl != null && priceEl != null) {
                    Double price = cleanPrice(priceEl.text());
                    if (price != null) {
                        Product p = new Product();
                        p.setPlatform("Flipkart");
                        p.setTitle(titleEl.text());
                        p.setPrice(price);
                        p.setPrice_str("₹" + String.format("%,d", price.intValue()));
                        p.setImage(imgEl != null ? imgEl.attr("src") : "");
                        
                        String relativeUrl = linkEl != null ? linkEl.attr("href") : "";
                        p.setUrl(relativeUrl.startsWith("/") ? "https://www.flipkart.com" + relativeUrl : url);
                        p.setLogo("https://static-assets-web.flixcart.com/batman-returns/batman-returns/p/images/fkheaderlogo_exploremore-74b5f4.svg");
                        p.setColor("#2874F0");
                        results.add(p);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Flipkart JSoup parsing error: " + e.getMessage());
        }
        return results;
    }

    // Concurrent Dispatcher and Scraper Registry
    public List<Product> dispatchScrapersConcurrently(String query, List<String> platformKeys) {
        List<CompletableFuture<List<Product>>> futures = new ArrayList<>();

        for (String platform : platformKeys) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                if ("amazon".equals(platform)) {
                    return scrapeAmazon(query);
                } else if ("flipkart".equals(platform)) {
                    return scrapeFlipkart(query);
                }
                return new ArrayList<>(); // Scrapers for Ajio/Meesho/Nykaa default to simulated models under blocks
            }, executor));
        }

        List<Product> scrapedResults = new ArrayList<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<List<Product>> f : futures) {
            try {
                scrapedResults.addAll(f.get());
            } catch (Exception e) {
                logger.warning("Error harvesting scraped future: " + e.getMessage());
            }
        }
        return scrapedResults;
    }
}
