
import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save } from "lucide-react";
import { apiGet, apiPatch, apiPost } from "../services/api";

type RoomData = {
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
};

const initialForm = {
    title: "",
    description: "",
    address: "",
    ward: "",
    district: "",
    city: "",
    monthly_price: "",
    area_m2: "",
    capacity: "1",
    amenities: "",
};

export default function RoomFormPage() {
    const navigate = useNavigate();
    const { roomId } = useParams();
    const isEdit = Boolean(roomId);

    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(isEdit);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!roomId) return;

        const loadRoom = async () => {
            try {
                const room = await apiGet<RoomData>(`admin/rooms/${roomId}`);

                setForm({
                    title: room.title,
                    description: room.description ?? "",
                    address: room.address,
                    ward: room.ward ?? "",
                    district: room.district ?? "",
                    city: room.city,
                    monthly_price: String(room.monthly_price),
                    area_m2: room.area_m2 == null ? "" : String(room.area_m2),
                    capacity: String(room.capacity),
                    amenities: (room.amenities ?? []).join(", "),
                });
            } catch (err) {
                setError(err instanceof Error ? err.message : "Unable to load room.");
            } finally {
                setLoading(false);
            }
        };

        void loadRoom();
    }, [roomId]);

    const updateField = (field: keyof typeof initialForm, value: string) => {
        setForm((current) => ({ ...current, [field]: value }));
    };

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setSaving(true);
        setError("");

        const payload = {
            title: form.title.trim(),
            description: form.description.trim() || null,
            address: form.address.trim(),
            ward: form.ward.trim() || null,
            district: form.district.trim() || null,
            city: form.city.trim(),
            monthly_price: Number(form.monthly_price),
            area_m2: form.area_m2 ? Number(form.area_m2) : null,
            capacity: Number(form.capacity),
            amenities: form.amenities
                .split(",")
                .map((item) => item.trim())
                .filter(Boolean),
        };

        try {
            if (isEdit) {
                await apiPatch(`admin/rooms/${roomId}`, payload);
            } else {
                await apiPost("admin/rooms", payload);
            }

            navigate("/rooms");
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to save room.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading room information...</div>;
    }

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>{isEdit ? "Edit Room" : "Add New Room"}</h1>
                    <p>{isEdit ? "Update room information." : "Create a new rental room."}</p>
                </div>
                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate("/rooms")}
                >
                    <ArrowLeft size={17} /> Back to Rooms
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <form className="form-layout" onSubmit={handleSubmit}>
                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Basic Information</h2>
                        <p>Enter the room details below.</p>
                    </div>

                    <div className="form-grid">
                        <div className="form-group full-width">
                            <label htmlFor="room-title">Room Name</label>
                            <input id="room-title" required maxLength={200}
                                value={form.title}
                                onChange={(e) => updateField("title", e.target.value)}
                                placeholder="Enter room name" />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-address">Street Address</label>
                            <input id="room-address" required
                                value={form.address}
                                onChange={(e) => updateField("address", e.target.value)}
                                placeholder="Enter street address" />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-ward">Ward</label>
                            <input id="room-ward" value={form.ward}
                                onChange={(e) => updateField("ward", e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-district">District</label>
                            <input id="room-district" value={form.district}
                                onChange={(e) => updateField("district", e.target.value)} />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-city">City</label>
                            <input id="room-city" required value={form.city}
                                onChange={(e) => updateField("city", e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-price">Monthly Price (VND)</label>
                            <input id="room-price" type="number" min="0" step="1"
                                required value={form.monthly_price}
                                onChange={(e) => updateField("monthly_price", e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-area">Area (m²)</label>
                            <input id="room-area" type="number" min="0.1" step="0.1"
                                value={form.area_m2}
                                onChange={(e) => updateField("area_m2", e.target.value)} />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-capacity">Capacity</label>
                            <input id="room-capacity" type="number" min="1" step="1"
                                required value={form.capacity}
                                onChange={(e) => updateField("capacity", e.target.value)} />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-description">Description</label>
                            <textarea id="room-description" rows={5}
                                value={form.description}
                                onChange={(e) => updateField("description", e.target.value)} />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-amenities">Amenities</label>
                            <input id="room-amenities"
                                value={form.amenities}
                                onChange={(e) => updateField("amenities", e.target.value)}
                                placeholder="Air conditioner, Wi-Fi, Parking" />
                            <small>Separate amenities with commas.</small>
                        </div>
                    </div>
                </section>

                <div className="form-actions">
                    <button type="button" className="secondary-button"
                        onClick={() => navigate("/rooms")}>
                        Cancel
                    </button>
                    <button type="submit" className="primary-button" disabled={saving}>
                        <Save size={17} />
                        {saving ? "Saving..." : isEdit ? "Save Changes" : "Create Room"}
                    </button>
                </div>
            </form>
        </div>
    );
}
