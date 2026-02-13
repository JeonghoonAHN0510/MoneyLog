import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Switch } from './ui/switch';
import { Calculator } from 'lucide-react';
import { formatKrw } from '../utils/currency';

export function TakeHomeCalculator() {
  const [annualSalary, setAnnualSalary] = useState(''); // 연봉 상태로 변경
  const [dependents, setDependents] = useState('1');
  const [childrenUnder20, setChildrenUnder20] = useState('0');
  const [isSMEYouth, setIsSMEYouth] = useState(false);

  const calculateTakeHome = () => {
    const annual = parseFloat(annualSalary) || 0;
    const gross = annual / 12; // 월 환산 급여 (비과세액 등 제외한 단순 계산)
    const deps = parseInt(dependents) || 1;
    const children = parseInt(childrenUnder20) || 0;

    // 국민연금 (4.5%, 상한액 적용)
    // 2024년 기준 상한액은 월 265,500원 정도이나 기존 코드 로직(243,000) 유지하되 감면 로직 제거
    const nationalPension = Math.min(Math.floor(gross * 0.045), 243000); 

    // 건강보험 (3.545%) - 감면 로직 제거
    const healthInsurance = Math.floor(gross * 0.03545);

    // 장기요양보험 (건강보험의 12.95%)
    const longTermCare = Math.floor(healthInsurance * 0.1295);

    // 고용보험 (0.9%) - 감면 로직 제거
    const employmentInsurance = Math.floor(gross * 0.009);

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
      
      // 중소기업 청년 소득세 감면 (90%) - 이 부분만 유지
      if (isSMEYouth) {
        incomeTax = incomeTax * 0.1; 
      }
    }
    
    // 원단위 절사
    incomeTax = Math.floor(incomeTax / 10) * 10;

    // 지방소득세 (소득세의 10%)
    const localTax = Math.floor(incomeTax * 0.1 / 10) * 10;

    const totalDeduction =
      nationalPension + healthInsurance + longTermCare + employmentInsurance + incomeTax + localTax;
    const takeHome = gross - totalDeduction;

    return {
      annual,
      gross, // 월 환산액
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

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Calculator className="size-5" />
          {/* todo User 테이블이나 따로 테이블 만들어서 로그인한 회원의 연봉을 입력받고, 해당 연봉으로 값을 바로 넣어주기 */}
          실수령액 계산기 (연봉 기준)
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="annualSalary">연봉 (세전)</Label>
            <div className="relative">
                <Input
                id="annualSalary"
                type="number"
                placeholder="40000000"
                value={annualSalary}
                onChange={(e) => setAnnualSalary(e.target.value)}
                />
                {result.annual > 0 && (
                    <p className="text-xs text-muted-foreground mt-1 absolute right-1 -bottom-5">
                        월 환산: 약 {formatKrw(result.gross, { round: true })}원
                    </p>
                )}
            </div>
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
              <Label htmlFor="isSMEYouth" className="cursor-pointer">
                  중소기업 청년 세제혜택
                  <span className="block text-xs text-muted-foreground font-normal mt-0.5">
                      (소득세 90% 감면 적용)
                  </span>
              </Label>
              <Switch
                id="isSMEYouth"
                checked={isSMEYouth}
                onCheckedChange={setIsSMEYouth}
              />
            </div>
          </div>
        </div>

        {/* 한 달 기준 공제액 박스 */}
        <div className="p-4 bg-muted rounded-lg space-y-3 mt-6">
          <div className="text-sm text-muted-foreground font-medium">한 달 기준 예상 급여</div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">국민연금 (4.5%)</div>
              <div className="text-sm">{formatKrw(result.nationalPension, { round: true })}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">건강보험 (3.545%)</div>
              <div className="text-sm">{formatKrw(result.healthInsurance, { round: true })}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">장기요양 (12.95%)</div>
              <div className="text-sm">{formatKrw(result.longTermCare, { round: true })}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">고용보험 (0.9%)</div>
              <div className="text-sm">{formatKrw(result.employmentInsurance, { round: true })}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">
                  소득세 {isSMEYouth && <span className="text-green-600 font-bold">(90% 감면)</span>}
              </div>
              <div className="text-sm">{formatKrw(result.incomeTax, { round: true })}원</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">지방소득세</div>
              <div className="text-sm">{formatKrw(result.localTax, { round: true })}원</div>
            </div>
          </div>
          <div className="pt-2 border-t">
            <div className="flex justify-between">
              <span className="text-sm">공제액 합계</span>
              <span className="text-sm text-red-600">{formatKrw(result.totalDeduction, { round: true })}원</span>
            </div>
          </div>
        </div>

        {result.gross > 0 && (
          <div className="space-y-4 pt-4 border-t">
            <div className="pt-2 space-y-2">
              <div className="flex justify-between items-center">
                <span className="font-medium text-muted-foreground">월 예상 공제액</span>
                <span className="text-red-600 font-medium">-{formatKrw(result.totalDeduction, { round: true })}원</span>
              </div>
              <div className="flex justify-between items-center text-lg">
                <span className="font-bold">월 예상 실수령액</span>
                <span className="text-green-600 font-bold text-xl">{formatKrw(result.takeHome, { round: true })}원</span>
              </div>
              <div className="text-right text-xs text-muted-foreground">
                  (연간 예상 실수령액: {formatKrw(result.takeHome * 12, { round: true })}원)
              </div>
            </div>

            <div className="bg-muted/50 p-3 rounded-lg text-xs text-muted-foreground">
              * 연봉을 12개월로 나눈 금액을 기준으로 계산하며, 실제 급여명세서와 차이가 있을 수 있습니다.<br/>
              * 비과세 식대 등은 고려되지 않았습니다.<br/>
              {isSMEYouth && (
                <>
                  * 중소기업 취업자 소득세 감면(90%)이 적용되었습니다. (4대보험료는 감면 대상이 아닙니다)
                </>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
