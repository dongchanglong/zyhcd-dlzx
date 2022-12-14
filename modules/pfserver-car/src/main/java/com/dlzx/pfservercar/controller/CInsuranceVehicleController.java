package com.dlzx.pfservercar.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.core.web.domain.SysExamineConfigParams;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfservercar.client.SystemClient;
import com.dlzx.pfservercar.domain.*;
import com.dlzx.pfservercar.mapper.SysDictDataMapper;
import com.dlzx.pfservercar.service.ICInsuranceTypeItemService;
import com.dlzx.pfservercar.service.ICInsuranceTypeService;
import com.dlzx.pfservercar.service.ICInsuranceVehicleGoogdsService;
import com.dlzx.pfservercar.service.ICInsuranceVehicleItemService;
import com.dlzx.pfservercar.service.ICVehicleService;
import com.dlzx.pfservercar.service.impl.CInsuranceVehicleGoogdsServiceImpl;
import com.dlzx.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dlzx.common.log.annotation.Log;
import com.dlzx.common.log.enums.BusinessType;
import com.dlzx.common.security.annotation.PreAuthorize;
import com.dlzx.pfservercar.service.ICInsuranceVehicleService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-????????????Controller
 * 
 * @author dlzx
 * @date 2020-11-24
 */
@RestController
@RequestMapping("/car/insuranceVehicle")
public class CInsuranceVehicleController extends BaseController
{
    @Autowired
    private ICInsuranceVehicleService cInsuranceVehicleService;
    @Autowired
    private ICInsuranceVehicleItemService icInsuranceVehicleItemService;
    @Autowired
    private ICInsuranceTypeItemService cInsuranceTypeItemService;
    @Autowired
    private ICInsuranceTypeService cInsuranceTypeService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ICInsuranceVehicleGoogdsService icInsuranceVehicleGoogdsService;
    @Autowired
    private SystemClient systemClient;
    @Autowired
    private ICVehicleService cVehicleService;

    /**
     * ????????????ID??????????????????
     */
    public BigDecimal insuranceCost(CInsuranceVehicleItem cInsuranceVehicleItem) {
        List<CInsuranceVehicleItem> cInsuranceVehicleItems = icInsuranceVehicleItemService.selectCInsuranceVehicleItemList(cInsuranceVehicleItem);
        BigDecimal total = BigDecimal.ZERO;
        for (CInsuranceVehicleItem cInsuranceVehicleItem1 : cInsuranceVehicleItems) {
            total=total.add(cInsuranceVehicleItem1.getInsuranceCost());
        }
        return total;
    }

    /**
     * ??????????????????-??????????????????  YLH
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:list")
    @PostMapping(value = {"/E01List"})
    public TableDataInfo E01List(@RequestBody CInsuranceVehicle cInsuranceVehicle)
    {
        // ??????cInsuranceVehicle???id
//        String id = cInsuranceVehicle.getId();
//        CInsuranceVehicleItem cInsuranceVehicleItem = new CInsuranceVehicleItem();
//        cInsuranceVehicleItem.setInsuranceId(id);
//        if (cInsuranceVehicleItem.getInsuranceId() != null){
//            // ??????
//            BigDecimal bigDecimal = insuranceCost(cInsuranceVehicleItem);
//            cInsuranceVehicle.setTotalPremium(bigDecimal);
//            // ??????
//            cInsuranceVehicleService.updateCInsuranceVehicle(cInsuranceVehicle);
//        }
        startPage(cInsuranceVehicle.getPageNum(),cInsuranceVehicle.getPageSize());
        List<CInsuranceVehicle> list = cInsuranceVehicleService.selectCInsuranceVe(cInsuranceVehicle);
//        ArrayList<CInsuranceVehicle> collect = list.stream().collect(Collectors.collectingAndThen(
//                Collectors.toCollection(() -> new TreeSet<>(
//                        Comparator.comparing(
//                                CInsuranceVehicle::getId))), ArrayList::new));
        return getDataTable(list);
    }

    @PostMapping("/E02Judge")
    public AjaxResult E02Judge(@RequestBody EinJudge einJudge){
        CInsuranceVehicle cInsuranceVehicle = cInsuranceVehicleService.selectCInsuranceLastList(einJudge.getId());
        if (cInsuranceVehicle != null){
            List<CInsuranceVehicleItem> cInsuranceVehicleItems = cInsuranceVehicleService.selectCInsuranceItemByInsuranceId(cInsuranceVehicle.getId());
            return AjaxResult.success(cInsuranceVehicleItems);
        }
        else {
            return AjaxResult.success(null);
        }
    }

    /**
     * ???????????????E02Box  YLH
     * @return
     */
    @PostMapping("/E02Box")
    public AjaxResult E02Box(){
        List<CVehicle> cVehicleList = cVehicleService.selectCVehicleList(new CVehicle());
        return AjaxResult.success(cVehicleList);
    }

    /**
     * /E02InsuranceTypeList   YLH
     * @param cInsuranceTypeItem
     * @return
     */
    @PostMapping("/E02InsuranceTypeList")
    @PreAuthorize(hasPermi = "car:insuranceVehicle:addtypelist")
    public TableDataInfo E02InsuranceTypeList(@RequestBody CInsuranceTypeItem cInsuranceTypeItem){
        return getDataTable(cInsuranceTypeItemService.selectTypeList(cInsuranceTypeItem));
    }



    /**
     * ??????????????????-?????????????????? YLH
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:gelist")
    @PostMapping("/E06List")
    public TableDataInfo E04List(@RequestBody CInsuranceVehicle cInsuranceVehicle)
    {
        startPage(cInsuranceVehicle.getPageNum(),cInsuranceVehicle.getPageSize());

        // ??????cInsuranceVehicle???id
//        String id = cInsuranceVehicle.getId();
//        CInsuranceVehicleItem cInsuranceVehicleItem = new CInsuranceVehicleItem();
//        cInsuranceVehicleItem.setInsuranceId(id);
//        if (cInsuranceVehicleItem.getInsuranceId() != null){
//            // ??????
//            BigDecimal bigDecimal = insuranceCost(cInsuranceVehicleItem);
//            cInsuranceVehicle.setTotalPremium(bigDecimal);
//            // ??????
//            cInsuranceVehicleService.updateCInsuranceVehicle(cInsuranceVehicle);
//        }

        List<CInsuranceVehicle> list = cInsuranceVehicleService.selectCInsuranceGe(cInsuranceVehicle);


//        ArrayList<CInsuranceVehicle> collect = list.stream().collect(Collectors.collectingAndThen(
//                Collectors.toCollection(() -> new TreeSet<>(
//                        Comparator.comparing(
//                                CInsuranceVehicle::getId))), ArrayList::new));
        return getDataTable(list);
    }

    /**
     * ??????????????????-??????????????????
     * E03 E04 E05
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:list")
    @GetMapping (value = {"/E04GetInfo/{id}"})
    public TableDataInfo E04GetInfo(@PathVariable("id") String id)
    {
        CInsuranceVehicleItem cInsuranceVehicleItem = new CInsuranceVehicleItem();
        CInsuranceVehicle cInsuranceVehicle = new CInsuranceVehicle();
        cInsuranceVehicleItem.setInsuranceId(id);
        if (cInsuranceVehicleItem.getInsuranceId() != null){
            // ??????
            BigDecimal bigDecimal = insuranceCost(cInsuranceVehicleItem);
            cInsuranceVehicle.setTotalPremium(bigDecimal);
            cInsuranceVehicle.setId(id);
            // ??????
            cInsuranceVehicleService.updateCInsuranceVehicle(cInsuranceVehicle);
        }

        List<CInsuranceVehicle> list = cInsuranceVehicleService.selectCInsuranceVehicleInfo(id);
        List<CInsuranceVehicleItem> list1 = new ArrayList<>();
        for (CInsuranceVehicle vehicle : list) {
            CInsuranceVehicleItem cInsuranceVehicleItem1 = new CInsuranceVehicleItem();
            cInsuranceVehicleItem1.setId(vehicle.getCIVIID());
            cInsuranceVehicleItem1.setInsuranceId(vehicle.getInsuranceId());
            cInsuranceVehicleItem1.setInsuranceTypeId(vehicle.getInsuranceTypeId());
            cInsuranceVehicleItem1.setInsuranceItemId(vehicle.getInsuranceItemId());
            cInsuranceVehicleItem1.setInsuranceItemName(vehicle.getInsuranceItemName());
            cInsuranceVehicleItem1.setInsuranceQuota(vehicle.getInsuranceQuota());
            cInsuranceVehicleItem1.setInsuranceCost(vehicle.getInsuranceCost());
            list1.add(cInsuranceVehicleItem1);
            vehicle.setcInsuranceVehicleItems(list1);
        }

        ArrayList<CInsuranceVehicle> collect = list.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(
                                CInsuranceVehicle::getId))), ArrayList::new));
        return getDataTable(collect);
    }

    /**
     * ??????????????????-???????????????????????? YLH
     * ??????????????????
     */
//    @PreAuthorize(hasPermi = "car:insuranceVehicle:query")
    @GetMapping("/E02GetInfo/{id}")
    public AjaxResult E02GetInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(cInsuranceVehicleService.selectCInsuranceVehicleById(id));
    }

    /**
     * ??????????????????-????????????   YLH
     *
     * ???????????????????????????????????????-??????????????????????????????
     * ??????CInsuranceTypeTableJoinAdd DTO??? ??????????????????????????????????????????
     * ????????????????????????????????? c_inserance_vehicle??? c_insurance_vehicle_item??? ???
     * ???????????????????????????????????????   car/insuranceTypeItem/E13List  ???????????????????????????   ???????????????????????????
     * E13List????????????name????????????  ?????????typeId???
     *
     * ?????????????????????????????? ?????? ?????? ?????? ???c_insurance_vehicle_item???????????????
     * @param cInsuranceTypeTableJoinAdd
     * @return
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:add")
    @Log(title = "????????????-????????????", businessType = BusinessType.INSERT)
    @PostMapping(value = {"/E02Add","/SubmitCommon"})
    public AjaxResult E02Add(@Validated @RequestBody CInsuranceTypeTableJoinAdd cInsuranceTypeTableJoinAdd) {
        if (cInsuranceTypeTableJoinAdd.getType()==1&&StringUtils.isEmpty(cInsuranceTypeTableJoinAdd.getFrameNumber())){
            return AjaxResult.error("?????????????????????");
        }
        if (cInsuranceTypeTableJoinAdd.getType()==2&&StringUtils.isEmpty(cInsuranceTypeTableJoinAdd.getLicensePlate())){
            return AjaxResult.error("?????????????????????");
        }
        if (cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems().size() == 0){
            return AjaxResult.error("?????????????????????");
        }
        else {
            for (CInsuranceVehicleItem cInsuranceVehicleItem:cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems()){
                if (cInsuranceVehicleItem.getInsuranceCost()==null){
                    return AjaxResult.error("???????????????");
                }
            }
        }
        // c_inserance_vehicle???
        CInsuranceVehicle cInsuranceVehicle = new CInsuranceVehicle();
        // ???cInsuranceVehicle???type???????????? ????????????
        cInsuranceVehicle.setType(cInsuranceTypeTableJoinAdd.getType());
        // ???cInsuranceVehicle???FrameNumber???????????? ?????????
        cInsuranceVehicle.setFrameNumber(cInsuranceTypeTableJoinAdd.getFrameNumber());
        // ???cInsuranceVehicle???LicensePlate???????????? ?????????
        cInsuranceVehicle.setLicensePlate(cInsuranceTypeTableJoinAdd.getLicensePlate());
        // ???cInsuranceVehicle???Remarks???????????? ??????
        cInsuranceVehicle.setRemarks(cInsuranceTypeTableJoinAdd.getRemarks());
        cInsuranceVehicle.setVehicleId(cInsuranceTypeTableJoinAdd.getVehicleId());
        UUID uuid = UUID.randomUUID();
        cInsuranceVehicle.setId(uuid+"");

//        CVehicle cVehicle = new CVehicle();
//        cVehicle.setFrameNumber(cInsuranceTypeTableJoinAdd.getFrameNumber());
//        cVehicle.setLicensePlate(cInsuranceTypeTableJoinAdd.getLicensePlate());
//        List<CVehicle> list = cVehicleService.selectCVehicleList(cVehicle);
//        String licensePlate = null;
//        for (CVehicle vehicle : list) {
//            licensePlate = vehicle.getId();
//        }

        LoginUser userInfo = tokenService.getLoginUser();
        cInsuranceVehicle.setCreateBy(userInfo.getUsername());

        // c_insurance_vehicle_item???
        if (cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems().size()>0){
            for (CInsuranceVehicleItem insuranceVehicleItem : cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems()) {
                // ???????????????????????????????????????
                insuranceVehicleItem.setCreateBy(userInfo.getUsername());
                insuranceVehicleItem.setInsuranceId(uuid+"");
                UUID uuid1 = UUID.randomUUID();
                insuranceVehicleItem.setId(uuid1+"");
                insuranceVehicleItem.setDeleteFlag((long) 1);
            }
            BigDecimal totalQuantity = cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems().stream().map(i -> {
                if (i.getInsuranceCost()==null){
                    return BigDecimal.ZERO;
                }else {
                    return i.getInsuranceCost();
                }
            }).reduce(BigDecimal.ZERO,BigDecimal::add);
            cInsuranceVehicle.setTotalPremium(totalQuantity);
            //????????????
            icInsuranceVehicleItemService.insertBatch(cInsuranceTypeTableJoinAdd.getcInsuranceVehicleItems());
        }
        // ????????????????????? 1??????
        cInsuranceVehicle.setDeleteFlag((long) 1);
        //????????????????????????
        int result = systemClient.adminSubmitCommon(11L,cInsuranceVehicle.getId(),"",userInfo.getSysUser().getNickName());//type = 11
        //??????????????????????????????
        if (result == 0){
            cInsuranceVehicle.setStatus(2L);
        }
        else {
            cInsuranceVehicle.setStatus(1L);
        }
        //c_insurance_vehicle_googds??????
        CInsuranceVehicleGoogds cInsuranceVehicleGoogds = new CInsuranceVehicleGoogds();
        cInsuranceVehicleGoogds.setId(UUID.randomUUID().toString());
        cInsuranceVehicleGoogds.setLicensePlate(cInsuranceTypeTableJoinAdd.getLicensePlate());
        cInsuranceVehicleGoogds.setVehicleId(cInsuranceTypeTableJoinAdd.getVehicleId());
        cInsuranceVehicleGoogds.setInsuranceId(cInsuranceVehicle.getId());
        cInsuranceVehicleGoogds.setFrameNumber(cInsuranceTypeTableJoinAdd.getFrameNumber());
        cInsuranceVehicleGoogds.setCreateBy(tokenService.getLoginUser().getUsername());
        icInsuranceVehicleGoogdsService.insertCInsuranceVehicleGoogds(cInsuranceVehicleGoogds);

        return toAjax(cInsuranceVehicleService.insertCInsuranceVehicle(cInsuranceVehicle));
    }
    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:edit")
    @Log(title = "????????????-????????????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CInsuranceVehicle cInsuranceVehicle)
    {
        return toAjax(cInsuranceVehicleService.updateCInsuranceVehicle(cInsuranceVehicle));
    }

    /**
     * ??????????????????-????????????  ????????????
     * c_insurance_vehicle_item??????????????????????????? ??????????????????????????????
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:edit")
    @Log(title = "????????????-????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/E02EditSave")
    public AjaxResult E02EditSave(@RequestBody CInsuranceVehicleItem cInsuranceVehicleItem)
    {
        LoginUser userInfo = tokenService.getLoginUser();
        cInsuranceVehicleItem.setUpdateBy(userInfo.getUsername());
        return toAjax(icInsuranceVehicleItemService.updateCInsuranceVehicleItem(cInsuranceVehicleItem));
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:remove")
    @Log(title = "????????????-????????????", businessType = BusinessType.DELETE)
	@GetMapping("/E02Delete/{id}")
    public AjaxResult E02Delete(@PathVariable("id") String id)
    {
        CInsuranceVehicle cInsuranceVehicle = cInsuranceVehicleService.selectCInsuranceVehicleById(id);
        cInsuranceVehicle.setDeleteFlag((long) 2);
        return toAjax(cInsuranceVehicleService.updateCInsuranceVehicle(cInsuranceVehicle));
    }

    /**
     * ????????? M05
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:list")
    @PostMapping(value = {"/M05List"})
    public TableDataInfo M05List(@RequestBody CInsuranceVehicle cInsuranceVehicle)
    {
        startPage(cInsuranceVehicle.getPageNum(),cInsuranceVehicle.getPageSize());
        List<CInsuranceVehicle> cInsuranceVehicles = cInsuranceVehicleService.selectCostIn(cInsuranceVehicle);

        // ????????????
        CInsuranceVehicle insuranceVehicle = new CInsuranceVehicle();
        insuranceVehicle.setTypeId(cInsuranceVehicle.getTypeId());
        insuranceVehicle.setLicensePlates(cInsuranceVehicle.getLicensePlates());
        insuranceVehicle.setStartTime(cInsuranceVehicle.getStartTime());
        insuranceVehicle.setFinshTime(cInsuranceVehicle.getFinshTime());
        List<CInsuranceVehicle> cInsuranceVehicleList = cInsuranceVehicleService.selectCostIn(insuranceVehicle);
        BigDecimal result2 = cInsuranceVehicleList.stream().map(i -> {
            if (i.getInsuranceCost()==null){
                return BigDecimal.ZERO;
            }else {
                return i.getInsuranceCost();
            }
        }).reduce(BigDecimal.ZERO,BigDecimal::add);

        // ??????????????????
        List<Map<String,Object>> mapList = new ArrayList<>();

        // ?????????????????? ??????
        Map<String,Object> map = new HashMap<>();
        map.put("list",cInsuranceVehicles);
        map.put("totalCost",result2);
        mapList.add(map);
        return getDataTableMap(mapList,cInsuranceVehicles);
    }

    /**
     * ????????? M05??????
     */
    @PreAuthorize(hasPermi = "car:insuranceVehicle:export")
    @Log(title = "?????????", businessType = BusinessType.EXPORT)
    @PostMapping("/M05Export")
    public void export(HttpServletResponse response, CInsuranceVehicleCostExport cInsuranceVehicle) throws IOException
    {
        CInsuranceVehicle cInsuranceVehicleOne = new CInsuranceVehicle();
        cInsuranceVehicleOne.setLicensePlate(cInsuranceVehicle.getLicensePlate());
        cInsuranceVehicleOne.setTypeId(cInsuranceVehicle.getInsuranceTypeId());
        cInsuranceVehicleOne.setStartTime(cInsuranceVehicle.getStartTime());
        cInsuranceVehicleOne.setFinshTime(cInsuranceVehicle.getFinshTime());
        List<CInsuranceVehicle> cInsuranceVehicles = cInsuranceVehicleService.selectCostIn(cInsuranceVehicleOne);
        for (CInsuranceVehicle cInsuranceVehicle1 : cInsuranceVehicles){
            if (cInsuranceVehicle1.getType() ==1){
                cInsuranceVehicle1.setTypeName("????????????");
            }
            if(cInsuranceVehicle1.getType() == 2){
                cInsuranceVehicle1.setTypeName("????????????");
            }
            if(cInsuranceVehicle1.getType() == 3){
                cInsuranceVehicle1.setTypeName("????????????");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (cInsuranceVehicle1.getInsuranceStartDate() != null){
                cInsuranceVehicle1.setInsuranceStartDateExport(sdf.format(cInsuranceVehicle1.getInsuranceStartDate()));
            }
            if (cInsuranceVehicle1.getInsuranceEndDate() != null){
                cInsuranceVehicle1.setInsuranceEndDateExport(sdf.format(cInsuranceVehicle1.getInsuranceEndDate()));
            }
            if (cInsuranceVehicle1.getInsuranceHandleTime() != null){
                cInsuranceVehicle1.setInsuranceHandleTimeExport(sdf.format(cInsuranceVehicle1.getInsuranceHandleTime()));
            }
        }
        ExcelUtil<CInsuranceVehicle> util = new ExcelUtil<CInsuranceVehicle>(CInsuranceVehicle.class);
        util.exportExcel(response, cInsuranceVehicles, "insuranceCost");
    }

    /**
     * ????????????-????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:vehicle:editsave")
    @Log(title = "????????????", businessType = BusinessType.UPDATE)
    @PostMapping(value = {"/reviewCommon", "/E10EditSave", "/E03EditSave"})
    public AjaxResult E03EditSave(@Validated @RequestBody  CInsuranceVehicle cInsuranceVehicle) {
        if (StringUtils.isNull(cInsuranceVehicle.getStatus())){
            return AjaxResult.error("?????????????????????");
        }
        if (cInsuranceVehicle.getStatus()==3 && StringUtils.isEmpty(cInsuranceVehicle.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        LoginUser userInfo = tokenService.getLoginUser();
        cInsuranceVehicle.setUserid(userInfo.getUserid().toString());
        return toAjax(cInsuranceVehicleService.checkCInsuranceVehicle(cInsuranceVehicle));
    }

    @PreAuthorize(hasPermi = "pfserver:vehicle:check")
    @Log(title = "????????????")
    @PostMapping("/E03Check")
    public AjaxResult E03Check(@Validated @RequestBody ReviewSubmit reviewSubmit){
        if (reviewSubmit.getStatus()==2 && StringUtils.isEmpty(reviewSubmit.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        reviewSubmit.setUserId(tokenService.getLoginUser().getUserid().toString());
        return cInsuranceVehicleService.checkReview(reviewSubmit);
    }
}
