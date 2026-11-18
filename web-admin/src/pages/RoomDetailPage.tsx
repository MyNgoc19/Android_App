
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Pencil, Image as ImageIcon } from "lucide-react";
import { apiGet } from "../services/api";

type Room = {
    id: number;
    title: string;
    description: string | null;
    address: string;
    ward: string | null;
    district: string | null;
    city: string;
    monthly_price: number;
    area_m2: number | null;
    capacity: number;
    amenities: string[];
    status: string;
    images: string[];
};

export default function RoomDetailPage() {
    const navigate = useNavigate();
    const { roomId } = useParams();
    const [room, setRoom] = useState<Room | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!roomId) return;

        apiGet<Room>(`admin/rooms/${roomId}`)
            .then(setRoom)
            .catch((err: unknown) => {
                setError(err instanceof Error ? err.message : "Unable to load room.");
            })
            .finally(() => setLoading(false));
    }, [roomId]);

    if (loading) return <div className="page-container">Loading room details...</div>;
    if (error || !room) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error || "Room not found."}</div>
                <button className="secondary-button" onClick={() => navigate("/rooms")}>
                    <ArrowLeft size={17} /> Back to Rooms
                </button>
            </div>
        );
    }

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Room Details</h1>
                    <p>View room information, amenities and images.</p>
                </div>
                <div className="detail-header-actions">
                    <button className="secondary-button" onClick={() => navigate("/rooms")}>
                        <ArrowLeft size={17} /> Back
                    </button>
                    <button className="primary-button"
                        onClick={() => navigate(`/rooms/${room.id}/edit`)}>
                        <Pencil size={17} /> Edit Room
                    </button>
                </div>
            </div>

            <div className="room-detail-grid">
                <section className="content-card room-detail-main">
                    <div className="room-detail-content">
                        <div className="room-detail-title">
                            <div>
                                <h2>{room.title}</h2>
                                <p>Room ID: {room.id}</p>
                            </div>
                            <span className={`dashboard-status ${room.status.toLowerCase()}`}>
                                {room.status}
                            </span>
                        </div>

                        <div className="detail-info-grid">
                            <div className="detail-info-item">
                                <span>Monthly Price</span>
                                <strong>{Number(room.monthly_price).toLocaleString("en-US")} VND</strong>
                            </div>
                            <div className="detail-info-item">
                                <span>Area</span>
                                <strong>{room.area_m2 ? `${room.area_m2} m²` : "—"}</strong>
                            </div>
                            <div className="detail-info-item">
                                <span>Capacity</span>
                                <strong>{room.capacity} people</strong>
                            </div>
                            <div className="detail-info-item full-width">
                                <span>Address</span>
                                <strong>
                                    {[room.address, room.ward, room.district, room.city]
                                        .filter(Boolean).join(", ")}
                                </strong>
                            </div>
                            <div className="detail-info-item full-width">
                                <span>Description</span>
                                <strong>{room.description || "No description provided."}</strong>
                            </div>
                        </div>
                    </div>
                </section>

                <div className="room-detail-sidebar">
                    <section className="content-card room-detail-side">
                        <div className="form-section-heading">
                            <h2>Amenities</h2>
                            <p>Room facilities and services.</p>
                        </div>
                        {room.amenities?.length ? (
                            <ul className="room-amenities">
                                {room.amenities.map((item) => <li key={item}>{item}</li>)}
                            </ul>
                        ) : <p>No amenities available.</p>}
                    </section>

                    <section className="content-card room-detail-side">
                        <div className="form-section-heading">
                            <h2>Room Images</h2>
                            <p>Images associated with this room.</p>
                        </div>
                        {room.images?.length ? (
                            <div className="room-images-grid">
                                {room.images.map((url, index) => (
                                    <img key={`${url}-${index}`} src={url}
                                        alt={`${room.title} ${index + 1}`} />
                                ))}
                            </div>
                        ) : (
                            <div className="room-no-images">
                                <ImageIcon size={30} />
                                <span>No images available.</span>
                            </div>
                        )}
                    </section>
                </div>
            </div>
        </div>
    );
}
