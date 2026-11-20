
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    ClipboardList,
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

type RentalRequestDetail = {
    id: number;
    room_id: number;
    user_id: number;
    message: string | null;
    desired_move_in: string | null;
    status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";
    created_at: string;
    updated_at: string;
    tenant_name: string | null;
    tenant_email: string;
    tenant_phone: string | null;
    tenant_avatar: string | null;
    room_title: string;
    room_description: string | null;
    room_address: string;
    room_ward: string | null;
    room_district: string | null;
    room_city: string;
    room_monthly_price: number;
    room_area_m2: number | null;
    room_capacity: number;
    room_status: string;
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "Not specified";

const formatPrice = (value: number) =>
    new Intl.NumberFormat("en-US").format(value) + " VND/month";

export default function RentalRequestDetailPage() {
    const navigate = useNavigate();
    const { requestId } = useParams();

    const [request, setRequest] = useState<RentalRequestDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadRequest = useCallback(async () => {
        if (!requestId) return;

        setLoading(true);
        setError("");

        try {
            const result = await apiGet<RentalRequestDetail>(
                `admin/rental-requests/${requestId}`,
            );
            setRequest(result);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to load rental request.",
            );
        } finally {
            setLoading(false);
        }
    }, [requestId]);

    useEffect(() => {
        void loadRequest();
    }, [loadRequest]);

    const updateStatus = async (nextStatus: "APPROVED" | "REJECTED") => {
        if (!request || request.status !== "PENDING") return;

        const action = nextStatus === "APPROVED" ? "approve" : "reject";

        if (!window.confirm(`Are you sure you want to ${action} this request?`)) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(
                `admin/rental-requests/${request.id}/status`,
                { status: nextStatus },
            );

            await loadRequest();
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to update rental request.",
            );
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading request details...</div>;
    }

    if (error && !request) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button
                    className="secondary-button"
                    onClick={() => navigate("/rental-requests")}
                >
                    <ArrowLeft size={17} /> Back to Requests
                </button>
            </div>
        );
    }

    if (!request) {
        return <div className="page-container">Rental request not found.</div>;
    }

    const fullAddress = [
        request.room_address,
        request.room_ward,
        request.room_district,
        request.room_city,
    ].filter(Boolean).join(", ");

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Rental Request Details</h1>
                    <p>Review the details of rental request #{request.id}.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/rental-requests")}
                >
                    <ArrowLeft size={17} /> Back to Requests
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="request-detail-layout">
                <section className="content-card request-detail-main">
                    <div className="request-detail-heading">
                        <div className="request-detail-icon">
                            <ClipboardList size={24} />
                        </div>

                        <div>
                            <h2>Request Information</h2>
                            <p>Request ID: #{request.id}</p>
                        </div>

                        <span className={`dashboard-status ${request.status.toLowerCase()}`}>
                            {request.status}
                        </span>
                    </div>

                    <div className="request-info-grid">
                        <div className="request-info-item">
                            <span>Request ID</span>
                            <strong>#{request.id}</strong>
                        </div>

                        <div className="request-info-item">
                            <span><CalendarDays size={15} /> Desired Move-in Date</span>
                            <strong>{formatDate(request.desired_move_in)}</strong>
                        </div>

                        <div className="request-info-item">
                            <span><Clock size={15} /> Submitted At</span>
                            <strong>{new Date(request.created_at).toLocaleString("en-GB")}</strong>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3><UserRound size={18} /> Tenant Information</h3>

                        <div className="request-live-info">
                            <div className="request-live-avatar">
                                {request.tenant_avatar ? (
                                    <img src={request.tenant_avatar} alt="Tenant avatar" />
                                ) : (
                                    <UserRound size={24} />
                                )}
                            </div>
                            <div>
                                <strong>{request.tenant_name || "Unnamed User"}</strong>
                                <p>{request.tenant_email}</p>
                                <p>
                                    <Phone size={14} />
                                    {request.tenant_phone || "Phone not provided"}
                                </p>
                                <small>User ID: {request.user_id}</small>
                            </div>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3><DoorOpen size={18} /> Room Information</h3>

                        <div className="request-room-info">
                            <strong>{request.room_title}</strong>
                            <p><MapPin size={15} /> {fullAddress}</p>
                            <p>{formatPrice(request.room_monthly_price)}</p>
                            <p>
                                Area: {request.room_area_m2 ?? "Not specified"} m²
                                {" · "}
                                Capacity: {request.room_capacity}
                            </p>
                            <p>
                                Room status: <strong>{request.room_status}</strong>
                            </p>
                            {request.room_description && (
                                <p>{request.room_description}</p>
                            )}
                            <button
                                type="button"
                                className="secondary-button"
                                onClick={() => navigate(`/rooms/${request.room_id}`)}
                            >
                                View Room
                            </button>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3>Tenant Message</h3>
                        <div className="request-message">
                            {request.message || "No message provided."}
                        </div>
                    </div>
                </section>

                <section className="content-card request-review-card">
                    <div className="form-section-heading">
                        <h2>Review Request</h2>
                        <p>Review the request before making a decision.</p>
                    </div>

                    <div className="request-review-status">
                        {request.status === "PENDING" ? (
                            <Clock size={20} />
                        ) : request.status === "APPROVED" ? (
                            <CheckCircle size={20} />
                        ) : (
                            <XCircle size={20} />
                        )}

                        <div>
                            <strong>{request.status}</strong>
                            <span>
                                {request.status === "PENDING"
                                    ? "This request is waiting for administrator review."
                                    : `This request has been ${request.status.toLowerCase()}.`}
                            </span>
                        </div>
                    </div>

                    {request.status === "PENDING" ? (
                        <div className="request-review-actions">
                            <button
                                type="button"
                                className="success-button"
                                disabled={updating}
                                aria-busy={updating}
                                onClick={() => void updateStatus("APPROVED")}
                            >
                                <CheckCircle size={17} />
                                {updating ? "Processing..." : "Approve Request"}
                            </button>

                            <button
                                type="button"
                                className="danger-button"
                                disabled={updating}
                                aria-busy={updating}
                                onClick={() => void updateStatus("REJECTED")}
                            >
                                <XCircle size={17} />
                                {updating ? "Processing..." : "Reject Request"}
                            </button>
                        </div>
                    ) : (
                        <p className="request-review-note">
                            This request has already been processed. Its status cannot
                            be changed from this page.
                        </p>
                    )}

                    {updating && <p>Updating request status...</p>}
                </section>
            </div>
        </div>
    );
}
