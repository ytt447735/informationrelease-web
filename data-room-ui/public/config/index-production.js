window.ENV = 'production'
var productionConfig = {
  baseUrl: 'http://192.168.5.55/backstage',
  fileUrlPrefix: 'http://192.168.5.55/backstage' + '/static'

}
// 必须的
window.CONFIG = configDeepMerge(window.CONFIG, productionConfig)
