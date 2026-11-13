
import { useNavigate } from "react-router-dom";
import {
    Search,
    SlidersHorizontal,
    Eye,
    Plus,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import PlaceholderTable from "../components/common/PlaceholderTable";

export default function ContractsPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <PageHeader
                title="Contracts"
                description="Manage rental contracts and their lifecycle."
                actionLabel="Create Contract"
                onAction={() => navigate("/contracts/new")}
            />

            <div className="toolbar">
                <div className="search-placeholder">
                    <Search size={18} />
                    <span>Search contracts...</span>
                </div>

                <button className="secondary-button" disabled>
                    <SlidersHorizontal size={17} />
                    Filters
                </button>
            </div>

            <PlaceholderTable
                columns={[
                    "Contract ID",
                    "Tenant",
                    "Room",
                    "Start Date",
                    "End Date",
                    "Status",
                    "Actions",
                ]}
                emptyTitle="No contracts to display"
                emptyDescription="Rental contracts will appear here after connecting the Contracts API."
            />

            <div className="placeholder-note">
                <div>
                    <strong>Contract management preview</strong>
                    <p>
                        View contract information, create contracts from
                        approved rental requests, and manage contract status.
                        Active contracts can be ended or cancelled.
                    </p>
                </div>

                <div className="placeholder-note-actions">
                    <button
                        className="secondary-button"
                        onClick={() => navigate("/contracts/preview")}
                    >
                        <Eye size={16} />
                        Preview Detail
                    </button>

                    <button
                        className="secondary-button"
                        onClick={() => navigate("/contracts/new")}
                    >
                        <Plus size={16} />
                        Preview Create
                    </button>
                </div>
            </div>
        </div>
    );
}
