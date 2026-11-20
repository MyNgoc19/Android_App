import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    Wrench,
    UserRound,
    DoorOpen,
    Clock,
    CheckCircle,
    XCircle,
    PlayCircle,
    MapPin,
    Phone,
} from "lucide-react";
import { apiGet, apiPatch } from "../services/api";
import { MAINTENANCE_STATUS_LABELS, PRIORITY_LABELS } from "./MaintenanceRequestsPage";
import "./requests.css";

type MaintenanceStatus = "PENDING" | "IN_PROGRESS" | "DONE" | "REJECTED" | "CANCELLED";
type NextStatus = "IN_PROGRESS" | "DONE" | "REJECTED";

type MaintenanceDetail = {
    id: number;
    contract_id: number;
    user_id: number;
    title: string;
    description: string;
    priority: "LOW" | "MEDIUM" | "HIGH";
    image_url: string | null;
    status: MaintenanceStatus;
    admin_note: string | null;
    resolved_at: string | null;
    created_at: string;
    updated_at: string;
    room_id: number;
    room_title: string;
    room_address: string;
    tenant_name: string | null;
    tenant_email: string;
    tenant_phone: string | null;
};

const formatDateTime = (value: string | null) =>
    value ? new Date(value).toLocaleString("en-GB") : "—";


const NEXT_STATUSES: Record<MaintenanceStatus, NextStatus[]> = {
    PENDING: ["IN_PROGRESS", "DONE", "REJECTED"],
    IN_PROGRESS: ["DONE", "REJECTED"],
    DONE: [],
    REJECTED: [],
    CANCELLED: [],
};

export default function MaintenanceRequestDetailPage() {
    const navigate = useNavigate();
    const { requestId } = useParams();

    const [request, setRequest] = useState<MaintenanceDetail | null>(null);
    const [adminNote, setAdminNote] = useState("");
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadRequest = useCallback(async () => {
        if (!requestId) return;

        setLoading(true);
        setError("");

        try {
            const result = await apiGet<MaintenanceDetail>(
                `admin/maintenance-requests/${requestId}`,
            );
            setRequest(result);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load maintenance request.",
            );
        } finally {
            setLoading(false);
        }
    }, [requestId]);

    useEffect(() => {
        void loadRequest();
    }, [loadRequest]);

    const updateStatus = async (nextStatus: NextStatus) => {
        if (!request) return;

        const labels: Record<NextStatus, string> = {
            IN_PROGRESS: "start working on",
            DONE: "mark as done",
            REJECTED: "reject",
        };

        if (!window.confirm(`Are you sure you want to ${labels[nextStatus]} this request?`)) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(
                `admin/maintenance-requests/${request.id}/status`,
                {
                    status: nextStatus,
                    admin_note: adminNote.trim() || null,
                },
            );
            setAdminNote("");
            await loadRequest();
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to update maintenance request.",
            );
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading maintenance request...</div>;
    }

    if (error && !request) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button
                    className="secondary-button"
                    onClick={() => navigate("/maintenance-requests")}
                >
                    <ArrowLeft size={17} /> Back to Maintenance
                </button>
            </div>
        );
    }

    if (!request) {
        return <div className="page-container">Maintenance request not found.</div>;
    }

    const nextStatuses = NEXT_STATUSES[request.status];

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Maintenance Request Details</h1>
                    <p>Review and resolve maintenance request #{request.id}.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/maintenance-requests")}
                >
                    <ArrowLeft size={17} /> Back to Maintenance
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="request-detail-layout">
                <section className="content-card request-detail-main">
                    <div className="request-detail-heading">
                        <div className="request-detail-icon">
                            <Wrench size={24} />
                        </div>

                        <div>
                            <h2>{request.title}</h2>
                            <p>Request ID: #{request.id} · Contract #{request.contract_id}</p>
                        </div>

                        <span className={`dashboard-status status-${request.status.toLowerCase()}`}>
                            {MAINTENANCE_STATUS_LABELS[request.status]}
                        </span>
                    </div>

                    <div className="request-info-grid">
                        <div className="request-info-item">
                            <span>Priority</span>
                            <strong>
                                <span className={`priority-badge priority-${request.priority.toLowerCase()}`}>
                                    {PRIORITY_LABELS[request.priority]}
                                </span>
                            </strong>
                        </div>

                        <div className="request-info-item">
                            <span><Clock size={15} /> Submitted At</span>
                            <strong>{formatDateTime(request.created_at)}</strong>
                        </div>

                        <div className="request-info-item">
                            <span><CheckCircle size={15} /> Resolved At</span>
                            <strong>{formatDateTime(request.resolved_at)}</strong>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3>Problem Description</h3>
                        <div className="request-message">{request.description}</div>
                        {request.image_url && (
                            <img
                                className="request-image-preview"
                                src={request.image_url}
                                alt="Reported issue"
                            />
                        )}
                    </div>

                    <div className="request-related-section">
                        <h3><UserRound size={18} /> Tenant Information</h3>
                        <div className="request-room-info">
                            <strong>{request.tenant_name || "Unnamed User"}</strong>
                            <p>{request.tenant_email}</p>
                            <p><Phone size={14} /> {request.tenant_phone || "Phone not provided"}</p>
                            <button
                                type="button"
                                className="secondary-button"
                                onClick={() => navigate(`/users/${request.user_id}`)}
                            >
                                View Tenant
                            </button>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3><DoorOpen size={18} /> Room</h3>
                        <div className="request-room-info">
                            <strong>{request.room_title}</strong>
                            <p><MapPin size={15} /> {request.room_address}</p>
                            <div className="detail-header-actions">
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() => navigate(`/rooms/${request.room_id}`)}
                                >
                                    View Room
                                </button>
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() => navigate(`/contracts/${request.contract_id}`)}
                                >
                                    View Contract
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="content-card request-review-card">
                    <div className="form-section-heading">
                        <h2>Handle Request</h2>
                        <p>Update progress so the tenant can follow it in the app.</p>
                    </div>

                    <div className="request-review-status">
                        {request.status === "DONE" ? (
                            <CheckCircle size={20} />
                        ) : request.status === "REJECTED" || request.status === "CANCELLED" ? (
                            <XCircle size={20} />
                        ) : (
                            <Clock size={20} />
                        )}
                        <div>
                            <strong>{MAINTENANCE_STATUS_LABELS[request.status]}</strong>
                            <span>
                                {request.status === "PENDING"
                                    ? "Waiting for administrator review."
                                    : request.status === "IN_PROGRESS"
                                        ? "Repair is in progress."
                                        : request.status === "CANCELLED"
                                            ? "The tenant cancelled this request."
                                            : `This request is ${MAINTENANCE_STATUS_LABELS[request.status].toLowerCase()}.`}
                            </span>
                        </div>
                    </div>

                    {request.admin_note && (
                        <div className="admin-note-current">
                            <strong>Current note to tenant:</strong>
                            <br />
                            {request.admin_note}
                        </div>
                    )}

                    {nextStatuses.length > 0 ? (
                        <>
                            <div className="admin-note-field">
                                <label htmlFor="admin-note">Note to tenant (optional)</label>
                                <textarea
                                    id="admin-note"
                                    value={adminNote}
                                    onChange={(event) => setAdminNote(event.target.value)}
                                    placeholder="e.g. A technician will come tomorrow afternoon."
                                    maxLength={2000}
                                    disabled={updating}
                                />
                            </div>

                            <div className="request-review-actions">
                                {nextStatuses.includes("IN_PROGRESS") && (
                                    <button
                                        className="primary-button"
                                        disabled={updating}
                                        onClick={() => void updateStatus("IN_PROGRESS")}
                                    >
                                        <PlayCircle size={17} />
                                        Start Repair
                                    </button>
                                )}

                                {nextStatuses.includes("DONE") && (
                                    <button
                                        className="success-button"
                                        disabled={updating}
                                        onClick={() => void updateStatus("DONE")}
                                    >
                                        <CheckCircle size={17} />
                                        Mark as Done
                                    </button>
                                )}

                                {nextStatuses.includes("REJECTED") && (
                                    <button
                                        className="danger-button"
                                        disabled={updating}
                                        onClick={() => void updateStatus("REJECTED")}
                                    >
                                        <XCircle size={17} />
                                        Reject
                                    </button>
                                )}
                            </div>
                        </>
                    ) : (
                        <p className="request-review-note">
                            This request has been closed. Its status can no longer be changed.
                        </p>
                    )}

                    {updating && <p>Updating request status...</p>}
                </section>
            </div>
        </div>
    );
}
