import { useState, useEffect, useCallback } from 'react';
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
import { CalendarView } from '../components/CalendarView';
import { DashboardView } from '../components/DashboardView';
import { TransactionList } from '../components/TransactionList';
import { AddTransactionDialog } from '../components/AddTransactionDialog';
import { TransferDialog } from '../components/TransferDialog';
import { TakeHomeCalculator } from '../components/TakeHomeCalculator';
import { BudgetManager } from '../components/BudgetManager';
import { AccountManager } from '../components/AccountManager';
import { CategoryManager } from '../components/CategoryManager';
import { Budget, Category, Account, Transaction, Payment, Transfer, Fixed } from '../types/finance';
import { Plus, Wallet, Calendar, ChartBar, Calculator, Target, List, User, LogOut } from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import api from '../api/axiosConfig';
import useResourceStore from '../stores/resourceStore';

export default function FinancePage() {
    const navigate = useNavigate();
    const { isAuthenticated, userInfo, setUserInfo, logout } = useUserStore();
    const {
        banks,
        budgets,
        categories,
        accounts,
        transactions,
        payments,
        setBanks,
        setBudgets,
        setCategories,
        setAccounts,
        setTransactions,
        setPayments
    } = useResourceStore();

    const [loading, setLoading] = useState(true);
    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
    const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState<string | undefined>(undefined);

    const [searchParams, setSearchParams] = useSearchParams();
    const currentTab = searchParams.get('tab') || 'dashboard';

    const handleTabChange = (value: string) => {
        setSearchParams({ tab: value });
    };

    // 1. 사용자 정보 및 초기 데이터 로드
    useEffect(() => {
        if (isAuthenticated) {
            fetchUserInfo();
            fetchReferenceData();
            fetchUserAssets();
        }
    }, [isAuthenticated]);

    const fetchUserInfo = async () => {
        try {
            const response = await api.get('/user/info');
            setUserInfo(response.data);
        } catch (error) {
            toast.error('사용자 정보를 불러오는데 실패했습니다.');
            logout();
            navigate('/login');
        }
    };

    // 1. [정적 데이터] 앱 켤 때 한 번만 불러오면 되는 것들
    const fetchReferenceData = useCallback(async () => {
        try {
            const [catRes, paymentRes, bankRes] = await Promise.allSettled([
                api.get('/category'),
                api.get('/payment'),
                api.get('/bank')
            ]);

            if (catRes.status === 'fulfilled') setCategories(catRes.value.data);
            if (paymentRes.status === 'fulfilled') setPayments(paymentRes.value.data);
            if (bankRes.status === 'fulfilled') setBanks(bankRes.value.data);
        } catch (e) {
            console.error("기준 정보 로드 실패", e);
        }
    }, [setCategories, setPayments, setBanks]);

    // 2. [동적 데이터] 거래 내역 추가 시 갱신해야 할 것들
    const fetchUserAssets = useCallback(async () => {
        try {
            const [accRes, transactionRes, budgetRes] = await Promise.allSettled([
                api.get('/account/list'), 
                api.get('/transaction'),
                api.get('/budget')         
            ]);

            if (accRes.status === 'fulfilled') setAccounts(accRes.value.data);
            if (transactionRes.status === 'fulfilled') setTransactions(transactionRes.value.data);
            if (budgetRes.status === 'fulfilled') setBudgets(budgetRes.value.data);
        } catch (e) {
            console.error("자산 정보 로드 실패", e);
        } finally {
            setLoading(false);
        }
    }, [setAccounts, setTransactions, setBudgets]);

    // 3. useEffect에서 최초 1회는 둘 다 실행
    useEffect(() => {
        fetchReferenceData();
        fetchUserAssets();
    }, [fetchReferenceData, fetchUserAssets]);

    // 로그인 체크
    useEffect(() => {
        if (!isAuthenticated && !loading) {
            toast.error('로그인이 필요합니다');
            navigate('/login');
        }
    }, [isAuthenticated, loading, navigate]);

    const handleLogout = async () => {
        try {
            await api.post('/user/logout');
            logout();
            toast.success('로그아웃 되었습니다');
            navigate('/');
        } catch (error) {
            logout();
            navigate('/');
        }
    };

    // --- [Transaction CRUD] ---
    const handleAddTransaction = async (transaction: Partial<Transaction>) => {
        try {
            await api.post('/transaction', transaction);
            toast.success("거래 내역이 추가되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("거래 내역 추가에 실패하였습니다.");
        }
    };

    const handleAddFixed = async (fixed: Partial<Fixed>) => {
        try {
            await api.post('/fixed', fixed);
            toast.success("고정 거래 내역이 추가되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("고정 거래 내역 추가에 실패하였습니다.");
        }
    };

    const handleUpdateTransaction = async (transaction: Partial<Transaction>) => {
        try {
            await api.put('/transaction', transaction);
            toast.success("거래 내역이 수정되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("거래 내역 수정 실패");
        }
    };

    const handleDeleteTransaction = async (transactionId: string) => {
        try {
            await api.delete(`/transaction?transactionId=${transactionId}`);
            toast.success("거래 내역이 삭제되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("거래 내역 삭제에 실패하였습니다.");
        }
    };

    // --- [Budget CRUD] ---
    const handleAddBudget = async (budget: Omit<Budget, 'budgetId' | "userId" | "budgetDate" | "createdAt" | "updatedAt" | "categoryName">) => {
        try {
            await api.post('/budget', budget);
            toast.success("예산이 설정되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("예산 설정에 실패하였습니다.");
        }
    };

    const handleUpdateBudget = async (budget: Partial<Budget>) => {
        try {
            await api.put(`/budget`, budget);
            toast.success("예산이 수정되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("예산 수정에 실패하였습니다.");
        }
    };

    const handleDeleteBudget = async (budgetId: string) => {
        try {
            await api.delete(`/budget?budgetId=${budgetId}`);
            toast.success("예산이 삭제되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("예산 삭제에 실패하였습니다.");
        }
    };

    // --- [Account CRUD] ---
    const handleAddAccount = async (account: Omit<Account, "accountId" | "userId" | "createdAt" | "updatedAt" | "bankName">) => {
        try {
            await api.post('/account', account);
            toast.success("계좌가 추가되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("계좌 추가에 실패하였습니다.");
        }
    };

    const handleUpdateAccount = async (account: Partial<Account>) => {
        try {
            await api.put(`/account`, account);
            toast.success("계좌가 수정되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("계좌 수정에 실패하였습니다.");
        }
    };

    const handleDeleteAccount = async (accountId: string) => {
        try {
            await api.delete(`/account?accountId=${accountId}`);
            toast.success("계좌가 삭제되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("계좌 삭제에 실패하였습니다.");
        }
    };

    // --- [Category CRUD] ---
    const handleAddCategory = async (category: Omit<Category, "categoryId" | "userId" | "createdAt" | "updatedAt">) => {
        try {
            await api.post('/category', category);
            toast.success("카테고리가 추가되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("카테고리 추가에 실패하였습니다.");
        }
    };

    const handleUpdateCategory = async (category: Partial<Category>) => {
        try {
            await api.put(`/category`, category);
            toast.success("카테고리가 수정되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("카테고리 수정에 실패하였습니다.");
        }
    };

    const handleDeleteCategory = async (categoryId: string) => {
        try {
            await api.delete(`/category?categoryId=${categoryId}`);
            toast.success("카테고리가 삭제되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("카테고리 삭제에 실패하였습니다.");
        }
    };

    // --- [Payment CRUD] ---
    const handleAddPayment = async (payment: Omit<Payment, "paymentId" | "userId" | "createdAt" | "updatedAt">) => {
        try {
            await api.post('/payment', payment);
            toast.success("결제수단이 추가되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("결제수단 추가에 실패하였습니다.");
        }
    };

    const handleUpdatePayment = async (payment: Partial<Payment>) => {
        try {
            await api.put(`/payment`, payment);
            toast.success("결제수단이 수정되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("결제수단 수정에 실패하였습니다.");
        }
    };

    const handleDeletePayment = async (paymentId: string) => {
        try {
            await api.delete(`/payment?paymentId=${paymentId}`);
            toast.success("결제수단이 삭제되었습니다.");
            fetchReferenceData();
        } catch (e) {
            toast.error("결제수단 삭제에 실패하였습니다.");
        }
    };

    // --- [Transfer Logic] ---
    const handleAddTransfer = async (transfer: Omit<Transfer, "transferId" | "userId" | "createdAt" | "updatedAt">) => {
        try {
            await api.put('/account/transfer', transfer);
            toast.success("이체가 완료되었습니다.");
            fetchUserAssets();
        } catch (e) {
            toast.error("이체에 실패하였습니다.");
        }
    };

    const handleDateClick = (date: string) => {
        setSelectedDate(date);
    };

    if (loading || !userInfo) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <p className="text-lg text-gray-500">정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (!isAuthenticated) return null;

    return (
        <div className="min-h-screen bg-background">
            <div className="container mx-auto p-4 md:p-8">
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="flex items-center gap-2 text-2xl font-bold">
                            <Wallet className="size-8"/>
                            내 가계부
                        </h1>
                        <p className="text-muted-foreground">사회 초년생을 위한 스마트 재무 관리</p>
                    </div>
                    <div className="flex items-center gap-3">
                        <Button onClick={() => setIsAddDialogOpen(true)}>
                            <Plus className="size-4 mr-2"/>
                            거래 추가
                        </Button>

                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="outline" className="gap-2">
                                    <User className="size-4"/>
                                    <span className="hidden sm:inline">{userInfo?.name}</span>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-56">
                                <DropdownMenuLabel>
                                    <div className="flex flex-col space-y-1">
                                        <p className="text-sm font-medium leading-none">{userInfo?.name}</p>
                                        <p className="text-xs leading-none text-muted-foreground">
                                            {userInfo?.email}
                                        </p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem onClick={handleLogout} className="text-red-600 cursor-pointer">
                                    <LogOut className="size-4 mr-2"/>
                                    로그아웃
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>

                <Tabs 
                    value={currentTab} 
                    onValueChange={handleTabChange} 
                    className="space-y-6"
                >
                    <TabsList className="grid w-full grid-cols-3 md:grid-cols-7">
                        <TabsTrigger value="dashboard" className="flex items-center gap-2">
                            <ChartBar className="size-4"/>
                            <span className="hidden sm:inline">대시보드</span>
                        </TabsTrigger>
                        <TabsTrigger value="calendar" className="flex items-center gap-2">
                            <Calendar className="size-4"/>
                            <span className="hidden sm:inline">캘린더</span>
                        </TabsTrigger>
                        <TabsTrigger value="transactions" className="flex items-center gap-2">
                            <Wallet className="size-4"/>
                            <span className="hidden sm:inline">거래내역</span>
                        </TabsTrigger>
                        <TabsTrigger value="accounts" className="flex items-center gap-2">
                            <Wallet className="size-4"/>
                            <span className="hidden sm:inline">계좌</span>
                        </TabsTrigger>
                        <TabsTrigger value="categories" className="flex items-center gap-2">
                            <List className="size-4"/>
                            <span className="hidden sm:inline">카테고리</span>
                        </TabsTrigger>
                        <TabsTrigger value="budget" className="flex items-center gap-2">
                            <Target className="size-4"/>
                            <span className="hidden sm:inline">예산</span>
                        </TabsTrigger>
                        <TabsTrigger value="calculator" className="flex items-center gap-2">
                            <Calculator className="size-4"/>
                            <span className="hidden sm:inline">계산기</span>
                        </TabsTrigger>
                    </TabsList>

                    <TabsContent value="dashboard" className="space-y-6">
                        <DashboardView
                            transactions={transactions}
                            budgets={budgets}
                            categories={categories}
                        />
                    </TabsContent>

                    <TabsContent value="calendar" className="space-y-6">
                        <CalendarView transactions={transactions} onDateClick={handleDateClick}/>
                        {selectedDate && (
                            <TransactionList
                                selectedDate={selectedDate}
                                onDelete={handleDeleteTransaction}
                            />
                        )}
                    </TabsContent>

                    <TabsContent value="transactions" className="space-y-6">
                        <TransactionList
                            onDelete={handleDeleteTransaction}
                        />
                    </TabsContent>

                    <TabsContent value="accounts" className="space-y-6">
                        <AccountManager
                            onAdd={handleAddAccount}
                            onUpdate={handleUpdateAccount}
                            onDelete={handleDeleteAccount}
                            onTransferClick={() => setIsTransferDialogOpen(true)}
                        />
                    </TabsContent>

                    <TabsContent value="categories" className="space-y-6">
                        <CategoryManager
                            onAdd={handleAddCategory}
                            onUpdate={handleUpdateCategory}
                            onDelete={handleDeleteCategory}
                            onAddPayment={handleAddPayment}
                            onUpdatePayment={handleUpdatePayment}
                            onDeletePayment={handleDeletePayment}
                        />
                    </TabsContent>

                    <TabsContent value="budget" className="space-y-6">
                        <BudgetManager
                            onAdd={handleAddBudget}
                            onUpdate={handleUpdateBudget}
                            onDelete={handleDeleteBudget}
                        />
                    </TabsContent>

                    <TabsContent value="calculator" className="space-y-6">
                        <TakeHomeCalculator/>
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
        </div>
    );
}