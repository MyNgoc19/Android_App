import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Outlet,
} from "react-router-dom";

import AdminLayout from "./components/layout/AdminLayout";
import LoginPage from "./pages/LoginPage";
import { useAuth } from "./contexts/useAuth";

import DashboardPage from "./pages/DashboardPage";

import RoomsPage from "./pages/RoomsPage";
import RoomFormPage from "./pages/RoomFormPage";
import RoomDetailPage from "./pages/RoomDetailPage";

import UsersPage from "./pages/UsersPage";
import UserDetailPage from "./pages/UserDetailPage";

import RentalRequestsPage from "./pages/RentalRequestsPage";
import RentalRequestDetailPage from "./pages/RentalRequestDetailPage";

import ContractsPage from "./pages/ContractsPage";
import ContractFormPage from "./pages/ContractFormPage";
import ContractDetailPage from "./pages/ContractDetailPage";

import PaymentsPage from "./pages/PaymentsPage";
import PaymentFormPage from "./pages/PaymentFormPage";
import PaymentDetailPage from "./pages/PaymentDetailPage";

function ProtectedRoute() {
  const { account, loading } = useAuth();

  if (loading) {
    return <div className="auth-loading">Checking administrator access...</div>;
  }

  if (!account) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}

function LoginRoute() {
  const { account, loading } = useAuth();

  if (loading) {
    return <div className="auth-loading">Checking login status...</div>;
  }

  if (account) {
    return <Navigate to="/" replace />;
  }

  return <LoginPage />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginRoute />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/" element={<DashboardPage />} />

            <Route path="/rooms" element={<RoomsPage />} />
            <Route path="/rooms/new" element={<RoomFormPage />} />
            <Route path="/rooms/:roomId" element={<RoomDetailPage />} />
            <Route path="/rooms/:roomId/edit" element={<RoomFormPage />} />

            <Route path="/users" element={<UsersPage />} />
            <Route path="/users/:userId" element={<UserDetailPage />} />

            <Route
              path="/rental-requests"
              element={<RentalRequestsPage />}
            />
            <Route
              path="/rental-requests/:requestId"
              element={<RentalRequestDetailPage />}
            />

            <Route path="/contracts" element={<ContractsPage />} />
            <Route path="/contracts/new" element={<ContractFormPage />} />
            <Route path="/contracts/:contractId" element={<ContractDetailPage />} />

            <Route path="/payments" element={<PaymentsPage />} />
            <Route path="/payments/new" element={<PaymentFormPage />} />
            <Route path="/payments/:paymentId" element={<PaymentDetailPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
