import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';

export default function SignUpPage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // 클라이언트 검증
    if (!name || !email || !password || !confirmPassword) {
      toast.error('모든 필드를 입력해주세요');
      setIsLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      toast.error('비밀번호가 일치하지 않습니다');
      setIsLoading(false);
      return;
    }

    if (password.length < 6) {
      toast.error('비밀번호는 최소 6자 이상이어야 합니다');
      setIsLoading(false);
      return;
    }

    // 시뮬레이션: localStorage에 사용자 저장
    // 실제 구현 시 Supabase auth로 교체
    try {
      const users = JSON.parse(localStorage.getItem('users') || '[]');
      
      // 이메일 중복 확인
      const existingUser = users.find((u: any) => u.email === email);
      if (existingUser) {
        toast.error('이미 등록된 이메일입니다');
        setIsLoading(false);
        return;
      }

      // 새 사용자 추가
      const newUser = {
        id: Date.now().toString(),
        name,
        email,
        password, // 실제로는 해싱해야 함
        createdAt: new Date().toISOString(),
      };

      users.push(newUser);
      localStorage.setItem('users', JSON.stringify(users));
      
      toast.success('회원가입이 완료되었습니다!');
      setTimeout(() => {
        navigate('/login');
      }, 1000);
    } catch (error) {
      toast.error('회원가입 중 오류가 발생했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-8">
          <Wallet className="size-10 text-blue-600" />
          <span className="text-3xl font-bold text-gray-900">내 가계부</span>
        </div>

        {/* SignUp Card */}
        <Card className="border-2">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl">회원가입</CardTitle>
            <CardDescription>
              계정을 생성하여 내 가계부를 시작하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSignUp} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">이름</Label>
                <Input
                  id="name"
                  type="text"
                  placeholder="홍길동"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
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

              <div className="space-y-2">
                <Label htmlFor="password">비밀번호</Label>
                <div className="relative">
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
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? (
                      <EyeOff className="size-4" />
                    ) : (
                      <Eye className="size-4" />
                    )}
                  </button>
                </div>
                <p className="text-xs text-gray-500">최소 6자 이상</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">비밀번호 확인</Label>
                <Input
                  id="confirmPassword"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? '처리 중...' : '회원가입'}
              </Button>
            </form>

            <div className="mt-6 text-center text-sm">
              <span className="text-gray-600">이미 계정이 있으신가요? </span>
              <Link 
                to="/login" 
                className="text-blue-600 hover:text-blue-700 hover:underline font-medium"
              >
                로그인
              </Link>
            </div>

            <div className="mt-6">
              <Button 
                variant="outline" 
                className="w-full" 
                onClick={() => navigate('/')}
              >
                홈으로 돌아가기
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
