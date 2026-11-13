
import { useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    Pencil,
    Image as ImageIcon,
} from "lucide-react";

import EmptyState from "../components/common/EmptyState";

export default function RoomDetailPage() {
    const navigate = useNavigate();

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>Room Details</h1>
                    <p>View room information, amenities and images.</p>
                </div>

                <div className="detail-header-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/rooms")}
                    >
                        <ArrowLeft size={17} aria-hidden="true" />
                        Back
                    </button>

                    <button
                        type="button"
                        className="primary-button"
                        onClick={() => navigate("/rooms/preview/edit")}
                    >
                        <Pencil size={17} aria-hidden="true" />
                        Edit Room
                    </button>
                </div>
            </div>

            <div className="form-placeholder-notice">
                This is a preview screen. No actual room record is loaded.
            </div>

            <div className="room-detail-grid">
                {/* MAIN ROOM INFORMATION */}
                <section className="content-card room-detail-main">
                    <div className="room-detail-image">
                        <ImageIcon size={42} aria-hidden="true" />
                        <span>Room image placeholder</span>
                    </div>

                    <div className="room-detail-content">
                        <div className="room-detail-title">
                            <div>
                                <h2>Room Information</h2>
                                <p>Room details will appear here.</p>
                            </div>

                            <span className="status-placeholder">
                                Status placeholder
                            </span>
                        </div>

                        <div className="detail-info-grid">
                            <div className="detail-info-item">
                                <span>Room Name (title)</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Monthly Price</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Area</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Capacity</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item full-width">
                                <span>Street Address</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item">
                                <span>Ward</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item">
                                <span>District</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item full-width">
                                <span>City</span>
                                <strong>Not available</strong>
                            </div>

                            <div className="detail-info-item full-width">
                                <span>Description</span>
                                <strong>Not available</strong>
                            </div>
                        </div>
                    </div>
                </section>

                {/* RIGHT SIDEBAR */}
                <div className="room-detail-sidebar">
                    <section className="content-card room-detail-side">
                        <div className="form-section-heading">
                            <h2>Amenities</h2>
                            <p>Room facilities and services.</p>
                        </div>

                        <EmptyState
                            title="No amenities available"
                            description="Amenities will appear here when room data is connected."
                        />
                    </section>

                    <section className="content-card room-detail-side">
                        <div className="form-section-heading">
                            <h2>Room Images</h2>
                            <p>Images associated with this room.</p>
                        </div>

                        <EmptyState
                            title="No images available"
                            description="Room images will appear here after connecting Firebase Storage and the room images API."
                        />
                    </section>
                </div>
            </div>

            <div className="form-placeholder-notice">
                Room images are stored in Firebase Storage.
                The backend stores image URLs and related image records,
                not the image binary files.
            </div>
        </div>
    );
}
