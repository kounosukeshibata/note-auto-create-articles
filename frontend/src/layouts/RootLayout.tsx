import React from 'react';
import { useNavigate } from '@tanstack/react-router';
import { useAuth } from '../contexts/AuthContext';

interface RootLayoutProps {
  children: React.ReactNode;
}

export function RootLayout({ children }: RootLayoutProps) {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    void navigate({ to: '/login' });
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <span className="app-header-title">NOTE AUTO POST</span>
        {isAuthenticated && user && (
          <div className="app-header-user">
            <span className="app-header-username">{user.name}</span>
            <button type="button" className="btn btn-logout" onClick={handleLogout}>
              ログアウト
            </button>
          </div>
        )}
      </header>

      <main className="app-main">
        {children}
      </main>
    </div>
  );
}
