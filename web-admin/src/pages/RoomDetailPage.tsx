import { useCallback, useEffect, useState, type ChangeEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    Pencil,
    Image as ImageIcon,
    Upload,
    Trash2,
    LoaderCircle,
} from "lucide-react";

import { apiDelete, apiGet, apiPost } from "../services/api";

type CloudinarySignature = {
    cloud_name: string;
    api_key: string;
    timestamp: number;
    folder: string;
    signature: string;
};

type CloudinaryUploadResponse = {
    secure_url: string;
    public_id: string;
};
type RoomImage = {
    id: number;
    image_url: string;
    sort_order: number;
};

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
    image_records: RoomImage[];
};

export default function RoomDetailPage() {
    const navigate = useNavigate();
    const { roomId } = useParams();

    const [room, setRoom] = useState<Room | null>(null);
    const [loading, setLoading] = useState(true);
    const [uploading, setUploading] = useState(false);
    const [deletingImageId, setDeletingImageId] = useState<number | null>(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const loadRoom = useCallback(async () => {
        if (!roomId) return;

        const data = await apiGet<Room>(`admin/rooms/${roomId}`);
        setRoom(data);
    }, [roomId]);

    useEffect(() => {
        let active = true;

        const fetchRoom = async () => {
            try {
                const data = await apiGet<Room>(`admin/rooms/${roomId}`);
                if (active) setRoom(data);
            } catch (err) {
                if (active) {
                    setError(err instanceof Error ? err.message : "Unable to load room.");
                }
            } finally {
                if (active) setLoading(false);
            }
        };

        if (roomId) void fetchRoom();

        return () => {
            active = false;
        };
    }, [roomId]);

    const handleUpload = async (event: ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(event.target.files ?? []);
        event.target.value = "";

        if (!files.length || !room || !roomId) return;

        setError("");
        setSuccess("");

        const invalidFile = files.find(
            (file) =>
                !file.type.startsWith("image/") ||
                file.size > 5 * 1024 * 1024,
        );

        if (invalidFile) {
            setError("Only image files up to 5 MB are allowed.");
            return;
        }

        setUploading(true);

        try {
            const existingImages = room.image_records ?? [];

            for (const [index, file] of files.entries()) {
                const signature = await apiPost<CloudinarySignature>(
                    `admin/rooms/${roomId}/images/signature`,
                    {},
                );

                const formData = new FormData();

                formData.append("file", file);
                formData.append("api_key", signature.api_key);
                formData.append("timestamp", String(signature.timestamp));
                formData.append("folder", signature.folder);
                formData.append("signature", signature.signature);

                const uploadResponse = await fetch(
                    `https://api.cloudinary.com/v1_1/${signature.cloud_name}/image/upload`,
                    {
                        method: "POST",
                        body: formData,
                    },
                );

                const uploadData = await uploadResponse.json();

                if (!uploadResponse.ok) {
                    throw new Error(
                        uploadData.error?.message || "Cloudinary upload failed.",
                    );
                }

                const imageData = uploadData as CloudinaryUploadResponse;

                await apiPost(`admin/rooms/${roomId}/images`, {
                    image_url: imageData.secure_url,
                    sort_order: existingImages.length + index,
                });
            }

            await loadRoom();
            setSuccess("Room images uploaded successfully.");
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to upload room images.",
            );
        } finally {
            setUploading(false);
        }
    };

    const handleDeleteImage = async (image: RoomImage) => {
        if (!roomId) return;

        const confirmed = window.confirm(
            "Are you sure you want to remove this image?",
        );

        if (!confirmed) return;

        setError("");
        setSuccess("");
        setDeletingImageId(image.id);

        try {
            await apiDelete(
                `admin/rooms/${roomId}/images/${image.id}`,
            );

            await loadRoom();
            setSuccess("Room image removed successfully.");
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to remove room image.",
            );
        } finally {
            setDeletingImageId(null);
        }
    };

    if (loading) {
        return <div className="page-container">Loading room details...</div>;
    }

    if (error && !room) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button className="secondary-button" onClick={() => navigate("/rooms")}>
                    <ArrowLeft size={17} /> Back to Rooms
                </button>
            </div>
        );
    }

    if (!room) {
        return <div className="page-container">Room not found.</div>;
    }

    const roomImages = room.image_records ?? [];

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Room Details</h1>
                    <p>View and manage room information, amenities and images.</p>
                </div>

                <div className="detail-header-actions">
                    <button className="secondary-button" onClick={() => navigate("/rooms")}>
                        <ArrowLeft size={17} /> Back
                    </button>
                    <button
                        className="primary-button"
                        onClick={() => navigate(`/rooms/${room.id}/edit`)}
                    >
                        <Pencil size={17} /> Edit Room
                    </button>
                </div>
            </div>

            {error && <div className="dashboard-error room-image-message">{error}</div>}
            {success && <div className="room-image-success">{success}</div>}

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
                                <strong>
                                    {Number(room.monthly_price).toLocaleString("en-US")} VND
                                </strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Area</span>
                                <strong>
                                    {room.area_m2 ? `${room.area_m2} m²` : "—"}
                                </strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Capacity</span>
                                <strong>{room.capacity} people</strong>
                            </div>

                            <div className="detail-info-item full-width">
                                <span>Address</span>
                                <strong>
                                    {[room.address, room.ward, room.district, room.city]
                                        .filter(Boolean)
                                        .join(", ")}
                                </strong>
                            </div>

                            <div className="detail-info-item full-width">
                                <span>Description</span>
                                <strong>
                                    {room.description || "No description provided."}
                                </strong>
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
                                {room.amenities.map((item) => (
                                    <li key={item}>{item}</li>
                                ))}
                            </ul>
                        ) : (
                            <p>No amenities available.</p>
                        )}
                    </section>

                    <section className="content-card room-detail-side room-image-management">
                        <div className="form-section-heading">
                            <h2>Room Images</h2>
                            <p>
                                {roomImages.length} image{roomImages.length === 1 ? "" : "s"} uploaded.
                            </p>
                        </div>

                        <label className="room-image-upload">
                            {uploading ? (
                                <>
                                    <LoaderCircle size={19} className="room-image-spinner" />
                                    <span>Uploading images...</span>
                                </>
                            ) : (
                                <>
                                    <Upload size={19} />
                                    <span>Upload Images</span>
                                </>
                            )}

                            <input
                                type="file"
                                accept="image/*"
                                multiple
                                disabled={uploading}
                                onChange={handleUpload}
                            />
                        </label>

                        <small className="room-image-hint">
                            Image files only. Maximum size: 5 MB per image.
                        </small>

                        {roomImages.length ? (
                            <div className="room-images-grid">
                                {roomImages.map((image, index) => (
                                    <div className="room-image-item" key={image.id}>
                                        <img
                                            src={image.image_url}
                                            alt={`${room.title} ${index + 1}`}
                                        />

                                        <div className="room-image-overlay">
                                            <span>Image {index + 1}</span>
                                            <button
                                                type="button"
                                                className="room-image-delete"
                                                title="Remove image"
                                                disabled={deletingImageId === image.id}
                                                onClick={() => void handleDeleteImage(image)}
                                            >
                                                {deletingImageId === image.id ? (
                                                    <LoaderCircle size={16} />
                                                ) : (
                                                    <Trash2 size={16} />
                                                )}
                                            </button>
                                        </div>
                                    </div>
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
