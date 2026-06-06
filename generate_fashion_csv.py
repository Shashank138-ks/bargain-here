import random

# Exact first 6 lines from user request
pasted_lines = [
    "10001,Wishful Silver High-Low 184,Wishful,Kurtis & Suits,1398,2185,36,3.3,5724,Flipkart Fashion,https://example.com/images/10001.jpg,https://example.com/product/10001",
    "10002,Anouk Green Patiala 961,Anouk,Kurtis & Suits,1785,5410,67,3.4,47381,Limeroad,https://example.com/images/10002.jpg,https://example.com/product/10002",
    "10003,Aurelia Orange Embroidered 156,Aurelia,Dresses,5737,7859,27,4.7,22254,Ajio,https://example.com/images/10003.jpg,https://example.com/product/10003",
    "10004,Joyalukkas Navy Necklace 44,Joyalukkas,Jewellery,33828,35609,5,3.9,23808,Craftsvilla,https://example.com/images/10004.jpg,https://example.com/product/10004",
    "10005,Joyalukkas Orange Anklet 621,Joyalukkas,Jewellery,54241,60946,11,3.7,14194,Nykaa Fashion,https://example.com/images/10005.jpg,https://example.com/product/10005",
    "10006,Crunchy Fashion Mustard Layered 677,Crunchy Fashion,Accessories,745,819,9,3.2,11268,Tata Cliq Fashion,https://example.com/images/10006.jpg,https://example.com/product/10006"
]

categories = [
    "Kurtis & Suits", "Dresses", "Jewellery", "Accessories",
    "Footwear", "Sarees", "Watches", "Bags & Handbags"
]

brands_by_category = {
    "Kurtis & Suits": ["Wishful", "Anouk", "Aurelia", "Biba", "W", "Fabindia"],
    "Dresses": ["Zara", "H&M", "Vero Moda", "ONLY", "AND", "Forever 21"],
    "Jewellery": ["Joyalukkas", "Malabar Gold", "Tanishq", "Giva", "Sukkhi", "Voylla"],
    "Accessories": ["Crunchy Fashion", "YouBella", "Zaveri Pearls", "Lenskart", "Fastrack"],
    "Footwear": ["Bata", "Metro", "Mochi", "Puma", "Adidas", "Nike", "Crocs"],
    "Sarees": ["Sabyasachi", "Nalli", "Fabindia", "Mimosa", "Vardhaman"],
    "Watches": ["Titan", "Fastrack", "Sonata", "Casio", "Fossil", "Daniel Wellington"],
    "Bags & Handbags": ["Caprese", "Baggit", "Lavie", "Lino Perros", "Hidesign", "Puma"]
}

product_templates = {
    "Kurtis & Suits": ["A-Line Kurta {num}", "Straight Suit Set {num}", "Anarkali Kurti {num}", "Patiala Suit {num}", "Printed Kurta {num}"],
    "Dresses": ["Maxi Dress {num}", "Floral Bodycon {num}", "Casual A-Line Dress {num}", "Evening Gown {num}", "Midi Dress {num}"],
    "Jewellery": ["Silver Necklace {num}", "Gold Plated Ring {num}", "Pearl Earrings {num}", "Diamond Bangle {num}", "Beaded Bracelet {num}"],
    "Accessories": ["Cat-Eye Sunglasses {num}", "Silk Scarf {num}", "Leather Belt {num}", "Hair Clip Set {num}", "Woolen Beanie {num}"],
    "Footwear": ["Running Shoes {num}", "Formal Leather Shoes {num}", "Casual Sneakers {num}", "Strappy Sandals {num}", "Block Heels {num}"],
    "Sarees": ["Banarasi Silk Saree {num}", "Cotton Sari {num}", "Georgette Printed Saree {num}", "Kanchipuram Silk {num}", "Chanderi Saree {num}"],
    "Watches": ["Analog Watch {num}", "Chronograph Watch {num}", "Smart Fitness Band {num}", "Leather Strap Watch {num}", "Stainless Steel Watch {num}"],
    "Bags & Handbags": ["Leather Tote Bag {num}", "Designer Handbag {num}", "Sling Purse {num}", "Travel Backpack {num}", "Clutch Bag {num}"]
}

platforms = ["Flipkart Fashion", "Limeroad", "Craftsvilla", "Nykaa Fashion", "Tata Cliq Fashion", "Amazon Fashion", "Ajio"]

# Seed for reproducibility
random.seed(42)

csv_lines = ["id,product_name,brand,category,price,original_price,discount_percent,rating,reviews,platform,image_url,product_url"]
csv_lines.extend(pasted_lines)

for pid in range(10007, 10344):
    cat = random.choice(categories)
    brand = random.choice(brands_by_category[cat])
    template = random.choice(product_templates[cat])
    pname = f"{brand} {template.format(num=pid % 1000)}"
    
    # Prices
    if cat == "Jewellery":
        price = random.randint(1500, 60000)
    elif cat == "Watches":
        price = random.randint(1000, 25000)
    else:
        price = random.randint(300, 5000)
        
    discount = random.randint(5, 75)
    orig_price = int(price * 100 / (100 - discount))
    rating = round(random.uniform(3.0, 5.0), 1)
    reviews = random.randint(10, 80000)
    plat = random.choice(platforms)
    
    img_url = f"https://example.com/images/{pid}.jpg"
    prod_url = f"https://example.com/product/{pid}"
    
    line = f"{pid},{pname},{brand},{cat},{price},{orig_price},{discount},{rating},{reviews},{plat},{img_url},{prod_url}"
    csv_lines.append(line)

output_path = r"c:\Users\lenovo LOQ\OneDrive\Desktop\sri\products_fashion.csv"
with open(output_path, "w", encoding="utf-8") as f:
    f.write("\n".join(csv_lines))

print(f"Successfully generated {len(csv_lines)-1} fashion products at {output_path}")
