
import { Inbox } from "lucide-react";

type EmptyStateProps = {
    title?: string;
    description?: string;
};

export default function EmptyState({
    title = "No data available",
    description = "Data will appear here after the backend is connected.",
}: EmptyStateProps) {
    return (
        <div className="empty-state" role="status">
            <div className="empty-state-icon">
                <Inbox size={28} aria-hidden="true" />
            </div>

            <h3>{title}</h3>

            <p>{description}</p>
        </div>
    );
}
