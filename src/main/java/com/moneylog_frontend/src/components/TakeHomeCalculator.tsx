import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Switch } from './ui/switch';
import { Calculator } from 'lucide-react';

export function TakeHomeCalculator() {
  const [grossSalary, setGrossSalary] = useState('');
  const [dependents, setDependents] = useState('1');
  const [childrenUnder20, setChildrenUnder20] = useState('0');
  const [isSMEYouth, setIsSMEYouth] = useState(false);

  const calculateTakeHome = () => {
    const gross = parseFloat(grossSalary) || 0;
    const deps = parseInt(dependents) || 1;
    const children = parseInt(childrenUnder20) || 0;

    // 국민연금 (4.5%)
    let nationalPension = Math.min(gross * 0.045, 243000);
    if (isSMEYouth) {
      nationalPension = 0; // 중소기업 청년 감면
    }

    // 건강보험 (3.545%)
    let healthInsurance = gross * 0.03545;
    if (isSMEYouth) {
      healthInsurance = 0; // 중소기업 청년 감면
    }

    // 장기요양보험 (건강보험의 12.95%)
    let longTermCare = healthInsurance * 0.1295;

    // 고용보험 (0.9%)
    let employmentInsurance = gross * 0.009;
    if (isSMEYouth) {
      employmentInsurance = 0; // 중소기업 청년 감면
    }

    // 소득세 간이세액표 기준 (매우 간략화된 계산)
    let incomeTax = 0;
    if (gross > 0) {
      const monthlyGross = gross;
      if (monthlyGross <= 1000000) {
        incomeTax = 0;
      } else if (monthlyGross <= 2000000) {
        incomeTax = (monthlyGross - 1000000) * 0.06;
      } else if (monthlyGross <= 3000000) {
        incomeTax = 60000 + (monthlyGross - 2000000) * 0.15;
      } else if (monthlyGross <= 5000000) {
        incomeTax = 210000 + (monthlyGross - 3000000) * 0.24;
      } else {
        incomeTax = 690000 + (monthlyGross - 5000000) * 0.35;
      }

      // 부양가족 공제
      incomeTax = Math.max(0, incomeTax - (deps - 1) * 12500);
      
      // 20세 이하 자녀 공제 (추가 공제)
      incomeTax = Math.max(0, incomeTax - children * 12500);
      
      // 중소기업 청년 소득세 감면 (70-90%)
      if (isSMEYouth) {
        incomeTax = incomeTax * 0.1; // 90% 감면 가정
      }
    }

    // 지방소득세 (소득세의 10%)
    const localTax = incomeTax * 0.1;

    const totalDeduction =
      nationalPension + healthInsurance + longTermCare + employmentInsurance + incomeTax + localTax;
    const takeHome = gross - totalDeduction;

    return {
      gross,
      nationalPension,
      healthInsurance,
      longTermCare,
      employmentInsurance,
      incomeTax,
      localTax,
      totalDeduction,
      takeHome,
    };
  };

  const result = calculateTakeHome();

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(Math.round(amount));
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Calculator className="size-5" />
          실수령액 계산기
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="grossSalary">세전 급여 (월)</Label>
            <Input
              id="grossSalary"
              type="number"
              placeholder="3000000"
              value={grossSalary}
              onChange={(e) => setGrossSalary(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="dependents">부양가족 수 (본인 포함)</Label>
            <Input
              id="dependents"
              type="number"
              placeholder="1"
              min="1"
              value={dependents}
              onChange={(e) => setDependents(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="childrenUnder20">20세 이하 자녀 수</Label>
            <Input
              id="childrenUnder20"
              type="number"
              placeholder="0"
              min="0"
              value={childrenUnder20}
              onChange={(e) => setChildrenUnder20(e.target.value)}
            />
          </div>
          <div className="space-y-2 flex items-end">
            <div className="flex items-center justify-between w-full pb-2">
              <Label htmlFor="isSMEYouth">중소기업 청년 세제혜택</Label>
              <Switch
                id="isSMEYouth"
                checked={isSMEYouth}
                onCheckedChange={setIsSMEYouth}
              />
            </div>
          </div>
        </div>

        {/* 한 달 기준 공제액 박스 */}
        <div className="p-4 bg-muted rounded-lg space-y-3">
          <div className="text-sm text-muted-foreground">한 달 기준 공제액</div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">국민연금</div>
              <div className="text-sm">{formatCurrency(result.nationalPension)}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">건강보험</div>
              <div className="text-sm">{formatCurrency(result.healthInsurance)}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">장기요양</div>
              <div className="text-sm">{formatCurrency(result.longTermCare)}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">고용보험</div>
              <div className="text-sm">{formatCurrency(result.employmentInsurance)}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">소득세</div>
              <div className="text-sm">{formatCurrency(result.incomeTax)}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">지방소득세</div>
              <div className="text-sm">{formatCurrency(result.localTax)}원</div>
            </div>
          </div>
          <div className="pt-2 border-t">
            <div className="flex justify-between">
              <span className="text-sm">공제액 합계</span>
              <span className="text-sm text-red-600">{formatCurrency(result.totalDeduction)}원</span>
            </div>
          </div>
        </div>

        {result.gross > 0 && (
          <div className="space-y-4 pt-4 border-t">
            <div>
              <div className="text-sm text-muted-foreground mb-2">공제 내역</div>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>국민연금 (4.5%){isSMEYouth && ' - 감면'}</span>
                  <span>{formatCurrency(result.nationalPension)}원</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>건강보험 (3.545%){isSMEYouth && ' - 감면'}</span>
                  <span>{formatCurrency(result.healthInsurance)}원</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>장기요양보험 (12.95%)</span>
                  <span>{formatCurrency(result.longTermCare)}원</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>고용보험 (0.9%){isSMEYouth && ' - 감면'}</span>
                  <span>{formatCurrency(result.employmentInsurance)}원</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>소득세{isSMEYouth && ' (90% 감면)'}</span>
                  <span>{formatCurrency(result.incomeTax)}원</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>지방소득세</span>
                  <span>{formatCurrency(result.localTax)}원</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t space-y-2">
              <div className="flex justify-between">
                <span>총 공제액</span>
                <span className="text-red-600">{formatCurrency(result.totalDeduction)}원</span>
              </div>
              <div className="flex justify-between">
                <span>실수령액</span>
                <span className="text-green-600">{formatCurrency(result.takeHome)}원</span>
              </div>
            </div>

            <div className="bg-muted p-3 rounded-lg text-xs text-muted-foreground">
              * 이 계산기는 간략화된 계산 방식을 사용합니다. 실제 급여는 회사의 급여 정책과 개인의
              상황에 따라 달라질 수 있습니다.
              {isSMEYouth && (
                <>
                  <br />* 중소기업 청년 세제혜택은 소득세 90% 감면, 4대보험 전액 감면을 가정하였습니다.
                </>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}