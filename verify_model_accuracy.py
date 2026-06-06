import joblib
import sqlite3
import os
from sklearn.metrics import accuracy_score, classification_report

def main():
    print("=" * 80)
    print(" EVALUATING SAVED ML MODEL ACCURACY ON DATABASE ".center(80, "="))
    print("=" * 80)

    db_path = "choukasi_products.db"
    model_path = "product_classifier.pkl"
    vectorizer_path = "tfidf_vectorizer.pkl"

    # Verify paths exist
    if not os.path.exists(db_path):
        print(f"ERROR: Database file '{db_path}' not found.")
        return
    if not os.path.exists(model_path) or not os.path.exists(vectorizer_path):
        print(f"ERROR: Saved model pickles not found. Please train first.")
        return

    # 1. Load model and vectorizer
    print("[AI Loader] Loading saved classifier and vectorizer...")
    model = joblib.load(model_path)
    vectorizer = joblib.load(vectorizer_path)

    # 2. Load database rows
    print("[Database] Loading evaluation dataset...")
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT title, category FROM products WHERE category IS NOT NULL AND category != 'generic'")
    rows = cursor.fetchall()
    conn.close()

    if not rows:
        print("ERROR: No labeled products found in the database.")
        return

    titles = [row[0] for row in rows]
    true_categories = [row[1] for row in rows]
    print(f"[Database] Loaded {len(titles)} products for evaluation.")

    # 3. Vectorize text features
    print("[AI Pipeline] Transforming text features via TF-IDF...")
    X_vec = vectorizer.transform(titles)

    # 4. Predict
    print("[AI Pipeline] Generating predictions on all rows...")
    predictions = model.predict(X_vec)

    # 5. Compute accuracy
    accuracy = accuracy_score(true_categories, predictions)
    print("\n" + "=" * 40)
    print(f"Overall Model Accuracy: {accuracy * 100:.2f}%")
    print("=" * 40 + "\n")

    print("Classification Report:")
    print(classification_report(true_categories, predictions))

if __name__ == "__main__":
    main()
