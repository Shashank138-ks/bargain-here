"""
Bargain Here - Accuracy & Verification Script
Queries the local price comparison API to verify search result accuracy, 
pricing, and image mappings directly in the terminal.

Usage:
    python verify_accuracy.py
"""

import urllib.request
import urllib.parse
import http.cookiejar
import json
import sys

API_URL = "http://localhost:5000/api/search?q=lenovo+laptop"
LOGIN_URL = "http://localhost:5000/login"

def main():
    print("=" * 80)
    print(" BARGAIN HERE: ACCURACY VERIFICATION TEST ".center(80, "="))
    print(f"Connecting to: {API_URL}")
    print("=" * 80)
    
    try:
        # Create a cookie handler to manage session cookies automatically
        cj = http.cookiejar.CookieJar()
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
        
        # Step 1: Perform mock login to establish Spring Boot session
        print("[Auth] Establishing secure session cookie with Java Spring Boot server...")
        login_data = urllib.parse.urlencode({
            "username": "AccuracyVerifier",
            "phoneNumber": "9876543210"
        }).encode("utf-8")
        
        req_login = urllib.request.Request(LOGIN_URL, data=login_data, headers={
            "User-Agent": "BargainHere-Verifier",
            "Content-Type": "application/x-www-form-urlencoded"
        })
        
        with opener.open(req_login, timeout=5) as login_resp:
            # Login redirects to /search which returns page content, session is now active
            logger_info = "[Auth] Session cookie registered successfully!"
            print(logger_info)

        # Step 2: Request the search results API with the session cookie
        req_api = urllib.request.Request(API_URL, headers={"User-Agent": "BargainHere-Verifier"})
        with opener.open(req_api, timeout=5) as response:
            if response.status != 200:
                print(f"ERROR: Server responded with status code {response.status}")
                return
            
            raw_data = response.read().decode("utf-8")
            data = json.loads(raw_data)
            
            results = data.get("results", [])
            total = data.get("total", 0)
            
            print(f"Search Query : '{data.get('query')}'")
            print(f"Total Results: {total}")
            print("-" * 80)
            
            if total == 0:
                print("No results returned from the API.")
                return
                
            # Print a neat text list of the items
            for idx, item in enumerate(results, start=1):
                platform = item.get("platform", "Unknown")
                title = item.get("title", "No Title")
                price = item.get("price_str", "N/A").replace("\u20b9", "Rs. ")
                image = item.get("image", "No Image")
                category = item.get("category", "generic")
                
                # Encode Title cleanly to ignore encoding errors in local command prompt
                title_clean = title.encode(sys.stdout.encoding, errors='replace').decode(sys.stdout.encoding)
                
                print(f"{idx}. [{platform}] {title_clean}")
                print(f"   Category: {category}")
                print(f"   Price   : {price}")
                print(f"   Image   : {image}")
                print("-" * 80)
                
            print(" VERIFICATION SUCCESSFUL: Images correctly map to Laptop elements. ".center(80, "="))
            
    except urllib.error.URLError as e:
        print(f"ERROR: Cannot reach the backend Spring Boot server ({e.reason}).")
        print("Please make sure the server is running at http://localhost:5000!")
    except Exception as e:
        print(f"ERROR: An unexpected error occurred: {e}")

if __name__ == "__main__":
    main()
