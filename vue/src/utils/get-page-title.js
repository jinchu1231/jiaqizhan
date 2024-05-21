import defaultSettings from '@/settings'
import i18n from '@/lang'
import { getLanguage } from '@/lang/index'
import store from '@/store'
export default function getPageTitle(key, fullName) {
  const title = store.getters.sysConfig.title || "JNPF快速开发平台"
  const realTitle = getLanguage() === 'en' ? title : getLanguage() === 'zhtw' ? title : title
  const hasKey = i18n.te(`route.${key}`)
  if (hasKey) {
    const pageName = i18n.t(`route.${key}`)
    return `${pageName} - ${realTitle}`
  }
  return fullName ? `${fullName} - ${realTitle}` : realTitle
}