package com.dlzx.pfservercar.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfservercar.domain.CInsuranceVehicle;
import com.dlzx.pfservercar.domain.CInsuranceVehicleGoogds;
import com.dlzx.pfservercar.domain.CVehicle;
import com.dlzx.pfservercar.domain.CVehicleInspectItem;
import com.dlzx.pfservercar.domain.CVehicleInspectMonthly;
import com.dlzx.pfservercar.domain.CVehicleInspectMonthlyItem;
import com.dlzx.pfservercar.domain.ReviewSubmitR;
import com.dlzx.pfservercar.service.ICInsuranceVehicleItemService;
import com.dlzx.pfservercar.service.ICVehicleInspectItemService;
import com.dlzx.pfservercar.service.ICVehicleInspectMonthlyItemService;
import com.dlzx.pfservercar.service.ICVehicleInspectMonthlyService;
import com.dlzx.pfservercar.service.ICVehicleService;
import com.dlzx.system.api.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dlzx.common.log.annotation.Log;
import com.dlzx.common.log.enums.BusinessType;
import com.dlzx.common.security.annotation.PreAuthorize;
import com.dlzx.pfservercar.domain.CVehicleInspect;
import com.dlzx.pfservercar.service.ICVehicleInspectService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-??????????????????Controller
 * 
 * @author dlzx
 * @date 2020-12-08
 */
@RestController
@RequestMapping("/car/vehicleInspect")
public class CVehicleInspectController extends BaseController
{
    @Autowired
    private ICVehicleInspectService cVehicleInspectService;
    @Autowired
    private ICVehicleInspectMonthlyService cVehicleInspectMonthlyService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ICVehicleService cVehicleService;
    @Autowired
    private ICVehicleInspectMonthlyItemService cVehicleInspectMonthlyItemService;
    @Autowired
    private ICVehicleInspectItemService icVehicleInspectItemService;


    /**
     * ??????????????????h22???h23??????????????????
     * ????????????
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:add")
    @Log(title = "??????????????????-??????????????????", businessType = BusinessType.INSERT)
    @PostMapping("/H22Add")
    public AjaxResult add()
    {
        List<CVehicle> list = cVehicleService.selectCVehicleIsInspect();
        // ?????????list??????????????????????????? ?????????cInsuranceVehicleGoogds????????????????????????
        // ?????????????????????
        int size = list.size();
        CVehicleInspectMonthly cVehicleInspectMonthly = new CVehicleInspectMonthly();
        //??????ID
        String s = UUID.randomUUID().toString();
        cVehicleInspectMonthly.setId(s);
        cVehicleInspectMonthly.setInspectDate(new Date());
        cVehicleInspectMonthly.setTotalNumber(Long.valueOf(size));
        cVehicleInspectMonthly.setCompleteNumber(Long.valueOf(0));
        cVehicleInspectMonthly.setIncompleteNumber(Long.valueOf(size));
        cVehicleInspectMonthlyService.insertCVehicleInspectMonthly(cVehicleInspectMonthly);

        // new ????????????????????????cVehicleInspectMonthlyItem
        // ??????????????????
        List<CVehicleInspectMonthlyItem> list1 = new ArrayList<>();

        // ?????????ID ??????????????????cVehicleInspectMonthlyItem??????
        for (CVehicle cVehicle : list) {
            CVehicleInspectMonthlyItem cVehicleInspectMonthlyItem = new CVehicleInspectMonthlyItem();
            cVehicleInspectMonthlyItem.setId(UUID.randomUUID().toString());
            // ??????MonthlyId
            cVehicleInspectMonthlyItem.setMonthlyId(s);
            // ????????????????????????ID ?????????cVehicleInspectMonthlyItem
            cVehicleInspectMonthlyItem.setVehicleId(cVehicle.getId());
            // ?????????????????????????????????cVehicleInspectMonthlyItem
            cVehicleInspectMonthlyItem.setLicensePlate(cVehicle.getLicensePlate());
            // ??????????????????1????????? sql???????????????
            list1.add(cVehicleInspectMonthlyItem);
        }
        // ????????????
        return toAjax(cVehicleInspectMonthlyItemService.insertList(list1));
    }

    /**
     * ??????????????????-????????????????????????
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:edit")
    @GetMapping(value = {"/H20GetInfo/{monthlyId}"})
    public TableDataInfo H20GetInfo(@PathVariable("monthlyId") String monthlyId)
    {
        List<CVehicleInspect> list = cVehicleInspectService.selectCVehicleInspectInfo(monthlyId);
        List<CVehicleInspectItem> list1 = new ArrayList<>();
        for (CVehicleInspect vehicleInspect : list) {
            CVehicleInspectItem cVehicleInspectItem = new CVehicleInspectItem();
            // item_type
            cVehicleInspectItem.setItemType(vehicleInspect.getItemType());
            // name????????????
            cVehicleInspectItem.setName(vehicleInspect.getName());
            // status????????????
            cVehicleInspectItem.setStatus(vehicleInspect.getCviiStatus());
            // files????????????
            cVehicleInspectItem.setFiles(vehicleInspect.getFiles());
            list1.add(cVehicleInspectItem);
            vehicleInspect.setcVehicleInspectItemList(list1);
        }
        ArrayList<CVehicleInspect> collect = list.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(
                                CVehicleInspect::getId))), ArrayList::new));
        return getDataTable(collect);
    }

    /**
     * ??????????????????-???????????????????????? YLH
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:list")
    @PostMapping(value = {"/H19List"})
    public TableDataInfo H19List(@RequestBody CVehicleInspect cVehicleInspect)
    {
        startPage(cVehicleInspect.getPageNum(), cVehicleInspect.getPageSize());
        List<CVehicleInspect> list = cVehicleInspectService.selectCVehicleInspectList(cVehicleInspect);
        return getDataTable(list);
    }

    /**
     * ??????????????????-?????????????????????????????? YLH
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:query")
    @GetMapping(value = "/H20List/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        CVehicleInspect cVehicleInspect = cVehicleInspectService.selectCVehicleInspectById(id);
        List<CVehicleInspectItem> cVehicleInspectItem =icVehicleInspectItemService.selectCVehicleInspectItemByInspectId(id);
        cVehicleInspect.setcVehicleInspectItemList(cVehicleInspectItem);
        return AjaxResult.success(cVehicleInspect);
    }

    /**
     * ??????????????????
     * ?????????????????? 1?????? 2?????????
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:edit")
    @Log(title = "????????????-????????????????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/H20Review")
    public AjaxResult H20Review(@Validated @RequestBody ReviewSubmitR reviewSubmit)
    {
        if (reviewSubmit.getStatus()==2 && StringUtils.isEmpty(reviewSubmit.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        // ???????????????
        LoginUser userInfo = tokenService.getLoginUser();
        reviewSubmit.setUserId(userInfo.getUserid()+"");
        return cVehicleInspectService.checkCInsuranceVehicle(reviewSubmit);
    }

    /**
     * ??????????????????-??????????????????
     */
    @PreAuthorize(hasPermi = "car:vehicleInspect:remove")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(cVehicleInspectService.deleteCVehicleInspectByIds(ids));
    }
}
