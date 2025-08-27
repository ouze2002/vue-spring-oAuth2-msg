import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import ko from './locales/ko.json';
import ja from './locales/ja.json';

const getInitialLocale = () => {
  const savedLocale = localStorage.getItem('locale');
  if (savedLocale) {
    return savedLocale;
  }
  const browserLocale = navigator.language || navigator.userLanguage;
  const language = browserLocale.split('-')[0];
  return ['en', 'ko', 'ja'].includes(language) ? language : 'en';
};

const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: 'en',
  messages: {
    en,
    ko,
    ja,
  },
});

export default i18n;
