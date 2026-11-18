
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, RefreshCw } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";

type RentalRequest = {
    id: number;
    room_id: number;
    user_id: number;
    desired_move_in: string | null;
    status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
    created_at: string;
    tenant_name: string | null;
    tenant_email: string;
    room_title: string;
    room_address: string;
};

type RequestResponse = {
    items: RentalRequest[];
    total: number;
    page: number;
    limit: number;
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "—";

export default function RentalRequestsPage() {
    const navigate = useNavigate();

    const [requests, setRequests] = useState<RentalRequest[]>([]);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadRequests = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams({
                page: String(page),
                limit: "10",
            });

            if (search.trim()) params.set("keyword", search.trim());
            if (status) params.set("status", status);

            const result = await apiGet<RequestResponse>(
                `admin/rental-requests?${params.toString()}`,
            );

            setRequests(result.items);
            setTotal(result.total);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load rental requests.",
            );
        } finally {
            setLoading(false);
        }
    }, [page, search, status]);

    useEffect(() => {
        void loadRequests();
    }, [loadRequests]);

    const totalPages = Math.max(1, Math.ceil(total / 10));

    return (
        <div className="page-container">
            <PageHeader
                title="Rental Requests"
                description="Review and manage room rental requests."
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
                    <Search size={18} />
                    <input
                        value={keyword}
                        onChange={(event) => setKeyword(event.target.value)}
                        placeholder="Search by tenant, email or room..."
                        aria-label="Search rental requests"
                    />
                    <button type="submit" className="primary-button">
                        Search
                    </button>
                </form>

                <select
                    className="rooms-status-filter"
                    value={status}
                    onChange={(event) => {
                        setPage(1);
                        setStatus(event.target.value);
                    }}
                    aria-label="Filter by request status"
                >
                    <option value="">All statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void loadRequests()}
                    disabled={loading}
                >
                    <RefreshCw size={16} /> Refresh
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="content-card">
                <div className="section-heading">
                    <div>
                        <h2>Rental Request List</h2>
                        <p>{total} rental requests</p>
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>Request ID</th>
                                <th>Tenant</th>
                                <th>Room</th>
                                <th>Desired Move-in</th>
                                <th>Submitted At</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan={7}>Loading rental requests...</td>
                                </tr>
                            ) : requests.length === 0 ? (
                                <tr>
                                    <td colSpan={7}>No rental requests found.</td>
                                </tr>
                            ) : requests.map((request) => (
                                <tr key={request.id}>
                                    <td>#{request.id}</td>
                                    <td>
                                        <strong>{request.tenant_name || "Unnamed User"}</strong>
                                        <small className="room-id">{request.tenant_email}</small>
                                    </td>
                                    <td>
                                        <strong>{request.room_title}</strong>
                                        <small className="room-id">{request.room_address}</small>
                                    </td>
                                    <td>{formatDate(request.desired_move_in)}</td>
                                    <td>{formatDate(request.created_at)}</td>
                                    <td>
                                        <span className={`dashboard-status ${request.status.toLowerCase()}`}>
                                            {request.status}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            type="button"
                                            className="icon-button"
                                            title="View request details"
                                            onClick={() => navigate(`/rental-requests/${request.id}`)}
                                        >
                                            <Eye size={17} />
                                        </button>
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
                        >
                            Previous
                        </button>
                        <button
                            className="secondary-button"
                            disabled={page >= totalPages || loading}
                            onClick={() => setPage((current) => current + 1)}
                        >
                            Next
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
