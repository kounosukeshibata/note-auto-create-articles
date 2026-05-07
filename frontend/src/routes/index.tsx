import { createRoute, redirect } from '@tanstack/react-router';
import { Route as rootRoute } from './__root';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  beforeLoad: ({ context }) => {
    if (context.isAuthenticated) {
      throw redirect({ to: '/generate' });
    } else {
      throw redirect({ to: '/login' });
    }
  },
  component: () => null,
});
