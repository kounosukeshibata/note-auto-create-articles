import { Route as rootRoute } from './routes/__root';
import { Route as IndexRoute } from './routes/index';
import { Route as LoginRoute } from './routes/login';
import { Route as GenerateRoute } from './routes/generate';
import { Route as PreviewArticleIdRoute } from './routes/preview/$articleId';

export const routeTree = rootRoute.addChildren([
  IndexRoute,
  LoginRoute,
  GenerateRoute,
  PreviewArticleIdRoute,
]);
