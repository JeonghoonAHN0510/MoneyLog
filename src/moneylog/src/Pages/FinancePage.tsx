import { useState, useEffect, useRef, type ChangeEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/avatar';
import { CalendarView } from '../components/CalendarView';
import { DashboardView } from '../components/DashboardView';
import { TransactionList } from '../components/TransactionList';
import { AddTransactionDialog } from '../components/AddTransactionDialog';
import { EditTransactionDialog } from '../components/EditTransactionDialog';
import { TransferDialog } from '../components/TransferDialog';
import { TransactionImportDialog } from '../components/TransactionImportDialog';
import { ScheduleDialog } from '../components/ScheduleDialog';
import { BudgetManager } from '../components/BudgetManager';
import { AccountManager } from '../components/AccountManager';
import { CategoryManager } from '../components/CategoryManager';
import { Budget, Category, Account, Transaction, Payment, Transfer, Fixed } from '../types/finance';
import {
    Plus,
    Wallet,
    Target,
    LogOut,
    ImagePlus,
    RefreshCcw,
    Upload
} from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import api, { clearAuthorizationHeader, setAuthorizationHeader } from '../api/axiosConfig';
import {
    useUserInfo,
    useUpdateProfileImage,
    useRefreshToken,
    useAddTransaction,
    useUpdateTransaction,
    useDeleteTransaction,
    useAddFixed,
    useAddAccount,
    useUpdateAccount,
    useDeleteAccount,
    useTransfer,
    useAddBudget,
    useUpdateBudget,
    useDeleteBudget,
    useAddCategory,
    useUpdateCategory,
    useDeleteCategory,
    useAddPayment,
    useUpdatePayment,
    useDeletePayment,
} from '../api/queries';
import { useDocumentTitle } from '../hooks/useDocumentTitle';
import { getApiErrorMessage, getApiErrorStatus } from '../utils/error';
import { buildProfileImageViewUrl, getProfileInitial } from '../utils/profileImage';
import { financeSections, type FinanceSection } from './FinancePage.constants';
import '../styles/pages/FinancePage.css';

export default function FinancePage() {
    const navigate = useNavigate();
    const { isAuthenticated, logout, refreshToken, setTokens } = useUserStore();

    // UI 상태
    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false);
    const [isEditTransactionDialogOpen, setIsEditTransactionDialogOpen] = useState(false);
    const [isScheduleDialogOpen, setIsScheduleDialogOpen] = useState(false);
    const [isTransactionImportOpen, setIsTransactionImportOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);
    const [selectedDate, setSelectedDate] = useState<string | undefined>(undefined);
    const profileImageInputRef = useRef<HTMLInputElement | null>(null);

    const [searchParams, setSearchParams] = useSearchParams();
    const currentTabParam = searchParams.get('tab');
    const currentTab = financeSections.some((section) => section.value === currentTabParam)
        ? currentTabParam as FinanceSection
        : 'dashboard';
    const currentSection = financeSections.find((section) => section.value === currentTab) ?? financeSections[0];
    const currentMonthLabel = new Intl.DateTimeFormat('ko-KR', { month: 'long' }).format(new Date());
    const heroHighlights = [
        { label: '이번 달 포커스', value: `${currentMonthLabel} 브리핑` },
        { label: '활성 섹션', value: currentSection.label },
        { label: '관리 흐름', value: '거래 · 예산 · 계좌' },
    ];

    useDocumentTitle(`${currentSection.label} | MoneyLog`);

    const handleTabChange = (value: FinanceSection) => {
        setSearchParams({ tab: value });
        if (value !== 'calendar') {
            setSelectedDate(undefined);
        }
    };

    useEffect(() => {
        if (currentTabParam && !financeSections.some((section) => section.value === currentTabParam)) {
            setSearchParams({ tab: 'dashboard' });
        }
    }, [currentTabParam, setSearchParams]);

    // --- [TanStack Query] 서버 데이터 ---
    const { data: userInfo, isLoading: userInfoLoading, error: userInfoError } = useUserInfo();
    const refreshTokenMut = useRefreshToken();
    const isAdmin = userInfo?.role === 'ADMIN';

    // 로그인 체크
    useEffect(() => {
        if (!isAuthenticated) {
            toast.error('로그인이 필요합니다');
            navigate('/login');
        }
    }, [isAuthenticated, navigate]);

    const runMutationWithToast = async <T,>({
        action,
        successMessage,
        errorMessage,
        onSuccess,
        onError,
        onFinally,
    }: {
        action: () => Promise<T>;
        successMessage?: string;
        errorMessage: string;
        onSuccess?: (result: T) => void;
        onError?: (error: unknown) => void;
        onFinally?: () => void;
    }) => {
        try {
            const result = await action();
            if (successMessage) {
                toast.success(successMessage);
            }
            onSuccess?.(result);
            return result;
        } catch (error) {
            toast.error(getApiErrorMessage(error, errorMessage));
            onError?.(error);
            return null;
        } finally {
            onFinally?.();
        }
    };

    // 사용자 정보 로드 실패 시 상태코드별 분기
    useEffect(() => {
        if (!userInfoError) {
            return;
        }

        const status = getApiErrorStatus(userInfoError);
        if (status === 401 || status === 403) {
            toast.error(getApiErrorMessage(userInfoError, '로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.'));
            clearAuthorizationHeader();
            logout();
            navigate('/login');
            return;
        }

        toast.error(getApiErrorMessage(userInfoError, '사용자 정보를 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.'));
    }, [userInfoError, logout, navigate]);

    useEffect(() => {
        if (!isAdmin && isScheduleDialogOpen) {
            setIsScheduleDialogOpen(false);
        }
    }, [isAdmin, isScheduleDialogOpen]);

    const resetClientSession = () => {
        clearAuthorizationHeader();
        logout();
    };

    const handleLogout = async () => {
        try {
            await api.post('/user/logout');
            resetClientSession();
            toast.success('로그아웃 되었습니다.');
            navigate('/');
        } catch {
            resetClientSession();
            navigate('/');
        }
    };

    const handleRefreshSession = async () => {
        if (!refreshToken) {
            toast.error('리프레시 토큰이 없습니다.');
            return;
        }

        await runMutationWithToast({
            action: () => refreshTokenMut.mutateAsync({ refreshToken }),
            successMessage: '로그인 세션이 연장되었습니다.',
            errorMessage: '로그인 연장에 실패했습니다.',
            onSuccess: (result) => {
                setTokens(result.accessToken, result.refreshToken);
                setAuthorizationHeader(result.accessToken);
            },
            onError: () => {
                resetClientSession();
                navigate('/login');
            },
        });
    };

    // --- [TanStack Query Mutations] ---
    const addTransactionMut = useAddTransaction();
    const updateTransactionMut = useUpdateTransaction();
    const deleteTransactionMut = useDeleteTransaction();
    const addFixedMut = useAddFixed();
    const addAccountMut = useAddAccount();
    const updateAccountMut = useUpdateAccount();
    const deleteAccountMut = useDeleteAccount();
    const transferMut = useTransfer();
    const addBudgetMut = useAddBudget();
    const updateBudgetMut = useUpdateBudget();
    const deleteBudgetMut = useDeleteBudget();
    const addCategoryMut = useAddCategory();
    const updateCategoryMut = useUpdateCategory();
    const deleteCategoryMut = useDeleteCategory();
    const addPaymentMut = useAddPayment();
    const updatePaymentMut = useUpdatePayment();
    const deletePaymentMut = useDeletePayment();
    const updateProfileImageMut = useUpdateProfileImage();

    const openProfileImagePicker = () => {
        profileImageInputRef.current?.click();
    };

    const openImportWorkspace = () => {
        handleTabChange('transactions');
        setIsTransactionImportOpen(true);
    };

    const handleProfileImageChange = async (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) {
            return;
        }

        await runMutationWithToast({
            action: () => updateProfileImageMut.mutateAsync(file),
            successMessage: '프로필 이미지가 변경되었습니다.',
            errorMessage: '프로필 이미지 변경에 실패하였습니다.',
            onFinally: () => {
                event.target.value = '';
            },
        });
    };

    // --- [Transaction CRUD] ---
    const handleAddTransaction = async (transaction: Partial<Transaction>) => {
        await runMutationWithToast({
            action: () => addTransactionMut.mutateAsync(transaction),
            successMessage: '거래 내역이 추가되었습니다.',
            errorMessage: '거래 내역 추가에 실패하였습니다.',
        });
    };

    const handleAddFixed = async (fixed: Partial<Fixed>) => {
        await runMutationWithToast({
            action: () => addFixedMut.mutateAsync(fixed),
            successMessage: '고정 거래 내역이 추가되었습니다.',
            errorMessage: '고정 거래 내역 추가에 실패하였습니다.',
        });
    };

    const handleUpdateTransaction = async (transaction: Partial<Transaction>) => {
        await runMutationWithToast({
            action: () => updateTransactionMut.mutateAsync(transaction),
            successMessage: '거래 내역이 수정되었습니다.',
            errorMessage: '거래 내역 수정에 실패했습니다.',
        });
    };

    const handleDeleteTransaction = async (transactionId: string) => {
        await runMutationWithToast({
            action: () => deleteTransactionMut.mutateAsync(transactionId),
            successMessage: '거래 내역이 삭제되었습니다.',
            errorMessage: '거래 내역 삭제에 실패하였습니다.',
        });
    };

    const handleEditTransaction = (transaction: Transaction) => {
        setEditingTransaction(transaction);
        setIsEditTransactionDialogOpen(true);
    };


    // --- [Budget CRUD] ---
    const handleAddBudget = async (budget: Omit<Budget, 'budgetId' | "userId" | "budgetDate" | "createdAt" | "updatedAt" | "categoryName">) => {
        await runMutationWithToast({
            action: () => addBudgetMut.mutateAsync(budget),
            successMessage: '예산이 설정되었습니다.',
            errorMessage: '예산 설정에 실패하였습니다.',
        });
    };

    const handleUpdateBudget = async (budget: Partial<Budget>) => {
        await runMutationWithToast({
            action: () => updateBudgetMut.mutateAsync(budget),
            successMessage: '예산이 수정되었습니다.',
            errorMessage: '예산 수정에 실패하였습니다.',
        });
    };

    const handleDeleteBudget = async (budgetId: string) => {
        await runMutationWithToast({
            action: () => deleteBudgetMut.mutateAsync(budgetId),
            successMessage: '예산이 삭제되었습니다.',
            errorMessage: '예산 삭제에 실패하였습니다.',
        });
    };

    // --- [Account CRUD] ---
    const handleAddAccount = async (account: Omit<Account, "accountId" | "userId" | "createdAt" | "updatedAt" | "bankName">) => {
        await runMutationWithToast({
            action: () => addAccountMut.mutateAsync(account),
            successMessage: '계좌가 추가되었습니다.',
            errorMessage: '계좌 추가에 실패하였습니다.',
        });
    };

    const handleUpdateAccount = async (account: Partial<Account>) => {
        await runMutationWithToast({
            action: () => updateAccountMut.mutateAsync(account),
            successMessage: '계좌가 수정되었습니다.',
            errorMessage: '계좌 수정에 실패하였습니다.',
        });
    };

    const handleDeleteAccount = async (accountId: string) => {
        await runMutationWithToast({
            action: () => deleteAccountMut.mutateAsync(accountId),
            successMessage: '계좌가 삭제되었습니다.',
            errorMessage: '계좌 삭제에 실패하였습니다.',
        });
    };

    // --- [Category CRUD] ---
    const handleAddCategory = async (category: Omit<Category, "categoryId" | "userId" | "createdAt" | "updatedAt">) => {
        await runMutationWithToast({
            action: () => addCategoryMut.mutateAsync(category),
            successMessage: '카테고리가 추가되었습니다.',
            errorMessage: '카테고리 추가에 실패하였습니다.',
        });
    };

    const handleUpdateCategory = async (category: Partial<Category>) => {
        await runMutationWithToast({
            action: () => updateCategoryMut.mutateAsync(category),
            successMessage: '카테고리가 수정되었습니다.',
            errorMessage: '카테고리 수정에 실패하였습니다.',
        });
    };

    const handleDeleteCategory = async (categoryId: string) => {
        await runMutationWithToast({
            action: () => deleteCategoryMut.mutateAsync(categoryId),
            successMessage: '카테고리가 삭제되었습니다.',
            errorMessage: '카테고리 삭제에 실패하였습니다.',
        });
    };

    // --- [Payment CRUD] ---
    const handleAddPayment = async (payment: Omit<Payment, "paymentId" | "userId" | "createdAt" | "updatedAt">) => {
        await runMutationWithToast({
            action: () => addPaymentMut.mutateAsync(payment),
            successMessage: '결제수단이 추가되었습니다.',
            errorMessage: '결제수단 추가에 실패하였습니다.',
        });
    };

    const handleUpdatePayment = async (payment: Partial<Payment>) => {
        await runMutationWithToast({
            action: () => updatePaymentMut.mutateAsync(payment),
            successMessage: '결제수단이 수정되었습니다.',
            errorMessage: '결제수단 수정에 실패하였습니다.',
        });
    };

    const handleDeletePayment = async (paymentId: string) => {
        await runMutationWithToast({
            action: () => deletePaymentMut.mutateAsync(paymentId),
            successMessage: '결제수단이 삭제되었습니다.',
            errorMessage: '결제수단 삭제에 실패하였습니다.',
        });
    };

    // --- [Transfer Logic] ---
    const handleAddTransfer = async (transfer: Omit<Transfer, "transferId" | "userId" | "createdAt" | "updatedAt">) => {
        await runMutationWithToast({
            action: () => transferMut.mutateAsync(transfer),
            successMessage: '이체가 완료되었습니다.',
            errorMessage: '이체에 실패하였습니다.',
        });
    };

    const handleDateClick = (date: string) => {
        setSelectedDate(date);
    };

    const renderCurrentSection = () => {
        switch (currentTab) {
            case 'calendar':
                return (
                    <>
                        <CalendarView onDateClick={handleDateClick} />
                        {selectedDate && (
                            <TransactionList
                                selectedDate={selectedDate}
                                onEdit={handleEditTransaction}
                                onDelete={handleDeleteTransaction}
                            />
                        )}
                    </>
                );
            case 'transactions':
                return (
                    <TransactionList
                        onEdit={handleEditTransaction}
                        onDelete={handleDeleteTransaction}
                    />
                );
            case 'accounts':
                return (
                    <AccountManager
                        onAdd={handleAddAccount}
                        onUpdate={handleUpdateAccount}
                        onDelete={handleDeleteAccount}
                        onTransferClick={() => setIsTransferDialogOpen(true)}
                    />
                );
            case 'categories':
                return (
                    <CategoryManager
                        onAdd={handleAddCategory}
                        onUpdate={handleUpdateCategory}
                        onDelete={handleDeleteCategory}
                        onAddPayment={handleAddPayment}
                        onUpdatePayment={handleUpdatePayment}
                        onDeletePayment={handleDeletePayment}
                    />
                );
            case 'budget':
                return (
                    <BudgetManager
                        onAdd={handleAddBudget}
                        onUpdate={handleUpdateBudget}
                        onDelete={handleDeleteBudget}
                    />
                );
            default:
                return <DashboardView />;
        }
    };

    if (userInfoLoading) {
        return (
            <div className="finance-loading">
                <p className="finance-loading-text">정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (!userInfo) {
        return (
            <div className="finance-loading">
                <p className="finance-loading-text">사용자 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.</p>
            </div>
        );
    }

    if (!isAuthenticated) return null;

    const profileImageViewUrl = buildProfileImageViewUrl(userInfo.profileImageUrl);

    return (
        <div className="finance-page">
            <div className="finance-orb finance-orb-primary" />
            <div className="finance-orb finance-orb-secondary" />
            <div className="finance-container">
                <input
                    ref={profileImageInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleProfileImageChange}
                />
                <section className="finance-hero">
                    <div className="finance-hero-main finance-glass-panel">
                        <div className="finance-hero-kicker">
                            <Wallet className="finance-title-icon" />
                            Blue Finance Workspace
                        </div>
                        <h1 className="finance-title">내 자산 흐름을 더 선명하게 관리하세요</h1>
                        <p className="finance-subtitle">
                            거래, 예산, 계좌, 카테고리를 하나의 금융 워크스페이스에서 연결하고
                            월간 흐름을 차분한 블루 테마로 정리합니다.
                        </p>
                        <div className="finance-hero-chip-row">
                            <span className="finance-hero-chip">수입 흐름</span>
                            <span className="finance-hero-chip">지출 통제</span>
                            <span className="finance-hero-chip">예산 모니터링</span>
                        </div>
                        <div className="finance-actions">
                            <Button size="lg" onClick={() => setIsAddDialogOpen(true)}>
                                <Plus className="finance-add-btn-icon" />
                                거래 추가
                            </Button>
                            <Button size="lg" variant="outline" onClick={openImportWorkspace}>
                                <Upload className="finance-add-btn-icon" />
                                거래 업로드
                            </Button>
                            <Button size="lg" variant="secondary" onClick={() => setIsTransferDialogOpen(true)}>
                                <Wallet className="finance-add-btn-icon" />
                                계좌 이체
                            </Button>
                        </div>
                    </div>

                    <div className="finance-profile-panel finance-glass-panel">
                        <div className="finance-profile-top">
                            <Avatar className="finance-profile-avatar">
                                {profileImageViewUrl && (
                                    <AvatarImage src={profileImageViewUrl} alt={`${userInfo.name} profile`} />
                                )}
                                <AvatarFallback className="finance-user-avatar-fallback">
                                    {getProfileInitial(userInfo.name)}
                                </AvatarFallback>
                            </Avatar>
                            <div className="finance-profile-copy">
                                <p className="finance-profile-name">{userInfo.name}</p>
                                <p className="finance-profile-email">{userInfo.email}</p>
                            </div>
                        </div>

                        <div className="finance-highlight-grid">
                            {heroHighlights.map((item) => (
                                <div key={item.label} className="finance-highlight-card">
                                    <span className="finance-highlight-label">{item.label}</span>
                                    <strong className="finance-highlight-value">{item.value}</strong>
                                </div>
                            ))}
                        </div>

                        <div className="finance-profile-actions">
                            <Button variant="outline" size="sm" onClick={openProfileImagePicker} disabled={updateProfileImageMut.isPending}>
                                <ImagePlus className="finance-profile-action-icon" />
                                프로필
                            </Button>
                            <Button variant="outline" size="sm" onClick={handleRefreshSession} disabled={refreshTokenMut.isPending}>
                                <RefreshCcw className="finance-profile-action-icon" />
                                세션 연장
                            </Button>
                            {isAdmin && (
                                <Button variant="outline" size="sm" onClick={() => setIsScheduleDialogOpen(true)}>
                                    <Target className="finance-profile-action-icon" />
                                    스케줄
                                </Button>
                            )}
                            <Button variant="ghost" size="sm" onClick={handleLogout}>
                                <LogOut className="finance-profile-action-icon" />
                                로그아웃
                            </Button>
                        </div>
                    </div>
                </section>

                <div className="finance-workspace">
                    <aside className="finance-sidebar finance-glass-panel">
                        <div className="finance-sidebar-header">
                            <p className="finance-sidebar-kicker">WORKSPACE</p>
                            <h2 className="finance-sidebar-title">자산 관리 허브</h2>
                            <p className="finance-sidebar-copy">핵심 금융 작업을 섹션 단위로 정리해 빠르게 이동합니다.</p>
                        </div>
                        <div className="finance-sidebar-list">
                            {financeSections.map((section) => {
                                const Icon = section.icon;
                                const isActive = currentTab === section.value;

                                return (
                                    <button
                                        key={section.value}
                                        type="button"
                                        className={`finance-side-nav ${isActive ? 'is-active' : ''}`}
                                        onClick={() => handleTabChange(section.value)}
                                    >
                                        <div className="finance-side-nav-icon">
                                            <Icon className="finance-tab-icon" />
                                        </div>
                                        <div className="finance-side-nav-copy">
                                            <span className="finance-side-nav-title">{section.label}</span>
                                            <span className="finance-side-nav-description">{section.description}</span>
                                        </div>
                                    </button>
                                );
                            })}
                        </div>
                    </aside>

                    <section className="finance-main">
                        <div className="finance-main-head finance-glass-panel">
                            <div>
                                <p className="finance-main-kicker">CURRENT SECTION</p>
                                <h2 className="finance-main-title">{currentSection.label}</h2>
                                <p className="finance-main-subtitle">{currentSection.description}</p>
                            </div>
                            <div className="finance-main-actions">
                                {currentTab === 'transactions' && (
                                    <Button variant="outline" size="sm" onClick={() => setIsTransactionImportOpen(true)}>
                                        <Upload className="finance-profile-action-icon" />
                                        CSV/Excel 업로드
                                    </Button>
                                )}
                                {currentTab === 'calendar' && selectedDate && (
                                    <div className="finance-selected-date-chip">{selectedDate}</div>
                                )}
                            </div>
                        </div>

                        <div className="finance-tab-content">
                            {renderCurrentSection()}
                        </div>
                    </section>
                </div>
            </div>

            <AddTransactionDialog
                open={isAddDialogOpen}
                onOpenChange={setIsAddDialogOpen}
                onAddTransaction={handleAddTransaction}
                onAddFixed={handleAddFixed}
            />

            <TransferDialog
                open={isTransferDialogOpen}
                onOpenChange={setIsTransferDialogOpen}
                onAdd={handleAddTransfer}
            />

            <EditTransactionDialog
                open={isEditTransactionDialogOpen}
                onOpenChange={setIsEditTransactionDialogOpen}
                transaction={editingTransaction}
                onUpdate={handleUpdateTransaction}
            />

            {isAdmin && (
                <ScheduleDialog
                    open={isScheduleDialogOpen}
                    onOpenChange={setIsScheduleDialogOpen}
                />
            )}

            <TransactionImportDialog
                open={isTransactionImportOpen}
                onOpenChange={setIsTransactionImportOpen}
            />
        </div>
    );
}
