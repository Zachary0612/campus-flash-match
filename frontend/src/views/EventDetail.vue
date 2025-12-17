<template>
  <Layout>
    <div class="event-detail-page relative z-10">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-20">
        <el-icon class="is-loading text-5xl text-primary"><Loading /></el-icon>
        <p class="mt-4 text-gray-500">加载中...</p>
      </div>

      <template v-else-if="event">
        <!-- 事件头部 -->
        <div class="glass-card rounded-3xl p-8 mb-8 animate-slide-up relative overflow-hidden group">
          <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-primary/10 to-purple-500/10 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
          
          <div class="flex flex-col md:flex-row justify-between items-start mb-8 relative z-10">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-4">
                <el-tag :type="event.eventType === 'group_buy' ? 'primary' : 'success'" effect="dark" class="!rounded-lg !px-3 !h-8 !text-sm !font-bold shadow-md">
                  {{ event.eventType === 'group_buy' ? '🛍️ 拼单' : '👥 约伴' }}
                </el-tag>
                <el-tag :type="getStatusTag(event.status)" effect="light" class="!rounded-lg !px-3 !h-8 !text-sm !font-bold border !bg-white/50 backdrop-blur-sm">
                  {{ getStatusName(event.status) }}
                </el-tag>
              </div>
              <h1 class="text-4xl font-black text-gray-800 mb-4 tracking-tight leading-tight">{{ event.title }}</h1>
              <p v-if="event.description" class="text-gray-600 text-lg leading-relaxed max-w-3xl bg-white/30 p-4 rounded-xl border border-white/40 backdrop-blur-sm shadow-sm">{{ event.description }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-3 mt-4 md:mt-0">
              <el-button
                :type="isFavorite ? 'warning' : 'default'"
                :icon="isFavorite ? StarFilled : Star"
                @click="handleFavorite"
                :loading="favoriteLoading"
                circle
                size="large"
                class="!shadow-md hover:!scale-110 transition-transform"
              />
              
              <template v-if="event.status === 'active'">
                <el-button
                  v-if="isOwner"
                  type="danger"
                  @click="handleCancelEvent"
                  class="!rounded-xl shadow-lg shadow-red-200"
                  size="large"
                >
                  取消事件
                </el-button>
                <el-button
                  v-else-if="!isJoined"
                  type="primary"
                  @click="handleJoinEvent"
                  :loading="joinLoading"
                  class="!rounded-xl shadow-lg shadow-blue-200 !font-bold !px-8"
                  size="large"
                >
                  立即参与
                </el-button>
                <el-button
                  v-else
                  type="warning"
                  @click="handleQuitEvent"
                  :loading="quitLoading"
                  class="!rounded-xl shadow-lg shadow-orange-200"
                  size="large"
                >
                  退出事件
                </el-button>
              </template>
              
              <!-- 待确认状态：显示确认按钮 -->
              <template v-if="event.status === 'pending_confirm' && (isOwner || isJoined)">
                <el-button
                  v-if="!confirmationStatus.currentUserConfirmed"
                  type="success"
                  @click="handleConfirmEvent"
                  :loading="confirmLoading"
                  class="!rounded-xl shadow-lg shadow-green-200 !font-bold animate-pulse-slow"
                  size="large"
                >
                  <el-icon class="mr-2"><Check /></el-icon>
                  确认完成
                </el-button>
                <el-tag v-else type="success" effect="dark" size="large" class="!rounded-xl !px-4 !h-10">
                  <el-icon class="mr-1"><Check /></el-icon>
                  已确认
                </el-tag>
              </template>
            </div>
          </div>
          
          <!-- 待确认状态提示 -->
          <div v-if="event.status === 'pending_confirm'" class="mb-6 bg-gradient-to-r from-orange-50 to-orange-100/50 border border-orange-200 rounded-xl p-5 shadow-inner">
            <div class="flex items-center text-orange-700 font-bold text-lg mb-2">
              <el-icon class="mr-2 text-xl animate-bounce-gentle"><Bell /></el-icon>
              <span>事件已满员，等待所有成员确认完成</span>
            </div>
            <div class="text-orange-600 font-medium ml-7">
              确认进度：{{ confirmationStatus.confirmedCount }}/{{ confirmationStatus.totalCount }} 人已确认
            </div>
            <div class="w-full bg-orange-200 h-2 rounded-full mt-3 ml-7 max-w-md overflow-hidden">
              <div class="bg-orange-500 h-full rounded-full transition-all duration-1000 ease-out" :style="{ width: `${(confirmationStatus.confirmedCount / confirmationStatus.totalCount) * 100}%` }"></div>
            </div>
          </div>

          <!-- 集合地点 -->
          <div v-if="eventLocation" class="mb-8 bg-blue-50/50 border border-blue-100/60 rounded-2xl p-5 flex items-center shadow-sm backdrop-blur-sm">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mr-4 shrink-0">
              <el-icon class="text-blue-600 text-2xl"><Location /></el-icon>
            </div>
            <div class="flex-1">
              <div class="text-xs font-bold text-blue-500 uppercase tracking-wider mb-1">集合地点</div>
              <div class="text-blue-900 font-bold text-lg">{{ eventLocation }}</div>
            </div>
            <el-button type="primary" link @click="scrollToMap">
              查看路线 <el-icon class="ml-1"><ArrowRight /></el-icon>
            </el-button>
          </div>

          <!-- 事件信息 Stats -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-8">
            <div class="bg-white/40 rounded-2xl p-5 text-center border border-white/50 shadow-sm backdrop-blur-md hover:-translate-y-1 transition-transform duration-300">
              <div class="text-3xl font-black text-primary mb-1">{{ event.currentNum }}<span class="text-lg text-gray-400 font-medium">/{{ event.targetNum }}</span></div>
              <div class="text-sm font-bold text-gray-500 uppercase tracking-wide">参与人数</div>
            </div>
            <div class="bg-white/40 rounded-2xl p-5 text-center border border-white/50 shadow-sm backdrop-blur-md hover:-translate-y-1 transition-transform duration-300">
              <div class="text-3xl font-black text-gray-800 mb-1">{{ event.expireMinutes }}<span class="text-sm text-gray-500 font-medium">m</span></div>
              <div class="text-sm font-bold text-gray-500 uppercase tracking-wide">有效时长</div>
            </div>
            <div class="bg-white/40 rounded-2xl p-5 text-center border border-white/50 shadow-sm backdrop-blur-md hover:-translate-y-1 transition-transform duration-300">
              <div class="text-3xl font-black text-gray-800 mb-1">{{ commentCount }}</div>
              <div class="text-sm font-bold text-gray-500 uppercase tracking-wide">评论数</div>
            </div>
            <div class="bg-white/40 rounded-2xl p-5 text-center border border-white/50 shadow-sm backdrop-blur-md hover:-translate-y-1 transition-transform duration-300">
              <div class="text-3xl font-black text-gray-800 mb-1">{{ event.favoriteCount || 0 }}</div>
              <div class="text-sm font-bold text-gray-500 uppercase tracking-wide">收藏数</div>
            </div>
          </div>
        </div>

        <el-row :gutter="20">
          <!-- 左侧：发起者和参与者 -->
          <el-col :span="8">
            <!-- 发起者信息 -->
            <div class="glass-card rounded-3xl p-6 mb-6 animate-slide-up bg-white/40 border border-white/50" style="animation-delay: 0.1s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center text-lg">
                <div class="p-1.5 bg-blue-100 rounded-lg mr-2 text-primary">
                  <el-icon><User /></el-icon>
                </div>
                发起者
              </h3>
              <div class="flex items-center gap-4 cursor-pointer hover:bg-white/50 p-3 rounded-2xl transition-all duration-300 group" @click="goToProfile(event.ownerId)">
                <el-avatar :size="56" :src="event.ownerAvatar" class="border-2 border-white shadow-md group-hover:scale-105 transition-transform">
                  {{ event.ownerNickname?.charAt(0) }}
                </el-avatar>
                <div>
                  <div class="font-bold text-gray-800 text-lg group-hover:text-primary transition-colors">{{ event.ownerNickname }}</div>
                  <div class="text-sm font-medium flex items-center mt-1">
                    <span class="text-gray-500 mr-2">信用分</span>
                    <span :class="getScoreClass(event.ownerCreditScore)" class="font-bold bg-gray-100 px-2 py-0.5 rounded text-xs">{{ event.ownerCreditScore }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 参与者列表 -->
            <div class="glass-card rounded-3xl p-6 shadow-sm backdrop-blur-xl bg-white/40 border border-white/50 animate-slide-up" style="animation-delay: 0.15s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center justify-between text-lg">
                <div class="flex items-center">
                  <div class="p-1.5 bg-green-100 rounded-lg mr-2 text-green-600">
                    <el-icon><UserFilled /></el-icon>
                  </div>
                  参与者 <span class="text-sm font-normal text-gray-500 ml-2">({{ event.participants?.length || 0 }})</span>
                </div>
              </h3>
              <div v-if="!event.participants?.length" class="text-center py-8 bg-white/30 rounded-2xl border border-dashed border-gray-300">
                <el-icon class="text-3xl text-gray-300 mb-2"><User /></el-icon>
                <p class="text-gray-400 text-sm">暂无参与者</p>
              </div>
              <div v-else class="space-y-2 max-h-[300px] overflow-y-auto pr-1 custom-scrollbar">
                <div
                  v-for="p in event.participants"
                  :key="p.userId"
                  class="flex items-center gap-3 p-2 rounded-xl hover:bg-white/60 cursor-pointer transition-all duration-300 group"
                  @click="goToProfile(p.userId)"
                >
                  <el-avatar :size="40" :src="p.avatar" class="border border-white shadow-sm group-hover:scale-105 transition-transform">{{ p.nickname?.charAt(0) }}</el-avatar>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                      <span class="text-gray-800 font-bold truncate">{{ p.nickname }}</span>
                      <el-tag v-if="p.isOwner" size="small" type="warning" effect="dark" class="ml-2 !rounded-md scale-90">发起者</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 即时交流（仅参与者可见） -->
            <div v-if="isJoined || isOwner" class="glass-card rounded-3xl p-6 mt-6 bg-white/40 border border-white/50 animate-slide-up flex flex-col h-[500px]" style="animation-delay: 0.2s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center text-lg shrink-0">
                <div class="p-1.5 bg-orange-100 rounded-lg mr-2 text-orange-500">
                  <el-icon><ChatLineSquare /></el-icon>
                </div>
                即时交流
                <span class="ml-2 text-xs font-normal px-2 py-0.5 bg-orange-50 text-orange-400 rounded-full">内部群聊</span>
              </h3>
              
              <!-- 聊天消息列表 - iMessage/WeChat 风格 -->
              <div ref="chatContainerRef" class="chat-messages flex-1 overflow-y-auto bg-gradient-to-b from-gray-50/50 to-white/30 rounded-2xl p-4 mb-4 space-y-3 shadow-inner border border-white/40 custom-scrollbar">
                <div v-if="chatMessages.length === 0" class="text-center py-12 text-gray-400 text-sm flex flex-col items-center">
                  <div class="w-20 h-20 bg-gradient-to-br from-gray-100 to-gray-50 rounded-full flex items-center justify-center mb-4 shadow-inner">
                    <el-icon class="text-3xl text-gray-300"><ChatDotRound /></el-icon>
                  </div>
                  <p class="font-medium">暂无消息</p>
                  <p class="text-xs text-gray-400 mt-1">快来打个招呼吧~</p>
                </div>
                <div
                  v-for="(msg, index) in chatMessages"
                  :key="msg.id"
                  class="chat-bubble-wrapper"
                >
                  <!-- 时间分割线 -->
                  <div v-if="shouldShowTimeDivider(msg, index)" class="flex justify-center my-4">
                    <span class="text-[10px] text-gray-400 bg-gray-100/80 px-3 py-1 rounded-full">{{ formatChatTime(msg.time) }}</span>
                  </div>
                  
                  <div
                    class="flex gap-2"
                    :class="msg.userId === userStore.userId ? 'flex-row-reverse' : ''"
                  >
                    <!-- 头像 -->
                    <el-avatar 
                      :size="32" 
                      :src="msg.avatar" 
                      class="shrink-0 cursor-pointer hover:scale-105 transition-transform shadow-sm"
                      @click="goToProfile(msg.userId)"
                    >
                      {{ msg.nickname?.charAt(0) }}
                    </el-avatar>
                    
                    <!-- 消息内容 -->
                    <div class="flex flex-col max-w-[70%]" :class="msg.userId === userStore.userId ? 'items-end' : 'items-start'">
                      <!-- 昵称 -->
                      <div v-if="msg.userId !== userStore.userId" class="text-[11px] text-gray-500 mb-1 ml-3 font-medium">
                        {{ msg.nickname }}
                      </div>
                      
                      <!-- 气泡 -->
                      <div class="relative group">
                        <div
                          class="chat-bubble px-4 py-2.5 text-sm break-words leading-relaxed"
                          :class="msg.userId === userStore.userId 
                            ? 'bg-gradient-to-br from-primary via-blue-500 to-blue-600 text-white bubble-right shadow-md shadow-blue-200/50' 
                            : 'bg-white text-gray-700 bubble-left shadow-sm border border-gray-100/80'"
                        >
                          {{ msg.content }}
                        </div>
                        <!-- 时间标签（悬停显示） -->
                        <div 
                          class="absolute top-1/2 -translate-y-1/2 text-[10px] text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap"
                          :class="msg.userId === userStore.userId ? 'right-full mr-2' : 'left-full ml-2'"
                        >
                          {{ formatChatTime(msg.time) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 发送消息 -->
              <div class="flex gap-2 shrink-0 bg-white/50 p-2 rounded-2xl border border-white/60 shadow-sm backdrop-blur-sm">
                <el-input
                  v-model="chatInput"
                  placeholder="说点什么..."
                  @keyup.enter="sendChatMessage"
                  class="flex-1 !border-none !bg-transparent"
                  :input-style="{ boxShadow: 'none', background: 'transparent' }"
                />
                <el-button type="primary" circle class="!w-10 !h-10 shadow-md" @click="sendChatMessage" :disabled="!chatInput.trim()">
                  <el-icon><Position /></el-icon>
                </el-button>
              </div>
            </div>
          </el-col>

          <!-- 右侧：路线导航和评论区 -->
          <el-col :span="16">
            <!-- 路线导航（仅参与者可见） -->
            <div v-if="(isJoined || isOwner) && destinationLocation" class="glass-card rounded-3xl p-6 mb-6 animate-slide-up bg-white/40 border border-white/50" style="animation-delay: 0.18s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center justify-between text-lg">
                <div class="flex items-center">
                  <div class="p-1.5 bg-emerald-100 rounded-lg mr-2 text-emerald-600">
                    <el-icon><Position /></el-icon>
                  </div>
                  前往集合点
                </div>
                <el-button 
                  v-if="currentLocation" 
                  size="small" 
                  @click="getCurrentLocation" 
                  :loading="gettingLocation"
                  class="!rounded-lg"
                >
                  <el-icon class="mr-1"><Aim /></el-icon>
                  重新定位
                </el-button>
              </h3>
              <div v-if="!currentLocation" class="text-center py-16 bg-white/30 rounded-2xl border border-dashed border-emerald-200">
                <div class="w-20 h-20 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-4 animate-pulse">
                  <el-icon class="text-4xl text-emerald-400"><Position /></el-icon>
                </div>
                <el-button type="primary" @click="getCurrentLocation" :loading="gettingLocation" size="large" class="!rounded-xl shadow-lg shadow-emerald-200/50 !bg-emerald-500 !border-emerald-500">
                  <el-icon class="mr-2"><Aim /></el-icon>
                  获取我的位置
                </el-button>
                <p class="text-sm text-gray-500 mt-4 font-medium">
                  {{ userStore.isLoggedIn ? '获取位置后为您规划最佳路线' : '登录后体验完整导航功能' }}
                </p>
              </div>
              <div v-else class="rounded-2xl overflow-hidden shadow-md border border-white/50">
                <RouteMap 
                  :origin="currentLocation"
                  :destination="destinationLocation"
                  height="500px"
                />
              </div>
            </div>
            
            <!-- 评论区 -->
            <div class="glass-card rounded-3xl p-8 bg-white/40 border border-white/50 animate-slide-up" style="animation-delay: 0.2s">
              <h3 class="font-bold text-gray-800 mb-6 flex items-center text-xl">
                <div class="p-2 bg-blue-100 rounded-xl mr-3 text-blue-600">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                评论区
              </h3>

              <!-- 发表评论 -->
              <div class="mb-8 bg-white/50 p-4 rounded-2xl border border-white/60 shadow-sm">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="分享你的想法..."
                  class="mb-3 !bg-transparent"
                  :input-style="{ backgroundColor: 'rgba(255,255,255,0.5)', borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)' }"
                />
                <div class="flex justify-end">
                  <el-button type="primary" @click="handleAddComment" :loading="commentLoading" :disabled="!commentContent.trim()" class="!rounded-xl !px-6 shadow-md">
                    发表评论
                  </el-button>
                </div>
              </div>

              <!-- 评论列表 - 嵌套卡片设计 -->
              <div v-if="comments.length === 0" class="text-center py-16 text-gray-400">
                <div class="w-24 h-24 bg-gradient-to-br from-gray-50 to-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 shadow-inner">
                  <el-icon class="text-4xl text-gray-300"><ChatDotRound /></el-icon>
                </div>
                <p class="font-medium text-gray-500">暂无评论</p>
                <p class="text-sm text-gray-400 mt-1">快来抢沙发吧~</p>
              </div>
              <div v-else class="space-y-4">
                <div v-for="(comment, cIndex) in comments" :key="comment.id" class="comment-card group">
                  <!-- 主评论卡片 -->
                  <div class="bg-gradient-to-br from-white/80 to-white/60 rounded-2xl p-5 border border-white/70 shadow-sm hover:shadow-lg transition-all duration-300 backdrop-blur-sm">
                    <!-- 评论头部 -->
                    <div class="flex items-start gap-4">
                      <el-avatar 
                        :size="44" 
                        :src="comment.avatar" 
                        @click="goToProfile(comment.userId)" 
                        class="cursor-pointer ring-2 ring-white shadow-md hover:scale-110 transition-transform shrink-0"
                      >
                        {{ comment.nickname?.charAt(0) }}
                      </el-avatar>
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center justify-between mb-1">
                          <div class="flex items-center gap-2">
                            <span class="font-bold text-gray-800 hover:text-primary cursor-pointer transition-colors" @click="goToProfile(comment.userId)">
                              {{ comment.nickname }}
                            </span>
                            <span class="text-[10px] text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">#{{ cIndex + 1 }}</span>
                          </div>
                          <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
                        </div>
                        <p class="text-gray-700 leading-relaxed mt-2">{{ comment.content }}</p>
                        
                        <!-- 操作按钮 -->
                        <div class="flex items-center gap-4 mt-3 pt-3 border-t border-gray-100">
                          <button 
                            class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-primary transition-colors group/btn"
                            @click="handleLikeComment(comment)"
                          >
                            <div class="p-1 rounded-full group-hover/btn:bg-blue-50 transition-colors">
                              <el-icon class="text-base"><Pointer /></el-icon>
                            </div>
                            <span class="font-medium">{{ comment.likeCount || 0 }}</span>
                          </button>
                          <button 
                            class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-primary transition-colors group/btn"
                            @click="handleReply(comment)"
                          >
                            <div class="p-1 rounded-full group-hover/btn:bg-blue-50 transition-colors">
                              <el-icon class="text-base"><ChatLineSquare /></el-icon>
                            </div>
                            <span class="font-medium">回复</span>
                          </button>
                          <span v-if="comment.replies?.length" class="text-xs text-gray-400 ml-auto">
                            {{ comment.replies.length }} 条回复
                          </span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 回复列表 - 嵌套卡片 -->
                    <div v-if="comment.replies?.length" class="mt-4 ml-14">
                      <div class="bg-gradient-to-br from-gray-50/80 to-white/50 rounded-xl p-4 border border-gray-100/80 space-y-3">
                        <div 
                          v-for="(reply, rIndex) in comment.replies" 
                          :key="reply.id" 
                          class="reply-item flex gap-3 pb-3 last:pb-0"
                          :class="{ 'border-b border-gray-100/60': rIndex < comment.replies.length - 1 }"
                        >
                          <el-avatar 
                            :size="28" 
                            :src="reply.avatar" 
                            class="ring-1 ring-white shadow-sm shrink-0 cursor-pointer hover:scale-105 transition-transform"
                            @click="goToProfile(reply.userId)"
                          >
                            {{ reply.nickname?.charAt(0) }}
                          </el-avatar>
                          <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-1 text-sm flex-wrap">
                              <span class="font-bold text-gray-700 hover:text-primary cursor-pointer transition-colors" @click="goToProfile(reply.userId)">
                                {{ reply.nickname }}
                              </span>
                              <span v-if="reply.replyToNickname" class="text-gray-400">
                                回复
                                <span class="text-primary font-medium cursor-pointer hover:underline">@{{ reply.replyToNickname }}</span>
                              </span>
                              <span class="text-[10px] text-gray-400 ml-auto">{{ formatTime(reply.createTime) }}</span>
                            </div>
                            <p class="text-sm text-gray-600 mt-1 leading-relaxed">{{ reply.content }}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 加载更多 -->
              <div v-if="comments.length > 0 && hasMoreComments" class="text-center mt-8">
                <el-button round @click="loadMoreComments" class="!px-8 !h-10">加载更多评论</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>

      <!-- 事件不存在 -->
      <div v-else class="text-center py-20">
        <el-icon class="text-6xl text-gray-300"><Warning /></el-icon>
        <p class="mt-4 text-gray-500">事件不存在或已被删除</p>
        <el-button type="primary" class="mt-4" @click="$router.push('/home')">返回首页</el-button>
      </div>

      <!-- 回复对话框 -->
      <el-dialog v-model="showReplyDialog" title="回复评论" width="400px">
        <p class="text-gray-500 mb-3">回复 @{{ replyTarget?.nickname }}</p>
        <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="输入回复内容..." />
        <template #footer>
          <el-button @click="showReplyDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitReply" :loading="replyLoading">回复</el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import RouteMap from '@/components/RouteMap.vue'
import { useUserStore } from '@/stores/user'
import { getEventDetail, joinEvent, quitEvent, cancelEvent, confirmEventCompletion, getConfirmationStatus } from '@/api/event'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getEventComments, addComment, likeComment, getCommentCount } from '@/api/comment'
import { getChatMessages } from '@/api/chat'
import { getMyProfile } from '@/api/profile'
import { useWebSocketStore } from '@/stores/websocket'
import { 
  Loading, Star, StarFilled, User, UserFilled, ChatDotRound, 
  Pointer, Warning, ChatLineSquare, Check, Bell, Position, Aim, Location, ArrowRight 
} from '@element-plus/icons-vue'
import { loadAMap } from '@/utils/mapLoader'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const event = ref(null)
const isFavorite = ref(false)
const favoriteLoading = ref(false)
const joinLoading = ref(false)
const quitLoading = ref(false)
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const commentCount = ref(0)
const commentPageNum = ref(1)
const hasMoreComments = ref(false)
const showReplyDialog = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replyLoading = ref(false)

// 聊天相关
const wsStore = useWebSocketStore()
const chatMessages = ref([])
const chatInput = ref('')
const chatContainerRef = ref(null)

// 确认相关
const confirmLoading = ref(false)
const confirmationStatus = ref({
  confirmedCount: 0,
  totalCount: 0,
  currentUserConfirmed: false
})

// 路线导航相关
const currentLocation = ref(null)
const gettingLocation = ref(false)
const destinationLocation = computed(() => {
  if (!event.value) return null
  
  // 优先使用地图选点的位置
  if (event.value.extMeta?.mapLocation) {
    return {
      lng: event.value.extMeta.mapLocation.lng,
      lat: event.value.extMeta.mapLocation.lat,
      address: event.value.extMeta.mapLocation.address || event.value.pointName || '集合点'
    }
  }
  
  // 使用校园点位的坐标（如果有）
  if (event.value.pointLng && event.value.pointLat) {
    return {
      lng: event.value.pointLng,
      lat: event.value.pointLat,
      address: event.value.pointName || '集合点'
    }
  }
  
  // 默认位置（可以设置为校园中心）
  if (event.value.pointName) {
    // 如果只有点位名称，使用默认坐标
    return {
      lng: 116.397428,
      lat: 39.90923,
      address: event.value.pointName
    }
  }
  
  return null
})

// 集合地点文本显示
const eventLocation = computed(() => {
  if (!event.value) return null
  
  // 优先使用地图选点的地址
  if (event.value.extMeta?.mapLocation?.address) {
    return event.value.extMeta.mapLocation.address
  }
  
  // 否则使用点位名称
  if (event.value.pointName) {
    return event.value.pointName
  }
  
  return null
})

// 注：已从 mapLoader.js 导入 loadAMap 统一加载函数

// 加载用户绑定的位置
const loadUserBindLocation = async () => {
  try {
    const res = await getMyProfile()
    if (res.code === 200 && res.data.extMeta?.campusLocation) {
      const location = res.data.extMeta.campusLocation
      if (location.lng && location.lat) {
        currentLocation.value = {
          lng: location.lng,
          lat: location.lat,
          address: location.address || '我的位置'
        }
        console.log('已加载用户绑定位置:', currentLocation.value)
      }
    }
  } catch (error) {
    console.error('加载用户绑定位置失败:', error)
  }
}

// 使用高德地图定位（国内更可靠）
const getCurrentLocation = () => {
  gettingLocation.value = true
  ElMessage.info('正在获取位置信息，请稍候...')
  
  // 动态加载高德地图API
  if (!window.AMap) {
    ElMessage.error('地图服务未加载，请刷新页面重试')
    gettingLocation.value = false
    return
  }
  
  // 1. 尝试使用高精度定位
  window.AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 5000, // 缩短超时时间到5秒
      position: 'RB',
      offset: [10, 20],
      zoomToAccuracy: true,
      noIpLocate: 0 // 允许AMap内部尝试IP定位
    })
    
    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        // 定位成功
        handleLocationSuccess(result)
      } else {
        console.warn('高精度定位失败，尝试CitySearch IP定位:', result)
        // 2. 如果高精度定位失败，尝试使用CitySearch插件
        tryIpLocation()
      }
    })
  })
}

// IP定位作为备选方案
const tryIpLocation = () => {
  window.AMap.plugin('AMap.CitySearch', () => {
    const citySearch = new window.AMap.CitySearch()
    citySearch.getLocalCity((status, result) => {
      if (status === 'complete' && result.info === 'OK') {
        // CitySearch 成功（通常只返回城市矩形bounds）
        gettingLocation.value = false
        console.log('CitySearch定位成功:', result)
        ElMessage.warning('无法获取精确位置，已切换到当前城市中心')
        
        // 由于CitySearch没有直接返回lng/lat中心点，我们需要计算bounds中心或使用默认逻辑
        // 这里简单处理，如果CitySearch成功，我们尝试获取bounds的中心
        // 但为了稳妥，这里直接回退到默认校园位置，因为城市中心离学校太远也没意义
        useDefaultLocation('定位精度不足，已使用默认校园位置')
      } else {
        console.warn('IP定位也失败:', result)
        // 3. 所有定位都失败，使用默认位置
        useDefaultLocation('定位失败，已使用默认校园位置')
      }
    })
  })
}

// 使用默认位置（西南大学）
const useDefaultLocation = (msg) => {
  gettingLocation.value = false
  currentLocation.value = {
    lng: 106.419704,
    lat: 29.817324,
    address: '西南大学(默认位置)'
  }
  ElMessage.warning(msg || '已切换到默认位置')
}

// 处理定位成功
const handleLocationSuccess = (result) => {
  gettingLocation.value = false
  currentLocation.value = {
    lng: result.position.lng,
    lat: result.position.lat,
    address: result.formattedAddress || '我的位置'
  }
  ElMessage.success('定位成功')
  console.log('定位成功:', currentLocation.value)
}

const eventId = computed(() => route.params.eventId)
const isOwner = computed(() => event.value?.ownerId === userStore.userId)
const isJoined = computed(() => event.value?.isJoined)

// 加载事件详情
const loadEventDetail = async () => {
  loading.value = true
  try {
    const res = await getEventDetail(eventId.value)
    if (res.code === 200) {
      event.value = res.data
      isFavorite.value = res.data.isFavorite || false
      
      // 如果是待确认状态，加载确认状态
      if (res.data.status === 'pending_confirm') {
        loadConfirmationStatus()
      }
    }
  } catch (error) {
    console.error('加载事件详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载确认状态
const loadConfirmationStatus = async () => {
  try {
    const res = await getConfirmationStatus(eventId.value)
    if (res.code === 200) {
      confirmationStatus.value = res.data
    }
  } catch (error) {
    console.error('加载确认状态失败:', error)
  }
}

// 确认事件完成
const handleConfirmEvent = async () => {
  confirmLoading.value = true
  try {
    const res = await confirmEventCompletion(eventId.value)
    if (res.code === 200) {
      ElMessage.success(res.message || '确认成功')
      // 重新加载事件详情和确认状态
      await loadEventDetail()
    }
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    confirmLoading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  try {
    const res = await getEventComments(eventId.value, {
      pageNum: commentPageNum.value,
      pageSize: 20
    })
    if (res.code === 200) {
      if (commentPageNum.value === 1) {
        comments.value = res.data || []
      } else {
        comments.value.push(...(res.data || []))
      }
      hasMoreComments.value = (res.data?.length || 0) >= 20
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 加载评论数
const loadCommentCount = async () => {
  try {
    const res = await getCommentCount(eventId.value)
    if (res.code === 200) {
      commentCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取评论数失败:', error)
  }
}

// 加载更多评论
const loadMoreComments = () => {
  commentPageNum.value++
  loadComments()
}

// 收藏/取消收藏
const handleFavorite = async () => {
  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavorite(eventId.value)
      isFavorite.value = false
      if (event.value.favoriteCount > 0) {
        event.value.favoriteCount--
      }
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(eventId.value)
      isFavorite.value = true
      event.value.favoriteCount = (event.value.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    favoriteLoading.value = false
  }
}

// 参与事件
const handleJoinEvent = async () => {
  joinLoading.value = true
  try {
    await joinEvent(eventId.value)
    ElMessage.success('参与成功')
    loadEventDetail()
  } catch (error) {
    console.error('参与失败:', error)
  } finally {
    joinLoading.value = false
  }
}

// 退出事件
const handleQuitEvent = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该事件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    quitLoading.value = true
    await quitEvent(eventId.value)
    ElMessage.success('已退出事件')
    loadEventDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  } finally {
    quitLoading.value = false
  }
}

// 取消事件
const handleCancelEvent = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该事件吗？此操作不可撤销。', '警告', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    await cancelEvent(eventId.value)
    ElMessage.success('事件已取消')
    loadEventDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消事件失败:', error)
    }
  }
}

// 发表评论
const handleAddComment = async () => {
  if (!commentContent.value.trim()) return
  
  commentLoading.value = true
  try {
    await addComment({
      eventId: eventId.value,
      content: commentContent.value
    })
    commentContent.value = ''
    commentPageNum.value = 1
    loadComments()
    loadCommentCount()
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

// 点赞评论
const handleLikeComment = async (comment) => {
  try {
    await likeComment(comment.id)
    comment.likeCount = (comment.likeCount || 0) + 1
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 回复评论
const handleReply = (comment) => {
  replyTarget.value = comment
  replyContent.value = ''
  showReplyDialog.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyContent.value.trim()) return
  
  replyLoading.value = true
  try {
    await addComment({
      eventId: eventId.value,
      content: replyContent.value,
      parentId: replyTarget.value.id,
      replyToUserId: replyTarget.value.userId
    })
    showReplyDialog.value = false
    commentPageNum.value = 1
    loadComments()
    ElMessage.success('回复成功')
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replyLoading.value = false
  }
}

// 跳转到用户主页
const goToProfile = (userId) => {
  router.push(`/user/${userId}`)
}

// 获取状态名称
const getStatusName = (status) => {
  const statuses = {
    'active': '进行中',
    'pending_confirm': '待确认',
    'settled': '已完成',
    'completed': '已完成',
    'cancelled': '已取消',
    'expired': '已过期'
  }
  return statuses[status] || status
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    'active': 'success',
    'pending_confirm': 'warning',
    'settled': 'info',
    'completed': 'info',
    'cancelled': 'danger',
    'expired': 'warning'
  }
  return tags[status] || 'info'
}

// 信用分颜色
const getScoreClass = (score) => {
  if (score >= 90) return 'text-emerald-500'
  if (score >= 80) return 'text-blue-500'
  if (score >= 60) return 'text-yellow-500'
  return 'text-red-500'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 格式化聊天时间
const formatChatTime = (time) => {
  const msgTime = dayjs(time)
  const now = dayjs()
  if (msgTime.isSame(now, 'day')) {
    return msgTime.format('HH:mm')
  } else if (msgTime.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天 ' + msgTime.format('HH:mm')
  } else if (msgTime.isSame(now, 'year')) {
    return msgTime.format('MM-DD HH:mm')
  }
  return msgTime.format('YYYY-MM-DD HH:mm')
}

// 是否显示时间分割线（每5分钟或第一条消息）
const shouldShowTimeDivider = (msg, index) => {
  if (index === 0) return true
  const prevMsg = chatMessages.value[index - 1]
  if (!prevMsg) return true
  const diff = dayjs(msg.time).diff(dayjs(prevMsg.time), 'minute')
  return diff >= 5
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(eventId.value)
    if (res.code === 200) {
      isFavorite.value = res.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 从后端加载聊天记录
const loadChatMessages = async () => {
  try {
    const res = await getChatMessages(eventId.value)
    if (res.code === 200 && res.data) {
      chatMessages.value = res.data.map(msg => ({
        id: msg.id,
        userId: msg.userId,
        nickname: msg.nickname,
        avatar: msg.avatar,
        content: msg.content,
        time: msg.createTime
      }))
    }
  } catch (e) {
    console.error('加载聊天记录失败:', e)
  }
}

// 添加消息（检查重复）
const addChatMessage = (msg) => {
  // 检查是否重复消息（根据时间和内容判断）
  const isDuplicate = chatMessages.value.some(
    m => m.content === msg.content && m.userId === msg.userId && 
         Math.abs(new Date(m.time).getTime() - new Date(msg.time).getTime()) < 2000
  )
  if (!isDuplicate) {
    chatMessages.value.push(msg)
  }
}

// 发送聊天消息
const sendChatMessage = () => {
  if (!chatInput.value.trim()) return
  
  const message = {
    type: 'event_chat',
    eventId: eventId.value,
    content: chatInput.value.trim()
  }
  
  wsStore.send(message)
  
  // 本地添加消息（乐观更新）
  const newMsg = {
    id: Date.now(),
    userId: userStore.userId,
    nickname: userStore.nickname,
    avatar: userStore.avatar,
    content: chatInput.value.trim(),
    time: new Date().toISOString()
  }
  addChatMessage(newMsg)
  
  chatInput.value = ''
  
  // 滚动到底部
  scrollToBottom()
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  }, 100)
}

// 初始化聊天
const initChat = async () => {
  // 从后端加载历史消息
  await loadChatMessages()
  scrollToBottom()
  
  // 订阅事件聊天
  wsStore.subscribeEvent(eventId.value)
  
  // 监听聊天消息
  wsStore.onMessage('event_chat', (data) => {
    if (data.eventId === eventId.value && data.userId !== userStore.userId) {
      const newMsg = {
        id: Date.now(),
        userId: data.userId,
        nickname: data.nickname,
        avatar: data.avatar,
        content: data.content,
        time: data.time || new Date().toISOString()
      }
      addChatMessage(newMsg)
      scrollToBottom()
    }
  })
}

onMounted(async () => {
  // 预加载高德地图API（用于定位功能）
  try {
    await loadAMap()
  } catch (error) {
    console.error('加载地图服务失败:', error)
  }
  
  loadEventDetail()
  loadComments()
  loadCommentCount()
  
  // 加载用户绑定的位置（用于路线导航）
  if (userStore.isLoggedIn) {
    loadUserBindLocation()
  }
  
  if (userStore.isLoggedIn) {
    checkFavoriteStatus()
    // 加载聊天记录并初始化 WebSocket
    await loadChatMessages()
    scrollToBottom()
    if (wsStore.connected) {
      // 订阅事件聊天
      wsStore.subscribeEvent(eventId.value)
      wsStore.onMessage('event_chat', (data) => {
        if (data.eventId === eventId.value && data.userId !== userStore.userId) {
          const newMsg = {
            id: Date.now(),
            userId: data.userId,
            nickname: data.nickname,
            avatar: data.avatar,
            content: data.content,
            time: data.time || new Date().toISOString()
          }
          addChatMessage(newMsg)
          scrollToBottom()
        }
      })
    }
  }
})
</script>

<style scoped>
.event-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.comment-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.comment-item:hover {
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* iMessage/WeChat 风格气泡 */
.chat-bubble {
  border-radius: 18px;
  position: relative;
}

.bubble-right {
  border-top-right-radius: 4px;
}

.bubble-left {
  border-top-left-radius: 4px;
}

/* 气泡尾巴效果 */
.bubble-right::after {
  content: '';
  position: absolute;
  top: 0;
  right: -6px;
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #3b82f6 100%);
  clip-path: polygon(0 0, 100% 0, 0 100%);
  border-radius: 0 4px 0 0;
}

.bubble-left::after {
  content: '';
  position: absolute;
  top: 0;
  left: -6px;
  width: 12px;
  height: 12px;
  background: white;
  clip-path: polygon(100% 0, 100% 100%, 0 0);
  border-radius: 4px 0 0 0;
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>
