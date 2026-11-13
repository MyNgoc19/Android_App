
import { useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    UserRound,
    Mail,
    Phone,
    Shield,
    CalendarDays,
    Ban,
    CheckCircle,
} from "lucide-react";

export default function UserDetailPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            {/* PAGE HEADER */}
            <div className="page-header">
                <div>
                    <h1>User Details</h1>
                    <p>View account information and manage account status.</p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate("/users")}
                >
                    <ArrowLeft size={17} />
                    Back to Users
                </button>
            </div>

            <div className="form-placeholder-notice">
                This is a UI preview. No actual user account is loaded.
            </div>

            <div className="user-detail-layout">
                {/* USER PROFILE */}
                <section className="content-card user-profile-card">
                    <div className="user-profile-header">
                        <div className="user-avatar-large">
                            <UserRound size={34} />
                        </div>

                        <div className="user-profile-heading">
                            <h2>User Information</h2>
                            <p>Account details will appear here.</p>
                        </div>

                        <span className="status-placeholder">
                            Status placeholder
                        </span>
                    </div>

                    <div className="user-info-list">
                        <div className="user-info-item">
                            <div className="user-info-icon">
                                <UserRound size={18} />
                            </div>

                            <div>
                                <span>Full Name</span>
                                <strong>Not available</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon">
                                <Mail size={18} />
                            </div>

                            <div>
                                <span>Email Address</span>
                                <strong>Not available</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon">
                                <Phone size={18} />
                            </div>

                            <div>
                                <span>Phone Number</span>
                                <strong>Not available</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon">
                                <Shield size={18} />
                            </div>

                            <div>
                                <span>Role</span>
                                <strong>Not available</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon">
                                <CalendarDays size={18} />
                            </div>

                            <div>
                                <span>Created At</span>
                                <strong>Not available</strong>
                            </div>
                        </div>
                    </div>
                </section>

                {/* ACCOUNT STATUS */}
                <section className="content-card user-status-card">
                    <div className="form-section-heading">
                        <h2>Account Status</h2>
                        <p>
                            Manage whether this account can access the system.
                        </p>
                    </div>

                    <div className="user-status-preview">
                        <span className="status-placeholder">
                            Current status placeholder
                        </span>

                        <p>
                            The actual account status will be displayed here
                            when user data is connected.
                        </p>
                    </div>

                    <div className="user-status-actions">
                        <button
                            type="button"
                            className="danger-button"
                            disabled
                            title="Available after connecting the block user API"
                        >
                            <Ban size={17} />
                            Block Account
                        </button>

                        <button
                            type="button"
                            className="success-button"
                            disabled
                            title="Available after connecting the unblock user API"
                        >
                            <CheckCircle size={17} />
                            Unblock Account
                        </button>
                    </div>

                    <p className="user-status-note">
                        Block and Unblock actions will be connected to
                        the backend API later. Only the appropriate action
                        will be enabled according to the account's current
                        status.
                    </p>
                </section>
            </div>

            <div className="form-placeholder-notice">
                User role and account status are determined by backend data.
                The frontend must not treat Firebase authentication alone
                as proof of ADMIN authorization.
            </div>
        </div>
    );
}
