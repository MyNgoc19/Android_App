import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    CreditCard,
    FileText,
    CalendarDays,
    CheckCircle,
    XCircle,
    RotateCcw,
    StickyNote,
    RefreshCw,
} from "lucide-react";

import { apiGet, apiPatch } from "../services/api";

interface Payment {
    id: number;
    contract_id: number;
    amount: number | string;
    payment_type: "DEPOSIT" | "RENT" | "OTHER";
    payment_period: string | null;
    status: "PENDING" | "PAID" | "FAILED" | "REFUNDED";
    paid_at: string | null;
    note: string | null;
    created_at: string;
    tenant_id: number;
    room_id: number;
    tenant_name: string;
    tenant_email: string;
    room_title: string;
}

interface PaymentResponse {
    items: Payment[];
    total: number;
    page: number;
    limit: number;
}

interface ContractDetail {
    id: number;
    request_id: number;
    room_id: number;
    tenant_id: number;
    start_date: string;
    end_date: string | null;
    monthly_price: number | string;
    deposit_amount: number | string;
    status: string;
    tenant_name: string;
    tenant_email: string;
    tenant_phone: string | null;
    room_title: string;
    room_address: string;
}

const formatCurrency = (value: number | string) =>
    `${Number(value).toLocaleString("vi-VN")} VND`;

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleString("vi-VN") : "—";

const statusLabel: Record<Payment["status"], string> = {
    PENDING: "Pending",
    PAID: "Paid",
    FAILED: "Failed",
    REFUNDED: "Refunded",
};

export default function PaymentDetailPage() {
    const navigate = useNavigate();
    const { paymentId } = useParams();

    const [payment, setPayment] = useState<Payment | null>(null);
    const [contract, setContract] = useState<ContractDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadPayment = async () => {
        if (!paymentId || !/^\d+$/.test(paymentId)) {
            setError("Invalid payment ID.");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError("");

        try {
            let foundPayment: Payment | undefined;
            let currentPage = 1;
            let total = 0;
            const limit = 100;

            do {
                const result = await apiGet<PaymentResponse>(
                    `admin/payments?page=${currentPage}&limit=${limit}`,
                );

                total = result.total ?? 0;
                foundPayment = result.items?.find(
                    (item) => item.id === Number(paymentId),
                );

                if (foundPayment) break;
                currentPage += 1;
            } while ((currentPage - 1) * limit < total);

            if (!foundPayment) {
                throw new Error("Payment not found.");
            }

            setPayment(foundPayment);

            const contractResult = await apiGet<ContractDetail>(
                `admin/contracts/${foundPayment.contract_id}`,
            );

            setContract(contractResult);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to load payment details.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadPayment();
    }, [paymentId]);

    const updateStatus = async (nextStatus: "PAID" | "FAILED" | "REFUNDED") => {
        if (!payment) return;

        const confirmation = window.confirm(
            `Are you sure you want to change this payment status to ${nextStatus}?`,
        );

        if (!confirmation) return;

        setUpdating(true);
        setError("");

        try {
            await apiPatch(
                `admin/payments/${payment.id}/status`,
                { status: nextStatus },
            );
            await loadPayment();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to update payment status.");
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return (
            <div className="page-container">
                <div className="table-empty">Loading payment details...</div>
            </div>
        );
    }

    if (error && !payment) {
        return (
            <div className="page-container">
                <div className="page-header">
                    <div>
                        <h1>Payment Details</h1>
                        <p>View payment information and manage its status.</p>
                    </div>
                    <button
                        className="secondary-button"
                        onClick={() => navigate("/payments")}
                    >
                        <ArrowLeft size={17} />
                        Back to Payments
                    </button>
                </div>
                <div className="form-error">{error}</div>
            </div>
        );
    }

    if (!payment) return null;

    const canMarkPaidOrFailed = payment.status === "PENDING";
    const canRefund = payment.status === "PAID";

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Payment #{payment.id}</h1>
                    <p>View payment information and manage its status.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/payments")}
                >
                    <ArrowLeft size={17} />
                    Back to Payments
                </button>
            </div>

            {error && <div className="form-error">{error}</div>}

            <div className="payment-detail-layout">
                <section className="content-card payment-detail-main">
                    <div className="payment-detail-heading">
                        <div className="payment-detail-icon">
                            <CreditCard size={24} />
                        </div>

                        <div>
                            <h2>Payment Information</h2>
                            <p>Payment record #{payment.id}</p>
                        </div>

                        <span className={`status-badge status-${payment.status.toLowerCase()}`}>
                            {statusLabel[payment.status]}
                        </span>
                    </div>

                    <div className="payment-amount-preview">
                        <span>Payment Amount</span>
                        <strong>{formatCurrency(payment.amount)}</strong>
                    </div>

                    <div className="payment-info-grid">
                        <div className="payment-info-item">
                            <span>Payment ID</span>
                            <strong>#{payment.id}</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Payment Type</span>
                            <strong>{payment.payment_type}</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Payment Period</span>
                            <strong>{payment.payment_period || "—"}</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Status</span>
                            <strong>{statusLabel[payment.status]}</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Created At</span>
                            <strong>{formatDate(payment.created_at)}</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Paid At</span>
                            <strong>{formatDate(payment.paid_at)}</strong>
                        </div>
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <FileText size={18} />
                            Related Contract
                        </h3>

                        {contract ? (
                            <div className="payment-related-placeholder">
                                <p><strong>Contract #{contract.id}</strong></p>
                                <p>{contract.room_title}</p>
                                <p>{contract.room_address}</p>
                                <p>Tenant: {contract.tenant_name}</p>
                                <p>Email: {contract.tenant_email}</p>
                                <p>Phone: {contract.tenant_phone || "—"}</p>
                                <p>Contract status: {contract.status}</p>
                                <p>Start date: {contract.start_date}</p>
                                <p>End date: {contract.end_date || "Not specified"}</p>
                                <p>Monthly rent: {formatCurrency(contract.monthly_price)}</p>
                                <p>Deposit: {formatCurrency(contract.deposit_amount)}</p>
                            </div>
                        ) : (
                            <div className="payment-related-placeholder">
                                Contract information is unavailable.
                            </div>
                        )}
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <StickyNote size={18} />
                            Payment Note
                        </h3>
                        <div className="payment-related-placeholder">
                            {payment.note || "No note provided."}
                        </div>
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <CalendarDays size={18} />
                            Payment Timeline
                        </h3>
                        <div className="payment-related-placeholder">
                            <p>Created: {formatDate(payment.created_at)}</p>
                            <p>Paid: {formatDate(payment.paid_at)}</p>
                        </div>
                    </div>
                </section>

                <section className="content-card payment-status-card">
                    <div className="form-section-heading">
                        <h2>Update Payment Status</h2>
                        <p>Available actions depend on the current payment status.</p>
                    </div>

                    <div className="payment-current-status">
                        <CreditCard size={20} />
                        <div>
                            <strong>{statusLabel[payment.status]}</strong>
                            <span>Current payment status</span>
                        </div>
                    </div>

                    <div className="payment-status-actions">
                        {canMarkPaidOrFailed && (
                            <>
                                <button
                                    className="success-button"
                                    disabled={updating}
                                    onClick={() => void updateStatus("PAID")}
                                >
                                    <CheckCircle size={17} />
                                    Mark as Paid
                                </button>

                                <button
                                    className="danger-button"
                                    disabled={updating}
                                    onClick={() => void updateStatus("FAILED")}
                                >
                                    <XCircle size={17} />
                                    Mark as Failed
                                </button>
                            </>
                        )}

                        {canRefund && (
                            <button
                                className="secondary-button"
                                disabled={updating}
                                onClick={() => void updateStatus("REFUNDED")}
                            >
                                <RotateCcw size={17} />
                                Mark as Refunded
                            </button>
                        )}

                        {!canMarkPaidOrFailed && !canRefund && (
                            <p className="payment-status-note">
                                No further status changes are available for this payment.
                            </p>
                        )}
                    </div>

                    <button
                        className="secondary-button"
                        disabled={updating}
                        onClick={() => void loadPayment()}
                    >
                        <RefreshCw size={16} />
                        Refresh Details
                    </button>

                    <p className="payment-status-note">
                        {updating
                            ? "Updating payment status..."
                            : "Payment status changes follow the backend transition rules."}
                    </p>
                </section>
            </div>
        </div>
    );
}