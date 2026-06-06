import requests
import json

def test_search(query):
    # Log in first to get session cookie
    session = requests.Session()
    login_url = "http://localhost:5000/login"
    login_data = {
        "username": "IntegrationTester",
        "phoneNumber": "9876543210"
    }
    
    print(f"Logging into {login_url}...")
    resp = session.post(login_url, data=login_data, allow_redirects=False)
    print(f"Login status code: {resp.status_code}")
    
    # Run search request
    search_url = f"http://localhost:5000/api/search?q={query}"
    print(f"Requesting search: {search_url}...")
    resp = session.get(search_url)
    
    if resp.status_code != 200:
        print(f"[ERROR] Search failed with status: {resp.status_code}")
        print(resp.text)
        return
        
    data = resp.json()
    results = data.get("results", [])
    print(f"Found {len(results)} results.")
    
    for item in results[:5]:
        print(f"Platform: {item.get('platform')}")
        print(f"Title: {item.get('title')}")
        print(f"Brand: {item.get('brand')}")
        print(f"URL: {item.get('url')}")
        print("-" * 50)

if __name__ == "__main__":
    test_search("realme")
    test_search("vivo")
