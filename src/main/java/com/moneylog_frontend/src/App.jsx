import { Routes, Route, Navigate } from 'react-router-dom';

// TODO: 나중에 실제 페이지 컴포넌트를 import 하세요.
// import LoginPage from './pages/LoginPage';
// import SignupPage from './pages/SignupPage';
// import LedgerPage from './pages/LedgerPage';

// [임시 페이지 컴포넌트] 테스트용
const Home = () => <div className="p-4 text-2xl font-bold">🏠 머니로그 홈</div>;
const Login = () => <div className="p-4">🔑 로그인 페이지</div>;
const Signup = () => <div className="p-4">📝 회원가입 페이지</div>;
const Ledger = () => <div className="p-4">💰 가계부 메인 페이지</div>;
const NotFound = () => <div className="p-4 text-red-500">404 페이지를 찾을 수 없습니다.</div>;

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header나 Sidebar 같은 공통 레이아웃을 여기에 둘 수 있습니다. 
        <Header /> 
      */}

      <Routes>
        {/* 기본 경로 접속 시 로그인 페이지로 리다이렉트하거나 홈으로 이동 */}
        <Route path="/" element={<Home />} />
        
        {/* 인증 관련 */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* 가계부 기능 (나중에 PrivateRoute로 감싸서 보호해야 함) */}
        <Route path="/ledger" element={<Ledger />} />

        {/* 없는 페이지 처리 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </div>
  );
}

export default App;