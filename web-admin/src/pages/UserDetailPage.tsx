import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    UserRound,
    Mail,
    Phone,
    Shield,
    CalendarDays,
    Ban,
    CheckCircle,
    RefreshCw,
} from "lucide-react";
import { apiGet, apiPatch } from "../services/api";
import { useAuth } from "../contexts/useAuth";

type UserDetail = {
    id: number;
    email: string;
    full_name: string | null;
    phone: string | null;
    avatar_url: string | null;
    role: "USER" | "ADMIN";
    status: "ACTIVE" | "BLOCKED";
    created_at: string;
    updated_at: string;
    statistics: {
        rental_requests: number;
        contracts: number;
    };
};

export default function UserDetailPage() {
    const navigate = useNavigate();
    const { userId } = useParams();

    const { account } = useAuth();
    const [selectedRole, setSelectedRole] = useState<"USER" | "ADMIN">("USER");
    const [user, setUser] = useState<UserDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState("");

    const loadUser = useCallback(async () => {
        if (!userId) return;

        setLoading(true);
        setError("");

        try {
            const result = await apiGet<UserDetail>(`admin/users/${userId}`);
            setUser(result);
            setSelectedRole(result.role);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to load user.");
        } finally {
            setLoading(false);
        }
    }, [userId]);

    useEffect(() => {
        void loadUser();
    }, [loadUser]);

    const updateStatus = async () => {
        if (!user) return;

        const nextStatus = user.status === "ACTIVE" ? "BLOCKED" : "ACTIVE";
        const action = nextStatus === "BLOCKED" ? "block" : "unblock";

        if (!window.confirm(`Are you sure you want to ${action} this account?`)) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(`admin/users/${user.id}/status`, {
                status: nextStatus,
            });
            await loadUser();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to update account.");
        } finally {
            setUpdating(false);
        }
    };

    const updateRole = async () => {
        if (!user || selectedRole === user.role) return;

        if (account?.id === user.id) {
            setError("You cannot change your own administrator role.");
            return;
        }

        const action =
            selectedRole === "ADMIN"
                ? "grant administrator privileges to"
                : "remove administrator privileges from";

        if (!window.confirm(`Are you sure you want to ${action} this user?`)) {
            return;
        }

        setUpdating(true);
        setError("");

        try {
            await apiPatch(`admin/users/${user.id}/role`, {
                role: selectedRole,
            });

            await loadUser();
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to update user role."
            );
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return <div className="page-container">Loading user details...</div>;
    }

    if (error && !user) {
        return (
            <div className="page-container">
                <div className="dashboard-error">{error}</div>
                <button className="secondary-button" onClick={() => navigate("/users")}>
                    <ArrowLeft size={17} /> Back to Users
                </button>
            </div>
        );
    }

    if (!user) {
        return <div className="page-container">User not found.</div>;
    }

    return (
        <div className="page-container">
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
                    <ArrowLeft size={17} /> Back to Users
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="user-detail-layout">
                <section className="content-card user-profile-card">
                    <div className="user-profile-header">
                        <div className="user-avatar-large">
                            {user.avatar_url ? (
                                <img src={user.avatar_url} alt="User avatar" />
                            ) : (
                                <UserRound size={34} />
                            )}
                        </div>

                        <div className="user-profile-heading">
                            <h2>{user.full_name || "Unnamed User"}</h2>
                            <p>User ID: {user.id}</p>
                        </div>

                        <span className={`dashboard-status ${user.status.toLowerCase()}`}>
                            {user.status}
                        </span>
                    </div>

                    <div className="user-info-list">
                        <div className="user-info-item">
                            <div className="user-info-icon"><Mail size={18} /></div>
                            <div>
                                <span>Email Address</span>
                                <strong>{user.email}</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon"><Phone size={18} /></div>
                            <div>
                                <span>Phone Number</span>
                                <strong>{user.phone || "Not provided"}</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon"><Shield size={18} /></div>
                            <div>
                                <span>Role</span>
                                <strong>{user.role}</strong>
                            </div>
                        </div>

                        <div className="user-info-item">
                            <div className="user-info-icon"><CalendarDays size={18} /></div>
                            <div>
                                <span>Created At</span>
                                <strong>{new Date(user.created_at).toLocaleString("en-GB")}</strong>
                            </div>
                        </div>
                    </div>
                </section>

                <div className="user-detail-sidebar">
                    <section className="content-card user-status-card">
                        <div className="form-section-heading">
                            <h2>Account Status</h2>
                            <p>Manage this user's access to the system.</p>
                        </div>

                        <span className={`dashboard-status ${user.status.toLowerCase()}`}>
                            {user.status}
                        </span>

                        <p>
                            {user.status === "ACTIVE"
                                ? "This account can currently access the system."
                                : "This account is currently blocked."}
                        </p>

                        <button
                            type="button"
                            className={user.status === "ACTIVE" ? "danger-button" : "success-button"}
                            onClick={() => void updateStatus()}
                            disabled={updating}
                        >
                            {updating ? (
                                <RefreshCw size={17} />
                            ) : user.status === "ACTIVE" ? (
                                <Ban size={17} />
                            ) : (
                                <CheckCircle size={17} />
                            )}
                            {updating
                                ? "Updating..."
                                : user.status === "ACTIVE"
                                    ? "Block Account"
                                    : "Unblock Account"}
                        </button>
                    </section>
                    <section className="content-card user-status-card">
                        <div className="form-section-heading">
                            <h2>Role Management</h2>
                            <p>Manage this user's system permissions.</p>
                        </div>

                        <label htmlFor="user-role">Account Role</label>

                        <select
                            id="user-role"
                            value={selectedRole}
                            onChange={(event) => {
                                setSelectedRole(event.target.value as "USER" | "ADMIN");
                            }}
                            disabled={updating || account?.id === user.id}
                            style={{
                                display: "block",
                                width: "100%",
                                minHeight: "42px",
                                padding: "8px 12px",
                                marginTop: "10px",
                                marginBottom: "16px",
                                position: "relative",
                                zIndex: 2,
                                pointerEvents: "auto",
                                cursor: "pointer",
                            }}
                        >
                            <option value="USER">User</option>
                            <option value="ADMIN">Administrator</option>
                        </select>

                        {account?.id === user.id && (
                            <p>You cannot change your own administrator role.</p>
                        )}

                        <button
                            type="button"
                            className="primary-button"
                            onClick={() => void updateRole()}
                            disabled={
                                updating ||
                                selectedRole === user.role ||
                                account?.id === user.id
                            }
                        >
                            {updating ? "Updating..." : "Save Role"}
                        </button>
                    </section>
                    <section className="content-card user-status-card">
                        <div className="form-section-heading">
                            <h2>Activity Overview</h2>
                            <p>User activity statistics.</p>
                        </div>

                        <div className="user-stat-row">
                            <span>Rental Requests</span>
                            <strong>{user.statistics.rental_requests}</strong>
                        </div>

                        <div className="user-stat-row">
                            <span>Contracts</span>
                            <strong>{user.statistics.contracts}</strong>
                        </div>
                    </section>
                </div>
            </div>

            <div className="form-placeholder-notice">
                User roles are managed through a trusted administrative process.
                This page only manages account status.
            </div>
        </div>
    );
}
