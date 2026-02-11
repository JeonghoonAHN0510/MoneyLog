import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { Wallet, Eye, EyeOff, User, Upload } from 'lucide-react';
import { toast } from 'sonner';
import api from '../api/axiosConfig';
import useResourceStore from '../stores/resourceStore';

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
  const [bankId, setBankId] = useState('');
  const [accountNumber, setAccountNumber] = useState('');

  const { banks, setBanks } = useResourceStore();

  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBanks = async () => {
      if (banks.length > 0) {
        if (!bankId) setBankId(String(banks[0].bankId));
        return;
      }

      try {
        const response = await api.get("/bank");
        setBanks(response.data);
      } catch (error) {
        console.error("은행 목록 로드 실패:", error);
        toast.error("은행 목록을 불러오지 못했습니다.");
      }
    };
    fetchBanks();
  }, [banks, setBanks, bankId]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setProfileImage(file);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    }
  };

  const handleBankChange = (value: string) => {
    setBankId(value);
  };

  const getBankName = (targetId: string) => {
    // bank_id -> bankId
    const targetBank = banks.find(bank => String(bank.bankId) === String(targetId));
    return targetBank?.name || "";
  }

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    // 클라이언트 검증
    if (!name || !id || !email || !password || !confirmPassword || !phone || !bankId || !accountNumber) {
      setError('모든 필드를 입력해주세요');
      setIsLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다');
      setIsLoading(false);
      return;
    }

    if (password.length < 6) {
      setError('비밀번호는 최소 6자 이상이어야 합니다');
      setIsLoading(false);
      return;
    }

    try {
      const formData = new FormData();

      // FormData 키값들도 카멜케이스로 변경 (백엔드 수용 여부에 따라 확인 필요)
      formData.append('id', id);
      formData.append('name', name);
      formData.append('email', email);
      formData.append('password', password);
      formData.append('phone', phone);
      formData.append('gender', String(gender));
      formData.append('bankId', bankId);
      formData.append('accountNumber', String(accountNumber));

      const bankName = getBankName(bankId);
      formData.append('bankName', String(bankName));

      if (profileImage) {
        formData.append('uploadFile', profileImage);
      }

      const response = await api.post('/user/signup', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const userPk = response.data;

      if (userPk > 0) {
        toast.success('회원가입이 완료되었습니다!');
        setTimeout(() => {
          navigate('/login');
        }, 1000);
      }

    } catch (error: any) {
      if (error.response && error.response.status === 409) {
        setError('이미 사용 중인 아이디 또는 이메일입니다.');
      } else {
        setError('회원가입 중 오류가 발생했습니다. 입력을 확인해주세요.');
      }
      toast.error('회원가입에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-2 sm:p-4 py-10">
      <div className="w-full max-w-md">
        <div className="flex items-center justify-center gap-2 mb-8">
          <Wallet className="size-10 text-blue-600" />
          <span className="text-3xl font-bold text-gray-900">내 가계부</span>
        </div>

        <Card className="border-2 shadow-lg">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl text-center">회원가입</CardTitle>
            <CardDescription className="text-center">
              계정을 생성하여 내 가계부를 시작하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSignUp} className="space-y-4" noValidate>
              {error && (
                <div className="p-3 text-sm text-red-500 bg-red-50 border border-red-200 rounded-md flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-alert-circle"><circle cx="12" cy="12" r="10" /><line x1="12" x2="12" y1="8" y2="12" /><line x1="12" x2="12.01" y1="16" y2="16" /></svg>
                  {error}
                </div>
              )}
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

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
                    {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
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

              <div className="grid grid-cols-1 sm:grid-cols-5 gap-4">
                <div className="sm:col-span-2 space-y-2">
                  <Label htmlFor="bank">주거래 은행</Label>
                  <Select value={bankId} onValueChange={handleBankChange}>
                    <SelectTrigger id="bank" className="w-full">
                      <SelectValue placeholder="은행 선택" />
                    </SelectTrigger>
                    <SelectContent>
                      {banks.map((bank) => (
                        <SelectItem key={bank.code} value={String(bank.bankId)}>
                          {bank.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="sm:col-span-3 space-y-2">
                  <Label htmlFor="accountNumber">계좌번호</Label>
                  <Input
                    id="accountNumber"
                    type="text"
                    placeholder="-를 제외하고 입력해주세요."
                    value={accountNumber}
                    onChange={(e) => setAccountNumber(e.target.value)}
                    required
                  />
                </div>
              </div>

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