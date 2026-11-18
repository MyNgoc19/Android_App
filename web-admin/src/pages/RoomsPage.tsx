
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, Pencil, RefreshCw } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import { apiGet, apiPatch } from "../services/api";

type Room = {
    id: number;
    title: string;
    address: string;
    ward: string | null;
    district: string | null;
    city: string;
    monthly_price: number;
    area_m2: number | null;
    capacity: number;
    status: "AVAILABLE" | "RENTED" | "INACTIVE";
};

type RoomResponse = {
    items: Room[];
    total: number;
    page: number;
    limit: number;
};

const formatPrice = (value: number) =>
    new Intl.NumberFormat("en-US").format(value) + " VND";

const statusLabel: Record<Room["status"], string> = {
    AVAILABLE: "Available",
    RENTED: "Rented",
    INACTIVE: "Inactive",
};

export default function RoomsPage() {
    const navigate = useNavigate();
    const [rooms, setRooms] = useState<Room[]>([]);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadRooms = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams({
                page: String(page),
                limit: "10",
            });

            if (search.trim()) params.set("keyword", search.trim());
            if (status) params.set("status", status);

            const result = await apiGet<RoomResponse>(
                `admin/rooms?${params.toString()}`,
            );

            setRooms(result.items);
            setTotal(result.total);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to load rooms.");
        } finally {
            setLoading(false);
        }
    }, [page, search, status]);

    useEffect(() => {
        void loadRooms();
    }, [loadRooms]);

    const changeStatus = async (room: Room) => {
        const nextStatus = room.status === "INACTIVE" ? "AVAILABLE" : "INACTIVE";

        if (!window.confirm(`Change "${room.title}" status to ${nextStatus}?`)) {
            return;
        }

        try {
            await apiPatch(`admin/rooms/${room.id}/status`, {
                status: nextStatus,
            });
            await loadRooms();
        } catch (err) {
            window.alert(err instanceof Error ? err.message : "Unable to update room status.");
        }
    };

    const totalPages = Math.max(1, Math.ceil(total / 10));

    return (
        <div className="page-container">
            <PageHeader
                title="Rooms"
                description="Manage rental rooms, room information and availability."
                actionLabel="Add Room"
                onAction={() => navigate("/rooms/new")}
            />

            <div className="toolbar rooms-toolbar">
                <form
                    className="rooms-search"
                    onSubmit={(event) => {
                        event.preventDefault();
                        setPage(1);
                        setSearch(keyword);
                    }}
                >
                    <Search size={18} aria-hidden="true" />
                    <input
                        value={keyword}
                        onChange={(event) => setKeyword(event.target.value)}
                        placeholder="Search by room name or address..."
                        aria-label="Search rooms"
                    />
                    <button type="submit" className="primary-button">Search</button>
                </form>

                <select
                    className="rooms-status-filter"
                    value={status}
                    onChange={(event) => {
                        setPage(1);
                        setStatus(event.target.value);
                    }}
                    aria-label="Filter by room status"
                >
                    <option value="">All statuses</option>
                    <option value="AVAILABLE">Available</option>
                    <option value="RENTED">Rented</option>
                    <option value="INACTIVE">Inactive</option>
                </select>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void loadRooms()}
                    disabled={loading}
                >
                    <RefreshCw size={16} /> Refresh
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="content-card">
                <div className="section-heading">
                    <div>
                        <h2>Room List</h2>
                        <p>{total} room records</p>
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>Room</th>
                                <th>Address</th>
                                <th>Monthly Price</th>
                                <th>Area</th>
                                <th>Capacity</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr><td colSpan={7}>Loading rooms...</td></tr>
                            ) : rooms.length === 0 ? (
                                <tr><td colSpan={7}>No rooms found.</td></tr>
                            ) : rooms.map((room) => (
                                <tr key={room.id}>
                                    <td>
                                        <strong>{room.title}</strong>
                                        <small className="room-id">ID: {room.id}</small>
                                    </td>
                                    <td>
                                        {[room.address, room.ward, room.district, room.city]
                                            .filter(Boolean).join(", ")}
                                    </td>
                                    <td>{formatPrice(Number(room.monthly_price))}</td>
                                    <td>{room.area_m2 ? `${room.area_m2} m²` : "—"}</td>
                                    <td>{room.capacity}</td>
                                    <td>
                                        <span className={`dashboard-status ${room.status.toLowerCase()}`}>
                                            {statusLabel[room.status]}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="room-actions">
                                            <button
                                                className="icon-button"
                                                title="View details"
                                                onClick={() => navigate(`/rooms/${room.id}`)}
                                            ><Eye size={17} /></button>
                                            <button
                                                className="icon-button"
                                                title="Edit room"
                                                onClick={() => navigate(`/rooms/${room.id}/edit`)}
                                            ><Pencil size={17} /></button>
                                            {room.status !== "RENTED" && (
                                                <button
                                                    className="secondary-button room-status-button"
                                                    onClick={() => void changeStatus(room)}
                                                >
                                                    {room.status === "INACTIVE" ? "Activate" : "Deactivate"}
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="rooms-pagination">
                    <span>Page {page} of {totalPages}</span>
                    <div>
                        <button
                            className="secondary-button"
                            disabled={page <= 1 || loading}
                            onClick={() => setPage((current) => current - 1)}
                        >Previous</button>
                        <button
                            className="secondary-button"
                            disabled={page >= totalPages || loading}
                            onClick={() => setPage((current) => current + 1)}
                        >Next</button>
                    </div>
                </div>
            </div>
        </div>
    );
}
