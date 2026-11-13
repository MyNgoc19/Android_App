
import { useLocation, useNavigate } from "react-router-dom";
import {
    ArrowLeft,
    ImagePlus,
    Save,
    X,
} from "lucide-react";

export default function RoomFormPage() {
    const navigate = useNavigate();
    const location = useLocation();

    const isEdit = location.pathname.endsWith("/edit");

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1>{isEdit ? "Edit Room" : "Add New Room"}</h1>
                    <p>
                        {isEdit
                            ? "Update room information."
                            : "Create a new rental room."}
                    </p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => navigate("/rooms")}
                >
                    <ArrowLeft size={17} aria-hidden="true" />
                    Back to Rooms
                </button>
            </div>

            <div className="form-placeholder-notice">
                This is a UI preview. Room information is not saved yet.
            </div>

            <div className="form-layout">
                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Basic Information</h2>
                        <p>Enter general information about the room.</p>
                    </div>

                    <div className="form-grid">
                        <div className="form-group full-width">
                            <label htmlFor="room-title">Room Name</label>
                            <input
                                id="room-title"
                                type="text"
                                placeholder="Enter room name"
                            />
                            <small>
                                API field: title.
                            </small>
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-address">Address</label>
                            <input
                                id="room-address"
                                type="text"
                                placeholder="Enter street address"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-ward">Ward</label>
                            <input
                                id="room-ward"
                                type="text"
                                placeholder="Enter ward"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-district">District</label>
                            <input
                                id="room-district"
                                type="text"
                                placeholder="Enter district"
                            />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-city">City</label>
                            <input
                                id="room-city"
                                type="text"
                                placeholder="Enter city"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-price">
                                Monthly Price (VND)
                            </label>
                            <input
                                id="room-price"
                                type="number"
                                min="0"
                                step="1"
                                placeholder="Enter monthly price"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-area">Area (m²)</label>
                            <input
                                id="room-area"
                                type="number"
                                min="0"
                                step="0.1"
                                placeholder="Enter room area"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="room-capacity">Capacity</label>
                            <input
                                id="room-capacity"
                                type="number"
                                min="1"
                                step="1"
                                placeholder="Enter maximum capacity"
                            />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-description">
                                Description
                            </label>
                            <textarea
                                id="room-description"
                                rows={5}
                                placeholder="Enter room description"
                            />
                        </div>

                        <div className="form-group full-width">
                            <label htmlFor="room-amenities">Amenities</label>
                            <input
                                id="room-amenities"
                                type="text"
                                placeholder="Example: Air conditioner, Wi-Fi, Parking"
                            />
                            <small>
                                Amenities will be converted into an array of
                                strings when connected to the backend.
                            </small>
                        </div>
                    </div>
                </section>

                <section className="content-card form-section">
                    <div className="form-section-heading">
                        <h2>Room Images</h2>
                        <p>Preview the room image upload area.</p>
                    </div>

                    <div className="image-upload-placeholder">
                        <ImagePlus size={32} aria-hidden="true" />

                        <strong>Upload room images</strong>

                        <span>
                            Images will be uploaded to Firebase Storage.
                            The backend will only store image URLs.
                        </span>

                        <button
                            type="button"
                            className="secondary-button"
                            disabled
                        >
                            Choose Images
                        </button>
                    </div>
                </section>

                <div className="form-placeholder-notice">
                    Room status is managed through a separate API.
                    Admin can set AVAILABLE or INACTIVE.
                    The backend automatically sets RENTED when an active
                    contract is created.
                </div>

                <div className="form-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/rooms")}
                    >
                        <X size={17} aria-hidden="true" />
                        Cancel
                    </button>

                    <button
                        type="button"
                        className="primary-button"
                        disabled
                    >
                        <Save size={17} aria-hidden="true" />
                        {isEdit ? "Save Changes" : "Create Room"}
                    </button>
                </div>
            </div>
        </div>
    );
}
