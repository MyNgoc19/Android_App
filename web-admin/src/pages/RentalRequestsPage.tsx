
import { useNavigate } from "react-router-dom";
import {
    Search,
    SlidersHorizontal,
    Eye,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import PlaceholderTable from "../components/common/PlaceholderTable";

export default function RentalRequestsPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <PageHeader
                title="Rental Requests"
                description="Review and manage room rental requests."
            />

            <div className="toolbar">
                <div className="search-placeholder">
                    <Search size={18} />
                    <span>Search rental requests...</span>
                </div>

                <button className="secondary-button" disabled>
                    <SlidersHorizontal size={17} />
                    Filters
                </button>
            </div>

            <PlaceholderTable
                columns={[
                    "Request ID",
                    "User",
                    "Room",
                    "Desired Move-in",
                    "Submitted At",
                    "Status",
                    "Actions",
                ]}
                emptyTitle="No rental requests to display"
                emptyDescription="Rental requests will appear here after connecting the Rental Requests API."
            />

            <div className="placeholder-note">
                <div>
                    <strong>Rental request management preview</strong>
                    <p>
                        Review rental request details and approve or reject
                        requests when the backend is connected.
                    </p>
                </div>

                <div className="placeholder-note-actions">
                    <button
                        className="secondary-button"
                        onClick={() =>
                            navigate("/rental-requests/preview")
                        }
                    >
                        <Eye size={16} />
                        Preview Request Detail
                    </button>
                </div>
            </div>
        </div>
    );
}
