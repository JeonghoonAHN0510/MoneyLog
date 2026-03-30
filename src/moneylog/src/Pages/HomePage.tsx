import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, BarChart3, Calendar, Target, ArrowRight, Upload, ShieldCheck, Sparkles } from 'lucide-react';
import { useDocumentTitle } from '../hooks/useDocumentTitle';
import '../styles/pages/HomePage.css';

export default function HomePage() {
  const navigate = useNavigate();

  useDocumentTitle('MoneyLog | 가계부');

  const features = [
    {
      icon: BarChart3,
      title: '월간 자산 브리핑',
      description: '수입, 지출, 순자산 변화를 프리미엄 대시보드에서 빠르게 확인',
    },
    {
      icon: Calendar,
      title: '캘린더 추적',
      description: '날짜별 현금흐름과 거래 기록을 일정처럼 살펴보는 화면',
    },
    {
      icon: Target,
      title: '예산 감시',
      description: '카테고리별 예산 사용률과 잔여 여력을 한눈에 점검',
    },
    {
      icon: Upload,
      title: '거래 업로드',
      description: 'CSV와 Excel 업로드로 기존 거래 내역을 빠르게 정리',
    },
  ];

  return (
    <div className="home-page">
      {/* Header */}
      <header className="home-header">
        <div className="home-header-inner">
          <div className="home-header-logo">
            <Wallet className="home-header-logo-icon" />
            <span className="home-header-logo-text">MoneyLog</span>
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
          <div className="home-hero-copy">
            <div className="home-hero-badge">
              <Sparkles className="home-hero-badge-icon" />
              프리미엄 개인 금융 워크스페이스
            </div>
            <h1 className="home-hero-title">
              푸른 금융 대시보드로
              <br />
              <span className="home-hero-highlight">내 자산 흐름을 선명하게</span>
            </h1>
            <p className="home-hero-subtitle">
              수입, 지출, 예산, 계좌 관리를 하나의 흐름으로 정리하는
              현대적인 개인 금융 관리 화면을 제공합니다.
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
            <div className="home-hero-meta">
              <div className="home-hero-meta-item">
                <ShieldCheck className="home-hero-meta-icon" />
                광고 없는 개인 금융 공간
              </div>
              <div className="home-hero-meta-item">
                <Upload className="home-hero-meta-icon" />
                거래 업로드와 일상 관리 연동
              </div>
            </div>
          </div>

          <div className="home-hero-showcase">
            <div className="home-showcase-card">
              <div className="home-showcase-top">
                <div>
                  <p className="home-showcase-kicker">MONTHLY SNAPSHOT</p>
                  <h2 className="home-showcase-title">3월 자산 브리핑</h2>
                </div>
                <div className="home-showcase-chip">실시간 정리</div>
              </div>

              <div className="home-showcase-balance">
                <span className="home-showcase-balance-label">순자산 변동</span>
                <strong className="home-showcase-balance-value">+1,280,000원</strong>
              </div>

              <div className="home-showcase-grid">
                <div className="home-showcase-metric">
                  <span>수입</span>
                  <strong>4,250,000원</strong>
                </div>
                <div className="home-showcase-metric">
                  <span>지출</span>
                  <strong>2,970,000원</strong>
                </div>
                <div className="home-showcase-metric">
                  <span>예산 사용률</span>
                  <strong>72%</strong>
                </div>
                <div className="home-showcase-metric">
                  <span>현금흐름 상태</span>
                  <strong>안정적</strong>
                </div>
              </div>

              <div className="home-showcase-footer">
                <div className="home-showcase-track">
                  <span className="home-showcase-track-label">지출 관리 집중도</span>
                  <div className="home-showcase-progress">
                    <div className="home-showcase-progress-fill" />
                  </div>
                </div>
                <p className="home-showcase-note">거래, 예산, 계좌 흐름을 하나의 리듬으로 연결</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="home-features">
        <div className="home-features-header">
          <h2 className="home-features-title">
            금융 흐름에 집중한 핵심 기능
          </h2>
          <p className="home-features-subtitle">
            시각 계층과 데이터 강조를 정리해 자산 관리에 바로 집중할 수 있게 구성합니다
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
                <h3 className="home-benefit-title">첫 월급 관리</h3>
                <p className="home-benefit-description">
                  수입과 지출을 분리해서 보고 싶은 사용자에게 맞는 금융 워크플로우
                </p>
              </div>

              <div className="home-benefit-item">
                <div className="home-benefit-icon">
                  <span className="home-benefit-emoji">🎯</span>
                </div>
                <h3 className="home-benefit-title">목표 기반 저축</h3>
                <p className="home-benefit-description">
                  카테고리 예산과 자산 흐름을 함께 보며 저축 전략을 세우고 싶은 분
                </p>
              </div>

              <div className="home-benefit-item">
                <div className="home-benefit-icon">
                  <span className="home-benefit-emoji">🔒</span>
                </div>
                <h3 className="home-benefit-title">신뢰 중심 사용성</h3>
                <p className="home-benefit-description">
                  과장된 마케팅보다 차분하고 안정적인 금융 UI를 선호하는 분
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
            <span className="home-footer-logo-text">MoneyLog</span>
          </div>
          <p>© 2026 MoneyLog. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
