import sqlite3
import os
import random
import re

DB_NAME = "choukasi_products.db"

def make_direct_url(platform, title, row_id):
    slug = re.sub(r'[^a-zA-Z0-9]+', '-', title.lower()).strip('-')
    if not slug:
        slug = f"product-{row_id}"
        
    plat = platform.lower().strip()
    
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


PLATFORMS_INFO = {
    "Amazon": {
        "color": "#FF9900",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/a/a9/Amazon_logo.svg",
        "base_url": "https://www.amazon.in/s?k="
    },
    "Flipkart": {
        "color": "#2874F0",
        "logo": "https://upload.wikimedia.org/wikipedia/commons/7/7a/Flipkart_logo.svg",
        "base_url": "https://www.flipkart.com/search?q="
    },
    "Tata Cliq": {
        "color": "#000000",
        "logo": "https://www.tatacliq.com/favicon.ico",
        "base_url": "https://www.tatacliq.com/search/?text="
    },
    "Snapdeal": {
        "color": "#E40046",
        "logo": "https://www.snapdeal.com/favicon.ico",
        "base_url": "https://www.snapdeal.com/search?keyword="
    },
    "Ajio": {
        "color": "#2C3E50",
        "logo": "https://www.ajio.com/favicon.ico",
        "base_url": "https://www.ajio.com/search/?text="
    },
    "Meesho": {
        "color": "#F43397",
        "logo": "https://images.meesho.com/images/marketing/1655285313128.png",
        "base_url": "https://www.meesho.com/search?q="
    },
    "Croma": {
        "color": "#00E9C0",
        "logo": "https://www.croma.com/favicon.ico",
        "base_url": "https://www.croma.com/search/?text="
    },
    "Vijay Sales": {
        "color": "#D12229",
        "logo": "https://www.vijaysales.com/favicon.ico",
        "base_url": "https://www.vijaysales.com/search?q="
    },
    "Reliance Digital": {
        "color": "#E42526",
        "logo": "https://www.reliancedigital.in/favicon.ico",
        "base_url": "https://www.reliancedigital.in/search?q="
    },
    "Myntra": {
        "color": "#FF3F6C",
        "logo": "https://constant.myntassets.com/web/assets/img/logo_myntra2x.png",
        "base_url": "https://www.myntra.com/search?q="
    }
}

# The 16 target categories with balanced distributions (100,000 total)
CATEGORY_COUNTS = {
    "tablets": 6250,
    "laptops": 6250,
    "cameras": 6250,
    "home_appliances": 6250,
    "televisions": 6250,
    "headphones": 6250,
    "smartphones": 6250,
    "smartwatches": 6250,
    "kurtis_suits": 6250,
    "dresses": 6250,
    "jewellery": 6250,
    "accessories": 6250,
    "footwear": 6250,
    "sarees": 6250,
    "watches": 6250,
    "bags_handbags": 6250
}

# Brand mappings per category
CATEGORY_BRANDS = {
    "tablets": ["Apple", "Samsung", "Lenovo", "Xiaomi", "Oppo", "Realme", "Motorola"],
    "laptops": ["Dell", "HP", "Lenovo", "Asus", "Acer", "Apple", "Xiaomi"],
    "cameras": ["Sony", "Canon", "Nikon", "GoPro", "Fujifilm", "Panasonic"],
    "home_appliances": ["LG", "Samsung", "Whirlpool", "Bosch", "Godrej", "Haier", "Panasonic"],
    "televisions": ["LG", "Sony", "Samsung", "Xiaomi", "OnePlus", "Realme", "TCL"],
    "headphones": ["Boat", "JBL", "Sony", "OnePlus", "Realme", "Noise", "Apple"],
    "smartphones": ["Apple", "Samsung", "OnePlus", "Xiaomi", "Oppo", "Vivo", "Realme", "Motorola"],
    "smartwatches": ["Apple", "Samsung", "OnePlus", "Noise", "Fire-Boltt", "Boat", "Fitbit", "Garmin"],
    "kurtis_suits": ["Biba", "W", "Aurelia", "Anouk", "Wishful", "Fabindia"],
    "dresses": ["Zara", "H&M", "Vero Moda", "ONLY", "AND", "Forever 21"],
    "jewellery": ["Giva", "Tanishq", "Joyalukkas", "Malabar Gold", "Sukkhi", "Voylla", "Zaveri Pearls"],
    "accessories": ["Fastrack", "Lenskart", "YouBella", "Crunchy Fashion", "Zara", "H&M"],
    "footwear": ["Nike", "Adidas", "Puma", "Bata", "Metro", "Mochi", "Crocs"],
    "sarees": ["Nalli", "Sabyasachi", "Mimosa", "Fabindia", "Vardhaman"],
    "watches": ["Titan", "Fastrack", "Sonata", "Casio", "Fossil", "Daniel Wellington"],
    "bags_handbags": ["Caprese", "Lavie", "Baggit", "Hidesign", "Lino Perros", "Puma"]
}

# Category-specific keywords for high classification accuracy
CATEGORY_KEYWORDS = {
    "tablets": ["Tablet", "iPad", "Tab"],
    "laptops": ["Laptop", "Notebook", "Chromebook", "MacBook"],
    "cameras": ["DSLR", "Mirrorless Camera", "Action Camera", "Handycam"],
    "home_appliances": ["Refrigerator", "Washing Machine", "Microwave Oven", "Air Conditioner", "Vacuum Cleaner"],
    "televisions": ["Smart TV", "LED TV", "OLED TV", "Ultra HD TV"],
    "headphones": ["Headphones", "Earphones", "Wireless Earbuds", "AirPods", "Headset"],
    "smartphones": ["Smartphone", "Mobile Phone", "Cell Phone"],
    "smartwatches": ["Smartwatch", "Fitness Tracker", "Smart Band"],
    "kurtis_suits": ["Kurti", "Suit Set", "Salwar Kurta"],
    "dresses": ["A-Line Dress", "Evening Gown", "Maxi Dress", "Bodycon Dress"],
    "jewellery": ["Silver Necklace", "Gold Ring", "Anklet", "Diamond Bangle"],
    "accessories": ["Sunglasses", "Leather Belt", "Silk Scarf", "Hair Clip Set"],
    "footwear": ["Running Shoes", "Block Heels", "Leather Boots", "Slippers"],
    "sarees": ["Silk Saree", "Georgette Saree", "Banarasi Saree"],
    "watches": ["Analog Watch", "Chronograph Watch", "Automatic Watch"],
    "bags_handbags": ["Handbag", "Tote Bag", "Backpack", "Sling Bag"]
}

# Unsplash photo ID pools per category for high-fidelity diversity
CATEGORY_PHOTOS = {
    "tablets": ["photo-1544244015-0df4b3ffc6b0", "photo-1589739900243-4b52cd9b104e", "photo-1561154464-82e9adf32764"],
    "laptops": ["photo-1496181133206-80ce9b88a853", "photo-1517336714731-489689fd1ca8", "photo-1588872657578-7efd1f1555ed"],
    "cameras": ["photo-1516035069371-29a1b244cc32", "photo-1502920917128-1da500748014", "photo-1510127852285-5b8d757a5e36"],
    "home_appliances": ["photo-1584622650111-993a426fbf0a", "photo-1574362848149-11496d93a7c7", "photo-1585515320310-259814833e6c"],
    "televisions": ["photo-1593305841991-05c297ba4575", "photo-1593789198777-f29bc259780e", "photo-1461151304267-38535e780c79"],
    "headphones": ["photo-1505740420928-5e560c06d30e", "photo-1546435770-a3e426bf472b", "photo-1583394838336-acd977736f90"],
    "smartphones": ["photo-1511707171634-5f897ff02aa9", "photo-1598327105666-5b89351aff97", "photo-1580910051074-3eb694886505"],
    "smartwatches": ["photo-1546868871-7041f2a55e12", "photo-1508685096489-7aacd43bd3b1", "photo-1434494878577-86c23bcb06b9"],
    "kurtis_suits": ["photo-1583391733956-3750e0ff4e8b"],
    "dresses": ["photo-1595777457583-95e059d581b8"],
    "jewellery": ["photo-1515562141207-7a88fb7ce338"],
    "accessories": ["photo-1523293182086-7651a899d37f"],
    "footwear": ["photo-1549298916-b41d501d3772"],
    "sarees": ["photo-1610030469983-98e550d6193c"],
    "watches": ["photo-1522312346375-d1a52e2b99b3"],
    "bags_handbags": ["photo-1584917865442-de89df76afd3"]
}

# A pool of 300 dictionary words to draw from to generate premium name patterns
DICTIONARY_WORDS = [
    "True", "Gas", "Box", "Between", "Concern", "Firm", "Some", "Fear", "Two", "See", "Difference", "Where", "Too",
    "Edge", "Gun", "Set", "Radio", "Bar", "Hot", "Mr", "Body", "Around", "Friend", "Commercial", "Middle", "Fly",
    "Position", "Wife", "Pass", "Chair", "Voice", "Our", "Positive", "South", "Pay", "Learn", "Strong", "Poor",
    "Structure", "Sister", "Treatment", "Lawyer", "Hope", "Place", "Provide", "Region", "Person", "More", "Indicate",
    "Guy", "Tv", "Enjoy", "Deal", "Show", "Animal", "Charge", "Impact", "Main", "Side", "Term", "Light", "Movement",
    "Member", "Both", "Walk", "Wrong", "Include", "Call", "So", "Feel", "Daughter", "Drive", "Fund", "Big", "Ground",
    "Once", "Her", "Instead", "Interview", "His", "Total", "Book", "Meeting", "Rise", "Inside", "Contain", "Film",
    "And", "News", "Official", "Financial", "Real", "Matter", "Turn", "Sometimes", "Wide", "Realize", "Foreign",
    "Research", "Have", "Relationship", "Blood", "Century", "Know", "Shoulder", "Can", "Everything", "Result",
    "Increase", "Magazine", "Business", "Hit", "Particularly", "Control", "Out", "Hospital", "Nice", "Break",
    "However", "Operation", "Summer", "Ask", "Central", "Identify", "Look", "Season", "Born", "Smile", "Home",
    "Subject", "Same", "Mrs", "Care", "Produce", "Another", "Few", "Modern", "Beyond", "Senior", "Cover", "Interesting",
    "Training", "Age", "Threat", "Cultural", "Determine", "Yourself", "Very", "Camera", "Lot", "Want", "Program",
    "Give", "Election", "Quickly", "Black", "Economic", "Hand", "Game", "Figure", "Bit", "Race", "Center", "Project",
    "Wait", "Lose", "Western", "New", "International", "Dark", "This", "Type", "According", "Effort", "Data", "Cause",
    "Mouth", "Everybody", "Model", "Great", "Close", "Social", "Next", "Like", "Ok", "Man", "Put", "Half", "Trouble",
    "Per", "Computer", "Simple", "Actually", "Tough", "Often", "Class", "Describe", "Just", "Attorney", "Attack",
    "Sure", "East", "Remain", "Purpose", "Treat", "Need", "Authority", "Recent", "Skin", "Organization", "Prevent",
    "Head", "Prove", "Building", "For", "Speech", "After", "Claim", "Affect", "Consumer", "Community", "Southern",
    "Window", "Hard", "Low", "It", "Anyone", "Method", "Read", "Girl", "Happen", "Bed", "History", "Laugh", "Budget",
    "First", "Successful", "Short", "Suffer", "Fall", "Eat", "Whether", "Card", "Campaign", "Certainly", "Part",
    "Build", "Do", "Step", "Nation", "Protect", "Color", "Buy", "Worker", "Performance", "Series", "Probably",
    "Seek", "Either", "Will", "How", "Make", "Even", "Street", "Participant", "Third", "Management", "Use", "Thousand",
    "Test", "During", "List", "That", "May", "Listen", "Student", "Newspaper", "Number", "Exist", "Idea", "Whatever",
    "Must", "Value", "Travel", "Leave", "Common", "Indeed", "Return", "Special", "Leg", "Simply", "Win", "Could",
    "Above", "Style", "Fish", "Local", "Success", "Audience", "Ago", "Huge", "Democratic", "Wall", "Benefit",
    "Feeling", "Stuff", "Memory", "Kid", "Information", "Nothing", "People", "Everyone", "Pull", "Language", "Art",
    "Those", "Last", "Recently", "Us", "Party", "Away", "Government", "Without", "Since", "Front", "Opportunity",
    "Into", "Sport", "Young", "Begin", "We", "Institution", "Relate", "Back", "When", "Message", "Quite", "Pretty",
    "Necessary", "Discover", "Something", "Onto", "Draw", "Compare", "Soon", "Coach", "Various", "Red", "A", "Cold",
    "Why", "Year", "Weight", "Majority", "Expert", "Allow", "By", "Hundred", "Word", "Reveal", "Player", "Carry",
    "Unit", "White", "Receive", "Several", "Garden", "Anything", "Live", "Practice", "Strong", "Security", "To",
    "Approach", "Follow", "Entire", "Room", "Standard", "Population"
]

def init_db():
    print(f"Connecting to database: {DB_NAME}...")
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    # Drop existing table to ensure clean refresh
    cursor.execute("DROP TABLE IF EXISTS products")

    # Create table schema
    cursor.execute("""
        CREATE TABLE products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            platform TEXT NOT NULL,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            price REAL NOT NULL,
            price_str TEXT NOT NULL,
            image TEXT,
            url TEXT,
            logo TEXT,
            color TEXT,
            specs TEXT
        )
    """)
    print("Created 'products' table schema.")

    # Seed random number generator for reproducibility
    random.seed(42)

    db_rows = []
    total_generated = 0
    unique_titles = set()

    for category, count in CATEGORY_COUNTS.items():
        print(f"Generating {count} products for category '{category}'...")
        brands = CATEGORY_BRANDS[category]
        photos = CATEGORY_PHOTOS[category]
        category_generated = 0

        # Specifications choices
        ram_options = [4, 6, 8, 12, 16]
        storage_options = [64, 128, 256, 512]
        size_options_tablet = [8.7, 10.1, 10.9, 11.0, 12.4]
        size_options_tv = [32, 43, 50, 55, 65, 75]
        mp_options = [12, 24.2, 45.7, 50, 108]
        cpu_options = ["Intel i5", "Intel i7", "AMD Ryzen 5", "AMD Ryzen 7", "Apple M2", "Apple M3"]
        stars_options = [3, 4, 5]
        capacity_liters = ["190L", "250L", "300L", "6kg", "7kg", "8kg"]
        playback_hours = [20, 30, 40, 50, 60]

        while category_generated < count:
            brand = random.choice(brands)
            kw = random.choice(CATEGORY_KEYWORDS[category])
            word = random.choice(DICTIONARY_WORDS)
            num = random.randint(1, 99)
            title = f"{brand} {kw} {word} {num}"

            # Ensure unique title within the DB to avoid duplicates
            if title in unique_titles:
                continue
            unique_titles.add(title)

            # Platform selection
            platform_name = random.choice(list(PLATFORMS_INFO.keys()))
            plat_info = PLATFORMS_INFO[platform_name]

            # Price baseline and specification generator
            if category == "tablets":
                base_price = random.randint(8000, 75000)
                specs = f"{random.choice(size_options_tablet)}\" Screen, {random.choice(ram_options)}GB RAM, {random.choice(storage_options)}GB Storage, Wi-Fi+4G"
            elif category == "laptops":
                base_price = random.randint(25000, 150000)
                specs = f"{random.choice(cpu_options)} CPU, {random.choice(ram_options)}GB RAM, {random.choice(storage_options)}GB SSD, Windows 11"
            elif category == "cameras":
                base_price = random.randint(12000, 125000)
                specs = f"{random.choice(mp_options)}MP DSLR Camera, {random.randint(3, 10)}x Zoom, 4K Ultra HD Video"
            elif category == "home_appliances":
                base_price = random.randint(4000, 65000)
                specs = f"{random.choice(stars_options)}-Star Energy Rating, Capacity: {random.choice(capacity_liters)}, Direct Cool Tech"
            elif category == "televisions":
                base_price = random.randint(10000, 140000)
                specs = f"{random.choice(size_options_tv)}\" 4K Smart Android TV, Dolby Atmos Sound, HDR10+"
            elif category == "headphones":
                base_price = random.randint(600, 22000)
                specs = f"Active Noise Cancellation, {random.choice(playback_hours)}hr Battery, Bluetooth 5.3"
            elif category == "smartphones":
                base_price = random.randint(5000, 110000)
                specs = f"{random.choice(ram_options)}GB RAM, {random.choice(storage_options)}GB ROM, 50MP Triple Camera, 5000mAh"
            elif category == "smartwatches":
                base_price = random.randint(1200, 35000)
                specs = f"AMOLED Display, Blood Oxygen & Sleep Tracker, IP68 Waterproof, {random.randint(5, 14)} Day Battery"
            elif category == "kurtis_suits":
                base_price = random.randint(499, 4999)
                specs = "Pure Cotton, Breathable fabric, Elegant ethnic design, Hand Wash Only"
            elif category == "dresses":
                base_price = random.randint(799, 8999)
                specs = "Designer fitting, Evening party wear, High-quality polyester blend, Dry Clean Only"
            elif category == "jewellery":
                base_price = random.randint(5000, 95000)
                specs = "92.5 Sterling Silver, Authenticity Certificate Included, Premium gift box packaging"
            elif category == "accessories":
                base_price = random.randint(199, 2999)
                specs = "Premium build, Durable finish, Versatile fashion statement styling"
            elif category == "footwear":
                base_price = random.randint(499, 9999)
                specs = "Cushioned insole, Non-slip durable rubber sole, Perfect grip styling"
            elif category == "sarees":
                base_price = random.randint(999, 25000)
                specs = "Authentic premium weave, Zari border detailing, Length: 5.5 meters with blouse piece"
            elif category == "watches":
                base_price = random.randint(999, 35000)
                specs = "Water resistant up to 30m, Stainless steel construction, Japanese quartz movement"
            elif category == "bags_handbags":
                base_price = random.randint(599, 7999)
                specs = "Spacious zip compartments, Premium faux leather finish, Adjustable shoulder strap handles"
            else:
                base_price = 1000
                specs = "High-quality consumer product"

            # Apply platform price adjustments slightly
            multiplier = {
                "Amazon": 1.00,
                "Flipkart": 0.98,
                "Meesho": 0.88,
                "Myntra": 1.02,
                "Ajio": 0.93,
                "Tata Cliq": 0.97,
                "Snapdeal": 0.90,
                "Croma": 1.01,
                "Vijay Sales": 0.99,
                "Reliance Digital": 1.00
            }[platform_name]

            price = int(base_price * multiplier)
            if price > 20:
                price = (price // 10) * 10 - 1 if (price % 10 < 5) else (price // 10) * 10 + 9
            if price < 5:
                price = 5

            price_str = f"₹{price:,}"
            
            # Select deterministic category image from the pool
            photo_id = photos[category_generated % len(photos)]
            image_url = f"https://images.unsplash.com/{photo_id}?w=400&auto=format&fit=crop&q=80"
            
            # Generate mock URL
            url = make_direct_url(platform_name, title, total_generated)

            db_rows.append((
                platform_name,
                title,
                category,
                float(price),
                price_str,
                image_url,
                url,
                plat_info["logo"],
                plat_info["color"],
                specs
            ))

            category_generated += 1
            total_generated += 1

    # Insert rows in batch
    cursor.executemany("""
        INSERT INTO products (platform, title, category, price, price_str, image, url, logo, color, specs)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, db_rows)

    conn.commit()
    conn.close()

    print("==============================================================")
    print("DATABASE SEEDING COMPLETED SUCCESSFULLY!")
    print(f"   - Total Seeded Products     : {total_generated}")
    print(f"   - Unique Naming Verifications: {len(unique_titles)} / 5000")
    print(f"   - SQLite File Location      : {os.path.abspath(DB_NAME)}")
    print("==============================================================")

if __name__ == "__main__":
    init_db()
