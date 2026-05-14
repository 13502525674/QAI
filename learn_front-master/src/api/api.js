import request, { post, get } from "@/utils/request";

// export function products(params) {
//     return request({
//       url: "/api/v1/products",  //接口路径
//       method: "get",  //接口方法
//       headers: { 'Content-Type': 'multipart/form-data' }, //给接口添加请求头
//       params  //接口参数
//     });
// }

//-------------------------------登陆---------------------------------------
// 登陆
export const login = (params) => post("/login",params)
//登出
export const logout = () => get("/login/logout")
//用户注册
export const register = (params) => post("/login/register",params)
//获取登陆用户信息
export const getUser = () => get("/user/getUserInfo")
//获取学校列表
export const getApeSchoolList = () => get("/school/getApeSchoolList")
//获取专业列表
export const getApeMajorList = () => get("/major/getApeMajorList")
//主页数据
export const getIndexAchievement = () => get("/index/getIndexAchievement")
//主页获取笔记
export const getIndexArticleList = () => get("/article/getIndexArticleList")
//分页获取课程
export const getApeTaskPage = (params) => post("/task/getApeTaskPage",params)
//获取分类列表 
export const getApeClassificationList = () => get("/classification/getApeClassificationList")
//获取教师分页
export const getUserPage = (params) => post("/user/getUserPage",params)
//分页获取笔记
export const getApeArticlePage = (params) => post("/article/getApeArticlePage",params)
//分页获取公告
export const getAccountPage = (params) => post("/account/getAccountPage",params)
//分页获取留言
export const getApeMessagePage = (params) => post("/message/getApeMessagePage",params)
//保存留言
export const saveApeMessage = (params) => post("/message/saveApeMessage",params)
//获取个人信息
export const getUserInfo = () => get("/user/getUserInfo")
//保存个人信息
export const setUserInfo = (params) => post("/user/setUserInfo",params)
//保存头像
export const setUserAvatar = (params) => post("/user/setUserAvatar",params)
//修改密码
export const changePassword = (params) => post("/user/changePassword",params)
//获取课程信息
export const getApeTaskById = (params) => get("/task/getApeTaskById",params)
//保存课程报名
export const saveApeTaskStudent = (params) => post("/student/saveApeTaskStudent",params)
//获取用户报名情况
export const getTaskStudent = (params) => get("/student/getTaskStudent",params)
//根据课程id获取章节
export const getApeChapterByTaskId = (params) => get("/chapter/getApeChapterByTaskId",params)
//保存课程评论
export const saveApeTaskComment = (params) => post("/comment/saveApeTaskComment",params)
//获取课程评论
export const getApeTaskCommentListByTaskId = (params) => get("/comment/getApeTaskCommentListByTaskId",params)
//获取教师课程列表
export const getApeTaskByTeacher = (params) => get("/task/getApeTaskByTeacher",params)
//获取当前教师的课程列表
export const getApeTaskByTeacherId = () => get("/task/getApeTaskByTeacherId")
//获取教师详情
export const getUserById = (params) => get("/user/getUserById",params)
//获取笔记详情
export const getApeArticleById = (params) => get("/article/getApeArticleById",params)
//保存笔记评论
export const saveApeArticleComment = (params) => post("/articleComment/saveApeArticleComment",params)
//获取笔记评论
export const getApeArticleCommentByArticleId = (params) => get("/articleComment/getApeArticleCommentByArticleId",params)
//查询笔记收藏
export const getApeArticleFavorPage = (params) => post("/favor/getApeArticleFavorPage",params)
//根据id查询笔记收藏
export const getApeArticleFavorById = (params) => get("/favor/getApeArticleFavorById",params)
//保存笔记收藏
export const saveApeArticleFavor = (params) => post("/favor/saveApeArticleFavor",params)
//更新笔记收藏
export const editApeArticleFavor = (params) => post("/favor/editApeArticleFavor",params)
//删除ApeArticleFavor
export const removeApeArticleFavor = (params) => post("/favor/removeApeArticleFavor",params)
//我的课程
export const getApeMyTaskPage = (params) => post("/student/getApeMyTaskPage",params)
//我的课程列表 
export const getApeMyTaskList = (params) => post("/student/getApeMyTaskList",params)
//保存笔记
export const saveApeArticle = (params) => post("/article/saveApeArticle",params)
//编辑笔记
export const editApeArticle = (params) => post("/article/editApeArticle",params)
//删除笔记
export const removeApeArticle = (params) => get("/article/removeApeArticle",params)
//根据id获取章节
export const getApeChapterById = (params) => get("/chapter/getApeChapterById",params)

//保存章节视频是否观看
export const saveApeChapterVideo = (params) => post("/chapterVideo/saveApeChapterVideo",params)
//后去考试列表
export const getTestListByUser = (params) => get("/test/getTestListByUser",params)
//查询用户考试题目
export const getApeTestStudentPage = (params) => post("/student/getApeTestStudentPage",params)
//根据id查询用户考试题目
export const getApeTestStudentById = (params) => get("/student/getApeTestStudentById",params)
//更新用户考试题目
export const editApeTestStudent = (params) => post("/student/editApeTestStudent",params)
//删除ApeTestStudent
export const removeApeTestStudent = (params) => get("/student/removeApeTestStudent",params)
//获取考试题目
export const getApeTestItemByTestId = (params) => get("/item/getApeTestItemByTestId",params)
//保存考试内容
export const saveApeTestStudent = (params) => post("/student/saveApeTestStudent",params)
//获取考试状态
export const getTestUserState = (params) => get("/student/getTestUserState",params)
export const getTaskChapterStudy = (params) => get("/chapter/getTaskChapterStudy",params)

//-------------------------------论坛---------------------------------------
//查询论坛
export const getApeForumPage = (params) => post("/forum/getApeForumPage",params)
//根据id查询论坛
export const getApeForumById = (params) => get("/forum/getApeForumById",params)
//保存论坛
export const saveApeForum = (params) => post("/forum/saveApeForum",params)
//更新论坛
export const editApeForum = (params) => post("/forum/editApeForum",params)
//删除ApeForum
export const removeApeForum = (params) => get("/forum/removeApeForum",params)

//-------------------------------论坛讨论---------------------------------------
//查询论坛讨论
export const getApeForumItemPage = (params) => post("/item/getApeForumItemPage",params)
export const getApeForumItemList = (params) => post("/item/getApeForumItemList",params)
//根据id查询论坛讨论
export const getApeForumItemById = (params) => get("/item/getApeForumItemById",params)
//保存论坛讨论
export const saveApeForumItem = (params) => post("/item/saveApeForumItem",params)
//更新论坛讨论
export const editApeForumItem = (params) => post("/item/editApeForumItem",params)
//删除ApeForumItem
export const removeApeForumItem = (params) => get("/item/removeApeForumItem",params)

//-------------------------------答疑---------------------------------------
//查询答疑
export const getApeQuestionPage = (params) => post("/question/getApeQuestionPage",params)
//根据id查询答疑
export const getApeQuestionById = (params) => get("/question/getApeQuestionById",params)
//保存答疑
export const saveApeQuestion = (params) => post("/question/saveApeQuestion",params)
//更新答疑
export const editApeQuestion = (params) => post("/question/editApeQuestion",params)
//删除ApeQuestion
export const removeApeQuestion = (params) => get("/question/removeApeQuestion",params)

export const getApeTaskFavorById = (params) => get("/favor/getApeTaskFavorById",params)

export const removeApeTaskFavor = (params) => get("/favor/removeApeTaskFavor",params)

export const saveApeTaskFavor = (params) => post("/favor/saveApeTaskFavor",params)

export const getApeTaskFavorPage = (params) => post("/favor/getApeTaskFavorPage",params)

//-------------------------------物理题库---------------------------------------
// 获取物理试卷分页
export const getPhysicsQuestionPaperPage = (params) => post("/physics/questionPaper/getPage",params)
// 根据id获取物理试卷
export const getPhysicsQuestionPaperById = (params) => get("/physics/questionPaper/getById",params)
// 保存物理试卷
export const savePhysicsQuestionPaper = (params) => post("/physics/questionPaper/save",params)
// 编辑物理试卷
export const editPhysicsQuestionPaper = (params) => post("/physics/questionPaper/edit",params)
// 删除物理试卷
export const removePhysicsQuestionPaper = (params) => get("/physics/questionPaper/remove",params)
// 获取用户可做的物理试卷
export const getPhysicsUserPapers = () => get("/physics/questionPaper/getUserPapers")
// 按物理分支获取试卷
export const getPhysicsPapersByBranch = (params) => get("/physics/questionPaper/getPapersByBranch",params)

// 获取物理练习记录分页
export const getPhysicsPracticeRecordsPage = (params) => post("/physics/practiceRecords/getPage",params)
// 根据id获取物理练习记录
export const getPhysicsPracticeRecordsById = (params) => get("/physics/practiceRecords/getById",params)
// 提交物理练习
export const submitPhysicsPractice = (params) => post("/physics/practiceRecords/submitPractice",params)
// 评分物理练习
export const gradePhysicsPractice = (params) => post("/physics/practiceRecords/gradePractice",params)
// 获取用户物理练习记录
export const getPhysicsUserRecords = (params) => get("/physics/practiceRecords/getUserRecords",params)
// 获取用户物理练习记录（包含试卷信息）
export const getPhysicsUserRecordsWithDetails = (params) => get("/physics/practiceRecords/getUserRecordsWithDetails",params)
// 获取待评分的练习记录
export const getPhysicsPendingGrading = () => get("/physics/practiceRecords/getPendingGrading",params)
// 获取教师批改列表
export const getPhysicsGradingList = (params) => post("/physics/practiceRecords/getGradingList",params)
// 更新练习记录评分
export const updatePhysicsPracticeRecord = (params) => post("/physics/practiceRecords/updatePracticeRecord",params)

//-------------------------------物理题库文档解析---------------------------------------
// 解析物理题库文档
export const parsePhysicsDocument = (formData) => postFormData("/physics/document/parse", formData)
// 解析并保存物理题库文档为试卷
export const parseAndSavePhysicsDocument = (formData) => postFormData("/physics/document/parseAndSave", formData)

//-------------------------------知识图谱---------------------------------------
// 获取知识点分页
export const getKnowledgePointPage = (params) => post("/knowledge/getPage",params)
// 获取知识点列表
export const getKnowledgePointList = () => get("/knowledge/getList")
// 根据ID获取知识点
export const getKnowledgePointById = (params) => get("/knowledge/getById",params)
// 保存知识点
export const saveKnowledgePoint = (params) => post("/knowledge/save",params)
// 编辑知识点
export const editKnowledgePoint = (params) => post("/knowledge/edit",params)
// 删除知识点
export const removeKnowledgePoint = (params) => get("/knowledge/remove",params)
// 根据分支获取知识点
export const getKnowledgePointByBranch = (params) => get("/knowledge/getByBranch",params)
// 获取知识点树
export const getKnowledgePointTree = () => get("/knowledge/getTree")
// 获取知识图谱数据
export const getKnowledgeGraphData = () => get("/knowledgeGraph/getGraphData")
// 获取知识点详情
export const getKnowledgePointDetail = (params) => get("/knowledgeGraph/getDetail",params)
// 获取学习路径
export const getLearningPath = (params) => get("/knowledgeGraph/getLearningPath",params)

//-------------------------------知识点关系---------------------------------------
// 获取知识点关系列表
export const getKnowledgeRelationList = () => get("/knowledgeRelation/getAllWithNames")
// 根据源知识点获取关系
export const getKnowledgeRelationByFrom = (params) => get("/knowledgeRelation/getByFromKpId",params)
// 保存知识点关系
export const saveKnowledgeRelation = (params) => post("/knowledgeRelation/save",params)
// 删除知识点关系
export const removeKnowledgeRelation = (params) => get("/knowledgeRelation/remove",params)

//-------------------------------学生掌握度---------------------------------------
// 获取学生雷达图数据
export const getStudentRadarData = (studentId) => get(`/api/student/radar/${studentId}`)
// 获取当前学生雷达图数据
export const getMyRadarData = () => get("/api/student/radar/my")
// 获取学生各分支掌握度
export const getStudentMasteryByBranch = (studentId) => get(`/api/student/mastery/branch/${studentId}`)
// 获取学生薄弱知识点
export const getStudentWeakPoints = (studentId, limit = 5) => get(`/api/student/weak/${studentId}`, { limit })
// 获取学生优势知识点
export const getStudentStrongPoints = (studentId, limit = 3) => get(`/api/student/strong/${studentId}`, { limit })
// 重新计算学生掌握度
export const recalculateStudentMastery = (studentId) => post(`/api/student/mastery/recalculate/${studentId}`)

//-------------------------------AI个性化推荐---------------------------------------
// 获取学生画像数据
export const getStudentProfile = (studentId) => get(`/api/ai/profile/${studentId}`)
// 构建带学生画像的Prompt
export const buildPromptWithProfile = (studentId, userMessage) => post("/api/ai/buildPrompt", null, { params: { studentId, userMessage } })

function postFormData(url, formData) {
  return request({
    url: url,
    method: "post",
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

//-------------------------------趣味物理模块---------------------------------------
// 获取趣味问题列表
export const getFunPhysicsQuestions = (params) => get("/funphysics/question/getRandom", params)
// 获取趣味问题详情
export const getFunPhysicsQuestionById = (params) => get("/funphysics/question/getById", params)
// 提交趣味问题答案
export const submitFunPhysicsAnswer = (params) => post("/funphysics/question/submitAnswer", params)
// 获取用户答题记录
export const getFunPhysicsAnswerRecords = (params) => get("/funphysics/question/getUserStats", params)
// 获取每日挑战问题
export const getFunPhysicsDailyChallenge = () => get("/funphysics/question/getDailyChallenge")

// 获取朋友圈列表
export const getFunPhysicsMomentsList = (params) => get("/funphysics/moments/getRandom", params)
// 获取朋友圈详情
export const getFunPhysicsMomentsById = (params) => get("/funphysics/moments/getById", params)
// 获取推荐朋友圈
export const getFunPhysicsRecommendMoments = () => get("/funphysics/moments/getRecommend")
// 获取热门朋友圈
export const getFunPhysicsHotMoments = (params) => get("/funphysics/moments/getRandom", params)
// 点赞朋友圈
export const likeFunPhysicsMoments = (params) => post("/funphysics/moments/like", params)
// 获取朋友圈评论
export const getFunPhysicsMomentsComments = (params) => get("/funphysics/moments/getComments", params)
// 添加朋友圈评论
export const addFunPhysicsMomentsComment = (params) => post("/funphysics/moments/comment", params)

// 获取物理拼图列表
export const getFunPhysicsPuzzles = (params) => get("/funphysics/puzzle/getRandom", params)
// 获取拼图详情
export const getFunPhysicsPuzzleById = (params) => get("/funphysics/puzzle/getById", params)
// 开始拼图
export const startFunPhysicsPuzzle = (params) => post("/funphysics/puzzle/start", params)
// 提交拼图答案
export const submitFunPhysicsPuzzle = (params) => post("/funphysics/puzzle/complete", params)
// 获取用户拼图记录
export const getFunPhysicsPuzzleRecords = (params) => get("/funphysics/puzzle/getUserStats", params)
// 获取拼图图片
export const getFunPhysicsPuzzleImage = (params) => get("/funphysics/puzzle/getImage", params)

// 获取公开画廊
export const getFunPhysicsGallery = (params) => get("/funphysics/image/getGallery", params)
// 获取公开图片列表
export const getFunPhysicsPublicImages = (params) => get("/funphysics/image/getHot", params)
// 获取热门图片
export const getFunPhysicsHotImages = (params) => get("/funphysics/image/getHot", params)
// 获取用户图片
export const getFunPhysicsUserImages = () => get("/funphysics/image/getUserImages")
// 获取图片详情
export const getFunPhysicsImageById = (params) => get("/funphysics/image/getById", params)
// 保存生成的图片
export const saveFunPhysicsImage = (params) => post("/funphysics/image/save", params)
// 更新图片公开状态
export const updateFunPhysicsImagePublic = (params) => post("/funphysics/image/updatePublic", params)
// 点赞图片
export const likeFunPhysicsImage = (params) => post("/funphysics/image/like", params)
// 删除图片
export const removeFunPhysicsImage = (params) => get("/funphysics/image/remove", params)
// 生成图片
export const generateFunPhysicsImage = (params) => post("/funphysics/text2image/generate", params)
// 生成图片提示词
export const generateFunPhysicsPrompt = (params) => post("/funphysics/text2image/generatePrompt", params)