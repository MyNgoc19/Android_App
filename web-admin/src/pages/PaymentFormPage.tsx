import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ArrowLeft, Save, X } from "lucide-react";

import { apiGet, apiPost } from "../services/api";

interface Contract {
    id: number;
    tenant_name: string;
    tenant_email: string;
    room_title: string;
    room_address: string;
    monthly_price: number | string;
    status: string;
}

interface ContractResponse {
    items: Contract[];
    total: number;
}

interface PaymentCreateResponse {
    message: string;
    payment: {
        id: number;
        contract_id: number;
    };
}

export default function PaymentFormPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [contracts, setContracts] = useState<Contract[]>([]);
    const [contractId, setContractId] = useState(searchParams.get("contractId") ?? "");
    const [amount, setAmount] = useState("");
    const [paymentType, setPaymentType] = useState("RENT");
    const [paymentPeriod, setPaymentPeriod] = useState("");
    const [note, setNote] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadContracts = async () => {
            setLoading(true);
            try {
                const result = await apiGet<ContractResponse>(
                    "admin/contracts?page=1&limit=100&status=ACTIVE",
                );
                setContracts(result.items ?? []);
            } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to load contracts.");
            } finally {
                setLoading(false);
            }
        };

        void loadContracts();
    }, []);

    const selectedContract = contracts.find(
        (contract) => String(contract.id) === contractId,
    );

    useEffect(() => {
        if (selectedContract && paymentType === "RENT" && !amount) {
            setAmount(String(selectedContract.monthly_price));
        }
    }, [selectedContract, paymentType, amount]);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError("");

        if (!contractId) {
            setError("Please select a contract.");
            return;
        }

        if (!amount || Number(amount) <= 0) {
            setError("Please enter a valid payment amount.");
            return;
        }

        if (paymentType === "RENT" && !paymentPeriod) {
            setError("Payment period is required for rent payments.");
            return;
        }

        setSaving(true);

        try {
            const result = await apiPost<PaymentCreateResponse>(
                `admin/contracts/${contractId}/payments`,
                {
                    amount: Number(amount),
                    payment_type: paymentType,
                    payment_period: paymentType === "RENT" ? paymentPeriod : null,
                    note: note.trim() || null,
                },
            );

            navigate(`/payments/${result.payment.id}`);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to create payment.");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Create Payment</h1>
                    <p>Create a payment record for an active rental contract.</p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate("/payments")}
                >
                    <ArrowLeft size={17} />
                    Back to Payments
                </button>
            </div>

            <form onSubmit={handleSubmit} className="form-layout">
                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Payment Information</h2>
                        <p>Enter the payment details associated with a contract.</p>
                    </div>

                    {error && <div className="form-error">{error}</div>}

                    <div className="form-grid">
                        <div className="form-group full-width">
                            <label htmlFor="payment-contract">Contract</label>
                            <select
                                id="payment-contract"
                                value={contractId}
                                onChange={(event) => {
                                    setContractId(event.target.value);
                                    setAmount("");
                                }}
                                required
                                disabled={loading}
                            >
                                <option value="">
                                    {loading ? "Loading contracts..." : "Select an active contract"}
                                </option>
                                {contracts.map((contract) => (
                                    <option key={contract.id} value={contract.id}>
                                        #{contract.id} - {contract.tenant_name} - {contract.room_title}
                                    </option>
                                ))}
                            </select>
                            <small>
                                Only active contracts can receive new payment records.
                            </small>
                        </div>

                        {selectedContract && (
                            <div className="form-group full-width">
                                <div className="payment-selected-contract">
                                    <strong>{selectedContract.room_title}</strong>
                                    <span>{selectedContract.tenant_name} · {selectedContract.tenant_email}</span>
                                    <span>{selectedContract.room_address}</span>
                                    <span>
                                        Monthly rent: {Number(selectedContract.monthly_price).toLocaleString("vi-VN")} VND
                                    </span>
                                </div>
                            </div>
                        )}

                        <div className="form-group">
                            <label htmlFor="payment-amount">Amount (VND)</label>
                            <input
                                id="payment-amount"
                                type="number"
                                min="1"
                                step="1"
                                value={amount}
                                onChange={(event) => setAmount(event.target.value)}
                                placeholder="Enter amount"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="payment-type">Payment Type</label>
                            <select
                                id="payment-type"
                                value={paymentType}
                                onChange={(event) => {
                                    setPaymentType(event.target.value);
                                    if (event.target.value !== "RENT") {
                                        setPaymentPeriod("");
                                    }
                                }}
                            >
                                <option value="DEPOSIT">Deposit</option>
                                <option value="RENT">Rent</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>

                        {paymentType === "RENT" && (
                            <div className="form-group full-width">
                                <label htmlFor="payment-period">Payment Period</label>
                                <input
                                    id="payment-period"
                                    type="month"
                                    value={paymentPeriod}
                                    onChange={(event) => setPaymentPeriod(event.target.value)}
                                    required
                                />
                                <small>Required for rent payments. Use YYYY-MM format.</small>
                            </div>
                        )}

                        <div className="form-group full-width">
                            <label htmlFor="payment-note">Note</label>
                            <textarea
                                id="payment-note"
                                rows={4}
                                value={note}
                                onChange={(event) => setNote(event.target.value)}
                                placeholder="Enter payment note (optional)"
                            />
                        </div>
                    </div>
                </section>

                <div className="form-placeholder-notice">
                    <strong>Payment status</strong>
                    <p>
                        New payment records are automatically created with PENDING status.
                        You can update the status after creating the record.
                    </p>
                    <p>
                        This module records payment information only. It does not
                        transfer money or connect to an online payment gateway.
                    </p>
                </div>

                <div className="form-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/payments")}
                        disabled={saving}
                    >
                        <X size={17} />
                        Cancel
                    </button>

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={saving || loading || contracts.length === 0}
                    >
                        <Save size={17} />
                        {saving ? "Creating..." : "Create Payment"}
                    </button>
                </div>
            </form>
        </div>
    );
}