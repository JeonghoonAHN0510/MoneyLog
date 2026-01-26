import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { Button } from '../components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '../components/ui/dropdown-menu';
import { CalendarView } from '../components/CalendarView';
import { DashboardView } from '../components/DashboardView';
import { TransactionList } from '../components/TransactionList';
import { AddTransactionDialog } from '../components/AddTransactionDialog';
import { TransferDialog } from '../components/TransferDialog';
import { TakeHomeCalculator } from '../components/TakeHomeCalculator';
import { BudgetManager } from '../components/BudgetManager';
import { AccountManager } from '../components/AccountManager';
import { CategoryManager } from '../components/CategoryManager';
import { Budget, Category, Account, Ledger } from '../types/finance';
import { Plus, Wallet, Calendar, ChartBar, Calculator, Target, List, User, LogOut } from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import api from '../api/axiosConfig';
import useResourceStore from '../stores/resourceStore';

export default function FinancePage() {
  const navigate = useNavigate();
  const { isAuthenticated, userInfo, setUserInfo, logout } = useUserStore();
  const { banks, budgets, categories, accounts, ledgers, setBanks, setBudgets, setCategories, setAccounts, setLedgers } = useResourceStore();
  
  // [변경] 초기값을 빈 배열로 설정 (Mock Data 제거)
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
      fetchAllData(); // 모든 금융 데이터 가져오기
    }
  }, [isAuthenticated]);

  const fetchUserInfo = async () => {
    try {
      const response = await api.get('/user/info');
      setUserInfo(response.data);
    } catch (error) {
      toast.error('사용자 정보를 불러오는데 실패했습니다.');
    }
  };

  // [추가] 모든 데이터를 서버에서 가져오는 함수
  const fetchAllData = useCallback(async () => {
    try {
      setLoading(true);
      // Promise.all로 병렬 요청 (성능 최적화)
      const [accRes, ledgerRes, budgetRes, catRes, paymentRes, bankRes] = await Promise.allSettled([
        api.get('/account/list'),      // 계좌 목록
        api.get('/ledger'),            // 거래 내역
        api.get('/budget'),            // 예산 목록
        api.get('/category'),          // 카테고리 목록
        api.get('/payment'),           // 결제수단
        api.get('/bank')               // 은행
      ]);

      // 성공한 요청만 State에 반영
      if (accRes.status === 'fulfilled') setAccounts(accRes.value.data);
      if (ledgerRes.status === 'fulfilled') setLedgers(ledgerRes.value.data);
      if (budgetRes.status === 'fulfilled') setBudgets(budgetRes.value.data);
      if (catRes.status === 'fulfilled') setCategories(catRes.value.data);
      if (paymentRes.status === 'fulfilled') setCategories(paymentRes.value.data);
      if (bankRes.status === 'fulfilled') setBanks(bankRes.value.data);
      
      // Transfer는 별도 엔드포인트가 있다면 추가 필요
      // const transferRes = await api.get('/transfer/list');
      // setTransfers(transferRes.data);

    } catch (error) {
      console.error("데이터 로드 실패:", error);
      toast.error('데이터를 불러오는 중 일부 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

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
      // 토큰 만료 등으로 API 실패해도 클라이언트 로그아웃은 진행
      logout();
      navigate('/');
    }
  };

  // --- [Transaction CRUD] ---
  const handleAddTransaction = async (transaction: Omit<Ledger, 'id'>) => {
    try {
      await api.post('/transaction', transaction);
      toast.success("거래 내역이 추가되었습니다.");
      fetchAllData(); // 목록 새로고침
    } catch (e) {
      toast.error("거래 내역 추가 실패");
    }
  };

  const handleDeleteTransaction = async (id: string) => {
    try {
      await api.delete(`/transaction/${id}`);
      toast.success("삭제되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("삭제 실패");
    }
  };

  // --- [Budget CRUD] ---
  const handleAddBudget = async (budget: Omit<Budget, 'id'>) => {
    try {
      await api.post('/budget', budget);
      toast.success("예산이 설정되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("예산 설정 실패");
    }
  };

  const handleDeleteBudget = async (id: string) => {
    try {
      await api.delete(`/budget/${id}`);
      toast.success("예산이 삭제되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("삭제 실패");
    }
  };

  // --- [Account CRUD] ---
  const handleAddAccount = async (newAccountData: any) => {
    try {
      await api.post('/account', newAccountData);
      toast.success("계좌가 추가되었습니다.");
      fetchAllData(); // 계좌 추가 후 목록 갱신
    } catch (e) {
      toast.error("추가 실패");
    }
  };

  const handleUpdateAccount = async (id: number, updateData: any) => {
    try {
      await api.put(`/account/${id}`, updateData);
      toast.success("계좌가 수정되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("수정 실패");
    }
  };

  const handleDeleteAccount = async (id: number) => {
    try {
      await api.delete(`/account/${id}`);
      toast.success("계좌가 삭제되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("삭제 실패");
    }
  };

  // --- [Category CRUD] ---
  const handleAddCategory = async (category: Omit<Category, 'id'>) => {
    try {
      await api.post('/category', category);
      toast.success("카테고리가 추가되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("추가 실패");
    }
  };

  const handleUpdateCategory = async (id: string, updates: Partial<Category>) => {
    try {
      await api.put(`/category/${id}`, updates);
      toast.success("수정되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("수정 실패");
    }
  };

  const handleDeleteCategory = async (id: string) => {
    try {
      await api.delete(`/category/${id}`);
      toast.success("삭제되었습니다.");
      fetchAllData();
    } catch (e) {
      toast.error("삭제 실패");
    }
  };

  // --- [Transfer Logic] ---
  const handleAddTransfer = async (transfer: Omit<Ledger, 'id'>) => {
    try {
      await api.post('/account/transfer', transfer); // 이체 API 호출
      toast.success("이체가 완료되었습니다.");
      fetchAllData(); // 계좌 잔액 변동 반영을 위해 전체 갱신
    } catch (e) {
      toast.error("이체 실패");
    }
  };

  const handleDateClick = (date: string) => {
    setSelectedDate(date);
  };

  // 로딩 화면
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
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="flex items-center gap-2">
              <Wallet className="size-8" />
              내 가계부
            </h1>
            <p className="text-muted-foreground">사회 초년생을 위한 스마트 재무 관리</p>
          </div>
          <div className="flex items-center gap-3">
            <Button onClick={() => setIsAddDialogOpen(true)}>
              <Plus className="size-4 mr-2" />
              거래 추가
            </Button>
            
            {/* User Menu */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" className="gap-2">
                  <User className="size-4" />
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
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout} className="text-red-600 cursor-pointer">
                  <LogOut className="size-4 mr-2" />
                  로그아웃
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        {/* Main Content */}
        <Tabs defaultValue="dashboard" className="space-y-6">
          <TabsList className="grid w-full grid-cols-3 md:grid-cols-7">
            <TabsTrigger value="dashboard" className="flex items-center gap-2">
              <ChartBar className="size-4" />
              <span className="hidden sm:inline">대시보드</span>
            </TabsTrigger>
            <TabsTrigger value="calendar" className="flex items-center gap-2">
              <Calendar className="size-4" />
              <span className="hidden sm:inline">캘린더</span>
            </TabsTrigger>
            <TabsTrigger value="transactions" className="flex items-center gap-2">
              <Wallet className="size-4" />
              <span className="hidden sm:inline">거래내역</span>
            </TabsTrigger>
            <TabsTrigger value="accounts" className="flex items-center gap-2">
              <Wallet className="size-4" />
              <span className="hidden sm:inline">계좌</span>
            </TabsTrigger>
            <TabsTrigger value="categories" className="flex items-center gap-2">
              <List className="size-4" />
              <span className="hidden sm:inline">카테고리</span>
            </TabsTrigger>
            <TabsTrigger value="budget" className="flex items-center gap-2">
              <Target className="size-4" />
              <span className="hidden sm:inline">예산</span>
            </TabsTrigger>
            <TabsTrigger value="calculator" className="flex items-center gap-2">
              <Calculator className="size-4" />
              <span className="hidden sm:inline">계산기</span>
            </TabsTrigger>
          </TabsList>

          <TabsContent value="dashboard" className="space-y-6">
            <DashboardView
              transactions={ledgers}
              budgets={budgets}
              categories={categories}
            />
          </TabsContent>

          <TabsContent value="calendar" className="space-y-6">
            <CalendarView transactions={ledgers} onDateClick={handleDateClick} />
            {selectedDate && (
              <TransactionList
                transactions={ledgers}
                categories={categories}
                selectedDate={selectedDate}
                onDelete={handleDeleteTransaction}
              />
            )}
          </TabsContent>

          <TabsContent value="transactions" className="space-y-6">
            <TransactionList
              transactions={ledgers}
              categories={categories}
              onDelete={handleDeleteTransaction}
            />
          </TabsContent>

          <TabsContent value="accounts" className="space-y-6">
            <AccountManager
              accounts={accounts}
              onAdd={handleAddAccount}
              onUpdate={handleUpdateAccount}
              onDelete={handleDeleteAccount}
              onTransferClick={() => setIsTransferDialogOpen(true)}
            />
          </TabsContent>

          <TabsContent value="categories" className="space-y-6">
            <CategoryManager
              categories={categories}
              onAdd={handleAddCategory}
              onUpdate={handleUpdateCategory}
              onDelete={handleDeleteCategory}
            />
          </TabsContent>

          <TabsContent value="budget" className="space-y-6">
            <BudgetManager
              budgets={budgets}
              categories={categories}
              onAdd={handleAddBudget}
              onDelete={handleDeleteBudget}
            />
          </TabsContent>

          <TabsContent value="calculator" className="space-y-6">
            <TakeHomeCalculator />
          </TabsContent>
        </Tabs>
      </div>

      {/* Add Transaction Dialog */}
      <AddTransactionDialog
        open={isAddDialogOpen}
        onOpenChange={setIsAddDialogOpen}
        onAdd={handleAddTransaction}
        categories={categories}
        accounts={accounts}
      />

      {/* Transfer Dialog */}
      <TransferDialog
        open={isTransferDialogOpen}
        onOpenChange={setIsTransferDialogOpen}
        onAdd={handleAddTransfer}
        accounts={accounts}
      />
    </div>
  );
}