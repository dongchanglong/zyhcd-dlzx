package com.dlzx.pfserverparts.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dlzx.common.core.exception.CustomException;
import com.dlzx.common.core.utils.DateUtils;
import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.web.domain.SysExamineConfigParams;
import com.dlzx.common.security.service.DriverTokenService;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfserverparts.client.SystemClient;
import com.dlzx.pfserverparts.domain.GGoods;
import com.dlzx.pfserverparts.domain.GGoodsCheck;
import com.dlzx.pfserverparts.domain.GGoodsCheckItem;
import com.dlzx.pfserverparts.domain.GGoodsInventory;
import com.dlzx.pfserverparts.domain.GGoodsReceivingRecord;
import com.dlzx.pfserverparts.domain.GGoodsReceivingRecordDate;
import com.dlzx.pfserverparts.domain.GGoodsReceivingRecordItem;
import com.dlzx.pfserverparts.domain.GGoodsType;
import com.dlzx.pfserverparts.domain.ReviewSubmit;
import com.dlzx.pfserverparts.mapper.GGoodsCheckItemMapper;
import com.dlzx.pfserverparts.mapper.GGoodsCheckMapper;
import com.dlzx.pfserverparts.mapper.GGoodsInventoryMapper;
import com.dlzx.pfserverparts.mapper.GGoodsMapper;
import com.dlzx.pfserverparts.mapper.GGoodsReceivingRecordDateMapper;
import com.dlzx.pfserverparts.mapper.GGoodsReceivingRecordItemMapper;
import com.dlzx.pfserverparts.mapper.GGoodsReceivingRecordMapper;
import com.dlzx.pfserverparts.mapper.GGoodsTypeMapper;
import com.dlzx.pfserverparts.service.IGGoodsReceivingRecordService;
import com.dlzx.system.api.model.LoginDriver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dlzx.pfserverparts.mapper.SysDictDataMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * ????????????-??????????????????Service???????????????
 * 
 * @author dlzx
 * @date 2020-12-09
 */
@Service
public class GGoodsReceivingRecordServiceImpl implements IGGoodsReceivingRecordService
{
    @Autowired
    private GGoodsReceivingRecordMapper gGoodsReceivingRecordMapper;

    @Autowired
    private GGoodsReceivingRecordItemMapper gGoodsReceivingRecordItemMapper;

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Autowired
    private GGoodsMapper gGoodsMapper;

    @Autowired
    private GGoodsTypeMapper gGoodsTypeMapper;

    @Autowired
    private GGoodsReceivingRecordDateMapper gGoodsReceivingRecordDateMapper;

    @Autowired
    private GGoodsCheckMapper gGoodsCheckMapper;

    @Autowired
    private GGoodsCheckItemMapper gGoodsCheckItemMapper;

    @Autowired
    private GGoodsInventoryMapper gGoodsInventoryMapper;

    @Resource
    private DriverTokenService driverTokenService;

    @Autowired
    private SystemClient systemClient;

    @Autowired
    private TokenService tokenService;
    /**
     * ??????????????????-??????????????????
     * 
     * @param id ????????????-??????????????????ID
     * @return ????????????-??????????????????
     */
    @Override
    public GGoodsReceivingRecord selectGGoodsReceivingRecordById(String id)
    {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordById(id);
    }

    /**
     * ??????ID????????????
     * @param id
     * @return
     */
    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordByGGRRId(String id) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordByGGRRId(id);
    }

    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordByGGRRIId(String id) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordByGGRRIId(id);
    }

    @Override
    public List<GGoods> selectGGoodsReceivingRecordByByGoodsTypeId(String goodsTypeId) {
        // ??????????????????ID?????? ??????
        return gGoodsMapper.selectGGoodsByTypeGoodsTypeIdList(goodsTypeId);
    }

    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordByByGoodsId(String goodsId) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordByGoodsId(goodsId);
    }

    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordByGoodsIdOrDate(String goodsId) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordByGoodsIdOrDate(goodsId);
    }

    @Override
    public GGoodsReceivingRecord selectGGoodsReceivingRecordInfo(String id) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordInfo(id);
    }

    // M11?????????????????? ??????
    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordDrop() {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordDrop();
    }

    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordInfoList() {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordInfoList();
    }

    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordAmount(GGoodsReceivingRecord gGoodsReceivingRecord) {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordAmount(gGoodsReceivingRecord);
    }

    /**
     * ??????????????????-??????????????????
     * 
     * @param gGoodsReceivingRecord ????????????-??????????????????
     * @return ??????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitGGoodsReceivingRecord(GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        try {
            LoginDriver loginDriver = driverTokenService.getLoginDriver();
            gGoodsReceivingRecord.setStatus(1L);
            gGoodsReceivingRecord.setDriverId(loginDriver.getUserid());
            gGoodsReceivingRecord.setCreateBy(loginDriver.getUsername());

            if (gGoodsReceivingRecord.getReceiveDate() != null && gGoodsReceivingRecord.getReceiveDate().size()>0){

                // ????????????
                List<Date> receiveDate = gGoodsReceivingRecord.getReceiveDate();
                List<GGoodsReceivingRecordDate> list = new ArrayList<>();
                for (Date date : receiveDate) {
                    GGoodsReceivingRecordDate gGoodsReceivingRecordDate = new GGoodsReceivingRecordDate();
                    gGoodsReceivingRecordDate.setReceiveDate(date);
                    gGoodsReceivingRecordDate.setId(UUID.randomUUID().toString());
                    gGoodsReceivingRecordDate.setRecordId(gGoodsReceivingRecord.getId());
                    list.add(gGoodsReceivingRecordDate);
                }
                if (list.size()>0){
                    // ?????????????????????
                    gGoodsReceivingRecordDateMapper.deleteGoodsDateByRecordId(gGoodsReceivingRecord.getId());

                    // ????????????
                    gGoodsReceivingRecordDateMapper.insertList(list);
                }
            }

            // ??????
            if (gGoodsReceivingRecord.getgGoodsReceivingRecordItems()!= null && gGoodsReceivingRecord.getgGoodsReceivingRecordItems().size()>0){
                List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItems =new ArrayList<>();
                for (GGoodsReceivingRecordItem gGoodsReceivingRecordItem : gGoodsReceivingRecord.getgGoodsReceivingRecordItems()) {

                    // ??????item???
                    GGoodsReceivingRecordItem goodsReceivingRecordItem = new GGoodsReceivingRecordItem();
                    BeanUtils.copyProperties(gGoodsReceivingRecordItem,goodsReceivingRecordItem);
                    goodsReceivingRecordItem.setId(UUID.randomUUID().toString());
                    goodsReceivingRecordItem.setRecordId(gGoodsReceivingRecord.getId());
                    goodsReceivingRecordItem.setGoodsTypeId(gGoodsReceivingRecord.getGoodsTypeId());
                    goodsReceivingRecordItem.setGoodsTypeName(gGoodsReceivingRecord.getGoodsTypeName());
                    gGoodsReceivingRecordItems.add(goodsReceivingRecordItem);

                    // ?????????
                    GGoods gGoods = gGoodsMapper.selectGGoodsById(gGoodsReceivingRecordItem.getGoodsId());
                    if (gGoods.getAmount() < gGoodsReceivingRecordItem.getNumber()) {
                        throw new CustomException("??????????????????????????????????????????,??????????????????");
                    }
                    gGoods.setAmount(gGoods.getAmount() - gGoodsReceivingRecordItem.getNumber());
                    gGoodsMapper.updateGGoods(gGoods);
                }
                if (gGoodsReceivingRecordItems.size()>0){

                    // ?????????????????????
                    gGoodsReceivingRecordItemMapper.deleteGoodsItemByRecordId(gGoodsReceivingRecord.getId());

                    // ????????????
                    gGoodsReceivingRecordItemMapper.insertList(gGoodsReceivingRecordItems);
                }
            }

            // ??????
            this.setExamineTask(gGoodsReceivingRecord);

            gGoodsReceivingRecord.setUpdateTime(DateUtils.getNowDate());
            return gGoodsReceivingRecordMapper.updateGGoodsReceivingRecord(gGoodsReceivingRecord);
        } catch (BeansException e) {
            throw e;
        }
    }

    @Override
    public int updateGGoodsReceivingRecord(GGoodsReceivingRecord gGoodsReceivingRecord) {
        LoginDriver loginDriver = driverTokenService.getLoginDriver();
        GGoodsReceivingRecord gGoodsReceivingRecordData = gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordById(gGoodsReceivingRecord.getId());
        if (gGoodsReceivingRecordData.getStatus()!= 2){
            throw new CustomException("?????????????????????????????????");
        }
        if(gGoodsReceivingRecordData.getGoodsId()!= null){
            GGoods gGoods = gGoodsMapper.selectGGoodsById(gGoodsReceivingRecordData.getGoodsId());
            if (gGoods == null ) {
                throw new CustomException("?????????????????????????????????");
            }
            // ????????????????????????
            GGoodsInventory gGoodsInventory = new GGoodsInventory();
            gGoodsInventory.setId(UUID.randomUUID()+"");
            gGoodsInventory.setInventoryType(4L);
            gGoodsInventory.setType(1L);
            gGoodsInventory.setInventoryTime(new Date());
            gGoodsInventory.setCode(DateUtils.getCode(3L));
            gGoodsInventory.setGoodsName(gGoods.getName());
            gGoodsInventory.setInventoryNumber(gGoodsReceivingRecordData.getNumber());
            gGoodsInventory.setUnitPrice(gGoods.getUnitPrice());
            gGoodsInventory.setUnit(gGoods.getUnit());
            gGoodsInventory.setSpecification(gGoods.getSpecification());
            gGoodsInventory.setGoodsCode(gGoods.getCode());
            gGoodsInventory.setGoodsTypeId(gGoods.getGoodsTypeId());
            gGoodsInventory.setGoodsTypeName(gGoods.getGoodsTypeName());
            gGoodsInventory.setGoodsId(gGoods.getId());
            gGoodsInventory.setCreateTime(new Date());
            gGoodsInventory.setCreateBy(loginDriver.getUsername());
            gGoodsInventory.setSupplier(gGoods.getSupplier()==null?"":gGoods.getSupplier());
            gGoodsInventory.setLicensePlate(gGoodsReceivingRecordData.getLicensePlate());
            gGoodsInventoryMapper.insertGGoodsInventory(gGoodsInventory);
        }else {

            GGoodsReceivingRecordItem gGoodsReceivingRecordItem = new GGoodsReceivingRecordItem();
            gGoodsReceivingRecordItem.setRecordId(gGoodsReceivingRecord.getId());
            List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItemList = gGoodsReceivingRecordItemMapper.selectGGoodsReceivingRecordItemList(gGoodsReceivingRecordItem);
            List<String> goodsIds = gGoodsReceivingRecordItemList.stream().map(GGoodsReceivingRecordItem::getGoodsId).collect(Collectors.toList());
            List<GGoods> gGoods = gGoodsMapper.selectGGoodsByIdsList(goodsIds);
            gGoods.forEach(item->{
                // ????????????????????????
                GGoodsInventory gGoodsInventory = new GGoodsInventory();
                gGoodsInventory.setId(UUID.randomUUID()+"");
                gGoodsInventory.setInventoryType(4L);
                gGoodsInventory.setType(1L);
                gGoodsInventory.setInventoryTime(new Date());
                gGoodsInventory.setCode(DateUtils.getCode(3L));
                gGoodsInventory.setGoodsName(item.getName());
                gGoodsInventory.setSpecification(item.getSpecification());
                gGoodsInventory.setInventoryNumber(gGoodsReceivingRecordItemList
                        .stream()
                        .filter(f->item.getId().equals(f.getGoodsId()))
                        .findFirst()
                        .orElse(new GGoodsReceivingRecordItem())
                        .getNumber());
                gGoodsInventory.setUnitPrice(item.getUnitPrice());
                gGoodsInventory.setUnit(item.getUnit());
                gGoodsInventory.setGoodsCode(item.getCode());
                gGoodsInventory.setGoodsTypeId(item.getGoodsTypeId());
                gGoodsInventory.setGoodsTypeName(item.getGoodsTypeName());
                gGoodsInventory.setGoodsId(item.getId());
                gGoodsInventory.setCreateTime(new Date());
                gGoodsInventory.setSupplier(item.getSupplier()==null?"":item.getSupplier());
                gGoodsInventory.setCreateBy(loginDriver.getUsername());
                gGoodsInventory.setLicensePlate(gGoodsReceivingRecordData.getLicensePlate());
                gGoodsInventoryMapper.insertGGoodsInventory(gGoodsInventory);
            });
        }
        // ??????????????????
        gGoodsReceivingRecord.setReceiveTime(new Date());
        gGoodsReceivingRecord.setStatus(4L);
        return gGoodsReceivingRecordMapper.updateGGoodsReceivingRecord(gGoodsReceivingRecord);
    }

    @Override
    public AjaxResult checkCInsuranceVehicle(GGoodsReceivingRecord gGoodsReceivingRecord) {
        // ????????????
        ReviewSubmit reviewSubmit = new ReviewSubmit();
        reviewSubmit.setRelationId(gGoodsReceivingRecord.getId());
        reviewSubmit.setReviewRemarks(gGoodsReceivingRecord.getReviewRemarks());
        reviewSubmit.setUserId(gGoodsReceivingRecord.getUserid()+"");
        reviewSubmit.setStatus(gGoodsReceivingRecord.getStatus());
        Map map = systemClient.reviewCommon(reviewSubmit);
        if (map.get("code").equals(1) || map.get("code").equals(2)){
            gGoodsReceivingRecord.setStatus(1L);
            return AjaxResult.success(map.get("msg").toString());
        }
        // ?????????
        if (map.get("code").equals(3)){
            gGoodsReceivingRecord.setStatus(2L);
        }
        // ?????????
        if (map.get("code").equals(4)){
            // ????????????????????????????????????????????????????????????
            GGoodsReceivingRecordItem gGoodsReceivingRecordItem = new GGoodsReceivingRecordItem();
            gGoodsReceivingRecordItem.setRecordId(gGoodsReceivingRecord.getId());
            List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItems = gGoodsReceivingRecordItemMapper.selectGGoodsReceivingRecordItemList(gGoodsReceivingRecordItem);
            if (gGoodsReceivingRecordItems.size()>0){

                List<GGoods> gGoodsList = new ArrayList<>();
                for (GGoodsReceivingRecordItem goodsReceivingRecordItem : gGoodsReceivingRecordItems) {
                    // ?????????
                    GGoods gGoods = gGoodsMapper.selectGGoodsById(goodsReceivingRecordItem.getGoodsId());
                    // ??????????????????????????????
                    gGoods.setAmount(gGoods.getAmount() + goodsReceivingRecordItem.getNumber());

                    gGoodsList.add(gGoods);
                }
                gGoodsMapper.updateList(gGoodsList);
            }else {
                GGoodsReceivingRecord goodsReceivingRecord = gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordInfo(gGoodsReceivingRecord.getId());

                // ?????????
                GGoods gGoods = gGoodsMapper.selectGGoodsById(goodsReceivingRecord.getGoodsId());
                // ??????????????????????????????
                gGoods.setAmount(gGoods.getAmount() + goodsReceivingRecord.getNumber());
                gGoodsMapper.updateGGoods(gGoods);
            }
            gGoodsReceivingRecord.setStatus(3L);
        }
        gGoodsReceivingRecordMapper.checkCInsuranceVehicle(gGoodsReceivingRecord);
        return AjaxResult.success(map.get("msg").toString());
    }

    /**
     * ????????????????????????-??????????????????
     * 
     * @param ids ???????????????????????????-??????????????????ID
     * @return ??????
     */
    @Override
    public int deleteGGoodsReceivingRecordByIds(String[] ids)
    {
        return gGoodsReceivingRecordMapper.deleteGGoodsReceivingRecordByIds(ids);
    }

    /**
     * ??????????????????-????????????????????????
     * 
     * @param id ????????????-??????????????????ID
     * @return ??????
     */
    @Override
    public int deleteGGoodsReceivingRecordById(String id)
    {
        return gGoodsReceivingRecordMapper.deleteGGoodsReceivingRecordById(id);
    }

    /**
     * @describe ??????
     * @author DongCL
     * @date 2021-01-17 15:57
     * @param
     * @return
     */
    @Override
    public int recallGGoodsReceivingRecord(GGoodsReceivingRecord gGoodsReceivingRecord) {
        GGoodsReceivingRecord gGoodsReceivingRecordData = gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordById(gGoodsReceivingRecord.getId());

        // ????????????????????????????????????????????????????????????
        GGoodsReceivingRecordItem gGoodsReceivingRecordItem = new GGoodsReceivingRecordItem();
        gGoodsReceivingRecordItem.setRecordId(gGoodsReceivingRecord.getId());
        List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItems = gGoodsReceivingRecordItemMapper.selectGGoodsReceivingRecordItemList(gGoodsReceivingRecordItem);
        if (gGoodsReceivingRecordItems.size()>0){

            List<GGoods> gGoodsList = new ArrayList<>();
            for (GGoodsReceivingRecordItem goodsReceivingRecordItem : gGoodsReceivingRecordItems) {
                // ?????????
                GGoods gGoods = gGoodsMapper.selectGGoodsById(goodsReceivingRecordItem.getGoodsId());
                // ??????????????????????????????
                gGoods.setAmount(gGoods.getAmount() + goodsReceivingRecordItem.getNumber());

                gGoodsList.add(gGoods);
            }
            gGoodsMapper.updateList(gGoodsList);

        }else {

            // ?????????
            GGoods gGoods = gGoodsMapper.selectGGoodsById(gGoodsReceivingRecordData.getGoodsId());
            // ??????????????????????????????
            gGoods.setAmount(gGoods.getAmount() + gGoodsReceivingRecordData.getNumber());
            gGoodsMapper.updateGGoods(gGoods);
        }
        // ?????????
        gGoodsReceivingRecord.setStatus(5L);
        return gGoodsReceivingRecordMapper.updateGGoodsReceivingRecord(gGoodsReceivingRecord);
    }


    /**
     * @describe
     * @author DongCL
     * @date 2021-01-19 14:34
     * @return
     */
    @Override
    public List<GGoodsType> queryGoodTypeList() {
        return  gGoodsTypeMapper.queryIsdeleteGoodTypeList();
    }


    /**
     * ??????????????????-????????????????????????
     *
     * @param gGoodsReceivingRecord ????????????-??????????????????
     * @return ????????????-??????????????????
     */
    @Override
    public List<GGoodsReceivingRecord> selectGGoodsReceivingRecordList(GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return gGoodsReceivingRecordMapper.selectGGoodsReceivingRecordList(gGoodsReceivingRecord);
    }

   /**
    * @describe ??????
    * @author DongCL
    * @date 2021-01-19 17:10
    * @param
    * @return
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertGGoodsReceivingRecord(GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        try {
            // ?????????
            this.setDetail(gGoodsReceivingRecord);
            return gGoodsReceivingRecordMapper.insertGGoodsReceivingRecord(gGoodsReceivingRecord);
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * @describe ?????????
     * @author DongCL
     * @date 2021-01-19 17:03
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertGGoodsReceivingRecordDate(GGoodsReceivingRecord gGoodsReceivingRecord) {

        try {
            // ?????????
            this.setDetail(gGoodsReceivingRecord);

            // ????????????
            List<Date> receiveDate = gGoodsReceivingRecord.getReceiveDate();
            List<GGoodsReceivingRecordDate> list = new ArrayList<>();
            for (Date date : receiveDate) {
                GGoodsReceivingRecordDate gGoodsReceivingRecordDate = new GGoodsReceivingRecordDate();
                gGoodsReceivingRecordDate.setReceiveDate(date);
                gGoodsReceivingRecordDate.setId(UUID.randomUUID().toString());
                gGoodsReceivingRecordDate.setRecordId(gGoodsReceivingRecord.getId());
                list.add(gGoodsReceivingRecordDate);
            }
            gGoodsReceivingRecordDateMapper.insertList(list);

            return gGoodsReceivingRecordMapper.insertGGoodsReceivingRecord(gGoodsReceivingRecord);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @describe ?????? / ???????????????
     * @author DongCL
     * @date 2021-01-19 17:12
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertGGoodsReceivingRecordTyre(GGoodsReceivingRecord gGoodsReceivingRecord) {
        try {
            // ?????????
            this.setDetail(gGoodsReceivingRecord);
            return gGoodsReceivingRecordMapper.insertGGoodsReceivingRecord(gGoodsReceivingRecord);
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * @describe ??????
     * @author DongCL
     * @date 2021-01-19 17:21
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertGGoodsReceivingRecordItem(GGoodsReceivingRecord gGoodsReceivingRecord) {

        LoginDriver loginDriver = driverTokenService.getLoginDriver();
        gGoodsReceivingRecord.setId(UUID.randomUUID()+"");
        gGoodsReceivingRecord.setStatus(1L);
        gGoodsReceivingRecord.setDriverId(loginDriver.getUserid());
        gGoodsReceivingRecord.setCreateBy(loginDriver.getUsername());
        gGoodsReceivingRecord.setCreateTime(DateUtils.getNowDate());
        gGoodsReceivingRecord.setGoodsName("??????");

        try {
            // ??????
            if (gGoodsReceivingRecord.getgGoodsReceivingRecordItems()!= null){
                List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItems =new ArrayList<>();

                if (gGoodsReceivingRecord.getgGoodsReceivingRecordItems().size()<=0){
                    throw new CustomException("??????????????????");
                }
                for (GGoodsReceivingRecordItem gGoodsReceivingRecordItem : gGoodsReceivingRecord.getgGoodsReceivingRecordItems()) {

                    // ??????item???
                    GGoodsReceivingRecordItem goodsReceivingRecordItem = new GGoodsReceivingRecordItem();
                    BeanUtils.copyProperties(gGoodsReceivingRecordItem,goodsReceivingRecordItem);
                    goodsReceivingRecordItem.setId(UUID.randomUUID().toString());
                    goodsReceivingRecordItem.setRecordId(gGoodsReceivingRecord.getId());
                    goodsReceivingRecordItem.setGoodsTypeId(gGoodsReceivingRecord.getGoodsTypeId());
                    goodsReceivingRecordItem.setGoodsTypeName(gGoodsReceivingRecord.getGoodsTypeName());
                    gGoodsReceivingRecordItems.add(goodsReceivingRecordItem);

                    // ?????????
                    GGoods gGoods = gGoodsMapper.selectGGoodsById(gGoodsReceivingRecordItem.getGoodsId());
                    if (gGoods == null){
                        throw new CustomException("??????????????????");
                    }
                    if (gGoods.getAmount() < gGoodsReceivingRecordItem.getNumber()) {
                        throw new CustomException("??????????????????????????????????????????,??????????????????");
                    }
                    gGoods.setAmount(gGoods.getAmount() - gGoodsReceivingRecordItem.getNumber());
                    gGoodsMapper.updateGGoods(gGoods);
                }
                if (gGoodsReceivingRecordItems.size()>0){
                    gGoodsReceivingRecordItemMapper.insertList(gGoodsReceivingRecordItems);
                }
            }

            // ??????
            this.setExamineTask(gGoodsReceivingRecord);

            gGoodsReceivingRecord.setDeleteFlag(1L);
            return gGoodsReceivingRecordMapper.insertGGoodsReceivingRecord(gGoodsReceivingRecord);
        } catch (BeansException e) {
            throw e;
        }
    }

    @Override
    public GGoodsReceivingRecord queryGGoodsReceivingRecordById(String id) {
        GGoodsReceivingRecord gGoodsReceivingRecord = gGoodsReceivingRecordMapper.queryGGoodsReceivingRecordById(id);

        // ????????????id?????????????????????
        GGoodsReceivingRecordItem gGoodsReceivingRecordItem = new GGoodsReceivingRecordItem();
        gGoodsReceivingRecordItem.setRecordId(id);
        List<GGoodsReceivingRecordItem> gGoodsReceivingRecordItems = gGoodsReceivingRecordItemMapper.queryGGoodsReceivingRecordItemList(gGoodsReceivingRecordItem);
        if (gGoodsReceivingRecordItems.size()>0){
            gGoodsReceivingRecord.setgGoodsReceivingRecordItems(gGoodsReceivingRecordItems);
        }

        // ????????????id?????????????????????
        GGoodsReceivingRecordDate gGoodsReceivingRecordDate = new GGoodsReceivingRecordDate();
        gGoodsReceivingRecordDate.setRecordId(id);
        List<GGoodsReceivingRecordDate> gGoodsReceivingRecordDates = gGoodsReceivingRecordDateMapper.selectGGoodsReceivingRecordDateList(gGoodsReceivingRecordDate);
        if (gGoodsReceivingRecordDates.size()>0){
            gGoodsReceivingRecord.setgGoodsReceivingRecordDates(gGoodsReceivingRecordDates);
        }

        GGoodsType gGoodsType = gGoodsTypeMapper.selectGGoodsTypeById(gGoodsReceivingRecord.getGoodsTypeId());
        gGoodsReceivingRecord.setGoodsType(gGoodsType.getType());
        return gGoodsReceivingRecord;
    }


    /**
     * @describe ??????????????????????????????????????????
     * @author DongCL
     * @date 2021-01-20 11:41
     * @param
     * @return
     */
    @Override
    public List<GGoodsReceivingRecord> queryGGoodsReceivingRecordList(GGoodsReceivingRecord gGoodsReceivingRecord) {
        // ?????????????????????  todo ?????? udpate by 2021 3.11 19.36
        LoginDriver loginDriver = driverTokenService.getLoginDriver();
        gGoodsReceivingRecord.setDriverId(loginDriver.getUserid());
        return gGoodsReceivingRecordMapper.queryGGoodsReceivingRecordList(gGoodsReceivingRecord);
    }


    /**
     * @describe ????????????
     * @author DongCL
     * @date 2021-01-21 17:42
     * @param
     * @return
     */
    @Override
    public List<GGoodsCheck> queryGGoodsCheckList(GGoodsCheck gGoodsCheck) {
        return gGoodsCheckMapper.queryAdminGGoodsCheckList(gGoodsCheck);
    }

    /**
     * @describe ????????????
     * @author DongCL
     * @date 2021-01-21 17:42
     * @param
     * @return
     */
    @Override
    public GGoodsCheck queryGGoodsCheckById(GGoodsCheck gGoodsCheckData) {

        // todo ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        GGoodsCheck gGoodsCheck = gGoodsCheckMapper.selectGGoodsCheckById(gGoodsCheckData.getId());
//
//        List<GGoodsCheckItem> gGoodsCheckItems = gGoodsCheckItemMapper.selectGGoodsCheckItemByCheckId(gGoodsCheck.getId());
//        List<String> goodIds = gGoodsCheckItems.stream().map(GGoodsCheckItem::getGoodsId).collect(Collectors.toList());
//        List<GGoods> gGoods = gGoodsMapper.queryInList(goodIds);
//        for (GGoodsCheckItem gGoodsCheckItem : gGoodsCheckItems) {
//            GGoods gGoodData = gGoods.stream().filter(g -> g.getId().equals(gGoodsCheckItem.getGoodsId())).findFirst().orElse(null);
//            if (gGoodData != null){
//                gGoodsCheckItem.setCurrentQuantity(gGoodData.getAmount());
//            }
//        }
//        gGoodsCheckItemMapper.updateList(gGoodsCheckItems);

        if (gGoodsCheck != null){
            GGoodsCheckItem gGoodsCheckItem = new GGoodsCheckItem();
            gGoodsCheckItem.setCheckId(gGoodsCheck.getId());
            if (!StringUtils.isEmpty(gGoodsCheckData.getGoodName())){
                gGoodsCheckItem.setGoodsName(gGoodsCheckData.getGoodName());
            }
            gGoodsCheck.setgGoodsCheckItems(gGoodsCheckItemMapper.selectGGoodsCheckItemList(gGoodsCheckItem));
        }
        return gGoodsCheck;
    }


    /**
     * @describe ??????????????????(????????????)
     * @author DongCL
     * @date 2021-01-20 11:41
     * @param
     * @return
     */
    @Override
    public List<GGoodsReceivingRecord> queryAdminGGoodsReceivingRecordList(GGoodsReceivingRecord gGoodsReceivingRecord) {
        return gGoodsReceivingRecordMapper.queryGGoodsReceivingRecordList(gGoodsReceivingRecord);
    }


    //  ?????? ?????? ????????????????????????
    @Transactional
    public int setDetail(GGoodsReceivingRecord gGoodsReceivingRecord) {
        try {
            LoginDriver loginDriver = driverTokenService.getLoginDriver();
            gGoodsReceivingRecord.setId(UUID.randomUUID()+"");
            gGoodsReceivingRecord.setStatus(1L);
            gGoodsReceivingRecord.setDriverId(loginDriver.getUserid());
            gGoodsReceivingRecord.setCreateBy(loginDriver.getUsername());
            gGoodsReceivingRecord.setDeleteFlag(1L);
            gGoodsReceivingRecord.setCreateTime(DateUtils.getNowDate());

            //?????????
            GGoods gGoods = gGoodsMapper.selectGGoodsById(gGoodsReceivingRecord.getGoodsId());
            gGoodsReceivingRecord.setSpecification(gGoods.getSpecification()==null?"":gGoods.getSpecification());
            gGoodsReceivingRecord.setUnit(gGoods.getUnit()==null?"":gGoods.getUnit());
            if (gGoods.getAmount() < gGoodsReceivingRecord.getNumber()) {
                throw new CustomException("??????????????????????????????????????????,??????????????????");
            }
            gGoods.setAmount(gGoods.getAmount() - gGoodsReceivingRecord.getNumber());

            // ??????
            this.setExamineTask(gGoodsReceivingRecord);

            return gGoodsMapper.updateGGoods(gGoods);
        } catch (Exception e) {
            throw e;
        }
    }


    //  ?????? ?????? ??????
    @Transactional
    public int setExamineTask(GGoodsReceivingRecord gGoodsReceivingRecord) {
        try {
            //?????????????????? type = 12
            int result = systemClient.submitCommon(new SysExamineConfigParams(12L,gGoodsReceivingRecord.getId(),gGoodsReceivingRecord.getDriverId(),gGoodsReceivingRecord.getCreateBy(),gGoodsReceivingRecord.getSysExamineConfigProcessList()));
            //????????????????????????
            if (result == 0){
                gGoodsReceivingRecord.setStatus(2L);
            }
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}
