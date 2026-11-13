
import EmptyState from "./EmptyState";

type PlaceholderTableProps = {
    columns: string[];
    emptyTitle?: string;
    emptyDescription?: string;
};

export default function PlaceholderTable({
    columns,
    emptyTitle,
    emptyDescription,
}: PlaceholderTableProps) {
    return (
        <div className="content-card">
            <div className="table-wrapper">
                <table className="data-table">
                    <thead>
                        <tr>
                            {columns.map((column, index) => (
                                <th
                                    key={`${column}-${index}`}
                                    scope="col"
                                >
                                    {column}
                                </th>
                            ))}
                        </tr>
                    </thead>
                </table>
            </div>

            <EmptyState
                title={emptyTitle}
                description={emptyDescription}
            />
        </div>
    );
}
