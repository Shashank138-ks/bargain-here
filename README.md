# 🛒 Bargain Here — Real-Time Indian E-Commerce Price Comparison Engine

Bargain Here is a modern, high-performance web comparison search engine that searches across 13 major Indian e-commerce platforms in real time to locate the lowest prices for any product.

Developed with a sleek, premium, highly interactive dark-themed dashboard frontend and an asynchronous Flask multi-threaded concurrent scraping backend.

---

## 🌟 Key Features

- **Concurrent Engine**: Utilizes python `threading` to query 13 platforms concurrently, bringing search times down to seconds.
- **Smart Hybrid Scraper**: Automatically attempts live e-commerce scraping, falling back on an intelligent, procedurally generated, consistent mock engine for platforms that are blocked or offline.
- **13 Supported Indian Platforms**:
  - Amazon India
  - Flipkart
  - Meesho
  - Myntra
  - JioMart
  - Shopsy
  - Blinkit
  - Swiggy Instamart
  - Purplle
  - Tira Beauty
  - NewMe Asia
  - FreeUp
  - Savana
- **Interactive Premium UI**: 
  - Gorgeous modern typography (Syne & DM Sans) and glassmorphism styling.
  - Interactive grid and spreadsheet/table views.
  - Sorting (Low-to-High, High-to-Low, By Platform).
  - Platform quick-filters (chips) with custom theme accents.
  - Automatic identification of the **Absolute Cheapest Deal** with custom saving badge highlighting the comparison against the highest price.

---

## 📁 File Structure

- `choukasi_backend.py`: Asynchronous Flask API server managing scraping threads, headers, proxies rotation, and fallback simulations.
- `choukasi_frontend.html`: Single-page comparison dashboard application containing design tokens, animations, sorting, and grid rendering.
- `requirements.txt`: Python package dependency manifest.
- `run.bat`: Fully-automated runner script to verify environments, install dependencies, spin up the backend, and open the frontend in your default browser in one click.

---

## 🚀 Quick Start

### Windows (One-Click)
Simply double-click the **`run.bat`** file in this directory. 
The script will automatically check if python is active, install dependencies, boot the backend API server, and launch the web interface in your default browser.

### Manual Launch
1. Install Python 3.x.
2. Install Python packages:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the Flask backend:
   ```bash
   python choukasi_backend.py
   ```
4. Open **`choukasi_frontend.html`** in any modern web browser.

---

## ⚡ Backend API Endpoints

Once the backend is active on `http://localhost:5000`, the following REST endpoints are available:

- **Health Check**:
  `GET http://localhost:5000/api/health`
- **Supported Platforms**:
  `GET http://localhost:5000/api/platforms`
- **Compare search**:
  `GET http://localhost:5000/api/search?q=tshirt&platforms=meesho,amazon,myntra`

---

*Enjoy finding the cheapest deals with Bargain Here! 🥇*
