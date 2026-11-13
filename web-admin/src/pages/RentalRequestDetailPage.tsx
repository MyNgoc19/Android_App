
import { useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    ClipboardList,
    UserRound,
    DoorOpen,
    CalendarDays,
    Clock,
    CheckCircle,
    XCircle,
} from "lucide-react";

export default function RentalRequestDetailPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Rental Request Details</h1>
                    <p>Review the details of a room rental request.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/rental-requests")}
                >
                    <ArrowLeft size={17} />
                    Back to Requests
                </button>
            </div>

            <div className="form-placeholder-notice">
                This is a UI preview. No actual rental request is loaded.
            </div>

            <div className="request-detail-layout">
                <section className="content-card request-detail-main">
                    <div className="request-detail-heading">
                        <div className="request-detail-icon">
                            <ClipboardList size={24} />
                        </div>

                        <div>
                            <h2>Request Information</h2>
                            <p>
                                Rental request information will appear here.
                            </p>
                        </div>

                        <span className="status-placeholder">
                            Status placeholder
                        </span>
                    </div>

                    <div className="request-info-grid">
                        <div className="request-info-item">
                            <span>Request ID</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="request-info-item">
                            <span>
                                <CalendarDays size={15} />
                                Desired Move-in Date
                            </span>
                            <strong>Not available</strong>
                        </div>

                        <div className="request-info-item">
                            <span>
                                <Clock size={15} />
                                Submitted At
                            </span>
                            <strong>Not available</strong>
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3>
                            <UserRound size={18} />
                            User Information
                        </h3>

                        <div className="request-related-placeholder">
                            User information will appear here when request
                            data is connected.
                        </div>
                    </div>

                    <div className="request-related-section">
                        <h3>
                            <DoorOpen size={18} />
                            Room Information
                        </h3>

                        <div className="request-related-placeholder">
                            Room information will appear here when request
                            data is connected.
                        </div>
                    </div>
                </section>

                <section className="content-card request-review-card">
                    <div className="form-section-heading">
                        <h2>Review Request</h2>
                        <p>
                            Review the request before making a decision.
                        </p>
                    </div>

                    <div className="request-review-status">
                        <Clock size={20} />

                        <div>
                            <strong>Status not available</strong>
                            <span>
                                The actual request status will appear here
                                after connecting the API.
                            </span>
                        </div>
                    </div>

                    <div className="request-review-actions">
                        <button className="success-button" disabled>
                            <CheckCircle size={17} />
                            Approve Request
                        </button>

                        <button className="danger-button" disabled>
                            <XCircle size={17} />
                            Reject Request
                        </button>
                    </div>

                    <p className="request-review-note">
                        These actions are placeholders and are not functional.
                        According to the API contract, only requests with
                        PENDING status can be approved or rejected.
                    </p>
                </section>
            </div>
        </div>
    );
}
