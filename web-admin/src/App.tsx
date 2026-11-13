
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import AdminLayout from "./components/layout/AdminLayout";

// Dashboard
import DashboardPage from "./pages/DashboardPage";

// Rooms
import RoomsPage from "./pages/RoomsPage";
import RoomFormPage from "./pages/RoomFormPage";
import RoomDetailPage from "./pages/RoomDetailPage";

// Users
import UsersPage from "./pages/UsersPage";
import UserDetailPage from "./pages/UserDetailPage";

// Rental Requests
import RentalRequestsPage from "./pages/RentalRequestsPage";
import RentalRequestDetailPage from "./pages/RentalRequestDetailPage";

// Contracts
import ContractsPage from "./pages/ContractsPage";
import ContractFormPage from "./pages/ContractFormPage";
import ContractDetailPage from "./pages/ContractDetailPage";

// Payments
import PaymentsPage from "./pages/PaymentsPage";
import PaymentFormPage from "./pages/PaymentFormPage";
import PaymentDetailPage from "./pages/PaymentDetailPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AdminLayout />}>
          {/* Dashboard */}
          <Route path="/" element={<DashboardPage />} />

          {/* Rooms */}
          <Route path="/rooms" element={<RoomsPage />} />
          <Route path="/rooms/new" element={<RoomFormPage />} />
          <Route
            path="/rooms/preview"
            element={<RoomDetailPage />}
          />
          <Route
            path="/rooms/preview/edit"
            element={<RoomFormPage />}
          />

          {/* Users */}
          <Route path="/users" element={<UsersPage />} />
          <Route
            path="/users/preview"
            element={<UserDetailPage />}
          />

          {/* Rental Requests */}
          <Route
            path="/rental-requests"
            element={<RentalRequestsPage />}
          />
          <Route
            path="/rental-requests/preview"
            element={<RentalRequestDetailPage />}
          />

          {/* Contracts */}
          <Route path="/contracts" element={<ContractsPage />} />
          <Route
            path="/contracts/new"
            element={<ContractFormPage />}
          />
          <Route
            path="/contracts/preview"
            element={<ContractDetailPage />}
          />

          {/* Payments */}
          <Route path="/payments" element={<PaymentsPage />} />
          <Route
            path="/payments/new"
            element={<PaymentFormPage />}
          />
          <Route
            path="/payments/preview"
            element={<PaymentDetailPage />}
          />

          {/* Fallback */}
          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
