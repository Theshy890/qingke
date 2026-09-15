import ExcelJS from 'exceljs'
import { saveAs } from 'file-saver'

/**
 * 使用 ExcelJS 导出 Excel 文件
 * @param {Array} data - 要导出的数据数组
 * @param {String} filename - 文件名（不含扩展名）
 * @param {Object} config - 配置对象
 * @param {Array} config.columns - 列配置 [{header: '列名', key: '字段名', width: 宽度}]
 * @param {String} config.sheetName - 工作表名称
 */
export async function exportToExcel(data, filename, config = {}) {
  const {
    columns = [],
    sheetName = 'Sheet1'
  } = config

  try {
    // 创建工作簿
    const workbook = new ExcelJS.Workbook()
    
    // 设置工作簿属性
    workbook.creator = 'Qingke System'
    workbook.lastModifiedBy = 'Qingke System'
    workbook.created = new Date()
    workbook.modified = new Date()
    
    // 添加工作表
    const worksheet = workbook.addWorksheet(sheetName)
    
    // 设置列
    worksheet.columns = columns.map(col => ({
      header: col.header,
      key: col.key,
      width: col.width || 15
    }))
    
    // 设置表头样式（简化样式，避免兼容性问题）
    const headerRow = worksheet.getRow(1)
    headerRow.height = 25
    headerRow.font = { bold: true, size: 11, color: { argb: 'FFFFFFFF' } }
    headerRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF4472C4' }
    }
    headerRow.alignment = { vertical: 'middle', horizontal: 'center' }
    
    // 添加数据行
    data.forEach((item, index) => {
      const rowData = {}
      columns.forEach(col => {
        let value = item[col.key]
        
        // 处理图片链接和其他长URL
        if (col.key === '图片链接' || col.key === '绿植图片') {
          if (value && value !== '-' && value.length > 50) {
            // 截取显示前50个字符
            rowData[col.key] = value.substring(0, 50) + '...'
          } else {
            rowData[col.key] = value || '-'
          }
        } else {
          // 确保值不为 undefined 或 null
          rowData[col.key] = value !== undefined && value !== null ? String(value) : '-'
        }
      })
      
      const row = worksheet.addRow(rowData)
      row.height = 20
      
      // 设置数据行样式
      row.alignment = { vertical: 'middle', horizontal: 'left', wrapText: true }
      
      // 交替行颜色
      if (index % 2 === 1) {
        row.fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: 'FFF5F5F5' }
        }
      }
      
      // 为图片链接列添加超链接（如果是完整URL）
      columns.forEach((col, colIndex) => {
        if ((col.key === '图片链接' || col.key === '绿植图片') && item[col.key] && item[col.key] !== '-') {
          const cell = row.getCell(colIndex + 1)
          const url = item[col.key]
          
          // 只为有效的URL添加超链接
          if (url.startsWith('http://') || url.startsWith('https://')) {
            cell.value = {
              text: '查看图片',
              hyperlink: url
            }
            cell.font = { color: { argb: 'FF0000FF' }, underline: true }
          }
        }
      })
    })
    
    // 添加边框
    worksheet.eachRow((row, rowNumber) => {
      row.eachCell((cell) => {
        cell.border = {
          top: { style: 'thin', color: { argb: 'FFD0D0D0' } },
          left: { style: 'thin', color: { argb: 'FFD0D0D0' } },
          bottom: { style: 'thin', color: { argb: 'FFD0D0D0' } },
          right: { style: 'thin', color: { argb: 'FFD0D0D0' } }
        }
      })
    })
    
    // 冻结首行
    worksheet.views = [
      { state: 'frozen', ySplit: 1 }
    ]
    
    // 生成文件
    const buffer = await workbook.xlsx.writeBuffer()
    
    // 创建 Blob
    const blob = new Blob([buffer], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    // 下载文件
    const timestamp = new Date().getTime()
    const safeFilename = filename.replace(/[<>:"/\\|?*]/g, '_')
    saveAs(blob, `${safeFilename}_${timestamp}.xlsx`)
    
  } catch (error) {
    console.error('导出Excel失败:', error)
    throw error
  }
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date) {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期
 */
export function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}