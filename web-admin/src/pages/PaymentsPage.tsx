
import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, RefreshCw, Plus } from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";

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

const LIMIT = 10;

const formatCurrency = (value: number | string) =>
    `${Number(value).toLocaleString("vi-VN")} VND`;

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("vi-VN") : "—";

const statusLabel: Record<Payment["status"], string> = {
    PENDING: "Pending",
    PAID: "Paid",
    FAILED: "Failed",
    REFUNDED: "Refunded",
};

export default function PaymentsPage() {
    const navigate = useNavigate();

    const [payments, setPayments] = useState<Payment[]>([]);
    const [keyword, setKeyword] = useState("");
    const [appliedKeyword, setAppliedKeyword] = useState("");
    const [status, setStatus] = useState("");
    const [paymentType, setPaymentType] = useState("");
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const totalPages = Math.max(1, Math.ceil(total / LIMIT));

    const loadPayments = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams({
                page: String(page),
                limit: String(LIMIT),
            });

            if (appliedKeyword) params.set("keyword", appliedKeyword);
            if (status) params.set("status", status);
            if (paymentType) params.set("payment_type", paymentType);

            const result = await apiGet<PaymentResponse>(
                `admin/payments?${params.toString()}`,
            );

            setPayments(result.items ?? []);
            setTotal(Number(result.total ?? 0));
        } catch (err) {
            setPayments([]);
            setTotal(0);
            setError(
                err instanceof Error
                    ? err.message
                    : "Failed to load payment records.",
            );
        } finally {
            setLoading(false);
        }
    }, [page, appliedKeyword, status, paymentType]);

    useEffect(() => {
        void loadPayments();
    }, [loadPayments]);

    const handleSearch = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setPage(1);
        setAppliedKeyword(keyword.trim());
    };

    const handleStatusChange = (value: string) => {
        setPage(1);
        setStatus(value);
    };

    const handleTypeChange = (value: string) => {
        setPage(1);
        setPaymentType(value);
    };

    return (
        <div className="page-container payments-page">
            <PageHeader
                title="Payments"
                description="Manage payment records and payment statuses."
                actionLabel="Create Payment"
                onAction={() => navigate("/payments/new")}
            />

            <form className="toolbar payments-toolbar" onSubmit={handleSearch}>
                <div className="search-input-wrapper payments-search">
                    <Search size={18} />
                    <input
                        value={keyword}
                        onChange={(event) => setKeyword(event.target.value)}
                        placeholder="Search tenant, room or note..."
                    />
                </div>

                <select
                    className="payments-filter"
                    value={status}
                    onChange={(event) => handleStatusChange(event.target.value)}
                    aria-label="Filter by payment status"
                >
                    <option value="">All statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="PAID">Paid</option>
                    <option value="FAILED">Failed</option>
                    <option value="REFUNDED">Refunded</option>
                </select>

                <select
                    className="payments-filter"
                    value={paymentType}
                    onChange={(event) => handleTypeChange(event.target.value)}
                    aria-label="Filter by payment type"
                >
                    <option value="">All types</option>
                    <option value="DEPOSIT">Deposit</option>
                    <option value="RENT">Rent</option>
                    <option value="OTHER">Other</option>
                </select>

                <button type="submit" className="secondary-button">
                    <Search size={16} />
                    Search
                </button>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void loadPayments()}
                    disabled={loading}
                >
                    <RefreshCw size={16} />
                    Refresh
                </button>
            </form>

            {error && (
                <div className="form-error payments-error">
                    {error}
                </div>
            )}

            <section className="content-card payments-card">
                <div className="table-container payments-table-container">
                    <table className="data-table payments-table">
                        <thead>
                            <tr>
                                <th>Payment ID</th>
                                <th>Tenant</th>
                                <th>Room</th>
                                <th>Amount</th>
                                <th>Type</th>
                                <th>Payment Period</th>
                                <th>Status</th>
                                <th>Created At</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan={9} className="table-empty">
                                        Loading payments...
                                    </td>
                                </tr>
                            ) : payments.length === 0 ? (
                                <tr>
                                    <td colSpan={9} className="table-empty">
                                        <div className="payments-empty">
                                            <div className="payments-empty-icon">
                                                <Search size={22} />
                                            </div>
                                            <strong>No payment records found</strong>
                                            <span>
                                                {appliedKeyword || status || paymentType
                                                    ? "Try changing your search or filters."
                                                    : "Payment records will appear here after they are created."}
                                            </span>
                                        </div>
                                    </td>
                                </tr>
                            ) : (
                                payments.map((payment) => (
                                    <tr key={payment.id}>
                                        <td>#{payment.id}</td>
                                        <td>
                                            <div className="payment-tenant">
                                                <strong>{payment.tenant_name || "Unknown"}</strong>
                                                <small>{payment.tenant_email || "—"}</small>
                                            </div>
                                        </td>
                                        <td>{payment.room_title || "—"}</td>
                                        <td className="payment-amount">
                                            {formatCurrency(payment.amount)}
                                        </td>
                                        <td>{payment.payment_type}</td>
                                        <td>{payment.payment_period || "—"}</td>
                                        <td>
                                            <span
                                                className={`status-badge status-${payment.status.toLowerCase()}`}
                                            >
                                                {statusLabel[payment.status]}
                                            </span>
                                        </td>
                                        <td>{formatDate(payment.created_at)}</td>
                                        <td>
                                            <button
                                                type="button"
                                                className="icon-button"
                                                title="View payment"
                                                onClick={() => navigate(`/payments/${payment.id}`)}
                                            >
                                                <Eye size={17} />
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="payments-pagination">
                    <span className="payments-pagination-info">
                        Showing {payments.length === 0 ? 0 : (page - 1) * LIMIT + 1}
                        {"–"}
                        {Math.min((page - 1) * LIMIT + payments.length, total)}
                        {" "}of {total} payments
                    </span>

                    <div className="payments-pagination-controls">
                        <button
                            type="button"
                            className="secondary-button"
                            disabled={page <= 1 || loading}
                            onClick={() => setPage((current) => current - 1)}
                        >
                            Previous
                        </button>

                        <span>
                            Page {page} of {totalPages}
                        </span>

                        <button
                            type="button"
                            className="secondary-button"
                            disabled={page >= totalPages || loading}
                            onClick={() => setPage((current) => current + 1)}
                        >
                            Next
                        </button>
                    </div>
                </div>
            </section>

            <div className="placeholder-note">
                <div>
                    <strong>Payment management</strong>
                    <p>
                        This module records payment information and status changes.
                        Online payment gateway integration is not included in the MVP.
                    </p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate("/payments/new")}
                >
                    <Plus size={16} />
                    Create Payment
                </button>
            </div>
        </div>
    );
}
