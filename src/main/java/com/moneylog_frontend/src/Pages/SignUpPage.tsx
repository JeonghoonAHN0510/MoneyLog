import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Wallet, Eye, EyeOff, User, Upload } from 'lucide-react';
import { toast } from 'sonner';
import api from '../api/axiosConfig';

export default function SignUpPage() {
  const navigate = useNavigate();

  // 기본 정보 State
  const [name, setName] = useState('');
  const [id, setId] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [gender, setGender] = useState<boolean>(true);
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');

  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // 이미지 변경 핸들러
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfileImage(file);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    }
  };

  // @ts-ignore
  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // 클라이언트 검증
    if (!name || !id || !email || !password || !confirmPassword || !phone) {
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

    try {
      const formData = new FormData();

      formData.append('id', id);
      formData.append('name', name);
      formData.append('email', email);
      formData.append('password', password);
      formData.append('phone', phone);
      formData.append('gender', gender);

      if (profileImage) {
        formData.append('upload_file', profileImage);
      }
      const response = await api.post('/user/signup', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const user_pk = response.data;

      if (user_pk > 0) {
        toast.success('회원가입이 완료되었습니다!');
        setTimeout(() => {
          navigate('/login');
        }, 1000);
      }

    } catch (error) {
      if (error.status == 409) {
        toast.error('중복된 아이디 또는 이메일이 존재합니다');
      } else {
        toast.error('회원가입 중 알 수 없는 오류가 발생했습니다');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-4 py-10">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-8">
          <Wallet className="size-10 text-blue-600" />
          <span className="text-3xl font-bold text-gray-900">내 가계부</span>
        </div>

        {/* SignUp Card */}
        <Card className="border-2 shadow-lg">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl text-center">회원가입</CardTitle>
            <CardDescription className="text-center">
              계정을 생성하여 내 가계부를 시작하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSignUp} className="space-y-4">

              {/* [1] 프로필 사진 업로드 섹션 */}
              <div className="flex flex-col items-center gap-4 mb-6">
                <div className="relative group cursor-pointer">
                  <div className="w-24 h-24 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center overflow-hidden bg-gray-50 hover:bg-gray-100 transition-colors">
                    {previewUrl ? (
                      <img src={previewUrl} alt="Profile Preview" className="w-full h-full object-cover" />
                    ) : (
                      <User className="text-gray-400 size-10" />
                    )}
                  </div>
                  <label
                    htmlFor="profile-upload"
                    className="absolute bottom-0 right-0 bg-blue-600 text-white p-1.5 rounded-full shadow-md cursor-pointer hover:bg-blue-700"
                  >
                    <Upload className="size-4" />
                  </label>
                  <input
                    id="profile-upload"
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleImageChange}
                  />
                </div>
                <Label htmlFor="profile-upload" className="text-xs text-gray-500 cursor-pointer">
                  프로필 사진 추가
                </Label>
              </div>

              {/* 아이디 & 이름 */}
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="id">아이디</Label>
                  <Input
                    id="id"
                    type="text"
                    placeholder="아이디"
                    value={id}
                    onChange={(e) => setId(e.target.value)}
                    required
                  />
                </div>
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

              {/* 비밀번호 섹션 */}
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

              {/* 전화번호 & 성별 */}
              <div className="grid grid-cols-1 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="phone">전화번호</Label>
                  <Input
                    id="phone"
                    type="tel"
                    placeholder="010-1234-5678"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    required
                  />
                </div>

                {/* [2] 성별 선택 섹션 */}
                <div className="space-y-2">
                  <Label>성별</Label>
                  <div className="flex gap-4">
                    <label className={`flex-1 flex items-center justify-center p-3 rounded-lg border cursor-pointer transition-all ${gender === true ? 'border-blue-500 bg-blue-50 text-blue-700 font-semibold' : 'border-gray-200 hover:bg-gray-50'}`}>
                      <input
                        type="radio"
                        name="gender"
                        className="hidden"
                        checked={gender === true}
                        onChange={() => setGender(true)}
                      />
                      <span>남성</span>
                    </label>
                    <label className={`flex-1 flex items-center justify-center p-3 rounded-lg border cursor-pointer transition-all ${gender === false ? 'border-pink-500 bg-pink-50 text-pink-700 font-semibold' : 'border-gray-200 hover:bg-gray-50'}`}>
                      <input
                        type="radio"
                        name="gender"
                        className="hidden"
                        checked={gender === false}
                        onChange={() => setGender(false)}
                      />
                      <span>여성</span>
                    </label>
                  </div>
                </div>
              </div>

              <Button type="submit" className="w-full mt-4" disabled={isLoading}>
                {isLoading ? '가입 처리 중...' : '회원가입'}
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

            <div className="mt-4">
              <Button
                variant="ghost"
                className="w-full text-gray-500"
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