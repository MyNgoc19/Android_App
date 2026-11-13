
type PageHeaderProps = {
    title: string;
    description: string;
    actionLabel?: string;
    onAction?: () => void;
};

export default function PageHeader({
    title,
    description,
    actionLabel,
    onAction,
}: PageHeaderProps) {
    return (
        <div className="page-header">
            <div>
                <h1>{title}</h1>
                <p>{description}</p>
            </div>

            {actionLabel && (
                <button
                    type="button"
                    className="primary-button"
                    onClick={onAction}
                    disabled={!onAction}
                    aria-label={actionLabel}
                >
                    {actionLabel}
                </button>
            )}
        </div>
    );
}
