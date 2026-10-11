import sqlite3
from pathlib import Path
from typing import Any


DATABASE_PATH = Path(__file__).with_name("rooms.db")


def get_connection() -> sqlite3.Connection:
	connection = sqlite3.connect(DATABASE_PATH)
	connection.row_factory = sqlite3.Row
	return connection


def initialize_database() -> None:
	with get_connection() as connection:
		connection.execute(
			"""
			CREATE TABLE IF NOT EXISTS rooms (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				name TEXT NOT NULL,
				members INTEGER NOT NULL DEFAULT 0 CHECK (members >= 0)
			)
			"""
		)
		room_count = connection.execute("SELECT COUNT(*) FROM rooms").fetchone()[0]
		if room_count == 0:
			connection.executemany(
				"INSERT INTO rooms (name, members) VALUES (?, ?)",
				[
					("Phòng ôn thi Toán", 12),
					("Phòng luyện viết Essay", 8),
					("Phòng IELTS Speaking", 15),
				],
			)


def get_rooms_from_database() -> list[dict[str, Any]]:
	with get_connection() as connection:
		rows = connection.execute(
			"SELECT id, name, members FROM rooms ORDER BY id"
		).fetchall()
	return [dict(row) for row in rows]


def create_room_in_database(name: str, members: int) -> dict[str, Any]:
	with get_connection() as connection:
		cursor = connection.execute(
			"INSERT INTO rooms (name, members) VALUES (?, ?)",
			(name, members),
		)
		room = connection.execute(
			"SELECT id, name, members FROM rooms WHERE id = ?",
			(cursor.lastrowid,),
		).fetchone()
	return dict(room)
