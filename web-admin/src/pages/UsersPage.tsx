
import { useNavigate } from "react-router-dom";
import {
    Search,
    SlidersHorizontal,
    Eye,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import PlaceholderTable from "../components/common/PlaceholderTable";

export default function UsersPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <PageHeader
                title="Users"
                description="Manage user accounts, roles and access status."
            />

            {/* SEARCH AND FILTERS */}
            <div className="toolbar">
                <div className="search-placeholder">
                    <Search size={18} />
                    <span>Search users...</span>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    disabled
                    title="Filters will be available after API integration"
                >
                    <SlidersHorizontal size={17} />
                    Filters
                </button>
            </div>

            {/* USER TABLE */}
            <PlaceholderTable
                columns={[
                    "User",
                    "Email",
                    "Phone",
                    "Role",
                    "Status",
                    "Created At",
                    "Actions",
                ]}
                emptyTitle="No users to display"
                emptyDescription="User accounts will appear here after connecting the Users API."
            />

            {/* USER MANAGEMENT NOTE */}
            <div className="placeholder-note">
                <div>
                    <strong>User management preview</strong>

                    <p>
                        User records will include account information,
                        role, status and creation date.
                    </p>

                    <p>
                        Admin can view account details and manage
                        account access through the available user APIs.
                    </p>
                </div>

                <div className="placeholder-note-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/users/preview")}
                    >
                        <Eye size={16} />
                        Preview User Detail
                    </button>
                </div>
            </div>

            <div className="form-placeholder-notice">
                <strong>Role management rule</strong>
                <p>
                    New accounts receive the USER role by default.
                    ADMIN privileges must be assigned through a trusted
                    process. The frontend must not determine or modify
                    user roles by itself.
                </p>
            </div>
        </div>
    );
}
