
import {
    Search,
    Sun,
    Moon,
    Bell,
    ChevronDown,
} from "lucide-react";

import { useTheme } from "../../contexts/useTheme";

export default function Header() {
    const { theme, toggleTheme } = useTheme();

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
                    <div className="profile-avatar">A</div>
                    <div className="profile-text">
                        <strong>Administrator</strong>
                        <span>Admin</span>
                    </div>
                    <ChevronDown size={15} />
                </div>
            </div>
        </header>
    );
}
