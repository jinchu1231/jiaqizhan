package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.PageModel;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.mapper.IMContentMapper;
import jnpf.message.model.ImReplySavaModel;
import jnpf.message.model.ImUnreadNumModel;
import jnpf.message.model.imrecord.PaginationImRecordModel;
import jnpf.message.service.ImContentService;
import jnpf.message.service.ImReplyService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * 聊天内容
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ImContentServiceImpl extends SuperServiceImpl<IMContentMapper, ImContentEntity> implements ImContentService {
    @Autowired
    private ImReplyService imReplyService;

    @Override
    public List<ImContentEntity> getMessageList(String sendUserId, String receiveUserId, PageModel pageModel) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        //发件人、收件人
        if (!StringUtil.isEmpty(sendUserId) && !StringUtil.isEmpty(receiveUserId)) {
            queryWrapper.lambda().and(wrapper -> {
                wrapper.eq(ImContentEntity::getSendUserId, sendUserId);
                wrapper.eq(ImContentEntity::getReceiveUserId, receiveUserId);
                wrapper.or().eq(ImContentEntity::getSendUserId, receiveUserId);
                wrapper.eq(ImContentEntity::getReceiveUserId, sendUserId);
            });
            queryWrapper.lambda().and(wrapper -> {
                wrapper.isNull(ImContentEntity::getDeleteUserId);
                wrapper.or().ne(ImContentEntity::getDeleteUserId,receiveUserId);
//                wrapper.ne(ImContentEntity::getDeleteMark, 1);
            });
        }
        //关键字查询
        if (pageModel != null && pageModel.getKeyword() != null) {
            queryWrapper.lambda().like(ImContentEntity::getContent, pageModel.getKeyword());
        }
//        pageModel.setSidx("F_SendTime");
//        if (StringUtil.isEmpty(pageModel.getSidx())) {
//            queryWrapper.lambda().orderByDesc(ImContentEntity::getSendTime);
//        } else {
//            queryWrapper = "asc".equals(pageModel.getSord().toLowerCase()) ? queryWrapper.orderByAsc(pageModel.getSidx()) : queryWrapper.orderByDesc(pageModel.getSidx());
//        }
        //排序
        queryWrapper.lambda().orderByDesc(ImContentEntity::getSendTime);
        Page<ImContentEntity> page = new Page<>(pageModel.getPage(), pageModel.getRows());
        IPage<ImContentEntity> userIPage = this.page(page, queryWrapper);
        return pageModel.setData(userIPage.getRecords(), page.getTotal());
    }

    @Override
    public List<ImContentEntity> getMessageList(PaginationImRecordModel imRecordModel) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        //发件人、收件人
        if (!StringUtil.isEmpty(imRecordModel.getToUserId()) && !StringUtil.isEmpty(imRecordModel.getFormUserId())) {
            queryWrapper.lambda().and(wrapper -> {
                wrapper.eq(ImContentEntity::getSendUserId, imRecordModel.getToUserId());
                wrapper.eq(ImContentEntity::getReceiveUserId, imRecordModel.getFormUserId());
                wrapper.or().eq(ImContentEntity::getSendUserId, imRecordModel.getFormUserId());
                wrapper.eq(ImContentEntity::getReceiveUserId, imRecordModel.getToUserId());
            });
        }
        //关键字查询
        if (imRecordModel != null && imRecordModel.getKeyword() != null) {
            queryWrapper.lambda().like(ImContentEntity::getContent, imRecordModel.getKeyword());
        }
        //排序
        imRecordModel.setSidx("F_SendTime");
        if (StringUtil.isEmpty(imRecordModel.getSidx())) {
            queryWrapper.lambda().orderByDesc(ImContentEntity::getSendTime);
        } else {
            queryWrapper = "asc".equals(imRecordModel.getSort().toLowerCase()) ? queryWrapper.orderByAsc(imRecordModel.getSort()) : queryWrapper.orderByDesc(imRecordModel.getSidx());
        }
//        queryWrapper.lambda().orderByDesc(ImContentEntity::getSendTime);
        Page<ImContentEntity> page = new Page<>(imRecordModel.getCurrentPage(), imRecordModel.getPageSize());
        IPage<ImContentEntity> userIPage = this.page(page, queryWrapper);
        return imRecordModel.setData(userIPage.getRecords(), page.getTotal());
    }

    @Override
    public List<ImUnreadNumModel> getUnreadList(String receiveUserId) {
        List<ImUnreadNumModel> list = this.baseMapper.getUnreadList(receiveUserId);
        List<ImUnreadNumModel> list1 = this.baseMapper.getUnreadLists(receiveUserId);
        for (ImUnreadNumModel item : list) {
            ImUnreadNumModel defaultItem = list1.stream().filter(q -> q.getSendUserId().equals(item.getSendUserId())).findFirst().get();
            item.setDefaultMessage(defaultItem.getDefaultMessage());
            item.setDefaultMessageType(defaultItem.getDefaultMessageType());
            item.setDefaultMessageTime(defaultItem.getDefaultMessageTime());
        }
        return list;
    }

    @Override
    public int getUnreadCount(String sendUserId, String receiveUserId) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImContentEntity::getSendUserId, sendUserId).eq(ImContentEntity::getReceiveUserId, receiveUserId).eq(ImContentEntity::getEnabledMark, 0);
        return (int)this.count(queryWrapper);
    }

    @Override
    public void sendMessage(String sendUserId, String receiveUserId, String message, String messageType) {
        ImContentEntity entity = new ImContentEntity();
        entity.setId(RandomUtil.uuId());
        entity.setSendUserId(sendUserId);
        entity.setSendTime(new Date());
        entity.setReceiveUserId(receiveUserId);
        entity.setEnabledMark(0);
        entity.setContent(message);
        entity.setContentType(messageType);
        this.save(entity);

        //写入到会话表中
        ImReplySavaModel imReplySavaModel = new ImReplySavaModel(sendUserId, receiveUserId, entity.getSendTime());
        ImReplyEntity imReplyEntity = JsonUtil.getJsonToBean(imReplySavaModel, ImReplyEntity.class);
        imReplyService.savaImReply(imReplyEntity);
    }

    @Override
    public void readMessage(String sendUserId, String receiveUserId) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImContentEntity::getSendUserId, sendUserId);
        queryWrapper.lambda().eq(ImContentEntity::getReceiveUserId, receiveUserId);
        queryWrapper.lambda().eq(ImContentEntity::getEnabledMark, 0);
        List<ImContentEntity> list = this.list(queryWrapper);
        for (ImContentEntity entity : list) {
            entity.setEnabledMark(1);
            entity.setReceiveTime(new Date());
            this.updateById(entity);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public boolean deleteChatRecord(String sendUserId, String receiveUserId) {
        QueryWrapper<ImContentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t-> {
            t.eq(ImContentEntity::getSendUserId, receiveUserId)
                    .eq(ImContentEntity::getReceiveUserId, sendUserId).or();
            t.eq(ImContentEntity::getReceiveUserId, receiveUserId)
                    .eq(ImContentEntity::getSendUserId, sendUserId);
        });
        List<ImContentEntity> list = this.list(queryWrapper);
        for (ImContentEntity entity : list) {
            if(entity.getDeleteUserId()!=null){
                if(!entity.getDeleteUserId().equals(sendUserId)) {
                    entity.setDeleteMark(1);
                    this.updateById(entity);
                }
            }
            entity.setDeleteUserId(sendUserId);
            this.updateById(entity);
        }
        QueryWrapper<ImContentEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ImContentEntity::getDeleteMark,1);
        this.remove(wrapper);
        return false;
    }

}
