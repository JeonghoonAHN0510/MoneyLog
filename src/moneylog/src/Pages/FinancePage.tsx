import { useState, useEffect, useRef, type ChangeEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { Button } from '../components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger
} from '../components/ui/dropdown-menu';
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/avatar';
import { CalendarView } from '../components/CalendarView';
import { DashboardView } from '../components/DashboardView';
import { TransactionList } from '../components/TransactionList';
import { AddTransactionDialog } from '../components/AddTransactionDialog';
import { EditTransactionDialog } from '../components/EditTransactionDialog';
import { TransferDialog } from '../components/TransferDialog';
import { TransactionImportDialog } from '../components/TransactionImportDialog';
import { ScheduleDialog } from '../components/ScheduleDialog';
import { TakeHomeCalculator } from '../components/TakeHomeCalculator';
import { BudgetManager } from '../components/BudgetManager';
import { AccountManager } from '../components/AccountManager';
import { CategoryManager } from '../components/CategoryManager';
import { Budget, Category, Account, Transaction, Payment, Transfer, Fixed } from '../types/finance';
import {
    Plus,
    Wallet,
    Calendar,
    ChartBar,
    Calculator,
    Target,
    List,
    LogOut,
    ImagePlus,
    RefreshCcw,
    Upload
} from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import api from '../api/axiosConfig';
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
import { getApiErrorMessage, getApiErrorStatus } from '../utils/error';
import { buildProfileImageViewUrl, getProfileInitial } from '../utils/profileImage';
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
    const currentTab = searchParams.get('tab') || 'dashboard';

    const handleTabChange = (value: string) => {
        setSearchParams({ tab: value });
    };

    // --- [TanStack Query] 서버 데이터 ---
    const { data: userInfo, isLoading: userInfoLoading, error: userInfoError } = useUserInfo();
    const refreshTokenMut = useRefreshToken();

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
            logout();
            navigate('/login');
            return;
        }

        toast.error(getApiErrorMessage(userInfoError, '사용자 정보를 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.'));
    }, [userInfoError, logout, navigate]);

    const handleLogout = async () => {
        try {
            await api.post('/user/logout');
            logout();
            toast.success('로그아웃 되었습니다.');
            navigate('/');
        } catch (error) {
            logout();
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
                api.defaults.headers.common['Authorization'] = `Bearer ${result.accessToken}`;
            },
            onError: () => {
                logout();
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
            <div className="finance-container">
                <input
                    ref={profileImageInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleProfileImageChange}
                />
                <div className="finance-header">
                    <div>
                        <h1 className="finance-title">
                            <Wallet className="finance-title-icon" />
                            내 가계부
                        </h1>
                        <p className="finance-subtitle">사회 초년생을 위한 스마트 재무 관리</p>
                    </div>
                    <div className="finance-actions">
                        <Button size="sm" onClick={() => setIsAddDialogOpen(true)}>
                            <Plus className="finance-add-btn-icon" />
                            <span className="finance-add-btn-text">거래 추가</span>
                        </Button>

                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="outline" className="finance-user-btn">
                                    <Avatar className="finance-user-avatar">
                                        {profileImageViewUrl && (
                                            <AvatarImage src={profileImageViewUrl} alt={`${userInfo.name} profile`} />
                                        )}
                                        <AvatarFallback className="finance-user-avatar-fallback">
                                            {getProfileInitial(userInfo.name)}
                                        </AvatarFallback>
                                    </Avatar>
                                    <span className="finance-user-name">{userInfo?.name}</span>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="finance-dropdown-content">
                                <DropdownMenuLabel>
                                    <div className="finance-dropdown-label-inner">
                                        <Avatar className="finance-dropdown-avatar">
                                            {profileImageViewUrl && (
                                                <AvatarImage src={profileImageViewUrl} alt={`${userInfo.name} profile`} />
                                            )}
                                            <AvatarFallback className="finance-user-avatar-fallback">
                                                {getProfileInitial(userInfo.name)}
                                            </AvatarFallback>
                                        </Avatar>
                                        <div className="finance-dropdown-userinfo">
                                            <p className="finance-dropdown-name">{userInfo?.name}</p>
                                            <p className="finance-dropdown-email">
                                                {userInfo?.email}
                                            </p>
                                        </div>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                    onClick={openProfileImagePicker}
                                    className="cursor-pointer"
                                    disabled={updateProfileImageMut.isPending}
                                >
                                    <ImagePlus className="mr-2 h-4 w-4" />
                                    <span>프로필 이미지 변경</span>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                    onClick={handleRefreshSession}
                                    className="cursor-pointer"
                                    disabled={refreshTokenMut.isPending}
                                >
                                    <RefreshCcw className="mr-2 h-4 w-4" />
                                    <span>로그인 연장</span>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={() => setIsScheduleDialogOpen(true)} className="cursor-pointer">
                                    <Target className="mr-2 h-4 w-4" />
                                    <span>스케줄 설정</span>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleLogout} className="finance-logout-item">
                                    <LogOut className="finance-logout-icon" />
                                    로그아웃
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>

                <Tabs
                    value={currentTab}
                    onValueChange={handleTabChange}
                    className="finance-tabs"
                >
                    <div className="finance-tabs-scroll">
                        <TabsList className="finance-tabs-list">
                            <TabsTrigger value="dashboard" className="finance-tab-trigger">
                                <ChartBar className="finance-tab-icon" />
                                <span className="finance-tab-text">대시보드</span>
                            </TabsTrigger>
                            <TabsTrigger value="calendar" className="finance-tab-trigger">
                                <Calendar className="finance-tab-icon" />
                                <span className="finance-tab-text">캘린더</span>
                            </TabsTrigger>
                            <TabsTrigger value="transactions" className="finance-tab-trigger">
                                <Wallet className="finance-tab-icon" />
                                <span className="finance-tab-text">거래내역</span>
                            </TabsTrigger>
                            <TabsTrigger value="accounts" className="finance-tab-trigger">
                                <Wallet className="finance-tab-icon" />
                                <span className="finance-tab-text">계좌</span>
                            </TabsTrigger>
                            <TabsTrigger value="categories" className="finance-tab-trigger">
                                <List className="finance-tab-icon" />
                                <span className="finance-tab-text">카테고리</span>
                            </TabsTrigger>
                            <TabsTrigger value="budget" className="finance-tab-trigger">
                                <Target className="finance-tab-icon" />
                                <span className="finance-tab-text">예산</span>
                            </TabsTrigger>
                            <TabsTrigger value="calculator" className="finance-tab-trigger">
                                <Calculator className="finance-tab-icon" />
                                <span className="finance-tab-text">계산기</span>
                            </TabsTrigger>
                        </TabsList>
                    </div>

                    <TabsContent value="dashboard" className="finance-tab-content">
                        <DashboardView />
                    </TabsContent>

                    <TabsContent value="calendar" className="finance-tab-content">
                        <CalendarView onDateClick={handleDateClick} />
                        {selectedDate && (
                            <TransactionList
                                selectedDate={selectedDate}
                                onEdit={handleEditTransaction}
                                onDelete={handleDeleteTransaction}
                            />
                        )}
                    </TabsContent>

                    <TabsContent value="transactions" className="finance-tab-content">
                        <Button size="sm" onClick={() => setIsTransactionImportOpen(true)}>
                            <Upload className="h-4 w-4 mr-2" />
                            거래 업로드(CSV/Excel)
                        </Button>
                        <TransactionList
                            onEdit={handleEditTransaction}
                            onDelete={handleDeleteTransaction}
                        />
                    </TabsContent>

                    <TabsContent value="accounts" className="finance-tab-content">
                        <AccountManager
                            onAdd={handleAddAccount}
                            onUpdate={handleUpdateAccount}
                            onDelete={handleDeleteAccount}
                            onTransferClick={() => setIsTransferDialogOpen(true)}
                        />
                    </TabsContent>

                    <TabsContent value="categories" className="finance-tab-content">
                        <CategoryManager
                            onAdd={handleAddCategory}
                            onUpdate={handleUpdateCategory}
                            onDelete={handleDeleteCategory}
                            onAddPayment={handleAddPayment}
                            onUpdatePayment={handleUpdatePayment}
                            onDeletePayment={handleDeletePayment}
                        />
                    </TabsContent>

                    <TabsContent value="budget" className="finance-tab-content">
                        <BudgetManager
                            onAdd={handleAddBudget}
                            onUpdate={handleUpdateBudget}
                            onDelete={handleDeleteBudget}
                        />
                    </TabsContent>

                    <TabsContent value="calculator" className="finance-tab-content">
                        <TakeHomeCalculator />
                    </TabsContent>
                </Tabs>
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

            <ScheduleDialog
                open={isScheduleDialogOpen}
                onOpenChange={setIsScheduleDialogOpen}
            />

            <TransactionImportDialog
                open={isTransactionImportOpen}
                onOpenChange={setIsTransactionImportOpen}
            />
        </div>
    );
}
