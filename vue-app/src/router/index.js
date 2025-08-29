import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/store/user';
import Cookies from 'js-cookie';

const modules = import.meta.glob('/src/views/**/*.vue');

const routes = [
  {
    path: '/',
    name: 'Home',
    component: modules['/src/views/user/LoginPage.vue'], // 로그인하지 않은 경우 기본 페이지
    meta: { layout: 'SimpleLayout' },
  },
  {
    path: '/login',
    name: 'LoginPage',
    component: modules['/src/views/user/LoginPage.vue'],
    meta: { layout: 'SimpleLayout' },
  },
  {
    path: '/signup',
    name: 'SignupPage',
    component: modules['/src/views/user/SignupPage.vue'],
    meta: { layout: 'SimpleLayout' },
  },
  {
    path: '/oauth2/redirect/:provider',
    name: 'OAuthRedirectPage',
    component: modules['/src/views/user/OAuthRedirectPage.vue'],
    props: true,
    meta: { layout: 'SimpleLayout' },
  },
  {
    path: '/users',
    name: 'UsersPage',
    component: () => import('@/views/Users.vue'),
    meta: { requiresAuth: true, layout: 'DefaultLayout' },
  },
  // 404 경로는 동적으로 마지막에 추가됩니다.
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

let hasGeneratedRoutes = false;

export async function setupDynamicRoutes() {
  if (hasGeneratedRoutes) return;

  const userStore = useUserStore();
  const isAuthenticated = Cookies.get('accessToken');

  if (isAuthenticated) {
    try {
      const rawMenus = await userStore.fetchMenus();
      rawMenus.forEach(menu => {
        const componentPath = `/src/${menu.component}`;
        if (modules[componentPath]) {
          const route = {
            path: menu.path,
            name: menu.name,
            component: modules[componentPath],
            meta: { layout: 'FullLayout', requiresAuth: true },
          };
          router.addRoute(route);
        } else {
          console.warn(`[Menu Router] Component for menu '${menu.name}' not found at path: ${componentPath}`);
        }
      });
      hasGeneratedRoutes = true;
    } catch (error) {
      console.error('Failed to create dynamic routes:', error);
      userStore.logout();
    }
  }

  // 404 라우트 추가 (인증 상태에 따라 조건부)
  if (!isAuthenticated || (isAuthenticated && hasGeneratedRoutes)) {
    // 기존 404 라우트가 있으면 제거
    if (router.hasRoute('NotFound')) {
      router.removeRoute('NotFound');
    }
    
    router.addRoute({
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: modules['/src/views/NotFound.vue'],
      meta: { layout: 'SimpleLayout' },
    });
  }
}

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  // Ensure we're checking the cookie in a consistent way
  const isAuthenticated = !!Cookies.get('accessToken');
  
  // Define public routes that do not require authentication
  const publicRoutes = ['/', '/login', '/signup', '/oauth2/redirect'];
  const isPublicRoute = publicRoutes.some(route => 
    to.path === route || to.path.startsWith(route + '/')
  );

  // If authenticated and trying to access a public route, redirect to dashboard
  if (isAuthenticated && isPublicRoute) {
    return next('/dashboard');
  }

  // If not authenticated and trying to access a protected route
  if (!isAuthenticated && !isPublicRoute) {
    // Clear any existing user data to ensure clean state
    userStore.logout();
    
    // Redirect to login with the intended URL
    return next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
  }

  // Handle dynamic routes for authenticated users
  if (isAuthenticated && !hasGeneratedRoutes) {
    try {
      await setupDynamicRoutes();
      // After setting up routes, retry the navigation
      return next({ ...to, replace: true });
    } catch (error) {
      console.error('Failed to setup routes:', error);
      userStore.logout();
      return next('/login');
    }
  }

  // Continue with the navigation
  next();
});

export default router;