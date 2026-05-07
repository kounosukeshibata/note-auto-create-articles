import { createRootRouteWithContext, Outlet } from '@tanstack/react-router';
import { RootLayout } from '../layouts/RootLayout';

interface RouterContext {
  isAuthenticated: boolean;
}

export const Route = createRootRouteWithContext<RouterContext>()({
  component: () => (
    <RootLayout>
      <Outlet />
    </RootLayout>
  ),
});
