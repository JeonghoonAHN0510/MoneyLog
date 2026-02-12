import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, BarChart3, Calendar, Calculator, Target, ArrowRight } from 'lucide-react';
import '../styles/pages/HomePage.css';

export default function HomePage() {
  const navigate = useNavigate();

  const features = [
    {
      icon: BarChart3,
      title: '직관적인 대시보드',
      description: '수입과 지출을 한눈에 파악할 수 있는 시각화된 리포트',
    },
    {
      icon: Calendar,
      title: '캘린더 뷰',
      description: '월별/일별 거래 내역을 달력에서 쉽게 확인',
    },
    {
      icon: Target,
      title: '예산 관리',
      description: '카테고리별 예산 설정 및 지출 경고 알림',
    },
    {
      icon: Calculator,
      title: '실수령액 계산기',
      description: '4대보험 공제를 고려한 정확한 실수령액 계산',
    },
  ];

  return (
    <div className="home-page">
      {/* Header */}
      <header className="home-header">
        <div className="home-header-inner">
          <div className="home-header-logo">
            <Wallet className="home-header-logo-icon" />
            <span className="home-header-logo-text">내 가계부</span>
          </div>
          <div className="home-header-actions">
            <Button variant="outline" size="sm" className="home-header-login-btn" onClick={() => navigate('/login')}>
              로그인
            </Button>
            <Button size="sm" className="home-header-signup-btn" onClick={() => navigate('/signup')}>
              시작하기
            </Button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="home-hero">
        <div className="home-hero-inner">
          <h1 className="home-hero-title">
            사회 초년생을 위한
            <br />
            <span className="home-hero-highlight">스마트 재무 관리</span>
          </h1>
          <p className="home-hero-subtitle">
            복잡한 가계부는 이제 그만! 직관적이고 안전한 나만의 재무 관리 시스템으로
            <br />
            돈의 흐름을 한눈에 파악하세요.
          </p>
          <div className="home-hero-actions">
            <Button size="lg" onClick={() => navigate('/signup')} className="home-hero-cta-btn">
              무료로 시작하기
              <ArrowRight className="home-hero-cta-icon" />
            </Button>
            <Button size="lg" variant="outline" onClick={() => navigate('/login')}>
              로그인
            </Button>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="home-features">
        <div className="home-features-header">
          <h2 className="home-features-title">
            왜 내 가계부를 선택해야 할까요?
          </h2>
          <p className="home-features-subtitle">
            광고 없는 깔끔한 UI와 개인정보 보호를 최우선으로 합니다
          </p>
        </div>

        <div className="home-features-grid">
          {features.map((feature, index) => (
            <Card key={index} className="home-feature-card">
              <CardHeader>
                <div className="home-feature-icon-wrapper">
                  <feature.icon className="home-feature-icon" />
                </div>
                <CardTitle className="home-feature-title">{feature.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="home-feature-description">{feature.description}</CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Benefits Section */}
      <section className="home-benefits">
        <div className="home-benefits-container">
          <div className="home-benefits-inner">
            <div className="home-benefits-header">
              <h2 className="home-benefits-title">이런 분들께 추천합니다</h2>
            </div>

            <div className="home-benefits-grid">
              <div className="home-benefit-item">
                <div className="home-benefit-icon">
                  <span className="home-benefit-emoji">💼</span>
                </div>
                <h3 className="home-benefit-title">사회 초년생</h3>
                <p className="home-benefit-description">
                  첫 월급을 받고 재무 관리를 시작하려는 당신에게 딱 맞는 도구
                </p>
              </div>

              <div className="home-benefit-item">
                <div className="home-benefit-icon">
                  <span className="home-benefit-emoji">🎯</span>
                </div>
                <h3 className="home-benefit-title">목표 저축러</h3>
                <p className="home-benefit-description">
                  체계적인 예산 관리로 저축 목표를 달성하고 싶은 분
                </p>
              </div>

              <div className="home-benefit-item">
                <div className="home-benefit-icon">
                  <span className="home-benefit-emoji">🔒</span>
                </div>
                <h3 className="home-benefit-title">프라이버시 중시자</h3>
                <p className="home-benefit-description">
                  개인정보 보안과 광고 없는 환경을 원하는 분
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="home-cta">
        <div className="home-cta-inner">
          <h2 className="home-cta-title">
            지금 바로 시작하세요
          </h2>
          <p className="home-cta-subtitle">
            무료로 시작하고, 당신의 재무 목표를 달성하세요
          </p>
          <Button size="lg" onClick={() => navigate('/signup')} className="home-cta-btn">
            무료 회원가입
            <ArrowRight className="home-cta-btn-icon" />
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="home-footer">
        <div className="home-footer-inner">
          <div className="home-footer-logo">
            <Wallet className="home-footer-logo-icon" />
            <span className="home-footer-logo-text">내 가계부</span>
          </div>
          <p>© 2024 내 가계부. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
