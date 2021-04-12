<template>
  <div>
    <el-row style="margin: 0 6px" type="flex" align="middle" :gutter="5">
      <el-col :span="16">
        <p class="table-header">{{ tableHeader }}</p>
      </el-col>
      <el-col :span="6">
        <el-input
          v-show="showElement.search"
          v-model="docTitle"
          placeholder="搜索文档"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            @click="searchDoc(docTitle)"
          ></el-button>
          <el-divider slot="append" direction="vertical"></el-divider>
          <el-button
            slot="append"
            icon="el-icon-close"
            @click="
              clearSearch()
              docTitle = ''
            "
          ></el-button>
        </el-input>
      </el-col>
      <el-col :span="2">
        <el-popover
          v-show="showElement.create"
          placement="top"
          width="160"
          v-model="createPop"
        >
          <el-input v-model="newDoc" placeholder="新文档名称"></el-input>
          <div style="text-align: right; margin-top: 10px">
            <el-button size="small" type="text" @click="createPop = false"
              >取消</el-button
            >
            <el-button
              type="primary"
              size="small"
              @click="
                createDoc(newDoc)
                createPop = false
              "
              >创建</el-button
            >
          </div>
          <el-button slot="reference" icon="el-icon-document-add"></el-button>
        </el-popover>
      </el-col>
    </el-row>
    <el-table :data="docData" style="width: 100%" @row-click="onRowClick">
      <el-table-column prop="title" label="文档名" min-width="70%">
        <template slot-scope="scope">
          <span>{{ scope.row.title }}</span>
          <span class="row-btn">
            <el-button
              v-show="showElement.star"
              icon="el-icon-star-on"
              circle
              size="mini"
              @click.stop="onRowStarClick(scope.row.id)"
            ></el-button>
            <el-popconfirm
              v-show="showElement.remove"
              placement="bottom"
              trigger="manual"
              :title="
                scope.row.delete
                  ? '是否彻底删除该文档？'
                  : '是否将该文档移入回收站？'
              "
              v-model="deletePop"
              @confirm="onRowDeleteClick(scope.row)"
            >
              <el-button
                slot="reference"
                icon="el-icon-delete"
                circle
                size="mini"
                @click.stop="deletePop = !deletePop"
              ></el-button>
            </el-popconfirm>
            <el-popconfirm
              v-show="showElement.remove && scope.row.delete"
              placement="bottom"
              trigger="manual"
              title="是否恢复该文档？"
              v-model="recoveryPop"
              @confirm="onRowRecoveryClick(scope.row)"
            >
              <el-button
                slot="reference"
                icon="el-icon-refresh-left"
                circle
                size="mini"
                @click.stop="recoveryPop = !recoveryPop"
              ></el-button>
            </el-popconfirm>
            <DocUrlPop
              v-show="showElement.share"
              :docInfo="scope.row"
              btnSize="mini"
            />
          </span>
        </template>
      </el-table-column>
      <el-table-column
        v-if="showElement.owner"
        prop="owner"
        label="所有者"
        width="150"
      >
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="230">
        <template slot-scope="scope">
          <span>{{ new Date(scope.row.updateTime) | dateFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import DocUrlPop from '../components/DocUrlPop'
export default {
  components: {
    DocUrlPop,
  },
  props: {
    docData: {
      type: Array,
      default: () => [],
    },
    tableHeader: {
      type: String,
      default: '个人文档',
    },
    showElement: {
      type: Object,
      default: () => ({
        search: true,
        create: true,
        star: true,
        remove: true,
        share: true,
        owner: false,
      }),
    },
    searchDoc: {
      type: Function,
      require: true,
    },
    clearSearch: {
      type: Function,
      require: true,
    },
    createDoc: {
      type: Function,
      require: true,
    },
    onRowClick: {
      type: Function,
      require: true,
    },
    onRowStarClick: {
      type: Function,
      require: true,
    },
    onRowDeleteClick: {
      type: Function,
      require: true,
    },
    onRowRecoveryClick: {
      type: Function,
      require: true,
    },
  },
  data() {
    return {
      createPop: false,
      deletePop: false,
      recoveryPop: false,
      docTitle: '',
      newDoc: '',
    }
  },
  methods: {},
}
</script>

<style scoped>
.row-btn {
  margin: 0 20px;
  transition: all 0.3s;
  opacity: 0;
}
.row-btn > * {
  margin-right: 10px;
}
.el-table__body tr:hover .row-btn {
  opacity: 1;
}
.table-header {
  font-size: 18px;
  font-family: SimSun, 宋体, sans-serif, 'Microsoft YaHei', 微软雅黑;
}
</style>