import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, ArrowLeft } from 'lucide-react';
import '../styles/pages/ForgotPasswordPage.css';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  return (
    <div className="forgot-page">
      <div className="forgot-wrapper">
        <div className="forgot-logo">
          <Wallet className="forgot-logo-icon" />
          <span className="forgot-logo-text">내 가계부</span>
        </div>

        <Card className="forgot-card">
          <CardHeader className="forgot-card-header">
            <CardTitle className="forgot-card-title">비밀번호 찾기</CardTitle>
            <CardDescription>
              현재 비밀번호 재설정 기능은 준비 중입니다.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="forgot-success-section">
              <div className="forgot-success-alert">
                <p>
                  운영용 비밀번호 재설정 API가 아직 준비되지 않았습니다.
                  계정 관련 도움이 필요하면 관리자에게 문의하거나 로그인 페이지로 돌아가 주세요.
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

            <div className="forgot-back-wrapper">
              <Button asChild variant="ghost" className="forgot-back-btn">
                <Link to="/login">
                  <ArrowLeft className="forgot-back-icon" />
                  로그인 페이지로 돌아가기
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
