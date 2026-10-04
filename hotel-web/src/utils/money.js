// 金额口径：接口与组件数据一律"分"整数，禁止在前端做金额换算与浮点运算；
// 唯一的分→元格式化出口，仅允许在模板渲染处调用
export function formatYuan(fen) {
  return (fen / 100).toFixed(2)
}
