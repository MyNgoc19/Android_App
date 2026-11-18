
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, RefreshCw } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";

type User = {
    id: number;
    email: string;
    full_name: string | null;
    phone: string | null;
    avatar_url: string | null;
    role: "USER" | "ADMIN";
    status: "ACTIVE" | "BLOCKED";
    created_at: string;
};

type UserResponse = {
    items: User[];
    total: number;
    page: number;
    limit: number;
};

const formatDate = (value: string) =>
    new Date(value).toLocaleDateString("en-GB");

export default function UsersPage() {
    const navigate = useNavigate();

    const [users, setUsers] = useState<User[]>([]);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");
    const [role, setRole] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadUsers = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams({
                page: String(page),
                limit: "10",
            });

            if (search.trim()) params.set("keyword", search.trim());
            if (role) params.set("role", role);
            if (status) params.set("status", status);

            const result = await apiGet<UserResponse>(
                `admin/users?${params.toString()}`,
            );

            setUsers(result.items);
            setTotal(result.total);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to load users.");
        } finally {
            setLoading(false);
        }
    }, [page, search, role, status]);

    useEffect(() => {
        void loadUsers();
    }, [loadUsers]);

    const totalPages = Math.max(1, Math.ceil(total / 10));

    return (
        <div className="page-container">
            <PageHeader
                title="Users"
                description="Manage user accounts, roles and access status."
            />

            <div className="toolbar rooms-toolbar">
                <form
                    className="rooms-search"
                    onSubmit={(event) => {
                        event.preventDefault();
                        setPage(1);
                        setSearch(keyword);
                    }}
                >
                    <Search size={18} />
                    <input
                        value={keyword}
                        onChange={(event) => setKeyword(event.target.value)}
                        placeholder="Search by name, email or phone..."
                        aria-label="Search users"
                    />
                    <button type="submit" className="primary-button">
                        Search
                    </button>
                </form>

                <select
                    className="rooms-status-filter"
                    value={role}
                    onChange={(event) => {
                        setPage(1);
                        setRole(event.target.value);
                    }}
                    aria-label="Filter by role"
                >
                    <option value="">All roles</option>
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                </select>

                <select
                    className="rooms-status-filter"
                    value={status}
                    onChange={(event) => {
                        setPage(1);
                        setStatus(event.target.value);
                    }}
                    aria-label="Filter by account status"
                >
                    <option value="">All statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="BLOCKED">Blocked</option>
                </select>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void loadUsers()}
                    disabled={loading}
                >
                    <RefreshCw size={16} /> Refresh
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="content-card">
                <div className="section-heading">
                    <div>
                        <h2>User List</h2>
                        <p>{total} registered accounts</p>
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>User</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Created At</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr><td colSpan={7}>Loading users...</td></tr>
                            ) : users.length === 0 ? (
                                <tr><td colSpan={7}>No users found.</td></tr>
                            ) : users.map((user) => (
                                <tr key={user.id}>
                                    <td>
                                        <strong>{user.full_name || "Unnamed User"}</strong>
                                        <small className="room-id">ID: {user.id}</small>
                                    </td>
                                    <td>{user.email}</td>
                                    <td>{user.phone || "—"}</td>
                                    <td>
                                        <span className={`user-role ${user.role.toLowerCase()}`}>
                                            {user.role}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`dashboard-status ${user.status.toLowerCase()}`}>
                                            {user.status}
                                        </span>
                                    </td>
                                    <td>{formatDate(user.created_at)}</td>
                                    <td>
                                        <button
                                            type="button"
                                            className="icon-button"
                                            title="View user details"
                                            onClick={() => navigate(`/users/${user.id}`)}
                                        >
                                            <Eye size={17} />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="rooms-pagination">
                    <span>Page {page} of {totalPages}</span>
                    <div>
                        <button
                            className="secondary-button"
                            disabled={page <= 1 || loading}
                            onClick={() => setPage((current) => current - 1)}
                        >
                            Previous
                        </button>
                        <button
                            className="secondary-button"
                            disabled={page >= totalPages || loading}
                            onClick={() => setPage((current) => current + 1)}
                        >
                            Next
                        </button>
                    </div>
                </div>
            </div>

            <div className="form-placeholder-notice">
                <strong>Account security</strong>
                <p>
                    New accounts receive the USER role by default.
                    Administrator privileges must be assigned through a trusted process.
                </p>
            </div>
        </div>
    );
}
