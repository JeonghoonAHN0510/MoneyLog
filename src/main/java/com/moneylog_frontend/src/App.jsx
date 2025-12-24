import { Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './components/common/PrivateRoute';

// TODO: 나중에 실제 페이지 컴포넌트를 import 하세요.
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import LedgerPage from './pages/LedgerPage';
import DashboardPage from './pages/DashboardPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header나 Sidebar 같은 공통 레이아웃을 여기에 둘 수 있습니다. 
        <Header /> 
      */}

      <Routes>
        {/* 기본 경로 접속 시 로그인 페이지로 리다이렉트하거나 홈으로 이동 */}
        <Route path="/" element={<HomePage />} />

        {/* 인증 관련 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        {/* 가계부 기능 (나중에 PrivateRoute로 감싸서 보호해야 함) */}
        <Route
          path="/ledger"
          element={
            <PrivateRoute>
              <LedgerPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <DashboardPage />
            </PrivateRoute>
          }
        />

        {/* 없는 페이지 처리 */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </div>
  );
}

export default App;