
import { useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    FileText,
    UserRound,
    DoorOpen,
    CalendarDays,
    Ban,
    CheckCircle,
} from "lucide-react";

export default function ContractDetailPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Contract Details</h1>
                    <p>View rental contract information and status.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/contracts")}
                >
                    <ArrowLeft size={17} />
                    Back to Contracts
                </button>
            </div>

            <div className="form-placeholder-notice">
                This is a UI preview. No actual contract is loaded.
            </div>

            <div className="contract-detail-layout">
                <section className="content-card contract-detail-main">
                    <div className="contract-detail-heading">
                        <div className="contract-detail-icon">
                            <FileText size={24} />
                        </div>

                        <div>
                            <h2>Contract Information</h2>
                            <p>Contract details will appear here.</p>
                        </div>

                        <span className="status-placeholder">
                            Status placeholder
                        </span>
                    </div>

                    <div className="contract-info-grid">
                        <div className="contract-info-item">
                            <span>Contract ID</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="contract-info-item">
                            <span>Status</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="contract-info-item">
                            <span>Start Date</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="contract-info-item">
                            <span>End Date</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="contract-info-item">
                            <span>Monthly Price</span>
                            <strong>Not available</strong>
                        </div>

                        <div className="contract-info-item">
                            <span>Deposit Amount</span>
                            <strong>Not available</strong>
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3>
                            <UserRound size={18} />
                            Tenant Information
                        </h3>

                        <div className="contract-related-placeholder">
                            Tenant information will appear here.
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3>
                            <DoorOpen size={18} />
                            Room Information
                        </h3>

                        <div className="contract-related-placeholder">
                            Room information will appear here.
                        </div>
                    </div>

                    <div className="contract-related-section">
                        <h3>
                            <CalendarDays size={18} />
                            Contract Timeline
                        </h3>

                        <div className="contract-related-placeholder">
                            Contract timeline information will appear here.
                        </div>
                    </div>
                </section>

                <section className="content-card contract-actions-card">
                    <div className="form-section-heading">
                        <h2>Contract Actions</h2>
                        <p>Manage the lifecycle of this contract.</p>
                    </div>

                    <div className="contract-status-preview">
                        <strong>Status not available</strong>
                        <span>
                            The actual contract status will appear here
                            after connecting the API.
                        </span>
                    </div>

                    <div className="contract-action-buttons">
                        <button className="success-button" disabled>
                            <CheckCircle size={17} />
                            End Contract
                        </button>

                        <button className="danger-button" disabled>
                            <Ban size={17} />
                            Cancel Contract
                        </button>
                    </div>

                    <p className="contract-action-note">
                        These actions are placeholders and are not functional.
                        According to the API contract, only ACTIVE contracts
                        can be ended or cancelled.
                    </p>
                </section>
            </div>
        </div>
    );
}
