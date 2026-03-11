import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { InputOTP, InputOTPGroup, InputOTPSlot } from '../components/ui/input-otp';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import StandalonePageHeader from '../components/StandalonePageHeader';

import { toast } from 'sonner';
import api from '../api/axiosConfig';
import { getApiErrorMessage } from '../utils/error';
import '../styles/pages/ForgotPasswordPage.css';

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState<'request' | 'verify' | 'confirm' | 'success'>('request');
  const [id, setId] = useState('');
  const [email, setEmail] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [otpTtlSeconds, setOtpTtlSeconds] = useState(0);
  const [resendCooldownSeconds, setResendCooldownSeconds] = useState(0);
  const [resetTokenTtlSeconds, setResetTokenTtlSeconds] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (step !== 'verify' && step !== 'confirm') {
      return undefined;
    }

    const timer = window.setInterval(() => {
      if (step === 'verify') {
        setOtpTtlSeconds((prev) => Math.max(0, prev - 1));
        setResendCooldownSeconds((prev) => Math.max(0, prev - 1));
      }

      if (step === 'confirm') {
        setResetTokenTtlSeconds((prev) => Math.max(0, prev - 1));
      }
    }, 1000);

    return () => window.clearInterval(timer);
  }, [step]);

  const formatSeconds = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainSeconds = seconds % 60;
    return `${minutes}:${String(remainSeconds).padStart(2, '0')}`;
  };

  const getDescription = () => {
    if (step === 'verify') {
      return '입력한 이메일로 받은 6자리 인증번호를 입력하세요.';
    }
    if (step === 'confirm') {
      return '인증이 완료되었습니다. 새 비밀번호를 설정하세요.';
    }
    if (step === 'success') {
      return '비밀번호가 변경되었습니다. 새 비밀번호로 로그인하세요.';
    }
    return '아이디와 이메일을 입력하면 비밀번호 재설정 인증번호를 보내드립니다.';
  };

  const handleRequestOtp = async () => {
    if (!id.trim() || !email.trim()) {
      setError('아이디와 이메일을 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      const response = await api.post('/user/password-reset/request', {
        id: id.trim(),
        email: email.trim(),
      });
      const data = response.data;
      setStep('verify');
      setOtpCode('');
      setOtpTtlSeconds(data.otpTtlSeconds ?? 300);
      setResendCooldownSeconds(data.resendCooldownSeconds ?? 60);
      toast.success('인증번호를 이메일로 전송했습니다.');
    } catch (requestError: unknown) {
      const errorMessage = getApiErrorMessage(requestError, '인증번호 요청 중 오류가 발생했습니다.');
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (otpCode.trim().length !== 6) {
      setError('인증번호 6자리를 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      const response = await api.post('/user/password-reset/verify-otp', {
        id: id.trim(),
        email: email.trim(),
        otpCode: otpCode.trim(),
      });
      const data = response.data;
      setResetToken(data.resetToken);
      setResetTokenTtlSeconds(data.resetTokenTtlSeconds ?? 600);
      setOtpCode('');
      setStep('confirm');
      toast.success('이메일 인증이 완료되었습니다.');
    } catch (requestError: unknown) {
      const errorMessage = getApiErrorMessage(requestError, '인증번호 확인 중 오류가 발생했습니다.');
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendCooldownSeconds > 0) {
      return;
    }
    await handleRequestOtp();
  };

  const handleConfirmReset = async () => {
    if (!newPassword || !confirmPassword) {
      setError('새 비밀번호와 비밀번호 확인을 입력해주세요.');
      return;
    }

    if (newPassword.length < 6) {
      setError('비밀번호는 최소 6자 이상이어야 합니다.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('비밀번호 확인이 일치하지 않습니다.');
      return;
    }

    if (!resetToken) {
      setError('비밀번호 재설정 세션이 유효하지 않습니다. 다시 인증해주세요.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      await api.post('/user/password-reset/confirm', {
        resetToken,
        newPassword,
      });
      setNewPassword('');
      setConfirmPassword('');
      setResetToken('');
      setResetTokenTtlSeconds(0);
      setStep('success');
      toast.success('비밀번호가 변경되었습니다.');
    } catch (requestError: unknown) {
      const errorMessage = getApiErrorMessage(requestError, '비밀번호 변경 중 오류가 발생했습니다.');
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetFlow = () => {
    setStep('request');
    setOtpCode('');
    setResetToken('');
    setNewPassword('');
    setConfirmPassword('');
    setOtpTtlSeconds(0);
    setResendCooldownSeconds(0);
    setResetTokenTtlSeconds(0);
    setError('');
  };

  return (
    <div className="forgot-page">
      <div className="forgot-wrapper">
        <StandalonePageHeader />

        <Card className="forgot-card">
          <CardHeader className="forgot-card-header">
            <CardTitle className="forgot-card-title">비밀번호 찾기</CardTitle>
            <CardDescription>{getDescription()}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="forgot-step-badge">
              {step === 'request' && '1단계 · 계정 확인'}
              {step === 'verify' && '2단계 · 이메일 인증'}
              {step === 'confirm' && '3단계 · 새 비밀번호 설정'}
              {step === 'success' && '완료'}
            </div>

            {error && <div className="forgot-error-alert">{error}</div>}

            {step === 'request' && (
              <form
                className="forgot-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  void handleRequestOtp();
                }}
              >
                <div className="forgot-field-group">
                  <Label htmlFor="forgot-id">아이디</Label>
                  <Input
                    id="forgot-id"
                    type="text"
                    placeholder="your_id"
                    value={id}
                    onChange={(event) => setId(event.target.value)}
                    required
                  />
                </div>

                <div className="forgot-field-group">
                  <Label htmlFor="forgot-email">이메일</Label>
                  <Input
                    id="forgot-email"
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    required
                  />
                </div>

                <Button type="submit" variant="outline" className="forgot-submit-btn" disabled={isSubmitting}>
                  {isSubmitting ? '인증번호 전송 중...' : '인증번호 요청'}
                </Button>
              </form>
            )}

            {step === 'verify' && (
              <form
                className="forgot-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  void handleVerifyOtp();
                }}
              >
                <div className="forgot-success-alert">
                  <p>
                    <strong>{email}</strong> 로 인증번호를 전송했습니다.
                    유효시간은 {formatSeconds(otpTtlSeconds)} 입니다.
                  </p>
                </div>

                <div className="forgot-field-group">
                  <Label htmlFor="forgot-otp">이메일 인증번호</Label>
                  <InputOTP
                    id="forgot-otp"
                    maxLength={6}
                    value={otpCode}
                    onChange={setOtpCode}
                    containerClassName="forgot-otp-container"
                  >
                    <InputOTPGroup className="forgot-otp-group">
                      {Array.from({ length: 6 }).map((_, index) => (
                        <InputOTPSlot key={index} index={index} className="forgot-otp-slot" />
                      ))}
                    </InputOTPGroup>
                  </InputOTP>
                </div>

                <div className="forgot-inline-actions">
                  <span className="forgot-helper-text">
                    {resendCooldownSeconds > 0
                      ? `재전송 가능까지 ${formatSeconds(resendCooldownSeconds)}`
                      : '인증번호를 받지 못하셨나요?'}
                  </span>
                  <Button
                    type="button"
                    variant="ghost"
                    className="forgot-inline-btn"
                    onClick={() => void handleResendOtp()}
                    disabled={isSubmitting || resendCooldownSeconds > 0}
                  >
                    인증번호 재전송
                  </Button>
                </div>

                <Button type="submit" className="forgot-submit-btn" disabled={isSubmitting}>
                  {isSubmitting ? '인증 확인 중...' : '인증번호 확인'}
                </Button>
              </form>
            )}

            {step === 'confirm' && (
              <form
                className="forgot-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  void handleConfirmReset();
                }}
              >
                <div className="forgot-success-alert">
                  <p>
                    재설정 세션 남은 시간은 {formatSeconds(resetTokenTtlSeconds)} 입니다.
                    시간이 지나면 이메일 인증부터 다시 진행해야 합니다.
                  </p>
                </div>

                <div className="forgot-field-group">
                  <Label htmlFor="forgot-new-password">새 비밀번호</Label>
                  <Input
                    id="forgot-new-password"
                    type="password"
                    placeholder="새 비밀번호"
                    value={newPassword}
                    onChange={(event) => setNewPassword(event.target.value)}
                    required
                  />
                </div>

                <div className="forgot-field-group">
                  <Label htmlFor="forgot-confirm-password">새 비밀번호 확인</Label>
                  <Input
                    id="forgot-confirm-password"
                    type="password"
                    placeholder="새 비밀번호 확인"
                    value={confirmPassword}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    required
                  />
                </div>

                {resetTokenTtlSeconds === 0 ? (
                  <Button type="button" className="forgot-submit-btn" onClick={resetFlow}>
                    처음부터 다시
                  </Button>
                ) : (
                  <Button type="submit" className="forgot-submit-btn" disabled={isSubmitting}>
                    {isSubmitting ? '비밀번호 변경 중...' : '비밀번호 변경'}
                  </Button>
                )}
              </form>
            )}

            {step === 'success' && (
              <div className="forgot-success-section">
                <div className="forgot-success-alert">
                  <p>
                    비밀번호가 정상적으로 변경되었습니다.
                    새 비밀번호로 다시 로그인해 주세요.
                  </p>
                </div>
                <Button className="forgot-submit-btn" onClick={() => navigate('/login')}>
                  로그인 페이지로 돌아가기
                </Button>
              </div>
            )}

            {step !== 'success' && (
              <div className="forgot-footer-actions">
                {step !== 'request' && (
                  <Button
                    type="button"
                    variant="ghost"
                    className="forgot-inline-btn"
                    onClick={resetFlow}
                    disabled={isSubmitting}
                  >
                    처음부터 다시
                  </Button>
                )}
                <Button
                  type="button"
                  variant="ghost"
                  className="forgot-inline-btn"
                  onClick={() => navigate('/login')}
                >
                  로그인 페이지로 돌아가기
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
