import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import '../styles/pages/ForgotPasswordPage.css';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    if (!email) {
      toast.error('이메일을 입력해주세요');
      setIsLoading(false);
      return;
    }

    // 시뮬레이션: 이메일 확인
    // 실제 구현 시 Supabase resetPasswordForEmail로 교체
    try {
      const users = JSON.parse(localStorage.getItem('users') || '[]');
      const user = users.find((u: any) => u.email === email);

      if (user) {
        setEmailSent(true);
        toast.success('비밀번호 재설정 이메일이 전송되었습니다');
      } else {
        toast.error('등록되지 않은 이메일입니다');
      }
    } catch (error) {
      toast.error('오류가 발생했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="forgot-page">
      <div className="forgot-wrapper">
        {/* Logo */}
        <div className="forgot-logo">
          <Wallet className="forgot-logo-icon" />
          <span className="forgot-logo-text">내 가계부</span>
        </div>

        {/* Forgot Password Card */}
        <Card className="forgot-card">
          <CardHeader className="forgot-card-header">
            <CardTitle className="forgot-card-title">비밀번호 찾기</CardTitle>
            <CardDescription>
              {emailSent
                ? '이메일을 확인해주세요'
                : '가입하신 이메일을 입력하시면 비밀번호 재설정 링크를 보내드립니다'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {!emailSent ? (
              <form onSubmit={handleResetPassword} className="forgot-form">
                <div className="forgot-field-group">
                  <Label htmlFor="email">이메일</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>

                <Button type="submit" className="forgot-submit-btn" disabled={isLoading}>
                  {isLoading ? '처리 중...' : '비밀번호 재설정 이메일 전송'}
                </Button>
              </form>
            ) : (
              <div className="forgot-success-section">
                <div className="forgot-success-alert">
                  <p>
                    <strong>{email}</strong>로 비밀번호 재설정 링크를 전송했습니다.
                    이메일을 확인하고 링크를 클릭하여 비밀번호를 재설정해주세요.
                  </p>
                </div>

                <Button
                  variant="outline"
                  className="forgot-success-btn"
                  onClick={() => navigate('/login')}
                >
                  로그인 페이지로 돌아가기
                </Button>
              </div>
            )}

            <div className="forgot-back-wrapper">
              <Link to="/login">
                <Button variant="ghost" className="forgot-back-btn">
                  <ArrowLeft className="forgot-back-icon" />
                  로그인 페이지로 돌아가기
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
