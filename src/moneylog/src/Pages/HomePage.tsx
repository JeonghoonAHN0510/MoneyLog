import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, BarChart3, Calendar, Calculator, Target, ArrowRight } from 'lucide-react';

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
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Header */}
      <header className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="container mx-auto px-4 py-3 md:py-4 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Wallet className="size-6 md:size-8 text-blue-600" />
            <span className="text-xl md:text-2xl font-bold text-gray-900">내 가계부</span>
          </div>
          <div className="flex gap-2 md:gap-3">
            <Button variant="outline" size="sm" className="md:size-default" onClick={() => navigate('/login')}>
              로그인
            </Button>
            <Button size="sm" className="md:size-default" onClick={() => navigate('/signup')}>
              <span className="hidden sm:inline">시작하기</span>
              <span className="sm:hidden">시작</span>
            </Button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="container mx-auto px-4 py-20 md:py-32">
        <div className="max-w-4xl mx-auto text-center space-y-6">
          <h1 className="text-4xl md:text-6xl font-bold text-gray-900 leading-tight">
            사회 초년생을 위한
            <br />
            <span className="text-blue-600">스마트 재무 관리</span>
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            복잡한 가계부는 이제 그만! 직관적이고 안전한 나만의 재무 관리 시스템으로
            <br />
            돈의 흐름을 한눈에 파악하세요.
          </p>
          <div className="flex gap-4 justify-center pt-6">
            <Button size="lg" onClick={() => navigate('/signup')} className="gap-2">
              무료로 시작하기
              <ArrowRight className="size-4" />
            </Button>
            <Button size="lg" variant="outline" onClick={() => navigate('/login')}>
              로그인
            </Button>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            왜 내 가계부를 선택해야 할까요?
          </h2>
          <p className="text-lg text-gray-600">
            광고 없는 깔끔한 UI와 개인정보 보호를 최우선으로 합니다
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto">
          {features.map((feature, index) => (
            <Card key={index} className="border-2 hover:border-blue-200 transition-colors">
              <CardHeader>
                <div className="size-12 rounded-lg bg-blue-100 flex items-center justify-center mb-4">
                  <feature.icon className="size-6 text-blue-600" />
                </div>
                <CardTitle className="text-xl">{feature.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">{feature.description}</CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Benefits Section */}
      <section className="bg-gradient-to-br from-blue-600 to-purple-600 text-white py-20">
        <div className="container mx-auto px-4">
          <div className="max-w-4xl mx-auto space-y-12">
            <div className="text-center space-y-4">
              <h2 className="text-3xl md:text-4xl font-bold">이런 분들께 추천합니다</h2>
            </div>

            <div className="grid md:grid-cols-3 gap-8">
              <div className="space-y-3">
                <div className="size-12 rounded-full bg-white/20 flex items-center justify-center">
                  <span className="text-2xl">💼</span>
                </div>
                <h3 className="text-xl font-semibold">사회 초년생</h3>
                <p className="text-blue-100">
                  첫 월급을 받고 재무 관리를 시작하려는 당신에게 딱 맞는 도구
                </p>
              </div>

              <div className="space-y-3">
                <div className="size-12 rounded-full bg-white/20 flex items-center justify-center">
                  <span className="text-2xl">🎯</span>
                </div>
                <h3 className="text-xl font-semibold">목표 저축러</h3>
                <p className="text-blue-100">
                  체계적인 예산 관리로 저축 목표를 달성하고 싶은 분
                </p>
              </div>

              <div className="space-y-3">
                <div className="size-12 rounded-full bg-white/20 flex items-center justify-center">
                  <span className="text-2xl">🔒</span>
                </div>
                <h3 className="text-xl font-semibold">프라이버시 중시자</h3>
                <p className="text-blue-100">
                  개인정보 보안과 광고 없는 환경을 원하는 분
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="container mx-auto px-4 py-20">
        <div className="max-w-3xl mx-auto text-center space-y-6 bg-gradient-to-br from-gray-50 to-blue-50 rounded-2xl p-12 border-2 border-blue-200">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900">
            지금 바로 시작하세요
          </h2>
          <p className="text-lg text-gray-600">
            무료로 시작하고, 당신의 재무 목표를 달성하세요
          </p>
          <Button size="lg" onClick={() => navigate('/signup')} className="gap-2">
            무료 회원가입
            <ArrowRight className="size-4" />
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t bg-gray-50 py-12">
        <div className="container mx-auto px-4 text-center text-gray-600">
          <div className="flex items-center justify-center gap-2 mb-4">
            <Wallet className="size-6 text-blue-600" />
            <span className="text-xl font-bold text-gray-900">내 가계부</span>
          </div>
          <p>© 2024 내 가계부. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
