
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Save, X, FileText } from "lucide-react";

export default function ContractFormPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Create Contract</h1>
                    <p>
                        Create a rental contract from an approved rental request.
                    </p>
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
                This is a UI preview. No contract will be created or saved.
            </div>

            <div className="form-layout">
                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>
                            <FileText size={19} />
                            Rental Request
                        </h2>
                        <p>
                            Select an approved rental request as the source
                            of this contract.
                        </p>
                    </div>

                    <div className="form-grid">
                        <div className="form-group full-width">
                            <label>Approved Rental Request</label>

                            <select defaultValue="">
                                <option value="" disabled>
                                    Select an approved rental request
                                </option>
                            </select>

                            <small>
                                Only requests with APPROVED status can be used
                                to create a contract. A request that already
                                has a contract cannot be used again.
                            </small>
                        </div>
                    </div>
                </section>

                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Contract Information</h2>
                        <p>
                            Enter the contract period, monthly rent and deposit.
                        </p>
                    </div>

                    <div className="form-grid">
                        <div className="form-group">
                            <label>Start Date</label>
                            <input type="date" />

                            <small>
                                The date when the contract starts.
                            </small>
                        </div>

                        <div className="form-group">
                            <label>End Date</label>
                            <input type="date" />

                            <small>
                                Optional. Leave empty if there is no specified
                                end date. The API receives null when empty.
                            </small>
                        </div>

                        <div className="form-group">
                            <label>Monthly Price (VND)</label>
                            <input
                                type="number"
                                min="0"
                                step="1"
                                placeholder="Enter monthly price"
                            />

                            <small>
                                Enter the monthly rental amount in VND.
                            </small>
                        </div>

                        <div className="form-group">
                            <label>Deposit Amount (VND)</label>
                            <input
                                type="number"
                                min="0"
                                step="1"
                                placeholder="Enter deposit amount"
                            />

                            <small>
                                Enter the required deposit amount in VND.
                            </small>
                        </div>
                    </div>
                </section>

                <div className="form-placeholder-notice">
                    <strong>Backend-managed information</strong>
                    <p>
                        Tenant ID and Room ID are derived by the backend
                        from the selected approved rental request.
                    </p>
                    <p>
                        The backend must verify the request status, check
                        that the room is available, and prevent duplicate
                        contract creation.
                    </p>
                    <p>
                        When the contract is successfully created, the
                        backend changes the room status to RENTED.
                    </p>
                    <p>
                        Do not submit tenant_id or room_id from the frontend.
                    </p>
                </div>

                <div className="form-actions">
                    <button
                        className="secondary-button"
                        onClick={() => navigate("/contracts")}
                    >
                        <X size={17} />
                        Cancel
                    </button>

                    <button className="primary-button" disabled>
                        <Save size={17} />
                        Create Contract
                    </button>
                </div>
            </div>
        </div>
    );
}
