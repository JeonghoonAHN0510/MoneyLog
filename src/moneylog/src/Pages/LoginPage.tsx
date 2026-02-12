import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';
import api from '../api/axiosConfig';
import useUserStore from '../stores/authStore';
import '../styles/pages/LoginPage.css';

export default function LoginPage() {
  const login = useUserStore((state) => state.login);
  const navigate = useNavigate();
  const [id, setId] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    // 간단한 클라이언트 검증
    if (!id || !password) {
      setError('아이디와 비밀번호를 입력해주세요');
      setIsLoading(false);
      return;
    }

    try {
      const user = { id, password };
      const response = await api.post('/user/login', user);
      const data = await response.data;
      if (data) {
        login(data.accessToken);
        toast.success('로그인 성공!');
        navigate('/finance');
      }
    } catch (error: any) {
      if (error.response && error.response.status === 401) {
        setError('아이디 또는 비밀번호가 일치하지 않습니다.');
      } else {
        setError('로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      toast.error('로그인에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-wrapper">
        {/* Logo */}
        <div className="login-logo">
          <Wallet className="login-logo-icon" />
          <span className="login-logo-text">내 가계부</span>
        </div>

        {/* Login Card */}
        <Card className="login-card">
          <CardHeader className="login-card-header">
            <CardTitle className="login-card-title">로그인</CardTitle>
            <CardDescription>
              아이디와 비밀번호를 입력하여 로그인하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="login-form" noValidate>
              {error && (
                <div className="login-error-alert">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10" /><line x1="12" x2="12" y1="8" y2="12" /><line x1="12" x2="12.01" y1="16" y2="16" /></svg>
                  {error}
                </div>
              )}
              <div className="login-field-group">
                <Label htmlFor="id">아이디</Label>
                <Input
                  id="id"
                  type="text"
                  placeholder="your_id"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                  required
                />
              </div>

              <div className="login-field-group">
                <Label htmlFor="password">비밀번호</Label>
                <div className="login-password-wrapper">
                  <Input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="login-password-toggle"
                  >
                    {showPassword ? (
                      <EyeOff className="login-password-toggle-icon" />
                    ) : (
                      <Eye className="login-password-toggle-icon" />
                    )}
                  </button>
                </div>
              </div>

              <div className="login-actions">
                <Link
                  to="/forgot-password"
                  className="login-forgot-link"
                >
                  비밀번호를 잊으셨나요?
                </Link>
              </div>

              <Button type="submit" className="login-submit-btn" disabled={isLoading}>
                {isLoading ? '로그인 중...' : '로그인'}
              </Button>
            </form>

            <div className="login-signup-prompt">
              <span className="login-signup-prompt-text">계정이 없으신가요? </span>
              <Link
                to="/signup"
                className="login-signup-link"
              >
                회원가입
              </Link>
            </div>

            <div className="login-home-btn-wrapper">
              <Button
                variant="outline"
                className="login-home-btn"
                onClick={() => navigate('/')}
              >
                홈으로 돌아가기
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Demo Info */}
        <div className="login-demo-info">
          <p>
            <strong>데모 계정:</strong> 아직 회원가입하지 않으셨다면, 먼저 회원가입을 진행해주세요.
          </p>
        </div>
      </div>
    </div>
  );
}
