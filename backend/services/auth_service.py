
from sqlalchemy import text
from sqlalchemy.orm import Session


def sync_user(db: Session, firebase_user: dict) -> dict:
    """Find or create a local user from verified Firebase claims."""

    firebase_uid = firebase_user.get("uid")
    email = firebase_user.get("email")

    if not firebase_uid or not email:
        raise ValueError("Firebase account must have a UID and email.")

    full_name = (
        firebase_user.get("name")
        or email.split("@")[0]
    )

    avatar_url = firebase_user.get("picture")

    result = db.execute(
        text("""
            INSERT INTO users (
                firebase_uid,
                email,
                full_name,
                avatar_url,
                role,
                status
            )
            VALUES (
                :firebase_uid,
                :email,
                :full_name,
                :avatar_url,
                'USER',
                'ACTIVE'
            )
            ON CONFLICT (firebase_uid)
            DO UPDATE SET
                email = EXCLUDED.email,
                full_name = COALESCE(
                    NULLIF(users.full_name, ''),
                    EXCLUDED.full_name
                ),
                avatar_url = COALESCE(
                    users.avatar_url,
                    EXCLUDED.avatar_url
                )
            RETURNING
                id,
                firebase_uid,
                email,
                full_name,
                phone,
                avatar_url,
                role,
                status
        """),
        {
            "firebase_uid": firebase_uid,
            "email": email,
            "full_name": full_name,
            "avatar_url": avatar_url,
        },
    )

    user = dict(result.mappings().one())
    db.commit()

    return user
