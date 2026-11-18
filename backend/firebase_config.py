
import os
from pathlib import Path

import firebase_admin
from firebase_admin import auth, credentials
from dotenv import load_dotenv

load_dotenv()

BASE_DIR = Path(__file__).resolve().parent

FIREBASE_PROJECT_ID = os.getenv("FIREBASE_PROJECT_ID")
FIREBASE_CREDENTIALS_PATH = os.getenv(
    "FIREBASE_CREDENTIALS_PATH",
    "./firebase-service-account.json",
)
FIREBASE_STORAGE_BUCKET = os.getenv("FIREBASE_STORAGE_BUCKET")

credentials_path = Path(FIREBASE_CREDENTIALS_PATH)

if not credentials_path.is_absolute():
    credentials_path = BASE_DIR / credentials_path


def get_firebase_app():
    """Initialize Firebase Admin SDK once."""
    if firebase_admin._apps:
        return firebase_admin.get_app()

    if not FIREBASE_PROJECT_ID:
        raise RuntimeError("FIREBASE_PROJECT_ID is not configured.")

    if not credentials_path.is_file():
        raise FileNotFoundError(
            f"Firebase service account not found: {credentials_path}"
        )

    firebase_credentials = credentials.Certificate(
        str(credentials_path)
    )

    options = {}

    if FIREBASE_STORAGE_BUCKET:
        options["storageBucket"] = FIREBASE_STORAGE_BUCKET

    return firebase_admin.initialize_app(
        firebase_credentials,
        options=options,
    )


def verify_id_token(id_token: str) -> dict:
    """Verify Firebase ID token and return decoded claims."""
    app = get_firebase_app()

    return auth.verify_id_token(
        id_token,
        app=app,
        check_revoked=True,
    )
