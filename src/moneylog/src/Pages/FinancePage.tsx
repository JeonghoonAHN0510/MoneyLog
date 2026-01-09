import { useState, useEffect } from 'react';
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
import { Transaction, Budget, Category, Account, Transfer } from '../types/finance';
import {
  mockTransactions,
  mockBudgets,
  mockAccounts,
  defaultExpenseCategories,
  defaultIncomeCategories,
} from '../data/mockData';
import { Plus, Wallet, Calendar, ChartBar, Calculator, Target, List, Settings, ArrowRightLeft, User, LogOut } from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import api from '../api/axiosConfig';

export default function FinancePage() {
  const navigate = useNavigate();
  const { isAuthenticated, userInfo, setUserInfo, logout } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [transactions, setTransactions] = useState<Transaction[]>(mockTransactions);
  const [budgets, setBudgets] = useState<Budget[]>(mockBudgets);
  const [accounts, setAccounts] = useState<Account[]>(mockAccounts);
  const [transfers, setTransfers] = useState<Transfer[]>([]);
  const [categories, setCategories] = useState<Category[]>([
    ...defaultExpenseCategories,
    ...defaultIncomeCategories,
  ]);
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<string | undefined>(undefined);
  const [activeTab, setActiveTab] = useState('dashboard');

  const [searchParams, setSearchParams] = useSearchParams();
  const currentTab = searchParams.get('tab') || 'dashboard';
  const handleTabChange = (value: string) => {
    setSearchParams({ tab: value });
  };

  useEffect(() => {
    fetchUserInfo();
  }, [])

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      const response = await api.get('/user/info');
      setUserInfo(response.data);
    } catch (error) {
      toast.error('정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }

  // 로그인 상태 확인
  useEffect(() => {
    if (!isAuthenticated) {
      toast.error('로그인이 필요합니다');
      navigate('/login');
    }
  }, [navigate]);

  const handleLogout = async () => {
    try {
      const response = await api.post('/user/logout');
      if (response.data) {
        logout();
        toast.success('로그아웃 되었습니다');
        navigate('/');
      } else {
        toast.error('로그아웃 중 오류가 발생했습니다.');
      }
    } catch (error) {
      toast.error('로그아웃 중 오류가 발생했습니다.');
    }
  };

  const handleAddTransaction = (transaction: Omit<Transaction, 'id'>) => {
    const newTransaction: Transaction = {
      ...transaction,
      id: Date.now().toString(),
    };
    setTransactions([...transactions, newTransaction]);
  };

  const handleDeleteTransaction = (id: string) => {
    setTransactions(transactions.filter((t) => t.id !== id));
  };

  const handleAddBudget = (budget: Omit<Budget, 'id'>) => {
    const newBudget: Budget = {
      ...budget,
      id: Date.now().toString(),
    };
    setBudgets([...budgets, newBudget]);
  };

  const handleDeleteBudget = (id: string) => {
    setBudgets(budgets.filter((b) => b.id !== id));
  };

  const handleAddAccount = (account: Omit<Account, 'id'>) => {
    const newAccount: Account = {
      ...account,
      id: Date.now().toString(),
    };
    setAccounts([...accounts, newAccount]);
  };

  const handleUpdateAccount = (id: string, updates: Partial<Account>) => {
    setAccounts(accounts.map((a) => (a.id === id ? { ...a, ...updates } : a)));
  };

  const handleDeleteAccount = (id: string) => {
    setAccounts(accounts.filter((a) => a.id !== id));
  };

  const handleAddCategory = (category: Omit<Category, 'id'>) => {
    const newCategory: Category = {
      ...category,
      id: Date.now().toString(),
    };
    setCategories([...categories, newCategory]);
  };

  const handleUpdateCategory = (id: string, updates: Partial<Category>) => {
    setCategories(categories.map((c) => (c.id === id ? { ...c, ...updates } : c)));
  };

  const handleDeleteCategory = (id: string) => {
    setCategories(categories.filter((c) => c.id !== id));
  };

  const handleAddTransfer = (transfer: Omit<Transfer, 'id'>) => {
    const newTransfer: Transfer = {
      ...transfer,
      id: Date.now().toString(),
    };
    setTransfers([...transfers, newTransfer]);

    // Update account balances
    setAccounts(accounts.map((acc) => {
      if (acc.id === transfer.fromAccountId) {
        return { ...acc, balance: acc.balance - transfer.amount };
      }
      if (acc.id === transfer.toAccountId) {
        return { ...acc, balance: acc.balance + transfer.amount };
      }
      return acc;
    }));
  };

  const handleDeleteTransfer = (id: string) => {
    setTransfers(transfers.filter((t) => t.id !== id));
  };

  const handleDateClick = (date: string) => {
    setSelectedDate(date);
  };

  // [수정된 부분] 로딩 중이거나 userInfo가 없으면 로딩 화면을 보여줍니다.
  if (loading || !userInfo) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg text-gray-500">정보를 불러오는 중입니다...</p>
      </div>
    );
  }
  if (!isAuthenticated) {
    return null;
  }

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
        <Tabs value={currentTab}
          onValueChange={handleTabChange}
          className="space-y-6">
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
              transactions={transactions}
              budgets={budgets}
              categories={categories}
            />
          </TabsContent>

          <TabsContent value="calendar" className="space-y-6">
            <CalendarView transactions={transactions} onDateClick={handleDateClick} />
            {selectedDate && (
              <TransactionList
                transactions={transactions}
                categories={categories}
                selectedDate={selectedDate}
                onDelete={handleDeleteTransaction}
              />
            )}
          </TabsContent>

          <TabsContent value="transactions" className="space-y-6">
            <TransactionList
              transactions={transactions}
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
