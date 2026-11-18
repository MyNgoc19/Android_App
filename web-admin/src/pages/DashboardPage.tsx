import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    BarChart3,
    Users,
    DoorOpen,
    FileText,
    CreditCard,
    RefreshCw,
    ClipboardList,
    ArrowUpRight,
    Clock,
    CheckCircle,
    AlertCircle,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";

interface RoomStats {
    total: number;
    available: number;
    rented: number;
    inactive: number;
}

interface UserStats {
    total: number;
    users: number;
    admins: number;
    active: number;
    blocked: number;
}

interface RequestStats {
    total: number;
    pending: number;
    approved: number;
    rejected: number;
    cancelled: number;
}

interface ContractStats {
    total: number;
    active: number;
    ended: number;
    cancelled: number;
}

interface PaymentStats {
    total: number;
    pending: number;
    paid: number;
    failed: number;
    refunded: number;
    total_paid: number | string;
    total_pending: number | string;
}

interface RecentRequest {
    id: number;
    status: string;
    created_at: string;
    tenant_name: string;
    room_title: string;
}

interface RecentPayment {
    id: number;
    amount: number | string;
    payment_type: string;
    status: string;
    created_at: string;
    tenant_name: string;
    room_title: string;
}

interface DashboardData {
    rooms: RoomStats;
    users: UserStats;
    rental_requests: RequestStats;
    contracts: ContractStats;
    payments: PaymentStats;
    recent_rental_requests: RecentRequest[];
    recent_payments: RecentPayment[];
}

const formatNumber = (value: number | string | undefined) =>
    Number(value ?? 0).toLocaleString("vi-VN");

const formatCurrency = (value: number | string | undefined) =>
    `${formatNumber(value)} VND`;

const formatDate = (value: string) =>
    new Date(value).toLocaleString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });

const statusClass = (status: string) =>
    `status-badge status-${status.toLowerCase()}`;

export default function DashboardPage() {
    const navigate = useNavigate();

    const [data, setData] = useState<DashboardData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadDashboard = async () => {
        setLoading(true);
        setError("");

        try {
            const result = await apiGet<DashboardData>("admin/dashboard");
            setData(result);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Failed to load dashboard data.",
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void loadDashboard();
    }, []);

    const overviewItems = data
        ? [
            {
                label: "Total Rooms",
                value: formatNumber(data.rooms.total),
                detail: `${formatNumber(data.rooms.available)} available · ${formatNumber(data.rooms.rented)} rented`,
                icon: DoorOpen,
                path: "/rooms",
            },
            {
                label: "Total Users",
                value: formatNumber(data.users.total),
                detail: `${formatNumber(data.users.active)} active · ${formatNumber(data.users.blocked)} blocked`,
                icon: Users,
                path: "/users",
            },
            {
                label: "Rental Requests",
                value: formatNumber(data.rental_requests.total),
                detail: `${formatNumber(data.rental_requests.pending)} pending approval`,
                icon: ClipboardList,
                path: "/rental-requests",
            },
            {
                label: "Active Contracts",
                value: formatNumber(data.contracts.active),
                detail: `${formatNumber(data.contracts.total)} total contracts`,
                icon: FileText,
                path: "/contracts",
            },
            {
                label: "Total Payments",
                value: formatNumber(data.payments.total),
                detail: `${formatNumber(data.payments.pending)} pending · ${formatNumber(data.payments.paid)} paid`,
                icon: CreditCard,
                path: "/payments",
            },
        ]
        : [];

    return (
        <div className="page-container">
            <PageHeader
                title="Dashboard"
                description="Overview of your rental management system."
            />

            <div className="dashboard-toolbar">
                <span>
                    {loading ? "Updating dashboard..." : "Live system overview"}
                </span>
                <button
                    className="secondary-button"
                    onClick={() => void loadDashboard()}
                    disabled={loading}
                >
                    <RefreshCw size={16} />
                    Refresh
                </button>
            </div>

            {error && (
                <div className="form-error dashboard-error">
                    <AlertCircle size={17} />
                    <span>{error}</span>
                    <button
                        className="secondary-button"
                        onClick={() => void loadDashboard()}
                    >
                        Try Again
                    </button>
                </div>
            )}

            <div className="dashboard-stats-grid">
                {loading && !data ? (
                    Array.from({ length: 5 }).map((_, index) => (
                        <div className="dashboard-stat-card" key={index}>
                            <div className="dashboard-stat-skeleton" />
                            <div className="dashboard-stat-skeleton short" />
                            <div className="dashboard-stat-skeleton medium" />
                        </div>
                    ))
                ) : (
                    overviewItems.map((item) => {
                        const Icon = item.icon;

                        return (
                            <button
                                type="button"
                                className="dashboard-stat-card"
                                key={item.label}
                                onClick={() => navigate(item.path)}
                            >
                                <div className="dashboard-stat-top">
                                    <div className="dashboard-stat-icon">
                                        <Icon size={21} />
                                    </div>
                                    <ArrowUpRight size={17} className="dashboard-stat-arrow" />
                                </div>

                                <span className="dashboard-stat-label">
                                    {item.label}
                                </span>

                                <strong className="dashboard-stat-value">
                                    {item.value}
                                </strong>

                                <small>{item.detail}</small>
                            </button>
                        );
                    })
                )}
            </div>

            {data && (
                <>
                    <div className="dashboard-content-grid">
                        <section className="content-card dashboard-panel">
                            <div className="dashboard-panel-heading">
                                <div>
                                    <h2>Revenue Overview</h2>
                                    <p>Payment summary across all contracts.</p>
                                </div>
                                <BarChart3 size={21} />
                            </div>

                            <div className="dashboard-revenue-grid">
                                <div className="dashboard-revenue-item">
                                    <span>
                                        <CheckCircle size={16} />
                                        Total Paid
                                    </span>
                                    <strong>{formatCurrency(data.payments.total_paid)}</strong>
                                    <small>
                                        {formatNumber(data.payments.paid)} successful payments
                                    </small>
                                </div>

                                <div className="dashboard-revenue-item pending">
                                    <span>
                                        <Clock size={16} />
                                        Pending Amount
                                    </span>
                                    <strong>{formatCurrency(data.payments.total_pending)}</strong>
                                    <small>
                                        {formatNumber(data.payments.pending)} pending payments
                                    </small>
                                </div>
                            </div>
                        </section>

                        <section className="content-card dashboard-panel">
                            <div className="dashboard-panel-heading">
                                <div>
                                    <h2>System Status</h2>
                                    <p>Current records by category.</p>
                                </div>
                                <ClipboardList size={21} />
                            </div>

                            <div className="dashboard-status-list">
                                <div>
                                    <span>Available Rooms</span>
                                    <strong>{formatNumber(data.rooms.available)}</strong>
                                </div>
                                <div>
                                    <span>Rented Rooms</span>
                                    <strong>{formatNumber(data.rooms.rented)}</strong>
                                </div>
                                <div>
                                    <span>Pending Requests</span>
                                    <strong>{formatNumber(data.rental_requests.pending)}</strong>
                                </div>
                                <div>
                                    <span>Active Contracts</span>
                                    <strong>{formatNumber(data.contracts.active)}</strong>
                                </div>
                                <div>
                                    <span>Blocked Users</span>
                                    <strong>{formatNumber(data.users.blocked)}</strong>
                                </div>
                                <div>
                                    <span>Failed Payments</span>
                                    <strong>{formatNumber(data.payments.failed)}</strong>
                                </div>
                            </div>
                        </section>
                    </div>

                    <div className="dashboard-content-grid">
                        <section className="content-card dashboard-panel">
                            <div className="dashboard-panel-heading">
                                <div>
                                    <h2>Recent Rental Requests</h2>
                                    <p>Latest rental applications.</p>
                                </div>
                                <button
                                    className="text-link-button"
                                    onClick={() => navigate("/rental-requests")}
                                >
                                    View all
                                    <ArrowUpRight size={15} />
                                </button>
                            </div>

                            {data.recent_rental_requests.length === 0 ? (
                                <div className="dashboard-empty">
                                    No rental requests found.
                                </div>
                            ) : (
                                <div className="dashboard-activity-list">
                                    {data.recent_rental_requests.map((request) => (
                                        <button
                                            type="button"
                                            className="dashboard-activity-item"
                                            key={request.id}
                                            onClick={() => navigate(`/rental-requests/${request.id}`)}
                                        >
                                            <div className="dashboard-activity-icon">
                                                <FileText size={18} />
                                            </div>
                                            <div className="dashboard-activity-info">
                                                <strong>{request.tenant_name}</strong>
                                                <span>{request.room_title}</span>
                                                <small>{formatDate(request.created_at)}</small>
                                            </div>
                                            <span className={statusClass(request.status)}>
                                                {request.status}
                                            </span>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </section>

                        <section className="content-card dashboard-panel">
                            <div className="dashboard-panel-heading">
                                <div>
                                    <h2>Recent Payments</h2>
                                    <p>Latest payment records.</p>
                                </div>
                                <button
                                    className="text-link-button"
                                    onClick={() => navigate("/payments")}
                                >
                                    View all
                                    <ArrowUpRight size={15} />
                                </button>
                            </div>

                            {data.recent_payments.length === 0 ? (
                                <div className="dashboard-empty">
                                    No payment records found.
                                </div>
                            ) : (
                                <div className="dashboard-activity-list">
                                    {data.recent_payments.map((payment) => (
                                        <button
                                            type="button"
                                            className="dashboard-activity-item"
                                            key={payment.id}
                                            onClick={() => navigate(`/payments/${payment.id}`)}
                                        >
                                            <div className="dashboard-activity-icon">
                                                <CreditCard size={18} />
                                            </div>
                                            <div className="dashboard-activity-info">
                                                <strong>{payment.tenant_name}</strong>
                                                <span>
                                                    {payment.room_title} · {formatCurrency(payment.amount)}
                                                </span>
                                                <small>{formatDate(payment.created_at)}</small>
                                            </div>
                                            <span className={statusClass(payment.status)}>
                                                {payment.status}
                                            </span>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </section>
                    </div>
                </>
            )}
        </div>
    );
}