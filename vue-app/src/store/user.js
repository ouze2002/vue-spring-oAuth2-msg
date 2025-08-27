import { defineStore } from 'pinia';
import { jwtDecode } from 'jwt-decode';
import Cookies from 'js-cookie';
import axiosInstance from '../api/axiosInstance';

function buildMenuTree(menus, parentId = null) {
  return menus
    .filter(menu => menu.parentId === parentId)
    .sort((a, b) => a.menuOrder - b.menuOrder)
    .map(menu => ({
      ...menu,
      children: buildMenuTree(menus, menu.id),
    }));
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false,
    username: null,
    isAuthLoaded: false, // 새로운 상태 추가
    rawMenus: [], // API 원본 데이터
    menus: [], // 계층 구조로 가공된 메뉴
  }),
  actions: {
    setUser(user) {
      this.user = user;
      this.isLoggedIn = !!user;
      this.isAuthLoaded = true; // 사용자 설정 시 로드 완료
    },
    logout() {
      this.user = null;
      this.isLoggedIn = false;
      this.username = null;
      this.rawMenus = [];
      this.menus = [];
      Cookies.remove('accessToken');
      this.isAuthLoaded = true; // 로그아웃 시에도 로드 완료
    },
    setMenus(rawMenus) {
      this.rawMenus = rawMenus;
      this.menus = buildMenuTree(rawMenus);
    },
    async fetchMenus() {
      try {
        const response = await axiosInstance.get('/menus');
        this.setMenus(response.data);
        return response.data; // 라우터 설정을 위해 반환
      } catch (error) {
        console.error('메뉴 정보를 가져오는데 실패했습니다.', error);
        this.setMenus([]); // 실패 시 메뉴 초기화
        return []; // 빈 배열 반환
      }
    },
    setUserFromToken(token) {
      try {
        const decoded = jwtDecode(token);
        this.user = {
          id: decoded.sub,
          role: decoded.role
        };
        this.isLoggedIn = true;
      } catch {
        this.user = null;
        this.isLoggedIn = false;
        this.username = null;
      }
      // fetchUserProfile이 호출될 것이므로 여기서 isAuthLoaded를 true로 설정하지 않습니다.
      // fetchUserProfile에서 최종적으로 설정합니다.
    },
    async fetchUserProfile() {
      try {
        const response = await axiosInstance.get('/auth/me');
        this.username = response.data.username;
        this.isAuthLoaded = true;
      } catch (error) {
        console.error('사용자 프로필 가져오기 실패:', error);
        this.username = null;
        this.isAuthLoaded = true;
        // 401 오류 발생 시 로그아웃 처리
        if (error.response && error.response.status === 401) {
          this.logout();
        }
      }
    },
  },
});