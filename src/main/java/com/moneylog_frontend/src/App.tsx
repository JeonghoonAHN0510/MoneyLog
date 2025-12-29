import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from './components/ui/sonner';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import FinancePage from './pages/FinancePage';
import PrivateRoute from './components/PrivateRoute'
import './styles/index.css';

export default function App() {
  return (
    <>
      <Toaster />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/finance" element={<PrivateRoute><FinancePage /> </PrivateRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}