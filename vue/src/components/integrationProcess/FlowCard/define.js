
const sourceTypeOptions = [
  { id: 1, fullName: '字段' },
  { id: 2, fullName: '自定义' },
];
const interfaceSourceTypeOptions = [...sourceTypeOptions, { id: 3, fullName: '为空' }];
const triggerRuleTypeOptions = [
  { id: 0, fullName: '无条件' },
  { id: 1, fullName: '按条件' },
];
const msgUserOptions = [
  { id: 1, fullName: '创建人' },
  { id: 2, fullName: '超级管理员' },
  { id: 3, fullName: '自定义' },
];
const noticeOptions = [
  { id: 3, fullName: '默认' },
  { id: 1, fullName: '自定义' },
  { id: 0, fullName: '关闭' },
];
const logicOptions = [
  { id: 'and', fullName: 'and' },
  { id: 'or', fullName: 'or' },
];
const symbolOptions = [
  { id: '>=', fullName: '大于等于' },
  { id: '>', fullName: '大于' },
  { id: '==', fullName: '等于' },
  { id: '<=', fullName: '小于等于' },
  { id: '<', fullName: '小于' },
  { id: '<>', fullName: '不等于' },
  { id: 'like', fullName: '包含' },
  { id: 'notLike', fullName: '不包含' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const baseSymbolOptions = [
  { id: '==', fullName: '等于' },
  { id: '<>', fullName: '不等于' },
  { id: 'like', fullName: '包含' },
  { id: 'notLike', fullName: '不包含' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const rangeSymbolOptions = [
  { id: '>=', fullName: '大于等于' },
  { id: '>', fullName: '大于' },
  { id: '==', fullName: '等于' },
  { id: '<=', fullName: '小于等于' },
  { id: '<', fullName: '小于' },
  { id: '<>', fullName: '不等于' },
  { id: 'between', fullName: '介于' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const switchSymbolOptions = [
  { id: '==', fullName: '等于' },
  { id: '<>', fullName: '不等于' },
];
const selectSymbolOptions = [
  ...switchSymbolOptions,
  { id: 'in', fullName: '包含任意一个' },
  { id: 'notIn', fullName: '不包含任意一个' },
  { id: 'null', fullName: '为空' },
  { id: 'notNull', fullName: '不为空' },
];
const relationFormSymbolOptions = [...switchSymbolOptions, { id: 'null', fullName: '为空' }, { id: 'notNull', fullName: '不为空' }];
const useRangeSymbolList = ['inputNumber', 'calculate', 'rate', 'slider', 'datePicker', 'timePicker', 'createTime', 'modifyTime'];
const useSelectSymbolList = [
  'radio',
  'checkbox',
  'select',
  'treeSelect',
  'cascader',
  'areaSelect',
  'organizeSelect',
  'depSelect',
  'posSelect',
  'userSelect',
  'usersSelect',
  'roleSelect',
  'groupSelect',
  'createUser',
  'modifyUser',
  'currOrganize',
  'currPosition',
  'popupTableSelect',
];
const useSwitchSymbolList = ['switch'];
const useRelationFormSymbolList = ['relationForm', 'popupSelect'];
export { sourceTypeOptions, interfaceSourceTypeOptions, triggerRuleTypeOptions, msgUserOptions, noticeOptions, logicOptions, symbolOptions, baseSymbolOptions, rangeSymbolOptions, selectSymbolOptions, switchSymbolOptions, relationFormSymbolOptions, useRangeSymbolList, useSelectSymbolList, useSwitchSymbolList, useRelationFormSymbolList };
