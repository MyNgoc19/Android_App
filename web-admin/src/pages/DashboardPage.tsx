
import {
    BarChart3,
    Users,
    DoorOpen,
    FileText,
} from "lucide-react";

import PageHeader from "../components/common/PageHeader";
import EmptyState from "../components/common/EmptyState";

const overviewItems = [
    {
        label: "Total Rooms",
        icon: DoorOpen,
    },
    {
        label: "Total Users",
        icon: Users,
    },
    {
        label: "Rental Requests",
        icon: FileText,
    },
    {
        label: "Revenue Overview",
        icon: BarChart3,
    },
];

export default function DashboardPage() {
    return (
        <div className="page-container">
            <PageHeader
                title="Dashboard"
                description="Overview of your rental management system."
            />

            {/* OVERVIEW CARDS */}
            <div className="dashboard-placeholder-grid">
                {overviewItems.map((item) => {
                    const Icon = item.icon;

                    return (
                        <div
                            className="dashboard-placeholder-card"
                            key={item.label}
                        >
                            <div className="placeholder-card-icon">
                                <Icon size={21} aria-hidden="true" />
                            </div>

                            <span>{item.label}</span>

                            <div
                                className="skeleton-value"
                                aria-hidden="true"
                            />

                            <small>
                                Waiting for backend integration
                            </small>
                        </div>
                    );
                })}
            </div>

            {/* SYSTEM OVERVIEW */}
            <div className="content-card dashboard-overview">
                <div className="section-heading">
                    <h2>System Overview</h2>
                    <span>Overview placeholder</span>
                </div>

                <EmptyState
                    title="Dashboard is not connected yet"
                    description="Statistics and recent activities will be displayed here after API integration."
                />
            </div>
        </div>
    );
}
