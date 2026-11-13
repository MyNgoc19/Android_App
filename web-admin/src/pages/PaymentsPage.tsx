
import { useNavigate } from "react-router-dom";
import {
    Search,
    SlidersHorizontal,
    Eye,
    Plus,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import PlaceholderTable from "../components/common/PlaceholderTable";

export default function PaymentsPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <PageHeader
                title="Payments"
                description="Manage payment records and payment statuses."
                actionLabel="Create Payment"
                onAction={() => navigate("/payments/new")}
            />

            <div className="toolbar">
                <div className="search-placeholder">
                    <Search size={18} />
                    <span>Search payments...</span>
                </div>

                <button className="secondary-button" disabled>
                    <SlidersHorizontal size={17} />
                    Filters
                </button>
            </div>

            <PlaceholderTable
                columns={[
                    "Payment ID",
                    "Contract",
                    "Amount",
                    "Type",
                    "Payment Period",
                    "Status",
                    "Actions",
                ]}
                emptyTitle="No payments to display"
                emptyDescription="Payment records will appear here after connecting the Payments API."
            />

            <div className="placeholder-note">
                <div>
                    <strong>Payment management preview</strong>
                    <p>
                        Create payment records for rental contracts,
                        review payment details and update payment statuses.
                    </p>
                    <p>
                        This module only records and manages payment
                        information. Online payment gateway integration
                        is not included in the MVP.
                    </p>
                </div>

                <div className="placeholder-note-actions">
                    <button
                        className="secondary-button"
                        onClick={() => navigate("/payments/preview")}
                    >
                        <Eye size={16} />
                        Preview Detail
                    </button>

                    <button
                        className="secondary-button"
                        onClick={() => navigate("/payments/new")}
                    >
                        <Plus size={16} />
                        Preview Create
                    </button>
                </div>
            </div>
        </div>
    );
}
