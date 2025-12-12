/**
 * 高德地图加载工具
 * 统一管理地图API的加载，避免重复加载和插件缺失问题
 */

let mapLoadPromise = null

/**
 * 加载高德地图API
 * @returns {Promise<AMap>}
 */
export function loadAMap() {
  // 如果已经在加载中，返回同一个Promise
  if (mapLoadPromise) {
    return mapLoadPromise
  }

  // 如果已加载完成，直接返回
  if (window.AMap) {
    return Promise.resolve(window.AMap)
  }

  const amapKey = import.meta.env.VITE_AMAP_KEY || ''
  const amapSecurityCode = import.meta.env.VITE_AMAP_SECURITY_CODE || ''

  mapLoadPromise = new Promise((resolve, reject) => {
    if (!amapKey) {
      reject(new Error('高德地图API Key未配置'))
      return
    }

    // 设置安全密钥
    window._AMapSecurityConfig = {
      securityJsCode: amapSecurityCode
    }

    const script = document.createElement('script')
    // 加载所有可能用到的插件
    const plugins = [
      'AMap.Geocoder',        // 地理编码（地址解析）
      'AMap.PlaceSearch',     // 地点搜索
      'AMap.AutoComplete',    // 自动补全
      'AMap.Geolocation',     // 定位
      'AMap.CitySearch',      // 城市查询
      'AMap.Driving',         // 驾车路线
      'AMap.Walking',         // 步行路线
      'AMap.Riding'           // 骑行路线
    ].join(',')

    script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}&plugin=${plugins}`
    
    script.onload = () => {
      if (window.AMap) {
        console.log('高德地图API加载成功，已加载插件:', plugins)
        mapLoadPromise = null // 重置Promise，允许后续重新加载（如果需要）
        resolve(window.AMap)
      } else {
        mapLoadPromise = null
        reject(new Error('加载高德地图失败'))
      }
    }
    
    script.onerror = () => {
      mapLoadPromise = null
      reject(new Error('加载高德地图脚本失败'))
    }
    
    document.head.appendChild(script)
  })

  return mapLoadPromise
}
