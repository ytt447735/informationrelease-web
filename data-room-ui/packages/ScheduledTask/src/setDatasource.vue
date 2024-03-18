<template>
  <el-dialog
    width="700px"
    :title="title"
    :visible.sync="setDatasourceVisible"
    :append-to-body="true"
    :close-on-click-modal="false"
    :before-close="handleClose"
    class="bs-dialog-wrap bs-el-dialog"
  >
    <div
      v-loading="linkLoading"
      element-loading-text="正在测试连接..."
      style="padding-right: 80px;"
    >
      <el-form
        ref="dataForm"
        class="bs-el-form"
        :model="dataForm"
        :rules="dataForm.id ? updateRules : rules"
        size="small"
        label-position="right"
        :label-width="dataForm.advanceSettingFlag ? '200px' : '150px'"
      >
        <el-form-item
          label="类型"
          prop="sourceType"
        >
          <el-select
            v-model="dataForm.type"
            placeholder="请选择类型"
            class="bs-el-select"
            popper-class="bs-el-select"
            clearable
            filterable
          >
            <el-option
              v-for="sourceType in sourceTypeList"
              :key="sourceType.code"
              :label="sourceType.label"
              :value="sourceType.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="设备组"
          prop="sourceDevice"
        >
          <el-select
            v-model="dataForm.device"
            placeholder="请选择设备组"
            class="bs-el-select"
            popper-class="bs-el-select"
            clearable
            filterable
            @change="sourceDeviceChange"
          >
            <el-option
              v-for="sourceDevice in sourceDeviceList"
              :key="sourceDevice.code"
              :label="sourceDevice.name"
              :value="sourceDevice.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="任务名称"
          prop="sourceName"
          :rules="nameValidationRules"
        >
          <el-input
            v-model="dataForm.name"
            placeholder="请输入任务名称"
            class="bs-el-input"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          label="Cron"
          prop="sourceCron"
        >
          <el-input
            v-model="dataForm.cron"
            placeholder="请输入Cron表达式"
            class="bs-el-input"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          v-if="dataForm.sourceType === '1'"
          label="节目"
          prop="sourceProgram"
        >
          <el-select
            v-model="dataForm.data"
            placeholder="请选择节目"
            class="bs-el-select"
            popper-class="bs-el-select"
            clearable
            filterable
            @change="sourceDeviceChange"
          >
            <el-option
              v-for="sourceProgram in sourceProgramList"
              :key="sourceProgram.code"
              :label="sourceProgram.label"
              :value="sourceProgram.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <span
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        class="bs-el-button-default"
        @click="handleClose"
      >
        取消
      </el-button>
      <el-button
        type="primary"
        @click="submitForm"
      >
        确定
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { sourceLinkTest, add, update } from 'data-room-ui/ScheduledTask/dataSourceService'
export default {
  props: {
    appCode: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      setDatasourceVisible: false,
      title: '',
      linkLoading: false,
      sourceDeviceList: [], // 设备列表
      sourceProgramList: [], // 节目列表
      dataForm: {
        id: '',
        name: '',
        type: '',
        cron: '',
        data: '',
        device: ''
      },
      // rules: {
      //   sourceType: [
      //     { required: true, message: '请选择类型', trigger: 'blur' }
      //   ],
      //   sourceDevice: [
      //     { required: true, message: '请选择设备组', trigger: 'blur' }
      //   ],
      //   sourceCron: [
      //     { required: true, message: '请输入Cron表达式', trigger: 'blur' },
      //     { pattern: /^((\*|\?|\d+|\w+|\d+(?:-\d+)?)(?:\/\d+)?(?:,(?:(\*|\?|\d+|\w+|\d+(?:-\d+)?)(?:\/\d+)?)+)?\s){5}(\*|\?|\d+|\w+|\d+(?:-\d+)?)(?:\/\d+)?(?:,(?:(\*|\?|\d+|\w+|\d+(?:-\d+)?)(?:\/\d+)?)+)?$/, message: 'Cron表达式格式不正确', trigger: 'blur' }
      //   ],
      //   sourceName: [
      //     { required: true, message: '请输入任务名称', trigger: 'blur' },
      //     { max: 200, message: '任务名称最多输入200个字符', trigger: 'blur' }
      //   ]
      // }

    }
  },
  computed: {
    sourceTypeList () {
      return window.BS_CONFIG?.sourceTypeList || [
        { label: '发送', code: '1' },
        { label: '黑屏', code: '2' },
        { label: '亮屏', code: '3' }
      ]
    }
  },
  methods: {
    // 初始化
    init (row) {
      console.log('init', row)
      this.getCatalogList()
      // 清除表单验证
      if (this.$refs.dataForm) {
        this.$refs.dataForm.clearValidate()
      }
      if (row && row.id) {
        this.dataForm = row
      }
    },
    sourceLinkTest (row) {
      this.linkLoading = true
      sourceLinkTest(row).then((r) => {
        this.$message.success(r)
        this.linkLoading = false
      }).catch(() => {
        this.linkLoading = false
      })
    },
    // 取消重制
    handleClose () {
      this.$refs.dataForm.resetFields()
      this.dataForm = {
        id: ''
      }
      this.setDatasourceVisible = false
    },
    // 获取设备列表
    getCatalogList () {
      this.sourceDeviceList = [{ code: -1, name: '全部' }]
      this.$dataRoomAxios.get('/api/client/group/list')
        .then((data) => {
          this.sourceDeviceList = this.sourceDeviceList.concat(data)
        })
        .catch(() => { })
    },
    // 保存
    submitForm () {
      // mysql 需要包含useOldAliasMetadataBehavior
      // if (this.dataForm.sourceType == 'Mysql') {
      //   if (this.dataForm.url.indexOf('useOldAliasMetadataBehavior') == -1) {
      //     if (this.dataForm.url.indexOf('?') == -1) {
      //       this.dataForm.url = this.dataForm.url + '?useOldAliasMetadataBehavior=true'
      //     } else {
      //       this.dataForm.url = this.dataForm.url + '&useOldAliasMetadataBehavior=true'
      //     }
      //   }
      // }
      console.log('this.dataForm', this.dataForm)
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          if (this.dataForm.id) {
            update(this.dataForm.id, {
              ...this.dataForm,
              moduleCode: this.appCode,
              editable: this.appCode ? 1 : 0
            }).then(() => {
              this.$message.success('保存成功')
              // 刷新表格
              this.$emit('refreshTable')
              this.handleClose()
            })
          } else {
            add({
              ...this.dataForm.id,
              ...this.dataForm,
              moduleCode: this.appCode,
              editable: this.appCode ? 1 : 0
            }).then(() => {
              this.$message.success('保存成功')
              // 刷新表格
              this.$emit('refreshTable')
              this.handleClose()
            })
          }
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets//style/bsTheme.scss';
</style>
