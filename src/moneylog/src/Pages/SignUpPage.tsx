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
import { useBanks } from '../api/queries';
import '../styles/pages/SignUpPage.css';

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

  const { data: banks = [] } = useBanks();

  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // 은행 목록 로드 시 첫 번째 은행을 기본값으로
  useEffect(() => {
    if (banks.length > 0 && !bankId) {
      setBankId(String(banks[0].bankId));
    }
  }, [banks, bankId]);

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
    <div className="signup-page">
      <div className="signup-wrapper">
        <div className="signup-logo">
          <Wallet className="signup-logo-icon" />
          <span className="signup-logo-text">내 가계부</span>
        </div>

        <Card className="signup-card">
          <CardHeader className="signup-card-header">
            <CardTitle className="signup-card-title">회원가입</CardTitle>
            <CardDescription className="signup-card-description">
              계정을 생성하여 내 가계부를 시작하세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSignUp} className="signup-form" noValidate>
              {error && (
                <div className="signup-error-alert">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10" /><line x1="12" x2="12" y1="8" y2="12" /><line x1="12" x2="12.01" y1="16" y2="16" /></svg>
                  {error}
                </div>
              )}
              <div className="signup-profile-section">
                <div className="signup-profile-wrapper group cursor-pointer">
                  <div className="signup-profile-circle">
                    {previewUrl ? (
                      <img src={previewUrl} alt="Profile Preview" className="signup-profile-preview" />
                    ) : (
                      <User className="signup-profile-placeholder" />
                    )}
                  </div>
                  <label
                    htmlFor="profile-upload"
                    className="signup-profile-upload-btn"
                  >
                    <Upload className="signup-profile-upload-icon" />
                  </label>
                  <input
                    id="profile-upload"
                    type="file"
                    accept="image/*"
                    className="signup-profile-upload-input"
                    onChange={handleImageChange}
                  />
                </div>
                <Label htmlFor="profile-upload" className="signup-profile-label">
                  프로필 사진 추가
                </Label>
              </div>

              <div className="signup-grid-2col">
                <div className="signup-field-group">
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
                <div className="signup-field-group">
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

              <div className="signup-field-group">
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

              <div className="signup-field-group">
                <Label htmlFor="password">비밀번호</Label>
                <div className="signup-password-wrapper">
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
                    className="signup-password-toggle"
                  >
                    {showPassword ? <EyeOff className="signup-password-toggle-icon" /> : <Eye className="signup-password-toggle-icon" />}
                  </button>
                </div>
              </div>

              <div className="signup-field-group">
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

              <div className="signup-grid-bank">
                <div className="signup-bank-select-col">
                  <Label htmlFor="bank">주거래 은행</Label>
                  <Select value={bankId} onValueChange={handleBankChange}>
                    <SelectTrigger id="bank" className="signup-bank-select-trigger">
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

                <div className="signup-account-col">
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

              <div className="signup-grid-1col">
                <div className="signup-field-group">
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

                <div className="signup-field-group">
                  <Label>성별</Label>
                  <div className="signup-gender-group">
                    <label className={`signup-gender-option ${gender === true ? 'active-male' : 'inactive'}`}>
                      <input
                        type="radio"
                        name="gender"
                        className="signup-gender-radio"
                        checked={gender === true}
                        onChange={() => setGender(true)}
                      />
                      <span>남성</span>
                    </label>
                    <label className={`signup-gender-option ${gender === false ? 'active-female' : 'inactive'}`}>
                      <input
                        type="radio"
                        name="gender"
                        className="signup-gender-radio"
                        checked={gender === false}
                        onChange={() => setGender(false)}
                      />
                      <span>여성</span>
                    </label>
                  </div>
                </div>
              </div>

              <Button type="submit" className="signup-submit-btn" disabled={isLoading}>
                {isLoading ? '가입 처리 중...' : '회원가입'}
              </Button>
            </form>

            <div className="signup-login-prompt">
              <span className="signup-login-prompt-text">이미 계정이 있으신가요? </span>
              <Link
                to="/login"
                className="signup-login-link"
              >
                로그인
              </Link>
            </div>

            <div className="signup-home-btn-wrapper">
              <Button
                variant="ghost"
                className="signup-home-btn"
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