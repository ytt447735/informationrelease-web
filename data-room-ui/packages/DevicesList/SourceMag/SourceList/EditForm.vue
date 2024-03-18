<template>
  <div>
    <el-dialog
      :close-on-click-modal="false"
      :title="'编辑'"
      :visible.sync="formVisible"
      :append-to-body="true"
      class="bs-dialog-wrap bs-el-dialog"
      @close="closeAddDialog"
    >
      <el-form
        ref="dataForm"
        :model="dataForm"
        :rules="dataFormRules"
        label-position="right"
        label-width="100px"
        class="bs-el-form"
      >
        <el-form-item
          label="名称"
          prop="name"
        >
          <el-input
            v-model="dataForm.name"
            autocomplete="off"
            placeholder="请输入名称"
            clearable
            maxlength="30"
            class="bs-el-input"
          />
        </el-form-item>
        <el-form-item
          label="分组"
        >
          <el-select
            v-model="dataForm.groupingName"
            class="bs-el-select"
            popper-class="bs-el-select"
            placeholder="请选择分组"
            clearable
          >
            <el-option
              v-for="item in resolutionRatioOptions"
              :key="item.id"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          class="bs-el-button-default"
          @click="closeAddDialog"
        >
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="sureLoading"
          :disabled="sureDisabled"
          @click="addOrUpdate()"
        >
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Icon from 'data-room-ui/assets/images/dataSourceIcon/export'
export default {
  name: 'EditForm',
  components: {
  },
  props: {
  },
  data () {
    return {
      resolutionRatioOptions: [],
      formVisible: false,
      title: '', // 弹框标题(新增/编辑)
      dataForm: {},
      dataFormRules: {
        name: [
          { required: true, message: '名称不能为空', trigger: 'blur' }
        ]
      },
      sureLoading: false,
      toDesignLoading: false,
      sureDisabled: false,
      toDesignDisabled: false
    }
  },
  computed: {},
  watch: {
  },
  mounted () {},
  methods: {
    // 关闭弹窗时
    closeAddDialog () {
      this.formVisible = false
      this.$refs.dataForm.resetFields()
    },
    // 初始化
    init (nodeData) {
      console.log(nodeData)
      this.dataForm = nodeData
      this.title = ''
      this.formVisible = true
      this.getCatalogList()
      this.$nextTick(() => {
      })
    },
    // 点击确定时
    addOrUpdate () {
      // console.log(this.dataForm.id)
      // console.log(this.dataForm.name)
      // console.log(this.dataForm.groupingName)
      this.closeAddDialog()
      this.$dataRoomAxios.post('/api/client/update', {
        id: this.dataForm.id,
        name: this.dataForm.name,
        group: this.dataForm.groupingName
      }, true).then((r) => {
        if (r.code === 200) {
          this.$message({
            type: 'success',
            message: r.msg
          })
        } else {
          this.$message({
            type: 'error',
            message: r.msg || '修改失败!'
          })
        }
      }).catch(() => {
        this.$message({
          type: 'error',
          message: '修改失败!'
        })
      })
    },
    // 获取目录的列表
    getCatalogList () {
      this.$dataRoomAxios.get('/api/client/group/list')
        .then((data) => {
          this.resolutionRatioOptions = data
        })
        .catch(() => { })
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../../../assets/style/bsTheme.scss';
.el-scrollbar {
  height: 300px;
  overflow-x: hidden;

  ::v-deep .el-scrollbar__view {
    overflow-x: hidden;
  }
}

.color-picker {
  display: flex;
  align-items: center;
}

.icon-svg {
  width: 25px;
  height: 25px;
}

.color-select {
  width: 350px;
  display: flex;
  margin-top: 5px;
  align-items: center;
  justify-content: space-between;

  div {
    width: 20px;
    height: 20px;
    cursor: pointer;
    position: relative;
  }

  ::v-deep .el-color-picker__trigger {
    top: 0;
    right: 0;
    width: 21px;
    height: 21px;
    padding: 0;
  }

  .el-icon-check {
    font-size: 20px;
    color: #ffffff;
    position: absolute;
  }
}

.icon-list {
  margin-top: 15px;
  padding: 5px;
  border-radius: 5px;
  border: 1px solid #e8e8e8;

  .el-button--mini {
    min-width: 37px;
    padding: 5px !important;
    border: 1px solid transparent !important;

    &:hover {
      background-color: rgba(217, 217, 217, 0.3);
    }
  }
}

.icon-uploader {
  width: 60px;
  height: 60px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;

}

.icon-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 60px;
  height: 60px;
  line-height: 60px;
  text-align: center;
}

.icon {
  width: 60px;
  height: 60px;
  display: block;
}

.default-layout-box {
  display: flex;
  flex-wrap: wrap;

  .default-layout-item {
    cursor: pointer;
    margin: 9px;
    display: flex;
    flex-direction: column;
    align-items: center;

    .component-name {
      font-size: 12px;
    }

    .sampleImg {
      margin: 0 auto;
      width: 102px;
      height: 73px;
      display: block;
    }

    .img_dispaly {
      margin: 0 auto;
      text-align: center;
      width: 100px;
      height: 70px;
      line-height: 70px;
      background-color: #D7D7D7;
      color: #999;
    }

    .demonstration {
      text-align: center;
    }
  }

  .default-layout-item:hover {
    cursor: pointer;
  }

  ::v-deep .el-radio__label {
    display: none;
  }
}

/*滚动条样式*/
::v-deep ::-webkit-scrollbar {
  width: 6px;
  border-radius: 4px;
  height: 4px;
}

::v-deep ::-webkit-scrollbar-thumb {
  background: #dddddd !important;
  border-radius: 10px;
}

.catalog-cascader {
  width: 100% !important;
}
</style>
