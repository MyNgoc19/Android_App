import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ArrowLeft, Save, X, FileText } from "lucide-react";
import { apiGet, apiPost } from "../services/api";

type ApprovedRequest = {
    id: number;
    desired_move_in: string | null;
    tenant_name: string | null;
    tenant_email: string;
    room_title: string;
    room_address: string;
    room_monthly_price: number;
};

type RequestResponse = {
    items: ApprovedRequest[];
    total: number;
};

export default function ContractFormPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [requests, setRequests] = useState<ApprovedRequest[]>([]);
    const [requestId, setRequestId] = useState(searchParams.get("requestId") || "");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [monthlyPrice, setMonthlyPrice] = useState("");
    const [depositAmount, setDepositAmount] = useState("0");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadApprovedRequests = async () => {
            setLoading(true);
            setError("");

            try {
                const result = await apiGet<RequestResponse>(
                    "admin/rental-requests?page=1&limit=100&status=APPROVED",
                );
                setRequests(result.items);
            } catch (err) {
                setError(
                    err instanceof Error
                        ? err.message
                        : "Unable to load approved requests.",
                );
            } finally {
                setLoading(false);
            }
        };

        void loadApprovedRequests();
    }, []);

    const selectedRequest = requests.find(
        (request) => String(request.id) === requestId,
    );

    useEffect(() => {
        if (selectedRequest) {
            setMonthlyPrice(String(selectedRequest.room_monthly_price));
        }
    }, [selectedRequest]);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!requestId || !startDate || !monthlyPrice || !depositAmount) {
            setError("Please complete all required fields.");
            return;
        }

        if (endDate && endDate < startDate) {
            setError("End date cannot be earlier than start date.");
            return;
        }

        setSaving(true);
        setError("");

        try {
            const result = await apiPost<{ contract_id: number }>(
                "admin/contracts",
                {
                    request_id: Number(requestId),
                    start_date: startDate,
                    end_date: endDate || null,
                    monthly_price: Number(monthlyPrice),
                    deposit_amount: Number(depositAmount),
                },
            );

            navigate(`/contracts/${result.contract_id}`);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to create contract.",
            );
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Create Contract</h1>
                    <p>Create a rental contract from an approved rental request.</p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => navigate("/contracts")}
                >
                    <ArrowLeft size={17} /> Back to Contracts
                </button>
            </div>

            {error && <div className="dashboard-error">{error}</div>}

            {loading ? (
                <div className="content-card form-section">Loading approved requests...</div>
            ) : (
                <form className="form-layout" onSubmit={handleSubmit}>
                    <section className="content-card form-section">
                        <div className="form-section-heading">
                            <h2><FileText size={19} /> Rental Request</h2>
                            <p>Select an approved rental request as the source of this contract.</p>
                        </div>

                        <div className="form-grid">
                            <div className="form-group full-width">
                                <label htmlFor="requestId">Approved Rental Request *</label>
                                <select
                                    id="requestId"
                                    value={requestId}
                                    onChange={(event) => setRequestId(event.target.value)}
                                    required
                                >
                                    <option value="">Select an approved request</option>
                                    {requests.map((request) => (
                                        <option key={request.id} value={request.id}>
                                            #{request.id} — {request.tenant_name || request.tenant_email}
                                            {" — "}
                                            {request.room_title}
                                        </option>
                                    ))}
                                </select>

                                {requests.length === 0 && (
                                    <small>No approved rental requests are currently available.</small>
                                )}
                            </div>
                        </div>

                        {selectedRequest && (
                            <div className="contract-selected-request">
                                <strong>{selectedRequest.room_title}</strong>
                                <p>{selectedRequest.tenant_name || "Unnamed User"} · {selectedRequest.tenant_email}</p>
                                <p>{selectedRequest.room_address}</p>
                                <p>
                                    Current room price:{" "}
                                    {new Intl.NumberFormat("en-US").format(selectedRequest.room_monthly_price)} VND
                                </p>
                            </div>
                        )}
                    </section>

                    <section className="content-card form-section">
                        <div className="form-section-heading">
                            <h2>Contract Information</h2>
                            <p>Enter the contract period, monthly rent and deposit.</p>
                        </div>

                        <div className="form-grid">
                            <div className="form-group">
                                <label htmlFor="startDate">Start Date *</label>
                                <input
                                    id="startDate"
                                    type="date"
                                    value={startDate}
                                    onChange={(event) => setStartDate(event.target.value)}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="endDate">End Date</label>
                                <input
                                    id="endDate"
                                    type="date"
                                    value={endDate}
                                    min={startDate || undefined}
                                    onChange={(event) => setEndDate(event.target.value)}
                                />
                                <small>Optional. Leave empty if there is no specified end date.</small>
                            </div>

                            <div className="form-group">
                                <label htmlFor="monthlyPrice">Monthly Price (VND) *</label>
                                <input
                                    id="monthlyPrice"
                                    type="number"
                                    min="0"
                                    step="1"
                                    value={monthlyPrice}
                                    onChange={(event) => setMonthlyPrice(event.target.value)}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="depositAmount">Deposit Amount (VND) *</label>
                                <input
                                    id="depositAmount"
                                    type="number"
                                    min="0"
                                    step="1"
                                    value={depositAmount}
                                    onChange={(event) => setDepositAmount(event.target.value)}
                                    required
                                />
                            </div>
                        </div>
                    </section>

                    <div className="form-placeholder-notice">
                        <strong>Backend-managed information</strong>
                        <p>
                            Tenant ID and Room ID are determined by the backend
                            from the selected rental request. The backend also
                            verifies request approval, room availability and duplicate contracts.
                        </p>
                    </div>

                    <div className="form-actions">
                        <button
                            type="button"
                            className="secondary-button"
                            onClick={() => navigate("/contracts")}
                        >
                            <X size={17} /> Cancel
                        </button>

                        <button
                            type="submit"
                            className="primary-button"
                            disabled={saving || requests.length === 0}
                        >
                            <Save size={17} />
                            {saving ? "Creating..." : "Create Contract"}
                        </button>
                    </div>
                </form>
            )}
        </div>
    );
}
