
import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    DoorOpen,
    Users,
    ClipboardList,
    FileText,
    CreditCard,
    Zap,
    BarChart3,
    Home,
} from "lucide-react";

const mainMenu = [
    {
        label: "Dashboard",
        path: "/",
        icon: LayoutDashboard,
    },
    {
        label: "Rooms",
        path: "/rooms",
        icon: DoorOpen,
    },
    {
        label: "Users",
        path: "/users",
        icon: Users,
    },
    {
        label: "Rental Requests",
        path: "/rental-requests",
        icon: ClipboardList,
    },
    {
        label: "Contracts",
        path: "/contracts",
        icon: FileText,
    },
    {
        label: "Payments",
        path: "/payments",
        icon: CreditCard,
    },
];

const futureMenu = [
    {
        label: "Utility Billing",
        icon: Zap,
    },
    {
        label: "Reports & Analytics",
        icon: BarChart3,
    },
];

export default function Sidebar() {
    return (
        <aside className="sidebar">
            {/* BRAND */}
            <div className="sidebar-brand">
                <div className="brand-icon">
                    <Home size={23} />
                </div>

                <div>
                    <h2>Room Rental</h2>
                    <span>Management System</span>
                </div>
            </div>

            {/* MAIN MENU */}
            <div className="sidebar-section">
                <p className="sidebar-heading">MAIN MENU</p>

                <nav className="sidebar-nav" aria-label="Main navigation">
                    {mainMenu.map((item) => {
                        const Icon = item.icon;

                        return (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                end={item.path === "/"}
                                title={item.label}
                                className={({ isActive }) =>
                                    `nav-item ${isActive ? "active" : ""}`
                                }
                            >
                                <Icon size={19} />
                                <span>{item.label}</span>
                            </NavLink>
                        );
                    })}
                </nav>
            </div>

            {/* FUTURE FEATURES */}
            <div className="sidebar-section">
                <p className="sidebar-heading">FUTURE FEATURES</p>

                <nav
                    className="sidebar-nav"
                    aria-label="Future features"
                >
                    {futureMenu.map((item) => {
                        const Icon = item.icon;

                        return (
                            <div
                                className="nav-item disabled"
                                key={item.label}
                                aria-disabled="true"
                                title="Coming soon"
                            >
                                <Icon size={19} />
                                <span>{item.label}</span>
                                <small className="coming-soon-tag">
                                    Soon
                                </small>
                            </div>
                        );
                    })}
                </nav>
            </div>

            {/* SIDEBAR FOOTER */}
            <div className="sidebar-footer">
                <div className="admin-avatar">A</div>

                <div className="admin-info">
                    <strong>Administrator</strong>
                    <span>Admin Account</span>
                </div>
            </div>
        </aside>
    );
}
