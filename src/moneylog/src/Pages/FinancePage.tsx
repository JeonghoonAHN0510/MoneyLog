import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { Button } from '../components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '../components/ui/dropdown-menu';
import { CalendarView } from '../components/CalendarView';
import { DashboardView } from '../components/DashboardView';
import { TransactionList } from '../components/TransactionList';
import { AddLedgerDialog } from '../components/AddLedgerDialog';
import { TransferDialog } from '../components/TransferDialog';
import { TakeHomeCalculator } from '../components/TakeHomeCalculator';
import { BudgetManager } from '../components/BudgetManager';
import { AccountManager } from '../components/AccountManager';
import { CategoryManager } from '../components/CategoryManager';
import { Budget, Category, Account, Ledger, Payment } from '../types/finance';
import { Plus, Wallet, Calendar, ChartBar, Calculator, Target, List, User, LogOut } from 'lucide-react';
import { toast } from 'sonner';
import useUserStore from '../stores/authStore';
import useResourceStore from '../stores/resourceStore';
import api from '../api/axiosConfig'; // For user info and logout
import * as ledgerApi from '../api/ledgerApi';
import * as accountApi from '../api/accountApi';
import * as budgetApi from '../api/budgetApi';
import * as categoryApi from '../api/categoryApi';
import * as paymentApi from '../api/paymentApi';
import { getBanks } from '../api/bankApi'; // Assuming a bankApi file exists or will be created

export default function FinancePage() {
  const navigate = useNavigate();
  const { isAuthenticated, userInfo, setUserInfo, logout } = useUserStore();
  const { banks, budgets, categories, accounts, ledgers, payments, setBanks, setBudgets, setCategories, setAccounts, setLedgers, setPayments } = useResourceStore();
  
  const [loading, setLoading] = useState(true);
  const [isAddLedgerDialogOpen, setIsAddLedgerDialogOpen] = useState(false);
  const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<string | undefined>(undefined);

  // Fetch static reference data once
  const fetchReferenceData = useCallback(async () => {
    const [categoriesData, paymentsData, banksData] = await Promise.all([
      categoryApi.getCategories(),
      paymentApi.getPayments(),
      getBanks(), // Assumes getBanks exists
    ]);
    setCategories(categoriesData);
    setPayments(paymentsData);
    setBanks(banksData);
  }, [setCategories, setPayments, setBanks]);

  // Fetch dynamic user assets
  const fetchUserAssets = useCallback(async () => {
    setLoading(true);
    const [accountsData, ledgersData, budgetsData] = await Promise.all([
      accountApi.getAccounts(),
      ledgerApi.getLedgers(),
      budgetApi.getBudgets(),
    ]);
    setAccounts(accountsData);
    setLedgers(ledgersData);
    setBudgets(budgetsData);
    setLoading(false);
  }, [setAccounts, setLedgers, setBudgets]);

  // Initial data load
  useEffect(() => {
    if (isAuthenticated) {
      api.get('/user/info').then(response => setUserInfo(response.data)).catch(() => toast.error('사용자 정보를 불러오는데 실패했습니다.'));
      fetchReferenceData();
      fetchUserAssets();
    }
  }, [isAuthenticated, fetchReferenceData, fetchUserAssets, setUserInfo]);

  // Auth check
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

  // --- [Ledger CRUD Handlers] ---
  const handleAddLedger = async (ledgerData: Omit<Ledger, 'ledger_id' | 'user_id' | 'created_at' | 'updated_at'>) => {
    const newLedger = await ledgerApi.addLedger(ledgerData);
    if (newLedger) {
      // Optimistic update not easy if it affects budget/account balances
      // Re-fetching assets is safer here
      fetchUserAssets(); 
    }
  };

  const handleDeleteLedger = async (ledger_id: number) => {
    const success = await ledgerApi.deleteLedger(ledger_id);
    if (success) {
      fetchUserAssets();
    }
  };

  // --- [Account CRUD Handlers] ---
  const handleAddAccount = async (accountData: Omit<Account, 'account_id' | 'user_id' | 'created_at' | 'updated_at'>) => {
    const newAccount = await accountApi.addAccount(accountData);
    if (newAccount) {
      setAccounts([...accounts, newAccount]);
    }
  };

  const handleUpdateAccount = async (accountData: Account) => {
    const updatedAccount = await accountApi.updateAccount(accountData);
    if (updatedAccount) {
      setAccounts(accounts.map(a => a.account_id === updatedAccount.account_id ? updatedAccount : a));
    }
  };

  const handleDeleteAccount = async (account_id: number) => {
    const success = await accountApi.deleteAccount(account_id);
    if (success) {
      setAccounts(accounts.filter(a => a.account_id !== account_id));
    }
  };

  const handleTransfer = async (from_account_id: number, to_account_id: number, amount: number) => {
    const success = await accountApi.transfer(from_account_id, to_account_id, amount);
    if (success) {
      fetchUserAssets(); // Re-fetch all assets as multiple balances change
    }
  };

  // --- [Category CRUD Handlers] ---
  const handleAddCategory = async (categoryData: Omit<Category, 'category_id' | 'user_id' | 'created_at' | 'updated_at'>) => {
    const newCategory = await categoryApi.addCategory(categoryData);
    if (newCategory) {
      setCategories([...categories, newCategory]);
    }
  };

  const handleUpdateCategory = async (categoryData: Partial<Category> & Pick<Category, 'category_id'>) => {
    const updatedCategory = await categoryApi.updateCategory(categoryData);
    if (updatedCategory) {
      setCategories(categories.map(c => c.category_id === updatedCategory.category_id ? updatedCategory : c));
    }
  };

  const handleDeleteCategory = async (category_id: number) => {
    const success = await categoryApi.deleteCategory(category_id);
    if (success) {
      setCategories(categories.filter(c => c.category_id !== category_id));
    }
  };

  // --- [Budget CRUD Handlers] ---
  const handleAddBudget = async (budgetData: Omit<Budget, 'budget_id' | 'user_id' | 'created_at' | 'updated_at'>) => {
    const newBudget = await budgetApi.addBudget(budgetData);
    if (newBudget) {
      setBudgets([...budgets, newBudget]);
    }
  };
  
  const handleDeleteBudget = async (budget_id: number) => {
    const success = await budgetApi.deleteBudget(budget_id);
    if (success) {
      setBudgets(budgets.filter(b => b.budget_id !== budget_id));
    }
  };

  if (loading || !userInfo) {
    return <div className="flex items-center justify-center min-h-screen"><p>정보를 불러오는 중입니다...</p></div>;
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto p-4 md:p-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="flex items-center gap-2 text-2xl font-bold"><Wallet className="size-8" />내 가계부</h1>
            <p className="text-muted-foreground">사회 초년생을 위한 스마트 재무 관리</p>
          </div>
          <div className="flex items-center gap-3">
            <Button onClick={() => setIsAddLedgerDialogOpen(true)}><Plus className="size-4 mr-2" />거래 추가</Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild><Button variant="outline" className="gap-2"><User className="size-4" /><span className="hidden sm:inline">{userInfo?.name}</span></Button></DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>
                    <p className="text-sm font-medium leading-none">{userInfo?.name}</p>
                    <p className="text-xs leading-none text-muted-foreground">{userInfo?.email}</p>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout} className="text-red-600 cursor-pointer"><LogOut className="size-4 mr-2" />로그아웃</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        {/* Main Content */}
        <Tabs defaultValue="dashboard" className="space-y-6">
          <TabsList className="grid w-full grid-cols-3 md:grid-cols-7">
            <TabsTrigger value="dashboard" className="flex items-center gap-2"><ChartBar className="size-4" />대시보드</TabsTrigger>
            <TabsTrigger value="calendar" className="flex items-center gap-2"><Calendar className="size-4" />캘린더</TabsTrigger>
            <TabsTrigger value="transactions" className="flex items-center gap-2"><Wallet className="size-4" />거래내역</TabsTrigger>
            <TabsTrigger value="accounts" className="flex items-center gap-2"><Wallet className="size-4" />계좌</TabsTrigger>
            <TabsTrigger value="categories" className="flex items-center gap-2"><List className="size-4" />카테고리</TabsTrigger>
            <TabsTrigger value="budget" className="flex items-center gap-2"><Target className="size-4" />예산</TabsTrigger>
            <TabsTrigger value="calculator" className="flex items-center gap-2"><Calculator className="size-4" />계산기</TabsTrigger>
          </TabsList>

          <TabsContent value="dashboard"><DashboardView transactions={ledgers} budgets={budgets} categories={categories} /></TabsContent>
          <TabsContent value="calendar" className="space-y-6">
            <CalendarView transactions={ledgers} onDateClick={setSelectedDate} />
            {selectedDate && <TransactionList transactions={ledgers} categories={categories} selectedDate={selectedDate} onDelete={handleDeleteLedger} />}
          </TabsContent>
          <TabsContent value="transactions"><TransactionList transactions={ledgers} categories={categories} onDelete={handleDeleteLedger} /></TabsContent>
          <TabsContent value="accounts"><AccountManager accounts={accounts} onAdd={handleAddAccount} onUpdate={handleUpdateAccount} onDelete={handleDeleteAccount} onTransferClick={() => setIsTransferDialogOpen(true)} /></TabsContent>
          <TabsContent value="categories"><CategoryManager categories={categories} onAdd={handleAddCategory} onUpdate={handleUpdateCategory} onDelete={handleDeleteCategory} /></TabsContent>
          <TabsContent value="budget"><BudgetManager budgets={budgets} categories={categories} onAdd={handleAddBudget} onDelete={handleDeleteBudget} /></TabsContent>
          <TabsContent value="calculator"><TakeHomeCalculator /></TabsContent>
        </Tabs>
      </div>

      <AddLedgerDialog open={isAddLedgerDialogOpen} onOpenChange={setIsAddLedgerDialogOpen} onAdd={handleAddLedger} categories={categories} accounts={accounts} />
      <TransferDialog open={isTransferDialogOpen} onOpenChange={setIsTransferDialogOpen} onAdd={(data) => handleTransfer(data.from_account_id, data.to_account_id, data.amount)} accounts={accounts} />
    </div>
  );
}