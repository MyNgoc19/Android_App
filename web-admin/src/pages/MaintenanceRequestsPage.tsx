import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, RefreshCw } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";
import "./requests.css";

type MaintenanceStatus = "PENDING" | "IN_PROGRESS" | "DONE" | "REJECTED" | "CANCELLED";
type MaintenancePriority = "LOW" | "MEDIUM" | "HIGH";

type MaintenanceRequest = {
    id: number;
    contract_id: number;
    user_id: number;
    title: string;
    priority: MaintenancePriority;
    status: MaintenanceStatus;
    created_at: string;
    room_id: number;
    room_title: string;
    room_address: string;
    tenant_name: string | null;
    tenant_email: string;
};

type MaintenanceResponse = {
    items: MaintenanceRequest[];
    total: number;
    page: number;
    limit: number;
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "—";

export const PRIORITY_LABELS: Record<MaintenancePriority, string> = {
    HIGH: "High",
    MEDIUM: "Medium",
    LOW: "Low",
};

export const MAINTENANCE_STATUS_LABELS: Record<MaintenanceStatus, string> = {
    PENDING: "Pending",
    IN_PROGRESS: "In Progress",
    DONE: "Done",
    REJECTED: "Rejected",
    CANCELLED: "Cancelled",
};

export default function MaintenanceRequestsPage() {
    const navigate = useNavigate();

    const [requests, setRequests] = useState<MaintenanceRequest[]>([]);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [priority, setPriority] = useState("");
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
            if (priority) params.set("priority", priority);

            const result = await apiGet<MaintenanceResponse>(
                `admin/maintenance-requests?${params.toString()}`,
            );

            setRequests(result.items);
            setTotal(result.total);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load maintenance requests.",
            );
        } finally {
            setLoading(false);
        }
    }, [page, search, status, priority]);

    useEffect(() => {
        void loadRequests();
    }, [loadRequests]);

    const totalPages = Math.max(1, Math.ceil(total / 10));

    return (
        <div className="page-container">
            <PageHeader
                title="Maintenance Requests"
                description="Track and resolve repair requests sent by tenants."
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
                        placeholder="Search by issue, tenant, email or room..."
                        aria-label="Search maintenance requests"
                    />
                    <button type="submit" className="primary-button">
                        Search
                    </button>
                </form>

                <div className="requests-filters">
                    <select
                        className="rooms-status-filter"
                        value={status}
                        onChange={(event) => {
                            setPage(1);
                            setStatus(event.target.value);
                        }}
                        aria-label="Filter by status"
                    >
                        <option value="">All statuses</option>
                        <option value="PENDING">Pending</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="DONE">Done</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>

                    <select
                        className="rooms-status-filter"
                        value={priority}
                        onChange={(event) => {
                            setPage(1);
                            setPriority(event.target.value);
                        }}
                        aria-label="Filter by priority"
                    >
                        <option value="">All priorities</option>
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                    </select>
                </div>

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
                        <h2>Maintenance Request List</h2>
                        <p>{total} maintenance requests · pending and high priority first</p>
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Issue</th>
                                <th>Tenant</th>
                                <th>Room</th>
                                <th>Priority</th>
                                <th>Submitted At</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan={8}>Loading maintenance requests...</td>
                                </tr>
                            ) : requests.length === 0 ? (
                                <tr>
                                    <td colSpan={8}>No maintenance requests found.</td>
                                </tr>
                            ) : requests.map((request) => (
                                <tr key={request.id}>
                                    <td>#{request.id}</td>
                                    <td>
                                        <strong>{request.title}</strong>
                                    </td>
                                    <td>
                                        <strong>{request.tenant_name || "Unnamed User"}</strong>
                                        <small className="room-id">{request.tenant_email}</small>
                                    </td>
                                    <td>
                                        <strong>{request.room_title}</strong>
                                        <small className="room-id">{request.room_address}</small>
                                    </td>
                                    <td>
                                        <span className={`priority-badge priority-${request.priority.toLowerCase()}`}>
                                            {PRIORITY_LABELS[request.priority]}
                                        </span>
                                    </td>
                                    <td>{formatDate(request.created_at)}</td>
                                    <td>
                                        <span className={`dashboard-status status-${request.status.toLowerCase()}`}>
                                            {MAINTENANCE_STATUS_LABELS[request.status]}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            type="button"
                                            className="icon-button"
                                            title="View maintenance request"
                                            onClick={() => navigate(`/maintenance-requests/${request.id}`)}
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
