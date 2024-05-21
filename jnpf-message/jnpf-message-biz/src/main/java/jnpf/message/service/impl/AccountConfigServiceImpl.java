package jnpf.message.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.message.entity.AccountConfigEntity;
import jnpf.message.mapper.AccountConfigMapper;
import jnpf.message.model.accountconfig.AccountConfigForm;
import jnpf.message.model.accountconfig.AccountConfigPagination;
import jnpf.message.service.AccountConfigService;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号配置功能
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
@Service
public class AccountConfigServiceImpl extends SuperServiceImpl<AccountConfigMapper, AccountConfigEntity> implements AccountConfigService {


    @Autowired
    private UserProvider userProvider;



    @Override
    public List<AccountConfigEntity> getList(AccountConfigPagination accountConfigPagination) {
        return getTypeList(accountConfigPagination, accountConfigPagination.getDataType());
    }

    @Override
    public List<AccountConfigEntity> getTypeList(AccountConfigPagination accountConfigPagination, String dataType) {
        String userId = userProvider.get().getUserId();
        int total = 0;
        int accountConfigNum = 0;
        QueryWrapper<AccountConfigEntity> accountConfigQueryWrapper = new QueryWrapper<>();

        //关键字
        if (StringUtil.isNotBlank(accountConfigPagination.getKeyword()) && !"null".equals(accountConfigPagination.getKeyword())) {
            accountConfigNum++;
            accountConfigQueryWrapper.lambda().and(t -> t.like(AccountConfigEntity::getEnCode, accountConfigPagination.getKeyword())
                    .or().like(AccountConfigEntity::getFullName, accountConfigPagination.getKeyword()).or().like(AccountConfigEntity::getAddressorName,accountConfigPagination.getKeyword())
                    .or().like(AccountConfigEntity::getSmtpUser,accountConfigPagination.getKeyword()).or().like(AccountConfigEntity::getSmsSignature,accountConfigPagination.getKeyword()));
        }
        //webhook类型
        if (ObjectUtil.isNotEmpty(accountConfigPagination.getWebhookType())) {
            accountConfigNum++;
            accountConfigQueryWrapper.lambda().eq(AccountConfigEntity::getWebhookType, accountConfigPagination.getWebhookType());
        }
        //渠道
        if (ObjectUtil.isNotEmpty(accountConfigPagination.getChannel())) {
            accountConfigNum++;
            accountConfigQueryWrapper.lambda().eq(AccountConfigEntity::getChannel, accountConfigPagination.getChannel());
        }
        //状态
        if(ObjectUtil.isNotEmpty(accountConfigPagination.getEnabledMark())){
            accountConfigNum++;
            int enabledMark = Integer.parseInt(accountConfigPagination.getEnabledMark());
            accountConfigQueryWrapper.lambda().eq(AccountConfigEntity::getEnabledMark, enabledMark);
        }
        //配置类型
        if (ObjectUtil.isNotEmpty(accountConfigPagination.getType())) {
            accountConfigNum++;
            accountConfigQueryWrapper.lambda().eq(AccountConfigEntity::getType, accountConfigPagination.getType());
        }

        //排序
        if (StringUtil.isEmpty(accountConfigPagination.getSidx())) {
            accountConfigQueryWrapper.lambda().orderByAsc(AccountConfigEntity::getSortCode).orderByDesc(AccountConfigEntity::getCreatorTime).orderByDesc(AccountConfigEntity::getLastModifyTime);
        } else {
            try {
                String sidx = accountConfigPagination.getSidx();
                AccountConfigEntity accountConfigEntity = new AccountConfigEntity();
                Field declaredField = accountConfigEntity.getClass().getDeclaredField(sidx);
                declaredField.setAccessible(true);
                String value = declaredField.getAnnotation(TableField.class).value();
                accountConfigQueryWrapper = "asc".equals(accountConfigPagination.getSort().toLowerCase()) ? accountConfigQueryWrapper.orderByAsc(value) : accountConfigQueryWrapper.orderByDesc(value);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (!"1".equals(dataType)) {
            if (total > 0 || total == 0) {
                Page<AccountConfigEntity> page = new Page<>(accountConfigPagination.getCurrentPage(), accountConfigPagination.getPageSize());
                IPage<AccountConfigEntity> userIPage = this.page(page, accountConfigQueryWrapper);
                return accountConfigPagination.setData(userIPage.getRecords(), userIPage.getTotal());
            } else {
                List<AccountConfigEntity> list = new ArrayList();
                return accountConfigPagination.setData(list, list.size());
            }
        } else {
            return this.list(accountConfigQueryWrapper);
        }
    }


    @Override
    public AccountConfigEntity getInfo(String id) {
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(AccountConfigEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, AccountConfigEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(AccountConfigEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
    //子表方法

    //列表子表数据方法


    //验证表单唯一字段
    @Override
    public boolean checkForm(AccountConfigForm form, int i,String type,String id) {
        int total = 0;
        if (ObjectUtil.isNotEmpty(form.getEnCode())) {
            QueryWrapper<AccountConfigEntity> codeWrapper = new QueryWrapper<>();
            codeWrapper.lambda().eq(AccountConfigEntity::getEnCode, form.getEnCode());
            codeWrapper.lambda().eq(AccountConfigEntity::getType,type);
            if(StringUtil.isNotBlank(id) && !"null".equals(id)) {
                codeWrapper.lambda().ne(AccountConfigEntity::getId, id);
            }
            total += (int) this.count(codeWrapper);
        }
        int c = 0;
        if (total > i + c) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkGzhId(String gzhId, int i,String type,String id) {
        int total = 0;
        if (StringUtil.isNotEmpty(gzhId) && !"null".equals(gzhId)) {
            QueryWrapper<AccountConfigEntity> codeWrapper = new QueryWrapper<>();
            codeWrapper.lambda().eq(AccountConfigEntity::getAppKey, gzhId);
            codeWrapper.lambda().eq(AccountConfigEntity::getType,type);
            if(StringUtil.isNotBlank(id) && !"null".equals(id)) {
                codeWrapper.lambda().ne(AccountConfigEntity::getId, id);
            }
            total += (int) this.count(codeWrapper);
        }
        int c = 0;
        if (total > i + c) {
            return true;
        }
        return false;
    }

    @Override
    public AccountConfigEntity getInfoByType(String appKey, String type) {
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getType, type);
        queryWrapper.lambda().eq(AccountConfigEntity::getAppKey,appKey);
        return this.getOne(queryWrapper);
    }

    @Override
    public AccountConfigEntity getInfoByEnCode(String enCode, String type){
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getType, type);
        queryWrapper.lambda().eq(AccountConfigEntity::getEnCode,enCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<AccountConfigEntity> getListByType(String type){
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getType,type);
        queryWrapper.lambda().eq(AccountConfigEntity::getEnabledMark,1);
        return this.list(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(AccountConfigEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id,String type) {
        QueryWrapper<AccountConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AccountConfigEntity::getEnCode, enCode);
        queryWrapper.lambda().eq(AccountConfigEntity::getType,type);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(AccountConfigEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public ActionResult ImportData(AccountConfigEntity entity) throws DataException {
        if (entity != null) {
//            if (isExistByFullName(entity.getFullName(), entity.getId())) {
//                return ActionResult.fail(MsgCode.EXIST001.get());
//            }
            if (isExistByEnCode(entity.getEnCode(), entity.getId(),entity.getType())) {
                return ActionResult.fail(MsgCode.EXIST002.get());
            }
            try {
                this.save(entity);
            } catch (Exception e) {
                throw new DataException(MsgCode.IMP003.get());
            }
            return ActionResult.success(MsgCode.IMP001.get());
        }
        return ActionResult.fail("导入数据格式不正确");
    }

}
