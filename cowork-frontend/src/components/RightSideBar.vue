<template>
  <el-aside width="200px" class="right-side-bar">
    <SearchGroup />
    <p>
      <i
        style="margin: 0 8px 0 3px; font-size: 18px; color: #909399"
        class="el-icon-tickets"
      ></i
      >最近文档
    </p>
    <el-table
      :show-header="false"
      size="small"
      :data="recentDoc"
      @row-click="onRowClick"
    >
      <el-table-column>
        <template slot-scope="scope">
          <span class="recent-doc">{{ scope.row.title }}</span>
        </template>
      </el-table-column>
    </el-table>
    <div v-show="showGroupUser" class="group-user">
      <p>
        <i
          style="margin: 0 8px 0 3px; font-size: 18px; color: #909399"
          class="el-icon-user"
        ></i
        >群组成员
      </p>
      <el-popover
        style="text-align: center"
        v-for="(user, i) in groupUser"
        :key="i"
        placement="bottom"
        width="200"
        trigger="click"
      >
        <div style="text-align: center; margin: 0">
          {{ user.username }}<br />
          {{ user.nickname }}<br />
          {{ user.email }}<br />
        </div>
        <el-button type="text" slot="reference">
          <el-avatar style="margin-right: 10px" :src="user ? user.avatar : ''">
            {{ user ? user.username : '' }}
          </el-avatar>
        </el-button>
      </el-popover>
      <el-button type="text" @click="groupUserDialog = !groupUserDialog">
        <el-avatar icon="el-icon-more"></el-avatar>
      </el-button>
      <GroupTable :groupUserDialog.sync="groupUserDialog" />
    </div>
  </el-aside>
</template>


<script>
import { mapState } from 'vuex'
import GroupTable from '../components/GroupTable'
import SearchGroup from '../components/SearchGroup'
export default {
  components: {
    GroupTable: GroupTable,
    SearchGroup: SearchGroup,
  },
  data() {
    return {
      recentDoc: [],
      groupUser: [],
      showGroupUser: false,
      groupUserDialog: false,
    }
  },
  methods: {
    getRecentDoc() {
      this.$axios.get(`/doc/recent`).then((res) => {
        if (res.data.code == 200) {
          this.recentDoc = res.data.data
        }
      })
    },
    getGroupUser(gid) {
      this.$axios.get(`/group/${gid}/user?p=0&s=9`).then((res) => {
        if (res.data.code == 200) {
          this.groupUser = res.data.data
          this.showGroupUser = true
        }
      })
    },
    onRowClick(data) {
      this.$router.push({ path: `/doc/${data.id}` })
    },
  },
  created() {
    this.getRecentDoc()
    if (this.currentGroup) this.getGroupUser(this.currentGroup)
  },
  watch: {
    currentGroup(val, oldVal) {
      if (val && val != oldVal) this.getGroupUser(val)
      else this.showGroupUser = false
    },
  },
  computed: {
    ...mapState({
      currentGroup: (state) => state.currentGroup,
    }),
  },
}
</script>

<style>
.right-side-bar {
  width: 200px;
  padding: 0 20px;
  font-size: 14px;
  height: calc(100% - 60px);
  position: absolute;
  bottom: 0;
  right: 0;
  overflow: hidden;
  -webkit-transition: width 0.3s;
  transition: width 0.3s;
  border-left: solid 1px #e6e6e6;
}
.group-user {
  color: #303133;
  transition: border-color 0.3s, background-color 0.3s, color 0.3s;
  box-sizing: border-box;
  margin-top: 20px;
}
.recent-doc {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>