import { createRouter } from '@tanstack/react-router';
import { routeTree } from './routeTree.gen';

interface RouterContext {
  isAuthenticated: boolean;
}

export const router = createRouter({
  routeTree,
  context: {
    isAuthenticated: false,
  } satisfies RouterContext,
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
