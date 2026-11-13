
import { useNavigate } from "react-router-dom";
import {
    Search,
    SlidersHorizontal,
    Eye,
    Pencil,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import PlaceholderTable from "../components/common/PlaceholderTable";

export default function RoomsPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <PageHeader
                title="Rooms"
                description="Manage rental rooms, room information and availability."
                actionLabel="Add Room"
                onAction={() => navigate("/rooms/new")}
            />

            {/* SEARCH AND FILTERS */}
            <div className="toolbar">
                <div className="search-placeholder">
                    <Search size={18} aria-hidden="true" />
                    <span>Search rooms...</span>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    disabled
                    title="Filters will be available after API integration"
                >
                    <SlidersHorizontal size={17} aria-hidden="true" />
                    Filters
                </button>
            </div>

            {/* ROOM TABLE */}
            <PlaceholderTable
                columns={[
                    "Room",
                    "Address",
                    "Monthly Price",
                    "Area",
                    "Capacity",
                    "Status",
                    "Actions",
                ]}
                emptyTitle="No rooms to display"
                emptyDescription="Room information will appear here after connecting the Rooms API."
            />

            {/* ROOM MANAGEMENT NOTE */}
            <div className="placeholder-note">
                <div>
                    <strong>Room management preview</strong>

                    <p>
                        Room records will include the room title, description,
                        full address, monthly price, area, capacity,
                        amenities and images.
                    </p>

                    <p>
                        Address information includes street address, ward,
                        district and city.
                    </p>
                </div>

                <div className="placeholder-note-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/rooms/preview")}
                    >
                        <Eye size={16} aria-hidden="true" />
                        Preview Detail
                    </button>

                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/rooms/preview/edit")}
                    >
                        <Pencil size={16} aria-hidden="true" />
                        Preview Edit
                    </button>
                </div>
            </div>

            <div className="form-placeholder-notice">
                <strong>Room status rules</strong>
                <p>
                    Admin can set a room to AVAILABLE or INACTIVE.
                    The backend automatically changes the room status to
                    RENTED when an active contract is created.
                </p>
            </div>
        </div>
    );
}
