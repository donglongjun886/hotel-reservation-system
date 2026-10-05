// 金额口径：接口与组件数据一律"分"整数，禁止在前端做金额换算与浮点运算；
// 唯一的分→元格式化出口，仅允许在模板渲染处调用
export function formatYuan(fen) {
  return (fen / 100).toFixed(2)
}

// 表单输入的"元"→接口"分"，唯一的元→分换算出口，仅允许在提交接口前调用
export function yuanToFen(yuan) {
  return Math.round(Number(yuan) * 100)
}

// 接口"分"→表单回填的"元"（Number），唯一的分→元换算出口，仅允许在表单回填处调用
export function fenToYuan(fen) {
  return Math.round(fen) / 100
}
