export const QuestionTypeMap = {
  0: '选择题',
  1: '填空题',
  2: '计算题',
  3: '判断题',
  4: '简答题',
  5: '证明题'
}

export const getQuestionTypeText = (type) => {
  return QuestionTypeMap[type] || '未知题型'
}

export const getQuestionTypeColor = (type) => {
  const colors = {
    0: 'primary',
    1: 'success',
    2: 'warning',
    3: 'info',
    4: 'danger'
  }
  return colors[type] || 'info'
}

export const getQuestionTypeClass = (type) => {
  const classes = {
    0: 'type-choice',
    1: 'type-fill',
    2: 'type-calc',
    3: 'type-essay',
    4: 'type-proof'
  }
  return classes[type] || 'type-unknown'
}
