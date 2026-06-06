package com.bargain.model;

public class Product {
    private String platform;
    private String title;
    private Double price;
    private String price_str;
    private String image;
    private String url;
    private String logo;
    private String color;
    private String brand;
    private Double rating;
    private Integer discount_pct;
    private Double original_price;
    private String original_price_str;
    private String category;
    private String specs;

    public Product() {}

    // Getters and Setters
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getPrice_str() { return price_str; }
    public void setPrice_str(String price_str) { this.price_str = price_str; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getDiscount_pct() { return discount_pct; }
    public void setDiscount_pct(Integer discount_pct) { this.discount_pct = discount_pct; }

    public Double getOriginal_price() { return original_price; }
    public void setOriginal_price(Double original_price) { this.original_price = original_price; }

    public String getOriginal_price_str() { return original_price_str; }
    public void setOriginal_price_str(String original_price_str) { this.original_price_str = original_price_str; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }
}
