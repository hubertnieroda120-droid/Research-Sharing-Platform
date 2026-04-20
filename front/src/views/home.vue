<template>
  <div class="content">
    <div class="welcome-box">
      <div class="title">欢迎使用 {{this.$project.projectName}}</div>
    </div>

    <div class="dashboard-box">
      <el-row :gutter="24">
        <el-col :span="6">
          <div class="data-card">
            <div class="card-icon" style="color: #409EFF;"><i class="el-icon-s-data"></i></div>
            <div class="card-info">
              <div class="card-num">1,256</div>
              <div class="card-name">全网科研课题总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="data-card">
            <div class="card-icon" style="color: #67C23A;"><i class="el-icon-document"></i></div>
            <div class="card-info">
              <div class="card-num">3,442</div>
              <div class="card-name">已归档学术论文</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="data-card">
            <div class="card-icon" style="color: #E6A23C;"><i class="el-icon-cpu"></i></div>
            <div class="card-info">
              <div class="card-num">8,089</div>
              <div class="card-name">IPFS 分布式文件数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="data-card">
            <div class="card-icon" style="color: #F56C6C;"><i class="el-icon-link"></i></div>
            <div class="card-info">
              <div class="card-num">12,112</div>
              <div class="card-name">区块链安全存证数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="visual-box">
      <el-row :gutter="24">
        
        <el-col :span="16">
          <div class="visual-card">
            <div class="card-header">
              <i class="el-icon-s-marketing"></i> 近半年全网科研成果上链趋势
            </div>
            <div id="trendChart" style="width: 100%; height: 350px;"></div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="visual-card">
            <div class="card-header">
              <i class="el-icon-c-scale-to-original"></i> 以太坊存证节点监控
              <span class="live-dot"></span>
            </div>
            <div class="log-container">
              <div class="scroll-list">
                <div class="log-item" v-for="(item, index) in txLogs" :key="index">
                  <div class="log-top">
                    <span class="log-time">{{ item.time }}</span>
                    <span class="log-action">{{ item.action }}</span>
                  </div>
                  <div class="log-hash">TxHash: {{ item.hash }}</div>
                  <div class="log-ipfs">IPFS CID: {{ item.ipfs }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

      </el-row>
    </div>

  </div>
</template>

<script>
import router from '@/router/router-static'
export default {
  data() {
    return {
      // 模拟生成的炫酷区块链底层日志数据
      txLogs: [
        { time: '刚刚', action: '新增软件著作权确权', hash: '0x4f3edf983ac6...2ce7c78d9', ipfs: 'QmRk1Y...V98mN' },
        { time: '1分钟前', action: '学术论文分布存储完成', hash: '0x8a7bcf219ab3...112e4f0a1', ipfs: 'QmXy9a...P2xZ1' },
        { time: '3分钟前', action: '科研成果哈希上链', hash: '0x1c2b3a4d5e6f...7g8h9i0j1', ipfs: 'QmNb3w...L9qR5' },
        { time: '5分钟前', action: '专利信息数字签名', hash: '0x99f8e7d6c5b4...3a2b1c0d9', ipfs: 'QmTz4e...J7kV2' },
        { time: '12分钟前', action: '节点共识验证通过', hash: '0x55a4b3c2d1e0...f9e8d7c6b', ipfs: 'QmPo1q...F5mC8' },
        { time: '18分钟前', action: '新增学术论文上链', hash: '0x77d6c5b4a392...1e0f9a8b7', ipfs: 'QmWs8x...H3nB6' },
        // 重复一遍以实现无缝无尽滚动
        { time: '刚刚', action: '新增软件著作权确权', hash: '0x4f3edf983ac6...2ce7c78d9', ipfs: 'QmRk1Y...V98mN' },
        { time: '1分钟前', action: '学术论文分布存储完成', hash: '0x8a7bcf219ab3...112e4f0a1', ipfs: 'QmXy9a...P2xZ1' },
        { time: '3分钟前', action: '科研成果哈希上链', hash: '0x1c2b3a4d5e6f...7g8h9i0j1', ipfs: 'QmNb3w...L9qR5' },
        { time: '5分钟前', action: '专利信息数字签名', hash: '0x99f8e7d6c5b4...3a2b1c0d9', ipfs: 'QmTz4e...J7kV2' },
      ]
    };
  },
  mounted() {
    this.init();
    // 延迟一点点加载图表，确保 DOM 已经渲染完毕
    setTimeout(() => {
      this.initChart();
    }, 500);
  },
  methods: {
    init() {
      if (this.$storage.get('Token')) {
        this.$http({
          url: `${this.$storage.get('sessionTable')}/session`,
          method: "get"
        }).then(({ data }) => {
          if (data && data.code != 0) {
            router.push({ name: 'login' })
          }
        });
      } else {
        router.push({ name: 'login' })
      }
    },
    // 初始化 ECharts 炫酷图表
    initChart() {
      // 使用项目自带的 echarts
      let trendChart = this.$echarts.init(document.getElementById("trendChart"), 'macarons');
      let option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['科研课题', '学术论文', '区块链存证量'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: ['10月', '11月', '12月', '1月', '2月', '3月', '4月'],
            axisLine: { lineStyle: { color: '#888' } }
          }
        ],
        yAxis: [ { type: 'value', axisLine: { lineStyle: { color: '#888' } } } ],
        series: [
          {
            name: '科研课题',
            type: 'line',
            smooth: true,
            itemStyle: { color: '#409EFF' },
            areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
            data: [120, 132, 101, 134, 90, 230, 210]
          },
          {
            name: '学术论文',
            type: 'line',
            smooth: true,
            itemStyle: { color: '#67C23A' },
            areaStyle: { color: 'rgba(103, 194, 58, 0.2)' },
            data: [220, 182, 191, 234, 290, 330, 310]
          },
          {
            name: '区块链存证量',
            type: 'line',
            smooth: true,
            itemStyle: { color: 'rgba(245, 182, 27, 1)' }, // 提取您的橘黄色主题
            areaStyle: { color: 'rgba(245, 182, 27, 0.3)' },
            data: [150, 232, 201, 154, 190, 330, 410]
          }
        ]
      };
      trendChart.setOption(option);
      // 窗口自适应
      window.addEventListener("resize", () => { trendChart.resize(); });
    }
  }
};
</script>

<style lang="scss" scoped>
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 800px;
  background: transparent;
  padding-top: 3vh;
  padding-bottom: 50px;

  .welcome-box {
    text-align: center;
    margin-bottom: 40px;
    .title {
      font-size: 38px;
      font-weight: bold;
      color: rgba(245, 182, 27, 1);
      text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
      letter-spacing: 2px;
    }
    .sub-title {
      font-size: 16px;
      color: #666;
      margin-top: 15px;
      letter-spacing: 3px;
    }
  }

  /* 数据卡片 */
  .dashboard-box {
    width: 90%;
    margin-bottom: 30px;
    .data-card {
      background: rgba(255, 255, 255, 0.85);
      border-radius: 12px;
      padding: 25px 20px;
      display: flex;
      align-items: center;
      box-shadow: 0 4px 15px rgba(0,0,0,0.05);
      border: 1px solid rgba(245, 182, 27, 0.3);
      transition: transform 0.3s ease;
      &:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(245, 182, 27, 0.2); }
      .card-icon { font-size: 54px; margin-right: 20px; }
      .card-info {
        .card-num { font-size: 32px; font-weight: bold; color: #333; margin-bottom: 5px; }
        .card-name { font-size: 15px; color: #666; }
      }
    }
  }

  /* 炫酷大屏区 */
  .visual-box {
    width: 90%;
    
    .visual-card {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 12px;
      border: 1px solid rgba(220, 223, 230, 0.6);
      box-shadow: 0 4px 15px rgba(0,0,0,0.05);
      padding: 20px;
      height: 420px; /* 固定高度 */
      
      .card-header {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 1px dashed #eee;
        display: flex;
        align-items: center;
        
        i { color: rgba(245, 182, 27, 1); margin-right: 10px; font-size: 22px; }
        
        /* 闪烁的在线红点 */
        .live-dot {
          width: 10px; height: 10px; background: #67C23A; border-radius: 50%;
          margin-left: auto; box-shadow: 0 0 8px #67C23A;
          animation: blink 1.5s infinite ease-in-out;
        }
      }
    }

    /* 区块链滚动日志样式 */
    .log-container {
      height: 330px;
      overflow: hidden; /* 隐藏超出部分以实现滚动 */
      position: relative;
      
      .scroll-list {
        animation: scrollUp 20s linear infinite; /* 无缝滚动动画 */
      }
      .scroll-list:hover {
        animation-play-state: paused; /* 鼠标移上去暂停滚动 */
      }

      .log-item {
        background: rgba(245, 182, 27, 0.05);
        border-left: 4px solid rgba(245, 182, 27, 1);
        padding: 12px 15px;
        margin-bottom: 15px;
        border-radius: 4px;
        font-family: 'Consolas', 'Courier New', monospace; /* 代码极客字体 */
        
        .log-top {
          display: flex; justify-content: space-between; margin-bottom: 8px;
          .log-time { color: #999; font-size: 12px; }
          .log-action { color: #409EFF; font-weight: bold; font-size: 13px;}
        }
        .log-hash, .log-ipfs {
          font-size: 12px; color: #555; word-break: break-all; line-height: 1.6;
        }
        .log-hash { color: #E6A23C; }
      }
    }
  }
}

/* 动画定义 */
@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.3; }
  100% { opacity: 1; }
}
@keyframes scrollUp {
  0% { transform: translateY(0); }
  100% { transform: translateY(-50%); } /* 向上滚动 50%，配合重复数据实现无缝滚动 */
}
</style>