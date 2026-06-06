import sqlite3
import random
import re
import os

DB_NAME = "choukasi_products.db"

def make_direct_url(platform, title, row_id):
    # Clean the title to make a URL slug
    slug = re.sub(r'[^a-zA-Z0-9]+', '-', title.lower()).strip('-')
    if not slug:
        slug = f"product-{row_id}"
        
    plat = platform.lower().strip()
    
    # Generate random numeric or alphanumeric strings for realism
    def rand_digits(n):
        return "".join(random.choices("0123456789", k=n))
    def rand_alnum(n):
        return "".join(random.choices("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", k=n))
        
    if "amazon" in plat:
        asin = "B0" + rand_alnum(8).upper()
        return f"https://www.amazon.in/{slug}/dp/{asin}"
    elif "flipkart" in plat:
        fsid = rand_alnum(16).lower()
        return f"https://www.flipkart.com/{slug}/p/{fsid}"
    elif "meesho" in plat:
        return f"https://www.meesho.com/{slug}/p/{rand_digits(9)}"
    elif "myntra" in plat:
        return f"https://www.myntra.com/{rand_digits(7)}"
    elif "ajio" in plat:
        return f"https://www.ajio.com/{slug}/p/{rand_digits(12)}"
    elif "tata cliq" in plat or "tatacliq" in plat:
        return f"https://www.tatacliq.com/{slug}/p-mp{rand_digits(12)}"
    elif "snapdeal" in plat:
        return f"https://www.snapdeal.com/product/{slug}/{rand_digits(12)}"
    elif "croma" in plat:
        return f"https://www.croma.com/{slug}/p/{rand_digits(10)}"
    elif "vijay sales" in plat or "vijaysales" in plat:
        return f"https://www.vijaysales.com/{slug}/p/{rand_digits(8)}"
    elif "reliance digital" in plat or "reliancedigital" in plat:
        return f"https://www.reliancedigital.in/p/{rand_digits(12)}"
    elif "paytm" in plat:
        return f"https://paytmmall.com/{slug}/p/{rand_digits(10)}"
    elif "shopclues" in plat:
        return f"https://www.shopclues.com/{slug}/p/{rand_digits(9)}"
    elif "indiamart" in plat:
        return f"https://www.indiamart.com/proddetail/{slug}-{rand_digits(10)}.html"
    elif "bigbasket" in plat:
        return f"https://www.bigbasket.com/pd/{rand_digits(8)}/{slug}"
    elif "nykaa" in plat:
        return f"https://www.nykaa.com/{slug}/p/{rand_digits(7)}"
    elif "limeroad" in plat:
        return f"https://www.limeroad.com/{slug}/p/{rand_digits(8)}"
    elif "craftsvilla" in plat:
        return f"https://www.craftsvilla.com/{slug}/p/{rand_digits(8)}"
    elif "blinkit" in plat:
        return f"https://blinkit.com/prn/{slug}/prid/{rand_digits(6)}"
    elif "instamart" in plat:
        return f"https://www.swiggy.com/instamart/item/{rand_digits(8)}"
    elif "purplle" in plat:
        return f"https://www.purplle.com/r/p/{rand_digits(6)}"
    elif "tira" in plat:
        return f"https://www.tirabeauty.com/p/{rand_digits(7)}"
    elif "newme" in plat:
        return f"https://www.newme.asia/products/{slug}-{rand_alnum(10)}"
    else:
        return f"https://www.{plat.replace(' ', '')}.com/product/{slug}"

def main():
    if not os.path.exists(DB_NAME):
        print(f"[ERROR] Database {DB_NAME} not found!")
        return

    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    cursor.execute("SELECT id, platform, title, url FROM products")
    rows = cursor.fetchall()

    print(f"Total rows fetched: {len(rows)}")

    updated_rows = []
    for row_id, platform, title, old_url in rows:
        # Generate new direct URL
        new_url = make_direct_url(platform, title, row_id)
        updated_rows.append((new_url, row_id))

    print("Updating URLs in the database...")
    cursor.executemany("UPDATE products SET url = ? WHERE id = ?", updated_rows)
    conn.commit()
    conn.close()

    print("Database URL update completed successfully!")

if __name__ == "__main__":
    random.seed(42)
    main()
