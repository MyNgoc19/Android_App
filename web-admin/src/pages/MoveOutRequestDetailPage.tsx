import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    LogOut,
    UserRound,
    DoorOpen,
    CalendarDays,
    Clock,
    CheckCircle,
    XCircle,
    MapPin,
    Phone,
} from "lucide-react";
import { apiGet, apiPatch } from "../services/api";
import { MOVE_OUT_STATUS_LABELS } from "./MoveOutRequestsPage";
import "./requests.css";

type MoveOutStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED" | "COMPLETED";
type NextStatus = "APPROVED" | "REJECTED" | "COMPLETED";

type MoveOutDetail = {
    id: number;
    contract_id: number;
    user_id: number;
    move_out_date: string;
    reason: string | null;
    status: MoveOutStatus;
    admin_note: string | null;
    created_at: string;
    updated_at: string;
    room_id: number;
    contract_start_date: string;
    contract_end_date: string | null;
    contract_status: string;
    deposit_amount: number;
    room_title: string;
    room_address: string;
    tenant_name: string | null;
    tenant_email: string;
    tenant_phone: string | null;
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "—";

const formatMoney = (value: number) =>
    new Intl.NumberFormat("en-US").format(value) + " VND";

// Trạng thái được phép chuyển (khớp backend routers/move_out.py)
const NEXT_STATUSES: Record<MoveOutStatus, NextStatus[]> = {
    PENDING: ["APPROVED", "REJECTED"],
    APPROVED: ["COMPLETED", "REJECTED"],
    REJECTED: [],
    CANCELLED: [],
    COMPLETED: [],
};

export default function MoveOutRequestDetailPage() {
    const navigate = useNavigate();
    const { requestId } = useParams();

    const [request, setRequest] = useState<MoveOutDetail | null>(null);
    const [adminNote, setAdminNote] = useState("");
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadRequest = useCallback(async () => {
        if (!requestId) return;

        setLoading(true);
        setError("");

        try {
            const result = await apiGet<MoveOutDetail>(
                `admin/move-out-requests/${requestId}`,
            );
            setRequest(result);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load move-out request.",
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

        const messages: Record<NextStatus, string> = {
            APPROVED: "Approve this move-out request?",
            REJECTED: "Reject this move-out request?",
            COMPLETED:
                "Complete this move-out?\n\nThe contract will be ENDED and the room "
                + "will become AVAILABLE again. This cannot be undone.",
        };

        if (!window.confirm(messages[nextStatus])) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(
                `admin/move-out-requests/${request.id}/status`,
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
                    : "Unable to update move-out request.",
            );
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading move-out request...</div>;
    }

    if (error && !request) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button
                    className="secondary-button"
                    onClick={() => navigate("/move-out-requests")}
                >
                    <ArrowLeft size={17} /> Back to Move-out Requests
                </button>
            </div>
        );
    }

    if (!request) {
        return <div className="page-container">Move-out request not found.</div>;
    }

    const nextStatuses = NEXT_STATUSES[request.status];

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Move-out Request Details</h1>
                    <p>Review move-out request #{request.id}.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/move-out-requests")}
                >
                    <ArrowLeft size={17} /> Back to Move-out Requests
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="request-detail-layout">
                <section className="content-card request-detail-main">
                    <div className="request-detail-heading">
                        <div className="request-detail-icon">
                            <LogOut size={24} />
                        </div>

                        <div>
                            <h2>Move-out Information</h2>
                            <p>Request ID: #{request.id} · Contract #{request.contract_id}</p>
                        </div>

                        <span className={`dashboard-status status-${request.status.toLowerCase()}`}>
                            {MOVE_OUT_STATUS_LABELS[request.status]}
                        </span>
                    </div>

                    <div className="request-info-grid">
                        <div className="request-info-item">
                            <span><CalendarDays size={15} /> Move-out Date</span>
                            <strong>{formatDate(request.move_out_date)}</strong>
                        </div>

                        <div className="request-info-item">
                            <span><Clock size={15} /> Submitted At</span>
                            <strong>{new Date(request.created_at).toLocaleString("en-GB")}</strong>
                        </div>

                        <div className="request-info-item">
                            <span>Deposit to settle</span>
                            <strong>{formatMoney(request.deposit_amount)}</strong>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3>Reason</h3>
                        <div className="request-message">
                            {request.reason || "No reason provided."}
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3><UserRound size={18} /> Tenant Information</h3>
                        <div className="request-room-info">
                            <strong>{request.tenant_name || "Unnamed User"}</strong>
                            <p>{request.tenant_email}</p>
                            <p><Phone size={14} /> {request.tenant_phone || "Phone not provided"}</p>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3><DoorOpen size={18} /> Room & Contract</h3>
                        <div className="request-room-info">
                            <strong>{request.room_title}</strong>
                            <p><MapPin size={15} /> {request.room_address}</p>
                            <p>
                                Contract: {formatDate(request.contract_start_date)}
                                {" → "}
                                {formatDate(request.contract_end_date)}
                                {" · "}
                                <strong>{request.contract_status}</strong>
                            </p>
                            <div className="detail-header-actions">
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() => navigate(`/contracts/${request.contract_id}`)}
                                >
                                    View Contract
                                </button>
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() => navigate(`/rooms/${request.room_id}`)}
                                >
                                    View Room
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="content-card request-review-card">
                    <div className="form-section-heading">
                        <h2>Review Move-out</h2>
                        <p>Approve first, then complete after checking the room.</p>
                    </div>

                    <div className="request-review-status">
                        {request.status === "COMPLETED" || request.status === "APPROVED" ? (
                            <CheckCircle size={20} />
                        ) : request.status === "PENDING" ? (
                            <Clock size={20} />
                        ) : (
                            <XCircle size={20} />
                        )}
                        <div>
                            <strong>{MOVE_OUT_STATUS_LABELS[request.status]}</strong>
                            <span>
                                {request.status === "PENDING"
                                    ? "Waiting for administrator review."
                                    : request.status === "APPROVED"
                                        ? "Approved. Complete it after the room inspection."
                                        : request.status === "COMPLETED"
                                            ? "Contract ended and the room is available again."
                                            : `This request is ${MOVE_OUT_STATUS_LABELS[request.status].toLowerCase()}.`}
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
                                    placeholder="e.g. Room inspection at 9:00 on the move-out day."
                                    maxLength={2000}
                                    disabled={updating}
                                />
                            </div>

                            <div className="request-review-actions">
                                {nextStatuses.includes("APPROVED") && (
                                    <button
                                        className="success-button"
                                        disabled={updating}
                                        onClick={() => void updateStatus("APPROVED")}
                                    >
                                        <CheckCircle size={17} />
                                        Approve
                                    </button>
                                )}

                                {nextStatuses.includes("COMPLETED") && (
                                    <button
                                        className="success-button"
                                        disabled={updating}
                                        onClick={() => void updateStatus("COMPLETED")}
                                    >
                                        <CheckCircle size={17} />
                                        Complete Move-out
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

                            {nextStatuses.includes("COMPLETED") && (
                                <p className="warning-note">
                                    Completing will end contract #{request.contract_id} and make
                                    the room available for new tenants.
                                </p>
                            )}
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
