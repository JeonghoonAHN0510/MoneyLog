import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Account } from '../types/finance';
import { Wallet, Trash, Plus, Pencil, ArrowRightLeft } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from './ui/dialog';
import useResourceStore from '../stores/resourceStore'; // 은행 목록 스토어
import { toast } from 'sonner';

// 부모 컴포넌트(FinancePage)에서 내려받을 Props 정의
interface AccountManagerProps {
  accounts: Account[];
  onAdd: (account: any) => void; 
  onUpdate: (id: number, account: any) => void;
  onDelete: (id: number) => void;
  onTransferClick?: () => void;
}

const defaultColors = ['#3b82f6', '#ef4444', '#22c55e', '#eab308', '#8b5cf6', '#ec4899', '#06b6d4'];

export function AccountManager({ accounts, onAdd, onUpdate, onDelete, onTransferClick }: AccountManagerProps) {
  const { banks } = useResourceStore(); // 은행 목록 가져오기

  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);

  // 폼 입력 상태 관리
  const [name, setName] = useState(''); // 계좌 별칭 (nickname)
  // account_type 상태 제거됨
  const [balance, setBalance] = useState('');
  const [color, setColor] = useState(defaultColors[0]);
  
  // 은행 관련 필드
  const [bankId, setBankId] = useState<string>('');
  const [accountNumber, setAccountNumber] = useState('');

  // 폼 초기화
  const resetForm = () => {
    setName('');
    // type 초기화 제거
    setBalance('');
    setColor(defaultColors[0]);
    setBankId('');
    setAccountNumber('');
  };

  // [CREATE] 계좌 추가 핸들러
  const handleAdd = () => {
    // 유효성 검사
    if (!name) {
      toast.error('계좌 별칭을 입력해주세요.');
      return;
    }
    
    // 부모 컴포넌트의 추가 함수 호출 (API 연결용)
    onAdd({
      nickname: name,
      // account_type 제거됨
      balance: parseFloat(balance) || 0,
      bank_id: bankId ? Number(bankId) : null, // 값이 있을 때만 전송
      account_number: accountNumber || null,     // 값이 있을 때만 전송
      // color: color, 
    });

    resetForm();
    setIsAddDialogOpen(false);
  };

  // [UPDATE] 수정 모드 진입
  const handleEdit = (account: Account) => {
    setEditingAccount(account);
    
    // 기존 데이터 폼에 채우기
    setName(account.nickname); 
    // type 설정 제거
    setBalance(String(account.balance));
    // setColor(account.color); 

    if (account.bank_id) setBankId(String(account.bank_id));
    else setBankId(''); // 없을 경우 초기화

    if (account.account_number) setAccountNumber(account.account_number);
    else setAccountNumber(''); // 없을 경우 초기화
    
    setIsEditDialogOpen(true);
  };

  // [UPDATE] 계좌 수정 핸들러
  const handleUpdate = () => {
    if (!editingAccount) return;

    if (!name) {
        toast.error('계좌 별칭을 입력해주세요.');
        return;
    }

    onUpdate(editingAccount.account_id, {
      nickname: name,
      balance: parseFloat(balance) || 0,
      bank_id: bankId ? Number(bankId) : null,
      account_number: accountNumber || null,
    });

    resetForm();
    setEditingAccount(null);
    setIsEditDialogOpen(false);
  };

  // [DELETE] 계좌 삭제 핸들러
  const handleDelete = (id: number) => {
    if (confirm('정말로 이 계좌를 삭제하시겠습니까? \n삭제된 데이터는 복구할 수 없습니다.')) {
        onDelete(id);
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

  // 공통 폼 컴포넌트
  const AccountForm = () => (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="account-name">계좌 별칭</Label>
        <Input
          id="account-name"
          placeholder="예: 월급통장, 비상금"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>

      {/* 유형 선택(Select) 제거됨 */}

      {/* 은행 및 계좌번호는 항상 노출 (선택 사항으로 간주) */}
      <div className="grid grid-cols-2 gap-4">
             <div className="space-y-2">
                <Label>은행 (선택)</Label>
                <Select value={bankId} onValueChange={setBankId}>
                    <SelectTrigger>
                        <SelectValue placeholder="은행 선택" />
                    </SelectTrigger>
                    <SelectContent>
                        {banks.map((bank) => (
                            <SelectItem key={bank.bank_id} value={String(bank.bank_id)}>
                                {bank.name}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
             </div>
             <div className="space-y-2">
                <Label>계좌번호 (선택)</Label>
                <Input 
                    placeholder="- 제외 입력"
                    value={accountNumber}
                    onChange={(e) => setAccountNumber(e.target.value)}
                />
             </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="account-balance">잔액</Label>
        <Input
          id="account-balance"
          type="number"
          placeholder="0"
          value={balance}
          onChange={(e) => setBalance(e.target.value)}
        />
      </div>

      {/* 색상 선택 */}
      <div className="space-y-2">
        <Label>색상 (앱 표시용)</Label>
        <div className="flex gap-2">
          {defaultColors.map((c) => (
            <button
              key={c}
              type="button"
              className={`size-8 rounded-full border-2 ${
                color === c ? 'border-foreground' : 'border-transparent'
              }`}
              style={{ backgroundColor: c }}
              onClick={() => setColor(c)}
            />
          ))}
        </div>
      </div>
    </div>
  );

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between flex-wrap gap-2">
            <CardTitle className="flex items-center gap-2">
              <Wallet className="size-5" />
              계좌 관리
            </CardTitle>
            <div className="flex gap-2">
              {onTransferClick && accounts.length >= 2 && (
                <Button onClick={onTransferClick} size="sm" variant="outline">
                  <ArrowRightLeft className="size-4 mr-2" />
                  계좌 이체
                </Button>
              )}
              <Button onClick={() => setIsAddDialogOpen(true)} size="sm">
                <Plus className="size-4 mr-2" />
                계좌 추가
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Total Balance */}
          <div className="p-4 bg-primary/10 rounded-lg">
            <div className="text-sm text-muted-foreground mb-1">총 자산</div>
            <div className="text-primary text-xl font-bold">{formatCurrency(totalBalance)}원</div>
          </div>

          {/* Account List */}
          <div className="space-y-2">
            {accounts.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">등록된 계좌가 없습니다</div>
            ) : (
              accounts.map((account) => {
                // 아이콘은 기본 Wallet 아이콘으로 통일
                const displayName = account.nickname; 
                
                return (
                  <div
                    key={account.account_id}
                    className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className="p-2 rounded-full"
                        style={{ backgroundColor: `${color}20` }} 
                      >
                        <Wallet className="size-4" />
                      </div>
                      <div>
                        <div className="font-medium">{displayName}</div>
                        <div className="text-xs text-muted-foreground flex gap-2">
                          {/* account_type 라벨 제거 */}
                          {account.nickname ? (
                            <span>{account.nickname}</span>
                          ) : (
                            <span>기본 계좌</span>
                          )}
                          {account.account_number && <span>| {account.account_number}</span>}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-semibold">{formatCurrency(account.balance)}원</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleEdit(account)}
                      >
                        <Pencil className="size-4 text-gray-500" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDelete(account.account_id)}
                      >
                        <Trash className="size-4 text-red-500" />
                      </Button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>

      {/* Add Dialog */}
      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>계좌 추가</DialogTitle>
            <DialogDescription>새로운 계좌를 등록합니다.</DialogDescription>
          </DialogHeader>
          <AccountForm />
          <div className="flex gap-2 pt-4">
            <Button variant="outline" className="flex-1" onClick={() => setIsAddDialogOpen(false)}>
              취소
            </Button>
            <Button className="flex-1" onClick={handleAdd}>
              추가
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>계좌 수정</DialogTitle>
            <DialogDescription>계좌 정보를 수정합니다.</DialogDescription>
          </DialogHeader>
          <AccountForm />
          <div className="flex gap-2 pt-4">
            <Button variant="outline" className="flex-1" onClick={() => setIsEditDialogOpen(false)}>
              취소
            </Button>
            <Button className="flex-1" onClick={handleUpdate}>
              수정
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}