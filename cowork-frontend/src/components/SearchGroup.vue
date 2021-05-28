<template>
  <div>
    <span>
      <el-button
        style="width: 100%"
        icon="el-icon-menu"
        size="small"
        plain
        @click="searchGroupDialog = !searchGroupDialog"
        >群组</el-button
      >
    </span>
    <el-dialog
      :visible.sync="searchGroupDialog"
      width="40%"
      append-to-body
      :show-close="false"
    >
      <el-row
        slot="title"
        style="margin: 0 6px"
        type="flex"
        align="middle"
        :gutter="5"
      >
        <el-col :span="12"
          ><span style="font-size: 18px">搜索群组</span>
        </el-col>
        <el-col :span="10">
          <el-input v-model="groupName" placeholder="群组名称" width="300px"
            ><el-button
              slot="append"
              icon="el-icon-search"
              circle
              @click="searchGroup()"
            ></el-button
          ></el-input>
        </el-col>
        <el-col :span="2">
          <el-popover placement="bottom" width="200" v-model="createGroupPop">
            <el-input
              v-model="newGroupName"
              placeholder="新群组名称"
            ></el-input>
            <div style="text-align: right; margin-top: 10px">
              <el-button
                size="small"
                type="text"
                @click="createGroupPop = false"
                >取消</el-button
              >
              <el-button
                type="primary"
                size="small"
                @click="
                  createGroup(newGroupName)
                  createGroupPop = false
                  newGroupName = ''
                "
                >创建</el-button
              >
            </div>
            <el-button slot="reference" circle icon="el-icon-plus"></el-button>
          </el-popover>
        </el-col>
        <el-col :span="2">
          <el-button
            circle
            icon="el-icon-close"
            @click="searchGroupDialog = !searchGroupDialog"
          ></el-button>
        </el-col>
      </el-row>
      <el-table
        :data="searchResult"
        height="300"
        style="width: 100%"
        :show-header="false"
      >
        <el-table-column property="name" width="120"> </el-table-column>
        <el-table-column property="describe" width="200"> </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
            <el-popconfirm
              placement="bottom"
              trigger="manual"
              title="是否申请加入该群组？"
              v-model="applyGroupPop"
              @confirm="applyGroup(scope.row.id)"
            >
              <el-button
                slot="reference"
                icon="el-icon-plus"
                circle
                size="mini"
                @click.stop="applyGroupPop = !applyGroupPop"
              ></el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchGroupDialog: false,
      applyGroupPop: false,
      searchResult: [],
      groupName: '',
      createGroupPop: false,
      newGroupName: '',
    }
  },
  methods: {
    searchGroup() {
      this.$axios
        .get(`/group/search?name=${this.groupName}`)
        .then((res) => {
          if (res.data.code == 200) {
            this.searchResult = res.data.data
            this.$message.success(`搜索到${this.searchResult.length}条数据`)
          } else this.$message.error(res.data.msg)
        })
        .catch(() => {})
    },
    applyGroup(id) {
      this.$axios.post(`/group/${id}/apply`).then((res) => {
        if (res.data.code == 200) {
          this.$message.success(res.data.msg)
        } else this.$message.error(res.data.msg)
      })
    },
    createGroup(name) {
      this.$axios
        .post('/group', new URLSearchParams({ name: name }))
        .then((res) => {
          if (res.data.code == 200) {
            this.$message.success(res.data.msg)
          } else this.$message.error(res.data.msg)
        })
    },
  },
}
</script>

<style>
</style>