
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Save, X } from "lucide-react";

export default function PaymentFormPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Create Payment</h1>
                    <p>
                        Create a payment record for a rental contract.
                    </p>
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
                This is a UI preview. No payment record will be created or saved.
            </div>

            <div className="form-layout">
                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Payment Information</h2>
                        <p>
                            Enter the payment details associated with a contract.
                        </p>
                    </div>

                    <div className="form-grid">
                        <div className="form-group full-width">
                            <label>Contract</label>

                            <select defaultValue="">
                                <option value="" disabled>
                                    Select a contract
                                </option>
                            </select>

                            <small>
                                Select the contract associated with this payment.
                            </small>
                        </div>

                        <div className="form-group">
                            <label>Amount (VND)</label>

                            <input
                                type="number"
                                min="0"
                                step="1"
                                placeholder="Enter amount"
                            />

                            <small>
                                Enter the payment amount as an integer in VND.
                            </small>
                        </div>

                        <div className="form-group">
                            <label>Payment Type</label>

                            <select defaultValue="">
                                <option value="" disabled>
                                    Select payment type
                                </option>
                                <option value="DEPOSIT">Deposit</option>
                                <option value="RENT">Rent</option>
                                <option value="OTHER">Other</option>
                            </select>

                            <small>
                                Choose DEPOSIT, RENT or OTHER.
                            </small>
                        </div>

                        <div className="form-group full-width">
                            <label>Payment Period</label>

                            <input type="month" />

                            <small>
                                For monthly rent payments, use YYYY-MM format.
                                Leave empty for payments without a specific
                                payment period.
                            </small>
                        </div>

                        <div className="form-group full-width">
                            <label>Note</label>

                            <textarea
                                rows={4}
                                placeholder="Enter payment note (optional)"
                            />

                            <small>
                                Optional additional information about this payment.
                            </small>
                        </div>
                    </div>
                </section>

                <div className="form-placeholder-notice">
                    <strong>Payment status management</strong>

                    <p>
                        New payment records automatically start with
                        PENDING status. Payment status is updated separately
                        through the payment status API.
                    </p>

                    <p>
                        This project records payment information only.
                        Online payment gateway integration and automatic
                        money transfers are not included in the current MVP.
                    </p>
                </div>

                <div className="form-actions">
                    <button
                        className="secondary-button"
                        onClick={() => navigate("/payments")}
                    >
                        <X size={17} />
                        Cancel
                    </button>

                    <button className="primary-button" disabled>
                        <Save size={17} />
                        Create Payment
                    </button>
                </div>
            </div>
        </div>
    );
}
