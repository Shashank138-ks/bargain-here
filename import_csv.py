import csv
import sqlite3
import os
import re

DB_NAME = "choukasi_products.db"
CSV_NAME = "products_fashion.csv"
FALLBACK_CSV = r"C:\Users\lenovo LOQ\.gemini\antigravity-ide\brain\19193878-b568-4f15-aaa0-4bf299de9918\scratch\latest_user_input.txt"

# Platform branding definitions matching backend logic
PLATFORMS_INFO = {
    "amazon": {
        "label": "Amazon",
        "color": "#FF9900",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg"
    },
    "flipkart": {
        "label": "Flipkart",
        "color": "#2874F0",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg"
    },
    "meesho": {
        "label": "Meesho",
        "color": "#F43397",
        "logo": "https://images.meesho.com/images/marketing/1655285313128.png"
    },
    "myntra": {
        "label": "Myntra",
        "color": "#FF3F6C",
        "logo": "https://constant.myntassets.com/web/assets/img/logo_myntra2x.png"
    },
    "ajio": {
        "label": "Ajio",
        "color": "#2C3E50",
        "logo": "https://www.ajio.com/favicon.ico"
    },
    "tata cliq": {
        "label": "Tata Cliq",
        "color": "#000000",
        "logo": "https://www.tatacliq.com/favicon.ico"
    },
    "snapdeal": {
        "label": "Snapdeal",
        "color": "#E40046",
        "logo": "https://www.snapdeal.com/favicon.ico"
    },
    "croma": {
        "label": "Croma",
        "color": "#00E9C0",
        "logo": "https://www.croma.com/favicon.ico"
    },
    "vijay sales": {
        "label": "Vijay Sales",
        "color": "#D12229",
        "logo": "https://www.vijaysales.com/favicon.ico"
    },
    "reliance digital": {
        "label": "Reliance Digital",
        "color": "#E42526",
        "logo": "https://www.reliancedigital.in/favicon.ico"
    },
    "paytm mall": {
        "label": "Paytm Mall",
        "color": "#00B9F1",
        "logo": "https://paytmmall.com/favicon.ico"
    },
    "shopclues": {
        "label": "ShopClues",
        "color": "#24A3B5",
        "logo": "https://www.shopclues.com/favicon.ico"
    },
    "indiamart": {
        "label": "IndiaMart",
        "color": "#005999",
        "logo": "https://www.indiamart.com/favicon.ico"
    },
    "bigbasket": {
        "label": "BigBasket",
        "color": "#84c225",
        "logo": "https://www.bigbasket.com/favicon.ico"
    },
    "nykaa": {
        "label": "Nykaa",
        "color": "#FC2779",
        "logo": "https://www.nykaa.com/favicon.ico"
    },
    "flipkart fashion": {
        "label": "Flipkart Fashion",
        "color": "#2874F0",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg"
    },
    "limeroad": {
        "label": "Limeroad",
        "color": "#99CC33",
        "logo": "https://www.limeroad.com/favicon.ico"
    },
    "craftsvilla": {
        "label": "Craftsvilla",
        "color": "#9C27B0",
        "logo": "https://www.craftsvilla.com/favicon.ico"
    },
    "nykaa fashion": {
        "label": "Nykaa Fashion",
        "color": "#FC2779",
        "logo": "https://www.nykaa.com/favicon.ico"
    },
    "tata cliq fashion": {
        "label": "Tata Cliq Fashion",
        "color": "#000000",
        "logo": "https://www.tatacliq.com/favicon.ico"
    },
    "amazon fashion": {
        "label": "Amazon Fashion",
        "color": "#FF9900",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg"
    }
}

# Normalize category names
CATEGORY_MAP = {
    "smartphones": "smartphones",
    "home appliances": "home_appliances",
    "laptops": "laptops",
    "smartwatches": "smartwatches",
    "cameras": "cameras",
    "tablets": "tablets",
    "headphones": "headphones",
    "televisions": "televisions",
    "kurtis & suits": "kurtis_suits",
    "kurtis and suits": "kurtis_suits",
    "kurtis_suits": "kurtis_suits",
    "dresses": "dresses",
    "jewellery": "jewellery",
    "accessories": "accessories",
    "footwear": "footwear",
    "sarees": "sarees",
    "watches": "watches",
    "bags & handbags": "bags_handbags",
    "bags and handbags": "bags_handbags",
    "bags_handbags": "bags_handbags"
}

def clean_csv_line(line):
    # Ignore tags like <USER_REQUEST>
    if "<USER_REQUEST>" in line or "</USER_REQUEST>" in line:
        return ""
    # Strip trailing truncation tags
    if "<truncated" in line:
        return ""
    return line

def main():
    csv_path = CSV_NAME
    is_fallback = False
    
    if not os.path.exists(csv_path):
        print(f"[Importer] '{CSV_NAME}' not found in workspace root.")
        if os.path.exists(FALLBACK_CSV):
            print(f"[Importer] Found recovered CSV file at: {FALLBACK_CSV}")
            csv_path = FALLBACK_CSV
            is_fallback = True
        else:
            print("[ERROR] No product CSV file found. Please create 'products.csv'.")
            return

    # Load and clean lines
    print(f"[Importer] Reading data from '{csv_path}'...")
    raw_lines = []
    with open(csv_path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            cleaned = clean_csv_line(line).strip()
            if cleaned:
                raw_lines.append(cleaned)

    # Parse CSV content
    reader = csv.DictReader(raw_lines)
    
    print("[Importer] Connecting to SQLite database...")
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    success_count = 0
    skipped_count = 0
    db_rows = []

    for idx, row in enumerate(reader):
        try:
            # Check fields
            title = row.get("product_name") or row.get("title")
            platform_name = row.get("platform")
            category_raw = row.get("category")
            price_val = row.get("price")
            
            if not title or not platform_name or not category_raw or not price_val:
                skipped_count += 1
                continue

            # Standardize platform key
            plat_key = platform_name.lower().strip()
            plat_info = PLATFORMS_INFO.get(plat_key, {
                "label": platform_name,
                "color": "#666666",
                "logo": ""
            })

            # Standardize category
            cat_key = category_raw.lower().replace("_", " ").strip()
            category = CATEGORY_MAP.get(cat_key, "generic")

            price = float(price_val)
            price_str = f"₹{int(price):,}"

            image = row.get("image_url") or row.get("image") or ""
            url = row.get("product_url") or row.get("url") or ""
            
            # Retrieve specifications
            specs = f"Rating: {row.get('rating', '4.0')}★, Reviews: {row.get('reviews', '100')}+"
            
            db_rows.append((
                plat_info["label"],
                title,
                category,
                price,
                price_str,
                image,
                url,
                plat_info["logo"],
                plat_info["color"],
                specs
            ))
            success_count += 1
        except Exception as e:
            print(f"[Warning] Skipped row {idx} due to error: {e}")
            skipped_count += 1

    if db_rows:
        print(f"[Importer] Writing {len(db_rows)} products to products table...")
        # We append to the existing table to expand it!
        cursor.executemany("""
            INSERT INTO products (platform, title, category, price, price_str, image, url, logo, color, specs)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, db_rows)
        conn.commit()
        print("[Importer] Database transactions committed successfully.")
    
    conn.close()
    
    print("=" * 60)
    print(" IMPORT COMPLETED ".center(60, "="))
    print(f" Successfully imported : {success_count} products")
    print(f" Rows skipped          : {skipped_count}")
    print(f" SQLite Database size  : {os.path.getsize(DB_NAME)} bytes")
    print("=" * 60)

if __name__ == "__main__":
    main()
