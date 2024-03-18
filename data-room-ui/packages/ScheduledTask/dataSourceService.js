/*!
 * 任务管理
 */
import Vue from 'vue'
/**
 * 添加任务
 * @param params
 * @param flag
 * @returns {*}
 */
const add = (params = {}, flag = false) => Vue.prototype.$dataRoomAxios.post('/api/tasks/add', params, flag)

/**
 * 删除任务
 * @param params
 * @param flag
 * @returns {*}
 */
const sourceRemove = (id = '-1', flag = false) => Vue.prototype.$dataRoomAxios.get(`/api/tasks/delete/${id}`, {}, flag)

/**
 * 修改任务
 * @param params
 * @param flag
 * @returns {*}
 */
const update = (id = '-1', params = {}, flag = false) => Vue.prototype.$dataRoomAxios.post(`/api/tasks/update/${id}`, params, flag)

/**
 * 获取任务列表
 * @param params
 * @param flag
 * @returns {*}
 */
const datasourcePage = (params = {}, flag = false) => Vue.prototype.$dataRoomAxios.get('/api/tasks/list', params, flag)

export {
  add,
  update,
  datasourcePage,
  sourceRemove
}
