<template>
  <li>
    <router-link :to="menu.path" class="menu-item" @click.prevent="menu.children.length ? toggle() : navigate()">
      <span class="material-icons">{{ menu.icon || 'radio_button_unchecked' }}</span>
      <span class="menu-name">{{ menu.name }}</span>
      <span v-if="menu.children.length" class="material-icons chevron" :class="{ open: isOpen }">expand_more</span>
    </router-link>
    <ul v-if="isOpen && menu.children.length" class="submenu">
      <menu-item
        v-for="child in menu.children"
        :key="child.id"
        :menu="child"
      />
    </ul>
  </li>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  menu: {
    type: Object,
    required: true,
  },
});

// MenuItem을 재귀적으로 사용하기 위해 자기 자신을 import 합니다.
import MenuItem from './MenuItem.vue';

const router = useRouter();
const isOpen = ref(false);

const toggle = () => {
  isOpen.value = !isOpen.value;
};

const navigate = () => {
  router.push(props.menu.path);
};

// 초기 렌더링 시 현재 경로에 해당하는 메뉴를 열어둡니다.
if (router.currentRoute.value.path.startsWith(props.menu.path)) {
  isOpen.value = true;
}
</script>

<style scoped>
li {
  list-style: none;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin: 4px 0;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: background-color 0.2s ease, color 0.2s ease;
  cursor: pointer;
}

.menu-item:hover {
  background-color: #e9e9e9;
}

.menu-item.router-link-exact-active {
  background-color: #42b983;
  color: white;
  font-weight: bold;
}

.menu-item.router-link-exact-active .material-icons {
  color: white;
}

.material-icons {
  margin-right: 16px;
  color: #555;
}

.menu-name {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0; /* Flexbox item이 줄어들 수 있도록 보장 */
}

.chevron {
  margin-right: 0;
  margin-left: auto;
  transition: transform 0.3s ease;
}

.chevron.open {
  transform: rotate(180deg);
}

.submenu {
  list-style: none;
  padding-left: 24px; /* 들여쓰기 */
  margin-top: 4px;
  overflow: hidden;
}
</style>