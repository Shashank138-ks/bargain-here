import urllib.request
import urllib.parse
import http.cookiejar
import json
import sys

LOGIN_URL = "http://localhost:5000/login"
QUERIES = [
    "lenovo laptop",
    "oppo tablet",
    "sony camera",
    "lg appliance",
    "samsung tv",
    "boat headphones",
    "redmi phone",
    "garmin watch"
]

def main():
    print("=" * 80)
    print(" RUNNING INTEGRATION ACCURACY TEST ".center(80, "="))
    print("=" * 80)
    
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    
    # Authenticate first
    login_data = urllib.parse.urlencode({
        "username": "IntegrationTester",
        "phoneNumber": "9999999999"
    }).encode("utf-8")
    
    req_login = urllib.request.Request(LOGIN_URL, data=login_data, headers={
        "User-Agent": "BargainHere-Tester",
        "Content-Type": "application/x-www-form-urlencoded"
    })
    
    try:
        opener.open(req_login, timeout=5)
        print("[Auth] Logged in successfully. Checking search queries...\n")
        
        for q in QUERIES:
            api_url = f"http://localhost:5000/api/search?q={urllib.parse.quote(q)}"
            req_api = urllib.request.Request(api_url, headers={"User-Agent": "BargainHere-Tester"})
            
            with opener.open(req_api, timeout=15) as response:
                raw_data = response.read().decode("utf-8")
                data = json.loads(raw_data)
                results = data.get("results", [])
                
                print(f"Query: '{q}' | Total Results: {len(results)}")
                if not results:
                    print("  -> NO RESULTS FOUND!")
                    continue
                
                # Check categories and print first 3 results
                for idx, item in enumerate(results[:3], start=1):
                    platform = item.get("platform", "Unknown")
                    title = item.get("title", "No Title")
                    category = item.get("category", "generic")
                    price = item.get("price_str", "N/A").replace("\u20b9", "Rs. ")
                    
                    # Safe title clean for cmd prompt printing
                    title_clean = title.encode(sys.stdout.encoding, errors='replace').decode(sys.stdout.encoding)
                    print(f"  {idx}. [{platform}] {title_clean} (Category: {category}, Price: {price})")
                print("-" * 80)
                
        print(" INTEGRATION TEST FINISHED ".center(80, "="))
    except Exception as e:
        print(f"ERROR: Test execution failed: {e}")

if __name__ == "__main__":
    main()
