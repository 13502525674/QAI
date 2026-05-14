<template>
  <div class="study-room">
    <!-- 顶部区域：子页面（草稿纸/记录等）时隐藏，避免遮挡 -->
    <div v-if="currentPage === 'main'" class="top-section">
      <div style="display: flex; align-items: center; gap: 10px;">
        <div class="logo">
          <span class="logo-icon">📚</span> <span style="color: #ffffff; text-shadow: 0 0 10px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);">西柚自习室</span>
        </div>
        <div class="countdown-display-wrap" style="display: flex; flex-wrap: wrap; gap: 10px; align-items: center;">
          <div v-for="item in countdownDisplayItems" :key="item.id" class="countdown-display">
            {{ item.text }}
          </div>
          <div v-if="!countdownDisplayItems.length" class="countdown-display">暂无目标</div>
        </div>
      </div>
      <div class="current-time">
        <span id="time-display">{{ currentTime }}</span>
      </div>
      <div class="daily-physics" id="dailyPhysics">
        <div class="physics-quote" id="physicsQuote">{{ physicsQuote }}</div>
        <button class="refresh-btn" @click="refreshPhysicsQuote" title="刷新物理定律每日一悟">🔄</button>
      </div>
      <div class="header-buttons">
        <button class="btn" @click="showCheckinModal">签到</button>
        <button class="btn" @click="showGoalCountdown">设置目标倒计日</button>
        <button class="btn" @click="exitStudyRoom">退出</button>
      </div>
    </div>

    <!-- 主内容区域：仅在主页面显示，避免遮挡草稿纸/记录页的按钮 -->
    <div v-if="currentPage === 'main'" class="main-content">
      <!-- 左侧工具区域 -->
      <div class="left-tools">
        <div class="tool-section tool-section-link">
          <div class="formula-library-btn" @click="showFormulaLibrary">
            <div class="tool-title">
              <span>📐</span> 动态公式库
            </div>
          </div>
        </div>

        <div class="tool-section tool-section-link">
          <div class="formula-library-btn" @click="showNotebook">
            <div class="tool-title">
              <span>📝</span> 草稿纸与计算器
            </div>
          </div>
        </div>

        <div class="tool-section tool-section-link">
          <div class="formula-library-btn" @click="showPhysicsLaws">
            <div class="tool-title">
              <span>💡</span> 物理定律每日一悟
            </div>
          </div>
        </div>

        <!-- 白噪音区域 - 不需要 tool-section 外框 -->
        <div class="noise-panel" style="margin-top: 20px;">
          <div class="tool-title noise-title">
            <span>🎵</span> 白噪音
            <button class="noise-close-btn" @click="stopNoise">关闭</button>
          </div>
          <div class="noise-buttons">
            <button class="noise-btn" @click="playNoise('summer')">🐛 夏夜</button>
            <button class="noise-btn" @click="playNoise('beach')">🌊 海边</button>
            <button class="noise-btn" @click="playNoise('forest')">🌲 森林</button>
            <button class="noise-btn" @click="playNoise('water')">💧 水滴</button>
          </div>
          <div class="volume-control">
            <span>🔊</span>
            <input type="range" class="volume-slider" id="volumeSlider" min="0" max="100" v-model="volume" @change="setVolume">
          </div>
        </div>
      </div>

      <!-- 中部自习室场景 -->
      <div class="center-room">
        <div class="room-background">
          <video ref="bgVideoPlayer" class="bg-video" autoplay muted loop :key="bgVideo">
            <source :src="bgVideo" type="video/mp4">
            Your browser does not support the video tag.
          </video>
        </div>
        
        <!-- 环境背景控制 -->
        <div style="position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); text-align: center; background-color: rgba(10, 17, 40, 0.05); border-radius: 5px; padding: 0px; border: 2px solid rgba(79, 195, 247, 0.4); z-index: 5; width: auto; min-width: 100px;">
          <div class="environment-controls">
            <div class="env-btn" title="星空背景" @click="changeBackground('starry')">🌌</div>
            <div class="env-btn" title="森林背景" @click="changeBackground('forest')">🌲</div>
            <div class="env-btn" title="海边背景" @click="changeBackground('beach')">🏖️</div>
          </div>
        </div>
        
        <!-- 学习时间和智能选座整体容器 -->
        <div style="position: absolute; bottom: 120px; left: 50%; transform: translateX(-50%); z-index: 10; display: flex; gap: 12px; justify-content: center;">
          <!-- 学习时间计时器 -->
          <div class="study-timer-container">
            <div class="tool-title">
              <span>⏰</span> 学习时间
            </div>
            <div class="timer-panel">
              <div class="timer-display" id="timerDisplay">{{ timerDisplay }}</div>
              <div class="timer-settings">
                <label>学习<br>时间：</label>
                <input type="number" class="timer-input" id="timerInput" v-model="timerMinutes" min="1" max="9999" placeholder="输入分钟数" @input="updateTimerFromInput">
                <span style="color: #ffffff; font-size: 12px;">分钟</span>
              </div>
              <div class="timer-controls">
                <button class="timer-btn start-btn" @click="startTimer">开始</button>
                <button class="timer-btn pause-btn" @click="pauseTimer">暂停</button>
                <button class="timer-btn reset-btn" @click="resetTimer">重置</button>
              </div>
              <!-- <div class="timer-stats">
                <span>今日学习: {{ todayStudyTime }}分钟</span>
                <span>本周学习: {{ weekStudyTime }}分钟</span>
              </div> -->
            </div>
          </div>

          <!-- 智能选座系统 -->
          <div class="smart-seat-container">
            <button class="smart-seat-btn" @click="openSeatSystemModal">智能选座</button>
            <div id="studyGoalDisplay">{{ studyGoal ? '自习目标：' + studyGoal : '自习目标：未设置' }}</div>
          </div>
        </div>
        
        <!-- 座位系统弹窗（已隐藏，仅使用下方智能选座6x7网格） -->
        <div class="custom-modal" v-if="false" :class="{ 'show': showSeatSystemModal }" @click.self="closeSeatSystemModal">
          <div class="custom-modal-content" style="max-width: 800px;">
            <div class="custom-modal-header">
              <div class="custom-modal-title">自习室选座系统</div>
            </div>
            <div class="custom-modal-body" style="text-align: left;">
              <!-- 目标面板 -->
              <div style="display: flex; gap: 16px; flex-wrap: wrap;">
                <!-- 左侧目标面板 -->
                <div style="flex: 0 0 200px; background: rgba(255, 255, 255, 0.05); border-radius: 16px; padding: 16px 12px; border: 1px solid rgba(79, 195, 247, 0.3); display: flex; flex-direction: column; gap: 18px;">
                  <div>
                    <div style="font-size: 14px; font-weight: 600; color: #4fc3f7; margin-bottom: 8px;">🎯 今日目标</div>
                    <input type="text" v-model="studyGoal" placeholder="例如：奋战电磁学" maxlength="20" autocomplete="off" style="width: 100%; padding: 10px 12px; border: 1px solid rgba(79, 195, 247, 0.4); border-radius: 10px; font-size: 13px; background: rgba(255,255, 255, 0.15); color: #ffffff; margin-bottom: 12px;">
                    <button @click="saveSeatGoal" style="width: 100%; padding: 8px 16px; background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%); border: none; border-radius: 10px; color: white; font-weight: 600; cursor: pointer; transition: all 0.3s ease;">保存目标</button>
                  </div>
                  <div>
                    <div style="font-size: 14px; font-weight: 600; color: #4fc3f7; margin-bottom: 8px;">👥 同桌信息</div>
                    <div v-if="deskmate" style="background: rgba(255, 255, 255, 0.05); border-radius: 10px; padding: 12px; border: 1px solid rgba(79, 195, 247, 0.3);">
                      <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 8px;">
                        <div style="width: 40px; height: 40px; border-radius: 50%; background: #4fc3f7; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px;">{{ deskmate.nickname ? deskmate.nickname.charAt(0) : '?' }}</div>
                        <div>
                          <div style="font-weight: bold; color: #ffffff;">{{ deskmate.nickname || '未知' }}</div>
                          <div style="font-size: 12px; opacity: 0.7;">{{ deskmate.goal || '暂无目标' }}</div>
                        </div>
                      </div>
                    </div>
                    <div v-else style="background: rgba(255, 255, 255, 0.05); border-radius: 10px; padding: 12px; border: 1px solid rgba(79, 195, 247, 0.3); text-align: center;">
                      <div style="color: rgba(255, 255, 255, 0.6); font-size: 13px;">暂无同桌</div>
                    </div>
                  </div>
                </div>
                
                <!-- 右侧座位面板 -->
                <div style="flex: 1; min-width: 300px; background: rgba(255, 255, 255, 0.05); border-radius: 16px; padding: 20px; border: 1px solid rgba(79, 195, 247, 0.3);">
                  <div style="font-size: 16px; font-weight: 600; color: #4fc3f7; margin-bottom: 16px; text-align: center;">🪑 自习室座位</div>
                  <div class="seats-container" style="grid-template-columns: repeat(5, 1fr); gap: 10px; margin-bottom: 16px;">
                    <div 
                      v-for="seat in seats" 
                      :key="seat.seatId"
                      class="seat"
                      :class="{
                        'occupied': seat.status === 1 && seat.userId !== currentUserId,
                        'your-seat': seat.status === 1 && seat.userId === currentUserId
                      }"
                      @click="selectSeat(seat)"
                    >
                      <div class="seat-number">{{ seat.seatNumber }}</div>
                      <div class="seat-status">
                        {{ seat.status === 1 ? (seat.userId === currentUserId ? '我的' : '已占') : '空闲' }}
                      </div>
                    </div>
                  </div>
                  <button class="random-seat-btn" @click="randomSelectSeat" style="width: 100%;">随机选座</button>
                </div>
              </div>
            </div>
            <div class="custom-modal-footer">
              <button class="custom-modal-btn secondary" @click="closeSeatSystemModal">{{ hasSelectedSeat() ? '选座完成，放回自习' : '关闭' }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧同伴区域 -->
      <div class="right-companions">
        <div class="tool-section">
          <div class="tool-title">
            <span>📋</span> 今日计划
          </div>
          <div class="todo-panel">
            <div class="todo-list">
              <div v-for="todo in todos" :key="todo.id" class="todo-item" :class="{ 'completed': todo.completed === 1 }">
                <input type="checkbox" class="todo-checkbox" :checked="todo.completed === 1" @change="toggleTodo(todo.id)">
                <div class="todo-text">{{ todo.content }}</div>
                <button class="todo-delete" @click="deleteTodo(todo.id)">×</button>
              </div>
              <div v-if="todos.length === 0" class="empty-tip">
                暂无待办事项
              </div>
            </div>
            <div class="add-todo">
              <input type="text" class="todo-input" v-model="newTodo" placeholder="添加待办事项..." @keyup.enter="addTodo">
              <button class="add-btn" @click="addTodo">添加</button>
            </div>
          </div>
        </div>

        <div class="tool-section chat-room-entry" @click="openChatRoom">
          <div class="tool-title">
            <span>💬</span> 学习交流室
          </div>
          <div class="chat-room-preview">
            <div class="preview-tip">点击进入聊天室，与同学交流学习心得</div>
          </div>
        </div>
      </div>
    </div>

    <ChatRoomModal :visible.sync="showChatRoom" @close="showChatRoom = false" />

    <!-- 弹窗 -->
    <div class="custom-modal" :class="{ 'show': showModal }" @click.self="closeModal">
      <div class="custom-modal-content">
        <div class="custom-modal-header">
          <div class="custom-modal-title" :class="{ 'goal-title': modalType === 'goal' }">{{ modalTitle }}</div>
        </div>
        <div class="custom-modal-body">
          <template v-if="modalType === 'goal'">
            <div class="goal-list" v-if="countdownGoals.length">
              <div v-for="(g, idx) in countdownGoals" :key="g.id" class="goal-item">
                <span class="goal-item-tag">目标{{ idx + 1 }}</span>
                <span class="goal-item-name">{{ g.goalName }}</span>
                <span class="goal-item-date">{{ g.goalDate }}</span>
                <button type="button" class="goal-item-del" @click="deleteCountdownGoal(g.id)">删除</button>
              </div>
            </div>
            <p class="modal-label" style="margin-top:12px">添加新目标</p>
            <input type="text" class="custom-modal-input" v-model="goalName" placeholder="例如：高考、考研、期末考试">
            <p class="modal-label">请选择您的目标日期</p>
            <el-date-picker v-model="goalDate" type="date" placeholder="请选择年、月、日" value-format="yyyy-MM-dd" format="yyyy年MM月dd日" class="goal-date-picker" style="width:100%"></el-date-picker>
          </template>
          <template v-else>
            {{ modalContent }}
            <input v-if="modalType === 'seat'" type="text" class="custom-modal-input" v-model="studyGoal" placeholder="设置学习目标...">
          </template>
        </div>
        <div class="custom-modal-footer">
          <button class="custom-modal-btn secondary" @click="closeModal">取消</button>
          <button class="custom-modal-btn primary" @click="confirmModal">确定</button>
        </div>
      </div>
    </div>

    <!-- 签到弹窗 -->
    <div class="custom-modal" :class="{ 'show': showCheckinModalFlag }" @click.self="closeCheckinModal">
      <div class="custom-modal-content checkin-modal-content">
        <div class="custom-modal-header">
          <div class="custom-modal-title checkin-title">每日签到</div>
        </div>
        <div class="custom-modal-body checkin-body">
          <!-- 连续签到天数 -->
          <div class="checkin-streak">
            <div class="streak-label">连续签到</div>
            <div class="streak-days"><span>{{ streakDays }}</span> 天</div>
            <div class="today-checked">今日已签到: <span :style="{ color: todayChecked ? '#4caf50' : '#ff5252' }">{{ todayChecked ? '是' : '否' }}</span></div>
          </div>
          
          <!-- 一键签到按钮 -->
          <div class="checkin-btn-wrapper">
            <button class="custom-modal-btn primary checkin-btn" @click="checkinToday" :disabled="todayChecked">一键签到</button>
          </div>
          
          <!-- 日历视图 -->
          <div class="calendar-container">
            <div class="calendar-header">
              <button class="calendar-nav-btn" @click="changeMonth(-1)">←</button>
              <div class="current-month">{{ currentMonthText }}</div>
              <button class="calendar-nav-btn" @click="changeMonth(1)">→</button>
            </div>
            <div class="calendar-grid">
              <div class="calendar-weekday" v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day">{{ day }}</div>
              <div 
                v-for="(date, index) in calendarDates" 
                :key="index"
                class="calendar-day"
                :class="{ 
                  'checked': date.checked, 
                  'today': date.isToday,
                  'other-month': date.otherMonth 
                }"
              >
                {{ date.day }}
              </div>
            </div>
          </div>
          
          <!-- 签到记录 -->
          <div class="checkin-legend">
            <div class="legend-item">
              <span class="legend-dot checked"></span>
              <span>已签到</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot unchecked"></span>
              <span>未签到</span>
            </div>
          </div>
        </div>
        <div class="custom-modal-footer">
          <button class="custom-modal-btn secondary" @click="closeCheckinModal" style="padding: 8px 16px; font-size: 12px;">关闭</button>
        </div>
      </div>
    </div>

      <!-- 智能选座系统弹窗 -->
      <div v-if="showSeatSystemModal" class="seat-system-modal">
        <div class="seat-system-overlay" @click="closeSeatSystemModal"></div>
        <div class="seat-system-container">
          <!-- 顶部 -->
          <div class="seat-system-header">
            <span class="seat-system-logo">🍊 西柚自习室</span>
            <div class="seat-system-user-bar">
              <span class="seat-system-user-name">{{ seatSystem.currentUserName || seatSystem.currentUser }}</span>
              <button class="seat-system-switch-btn" @click="closeSeatSystemModal">退出</button>
            </div>
          </div>

          <!-- 三栏主体 -->
          <div class="seat-system-main-panel">
            <!-- 左侧 目标标记 -->
            <div class="seat-system-left-goal">
              <div class="seat-system-goal-section">
                <span class="seat-system-section-title">🎯 今日目标</span>
                <input 
                  type="text" 
                  class="seat-system-goal-input" 
                  v-model="seatSystem.goalInput" 
                  placeholder="例如：奋战电磁学" 
                  maxlength="20" 
                  :disabled="!getMySeat()"
                >
                <button class="seat-system-btn seat-system-btn-primary" @click="saveGoal">保存目标</button>
              </div>
              <div class="seat-system-goal-section">
                <span class="seat-system-section-title">🪑 我的座位</span>
                <button class="seat-system-btn seat-system-btn-danger" @click="leaveSeat">离开座位</button>
              </div>
              <div class="seat-system-tip">
                <span>💡 悬停座位看目标</span>
              </div>
            </div>

            <!-- 中间 极小座位网格 6x7，用 key 强制选座后重绘以正确显示绿色 -->
            <div class="seat-system-middle-seats">
              <div class="seat-system-seat-grid" :key="seatSystemUpdateKey">
                <div v-for="row in ['A','B','C','D','E','F']" :key="row" class="seat-system-seat-row">
                  <div 
                    v-for="col in [1,2,3,4,5,6,7]" 
                    :key="row+col"
                    class="seat-system-seat"
                    :class="seatClass(row, col)"
                    :data-tip="seatSystem.seats[row+col]?.occupied ? ((seatSystem.seats[row+col]?.nickname || seatSystem.seats[row+col]?.user) + (seatSystem.seats[row+col]?.goal ? ': ' + seatSystem.seats[row+col]?.goal : ' 学习中')) : '空闲'"
                    @click="onSeatClick(row+col)"
                  >
                    {{ row+col }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部统计 -->
          <div class="seat-system-footer-stats">
            <span>🟥 占用 {{ Object.values(seatSystem.seats).filter(s => s.occupied).length }}</span>
            <span>🟩 我的 {{ getMySeat() || '无' }}</span>
            <span>⬜ 空闲 {{ Object.values(seatSystem.seats).filter(s => !s.occupied).length }}</span>
          </div>
          
          <!-- 在线同学列表 -->
          <div class="seat-system-online-users">
            <div class="online-users-header">
              <span>👥 在线同学</span>
              <span class="online-count">{{ onlineUsers.length }}人</span>
            </div>
            <div class="online-users-list">
              <div v-for="user in onlineUsers" :key="user.id" class="online-user-item">
                <div class="user-avatar">{{ (user.nickname || '用户').charAt(0) }}</div>
                <div class="user-info">
                  <div class="user-name">{{ user.nickname || '用户' }}</div>
                  <div class="user-seat">座位: {{ user.seatId || '未选座' }}</div>
                </div>
                <div v-if="user.goal" class="user-goal">{{ user.goal }}</div>
              </div>
              <div v-if="onlineUsers.length === 0" class="no-users">暂无在线同学</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 动态公式库页面 -->
      <div v-if="currentPage === 'formulaLibrary'" class="formula-library-page">
        <div class="formula-library-container">
          <header class="formula-library-header">
            <div class="formula-library-title">物理公式库</div>
            <button class="formula-library-back-btn" @click="currentPage = 'main'">返回自习室</button>
          </header>

          <div class="formula-library-banner">
            <div class="formula-library-slider-container">
              <div 
                class="formula-library-slider"
                :class="{ 'paused': formulaLibrary.isPaused }"
                style="--quantity: 10"
              >
                <div 
                  v-for="(formula, index) in formulaLibrary.formulas" 
                  :key="formula.id"
                  class="formula-library-item"
                  :style="{ '--position': index + 1 }"
                >
                  <div class="formula-library-item-content">
                    <div class="formula-library-item-icon">{{ formula.icon }}</div>
                    <div class="formula-library-item-title">{{ formula.title }}</div>
                    <div class="formula-library-item-formula">{{ formula.formula }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="formula-library-controls">
              <button class="formula-library-control-btn" @click="pauseFormulaLibraryRotation">
                {{ formulaLibrary.isPaused ? '▶️ 继续' : '⏸️ 暂停' }}
              </button>
            </div>

            <div class="formula-library-instructions">
              🔍 鼠标悬停查看详情 | ⏸️ 点击暂停旋转 | 📱 支持移动端查看
            </div>
          </div>
        </div>
      </div>

      <!-- 草稿纸与计算器页面：用 Portal 渲染到 body 层，避免 stacking context 导致按钮无法点击 -->
      <portal to="notebook-pages">
      <div v-if="currentPage === 'notebook'" class="notebook-page">
        <div class="notebook-wrapper">
          <header class="notebook-header">
            <div class="header-title">草稿纸与计算器</div>
            <button class="notebook-back-btn" @click="currentPage = 'main'">← 返回自习室</button>
          </header>

          <div class="notebook-main-content">
            <div class="notebook-container content-section">
              <div class="notebook-title">在这里写下你的思考...</div>
              <textarea 
                class="notebook-area" 
                v-model="notebook.textContent" 
                placeholder="开始记录你的物理学习思考、公式推导或解题过程..."
              ></textarea>
              <div class="notebook-controls">
                <button type="button" class="record-btn" @click="showNotebookRecords">📋 草稿记录</button>
                <div class="control-buttons">
                  <button type="button" class="control-btn" @click="confirmClearNotebook">清空</button>
                  <button type="button" class="control-btn primary" @click="saveNotebookRecord">保存</button>
                </div>
              </div>
            </div>

            <div class="calculator-container content-section">
              <div class="calculator-title">科学计算器</div>
              <div class="calculator-display">
                <input type="text" class="calculator-input" :value="notebook.calculatorValue" readonly>
              </div>
              <div class="calculator-buttons">
                  <button class="calc-btn function" @click="appendFunction('sin')">sin</button>
                  <button class="calc-btn function" @click="appendFunction('cos')">cos</button>
                  <button class="calc-btn function" @click="appendFunction('tan')">tan</button>
                  <button class="calc-btn function" @click="appendFunction('log')">log</button>
                  <button class="calc-btn function" @click="appendFunction('ln')">ln</button>
                  
                  <button class="calc-btn function" @click="appendSquareRoot()">√</button>
                  <button class="calc-btn function" @click="appendSquare()">x²</button>
                  <button class="calc-btn function" @click="appendCube()">x³</button>
                  <button class="calc-btn function" @click="appendPi()">π</button>
                  <button class="calc-btn function" @click="appendDegree()">°</button>
                  
                  <button class="calc-btn" @click="appendValue('7')">7</button>
                  <button class="calc-btn" @click="appendValue('8')">8</button>
                  <button class="calc-btn" @click="appendValue('9')">9</button>
                  <button class="calc-btn operator" @click="appendOperator('+')">+</button>
                  <button class="calc-btn clear" @click="clearDisplay()">清空</button>
                  
                  <button class="calc-btn" @click="appendValue('4')">4</button>
                  <button class="calc-btn" @click="appendValue('5')">5</button>
                  <button class="calc-btn" @click="appendValue('6')">6</button>
                  <button class="calc-btn operator" @click="appendOperator('-')">-</button>
                  <button class="calc-btn" @click="deleteLast()">⌫</button>
                  
                  <button class="calc-btn" @click="appendValue('1')">1</button>
                  <button class="calc-btn" @click="appendValue('2')">2</button>
                  <button class="calc-btn" @click="appendValue('3')">3</button>
                  <button class="calc-btn operator" @click="appendOperator('*')">×</button>
                  <button class="calc-btn" @click="appendOperator('(')">(</button>
                  
                  <button class="calc-btn" @click="appendValue('0')">0</button>
                  <button class="calc-btn" @click="appendValue('.')">.</button>
                  <button class="calc-btn" @click="appendOperator('^')">^</button>
                  <button class="calc-btn operator" @click="appendOperator('/')">÷</button>
                  <button class="calc-btn" @click="appendOperator(')')">)</button>
                  
                  <button class="calc-btn equal disabled" style="grid-column: span 3;">根号后面需要自行加括号</button>
                  <button class="calc-btn equal" @click="calculate()">=</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 草稿记录页面 -->
      <div v-if="currentPage === 'notebookRecords'" class="notebook-records-page">
        <div class="notebook-records-container">
          <header class="notebook-records-header">
            <div class="notebook-records-title">草稿记录</div>
            <button class="notebook-records-back-btn" @click="currentPage = 'notebook'">← 返回草稿纸</button>
          </header>

          <div class="notebook-records-main-content">
            <div class="notebook-records-fig3-section">
              <div class="notebook-records-fig3-title">📋 所有草稿记录</div>
              <div v-if="notebookRecords.records.length === 0" class="empty-records">
                <div class="empty-records-icon">📝</div>
                <div class="empty-records-text">暂无草稿记录</div>
                <div class="empty-records-hint">开始使用草稿纸，记录你的学习思路吧！</div>
              </div>
              <div 
                v-for="(record, idx) in notebookRecords.records" 
                :key="record.id"
                class="notebook-record-card"
              >
                <div class="notebook-record-card-header">
                  <div class="notebook-record-card-title">{{ record.title || ('草稿 ' + (notebookRecords.records.length - idx)) }}</div>
                  <div class="notebook-record-card-time">{{ formatRecordTime(record.updateTime || record.createTime) }}</div>
                </div>
                <div class="notebook-record-card-snippet">{{ getRecordSnippet(record) }}</div>
                <div class="notebook-record-card-actions">
                  <button type="button" class="record-action-btn edit" @click.stop="editNotebookRecord(record)">编辑</button>
                  <button type="button" class="record-action-btn rename" @click.stop="renameNotebookRecord(record)">重命名</button>
                  <button type="button" class="record-action-btn view" @click.stop="viewNotebookRecord(record)">查看</button>
                  <button type="button" class="record-action-btn delete" @click.stop="deleteNotebookRecord(record.id)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 查看草稿弹窗（需在 portal 内以正确显示在记录列表之上） -->
      <div v-if="notebookRecords.recordView" class="notebook-view-modal" @click="closeRecordView">
        <div class="notebook-view-content" @click.stop>
          <div class="notebook-view-header">
            <h3>{{ notebookRecords.recordView.title }}</h3>
            <button class="notebook-view-close" @click="closeRecordView">✕</button>
          </div>
          <div class="notebook-view-body">
            <div v-if="getRecordText(notebookRecords.recordView)" class="notebook-view-text">{{ getRecordText(notebookRecords.recordView) }}</div>
            <img v-if="getRecordCanvas(notebookRecords.recordView)" :src="getRecordCanvas(notebookRecords.recordView)" alt="涂鸦" class="notebook-view-canvas">
          </div>
        </div>
      </div>

      <!-- 编辑草稿弹窗（风格与重命名一致） -->
      <div v-if="notebookRecords.editRecord" class="notebook-edit-modal" @click="closeEditNotebookRecord">
        <div class="notebook-edit-content" @click.stop>
          <div class="notebook-edit-header">
            <h3>编辑草稿 - {{ notebookRecords.editRecord.title || '草稿' }}</h3>
            <button class="notebook-edit-close" @click="closeEditNotebookRecord">✕</button>
          </div>
          <div class="notebook-edit-body">
            <textarea v-model="notebookRecords.editContent" class="notebook-edit-textarea" placeholder="在这里编辑草稿内容..."></textarea>
          </div>
          <div class="notebook-edit-footer">
            <button type="button" class="notebook-edit-btn cancel" @click="closeEditNotebookRecord">取消</button>
            <button type="button" class="notebook-edit-btn primary" @click="saveEditNotebookRecord">保存修改</button>
          </div>
        </div>
      </div>
      </portal>

      <!-- 物理定律每日一悟页面 -->
      <div v-if="currentPage === 'physicsLaws'" class="physics-laws-page">
        <div class="physics-laws-container">
          <header class="physics-laws-header">
            <div class="header-left">
              <div class="header-title">物理定律每日一悟</div>
            </div>
            <div class="header-right" style="display: flex; gap: 15px; align-items: center;">
              <div class="search-container">
                <input 
                  type="text" 
                  class="search-input" 
                  v-model="physicsLaws.searchQuery"
                  placeholder="搜索物理定律..."
                  @keyup.enter="performSearch"
                >
                <button 
                  class="search-btn" 
                  @click="performSearch"
                  v-show="!physicsLaws.searchTerm"
                >🔍</button>
                <button 
                  class="clear-btn" 
                  @click="clearSearch"
                  v-show="physicsLaws.searchTerm"
                >✕</button>
              </div>
              <button class="favorites-btn" @click="showFavorites">
                ⭐ 收藏
                <span class="favorites-count">{{ physicsLaws.favorites.length }}</span>
              </button>
              <button class="back-btn" @click="currentPage = 'main'">← 返回自习室</button>
            </div>
          </header>

          <div class="physics-laws-scroll-wrap">
            <div class="main-content">
            <div class="section-title">
              <h1>🔬 物理定律每日一悟</h1>
              <p>探索物理世界的智慧，感悟学习与生活的真谛</p>
            </div>

            <div class="physics-laws-list">
              <div 
                v-for="(law, index) in getFilteredLaws()" 
                :key="law.id"
                class="law-item"
              >
                <div class="law-header">
                  <div class="law-icon">{{ law.icon }}</div>
                  <div class="law-title">
                    <h2 v-html="highlightText(law.law, physicsLaws.searchTerm)"></h2>
                    <p v-html="highlightText(law.category, physicsLaws.searchTerm)"></p>
                  </div>
                  <div class="law-actions" style="display: flex; align-items: center; gap: 15px;">
                    <button 
                      class="favorite-btn" 
                      :class="{ active: isFavorite(law) }"
                      @click="toggleFavorite(law)"
                      title="收藏"
                    >
                      {{ isFavorite(law) ? '★' : '☆' }}
                    </button>
                    <div class="law-index">#{{ law.id }}</div>
                  </div>
                </div>
                <div class="law-quote" v-html="highlightText(law.quote, physicsLaws.searchTerm)"></div>
                <div class="law-meta">
                  <span>
                    {{ physicsLaws.searchTerm ? '搜索结果第 ' + (index + 1) + ' 条' : '第 ' + (index + 1) + ' 条' }}
                  </span>
                  <span>
                    共 {{ getFilteredLaws().length }} 条
                  </span>
                </div>
              </div>
              <div v-if="getFilteredLaws().length === 0" class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <h3>未找到相关物理定律</h3>
                <p v-if="physicsLaws.searchTerm">没有找到与 "{{ physicsLaws.searchTerm }}" 相关的物理定律</p>
                <p v-else>暂无物理定律</p>
                <button class="back-btn" style="margin-top: 20px;" @click="clearSearch" v-if="physicsLaws.searchTerm">显示全部</button>
              </div>
            </div>
            </div>
          </div>

          <!-- 收藏模态框 -->
          <div class="favorites-modal" v-if="physicsLaws.showFavoritesModal" @click="closeFavorites">
            <div class="favorites-content" @click.stop>
              <div class="favorites-header">
                <h2>⭐ 我的收藏</h2>
                <button class="close-modal" @click="closeFavorites">✕</button>
              </div>
              <div class="favorites-list">
                <div v-if="physicsLaws.favorites.length === 0" class="favorites-empty">
                  <div class="favorites-empty-icon">⭐</div>
                  <h3>暂无收藏</h3>
                  <p>点击卡片上的五角星按钮收藏你喜欢的物理定律</p>
                </div>
                <div 
                  v-for="(law, index) in getFavoriteLaws()" 
                  :key="law.id"
                  class="law-item"
                  style="padding: 20px;"
                >
                  <div class="law-header" style="margin-bottom: 15px;">
                    <div class="law-icon" style="width: 50px; height: 50px; font-size: 24px;">{{ law.icon }}</div>
                    <div class="law-title">
                      <h2 style="font-size: 18px;">{{ law.law }}</h2>
                      <p>{{ law.category }}</p>
                    </div>
                    <button 
                      class="favorite-btn active" 
                      @click="toggleFavorite(law)"
                      title="取消收藏"
                    >
                      ★
                    </button>
                  </div>
                  <div class="law-quote" style="font-size: 14px; padding: 15px;">{{ law.quote }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>

<script>
import request, { get, post } from '@/utils/request'
import { MessageBox } from 'element-ui'
import ChatRoomModal from '@/components/ChatRoom/ChatRoomModal.vue'

export default {
  components: {
    ChatRoomModal
  },
  data() {
    return {
      currentTime: '',
      physicsQuote: '知识是负熵，你今天的学习，正在对抗全世界的混乱。',
      volume: 50,
      bgVideo: '/wp/林间晨光.mp4',
      timerMinutes: 25,
      timerDisplay: '25:00',
      timerRunning: false,
      timerInterval: null,
      timerSeconds: 25 * 60,
      todayStudyTime: 0,
      weekStudyTime: 0,
      seats: [],
      currentUserId: null,
      onlineUsers: [],
      messages: [],
      messageContent: '',
      todos: [],
      newTodo: '',
      showModal: false,
      modalTitle: '',
      modalContent: '',
      showChatRoom: false,
      modalType: '',
      goalDate: '',
      goalName: '',
      studyGoal: '',
      countdownGoals: [],
      selectedSeat: null,
      showSeatSystemModal: false,
      currentNoise: null,
      // 签到数据
      showCheckinModalFlag: false,
      streakDays: 0,
      todayChecked: false,
      checkinData: {},
      currentCheckinMonth: new Date(),
      // 选座系统数据
      seatSystem: {
        currentUser: '',
        currentUserName: '',
        seats: {},
        chats: {},
        goalInput: '',
        chatInput: '',
        deskmates: [],
        deskmateNames: []
      },
      seatSystemUpdateKey: 0,
      mySeatIdInModal: '',
      // 当前页面状态
      currentPage: 'main',
      // 动态公式库
      formulaLibrary: {
        formulas: [
          { id: 1, icon: '⚖️', title: '牛顿运动定律', formula: 'F = ma\n∑F = 0（平衡条件）\nF₁₂ = -F₂₁（作用反作用）' },
          { id: 2, icon: '📏', title: '匀变速直线运动', formula: 'v = v₀ + at\ns = v₀t + ½at²\nv² - v₀² = 2as' },
          { id: 3, icon: '⚡', title: '电场与电场强度', formula: 'E = F/q\nE = kQ/r²\nU = Ed（匀强电场）' },
          { id: 4, icon: '💡', title: '光的直线传播与反射', formula: 'θᵢ = θᵣ（反射定律）\nc = λf\nn = c/v' },
          { id: 5, icon: '🌡️', title: '分子动理论', formula: 'PV = nRT\nKE = 3/2 kT\nvrms = √(3RT/M)' },
          { id: 6, icon: '🔍', title: '受力分析', formula: '∑Fₓ = 0, ∑Fᵧ = 0\n∑τ = 0（转动平衡）\nf ≤ μN（静摩擦）' },
          { id: 7, icon: '🎯', title: '平抛/类平抛运动', formula: 'x = v₀t\ny = ½gt²\nR = v₀√(2h/g)' },
          { id: 8, icon: '🔌', title: '欧姆定律与电路分析', formula: 'V = IR\nP = VI = I²R = V²/R\nR串 = R₁+R₂, 1/R并 = 1/R₁+1/R₂' },
          { id: 9, icon: '🔄', title: '光的折射与全反射', formula: 'n₁sinθ₁ = n₂sinθ₂\nsinθc = n₂/n₁\nD = (n-1)A（薄透镜）' },
          { id: 10, icon: '🔥', title: '热力学第一定律', formula: 'ΔU = Q - W\nQ = mcΔT\nPV/T = constant' }
        ],
        isPaused: false,
        autoRotateSpeed: 20
      },
      // 草稿纸与计算器
      notebook: {
        calculatorValue: '',
        textContent: '',
        editingRecordId: null
      },
      // 物理定律每日一悟
      physicsLaws: {
        laws: [
          { id: 1, law: '热力学第二定律', quote: '知识是负熵，你今天的学习，正在对抗全世界的混乱。<br><br>（熵增原理：孤立系统的熵总是趋向于增加，学习是引入负熵的过程）', icon: '🔥', category: '热学' },
          { id: 2, law: '牛顿第一定律', quote: '惯性是思维的敌人，持续学习才能打破认知的惰性。<br><br>（惯性定律：物体保持静止或匀速直线运动状态，需要外力才能改变）', icon: '⚡', category: '力学' },
          { id: 3, law: '能量守恒定律', quote: '你投入的每一份努力，都不会凭空消失，它会转化为你成长的能量。<br><br>（能量守恒定律：能量既不会凭空产生，也不会凭空消失，只能从一种形式转化为另一种形式）', icon: '💫', category: '能量学' },
          { id: 4, law: '万有引力定律', quote: '知识的引力与质量成正比，你的积累越深厚，越能吸引更多智慧。<br><br>（万有引力定律：两个物体之间的引力与它们的质量乘积成正比，与距离平方成反比）', icon: '🌍', category: '力学' },
          { id: 5, law: '库仑定律', quote: '思想的火花如同电荷，志同道合的人总会相互吸引。<br><br>（库仑定律：真空中两个点电荷之间的作用力与电荷量的乘积成正比，与距离平方成反比）', icon: '⚡', category: '电学' },
          { id: 6, law: '欧姆定律', quote: '学习的阻力与努力成正比，克服阻力才能获得成长的电流。<br><br>（欧姆定律：导体中的电流与电压成正比，与电阻成反比）', icon: '🔌', category: '电学' },
          { id: 7, law: '光的反射定律', quote: '你的态度如同镜面，你如何对待学习，学习就如何回报你。<br><br>（反射定律：入射角等于反射角，光线在界面上的反射遵循这一规律）', icon: '🔦', category: '光学' },
          { id: 8, law: '光的折射定律', quote: '当你改变学习的角度，会发现知识的世界更加丰富多彩。<br><br>（折射定律：斯涅尔定律，描述光线从一种介质进入另一种介质时的偏折规律）', icon: '🌈', category: '光学' },
          { id: 9, law: '牛顿第二定律', quote: '学习的加速度与你的努力成正比，与困难的质量成反比。<br><br>（F=ma：物体的加速度与所受合外力成正比，与质量成反比）', icon: '🚀', category: '力学' },
          { id: 10, law: '牛顿第三定律', quote: '你对知识的付出，终将得到知识对你的回报。<br><br>（作用力与反作用力：两个物体之间的作用力和反作用力大小相等、方向相反）', icon: '⚖️', category: '力学' },
          { id: 11, law: '动量守恒定律', quote: '学习的动力一旦形成，就会持续推动你前进。<br><br>（动量守恒：在没有外力作用的情况下，系统的总动量保持不变）', icon: '⚡', category: '力学' },
          { id: 12, law: '角动量守恒定律', quote: '保持学习的方向，即使遇到干扰也能稳定前行。<br><br>（角动量守恒：在没有外力矩作用的情况下，物体的角动量保持不变）', icon: '🌀', category: '力学' },
          { id: 13, law: '热力学第一定律', quote: '学习没有捷径，你必须投入足够的能量才能有所收获。<br><br>（能量守恒定律在热力学中的表述：内能的变化等于热量与做功之和）', icon: '⚡', category: '热学' },
          { id: 14, law: '理想气体状态方程', quote: '知识的压力与温度成正比，适当的压力能激发学习的热情。<br><br>（PV=nRT：理想气体的压强、体积和温度之间的关系）', icon: '💨', category: '热学' },
          { id: 15, law: '法拉第电磁感应定律', quote: '知识的磁场会感应出智慧的电流，只要你愿意靠近。<br><br>（电磁感应：变化的磁场会在导体中产生感应电动势和感应电流）', icon: '🧲', category: '电磁学' },
          { id: 16, law: '安培定律', quote: '学习的电流会在你的思维中产生智慧的磁场。<br><br>（安培定律：电流会在其周围产生磁场，磁场方向遵循右手螺旋定则）', icon: '⚡', category: '电磁学' },
          { id: 17, law: '电磁波方程', quote: '思想如同电磁波，能够超越时空的限制传播智慧。<br><br>（电磁波：变化的电场和磁场相互激发，形成可以在真空中传播的电磁波）', icon: '📡', category: '电磁学' },
          { id: 18, law: '薛定谔方程', quote: '你的学习状态处于多种可能性的叠加态，只有行动才能使它坍缩为现实。<br><br>（薛定谔方程：描述量子系统状态随时间演化的基本方程，波函数描述粒子的概率分布）', icon: '🔬', category: '量子力学' },
          { id: 19, law: '海森堡不确定性原理', quote: '不要试图同时掌握所有知识，专注于当下才能取得真正的进步。<br><br>（不确定性原理：粒子的位置和动量不能同时被精确测量，测量精度存在根本限制）', icon: '🔍', category: '量子力学' },
          { id: 20, law: '泡利不相容原理', quote: '每个人的学习路径都是独特的，找到属于自己的轨道才能发光发热。<br><br>（泡利不相容原理：两个费米子不能同时占据相同的量子态）', icon: '⭐', category: '量子力学' },
          { id: 21, law: '光电效应定律', quote: '知识的光芒能够激发你内在的潜能，如同光照激发电子一样。<br><br>（光电效应：光照射金属表面时，当光子能量大于金属逸出功时，会释放电子）', icon: '💡', category: '量子力学' },
          { id: 22, law: '德布罗意波粒二象性', quote: '学习既是一种积累（粒子性），也是一种感悟（波动性）。<br><br>（波粒二象性：微观粒子既具有粒子性，又具有波动性，德布罗意波长λ=h/p）', icon: '🌊', category: '量子力学' },
          { id: 23, law: '相对论质量能量等价原理', quote: '知识的质量蕴含着巨大的能量，学习能够释放出无限的潜能。<br><br>（E=mc²：质量和能量是等价的，质量可以转化为能量，能量也可以转化为质量）', icon: '⚛️', category: '相对论' },
          { id: 24, law: '时间 dilation效应', quote: '专注学习时，时间会变慢，因为你的每一刻都充满了价值。<br><br>（时间膨胀：在高速运动的参考系中，时间流逝速度比静止参考系慢）', icon: '⏰', category: '相对论' },
          { id: 25, law: '长度收缩效应', quote: '当你朝着目标高速前进时，困难的距离会显得缩短。<br><br>（长度收缩：在高速运动的参考系中，沿运动方向的长度会缩短）', icon: '📏', category: '相对论' },
          { id: 26, law: '多普勒效应', quote: '当你靠近知识的源头时，智慧的频率会变得更高。<br><br>（多普勒效应：波源和观察者相对运动时，观察到的频率会发生变化）', icon: '🔊', category: '波动学' },
          { id: 27, law: '波的干涉原理', quote: '不同的知识之间会产生干涉，有时会加强，有时会抵消，关键在于如何整合。<br><br>（波的干涉：两列波相遇时，某些区域振动加强，某些区域振动减弱）', icon: '🌊', category: '波动学' },
          { id: 28, law: '波的衍射原理', quote: '知识能够绕过障碍传播，只要你有足够的耐心和智慧。<br><br>（波的衍射：波遇到障碍物时，能够绕过障碍物继续传播）', icon: '🔍', category: '波动学' }
        ],
        searchQuery: '',
        favorites: [],
        showFavoritesModal: false,
        searchTerm: ''
      },
      // 草稿记录
      notebookRecords: {
        records: [],
        recordView: null,
        editRecord: null,
        editContent: ''
      },
      // 日历数据
      calendarDates: []
    }
  },
  computed: {
    currentMonthText() {
      const year = this.currentCheckinMonth.getFullYear()
      const month = this.currentCheckinMonth.getMonth() + 1
      return `${year}年${month}月`
    },
    countdownDisplayItems() {
      this.currentTime
      const now = new Date()
      const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
      const goals = this.countdownGoals || []
      const future = goals.filter(g => g.goalDate >= today).sort((a, b) => a.goalDate.localeCompare(b.goalDate))
      return future.map(g => {
        const end = new Date(g.goalDate + 'T23:59:59')
        let ms = end - now
        const days = ms > 0 ? Math.ceil(ms / 86400000) : 0
        const idx = goals.findIndex(x => x.id === g.id) + 1
        return { id: g.id, text: `目标${idx}：距离${g.goalName}还有第${days}天` }
      })
    }
  },
  mounted() {
    this.init()
    this.updateTime()
    setInterval(this.updateTime, 1000)
    // 消息功能已禁用，不再轮询
  },
  methods: {
    openChatRoom() {
      this.showChatRoom = true
    },
    async init() {
      this.getCurrentUser()
      const silent = { skipGlobalError: true }
      this.getSeats(silent)
      await this.getOnlineUsers(silent)
      this.getTodos(silent)
      // this.getRoomMessages(silent) // 消息功能已禁用
      this.loadCountdownGoals(silent)
    },
    getCurrentUser() {
      const userInfoStr = window.localStorage.getItem('user_info')
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          if (userInfo) {
            this.currentUserId = userInfo.id
          }
        } catch (error) {
          console.error('解析用户信息失败:', error)
        }
      }
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', { hour12: false })
    },
    refreshPhysicsQuote() {
      const quotes = [
        '知识是负熵，你今天的学习，正在对抗全世界的混乱。',
        '物理定律是大自然的语言，学习它们就是与自然对话。',
        '每一个公式背后，都隐藏着宇宙的奥秘。',
        '学习是一场马拉松，不是短跑。坚持就是胜利。',
        '好奇心是科学的驱动力，保持好奇，不断探索。',
        '理论与实践相结合，才能真正理解物理的精髓。',
        '错误是学习的机会，每一次失败都是进步的阶梯。',
        '物理教会我们用理性思维看待世界，用数学语言描述自然。'
      ]
      const randomIndex = Math.floor(Math.random() * quotes.length)
      this.physicsQuote = quotes[randomIndex]
    },
    playNoise(type) {
      // 停止当前播放的音频
      if (this.currentNoise) {
        this.currentNoise.pause()
        this.currentNoise = null
      }
      
      // 移除所有按钮的 active 类
      document.querySelectorAll('.noise-btn').forEach(btn => {
        btn.classList.remove('active')
      })
      
      // 给当前按钮添加 active 类
      event.target.classList.add('active')
      
      // 根据类型设置音频路径
      let audioSrc = ''
      if (type === 'summer') {
        audioSrc = '/bgm/夏夜.mp3'
      } else if (type === 'beach') {
        audioSrc = '/bgm/海边.mp3'
      } else if (type === 'forest') {
        audioSrc = '/bgm/森林.mp3'
      } else if (type === 'water') {
        audioSrc = '/bgm/水滴.mp3'
      }
      
      // 创建并播放音频
      this.currentNoise = new Audio(audioSrc)
      this.currentNoise.loop = true
      this.currentNoise.volume = this.volume / 100
      this.currentNoise.play().catch(e => {
        console.error('播放错误:', e)
      })
    },
    stopNoise() {
      // 停止白噪音
      if (this.currentNoise) {
        this.currentNoise.pause()
        this.currentNoise = null
      }
      
      // 移除所有按钮的 active 类
      document.querySelectorAll('.noise-btn').forEach(btn => {
        btn.classList.remove('active')
      })
    },
    setVolume() {
      // 设置音量
      if (this.currentNoise) {
        this.currentNoise.volume = this.volume / 100
      }
    },
    changeBackground(type) {
      // 切换背景逻辑
      const backgrounds = {
        starry: '/wp/梦幻星空.mp4',
        forest: '/wp/林间晨光.mp4',
        beach: '/wp/天际倒影.mp4'
      }
      this.bgVideo = backgrounds[type]
      console.log('切换背景:', type, '->', this.bgVideo)
    },
    updateTimerFromInput() {
      this.timerMinutes = parseInt(this.timerMinutes) || 1
      this.timerSeconds = this.timerMinutes * 60
      this.updateTimerDisplay()
    },
    updateTimerDisplay() {
      const minutes = Math.floor(this.timerSeconds / 60)
      const seconds = this.timerSeconds % 60
      this.timerDisplay = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    },
    startTimer() {
      if (!this.timerRunning) {
        this.timerRunning = true
        this.timerInterval = setInterval(() => {
          if (this.timerSeconds > 0) {
            this.timerSeconds--
            this.updateTimerDisplay()
          } else {
            this.pauseTimer()
            this.$message.success('学习时间结束！')
          }
        }, 1000)
      }
    },
    pauseTimer() {
      this.timerRunning = false
      if (this.timerInterval) {
        clearInterval(this.timerInterval)
        this.timerInterval = null
      }
    },
    resetTimer() {
      this.pauseTimer()
      this.timerSeconds = this.timerMinutes * 60
      this.updateTimerDisplay()
    },
    async getSeats(opts) {
      try {
        const res = await get('/studyRoom/seats', null, opts)
        if (res.success) {
          this.seats = res.data
          const mySeat = this.seats.find(seat => seat.status === 1 && seat.userId === this.currentUserId)
          if (mySeat && mySeat.goal) {
            this.studyGoal = mySeat.goal
          }
        }
      } catch (error) {
        console.error('获取座位信息失败:', error)
      }
    },
    async getOnlineUsers(opts) {
      try {
        const res = await get('/studyRoom/onlineUsers', null, opts)
        if (res.success) {
          this.onlineUsers = res.data
        }
      } catch (error) {
        console.error('获取在线用户失败:', error)
      }
    },
    async selectSeat(seat) {
      // 获取当前用户信息
      const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
      const currentUserId = userInfo.id
      
      if (seat.status === 1 && seat.userId !== currentUserId) {
        this.$message.warning('座位已被占用')
        return
      }
      
      this.selectedSeat = seat
      this.modalTitle = '选座确认'
      this.modalContent = `确定要选择座位 ${seat.seatNumber} 吗？`
      this.modalType = 'seat'
      this.studyGoal = seat.goal || ''
      this.showModal = true
    },
    async confirmSelectSeat() {
      try {
        // 获取当前用户信息
        const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
        const currentUserId = userInfo.id
        const currentUserName = userInfo.userName || userInfo.loginAccount || '未知用户'
        
        if (!currentUserId) {
          this.$message.error('请先登录')
          return
        }
        
        // 前端即时更新座位状态
        const updatedSeats = this.seats.map(seat => {
          // 释放当前用户的旧座位
          if (seat.status === 1 && seat.userId === currentUserId) {
            return {
              ...seat,
              status: 0,
              userId: null,
              nickname: null,
              goal: null
            }
          }
          // 占用新座位
          if (seat.seatId === this.selectedSeat.seatId) {
            return {
              ...seat,
              status: 1,
              userId: currentUserId,
              nickname: currentUserName,
              goal: this.studyGoal
            }
          }
          return seat
        })
        
        // 替换整个seats数组，确保Vue能检测到变化
        this.seats = [...updatedSeats]
        
        // 发送请求到后端
        const res = await post('/studyRoom/seat/select', {
          seatId: this.selectedSeat.seatId,
          goal: this.studyGoal,
          nickname: currentUserName
        })
        if (res.success) {
          this.$message.success('选座成功')
          this.getOnlineUsers()
        } else {
          this.$message.error(res.message || '选座失败')
          // 失败时恢复座位状态
          this.getSeats()
        }
      } catch (error) {
        console.error('选座失败:', error)
        this.$message.error('选座失败')
        // 失败时恢复座位状态
        this.getSeats()
      }
    },
    async randomSelectSeat() {
      const availableSeats = this.seats.filter(seat => seat.status === 0)
      if (availableSeats.length === 0) {
        this.$message.warning('暂无可用座位')
        return
      }
      
      const randomSeat = availableSeats[Math.floor(Math.random() * availableSeats.length)]
      this.selectSeat(randomSeat)
    },
    async getTodos(opts) {
      try {
        const res = await get('/studyRoom/todos', null, opts)
        if (res.success) {
          this.todos = res.data
        }
      } catch (error) {
        console.error('获取待办事项失败:', error)
      }
    },
    async addTodo() {
      if (!this.newTodo.trim()) return
      
      try {
        const res = await post('/studyRoom/todo/add', {
          content: this.newTodo
        })
        if (res.success) {
          this.$message.success('添加成功')
          this.newTodo = ''
          this.getTodos()
        }
      } catch (error) {
        console.error('添加待办事项失败:', error)
      }
    },
    async toggleTodo(id) {
      try {
        const res = await post(`/studyRoom/todo/complete/${id}`)
        if (res.success) {
          this.getTodos()
        }
      } catch (error) {
        console.error('更新待办事项失败:', error)
      }
    },
    async deleteTodo(id) {
      if (!id) return
      const prev = [...this.todos]
      this.todos = this.todos.filter(t => t.id !== id)
      try {
        const res = await request.delete(`/studyRoom/todo/${id}`)
        if (!res.success) {
          this.todos = prev
          this.$message.error(res.message || '删除失败')
        } else {
          this.$message.success('删除成功')
        }
      } catch (error) {
        this.todos = prev
        this.$message.error('删除失败')
      }
    },
    async sendRoomMessage() {
      // 功能已禁用
      this.$message.warning('消息功能暂未开放')
    },
    async getRoomMessages(opts) {
      // 功能已禁用，返回空数据
      this.messages = []
    },
    async getMessages(deskmateId) {
      // 功能已禁用，返回空数据
      this.messages = []
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    async showCheckinModal() {
      this.showCheckinModalFlag = true
      await this.loadCheckinData()
    },
    closeCheckinModal() {
      this.showCheckinModalFlag = false
    },
    formatDateYMD(d) {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    async checkinToday() {
      const today = this.formatDateYMD(new Date())
      if (this.checkinData[today]) {
        this.$message.warning('今日已签到！')
        return
      }
      try {
        const res = await post('/studyRoom/checkin', { date: today })
        if (res.success) {
          this.checkinData[today] = true
          this.todayChecked = true
          this.calculateStreakDays()
          this.generateCalendarDates()
          this.$message.success('签到成功！')
        } else {
          this.$message.error(res.message || '签到失败')
        }
      } catch (e) {
        this.$message.error('签到失败')
      }
    },
    async loadCheckinData() {
      try {
        const res = await get('/studyRoom/checkins', null, { skipGlobalError: true })
        if (res.success && Array.isArray(res.data)) {
          this.checkinData = {}
          res.data.forEach(d => { this.checkinData[d] = true })
        } else {
          this.checkinData = {}
        }
      } catch (e) {
        this.checkinData = {}
      }
      const today = this.formatDateYMD(new Date())
      this.todayChecked = !!this.checkinData[today]
      this.calculateStreakDays()
      this.generateCalendarDates()
    },
    calculateStreakDays() {
      let streak = 0
      const today = new Date()
      if (this.checkinData[this.formatDateYMD(today)]) {
        streak = 1
      }
      for (let i = 1; i <= 365; i++) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        if (this.checkinData[this.formatDateYMD(date)]) {
          streak++
        } else {
          break
        }
      }
      this.streakDays = streak
    },
    changeMonth(direction) {
      const newMonth = new Date(this.currentCheckinMonth)
      newMonth.setMonth(newMonth.getMonth() + direction)
      this.currentCheckinMonth = newMonth
      this.generateCalendarDates()
    },
    generateCalendarDates() {
      const year = this.currentCheckinMonth.getFullYear()
      const month = this.currentCheckinMonth.getMonth()
      
      const firstDay = new Date(year, month, 1)
      const startDate = new Date(firstDay)
      startDate.setDate(startDate.getDate() - firstDay.getDay())
      
      const todayStr = this.formatDateYMD(new Date())
      const dates = []
      for (let i = 0; i < 42; i++) {
        const date = new Date(startDate)
        date.setDate(startDate.getDate() + i)
        const dateStr = this.formatDateYMD(date)
        dates.push({
          day: date.getDate(),
          checked: !!this.checkinData[dateStr],
          isToday: dateStr === todayStr,
          otherMonth: date.getMonth() !== month
        })
      }
      this.calendarDates = dates
    },
    async showGoalCountdown() {
      this.modalTitle = '🎯 设置目标倒计日'
      this.modalContent = ''
      this.modalType = 'goal'
      this.showModal = true
      await this.loadCountdownGoals()
    },
    exitStudyRoom() {
      this.$router.push('/')
    },
    closeModal() {
      this.showModal = false
      this.selectedSeat = null
    },
    async loadCountdownGoals(opts = {}) {
      try {
        const res = await get('/studyRoom/countdownGoals', null, opts)
        if (res.success && Array.isArray(res.data)) {
          this.countdownGoals = res.data
        } else {
          this.countdownGoals = []
        }
      } catch (e) {
        this.countdownGoals = []
      }
    },
    async deleteCountdownGoal(id) {
      try {
        const res = await request.delete(`/studyRoom/countdownGoal/${id}`)
        if (res.success) {
          this.countdownGoals = this.countdownGoals.filter(g => g.id !== id)
          this.$message.success('已删除')
        }
      } catch (e) {
        this.$message.error('删除失败')
      }
    },
    async confirmModal() {
      if (this.modalType === 'seat') {
        await this.confirmSelectSeat()
      } else if (this.modalType === 'checkin') {
        this.$message.success('签到成功')
      } else if (this.modalType === 'goal') {
        if (!this.goalName || !this.goalName.trim()) {
          this.$message.warning('请输入目标名称')
          return
        }
        if (!this.goalDate) {
          this.$message.warning('请选择目标日期')
          return
        }
        if (this.countdownGoals.length >= 3) {
          MessageBox.alert('最多设置三个目标', '提示', { confirmButtonText: '确定' })
          return
        }
        try {
          const res = await post('/studyRoom/countdownGoal', { goalName: this.goalName.trim(), goalDate: this.goalDate })
          if (res.success) {
            this.countdownGoals.push(res.data)
            this.goalName = ''
            this.goalDate = ''
            this.$message.success('已添加')
          } else {
            this.$message.error(res.message || '添加失败')
          }
        } catch (e) {
          this.$message.error('添加失败')
        }
        return
      }
      this.closeModal()
    },
    showFormulaLibrary() {
      this.currentPage = 'formulaLibrary'
    },
    pauseFormulaLibraryRotation() {
      this.formulaLibrary.isPaused = !this.formulaLibrary.isPaused
    },
    showNotebook() {
      this.currentPage = 'notebook'
    },
    confirmClearNotebook() {
      this.$confirm('确定要清空草稿纸吗？', '清空确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.notebook.textContent = ''
        this.notebook.editingRecordId = null
        this.$message.success('已清空')
      }).catch(() => {})
    },
    appendValue(value) {
      this.notebook.calculatorValue += value
    },
    appendOperator(operator) {
      this.notebook.calculatorValue += operator
    },
    appendFunction(func) {
      this.notebook.calculatorValue += func
    },
    appendDegree() {
      this.notebook.calculatorValue += '°'
    },
    appendSquareRoot() {
      this.notebook.calculatorValue += '√('
    },
    appendSquare() {
      if (this.notebook.calculatorValue === '') {
        this.notebook.calculatorValue = 'x²'
      } else {
        this.notebook.calculatorValue = '(' + this.notebook.calculatorValue + ')²'
      }
    },
    appendCube() {
      if (this.notebook.calculatorValue === '') {
        this.notebook.calculatorValue = 'x³'
      } else {
        this.notebook.calculatorValue = '(' + this.notebook.calculatorValue + ')³'
      }
    },
    appendPi() {
      this.notebook.calculatorValue += 'π'
    },
    deleteLast() {
      this.notebook.calculatorValue = this.notebook.calculatorValue.slice(0, -1)
    },
    clearDisplay() {
      this.notebook.calculatorValue = ''
    },
    calculate() {
      const input = this.notebook.calculatorValue
      try {
        let expression = input
        
        // 替换度数为弧度
        expression = expression.replace(/(\d+)°/g, function(match, degrees) {
          return '(' + degrees + ' * Math.PI / 180)'
        })
        
        // 处理三角函数
        expression = expression.replace(/sin(\d+|\(.+?\))/g, 'Math.sin($1)')
        expression = expression.replace(/cos(\d+|\(.+?\))/g, 'Math.cos($1)')
        expression = expression.replace(/tan(\d+|\(.+?\))/g, 'Math.tan($1)')
        
        // 处理对数函数
        expression = expression.replace(/log(\d+|\(.+?\))/g, 'Math.log10($1)')
        expression = expression.replace(/ln(\d+|\(.+?\))/g, 'Math.log($1)')
        
        // 替换平方根符号
        expression = expression.replace(/√\(/g, 'Math.sqrt(')
        
        // 替换平方符号
        expression = expression.replace(/\((.+?)\)²/g, 'Math.pow($1, 2)')
        
        // 替换立方符号
        expression = expression.replace(/\((.+?)\)³/g, 'Math.pow($1, 3)')
        
        // 替换π
        expression = expression.replace(/π/g, 'Math.PI')
        
        // 替换幂运算符
        expression = expression.replace(/\^/g, '**')
        
        // 替换运算符符号
        expression = expression.replace(/×/g, '*').replace(/÷/g, '/')
        
        const result = eval(expression)
        
        // 处理特殊值
        if (result === Infinity || result === -Infinity) {
          this.notebook.calculatorValue = '无穷大'
        } else if (isNaN(result)) {
          this.notebook.calculatorValue = '错误'
        } else {
          // 保留适当的小数位数
          const roundedResult = Math.round(result * 1000000000) / 1000000000
          this.notebook.calculatorValue = roundedResult.toString()
        }
      } catch (error) {
        this.notebook.calculatorValue = '错误'
      }
    },
    showPhysicsLaws() {
      this.currentPage = 'physicsLaws'
      this.loadLawFavors()
    },
    async loadLawFavors() {
      try {
        const res = await get('/studyRoom/lawFavors')
        if (res.success && Array.isArray(res.data)) {
          this.physicsLaws.favorites = res.data.map(f => f.lawName)
        }
      } catch (e) {
        console.error('加载收藏失败:', e)
      }
    },
    async toggleFavorite(law) {
      const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
      if (!userInfo.id && !userInfo.userId) {
        this.$message.warning('请先登录')
        return
      }
      try {
        const res = await post('/studyRoom/lawFavor/toggle', {
          lawName: law.law,
          lawContent: law.quote,
          category: law.category
        })
        if (res.success) {
          const idx = this.physicsLaws.favorites.indexOf(law.law)
          if (idx > -1) {
            this.physicsLaws.favorites.splice(idx, 1)
          } else {
            this.physicsLaws.favorites.push(law.law)
          }
        } else {
          this.$message.error(res.message || '操作失败')
        }
      } catch (e) {
        console.error('收藏操作失败:', e)
        this.$message.error('操作失败')
      }
    },
    isFavorite(law) {
      return this.physicsLaws.favorites.includes(law.law)
    },
    getFilteredLaws() {
      if (!this.physicsLaws.searchTerm) {
        return this.physicsLaws.laws
      }
      const query = this.physicsLaws.searchTerm.toLowerCase()
      return this.physicsLaws.laws.filter(law => 
        law.law.toLowerCase().includes(query) || 
        law.quote.toLowerCase().includes(query) ||
        law.category.toLowerCase().includes(query)
      )
    },
    getFavoriteLaws() {
      return this.physicsLaws.laws.filter(law => this.physicsLaws.favorites.includes(law.law))
    },
    performSearch() {
      this.physicsLaws.searchTerm = this.physicsLaws.searchQuery.trim()
    },
    clearSearch() {
      this.physicsLaws.searchQuery = ''
      this.physicsLaws.searchTerm = ''
    },
    highlightText(text, searchTerm) {
      if (!text || !searchTerm) return (text || '')
      const escaped = searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
      const regex = new RegExp(`(${escaped})`, 'gi')
      return String(text).replace(regex, '<span class="physics-law-highlight">$1</span>')
    },
    showFavorites() {
      this.physicsLaws.showFavoritesModal = true
    },
    closeFavorites() {
      this.physicsLaws.showFavoritesModal = false
    },
    showNotebookRecords() {
      this.currentPage = 'notebookRecords'
      this.loadNotebookRecords()
    },
    async loadNotebookRecords() {
      try {
        const res = await get('/studyRoom/notebooks')
        if (res.success && Array.isArray(res.data)) {
          this.notebookRecords.records = res.data
        } else {
          this.notebookRecords.records = []
        }
      } catch (e) {
        this.notebookRecords.records = []
      }
    },
    async saveNotebookRecord() {
      const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
      if (!userInfo.id && !userInfo.userId) {
        this.$message.warning('请先登录')
        return
      }
      const text = (this.notebook.textContent || '').trim()
      if (!text) {
        this.$message.warning('请先在草稿纸上写下内容再保存！')
        return
      }
      const content = JSON.stringify({ text })
      let payload
      if (this.notebook.editingRecordId) {
        const rec = this.notebookRecords.records.find(r => r.id === this.notebook.editingRecordId)
        payload = {
          id: rec.id,
          userId: rec.userId,
          title: rec.title || '草稿',
          content,
          createTime: rec.createTime
        }
      } else {
        payload = {
          title: `草稿 ${this.notebookRecords.records.length + 1}`,
          content
        }
      }
      try {
        const res = await post('/studyRoom/notebook/save', payload)
        if (res.success) {
          this.notebook.textContent = ''
          this.notebook.editingRecordId = null
          this.loadNotebookRecords()
          await MessageBox.alert('草稿已保存！', '保存成功', { customClass: 'notebook-rename-messagebox' })
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      }
    },
    async deleteNotebookRecord(recordId) {
      try {
        await MessageBox.confirm('确定要删除这条草稿吗？删除后无法恢复。', '删除确认', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          customClass: 'notebook-rename-messagebox'
        })
        const res = await request.delete(`/studyRoom/notebook/${recordId}`)
        if (res.success) {
          this.notebookRecords.records = this.notebookRecords.records.filter(r => r.id !== recordId)
          this.$message.success('草稿已删除')
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败')
      }
    },
    editNotebookRecord(record) {
      this.notebookRecords.editRecord = record
      this.notebookRecords.editContent = this.parseRecordContent(record.content).text || ''
    },
    closeEditNotebookRecord() {
      this.notebookRecords.editRecord = null
      this.notebookRecords.editContent = ''
    },
    async saveEditNotebookRecord() {
      const rec = this.notebookRecords.editRecord
      if (!rec) return
      const content = JSON.stringify({ text: (this.notebookRecords.editContent || '').trim() })
      try {
        const res = await post('/studyRoom/notebook/save', {
          id: rec.id,
          userId: rec.userId,
          title: rec.title || '草稿',
          content,
          createTime: rec.createTime
        })
        if (res.success) {
          this.$set(rec, 'content', content)
          await this.loadNotebookRecords()
          this.closeEditNotebookRecord()
          await MessageBox.alert('修改已保存', '保存成功', { customClass: 'notebook-rename-messagebox' })
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      }
    },
    async renameNotebookRecord(record) {
      try {
        const { value } = await MessageBox.prompt('请输入新的草稿标题:', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputValue: record.title || '',
          inputPlaceholder: '草稿标题',
          customClass: 'notebook-rename-messagebox'
        })
        if (!value || !value.trim()) {
          this.$message.warning('标题不能为空')
          return
        }
        const res = await post('/studyRoom/notebook/save', {
          id: record.id,
          userId: record.userId,
          title: value.trim(),
          content: record.content,
          createTime: record.createTime
        })
        if (res.success) {
          this.$set(record, 'title', value.trim())
          await this.loadNotebookRecords()
          this.$message.success('重命名成功')
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('重命名失败')
      }
    },
    viewNotebookRecord(record) {
      this.notebookRecords.recordView = record
    },
    closeRecordView() {
      this.notebookRecords.recordView = null
    },
    parseRecordContent(content) {
      if (!content) return { text: '', canvas: '' }
      try {
        const parsed = JSON.parse(content)
        return { text: parsed.text || '', canvas: parsed.canvas || '' }
      } catch {
        return { text: '', canvas: typeof content === 'string' && content.startsWith('data:') ? content : '' }
      }
    },
    getRecordSnippet(record) {
      const data = this.parseRecordContent(record.content)
      if (data.text) return data.text.replace(/\s+/g, ' ').slice(0, 50) + (data.text.length > 50 ? '...' : '')
      if (data.canvas) return '[涂鸦]'
      return '无内容'
    },
    getRecordText(record) {
      return this.parseRecordContent(record.content).text
    },
    getRecordCanvas(record) {
      return this.parseRecordContent(record.content).canvas
    },
    formatRecordTime(t) {
      if (!t) return ''
      const d = new Date(t)
      return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    openSeatSystemModal() {
      this.showSeatSystemModal = true
      this.initSeatSystem()
    },
    closeSeatSystemModal() {
      this.showSeatSystemModal = false
    },
    async initSeatSystem() {
      const STORAGE_CHATS = 'study_room_chats'
      const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
      const raw = userInfo.id || userInfo.userId
      const currentUserId = raw ? String(raw) : ''
      this.seatSystem.currentUser = currentUserId || ('用户' + Math.floor(Math.random() * 1000))
      this.seatSystem.currentUserName = userInfo.userName || userInfo.loginAccount || ''

      const ROWS = ['A','B','C','D','E','F']
      const COLS = [1,2,3,4,5,6,7]
      const seats = {}
      for (let r of ROWS) {
        for (let c of COLS) {
          seats[r + c] = { occupied: false, user: null, goal: '' }
        }
      }

      try {
        const res = await get('/studyRoom/seats')
        if (res.success && Array.isArray(res.data)) {
          for (const s of res.data) {
            const id = s.seatId
            if (id && seats[id] !== undefined) {
              seats[id] = {
                occupied: s.status === 1,
                user: s.userId ? String(s.userId) : null,
                goal: s.goal || '',
                nickname: s.nickname || ''
              }
              if (s.status === 1 && String(s.userId) === currentUserId) {
                this.mySeatIdInModal = id
              }
            }
          }
        }
      } catch (e) {
        console.error('获取座位失败:', e)
      }
      if (!this.mySeatIdInModal) {
        this.mySeatIdInModal = ''
      }
      if (this.mySeatIdInModal && seats[this.mySeatIdInModal]?.goal) {
        this.seatSystem.goalInput = seats[this.mySeatIdInModal].goal
        this.studyGoal = seats[this.mySeatIdInModal].goal
      }
      this.$set(this.seatSystem, 'seats', seats)
      this.seatSystemUpdateKey += 1

      this.updateDeskmates()
      await this.loadSeatSystemChats()
    },
    async loadSeatSystemChats() {
      // 功能已禁用，返回空数据
      this.seatSystem.chats = {}
    },
    generateEnhancedSeats() {
      const ROWS = ['A','B','C','D','E','F']
      const COLS = [1,2,3,4,5,6,7]
      const seats = {}
      
      // 先全部空闲
      for (let r of ROWS) {
        for (let c of COLS) {
          const id = r + c
          seats[id] = { occupied: false, user: null, goal: "" }
        }
      }
      
      // 固定基础用户
      const BASE_USERS = [
        { name: "张三", seat: "A3", goal: "考研冲刺" },
        { name: "李四", seat: "B5", goal: "写论文" },
        { name: "王五", seat: "D2", goal: "Java学习" },
        { name: "赵六", seat: "F7", goal: "英语四级" }
      ]
      
      BASE_USERS.forEach(u => {
        if (seats[u.seat]) {
          seats[u.seat].occupied = true
          seats[u.seat].user = u.name
          seats[u.seat].goal = u.goal
        }
      })
      
      // 特意增加一组"两座夹一空"：B3也占用
      if (seats['B3'] && !seats['B3'].occupied) {
        seats['B3'].occupied = true
        seats['B3'].user = '钱七'
        seats['B3'].goal = '奋战电磁学'
      }
      
      // 随机生成一些其他占座
      const additionalNames = ['孙八', '周九', '吴十', '郑十一', '王十二', '冯十三', '陈十四', '褚十五', '卫十六', '蒋十七']
      const allSeatIds = []
      for (let r of ROWS) {
        for (let c of COLS) {
          allSeatIds.push(r + c)
        }
      }
      const freeSeats = allSeatIds.filter(id => !seats[id].occupied)
      const shuffle = (arr) => arr.sort(() => 0.5 - Math.random())
      const selected = shuffle([...freeSeats]).slice(0, 10)
      
      selected.forEach((seatId, idx) => {
        if (idx < additionalNames.length) {
          seats[seatId].occupied = true
          seats[seatId].user = additionalNames[idx]
          seats[seatId].goal = ['复习讲义', '做真题', '背单词', '看文献', '刷题库', '写报告', '电磁学', '量子力学', '热统', '数理方法'][idx % 10]
        }
      })
      
      // 确保B4是空闲的
      if (seats['B4'] && seats['B4'].occupied) {
        seats['B4'].occupied = false
        seats['B4'].user = null
        seats['B4'].goal = ''
      }
      
      return seats
    },
    seatClass(row, col) {
      const id = String(row) + col
      if (id === this.mySeatIdInModal) return { 'seat-system-seat-mine': true }
      const seat = this.seatSystem.seats[id]
      if (!seat) return { 'seat-system-seat-free': true }
      return {
        'seat-system-seat-free': !seat.occupied,
        'seat-system-seat-occupied': seat.occupied
      }
    },
    getMySeat() {
      for (let id in this.seatSystem.seats) {
        if (this.seatSystem.seats[id].user === this.seatSystem.currentUser) return id
      }
      return null
    },
    updateDeskmates() {
      const mySeat = this.getMySeat()
      if (!mySeat) {
        this.seatSystem.deskmates = []
        this.seatSystem.deskmateNames = []
        return
      }
      
      const row = mySeat[0]
      const col = parseInt(mySeat.slice(1))
      const possible = []
      if (col > 1) possible.push(row + (col-1))
      if (col < 7) possible.push(row + (col+1))
      
      const deskmates = []
      const deskmateNames = []
      for (let seatId of possible) {
        const seat = this.seatSystem.seats[seatId]
        if (seat && seat.occupied && seat.user !== this.seatSystem.currentUser) {
          deskmates.push(seat.user)
          deskmateNames.push(seat.nickname || seat.user || '同桌')
        }
      }
      this.seatSystem.deskmates = deskmates
      this.seatSystem.deskmateNames = deskmateNames
    },
    onSeatClick(seatId) {
      const seat = this.seatSystem.seats[seatId]
      if (!seat) return
      const myCurrentSeat = this.getMySeat()

      if (seat.occupied && seat.user !== this.seatSystem.currentUser) {
        this.toast(`❌ 已被 ${seat.nickname || seat.user} 占用`)
        return
      }

      const userInfo = JSON.parse(window.localStorage.getItem('user_info') || '{}')
      const currentUserId = String(userInfo.id || userInfo.userId || '')
      if (!currentUserId) {
        this.toast('请先登录')
        return
      }

      const prevMySeatId = this.mySeatIdInModal
      const prevSeats = JSON.parse(JSON.stringify(this.seatSystem.seats))

      const nextSeats = { ...this.seatSystem.seats }
      if (myCurrentSeat && nextSeats[myCurrentSeat]) {
        nextSeats[myCurrentSeat] = { ...nextSeats[myCurrentSeat], occupied: false, user: null, goal: '' }
      }
      const currentUserName = this.seatSystem.currentUserName || ''
      nextSeats[seatId] = {
        ...(nextSeats[seatId] || { occupied: false, user: null, goal: '' }),
        occupied: true,
        user: currentUserId,
        nickname: currentUserName,
        goal: this.seatSystem.goalInput || ''
      }
      this.mySeatIdInModal = seatId
      this.studyGoal = this.seatSystem.goalInput || ''
      this.$set(this.seatSystem, 'seats', nextSeats)
      this.seatSystemUpdateKey += 1
      this.updateDeskmates()

      post('/studyRoom/seat/select', { seatId, goal: this.seatSystem.goalInput || '', nickname: currentUserName })
        .then(res => {
          if (!res.success) {
            this.mySeatIdInModal = prevMySeatId
            this.$set(this.seatSystem, 'seats', prevSeats)
            this.seatSystemUpdateKey += 1
            this.toast(res.message || '选座失败')
            return
          }
          this.toast(`✅ 已选择座位 ${seatId}`)
          localStorage.setItem('study_room_seats', JSON.stringify(this.seatSystem.seats))
          this.getSeats()
          this.getOnlineUsers()
        })
        .catch(err => {
          console.error('智能选座系统选座失败:', err)
          this.mySeatIdInModal = prevMySeatId
          this.$set(this.seatSystem, 'seats', prevSeats)
          this.seatSystemUpdateKey += 1
          this.toast('选座失败，请稍后重试')
        })
    },
    saveGoal() {
      const mySeat = this.getMySeat()
      if (!mySeat) {
        this.toast('请先选择一个座位')
        return
      }
      
      const goal = this.seatSystem.goalInput.trim()
      this.seatSystem.seats[mySeat].goal = goal || ''
      this.studyGoal = goal || ''
      localStorage.setItem('study_room_seats', JSON.stringify(this.seatSystem.seats))
      if (goal) {
        post('/studyRoom/seat/updateGoal', { goal }).then(res => {
          if (res.success) this.toast('目标已保存')
        }).catch(() => {})
      } else {
        this.toast('目标已保存')
      }
    },
    leaveSeat() {
      const mySeat = this.getMySeat()
      if (!mySeat) {
        this.toast('你还没有座位')
        return
      }
      
      this.mySeatIdInModal = ''
      this.seatSystem.seats[mySeat].occupied = false
      this.seatSystem.seats[mySeat].user = null
      this.seatSystem.seats[mySeat].goal = ''
      localStorage.setItem('study_room_seats', JSON.stringify(this.seatSystem.seats))
      this.toast('已离开座位')
      this.updateDeskmates()
    },
    async sendSeatMessage() {
      const text = this.seatSystem.chatInput && this.seatSystem.chatInput.trim()
      if (!text) return
      
      const deskmates = this.seatSystem.deskmates
      if (deskmates.length === 0) {
        this.toast('没有同桌可以聊天')
        return
      }
      
      // 功能已禁用
      this.toast('聊天功能暂未开放')
      this.seatSystem.chatInput = ''
    },
    toast(msg) {
      // 简单的toast提示
      const toast = document.createElement('div')
      toast.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: rgba(10, 17, 40, 0.95);
        color: white;
        padding: 10px 20px;
        border-radius: 20px;
        font-size: 14px;
        z-index: 9999;
        border: 1px solid rgba(79, 195, 247, 0.3);
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
      `
      toast.innerText = msg
      document.body.appendChild(toast)
      
      setTimeout(() => {
        toast.style.opacity = '0'
        toast.style.transition = 'opacity 0.3s'
        setTimeout(() => {
          document.body.removeChild(toast)
        }, 300)
      }, 1500)
    },
    hasSelectedSeat() {
      return this.seats.some(seat => seat.status === 1 && seat.userId === this.currentUserId)
    },
    saveSeatGoal() {
      if (!this.studyGoal.trim()) {
        this.$message.warning('请输入学习目标')
        return
      }
      
      // 找到当前用户的座位
      const mySeat = this.seats.find(seat => seat.status === 1 && seat.userId === this.currentUserId)
      
      if (mySeat) {
        // 更新座位目标
        this.updateSeatGoal(mySeat.seatId, this.studyGoal)
      } else {
        this.$message.info('请先选择座位')
      }
    },
    async updateSeatGoal(seatId, goal) {
      try {
        const res = await post('/studyRoom/seat/updateGoal', {
          seatId: seatId,
          goal: goal
        })
        if (res.success) {
          this.$message.success('目标保存成功')
          this.getSeats()
        } else {
          this.$message.error(res.message || '更新目标失败')
        }
      } catch (error) {
        console.error('更新目标失败:', error)
        this.$message.error('更新目标失败')
      }
    }
  }
}
</script>

<style>
/* 重命名弹窗 - 确保在深色主题下可见，且不被遮挡 */
.notebook-rename-messagebox {
  z-index: 30000 !important;
}
.el-message-box__wrapper {
  z-index: 30000 !important;
}
.notebook-rename-messagebox.el-message-box {
  background: rgba(20, 35, 70, 0.98) !important;
  border: 1px solid rgba(79, 195, 247, 0.5) !important;
}
.notebook-rename-messagebox .el-message-box__title {
  color: #4fc3f7 !important;
  font-size: 18px !important;
}
.notebook-rename-messagebox .el-message-box__content {
  color: rgba(255, 255, 255, 0.95) !important;
}
.notebook-rename-messagebox .el-message-box__message {
  color: rgba(255, 255, 255, 0.95) !important;
}
.notebook-rename-messagebox .el-input__inner {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(79, 195, 247, 0.5) !important;
  color: #fff !important;
}
.notebook-rename-messagebox .el-message-box__btns .el-button--default {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
}
.notebook-rename-messagebox .el-message-box__btns .el-button--primary {
  background: rgba(79, 195, 247, 0.4) !important;
  border-color: rgba(79, 195, 247, 0.6) !important;
  color: #fff !important;
}
.notebook-rename-messagebox .el-message-box__status.el-icon-warning {
  color: #f59e0b !important;
}
</style>

<style scoped>
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: transparent;
    color: #ffffff;
    overflow: hidden;
    height: 100vh;
  }

  .study-room {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: transparent;
    color: #ffffff;
    overflow: hidden;
    height: 100vh;
    display: flex;
    flex-direction: column;
  }

  /* 自定义弹窗样式 */
  .custom-modal {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: 2000;
    justify-content: center;
    align-items: center;
  }

  .custom-modal.show {
    display: flex;
  }

  .custom-modal-content {
    background: rgba(25, 39, 76, 0.9);
    border-radius: 20px;
    padding: 30px;
    max-width: 400px;
    width: 90%;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    animation: modalSlideIn 0.3s ease;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
  }

  @keyframes modalSlideIn {
    from {
      opacity: 0;
      transform: translateY(-30px) scale(0.9);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }

  .custom-modal-header {
    text-align: center;
    margin-bottom: 20px;
  }

  .custom-modal-title {
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 15px;
  }

  .custom-modal-title.goal-title {
    font-size: 24px;
    color: #4fc3f7;
    text-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
  }

  .custom-modal-body {
    text-align: center;
    margin-bottom: 30px;
    color: #cccccc;
    line-height: 1.5;
  }

  .modal-label {
    margin-bottom: 15px;
    color: #ffffff;
    text-align: left;
    font-size: 14px;
  }

  .goal-list {
    max-height: 120px;
    overflow-y: auto;
    margin-bottom: 8px;
    text-align: left;
  }
  .goal-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 12px;
    background: rgba(255,255,255,0.08);
    border-radius: 8px;
    margin-bottom: 6px;
    font-size: 13px;
  }
  .goal-item-tag { color: #4fc3f7; font-size: 12px; margin-right: 8px; }
  .goal-item-name { flex: 1; color: #fff; }
  .goal-item-date { color: #4fc3f7; }
  .goal-item-del {
    padding: 4px 10px;
    font-size: 12px;
    background: rgba(255,82,82,0.3);
    border: 1px solid rgba(255,82,82,0.5);
    color: #ff5252;
    border-radius: 6px;
    cursor: pointer;
  }
  .goal-item-del:hover { background: rgba(255,82,82,0.5); }

  .custom-modal-footer {
    display: flex;
    gap: 10px;
    justify-content: center;
  }

  .custom-modal-btn {
    padding: 10px 20px;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .custom-modal-btn.primary {
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    border: none;
    color: white;
    box-shadow: 0 0 15px rgba(79, 195, 247, 0.5);
  }

  .custom-modal-btn.primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 0 20px rgba(79, 195, 247, 0.7);
  }

  .custom-modal-btn.secondary {
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    backdrop-filter: blur(5px);
    border: 1px solid rgba(79, 195, 247, 0.4);
  }

  .custom-modal-btn.secondary:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  /* 输入框样式 */
  .custom-modal-input {
    width: 100%;
    padding: 12px;
    background-color: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    color: #ffffff;
    font-size: 16px;
    margin-bottom: 20px;
    outline: none;
    transition: border-color 0.3s ease;
  }

  .custom-modal-input:focus {
    border-color: #4fc3f7;
  }

  .custom-modal .goal-date-picker .el-input__inner {
    background-color: rgba(255, 255, 255, 0.15) !important;
    border: 1px solid rgba(79, 195, 247, 0.4) !important;
    border-radius: 10px;
    color: #ffffff !important;
    padding: 12px;
    font-size: 16px;
  }
  .custom-modal .goal-date-picker .el-input__inner::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }
  .custom-modal .goal-date-picker .el-input__prefix,
  .custom-modal .goal-date-picker .el-input__suffix {
    color: rgba(255, 255, 255, 0.8);
  }

  /* 签到弹窗样式 */
  .checkin-modal-content {
    max-width: 320px !important;
    padding: 20px !important;
  }

  .checkin-title {
    font-size: 20px !important;
    margin-bottom: 10px !important;
  }

  .checkin-body {
    margin-bottom: 15px !important;
  }

  .checkin-streak {
    text-align: center;
    margin-bottom: 15px;
  }

  .streak-label {
    font-size: 12px;
    color: #cccccc;
    margin-bottom: 3px;
  }

  .streak-days {
    font-size: 28px;
    font-weight: bold;
    color: #4fc3f7;
  }

  .streak-days span {
    color: #4fc3f7;
  }

  .today-checked {
    font-size: 10px;
    color: #999;
    margin-top: 3px;
  }

  .checkin-btn-wrapper {
    text-align: center;
    margin-bottom: 15px;
  }

  .checkin-btn {
    padding: 8px 20px !important;
    font-size: 14px !important;
  }

  .checkin-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .calendar-container {
    margin-bottom: 15px;
  }

  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .calendar-nav-btn {
    padding: 3px 8px;
    font-size: 12px;
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .calendar-nav-btn:hover {
    background: rgba(255, 255, 255, 0.2);
  }

  .current-month {
    font-size: 14px;
    font-weight: bold;
    color: #ffffff;
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 3px;
    text-align: center;
  }

  .calendar-weekday {
    font-size: 12px;
    color: #4fc3f7;
    padding: 5px 0;
  }

  .calendar-day {
    font-size: 12px;
    padding: 5px 0;
    border-radius: 50%;
    color: #ffffff;
    cursor: default;
  }

  .calendar-day.checked {
    background: #4caf50;
    color: white;
  }

  .calendar-day.today {
    border: 2px solid #4fc3f7;
  }

  .calendar-day.other-month {
    color: rgba(255, 255, 255, 0.3);
  }

  .checkin-legend {
    font-size: 10px;
    color: #999;
    text-align: center;
    display: flex;
    justify-content: center;
    gap: 15px;
  }

  .legend-item {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .legend-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    display: inline-block;
  }

  .legend-dot.checked {
    background: #4caf50;
  }

  .legend-dot.unchecked {
    background: transparent;
    border: 1px solid #ffffff;
  }

  /* 物理定律每日一悟样式 */
  .physics-laws-page {
    width: 100%;
    min-height: 100vh;
    background-color: #0a1128;
    color: #ffffff;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    display: flex;
    flex-direction: column;
  }

  .physics-laws-container {
    width: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .physics-laws-header {
    padding: 20px;
    background-color: rgba(10, 17, 40, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    font-size: 24px;
    font-weight: bold;
    color: #4fc3f7;
  }

  .back-btn,
  .favorites-btn {
    padding: 10px 20px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s ease;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  .back-btn:hover,
  .favorites-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  .favorites-btn {
    background-color: rgba(255, 215, 0, 0.1);
    border: 1px solid rgba(255, 215, 0, 0.3);
    color: #ffd700;
  }

  .favorites-btn:hover {
    background-color: rgba(255, 215, 0, 0.2);
  }

  .search-container {
    position: relative;
    display: flex;
    align-items: center;
  }

  .search-input {
    padding: 10px 40px 10px 15px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    font-size: 14px;
    outline: none;
    transition: all 0.3s ease;
    width: 200px;
  }

  .search-input::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }

  .search-input:focus {
    border-color: #4fc3f7;
    box-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
    width: 250px;
  }

  .search-btn {
    position: absolute;
    right: 5px;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.6);
    cursor: pointer;
    font-size: 14px;
    padding: 8px;
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .search-btn:hover {
    color: #4fc3f7;
    background-color: rgba(79, 195, 247, 0.2);
  }

  .clear-btn {
    position: absolute;
    right: 5px;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.6);
    cursor: pointer;
    font-size: 14px;
    padding: 8px;
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .clear-btn:hover {
    color: #ff6b6b;
    background-color: rgba(255, 107, 107, 0.2);
  }

  .highlight {
    background-color: rgba(255, 215, 0, 0.5);
    color: #000000;
    padding: 0 2px;
    border-radius: 3px;
    font-weight: bold;
  }

  .favorites-count {
    background-color: rgba(255, 215, 0, 0.3);
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 12px;
  }

  .physics-laws-scroll-wrap {
    flex: 1;
    width: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
    scrollbar-width: thin;
    scrollbar-color: rgba(79, 195, 247, 0.5) rgba(255, 255, 255, 0.05);
  }

  .physics-laws-scroll-wrap::-webkit-scrollbar {
    width: 8px;
  }

  .physics-laws-scroll-wrap::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
  }

  .physics-laws-scroll-wrap::-webkit-scrollbar-thumb {
    background: rgba(79, 195, 247, 0.5);
    border-radius: 4px;
  }

  .physics-laws-scroll-wrap::-webkit-scrollbar-thumb:hover {
    background: rgba(79, 195, 247, 0.8);
  }

  .physics-laws-page .main-content {
    padding: 40px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .section-title {
    text-align: center;
    margin-bottom: 40px;
    display: block;
    width: 100%;
  }

  .section-title h1 {
    font-size: 36px;
    font-weight: bold;
    color: #4fc3f7;
    margin-bottom: 10px;
  }

  .section-title p {
    font-size: 16px;
    color: #cccccc;
  }

  .physics-laws-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    width: 100%;
  }

  .physics-laws-page ::v-deep .physics-law-highlight {
    background-color: #ffeb3b;
    color: #1a1a1a;
    padding: 0 2px;
    border-radius: 2px;
  }

  .law-item {
    background-color: rgba(10, 17, 40, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
  }

  .law-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
    border-color: rgba(76, 195, 247, 0.3);
  }

  .law-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 20px;
  }

  .law-icon {
    font-size: 32px;
    background-color: rgba(76, 195, 247, 0.2);
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid rgba(76, 195, 247, 0.3);
  }

  .law-title {
    flex: 1;
  }

  .law-title h2 {
    font-size: 20px;
    font-weight: bold;
    color: #4fc3f7;
    margin-bottom: 5px;
  }

  .law-title p {
    font-size: 14px;
    color: #999999;
  }

  .law-quote {
    font-size: 16px;
    line-height: 1.6;
    color: #ffffff;
    padding: 20px;
    background-color: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    border-left: 4px solid #4fc3f7;
    font-style: italic;
  }

  .law-meta {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: #999999;
  }

  .law-index {
    font-weight: bold;
    color: #4fc3f7;
  }

  .favorite-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 24px;
    color: rgba(255, 255, 255, 0.3);
    transition: all 0.3s ease;
    padding: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .favorite-btn:hover {
    color: rgba(255, 215, 0, 0.7);
    transform: scale(1.2);
  }

  .favorite-btn.active {
    color: #ffd700;
    text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    background-color: rgba(10, 17, 40, 0.8);
    border-radius: 20px;
    border: 1px dashed rgba(255, 255, 255, 0.2);
  }

  .empty-state-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }

  .empty-state h3 {
    font-size: 20px;
    color: #cccccc;
    margin-bottom: 10px;
  }

  .empty-state p {
    font-size: 16px;
    color: #999999;
  }

  .favorites-modal {
    display: flex;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(10px);
    z-index: 1000;
    justify-content: center;
    align-items: center;
  }

  .favorites-content {
    background-color: rgba(10, 17, 40, 0.95);
    border-radius: 20px;
    border: 1px solid rgba(255, 215, 0, 0.3);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
    max-width: 800px;
    width: 90%;
    max-height: 80vh;
    overflow-y: auto;
    padding: 30px;
  }

  .favorites-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  }

  .favorites-header h2 {
    color: #ffd700;
    font-size: 24px;
  }

  .close-modal {
    background: none;
    border: none;
    color: #ffffff;
    font-size: 28px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .close-modal:hover {
    color: #ffd700;
    transform: rotate(90deg);
  }

  .favorites-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .favorites-empty {
    text-align: center;
    padding: 40px 20px;
    color: #999999;
  }

  .favorites-empty-icon {
    font-size: 48px;
    margin-bottom: 15px;
  }

  @media (max-width: 768px) {
    .physics-laws-page .main-content {
      padding: 20px;
    }

    .section-title h1 {
      font-size: 28px;
    }

    .law-item {
      padding: 20px;
    }

    .law-header {
      flex-direction: column;
      text-align: center;
      gap: 10px;
    }

    .law-icon {
      width: 50px;
      height: 50px;
      font-size: 24px;
    }

    .law-meta {
      flex-direction: column;
      gap: 10px;
    }
  }

  /* 顶部区域 */
  .top-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 20px;
    background-color: transparent;
    z-index: 100;
    position: relative;
  }

  .header-buttons {
    display: flex;
    gap: 12px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #ffffff;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
  }

  .logo-icon {
    font-size: 20px;
  }

  .countdown-display {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    padding: 8px 15px;
    border-radius: 20px;
    color: #ffffff;
    font-size: 14px;
    width: fit-content;
  }

  .current-time {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    font-size: 24px;
    font-weight: bold;
    color: #b3e5fc;
    text-shadow: 0 0 20px rgba(79, 195, 247, 0.5);
  }

  .btn {
    padding: 8px 16px;
    border: 1px solid rgba(255, 255, 255, 0.8);
    border-radius: 20px;
    background-color: transparent;
    color: #ffffff;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .btn:hover {
    background-color: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 1);
    transform: translateY(-2px);
    box-shadow: 0 0 15px rgba(79, 195, 247, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  .btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .btn-primary:hover {
    background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
    box-shadow: 0 0 15px rgba(118, 75, 162, 0.8);
  }

  .daily-physics {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-top: 0;
    justify-content: center;
  }

  .physics-quote {
    flex: 1;
    color: #ffffff;
    font-size: 20px;
    line-height: 1.4;
    text-align: center;
    font-weight: bold;
    text-shadow: 0 2px 10px rgba(79, 195, 247, 0.8), 0 4px 20px rgba(79, 195, 247, 0.5);
    position: relative;
    top: 40px;
    left: -20px;
  }

  .refresh-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    transition: all 0.3s ease;
    color: #ffffff;
    padding: 5px;
    position: relative;
    top: 40px;
    left: -40px;
  }

  .refresh-btn:hover {
    transform: rotate(180deg);
  }

  .study-status {
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .online-users {
    font-size: 14px;
    background-color: rgba(76, 175, 80, 0.1);
    padding: 6px 12px;
    border-radius: 15px;
    border: 1px solid rgba(76, 175, 80, 0.1);
  }

  .formula-btn {
    font-size: 14px;
    background-color: rgba(33, 150, 243, 0.2);
    padding: 6px 12px;
    border-radius: 15px;
    border: 1px solid rgba(33, 150, 243, 0.3);
    color: #ffffff;
    text-decoration: none;
    margin-left: 10px;
    transition: all 0.3s ease;
  }

  .formula-btn:hover {
    background-color: rgba(33, 150, 243, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 0 15px rgba(33, 150, 243, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  /* 主内容区域 */
  .main-content {
    flex: 1;
    display: flex;
    overflow: hidden;
    background-color: transparent;
    position: relative;
    z-index: 1;
  }

  /* 左侧工具区域 */
  .left-tools {
    width: 250px;
    background-color: rgba(10, 17, 40, 0.02);
    border-right: 1px solid rgba(255, 255, 255, 0.02);
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .formula-library-btn {
    text-decoration: none;
    color: inherit;
    display: flex;
    align-items: center;
    background-color: rgba(79, 195, 247, 0.2);
    border-radius: 10px;
    padding: 5px 15px;
    border: 2px solid rgba(79, 195, 247, 0.4);
    transition: all 0.3s ease;
  }

  .formula-library-btn:hover {
    background-color: rgba(79, 195, 247, 0.4);
    box-shadow: 0 0 20px rgba(79, 195, 247, 0.8);
    border-color: rgba(79, 195, 247, 0.7);
    transform: translateY(-2px);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  .formula-library-btn:hover .tool-title {
    color: #b3e5fc;
  }

  .tool-section {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    padding: 15px;
  }

  /* 左侧工具链接按钮的 tool-section 不需要背景框 */
  .tool-section-link {
    background-color: transparent;
    backdrop-filter: none;
    border: none;
    border-radius: 0;
    padding: 0;
  }

  /* 右侧面板的 tool-section 不需要背景框，由内部面板提供 */
  .right-companions .tool-section {
    background-color: transparent;
    backdrop-filter: none;
    border: none;
    border-radius: 0;
    padding: 0;
    margin-bottom: 20px;
  }

  .chat-room-entry {
    cursor: pointer;
    transition: all 0.3s ease;
    background: linear-gradient(135deg, rgba(79, 195, 247, 0.15) 0%, rgba(56, 189, 248, 0.1) 100%) !important;
    border: 1px solid rgba(79, 195, 247, 0.4) !important;
  }

  .chat-room-entry:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(79, 195, 247, 0.3);
    border-color: rgba(79, 195, 247, 0.6) !important;
  }

  .chat-room-badge {
    font-size: 12px;
    font-weight: normal;
    color: #22c55e;
    background: rgba(34, 197, 94, 0.15);
    padding: 2px 10px;
    border-radius: 12px;
    margin-left: auto;
  }

  .chat-room-preview {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .preview-users {
    display: flex;
    gap: -8px;
    padding-left: 4px;
  }

  .preview-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-weight: 600;
    font-size: 12px;
    border: 2px solid rgba(30, 41, 59, 0.9);
    margin-left: -8px;
  }

  .preview-avatar:first-child {
    margin-left: 0;
  }

  .preview-avatar.more {
    background: rgba(79, 195, 247, 0.3);
    font-size: 10px;
  }

  .preview-tip {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
    text-align: center;
    padding: 8px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
  }

  /* 白噪音面板不需要背景框 */
  .noise-panel {
    background-color: transparent;
    backdrop-filter: none;
    border: none;
    border-radius: 0;
    padding: 0;
    margin-bottom: 30px;
  }

  .tool-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #ffffff;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .formula-library {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 10px;
    padding: 15px;
  }

  .formula-category {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #ffffff;
  }

  .formula-item {
    font-size: 13px;
    margin-bottom: 8px;
    padding: 8px;
    background-color: rgba(255, 255, 255, 0.03);
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .formula-item:hover {
    background-color: rgba(255, 255, 255, 0.08);
    transform: translateX(5px);
  }

  .virtual-notebook {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 10px;
    padding: 15px;
  }

  .notebook-area {
    width: 100%;
    min-height: 150px;
    background-color: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 6px;
    padding: 10px;
    color: #ffffff;
    font-family: inherit;
    resize: vertical;
  }

  .physics-law {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 8px;
    color: #b3e5fc;
  }

  .todo-panel {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    padding: 15px;
  }

  .todo-list {
    max-height: 200px;
    overflow-y: auto;
    margin-bottom: 10px;
    border: none;
    box-shadow: none;
    outline: none;
  }

  .todo-list * {
    border: none !important;
    box-shadow: none !important;
    outline: none !important;
  }

  .empty-tip {
    text-align: center;
    color: #ffffff;
    padding: 20px;
    font-size: 12px;
    font-weight: bold;
  }

  .todo-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    background: rgba(255, 255, 255, 0.03);
    border-radius: 6px;
    margin-bottom: 0;
    margin-top: 6px;
    transition: all 0.3s ease;
    border-bottom: none;
    box-shadow: none;
    outline: none;
  }

  .todo-item:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateX(3px);
    border-bottom: none;
    box-shadow: none;
    outline: none;
  }

  .todo-item.completed {
    opacity: 0.6;
  }

  .todo-item.completed .todo-text {
    text-decoration: line-through;
    color: #999;
  }

  .todo-checkbox {
    width: 16px;
    height: 16px;
    cursor: pointer;
    accent-color: #4fc3f7;
  }

  .todo-checkbox:focus {
    outline: none;
    box-shadow: none;
    border: none;
  }

  .todo-text {
    flex: 1;
    font-size: 12px;
    color: #ffffff;
    word-break: break-word;
  }

  .todo-delete {
    background: rgba(255, 107, 107, 0.3);
    color: white;
    border: none;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
  }

  .todo-delete:hover {
    background: rgba(255, 107, 107, 0.5);
    transform: scale(1.1);
  }

  .add-todo {
    display: flex;
    gap: 8px;
    align-items: center;
    width: 100%;
    box-sizing: border-box;
  }

  .todo-input {
    flex: 1;
    padding: 8px 10px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    font-size: 12px;
    background: rgba(255, 255, 255, 0.05);
    color: #ffffff;
    outline: none;
    transition: border-color 0.3s ease;
    min-width: 0;
    box-sizing: border-box;
  }

  .todo-input::placeholder {
    color: #ffffff;
    font-weight: bold;
  }

  .todo-input:focus {
    border-color: #4fc3f7;
  }

  .add-btn {
    background: rgba(79, 195, 247, 0.3);
    color: white;
    border: 2px solid rgba(79, 195, 247, 0.8);
    padding: 8px 12px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.3s ease;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .add-btn:hover {
    background: rgba(79, 195, 247, 0.5);
    border-color: rgba(79, 195, 247, 1);
    transform: translateY(-2px);
    box-shadow: 0 0 15px rgba(79, 195, 247, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  /* 学习时间计时器 */
  .timer-panel {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    padding: 15px;
  }

  .timer-display {
    font-size: 32px;
    font-weight: bold;
    color: #ffffff;
    margin: 10px 0;
    font-family: 'Courier New', monospace;
    text-align: center;
  }

  .timer-settings {
    margin: 15px 0;
    text-align: center;
    color: #ffffff;
    font-size: 14px;
    font-weight: bold;
  }
  .timer-settings label {
    text-align: left;
    display: inline-block;
    line-height: 1.4;
  }

  .timer-input {
    padding: 6px 10px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    background: rgba(255, 255, 255, 0.05);
    color: #ffffff;
    font-size: 14px;
    cursor: text;
    width: 90px;
    min-width: 90px;
    text-align: center;
    margin: 0 5px;
    outline: none;
    transition: border-color 0.3s ease;
    -moz-appearance: textfield;
  }
  .timer-input::-webkit-inner-spin-button,
  .timer-input::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }

  .timer-input:focus {
    border-color: #4fc3f7;
  }

  .timer-controls {
    display: flex;
    gap: 8px;
    justify-content: center;
    margin-top: 15px;
  }

  .timer-btn {
    background: rgba(79, 195, 247, 0.2);
    color: white;
    border: 1px solid rgba(79, 195, 247, 0.4);
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.3s ease;
    flex: 1;
  }

  .timer-btn:hover {
    background: rgba(79, 195, 247, 0.4);
    border-color: rgba(79, 195, 247, 0.6);
    transform: translateY(-2px);
  }

  .timer-btn.start-btn {
    background: rgba(79, 195, 247, 0.3);
    border-color: rgba(79, 195, 247, 0.5);
  }

  .timer-btn.start-btn:hover {
    background: rgba(79, 195, 247, 0.5);
    border-color: rgba(79, 195, 247, 0.7);
  }

  .timer-stats {
    display: flex;
    gap: 15px;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    font-size: 12px;
    color: #ffffff;
    font-weight: bold;
    justify-content: center;
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .stat-value {
    font-weight: bold;
    color: #ffffff;
  }

  /* 中部自习室场景 */
  .center-room {
    flex: 1;
    position: relative;
    overflow: hidden;
  }

  .room-background {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: -1;
    overflow: hidden;
  }

  .bg-video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  /* 确保页面内容在视频之上 */
  .top-section {
    position: relative;
    z-index: 100;
  }

  .main-content {
    position: relative;
    z-index: 1;
  }

  /* 座位系统样式 */
  .seating-system {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    background-color: rgba(10, 17, 40, 0.7);
    backdrop-filter: blur(10px);
    border-radius: 15px;
    padding: 20px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }

  .seating-title {
    font-size: 16px;
    font-weight: bold;
    color: #ffffff;
  }

  .seats-container {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
  }

  .seat {
    width: 60px;
    height: 60px;
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.2);
  }

  .seat:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-5px);
  }

  .seat.occupied {
    background-color: rgba(255, 87, 34, 0.3);
    border-color: rgba(255, 87, 34, 0.5);
  }

  .seat.your-seat {
    background-color: rgba(76, 175, 80, 0.3);
    border-color: rgba(76, 175, 80, 0.5);
  }

  .seat-number {
    font-size: 12px;
    margin-bottom: 4px;
  }

  .seat-status {
    font-size: 10px;
    opacity: 0.7;
  }

  .random-seat-btn {
    padding: 8px 20px;
    background-color: rgba(156, 39, 176, 0.3);
    border: 1px solid rgba(156, 39, 176, 0.5);
    border-radius: 20px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 14px;
  }

  .random-seat-btn:hover {
    background-color: rgba(156, 39, 176, 0.5);
    transform: translateY(-2px);
    box-shadow: 0 0 15px rgba(156, 39, 176, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  /* 右侧同伴区域 */
  .right-companions {
    width: 250px;
    background-color: rgba(10, 17, 40, 0.02);
    border-left: 1px solid rgba(255, 255, 255, 0.02);
    padding: 20px;
    overflow-y: auto;
  }

  .companions-list {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    padding: 15px;
  }

  .companion-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    padding: 8px;
    background-color: rgba(255, 255, 255, 0.005);
    border-radius: 6px;
    transition: all 0.3s ease;
  }

  .companion-item:hover {
    background-color: rgba(255, 255, 255, 0.08);
  }

  .companion-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #4fc3f7;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 16px;
  }

  .companion-info {
    flex: 1;
  }

  .companion-name {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 2px;
  }

  .companion-goal {
    font-size: 12px;
    opacity: 0.7;
  }

  .interaction-panel {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 10px;
    padding: 15px;
    margin-top: 20px;
    overflow: hidden;
  }

  .chat-input-wrap {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-bottom: 10px;
  }

  .chat-input {
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    padding: 10px 15px;
    color: #ffffff;
    font-family: inherit;
  }

  .chat-send-btn {
    flex-shrink: 0;
    padding: 8px 14px;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    border: none;
    border-radius: 20px;
    color: white;
    font-size: 13px;
    cursor: pointer;
  }

  .chat-send-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .chat-messages {
    max-height: 200px;
    overflow-y: auto;
  }

  .message-item {
    font-size: 13px;
    margin-bottom: 8px;
    padding: 6px 10px;
    background-color: rgba(255, 255, 255, 0.03);
    border-radius: 10px;
  }

  .message-sender {
    font-weight: bold;
    color: #ffffff;
    margin-right: 5px;
  }

  .message-time {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.6);
    margin-left: 5px;
  }

  .environment-controls {
    display: flex;
    justify-content: center;
    gap: 15px;
    padding: 20px;
  }

  .env-btn {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: rgba(79, 195, 247, 0.3);
    border: 2px solid rgba(79, 195, 247, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 18px;
  }

  .env-btn:hover {
    background-color: rgba(79, 195, 247, 0.5);
    border-color: rgba(79, 195, 247, 1);
    transform: translateY(-2px);
    box-shadow: 0 0 15px rgba(79, 195, 247, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  /* 徽章墙相关样式 */
  .achievement-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 6px;
    margin-bottom: 6px;
    transition: all 0.3s ease;
  }

  .achievement-icon {
    font-size: 18px;
  }

  .achievement-info {
    flex: 1;
  }

  .achievement-name {
    font-size: 12px;
    font-weight: bold;
  }

  .achievement-desc {
    font-size: 10px;
    color: #cccccc;
  }

  /* 白噪音标题样式 */
  .noise-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  /* 白噪音按钮样式 */
  .noise-buttons {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .noise-btn {
    padding: 8px 12px;
    border: 2px solid rgba(255, 152, 0, 0.4);
    border-radius: 8px;
    background: rgba(255, 152, 0, 0.2);
    color: #ffffff;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.3s ease;
  }

  .noise-btn:hover {
    background: rgba(255, 152, 0, 0.4);
    border-color: rgba(255, 152, 0, 0.8);
    box-shadow: 0 0 15px rgba(255, 152, 0, 0.8);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
  }

  .noise-btn.active {
    background: rgba(255, 152, 0, 0.3);
    color: #ffffff;
    border-color: rgba(255, 152, 0, 0.8);
  }

  .volume-control {
    margin-top: 10px;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .volume-slider {
    flex: 1;
    -webkit-appearance: none;
    height: 6px;
    border-radius: 3px;
    background: rgba(255, 255, 255, 0.4);
    outline: none;
  }

  .volume-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #4fc3f7;
    cursor: pointer;
  }

  .noise-close-btn {
    background: rgba(255, 107, 107, 0.3);
    color: white;
    border: 1px solid rgba(255, 107, 107, 0.5);
    padding: 4px 8px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 11px;
    transition: all 0.3s ease;
    white-space: nowrap;
  }

  .noise-close-btn:hover {
    background: rgba(255, 107, 107, 0.5);
    transform: scale(1.05);
  }

  /* 智能选座系统弹窗 */
  .seat-system-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1000;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .seat-system-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
  }
  
  .seat-system-container {
    position: relative;
    z-index: 1001;
    max-width: 880px;
    width: 100%;
    background: rgba(10, 17, 40, 0.95);
    border-radius: 20px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.3);
    overflow: hidden;
    padding: 16px 16px 12px 16px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(79, 195, 247, 0.3);
    margin-bottom: 16px;
    font-size: 15px;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .seat-system-logo {
    font-weight: 600;
    color: #4fc3f7;
    letter-spacing: 0.3px;
    text-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
  }
  
  .seat-system-user-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    background: rgba(255, 255, 255, 0.1);
    padding: 5px 12px;
    border-radius: 40px;
    border: 1px solid rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-user-name {
    font-weight: 500;
    color: #ffffff;
  }
  
  .seat-system-switch-btn {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(79, 195, 247, 0.3);
    padding: 4px 10px;
    border-radius: 30px;
    font-size: 12px;
    color: #ffffff;
    cursor: pointer;
    transition: 0.3s;
  }
  
  .seat-system-switch-btn:hover {
    background: rgba(79, 195, 247, 0.2);
    box-shadow: 0 0 10px rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-main-panel {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }
  
  .seat-system-left-goal {
    flex: 0 0 200px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 16px;
    padding: 16px 12px;
    border: 1px solid rgba(79, 195, 247, 0.3);
    display: flex;
    flex-direction: column;
    gap: 18px;
  }
  
  .seat-system-goal-section {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .seat-system-section-title {
    font-size: 14px;
    font-weight: 600;
    color: #4fc3f7;
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .seat-system-goal-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid rgba(79, 195, 247, 0.3);
    border-radius: 12px;
    font-size: 13px;
    resize: none;
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff;
  }
  
  .seat-system-goal-input::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }
  
  .seat-system-goal-input:focus {
    outline: none;
    border-color: #4fc3f7;
    box-shadow: 0 0 0 2px rgba(79, 195, 247, 0.2);
  }
  
  .seat-system-goal-input:disabled {
    background: rgba(255, 255, 255, 0.05);
    color: rgba(255, 255, 255, 0.5);
    cursor: not-allowed;
  }
  
  .seat-system-btn {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(79, 195, 247, 0.3);
    padding: 8px 0;
    border-radius: 30px;
    font-size: 13px;
    font-weight: 500;
    color: #ffffff;
    cursor: pointer;
    transition: 0.3s;
    text-align: center;
  }
  
  .seat-system-btn-primary {
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    border: 1px solid #4fc3f7;
    color: white;
    box-shadow: 0 0 15px rgba(79, 195, 247, 0.5);
  }
  
  .seat-system-btn-primary:hover {
    background: linear-gradient(135deg, #29b6f6 0%, #03a9f4 100%);
    box-shadow: 0 0 20px rgba(79, 195, 247, 0.7);
  }
  
  .seat-system-btn-danger {
    background: rgba(239, 68, 68, 0.2);
    border: 1px solid rgba(239, 68, 68, 0.4);
    color: #fecaca;
  }
  
  .seat-system-btn-danger:hover {
    background: rgba(239, 68, 68, 0.3);
    box-shadow: 0 0 15px rgba(239, 68, 68, 0.4);
  }
  
  .seat-system-btn:active {
    transform: scale(0.97);
  }
  
  .seat-system-tip {
    font-size:12px;
    color:#4fc3f7;
    background:rgba(79, 195, 247, 0.1);
    padding:8px;
    border-radius:12px;
    border:1px solid rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-middle-seats {
    flex: 1;
    min-width: 300px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 16px;
    padding: 16px 10px;
    border: 1px solid rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-seat-grid {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  
  .seat-system-seat-row {
    display: flex;
    justify-content: center;
    gap: 6px;
    flex-wrap: wrap;
  }
  
  .seat-system-seat {
    width: 40px;
    height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0,0,0,0.2);
    user-select: none;
    position: relative;
  }
  
  .seat-system-seat-free {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1.5px solid rgba(79, 195, 247, 0.3);
    color: #ffffff;
  }
  
  .seat-system-seat-free:hover {
    background-color: rgba(79, 195, 247, 0.2);
    border-color: #4fc3f7;
    box-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
  }
  
  .seat-system-seat-mine {
    background-color: #22c55e;
    border: 1.5px solid #16a34a;
    color: white;
    box-shadow: 0 0 15px rgba(34, 197, 94, 0.5);
  }
  
  .seat-system-seat-occupied {
    background-color: #ef4444;
    border: 1.5px solid #dc2626;
    color: white;
    box-shadow: 0 0 15px rgba(239, 68, 68, 0.4);
  }
  
  .seat-system-seat:hover::after {
    content: attr(data-tip);
    position: absolute;
    bottom: 120%;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(10, 17, 40, 0.95);
    color: white;
    font-size: 11px;
    padding: 4px 8px;
    border-radius: 16px;
    white-space: nowrap;
    z-index: 100;
    pointer-events: none;
    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    border: 1px solid rgba(79, 195, 247, 0.3);
  }
  
  .seat-system-footer-stats {
    display: flex;
    justify-content: space-between;
    background: rgba(255, 255, 255, 0.05);
    padding: 10px 16px;
    border-radius: 30px;
    font-size: 13px;
    color: #ffffff;
    margin-top: 8px;
    border: 1px solid rgba(79, 195, 247, 0.3);
  }

  /* 在线用户列表样式 */
  .seat-system-online-users {
    margin-top: 12px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    border: 1px solid rgba(79, 195, 247, 0.3);
    overflow: hidden;
  }

  .online-users-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: rgba(79, 195, 247, 0.1);
    border-bottom: 1px solid rgba(79, 195, 247, 0.2);
    font-size: 14px;
    font-weight: 600;
    color: #4fc3f7;
  }

  .online-count {
    background: rgba(34, 197, 94, 0.2);
    color: #22c55e;
    padding: 2px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
  }

  .online-users-list {
    max-height: 200px;
    overflow-y: auto;
    padding: 8px;
  }

  .online-user-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    transition: background 0.2s;
    cursor: default;
  }

  .online-user-item:hover {
    background: rgba(79, 195, 247, 0.1);
  }

  .online-user-item .user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
  }

  .online-user-item .user-info {
    flex: 1;
    min-width: 0;
  }

  .online-user-item .user-name {
    font-size: 13px;
    font-weight: 500;
    color: #e2e8f0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .online-user-item .user-seat {
    font-size: 11px;
    color: #94a3b8;
    margin-top: 2px;
  }

  .online-user-item .user-goal {
    font-size: 11px;
    color: #4fc3f7;
    background: rgba(79, 195, 247, 0.1);
    padding: 4px 8px;
    border-radius: 6px;
    max-width: 100px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .no-users {
    text-align: center;
    color: #64748b;
    font-size: 13px;
    padding: 20px;
  }

  .online-users-list::-webkit-scrollbar {
    width: 4px;
  }

  .online-users-list::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 2px;
  }

  .online-users-list::-webkit-scrollbar-thumb {
    background: rgba(79, 195, 247, 0.3);
    border-radius: 2px;
  }
  
  /* 窄屏适配 */
  @media (max-width: 700px) {
    .seat-system-left-goal {
      flex: 1 1 100%;
      order: 1;
    }
    .seat-system-middle-seats { order: 2; }
  }

  /* 学习时间和智能选座组件 */
  .study-timer-container {
    background-color: rgba(79, 195, 247, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(79, 195, 247, 0.4);
    padding: 5px;
    border-radius: 8px;
    width: 220px;
  }
  
  .study-timer-container .tool-title {
    text-align: center;
    margin-bottom: 3px;
    font-size: 14px;
    font-weight: bold;
    color: #ffffff;
  }
  
  .smart-seat-container {
    background: linear-gradient(135deg, rgba(79, 195, 247, 0.15) 0%, rgba(56, 189, 248, 0.1) 100%);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(79, 195, 247, 0.5);
    padding: 10px;
    border-radius: 12px;
    width: 220px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    box-shadow: 
      0 4px 20px rgba(79, 195, 247, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
    position: relative;
    overflow: hidden;
  }
  
  .smart-seat-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    animation: shimmer 3s infinite;
  }
  
  @keyframes shimmer {
    0% { left: -100%; }
    100% { left: 100%; }
  }
  
  .smart-seat-btn {
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    background: linear-gradient(135deg, rgba(255, 152, 0, 0.8) 0%, rgba(251, 146, 60, 0.9) 50%, rgba(245, 158, 11, 0.8) 100%);
    color: white;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-size: 14px;
    font-weight: bold;
    position: relative;
    overflow: hidden;
    box-shadow: 
      0 4px 15px rgba(255, 152, 0, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
  
  .smart-seat-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
  }
  
  .smart-seat-btn:hover {
    transform: translateY(-3px) scale(1.02);
    box-shadow: 
      0 8px 25px rgba(255, 152, 0, 0.5),
      0 0 30px rgba(255, 152, 0, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
  
  .smart-seat-btn:hover::before {
    left: 100%;
  }
  
  .smart-seat-btn:active {
    transform: translateY(-1px) scale(0.98);
  }
  
  #studyGoalDisplay {
    color: #ffffff;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    padding: 6px 12px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 6px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    width: 100%;
    box-sizing: border-box;
  }
  
  .timer-panel {
    text-align: center;
  }
  
  #timerDisplay {
    font-size: 28px;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 10px;
  }
  
  .timer-settings {
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 8px;
    justify-content: center;
  }
  
  .timer-settings label {
    color: #ffffff;
    font-size: 12px;
    text-align: left;
    line-height: 1.4;
  }
  
  .timer-input {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 4px;
    padding: 2px 6px;
    color: #ffffff;
    width: 90px;
    min-width: 90px;
    text-align: center;
    font-size: 12px;
    -moz-appearance: textfield;
  }
  .timer-input::-webkit-inner-spin-button,
  .timer-input::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  
  .timer-buttons {
    display: flex;
    gap: 5px;
    justify-content: center;
  }
  
  .timer-controls {
    display: flex;
    gap: 6px;
    justify-content: center;
    margin-bottom: 3px;
  }
  
  .timer-stats {
    color: #ffffff;
    font-size: 10px;
  }

  /* 动态公式库样式 */
  .formula-library-page {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 10000;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, rgba(10, 17, 40, 0.98), rgba(26, 35, 126, 0.98));
  }

  .formula-library-container {
    position: relative;
    width: 100vw;
    height: 100vh;
    background: transparent;
    border: none;
    border-radius: 0;
    box-shadow: none;
    backdrop-filter: none;
    overflow: hidden;
    z-index: 10001;
  }

  .formula-library-header {
    padding: 12px 20px;
    background: transparent;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(79, 195, 247, 0.3);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 100;
  }

  .formula-library-title {
    font-size: 24px;
    font-weight: bold;
    color: #4fc3f7;
    text-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
  }

  .formula-library-back-btn {
    padding: 12px 24px;
    background: linear-gradient(45deg, rgba(79, 195, 247, 0.2), rgba(123, 179, 255, 0.2));
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 30px;
    color: #ffffff;
    cursor: pointer;
    font-size: 16px;
    transition: all 0.3s ease;
  }

  .formula-library-back-btn:hover {
    background: linear-gradient(45deg, rgba(79, 195, 247, 0.3), rgba(123, 179, 255, 0.3));
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(79, 195, 247, 0.3);
  }

  .formula-library-banner {
    width: 100%;
    height: calc(100% - 80px);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
    margin-top: 10px;
  }

  .formula-library-slider-container {
    position: relative;
    width: 100%;
    height: 85%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .formula-library-slider {
    position: absolute;
    width: 16vw;
    max-width: 200px;
    min-width: 140px;
    height: 19vw;
    max-height: 240px;
    min-height: 170px;
    top: 5%;
    transform-style: preserve-3d;
    transform: perspective(1200px);
    animation: autoRotate 20s linear infinite;
    transition: transform 0.5s ease;
  }

  .formula-library-slider.paused {
    animation-play-state: paused;
  }

  @keyframes autoRotate {
    from {
      transform: perspective(1200px) rotateX(-10deg) rotateY(0deg);
    }
    to {
      transform: perspective(1200px) rotateX(-10deg) rotateY(360deg);
    }
  }

  .formula-library-item {
    position: absolute;
    width: 100%;
    height: 100%;
    transform: rotateY(calc((var(--position) - 1) * (360 / var(--quantity)) * 1deg)) translateZ(calc(192vw * 0.1));
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(79, 195, 247, 0.1));
    border: 1px solid rgba(79, 195, 247, 0.3);
    backdrop-filter: blur(10px);
  }

  .formula-library-item:hover {
    transform: rotateY(calc((var(--position) - 1) * (360 / var(--quantity)) * 1deg)) translateZ(calc(224vw * 0.1)) scale(1.1);
    border: 2px solid #4fc3f7;
    box-shadow: 0 0 24px rgba(79, 195, 247, 0.6);
  }

  .formula-library-item-content {
    padding: 20px;
    text-align: center;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    overflow: hidden;
  }

  .formula-library-item-icon {
    font-size: 2.5vw;
    max-font-size: 30px;
    min-font-size: 20px;
    margin-bottom: 8px;
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
  }

  .formula-library-item-title {
    font-size: 0.9vw;
    max-font-size: 14px;
    min-font-size: 10px;
    font-weight: bold;
    color: #4fc3f7;
    margin-bottom: 6px;
    text-align: center;
  }

  .formula-library-item-formula {
    font-size: 0.8vw;
    max-font-size: 12px;
    min-font-size: 9px;
    font-family: 'Times New Roman', Times, serif;
    color: #ffcc80;
    text-align: center;
    line-height: 1.4;
    max-height: 70px;
    overflow-y: auto;
    padding: 5px;
  }

  .formula-library-controls {
    position: absolute;
    bottom: 100px;
    display: flex;
    gap: 10px;
    z-index: 10;
  }

  .formula-library-control-btn {
    padding: 10px 20px;
    background: rgba(79, 195, 247, 0.2);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 30px;
    color: #ffffff;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .formula-library-control-btn:hover {
    background: rgba(79, 195, 247, 0.4);
    transform: translateY(-2px);
  }

  .formula-library-instructions {
    text-align: center;
    margin-top: 10px;
    color: #bbdefb;
    font-size: 14px;
    padding: 5px 20px;
  }

  /* 草稿纸与计算器样式 - 对齐 notebook-calculator.html */
  .notebook-page {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 20000;
    pointer-events: auto;
    display: flex;
    flex-direction: column;
    background-color: #0a1128;
    overflow: auto;
  }

  .notebook-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 100%;
  }

  .notebook-page .notebook-header {
    padding: 20px;
    background-color: rgba(10, 17, 40, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .notebook-page .header-title {
    font-size: 24px;
    font-weight: bold;
    color: #4fc3f7;
  }

  .notebook-page .notebook-back-btn {
    padding: 10px 20px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .notebook-page .notebook-back-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  .notebook-page .notebook-main-content {
    flex: 1;
    padding: 40px;
    display: flex;
    gap: 30px;
    align-items: flex-start;
  }

  .notebook-page .content-section {
    background-color: rgba(10, 17, 40, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 40px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }

  .notebook-page .notebook-container {
    flex: 2;
    min-width: 0;
  }

  .notebook-page .notebook-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 20px;
    color: #4fc3f7;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .notebook-page .notebook-area {
    width: 100%;
    min-height: 400px;
    background-color: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 20px;
    color: #ffffff;
    font-family: inherit;
    font-size: 16px;
    resize: vertical;
    line-height: 1.5;
    box-sizing: border-box;
  }

  .notebook-page .notebook-area:focus {
    outline: none;
    border-color: rgba(76, 175, 255, 0.5);
    box-shadow: 0 0 10px rgba(76, 175, 255, 0.3);
  }

  .notebook-page .notebook-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin-top: 20px;
  }

  .notebook-page .control-buttons {
    display: flex;
    gap: 10px;
  }

  .notebook-page .record-btn {
    padding: 10px 20px;
    background-color: rgba(76, 195, 247, 0.3);
    border: 1px solid rgba(76, 195, 247, 0.5);
    border-radius: 20px;
    color: #ffffff;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .notebook-page .record-btn:hover {
    background-color: rgba(76, 195, 247, 0.4);
    transform: translateY(-2px);
  }

  .notebook-page .control-btn {
    padding: 10px 20px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    z-index: 2;
    pointer-events: auto;
  }

  .notebook-page .control-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  .notebook-page .control-btn.primary {
    background-color: rgba(76, 175, 80, 0.3);
    border-color: rgba(76, 175, 80, 0.5);
  }

  .notebook-page .control-btn.primary:hover {
    background-color: rgba(76, 175, 80, 0.4);
  }

  .notebook-page .calculator-container {
    flex: 1;
    min-width: 300px;
  }

  .notebook-page .calculator-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 20px;
    color: #4fc3f7;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .notebook-page .calculator-display {
    margin-bottom: 20px;
  }

  .notebook-page .calculator-input {
    width: 100%;
    background-color: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 15px;
    color: #ffffff;
    font-size: 18px;
    text-align: right;
    font-family: 'Courier New', Courier, monospace;
  }

  .notebook-page .calculator-input:focus {
    outline: none;
    border-color: rgba(76, 175, 255, 0.5);
    box-shadow: 0 0 10px rgba(76, 175, 255, 0.3);
  }

  .notebook-page .calculator-buttons {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }

  .calc-btn {
    background-color: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 12px 8px;
    color: #ffffff;
    font-size: 18px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .calc-btn:hover {
    background-color: rgba(255, 255, 255, 0.1);
    transform: translateY(-2px);
  }

  .calc-btn.operator {
    background-color: rgba(255, 152, 0, 0.2);
    border-color: rgba(255, 152, 0, 0.3);
  }

  .calc-btn.operator:hover {
    background-color: rgba(255, 152, 0, 0.3);
  }

  .calc-btn.function {
    background-color: rgba(156, 39, 176, 0.2);
    border-color: rgba(156, 39, 176, 0.3);
    font-size: 16px;
  }

  .calc-btn.function:hover {
    background-color: rgba(156, 39, 176, 0.3);
  }

  .calc-btn.equal {
    background-color: rgba(76, 175, 80, 0.3);
    border-color: rgba(76, 175, 80, 0.5);
    grid-column: span 2;
  }

  .calc-btn.equal:hover {
    background-color: rgba(76, 175, 80, 0.4);
  }

  .calc-btn.clear {
    background-color: rgba(244, 67, 54, 0.3);
    border-color: rgba(244, 67, 54, 0.5);
  }

  .calc-btn.clear:hover {
    background-color: rgba(244, 67, 54, 0.4);
  }

  .calc-btn.disabled {
    background-color: rgba(76, 175, 80, 0.3);
    border-color: rgba(76, 175, 80, 0.5);
    cursor: not-allowed;
    font-size: 12px;
  }

  .calc-btn.disabled:hover {
    background-color: rgba(76, 175, 80, 0.3);
    transform: none;
  }

  /* 物理定律每日一悟样式 */
  .physics-laws-page {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 10000;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, rgba(10, 17, 40, 0.98), rgba(26, 35, 126, 0.98));
  }

  .physics-laws-container {
    position: relative;
    width: 100vw;
    height: 100vh;
    background: transparent;
    border: none;
    border-radius: 0;
    box-shadow: none;
    backdrop-filter: none;
    overflow: hidden;
    z-index: 10001;
    display: flex;
    flex-direction: column;
  }

  .physics-laws-header {
    padding: 20px;
    background-color: rgba(10, 17, 40, 0.9);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 100;
  }

  .physics-laws-title {
    font-size: 24px;
    font-weight: bold;
    color: #4fc3f7;
  }

  .physics-laws-header-right {
    display: flex;
    gap: 15px;
    align-items: center;
  }

  .search-container {
    position: relative;
    display: flex;
    align-items: center;
  }

  .search-input {
    padding: 10px 40px 10px 15px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    font-size: 14px;
    outline: none;
    transition: all 0.3s ease;
    width: 200px;
  }

  .search-input::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }

  .search-input:focus {
    border-color: #4fc3f7;
    box-shadow: 0 0 10px rgba(79, 195, 247, 0.5);
    width: 250px;
  }

  .search-btn {
    position: absolute;
    right: 5px;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.6);
    cursor: pointer;
    font-size: 14px;
    padding: 8px;
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .search-btn:hover {
    color: #4fc3f7;
    background-color: rgba(79, 195, 247, 0.2);
  }

  .clear-btn {
    position: absolute;
    right: 5px;
    background: none;
    border: none;
    color: rgba(255, 255, 255, 0.6);
    cursor: pointer;
    font-size: 14px;
    padding: 8px;
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .clear-btn:hover {
    color: #ff6b6b;
    background-color: rgba(255, 107, 107, 0.2);
  }

  .favorites-btn {
    padding: 10px 20px;
    background-color: rgba(255, 215, 0, 0.1);
    border: 1px solid rgba(255, 215, 0, 0.3);
    border-radius: 20px;
    color: #ffd700;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  .favorites-btn:hover {
    background-color: rgba(255, 215, 0, 0.2);
    transform: translateY(-2px);
  }

  .favorites-count {
    background-color: rgba(255, 215, 0, 0.3);
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 12px;
  }

  .physics-laws-back-btn {
    padding: 10px 20px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }

  .physics-laws-back-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  .physics-laws-main-content {
    flex: 1;
    padding: 40px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    height: calc(100% - 80px);
    overflow-y: auto;
  }

  .section-title {
    text-align: center;
    margin-bottom: 40px;
  }

  .section-title h1 {
    font-size: 36px;
    font-weight: bold;
    color: #4fc3f7;
    margin-bottom: 10px;
  }

  .section-title p {
    font-size: 16px;
    color: #cccccc;
  }

  .physics-laws-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .physics-laws-page ::v-deep .physics-law-highlight {
    background-color: #ffeb3b;
    color: #1a1a1a;
    padding: 0 2px;
    border-radius: 2px;
  }

  .law-item {
    background-color: rgba(10, 17, 40, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
  }

  .law-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
    border-color: rgba(76, 195, 247, 0.3);
  }

  .law-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 20px;
  }

  .law-icon {
    font-size: 32px;
    background-color: rgba(76, 195, 247, 0.2);
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid rgba(76, 195, 247, 0.3);
  }

  .law-title {
    flex: 1;
  }

  .law-title h2 {
    font-size: 20px;
    font-weight: bold;
    color: #4fc3f7;
    margin-bottom: 5px;
  }

  .law-title p {
    font-size: 14px;
    color: #999999;
  }

  .law-quote {
    font-size: 16px;
    line-height: 1.6;
    color: #ffffff;
    padding: 20px;
    background-color: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    border-left: 4px solid #4fc3f7;
    font-style: italic;
  }

  .law-meta {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: #999999;
  }

  .law-index {
    font-weight: bold;
    color: #4fc3f7;
  }

  .favorite-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 24px;
    color: rgba(255, 255, 255, 0.3);
    transition: all 0.3s ease;
    padding: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .favorite-btn:hover {
    color: rgba(255, 215, 0, 0.7);
    transform: scale(1.2);
  }

  .favorite-btn.active {
    color: #ffd700;
    text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    background-color: rgba(10, 17, 40, 0.8);
    border-radius: 20px;
    border: 1px dashed rgba(255, 255, 255, 0.2);
  }

  .empty-state-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }

  .empty-state h3 {
    font-size: 20px;
    color: #cccccc;
    margin-bottom: 10px;
  }

  .empty-state p {
    font-size: 16px;
    color: #999999;
  }

  .favorites-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(10px);
    z-index: 1000;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .favorites-content {
    background-color: rgba(10, 17, 40, 0.95);
    border-radius: 20px;
    border: 1px solid rgba(255, 215, 0, 0.3);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
    max-width: 800px;
    width: 90%;
    max-height: 80vh;
    overflow-y: auto;
    padding: 30px;
  }

  .favorites-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  }

  .favorites-header h2 {
    color: #ffd700;
    font-size: 24px;
  }

  .close-modal {
    background: none;
    border: none;
    color: #ffffff;
    font-size: 28px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .close-modal:hover {
    color: #ffd700;
    transform: rotate(90deg);
  }

  .favorites-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .favorites-empty {
    text-align: center;
    padding: 40px 20px;
    color: #999999;
  }

  .favorites-empty-icon {
    font-size: 48px;
    margin-bottom: 15px;
  }

  .highlight {
    background-color: rgba(255, 215, 0, 0.5);
    color: #000000;
    padding: 0 2px;
    border-radius: 3px;
    font-weight: bold;
  }

  /* 响应式设计 */
  @media (max-width: 1200px) {
    .notebook-page .notebook-main-content {
      flex-direction: column;
      align-items: center;
    }

    .notebook-page .notebook-container,
    .notebook-page .calculator-container {
      width: 100%;
      max-width: 800px;
    }
  }

  @media (max-width: 768px) {
    .notebook-page .notebook-main-content {
      padding: 20px;
    }

    .notebook-page .notebook-area {
      min-height: 300px;
    }
  }

  @media (max-width: 768px) {
    .physics-laws-main-content {
      padding: 20px;
    }

    .section-title h1 {
      font-size: 28px;
    }

    .law-item {
      padding: 20px;
    }

    .law-header {
      flex-direction: column;
      text-align: center;
      gap: 10px;
    }

    .law-icon {
      width: 50px;
      height: 50px;
      font-size: 24px;
    }

    .law-meta {
      flex-direction: column;
      gap: 10px;
    }
  }

  @media (max-width: 768px) {
    .formula-library-header,
    .notebook-header,
    .physics-laws-header,
    .notebook-records-header {
      padding: 15px 20px;
    }

    .formula-library-title,
    .notebook-title,
    .physics-laws-title,
    .notebook-records-title {
      font-size: 20px;
    }

    .notebook-main-content,
    .physics-laws-main-content,
    .notebook-records-main-content {
      padding: 15px;
      gap: 15px;
    }

    .formula-library-slider {
      width: 130px;
      height: 160px;
      top: 3%;
    }

    .formula-library-item {
      transform: rotateY(calc((var(--position) - 1) * (360 / var(--quantity)) * 1deg)) translateZ(280px);
    }

    .formula-library-item:hover {
      transform: rotateY(calc((var(--position) - 1) * (360 / var(--quantity)) * 1deg)) translateZ(340px) scale(1.05);
    }

    .formula-library-banner {
      height: 78vh;
      padding-top: 5px;
    }

    .formula-library-controls {
      bottom: 60px;
    }

    .formula-library-control-btn {
      padding: 8px 15px;
      font-size: 14px;
    }

    .formula-library-item-icon {
      font-size: 18px;
      margin-bottom: 4px;
    }

    #notebookCanvas {
      height: 300px;
    }

    .calculator-btn {
      padding: 12px;
      font-size: 16px;
    }

    .physics-law-item,
    .notebook-record-item {
      padding: 20px;
    }

    .notebook-record-image {
      max-width: 100%;
      height: auto;
    }
  }

  .notebook-records-fig3-section {
    background: rgba(10, 17, 40, 0.6);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 25px;
    margin-top: 20px;
  }
  .notebook-records-fig3-title {
    font-size: 16px;
    color: #4fc3f7;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .notebook-record-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 18px 20px;
    margin-bottom: 12px;
  }
  .notebook-record-card:last-child {
    margin-bottom: 0;
  }
  .notebook-record-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  .notebook-record-card-title {
    font-size: 16px;
    color: #4fc3f7;
    font-weight: 500;
  }
  .notebook-record-card-time {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
  }
  .notebook-record-card-snippet {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 12px;
    line-height: 1.5;
  }
  .notebook-record-card-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  .record-action-btn {
    padding: 6px 14px;
    border-radius: 8px;
    font-size: 13px;
    cursor: pointer;
    border: none;
    position: relative;
    z-index: 2;
    pointer-events: auto;
  }
  .record-action-btn.edit {
    background: rgba(255, 193, 7, 0.3);
    color: #ffc107;
  }
  .record-action-btn.rename {
    background: rgba(156, 39, 176, 0.3);
    color: #ce93d8;
  }
  .record-action-btn.view {
    background: rgba(79, 195, 247, 0.3);
    color: #4fc3f7;
  }
  .record-action-btn.delete {
    background: rgba(244, 67, 54, 0.3);
    color: #f44336;
  }
  .record-action-btn:hover {
    opacity: 0.9;
  }
  .notebook-view-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    z-index: 20000;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .notebook-view-content {
    background: rgba(10, 17, 40, 0.98);
    border: 1px solid rgba(79, 195, 247, 0.4);
    border-radius: 16px;
    width: 75%;
    min-width: 560px;
    max-width: 900px;
    min-height: 320px;
    max-height: 85vh;
    overflow: auto;
    padding: 25px;
  }
  .notebook-view-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  .notebook-view-header h3 {
    color: #4fc3f7;
    font-size: 18px;
  }
  .notebook-view-close {
    background: none;
    border: none;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
  }
  .notebook-view-body {
    color: rgba(255, 255, 255, 0.9);
    white-space: pre-wrap;
    word-break: break-word;
    min-height: 200px;
  }
  .notebook-view-text {
    margin-bottom: 15px;
    line-height: 1.6;
  }
  .notebook-edit-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    z-index: 20001;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .notebook-edit-content {
    background: rgba(20, 35, 70, 0.98);
    border: 1px solid rgba(79, 195, 247, 0.5);
    border-radius: 16px;
    width: 75%;
    min-width: 560px;
    max-width: 900px;
    max-height: 85vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .notebook-edit-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(79, 195, 247, 0.3);
  }
  .notebook-edit-header h3 {
    color: #4fc3f7;
    font-size: 18px;
  }
  .notebook-edit-close {
    background: none;
    border: none;
    color: #fff;
    font-size: 20px;
    cursor: pointer;
  }
  .notebook-edit-body {
    flex: 1;
    padding: 20px;
    overflow: auto;
  }
  .notebook-edit-textarea {
    width: 100%;
    min-height: 280px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(79, 195, 247, 0.5);
    border-radius: 10px;
    padding: 16px;
    color: #fff;
    font-size: 15px;
    line-height: 1.6;
    resize: vertical;
    box-sizing: border-box;
  }
  .notebook-edit-textarea:focus {
    outline: none;
    border-color: rgba(79, 195, 247, 0.8);
  }
  .notebook-edit-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid rgba(79, 195, 247, 0.3);
  }
  .notebook-edit-btn {
    padding: 10px 24px;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    border: 1px solid;
  }
  .notebook-edit-btn.cancel {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
    color: #fff;
  }
  .notebook-edit-btn.primary {
    background: rgba(79, 195, 247, 0.4);
    border-color: rgba(79, 195, 247, 0.6);
    color: #fff;
  }
  .notebook-edit-btn:hover {
    opacity: 0.9;
  }
  .notebook-view-canvas {
    max-width: 100%;
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  /* 草稿记录样式 */
  .notebook-records-page {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 20000;
    pointer-events: auto;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, rgba(10, 17, 40, 0.98), rgba(26, 35, 126, 0.98));
  }

  .notebook-records-container {
    position: relative;
    width: 100vw;
    height: 100vh;
    background: transparent;
    border: none;
    border-radius: 0;
    box-shadow: none;
    backdrop-filter: none;
    overflow: hidden;
    z-index: 10001;
  }

  .notebook-records-header {
    padding: 12px 20px;
    background: transparent;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(79, 195, 247, 0.3);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 100;
  }

  .notebook-records-title {
    font-size: 24px;
    font-weight: bold;
    color: #4fc3f7;
  }

  .notebook-records-back-btn {
    padding: 10px 20px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    color: #ffffff;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .notebook-records-back-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

  .notebook-records-main-content {
    height: calc(100% - 120px);
    padding: 40px;
    overflow-y: auto;
  }

  .empty-records {
    text-align: center;
    padding: 60px 20px;
    color: #999;
  }

  .empty-records-icon {
    font-size: 48px;
    margin-bottom: 20px;
  }

  .empty-records-text {
    font-size: 18px;
    margin-bottom: 10px;
    color: #ffffff;
  }

  .empty-records-hint {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.6);
  }

  .notebook-records-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .notebook-record-item {
    background-color: rgba(10, 17, 40, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
  }

  .notebook-record-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
  }

  .notebook-record-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .notebook-record-title {
    color: #4fc3f7;
    font-size: 18px;
    font-weight: bold;
  }

  .notebook-record-actions {
    display: flex;
    gap: 10px;
  }

  .action-btn {
    padding: 6px 12px;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 15px;
    color: #ffffff;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.3s ease;
  }

  .action-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }

  .notebook-record-timestamp {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.6);
    margin-bottom: 15px;
  }

  .notebook-record-content {
    margin-top: 10px;
  }

  .notebook-record-image {
    max-width: 100%;
    height: auto;
    border-radius: 10px;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .notebook-records-footer {
    padding: 20px 30px;
    background: rgba(10, 17, 40, 0.8);
    border-top: 1px solid rgba(79, 195, 247, 0.3);
    display: flex;
    justify-content: flex-end;
  }

  .notebook-records-clear-btn {
    padding: 10px 20px;
    background-color: rgba(244, 67, 54, 0.2);
    border: 1px solid rgba(244, 67, 54, 0.4);
    border-radius: 20px;
    color: #f44336;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .notebook-records-clear-btn:hover {
    background-color: rgba(244, 67, 54, 0.3);
    transform: translateY(-2px);
  }
  
  .timer-stats span {
    margin-left: 8px;
  }
  
  .timer-stats span:first-child {
    margin-left: 0;
  }
  
  .timer-btn {
    padding: 3px 6px;
    border: 1px solid rgba(255, 152, 0, 0.4);
    border-radius: 4px;
    background-color: rgba(255, 152, 0, 0.2);
    color: white;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 12px;
  }
  
  /* 响应式设计 */
  @media (max-width: 1200px) {
    .left-tools,
    .right-companions {
      width: 250px;
    }
  }

  @media (max-width: 992px) {
    .main-content {
      flex-direction: column;
    }

    .left-tools,
    .right-companions {
      width: 100%;
      height: 200px;
      border: none;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .center-room {
      flex: 1;
    }
  }

  /* 滚动条样式 */
  ::-webkit-scrollbar {
    width: 6px;
  }

  ::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }

  ::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
  }

  ::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.3);
  }
</style>