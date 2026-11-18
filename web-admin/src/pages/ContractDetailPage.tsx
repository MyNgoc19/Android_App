
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    FileText,
    UserRound,
    DoorOpen,
    CalendarDays,
    Ban,
    CheckCircle,
    Clock,
    CreditCard,
} from "lucide-react";
import { apiGet, apiPatch } from "../services/api";

type Payment = {
    id: number;
    amount: number;
    payment_type: string;
    payment_period: string | null;
    status: string;
    paid_at: string | null;
    note: string | null;
    created_at: string;
};

type ContractDetail = {
    id: number;
    request_id: number;
    room_id: number;
    tenant_id: number;
    start_date: string;
    end_date: string | null;
    monthly_price: number;
    deposit_amount: number;
    status: "ACTIVE" | "ENDED" | "CANCELLED";
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
    current_room_price: number;
    room_status: string;
    payments: Payment[];
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "No end date";

const formatPrice = (value: number) =>
    new Intl.NumberFormat("en-US").format(value) + " VND";

export default function ContractDetailPage() {
    const navigate = useNavigate();
    const { contractId } = useParams();

    const [contract, setContract] = useState<ContractDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadContract = useCallback(async () => {
        if (!contractId) return;

        setLoading(true);
        setError("");

        try {
            const result = await apiGet<ContractDetail>(
                `admin/contracts/${contractId}`,
            );
            setContract(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to load contract.");
        } finally {
            setLoading(false);
        }
    }, [contractId]);

    useEffect(() => {
        void loadContract();
    }, [loadContract]);

    const updateStatus = async (nextStatus: "ENDED" | "CANCELLED") => {
        if (!contract || contract.status !== "ACTIVE") return;

        const action = nextStatus === "ENDED" ? "end" : "cancel";

        if (!window.confirm(`Are you sure you want to ${action} this contract?`)) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(
                `admin/contracts/${contract.id}/status`,
                { status: nextStatus },
            );
            await loadContract();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to update contract.");
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading contract details...</div>;
    }

    if (error && !contract) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button className="secondary-button" onClick={() => navigate("/contracts")}>
                    <ArrowLeft size={17} /> Back to Contracts
                </button>
            </div>
        );
    }

    if (!contract) {
        return <div className="page-container">Contract not found.</div>;
    }

    const fullAddress = [
        contract.room_address,
        contract.room_ward,
        contract.room_district,
        contract.room_city,
    ].filter(Boolean).join(", ");

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Contract Details</h1>
                    <p>Contract #{contract.id}</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/contracts")}
                >
                    <ArrowLeft size={17} /> Back to Contracts
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="contract-detail-layout">
                <section className="content-card contract-detail-main">
                    <div className="contract-detail-heading">
                        <div className="contract-detail-icon"><FileText size={24} /></div>
                        <div>
                            <h2>Contract Information</h2>
                            <p>Contract ID: #{contract.id}</p>
                        </div>
                        <span className={`dashboard-status ${contract.status.toLowerCase()}`}>
                            {contract.status}
                        </span>
                    </div>

                    <div className="contract-info-grid">
                        <div className="contract-info-item">
                            <span>Start Date</span>
                            <strong>{formatDate(contract.start_date)}</strong>
                        </div>
                        <div className="contract-info-item">
                            <span>End Date</span>
                            <strong>{formatDate(contract.end_date)}</strong>
                        </div>
                        <div className="contract-info-item">
                            <span>Monthly Price</span>
                            <strong>{formatPrice(contract.monthly_price)}</strong>
                        </div>
                        <div className="contract-info-item">
                            <span>Deposit Amount</span>
                            <strong>{formatPrice(contract.deposit_amount)}</strong>
                        </div>
                        <div className="contract-info-item">
                            <span>Rental Request</span>
                            <strong>#{contract.request_id}</strong>
                        </div>
                        <div className="contract-info-item">
                            <span>Created At</span>
                            <strong>{new Date(contract.created_at).toLocaleDateString("en-GB")}</strong>
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3><UserRound size={18} /> Tenant Information</h3>
                        <div className="contract-live-info">
                            <div className="request-live-avatar">
                                {contract.tenant_avatar ? (
                                    <img src={contract.tenant_avatar} alt="Tenant avatar" />
                                ) : (
                                    <UserRound size={24} />
                                )}
                            </div>
                            <div>
                                <strong>{contract.tenant_name || "Unnamed User"}</strong>
                                <p>{contract.tenant_email}</p>
                                <p>{contract.tenant_phone || "Phone not provided"}</p>
                                <small>User ID: {contract.tenant_id}</small>
                            </div>
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3><DoorOpen size={18} /> Room Information</h3>
                        <div className="request-room-info">
                            <strong>{contract.room_title}</strong>
                            <p>{fullAddress}</p>
                            <p>Current room price: {formatPrice(contract.current_room_price)}</p>
                            <p>Room status: {contract.room_status}</p>
                            <button
                                type="button"
                                className="secondary-button"
                                onClick={() => navigate(`/rooms/${contract.room_id}`)}
                            >
                                View Room
                            </button>
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3><CalendarDays size={18} /> Contract Timeline</h3>
                        <div className="contract-timeline">
                            <div>
                                <span className="timeline-dot" />
                                <div>
                                    <strong>Contract Created</strong>
                                    <p>{new Date(contract.created_at).toLocaleString("en-GB")}</p>
                                </div>
                            </div>
                            <div>
                                <span className="timeline-dot" />
                                <div>
                                    <strong>Start Date</strong>
                                    <p>{formatDate(contract.start_date)}</p>
                                </div>
                            </div>
                            {contract.end_date && (
                                <div>
                                    <span className="timeline-dot" />
                                    <div>
                                        <strong>Scheduled End Date</strong>
                                        <p>{formatDate(contract.end_date)}</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3><CreditCard size={18} /> Payment History</h3>
                        {contract.payments.length === 0 ? (
                            <p className="contract-empty-payments">No payments recorded for this contract.</p>
                        ) : (
                            <div className="table-responsive">
                                <table className="dashboard-table">
                                    <thead>
                                        <tr>
                                            <th>Payment ID</th>
                                            <th>Type</th>
                                            <th>Period</th>
                                            <th>Amount</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {contract.payments.map((payment) => (
                                            <tr key={payment.id}>
                                                <td>#{payment.id}</td>
                                                <td>{payment.payment_type}</td>
                                                <td>{payment.payment_period || "—"}</td>
                                                <td>{formatPrice(payment.amount)}</td>
                                                <td>
                                                    <span className={`dashboard-status ${payment.status.toLowerCase()}`}>
                                                        {payment.status}
                                                    </span>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </section>

                <section className="content-card contract-actions-card">
                    <div className="form-section-heading">
                        <h2>Contract Actions</h2>
                        <p>Manage the lifecycle of this contract.</p>
                    </div>

                    <div className="contract-status-preview">
                        <strong>{contract.status}</strong>
                        <span>
                            {contract.status === "ACTIVE"
                                ? "This contract is currently active."
                                : `This contract has been ${contract.status.toLowerCase()}.`}
                        </span>
                    </div>

                    {contract.status === "ACTIVE" ? (
                        <div className="contract-action-buttons">
                            <button
                                className="success-button"
                                disabled={updating}
                                onClick={() => void updateStatus("ENDED")}
                            >
                                <CheckCircle size={17} /> End Contract
                            </button>
                            <button
                                className="danger-button"
                                disabled={updating}
                                onClick={() => void updateStatus("CANCELLED")}
                            >
                                <Ban size={17} /> Cancel Contract
                            </button>
                        </div>
                    ) : (
                        <p className="contract-action-note">
                            This contract has already been processed. No further status changes are available.
                        </p>
                    )}

                    {updating && <p><Clock size={14} /> Updating contract...</p>}

                    <button
                        className="primary-button"
                        style={{ width: "100%", marginTop: 16 }}
                        onClick={() => navigate(`/payments/new?contractId=${contract.id}`)}
                        disabled={contract.status !== "ACTIVE"}
                    >
                        <CreditCard size={17} /> Record Payment
                    </button>
                </section>
            </div>
        </div>
    );
}
