package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.entity.ProductGoodsEntity;
import jnpf.mapper.ProductGoodsMapper;
import jnpf.model.productgoods.ProductGoodsPagination;
import jnpf.service.ProductGoodsService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 产品商品
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 15:57:50
 */
@Service
public class ProductGoodsServiceImpl extends SuperServiceImpl<ProductGoodsMapper, ProductGoodsEntity> implements ProductGoodsService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ProductGoodsEntity> getGoodList(String type) {
        QueryWrapper<ProductGoodsEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(type)) {
            queryWrapper.lambda().eq(ProductGoodsEntity::getType, type);
        }
        return this.list(queryWrapper);
    }

    @Override
    public List<ProductGoodsEntity> getList(ProductGoodsPagination goodsPagination) {
        QueryWrapper<ProductGoodsEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(goodsPagination.getCode())) {
            queryWrapper.lambda().like(ProductGoodsEntity::getEnCode, goodsPagination.getCode());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getFullName())) {
            queryWrapper.lambda().like(ProductGoodsEntity::getFullName, goodsPagination.getFullName());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getClassifyId())) {
            queryWrapper.lambda().like(ProductGoodsEntity::getClassifyId, goodsPagination.getClassifyId());
        }
        if (StringUtil.isNotEmpty(goodsPagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(ProductGoodsEntity::getFullName, goodsPagination.getKeyword())
                            .or().like(ProductGoodsEntity::getEnCode, goodsPagination.getKeyword())
                            .or().like(ProductGoodsEntity::getProductSpecification, goodsPagination.getKeyword())
            );
        }
        //排序
        if (StringUtil.isEmpty(goodsPagination.getSidx())) {
            queryWrapper.lambda().orderByDesc(ProductGoodsEntity::getId);
        } else {
            queryWrapper = "asc".equals(goodsPagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(goodsPagination.getSidx()) : queryWrapper.orderByDesc(goodsPagination.getSidx());
        }
        Page<ProductGoodsEntity> page = new Page<>(goodsPagination.getCurrentPage(), goodsPagination.getPageSize());
        IPage<ProductGoodsEntity> userIPage = this.page(page, queryWrapper);
        return goodsPagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }

    @Override
    public ProductGoodsEntity getInfo(String id) {
        QueryWrapper<ProductGoodsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ProductGoodsEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ProductGoodsEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        this.save(entity);
    }

    @Override
    public boolean update(String id, ProductGoodsEntity entity) {
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        return this.updateById(entity);
    }

    @Override
    public void delete(ProductGoodsEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

}
