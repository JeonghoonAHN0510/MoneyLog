import { Link } from 'react-router-dom';
import { Wallet } from 'lucide-react';
import { Button } from './ui/button';
import '../styles/components/StandalonePageHeader.css';

export default function StandalonePageHeader() {
  return (
    <div className="standalone-page-header">
      <div className="standalone-page-header-brand">
        <Wallet className="standalone-page-header-icon" />
        <span className="standalone-page-header-text">MoneyLog</span>
      </div>

      <Button asChild variant="outline" size="sm" className="standalone-page-header-home">
        <Link to="/">홈으로</Link>
      </Button>
    </div>
  );
}
