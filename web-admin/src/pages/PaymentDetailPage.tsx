
import { useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    CreditCard,
    FileText,
    CalendarDays,
    Clock,
    CheckCircle,
    XCircle,
    RotateCcw,
    StickyNote,
} from "lucide-react";

export default function PaymentDetailPage() {
    const navigate = useNavigate();

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

            <div className="form-placeholder-notice">
                This is a UI preview. No actual payment record is loaded.
            </div>

            <div className="payment-detail-layout">
                <section className="content-card payment-detail-main">
                    <div className="payment-detail-heading">
                        <div className="payment-detail-icon">
                            <CreditCard size={24} />
                        </div>

                        <div>
                            <h2>Payment Information</h2>
                            <p>Payment details will appear here.</p>
                        </div>

                        <span className="status-placeholder">
                            Status placeholder
                        </span>
                    </div>

                    <div className="payment-amount-preview">
                        <span>Payment Amount</span>
                        <strong>Not available</strong>
                        <small>VND</small>
                    </div>

                    <div className="payment-info-grid">
                        <div className="payment-info-item">
                            <span>Payment ID</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Payment Type</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Payment Period</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="payment-info-item">
                            <span>Status</span>
                            <strong>Not available</strong>
                        </div>
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <FileText size={18} />
                            Related Contract
                        </h3>

                        <div className="payment-related-placeholder">
                            Contract information will appear here when
                            payment data is connected.
                        </div>
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <StickyNote size={18} />
                            Payment Note
                        </h3>

                        <div className="payment-related-placeholder">
                            Payment note will appear here when payment
                            data is connected.
                        </div>
                    </div>

                    <div className="payment-related-section">
                        <h3>
                            <CalendarDays size={18} />
                            Payment Timeline
                        </h3>

                        <div className="payment-related-placeholder">
                            Payment creation and status update history
                            will appear here when connected to the API.
                        </div>
                    </div>
                </section>

                <section className="content-card payment-status-card">
                    <div className="form-section-heading">
                        <h2>Update Payment Status</h2>
                        <p>
                            Manage the status of this payment record.
                        </p>
                    </div>

                    <div className="payment-current-status">
                        <Clock size={20} />

                        <div>
                            <strong>Status not available</strong>
                            <span>
                                The actual payment status will appear here
                                after connecting the API.
                            </span>
                        </div>
                    </div>

                    <div className="payment-status-actions">
                        <button className="success-button" disabled>
                            <CheckCircle size={17} />
                            Mark as Paid
                        </button>

                        <button className="danger-button" disabled>
                            <XCircle size={17} />
                            Mark as Failed
                        </button>

                        <button className="secondary-button" disabled>
                            <RotateCcw size={17} />
                            Mark as Refunded
                        </button>
                    </div>

                    <p className="payment-status-note">
                        These actions are placeholders and are not functional.
                        The actual status transitions must follow the backend
                        API rules.
                    </p>
                </section>
            </div>
        </div>
    );
}
