<template>
  <el-dialog
    title="设备信息"
    :visible.sync="dialogVisible"
    width="80%"
    class="bs-dialog-wrap bs-el-dialog"
  >
    <div class="content">
      <el-descriptions title="  " style="background: var(--bs-background-2);">
        <el-descriptions-item label="软件版本">{{ data.AppVersion }}</el-descriptions-item>
        <el-descriptions-item label="ABI">{{ data.ABI }}</el-descriptions-item>
        <el-descriptions-item label="系统">{{ data.system }}</el-descriptions-item>
        <el-descriptions-item label="设备SN">{{ data.device_id }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ data.ip }}</el-descriptions-item>
        <el-descriptions-item label="加入时间">{{ data.create_time }}</el-descriptions-item>
        <el-descriptions-item label="离线时间">{{ datas.offline_time }}</el-descriptions-item>
        <el-descriptions-item label="在线时间">{{ datas.OnlineTime }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div
      slot="footer"
      class="dialog-footer"
    >
      <el-button
        class="bs-el-button-default"
        @click="dialogVisible = false"
      >
        取消
      </el-button>
      <el-button
        type="primary"
        @click="confirm"
      >
        确定
      </el-button>
    </div>
  </el-dialog>
</template>
<script>
import { pageMixins } from 'data-room-ui/js/mixins/page'

export default {
  name: 'OperateInfo',
  mixins: [pageMixins],
  props: {
    imgUrl: {
      type: String,
      default: ''
    }
  },
  model: {
    prop: 'imgUrl',
    event: 'change'
  },
  data () {
    return {
      dialogVisible: false,
      data: {},
      datas: {}
    }
  },
  computed: {
    gridComputed () {
      return this.list.length > 3
    }
  },
  mounted () {},
  methods: {
    close () { },
    init (screen) {
      this.datas = screen
      console.log(screen)
      const jsonObject = JSON.parse(screen.equipment)
      this.data = jsonObject
      this.dialogVisible = true
      this.getPiteurt(screen.device_id)
      setTimeout(() => {
        // console.log(document.getElementsByTagName('table'))
        document.getElementsByTagName('table')[0].style.background = 'var(--bs-background-2)'
        document.getElementsByTagName('table')[0].style.fontSize = '20px'
        document.getElementsByTagName('table')[0].style.color = 'var(--bs-el-text)'
      }, 10)
    },
    confirm () {
      this.dialogVisible = false
    },
    getPiteurt (key) {
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../../../assets/style/bsTheme.scss';
::v-deep .el-dialog__body{
  position: relative;
  min-height: 500px;
  padding: 0 16px 16px 16px !important;
}
.big-screen-list-wrap {
  .el-select {
    display: inline-block !important;
    position: relative !important;
    width: auto !important;
  }

  position: relative;
  height: 100%;
  // margin:0 16px;
  // padding: 16px;
  color: #9ea9b2;
  background-color: var(--bs-background-2) !important;

  .top-search-wrap {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    position: sticky;
    top: 0px;
    z-index: 999;
    padding: 16px 0;
    background-color: var(--bs-background-2);

    .el-input {
      width: 200px;
      margin-right: 20px;

      ::v-deep.el-input__inner {
        background-color: var(--bs-background-1) !important;
      }
    }

    .el-select {
      margin-right: 20px;

      ::v-deep.el-input__inner {
        background: var(--bs-background-1) !important;
        background-color: var(--bs-background-1) !important;
      }
    }
  }

  .list-wrap {
    /* display: grid; */
    overflow: auto;
    // 间隙自适应
    justify-content: space-around;
    // max-height: calc(100vh - 270px);
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    grid-gap: 15px;

    .big-screen-card-wrap {
      position: relative;
      height: 230px;
      cursor: pointer;

      &:hover {
        .screen-card__hover {
          height: 180px;
        }
      }

      .screen-card__hover {
        position: absolute;
        z-index: 999;
        top: 0;
        right: 0;
        left: 0;
        display: flex;
        overflow: hidden;
        align-items: center;
        justify-content: center;
        height: 0;
        transition: height 0.4s;
        background: #00000099;
      }

      .focus {
        color: var(--bs-el-text) !important;
        border: 1px solid var(--bs-el-color-primary) !important;
      }

      .big-screen-card-inner {
        overflow: hidden;
        width: 100%;
        height: 100%;
        cursor: pointer;
        background-color: var(--bs-background-2);
        box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
        color: var(--bs-el-title);
        border: 1px solid var(--bs-background-2);

        &:hover {
          color: var(--bs-el-text);
          border: 1px solid var(--bs-el-color-primary);
        }

        .add-big-screen-card-text {
          color: var(--bs-el-color-primary);
          font-size: 24px;
        }

        .big-screen-card-img {
          width: 100%;
          height: 180px;

          img {
            width: 100%;
            height: 100%;

            object-fit: cover;
          }

          ::v-deep.image-slot {
            height: 100%;
            background-color: var(--bs-background-2);
            display: flex;
            align-items: center;
            justify-content: center;
          }

          ::v-deep.el-image__error {
            background-color: #1d1d1d;
          }
        }

        .big-screen-bottom {
          display: flex;
          align-items: center;
          flex-direction: row;
          justify-content: space-between;
          box-sizing: border-box;
          width: 100%;
          /*height: 26px;*/
          padding: 0 10px;
          height: calc(100% - 180px);
          color: var(--bs-el-title);
          background-color: var(--bs-background-2);

          .left-bigscreen-title {
            font-size: 14px;
            overflow: hidden;
            width: 100%;
            white-space: nowrap;
            text-overflow: ellipsis;
          }

          .right-bigscreen-time-title {
            font-size: 14px;
            overflow: hidden;
            width: 140px;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
        }

        .big-screen-card-text {
          font-size: 14px;
          padding: 10px;
          text-align: center;
          color: #333;
        }
      }

      .big-screen-card-inner-add {
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .el-loading-parent--relative {
    position: unset !important;
  }
}

.footer-pagination-wrap {
    bottom: 5px;
    right: 16px;
    width: 100%;
    margin-top: 16px;
    position: absolute;
  }
.bs-pagination {
  ::v-deep .el-input__inner {
    width: 110px !important;
    border: none;
    background: var(--bs-el-background-1);
  }
}

.empty {
  width: 100%;
  height: 70%;
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.el-descriptions {
    background-color: #f0f0f0;
}
</style>
