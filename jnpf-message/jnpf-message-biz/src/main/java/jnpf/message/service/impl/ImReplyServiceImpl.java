package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.ImReplyEntity;
import jnpf.message.mapper.ImReplyMapper;
import jnpf.message.model.ImReplyListModel;
import jnpf.message.service.ImReplyService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-05-29
 */
@Service
public class ImReplyServiceImpl extends SuperServiceImpl<ImReplyMapper, ImReplyEntity> implements ImReplyService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;

    @Override
    public List<ImReplyEntity> getList() {
        QueryWrapper<ImReplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImReplyEntity::getUserId, userProvider.get().getUserId()).or()
                .eq(ImReplyEntity::getReceiveUserId, userProvider.get().getUserId())
                .orderByDesc(ImReplyEntity::getUserId);
        return this.list();
    }

    @Override
    public boolean savaImReply(ImReplyEntity entity) {
        QueryWrapper<ImReplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ImReplyEntity::getUserId, entity.getUserId()).eq(ImReplyEntity::getReceiveUserId, entity.getReceiveUserId());
        //判断数据是否存在
        ImReplyEntity imReplyEntity = this.getOne(queryWrapper);
        if (imReplyEntity != null) {
            entity.setId(imReplyEntity.getId());
            this.updateById(entity);
            return true;
        }
        this.save(entity);
        return true;
    }

    @Override
    public List<ImReplyListModel> getImReplyList() {
        List<ImReplyListModel> imReplyList = this.baseMapper.getImReplyList();
        List<ImReplyListModel> imReplyLists = new ArrayList<>(imReplyList);
        // 过滤掉用户id和接收id相同的
        imReplyLists = imReplyList.stream().filter(t ->{
            if(t.getImreplySendDeleteMark() == null){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
//        List<ImReplyListModel> imReplyListModels = new ArrayList<>(imReplyList);
//        for (int i = 0; i < imReplyList.size(); i++) {
//            ImReplyListModel imReplyListModel = imReplyList.get(i);
//            // 不和自己比
//            imReplyListModels.remove(imReplyList.get(i));
//            List<ImReplyListModel> irs = new ArrayList<>(imReplyListModels);
//            ImReplyListModel model = irs.stream().filter(t -> t.getUserId().equals(imReplyListModel.getUserId()) && t.getId().equals(imReplyListModel.getId())).findFirst().orElse(null);
//            if (model != null) {
//                imReplyLists.remove(model);
//            }
//        }
        //我发给别人
        List<ImReplyListModel> collect = imReplyLists.stream().filter(t -> t.getUserId().equals(userProvider.get().getUserId())).collect(Collectors.toList());
        //别人发给我
        List<ImReplyListModel> list = imReplyLists.stream().filter(t -> t.getId().equals(userProvider.get().getUserId())).collect(Collectors.toList());
        //头像替换成对方的
        for (ImReplyListModel imReplyListModel : collect) {
            UserEntity entity = userService.getInfo(imReplyListModel.getId());
            if (entity != null) {
                imReplyListModel.setHeadIcon(entity.getHeadIcon() != null ? entity.getHeadIcon() : "");
            }
        }
        for (ImReplyListModel model : list) {
            //移除掉互发的
            List<ImReplyListModel> collect1 = collect.stream().filter(t -> t.getId().equals(model.getUserId())).collect(Collectors.toList());
            if (collect1.size() > 0) {
                //判断我发给别人的时间和接收的时间大小
                //接收的大于发送的
                if (model.getLatestDate().getTime() > collect1.get(0).getLatestDate().getTime()) {
                    collect.remove(collect1.get(0));
                } else { //发送的大于接收的则跳过
                    continue;
                }
            }
            ImReplyListModel imReplyListModel = new ImReplyListModel();
            UserEntity entity = userService.getInfo(model.getUserId());
            imReplyListModel.setHeadIcon(entity.getHeadIcon());
            imReplyListModel.setUserId(userProvider.get().getUserId());
            imReplyListModel.setId(entity.getId());
            imReplyListModel.setLatestDate(model.getLatestDate());
            imReplyListModel.setLatestMessage(model.getLatestMessage());
            imReplyListModel.setMessageType(model.getMessageType());
            if(model.getImreplySendDeleteMark()!=null &&!model.getImreplySendDeleteMark().equals(userProvider.get().getUserId())){
                imReplyListModel.setSendDeleteMark(model.getSendDeleteMark());
                imReplyListModel.setImreplySendDeleteMark(model.getImreplySendDeleteMark());
                imReplyListModel.setDeleteMark(model.getDeleteMark());
            }
            collect.add(imReplyListModel);
        }
        return collect;
    }

    @Override
    public boolean relocation(String sendUserId, String receiveUserId) {

        QueryWrapper<ImReplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(t-> {
            t.eq(ImReplyEntity::getUserId, receiveUserId)
                    .eq(ImReplyEntity::getReceiveUserId, sendUserId).or();
            t.eq(ImReplyEntity::getReceiveUserId, receiveUserId)
                    .eq(ImReplyEntity::getUserId, sendUserId);
        });
        List<ImReplyEntity> list = this.list(queryWrapper);
        for (ImReplyEntity entity : list) {
            if(entity.getDeleteUserId()!=null){
                if(!entity.getDeleteUserId().equals(sendUserId)) {
                    entity.setDeleteMark(1);
                    this.updateById(entity);
                }
            }
            entity.setDeleteUserId(sendUserId);
            this.removeById(entity.getId());
        }
        QueryWrapper<ImReplyEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ImReplyEntity::getDeleteMark,1);
        this.remove(wrapper);
        return false;
    }
}
