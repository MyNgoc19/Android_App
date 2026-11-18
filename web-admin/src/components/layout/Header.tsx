
import {
    Search,
    Sun,
    Moon,
    Bell,
    ChevronDown,
    LogOut,
} from "lucide-react";

import { useTheme } from "../../contexts/useTheme";
import { useAuth } from "../../contexts/useAuth";

export default function Header() {
    const { theme, toggleTheme } = useTheme();
    const { account, logout } = useAuth();

    return (
        <header className="header">
            <div className="header-search">
                <Search size={18} aria-hidden="true" />
                <input
                    type="text"
                    placeholder="Search..."
                    disabled
                    aria-label="Search"
                />
            </div>

            <div className="header-actions">
                <button
                    type="button"
                    className="theme-toggle"
                    onClick={toggleTheme}
                    title={
                        theme === "light"
                            ? "Switch to dark mode"
                            : "Switch to light mode"
                    }
                    aria-label={
                        theme === "light"
                            ? "Switch to dark mode"
                            : "Switch to light mode"
                    }
                >
                    {theme === "light" ? (
                        <Moon size={19} />
                    ) : (
                        <Sun size={19} />
                    )}
                </button>

                <button
                    type="button"
                    className="notification-button"
                    disabled
                    aria-label="Notifications"
                    title="Notifications - Coming soon"
                >
                    <Bell size={20} />
                    <span className="notification-dot" />
                </button>

                <div className="header-profile">
                    <div className="profile-avatar">
                        {account?.full_name?.charAt(0).toUpperCase() || "A"}
                    </div>

                    <div className="profile-text">
                        <strong>{account?.full_name || "Administrator"}</strong>
                        <span>{account?.email || "Admin"}</span>
                    </div>

                    <ChevronDown size={15} />
                </div>

                <button
                    type="button"
                    className="theme-toggle"
                    onClick={() => void logout()}
                    title="Sign out"
                    aria-label="Sign out"
                >
                    <LogOut size={19} />
                </button>
            </div>
        </header>
    );
}
