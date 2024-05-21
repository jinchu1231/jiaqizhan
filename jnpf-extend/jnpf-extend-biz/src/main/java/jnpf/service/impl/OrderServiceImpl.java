package jnpf.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.UserInfo;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.entity.OrderEntity;
import jnpf.entity.OrderEntryEntity;
import jnpf.entity.OrderReceivableEntity;
import jnpf.exception.DataException;
import jnpf.mapper.OrderMapper;
import jnpf.model.order.OrderEntryModel;
import jnpf.model.order.OrderForm;
import jnpf.model.order.OrderInfoOrderEntryModel;
import jnpf.model.order.OrderInfoOrderReceivableModel;
import jnpf.model.order.OrderInfoVO;
import jnpf.model.order.OrderReceivableModel;
import jnpf.model.order.PaginationOrder;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.OrderEntryService;
import jnpf.service.OrderReceivableService;
import jnpf.service.OrderService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:19
 */
@Service
@DSTransactional
public class OrderServiceImpl extends SuperServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrderReceivableService orderReceivableService;
    @Autowired
    private OrderEntryService orderEntryService;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private UserService userService;

    /**
     * 前单
     **/
    private static String PREV = "prev";
    /**
     * 后单
     **/
    private static String NEXT = "next";

    @Override
    public List<OrderEntity> getList(PaginationOrder paginationOrder) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        //关键字（订单编码、客户名称、业务人员）
        String keyWord = paginationOrder.getKeyword() != null ? paginationOrder.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            String word = keyWord;
            queryWrapper.lambda().and(
                    t -> t.like(OrderEntity::getOrderCode, word)
                            .or().like(OrderEntity::getCustomerName, word)
                            .or().like(OrderEntity::getSalesmanName, word)
            );
        }
        //起始日期-结束日期
        Long startTime = paginationOrder.getStartTime() != null ? paginationOrder.getStartTime() : 0;
        Long endTime = paginationOrder.getEndTime() != null ? paginationOrder.getEndTime() : 0;
//        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
        if (startTime > 0 && endTime > 0) {
            Date startTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(startTime) + " 00:00:00");
            Date endTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(endTime) + " 23:59:59");
            queryWrapper.lambda().ge(OrderEntity::getOrderDate, startTimes).le(OrderEntity::getOrderDate, endTimes);
        }
        //订单状态
        String mark = paginationOrder.getEnabledMark() != null ? paginationOrder.getEnabledMark() : null;
        if (!StringUtils.isEmpty(mark)) {
            queryWrapper.lambda().eq(OrderEntity::getEnabledMark, mark);
        }
        //排序
        if (StringUtils.isEmpty(paginationOrder.getSidx())) {
            queryWrapper.lambda().orderByDesc(OrderEntity::getCreatorTime);
        } else {
            queryWrapper = "asc".equals(paginationOrder.getSort().toLowerCase()) ? queryWrapper.orderByAsc(paginationOrder.getSidx()) : queryWrapper.orderByDesc(paginationOrder.getSidx());
        }
        Page<OrderEntity> page = new Page<>(paginationOrder.getCurrentPage(), paginationOrder.getPageSize());
        IPage<OrderEntity> orderEntityPage = this.page(page, queryWrapper);
        List<OrderEntity> data = orderEntityPage.getRecords();
        return paginationOrder.setData(data, page.getTotal());
    }

    @Override
    public List<OrderEntryEntity> getOrderEntryList(String id) {
        QueryWrapper<OrderEntryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrderEntryEntity::getOrderId, id).orderByDesc(OrderEntryEntity::getSortCode);
        return orderEntryService.list(queryWrapper);
    }

    @Override
    public List<OrderReceivableEntity> getOrderReceivableList(String id) {
        QueryWrapper<OrderReceivableEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrderReceivableEntity::getOrderId, id).orderByDesc(OrderReceivableEntity::getSortCode);
        return orderReceivableService.list(queryWrapper);
    }

    @Override
    public OrderEntity getPrevOrNextInfo(String id, String method) {
        QueryWrapper<OrderEntity> result = new QueryWrapper<>();
        OrderEntity orderEntity = getInfo(id);
        String orderBy = "desc";
        if (PREV.equals(method)) {
            result.lambda().gt(OrderEntity::getCreatorTime, orderEntity.getCreatorTime());
            orderBy = "";
        } else if (NEXT.equals(method)) {
            result.lambda().lt(OrderEntity::getCreatorTime, orderEntity.getCreatorTime());
        }
        result.lambda().notIn(OrderEntity::getId, orderEntity.getId());
        if (StringUtil.isNotEmpty(orderBy)) {
            result.lambda().orderByDesc(OrderEntity::getCreatorTime);
        }
        List<OrderEntity> data = this.list(result);
        if (data.size() > 0) {
            return data.get(0);
        }
        return null;
    }

    @Override
    public OrderInfoVO getInfoVo(String id, String method) throws DataException {
        OrderInfoVO infoModel = null;
        OrderEntity orderEntity = this.getPrevOrNextInfo(id, method);
        if (orderEntity != null) {
            List<OrderEntryEntity> orderEntryList = this.getOrderEntryList(orderEntity.getId());
            List<OrderReceivableEntity> orderReceivableList = this.getOrderReceivableList(orderEntity.getId());
            infoModel = JsonUtilEx.getJsonToBeanEx(orderEntity, OrderInfoVO.class);
            UserEntity createUser = null;
            if (StringUtil.isNotEmpty(infoModel.getCreatorUserId())) {
                createUser = userService.getInfo(infoModel.getCreatorUserId());
            }
            infoModel.setCreatorUserId(createUser != null ? createUser.getRealName() + "/" + createUser.getAccount() : "");
            UserEntity lastUser = null;
            if (StringUtil.isNotEmpty(infoModel.getLastModifyUserId())) {
                lastUser = userService.getInfo(infoModel.getLastModifyUserId());
            }
            infoModel.setLastModifyUserId(lastUser != null ? lastUser.getRealName() + "/" + lastUser.getAccount() : "");
            List<OrderInfoOrderEntryModel> orderEntryModels = JsonUtil.getJsonToList(orderEntryList, OrderInfoOrderEntryModel.class);
            infoModel.setGoodsList(orderEntryModels);
            List<OrderInfoOrderReceivableModel> orderReceivableModels = JsonUtil.getJsonToList(orderReceivableList, OrderInfoOrderReceivableModel.class);
            infoModel.setCollectionPlanList(orderReceivableModels);
        }
        return infoModel;
    }

    @Override
    public OrderEntity getInfo(String id) {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrderEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(OrderEntity entity) {
        QueryWrapper<OrderEntity> orderWrapper = new QueryWrapper<>();
        orderWrapper.lambda().eq(OrderEntity::getId, entity.getId());
        this.remove(orderWrapper);
        QueryWrapper<OrderEntryEntity> entryWrapper = new QueryWrapper<>();
        entryWrapper.lambda().eq(OrderEntryEntity::getOrderId, entity.getId());
        orderEntryService.remove(entryWrapper);
        QueryWrapper<OrderReceivableEntity> receivableWrapper = new QueryWrapper<>();
        receivableWrapper.lambda().eq(OrderReceivableEntity::getOrderId, entity.getId());
        orderReceivableService.remove(receivableWrapper);
    }

    @Override
    @DSTransactional
    public void create(OrderEntity entity, List<OrderEntryEntity> orderEntryList, List<OrderReceivableEntity> orderReceivableList) {
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setEnabledMark(1);
        for (int i = 0; i < orderEntryList.size(); i++) {
            orderEntryList.get(i).setId(RandomUtil.uuId());
            orderEntryList.get(i).setOrderId(entity.getId());
            orderEntryList.get(i).setSortCode(Long.parseLong(i + ""));
            orderEntryService.save(orderEntryList.get(i));
        }
        for (int i = 0; i < orderReceivableList.size(); i++) {
            orderReceivableList.get(i).setId(RandomUtil.uuId());
            orderReceivableList.get(i).setOrderId(entity.getId());
            orderReceivableList.get(i).setSortCode(Long.parseLong(i + ""));
            orderReceivableService.save(orderReceivableList.get(i));
        }
        billRuleService.useBillNumber("OrderNumber");
        this.save(entity);
    }

    @Override
    @DSTransactional
    public boolean update(String id, OrderEntity entity, List<OrderEntryEntity> orderEntryList, List<OrderReceivableEntity> orderReceivableList) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        QueryWrapper<OrderEntryEntity> entryWrapper = new QueryWrapper<>();
        entryWrapper.lambda().eq(OrderEntryEntity::getOrderId, entity.getId());
        orderEntryService.remove(entryWrapper);
        QueryWrapper<OrderReceivableEntity> receivableWrapper = new QueryWrapper<>();
        receivableWrapper.lambda().eq(OrderReceivableEntity::getOrderId, entity.getId());
        orderReceivableService.remove(receivableWrapper);
        for (int i = 0; i < orderEntryList.size(); i++) {
            orderEntryList.get(i).setId(RandomUtil.uuId());
            orderEntryList.get(i).setOrderId(entity.getId());
            orderEntryList.get(i).setSortCode(Long.parseLong(i + ""));
            orderEntryService.save(orderEntryList.get(i));
        }
        for (int i = 0; i < orderReceivableList.size(); i++) {
            orderReceivableList.get(i).setId(RandomUtil.uuId());
            orderReceivableList.get(i).setOrderId(entity.getId());
            orderReceivableList.get(i).setSortCode(Long.parseLong(i + ""));
            orderReceivableService.save(orderReceivableList.get(i));
        }
        boolean flag = this.updateById(entity);
        return flag;
    }

    @Override
    public void data(String id, String data) {
        OrderForm orderForm = JsonUtil.getJsonToBean(data, OrderForm.class);
        OrderEntity entity = JsonUtil.getJsonToBean(orderForm, OrderEntity.class);
        List<OrderEntryModel> goodsList = orderForm.getGoodsList() != null ? orderForm.getGoodsList() : new ArrayList<>();
        List<OrderEntryEntity> orderEntryList = JsonUtil.getJsonToList(goodsList, OrderEntryEntity.class);
        List<OrderReceivableModel> collectionPlanList = orderForm.getCollectionPlanList() != null ? orderForm.getCollectionPlanList() : new ArrayList<>();
        List<OrderReceivableEntity> orderReceivableList = JsonUtil.getJsonToList(collectionPlanList, OrderReceivableEntity.class);
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        QueryWrapper<OrderEntryEntity> entryWrapper = new QueryWrapper<>();
        entryWrapper.lambda().eq(OrderEntryEntity::getOrderId, entity.getId());
        orderEntryService.remove(entryWrapper);
        QueryWrapper<OrderReceivableEntity> receivableWrapper = new QueryWrapper<>();
        receivableWrapper.lambda().eq(OrderReceivableEntity::getOrderId, entity.getId());
        orderReceivableService.remove(receivableWrapper);
        for (int i = 0; i < orderEntryList.size(); i++) {
            orderEntryList.get(i).setId(RandomUtil.uuId());
            orderEntryList.get(i).setOrderId(entity.getId());
            orderEntryList.get(i).setSortCode(Long.parseLong(i + ""));
            orderEntryService.save(orderEntryList.get(i));
        }
        for (int i = 0; i < orderReceivableList.size(); i++) {
            orderReceivableList.get(i).setId(RandomUtil.uuId());
            orderReceivableList.get(i).setOrderId(entity.getId());
            orderReceivableList.get(i).setSortCode(Long.parseLong(i + ""));
            orderReceivableService.save(orderReceivableList.get(i));
        }
        this.saveOrUpdate(entity);
    }


}
