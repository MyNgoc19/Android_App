
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Eye, RefreshCw } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import { apiGet } from "../services/api";

type Contract = {
    id: number;
    tenant_name: string | null;
    tenant_email: string;
    room_title: string;
    room_address: string;
    start_date: string;
    end_date: string | null;
    monthly_price: number;
    status: "ACTIVE" | "ENDED" | "CANCELLED";
};

type ContractResponse = {
    items: Contract[];
    total: number;
    page: number;
    limit: number;
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleDateString("en-GB") : "No end date";

const formatPrice = (value: number) =>
    new Intl.NumberFormat("en-US").format(value) + " VND";

export default function ContractsPage() {
    const navigate = useNavigate();

    const [contracts, setContracts] = useState<Contract[]>([]);
    const [keyword, setKeyword] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadContracts = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams({
                page: String(page),
                limit: "10",
            });

            if (search.trim()) params.set("keyword", search.trim());
            if (status) params.set("status", status);

            const result = await apiGet<ContractResponse>(
                `admin/contracts?${params.toString()}`,
            );

            setContracts(result.items);
            setTotal(result.total);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to load contracts.");
        } finally {
            setLoading(false);
        }
    }, [page, search, status]);

    useEffect(() => {
        void loadContracts();
    }, [loadContracts]);

    const totalPages = Math.max(1, Math.ceil(total / 10));

    return (
        <div className="page-container">
            <PageHeader
                title="Contracts"
                description="Manage rental contracts and their lifecycle."
                actionLabel="Create Contract"
                onAction={() => navigate("/contracts/new")}
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
                        placeholder="Search tenant, email or room..."
                        aria-label="Search contracts"
                    />
                    <button type="submit" className="primary-button">Search</button>
                </form>

                <select
                    className="rooms-status-filter"
                    value={status}
                    onChange={(event) => {
                        setPage(1);
                        setStatus(event.target.value);
                    }}
                    aria-label="Filter by contract status"
                >
                    <option value="">All statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="ENDED">Ended</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void loadContracts()}
                    disabled={loading}
                >
                    <RefreshCw size={16} /> Refresh
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            <div className="content-card">
                <div className="section-heading">
                    <div>
                        <h2>Contract List</h2>
                        <p>{total} rental contracts</p>
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>Contract ID</th>
                                <th>Tenant</th>
                                <th>Room</th>
                                <th>Start Date</th>
                                <th>End Date</th>
                                <th>Monthly Price</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>

                        <tbody>
                            {loading ? (
                                <tr><td colSpan={8}>Loading contracts...</td></tr>
                            ) : contracts.length === 0 ? (
                                <tr><td colSpan={8}>No contracts found.</td></tr>
                            ) : contracts.map((contract) => (
                                <tr key={contract.id}>
                                    <td>#{contract.id}</td>
                                    <td>
                                        <strong>{contract.tenant_name || "Unnamed User"}</strong>
                                        <small className="room-id">{contract.tenant_email}</small>
                                    </td>
                                    <td>
                                        <strong>{contract.room_title}</strong>
                                        <small className="room-id">{contract.room_address}</small>
                                    </td>
                                    <td>{formatDate(contract.start_date)}</td>
                                    <td>{formatDate(contract.end_date)}</td>
                                    <td>{formatPrice(contract.monthly_price)}</td>
                                    <td>
                                        <span className={`dashboard-status ${contract.status.toLowerCase()}`}>
                                            {contract.status}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            type="button"
                                            className="icon-button"
                                            title="View contract"
                                            onClick={() => navigate(`/contracts/${contract.id}`)}
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
        </div>
    );
}
