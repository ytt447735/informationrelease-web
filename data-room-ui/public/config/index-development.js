window.ENV = 'development'
var developmentConfig = {
  baseUrl: 'http://127.0.0.1:8080',
  fileUrlPrefix: 'http://127.0.0.1:8080/static'
}
// 必须的
window.CONFIG = configDeepMerge(window.CONFIG, developmentConfig)
