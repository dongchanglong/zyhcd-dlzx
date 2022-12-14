package com.dlzx.pfservercar.controller.driver_app;

import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.web.page.TableDataInfo;
import com.dlzx.common.log.annotation.Log;
import com.dlzx.common.security.annotation.PreAuthorize;
import com.dlzx.common.security.service.DriverTokenService;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfservercar.client.SystemClient;
import com.dlzx.pfservercar.domain.CVehicle;
import com.dlzx.pfservercar.domain.CVehicleH;
import com.dlzx.pfservercar.domain.Common.SubmitVo;
import com.dlzx.pfservercar.domain.RRepair;
import com.dlzx.pfservercar.domain.RRepairAp;
import com.dlzx.pfservercar.domain.RRepairDetailed;
import com.dlzx.pfservercar.domain.RRepairItem;
import com.dlzx.pfservercar.domain.RRepairItemDate;
import com.dlzx.pfservercar.domain.SafeAccident;
import com.dlzx.pfservercar.domain.SysExamineTask;
import com.dlzx.pfservercar.service.ICVehicleService;
import com.dlzx.pfservercar.service.IRRepairDetailedService;
import com.dlzx.pfservercar.service.IRRepairItemDateService;
import com.dlzx.pfservercar.service.IRRepairItemService;
import com.dlzx.pfservercar.service.IRRepairService;
import com.dlzx.pfservercar.service.ISafeAccidentService;
import com.dlzx.pfservercar.service.ITRouteService;
import com.dlzx.system.api.model.LoginDriver;
import com.dlzx.system.api.model.LoginUser;
import io.swagger.annotations.Api;
import org.checkerframework.checker.index.qual.LengthOf;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.dlzx.common.core.web.controller.BaseController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * @author ylh
 * @program:dlzx
 * @description?????????????????????App?????????
 * @create:2020-12-08 21-03
 */
@RestController
@RequestMapping("/driver-api/repair")
public class RRepairMtnAppController extends BaseController {

    @Autowired
    private IRRepairService rRepairService;
    @Autowired
    private IRRepairItemDateService repairItemDateService;
    @Autowired
    private DriverTokenService driverTokenService;
    @Autowired
    private ICVehicleService vehicleService;
    @Autowired
    private IRRepairItemService repairItemService;
    @Autowired
    private ISafeAccidentService safeAccidentService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private IRRepairDetailedService repairDetailedService;
    @Autowired
    private SystemClient systemClient;

    /**
     * ????????????????????????App
     */
    @RequestMapping("/H01RepairList")
    @ResponseBody
    @Log(title = "????????????????????????App")
    public TableDataInfo H01RepairList(@RequestBody RRepairAp rRepairAp) {
        startPage(rRepairAp.getPageNum(), rRepairAp.getPageSize());
        RRepair rRepair = new RRepair();
        rRepair.setStatus(rRepairAp.getStatus());
        rRepair.setRepairStatus(rRepairAp.getRepairStatus());
        rRepair.setRepairType(rRepairAp.getRepairType());
        rRepair.setDriverId(driverTokenService.getLoginDriver().getUserid());
        List<RRepair> rRepairList = rRepairService.selectRRepairAppList(rRepair);
        List<RRepair> rRepairs = rRepairList.stream().filter(a -> a.getDriverId().equals(driverTokenService.getLoginDriver().getUserid()))
                .collect(Collectors.toList());
        return getDataTable(rRepairs);
    }

    /**
     * ????????????(?????????)
     */
    @RequestMapping("/H01Info")
    @ResponseBody
    @Log(title = "???????????????????????????App")
    public AjaxResult H01Info(@RequestBody RRepairAp rRepairAp) {
        RRepair rRepair = rRepairService.selectRRepairById(rRepairAp.getId());
        if (null == rRepair) {
            return AjaxResult.error("???????????????????????????");
        } else {
//            List<RRepairItemDate> rRepairItemDate = repairItemDateService.selectRRepairItemDateById(rRepairAp.getId());
//            Map<String,Object> map = new HashMap<>();
//            map.put("rRepairList",rRepair);
//            map.put("rRepairDate",rRepairItemDate);
            return AjaxResult.success(rRepair);
        }
    }

    /**
     * ????????????????????????App
     */
    @RequestMapping("/H01Add")
    @ResponseBody
    @Log(title = "????????????????????????App")
    public AjaxResult H01Add(@RequestBody RRepair rRepair) {
        if (rRepair.getIsOther() == null){
            return AjaxResult.error("???????????????????????????");
        }
        else {
            if (rRepair.getIsOther() == 1L){
                if (rRepair.getAccidentVideo() == null){
                    return AjaxResult.error("???????????????");
                }
            }
        }

        if (rRepair.getMainLicensePlate() == null ||rRepair.getMainVehicleModel() == null){
            return AjaxResult.error("?????????????????????");
        }
        if (rRepair.getMileage()== null){
            return AjaxResult.error("????????????????????????");
        }
        if (rRepair.getRouteId() == null){
            return AjaxResult.error("?????????????????????");
        }


        if (rRepair.getRepairType()==null){
            return AjaxResult.error("?????????????????????");
        }
        else {
            if (rRepair.getRepairType() == 3L ){
                if (rRepair.getType() == 2L){
                    if (rRepair.getHangLicensePlate() == null || rRepair.getHangVehicleModel() == null){
                        return AjaxResult.error("?????????????????????");
                    }
                }
            }
            else {
                if (rRepair.getAccidentLocation() == null){
                    return AjaxResult.error("?????????????????????");
                }
                if (rRepair.getTotalMass() == null){
                    return AjaxResult.error("????????????????????????/?????????");
                }
                if (rRepair.getRoadCondition() == null){
                    return AjaxResult.error("????????????????????????");
                }
                if (rRepair.getWeatherCondition() == null){
                    return AjaxResult.error("?????????????????????");
                }
                if (rRepair.getType() == 2L){
                    if (rRepair.getHangLicensePlate() == null || rRepair.getHangVehicleModel() == null){
                        return AjaxResult.error("?????????????????????");
                    }
                }
            }
        }
        Calendar date = Calendar.getInstance();
        String data = String.valueOf(date.get(Calendar.YEAR)) + String.valueOf(date.get(Calendar.MONTH)) + String.valueOf(date.get(Calendar.DATE));
        String ms = String.valueOf(date.get(Calendar.HOUR_OF_DAY)) + String.valueOf(date.get(Calendar.MINUTE)) + String.valueOf(date.get(Calendar.SECOND));
        if (StringUtils.isEmpty(rRepair.getId())) {
            rRepair.setId(UUID.randomUUID().toString());
            rRepair.setRepairStatus(1L);
            switch (rRepair.getRepairType().intValue()) {
                case 1:
                    if (rRepair.getType() == 1L) {
                        rRepair.setCode("ZCZC" + data + ms);
                    } else {
                        rRepair.setCode("ZCGC" + data + ms);
                    }
                    break;
                case 2:
                    if (rRepair.getType() == 1L) {
                        rRepair.setCode("SGZC" + data + ms);
                    } else {
                        rRepair.setCode("SGGC" + data + ms);
                    }
                    break;
                case 3:
                    if (rRepair.getType() == 1L) {
                        rRepair.setCode("BYZC" + data + ms);
                    } else {
                        rRepair.setCode("BYGC" + data + ms);
                    }
                    break;
                default:
                    assert false;
                    break;
            }
            rRepair.setDriverId(driverTokenService.getLoginDriver().getUserid());
            rRepair.setDriverName(driverTokenService.getLoginDriver().getUsername());
            List<RRepairItemDate> rRepairItemDateList = new ArrayList<>();
            for (RRepairItemDate rRepairItemDate : rRepair.getRepairItemDate()) {
                for (String files : rRepairItemDate.getFileList()) {
                    RRepairItemDate repairItemDate = new RRepairItemDate();
                    repairItemDate.setId(UUID.randomUUID().toString());
                    repairItemDate.setRepairId(rRepair.getId());
                    repairItemDate.setFiles(files);
                    repairItemDate.setItemId(rRepairItemDate.getItemId());
                    repairItemDate.setItemName(rRepairItemDate.getItemName());
                    repairItemDate.setRemarks(rRepairItemDate.getRemarks());
                    rRepairItemDateList.add(repairItemDate);
                }
            }
            rRepair.setRepairItemDate(rRepairItemDateList);
            rRepair.setStatus(1L);
            rRepair.setDeleteFlag(1L);
            return toAjax(rRepairService.insertAffair(rRepair));
        } else {
            rRepair.setStatus(1L);
            rRepair.setGarageStatus(1L);
            rRepair.setRepairStatus(1L);
            rRepair.setUpdateBy(driverTokenService.getLoginDriver().getUsername());
            rRepair.setUpdateTime(new Date());
            repairItemDateService.deleteRRepairItemDateBatch(rRepair.getId());
            return toAjax(rRepairService.updateAffair(rRepair));
        }
    }

    /**
     * ????????????????????????App????????????????????????????????????
     *
     * @param
     * @return
     */
    @Log(title = "????????????????????????App????????????????????????????????????")
    @PostMapping("/H01Submit")
    @ResponseBody
    public AjaxResult H01Submit(@RequestBody SubmitVo submitVo) {
        return toAjax(rRepairService.submit(submitVo));
    }

    /**
     * ??????-??????????????????App
     */
    @RequestMapping("/H06LicensePlate")
    @ResponseBody
    @Log(title = "??????-??????????????????App")
    public AjaxResult H06LicensePlate() {
        CVehicle cVehicle = new CVehicle();
        //?????????????????????????????????
        List<CVehicle> cVehicleList = vehicleService.selectCVehicleList(cVehicle);
        List<CVehicleH> vehicleHMainList = new ArrayList<>();//??????list
        List<CVehicleH> vehicleHHangList = new ArrayList<>();//??????list
        for (CVehicle vehicle : cVehicleList) {
            CVehicleH cVehicleH = new CVehicleH();
            cVehicleH.setId(vehicle.getId());
            cVehicleH.setVehicleModel(vehicle.getVehicleModel());
            cVehicleH.setLicensePlate(vehicle.getLicensePlate());
            cVehicleH.setVehicleModelName(vehicle.getVehicleModelName());
            //????????????????????????????????????
            if (vehicle.getVehicleTypeName().equals("??????")) {
                vehicleHMainList.add(cVehicleH);
            } else {
                vehicleHHangList.add(cVehicleH);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("main", vehicleHMainList);
        map.put("hang", vehicleHHangList);
        return AjaxResult.success(map);
    }

    /**
     * ??????-???????????????????????????App
     */
    @RequestMapping("/H08RepairItemInfo")
    @ResponseBody
    @Log(title = "??????-???????????????????????????App")
    public AjaxResult H08RepairItemInfo(@RequestBody RRepairAp rRepairAp) {
        List<RRepairItem> rRepairItemList = repairItemService.selectRRepairItemByType(rRepairAp.getStatus());
        return AjaxResult.success(rRepairItemList);
    }

    /**
     * ??????-???????????????App
     */
    @RequestMapping("/H08Accident")
    @ResponseBody
    @Log(title = "??????-???????????????App")
    public AjaxResult H08Accident(@RequestBody RRepairAp rRepairAp) {
        List<SafeAccident> safeAccidentList = safeAccidentService.selectSafeAccidentByType(rRepairAp.getId());
        for (SafeAccident safeAccident : safeAccidentList) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            safeAccident.setContentBox(formatter.format(safeAccident.getAccidentTime()) + safeAccident.getAccidentPlace());
        }
        return AjaxResult.success(safeAccidentList);
    }

    /**
     * ??????????????????????????????App
     */
    @RequestMapping("/H01Revoke")
    @ResponseBody
    @Log(title = "??????????????????????????????App")
    public AjaxResult H01Revoke(String id) {
        systemClient.deleteExamineTaskByRelationId(id);
        RRepair rRepair = rRepairService.selectRRepairById(id);
        if (null == rRepair) {
            return AjaxResult.error("????????????????????????");
        }
        rRepair.setDeleteFlag(2L);
        return toAjax(rRepairService.updateRRepair(rRepair));
    }

    /**
     * ??????-??????????????????App
     */
    @RequestMapping("/H02Save/{id}")
    @ResponseBody
    @Log(title = "??????-??????????????????App")
    public AjaxResult H02Save(@PathVariable("id") String id) {
        RRepair rRepair = rRepairService.selectRRepairById(id);
        if (null == rRepair) {
            return AjaxResult.error("????????????????????????");
        }
        if (rRepair.getStatus() != 2L || rRepair.getGarageStatus() != 3L) {
            return AjaxResult.error("?????????????????????????????????");
        }
        rRepair.setStatus(4L);
        rRepair.setRepairStatus(3L);
        rRepair.setCompleteTime(new Date());
        long diff = rRepair.getCompleteTime().getTime() - rRepair.getReviewTime().getTime();
        long min = diff % (1000 * 24 * 60 * 60) % (1000 * 60 * 60) / (1000 * 60);
        rRepair.setRepairDuration(min);
        rRepair.setUpdateBy(driverTokenService.getLoginDriver().getUsername());
        return toAjax(rRepairService.updateRRepair(rRepair));
    }

    /**
     * ??????????????????????????????App????????????
     */
    @RequestMapping("/H03Edit")
    @ResponseBody
    @Log(title = "??????????????????????????????App????????????")
    public AjaxResult H03Edit(String id) {
        List<RRepairItemDate> rRepairItemDate = repairItemDateService.selectRRepairItemDateByRepairId(id);
        return AjaxResult.success(rRepairItemDate);
    }

    /**
     * ??????????????????????????????App??????
     */
    @RequestMapping("/H03EditSave")
    @ResponseBody
    @Log(title = "??????????????????????????????App??????")
    public AjaxResult H03EditSave(@RequestBody RRepairAp rRepairAp) {
        RRepair rRepair = rRepairService.selectRRepairById(rRepairAp.getId());
        rRepair.setIsOther(rRepairAp.getIsOther());
        rRepair.setRepairType(rRepairAp.getRepairType());
        rRepairService.updateRRepair(rRepair);
        List<RRepairItemDate> rRepairItemDateList = repairItemDateService.selectRRepairItemDateByRepairId(rRepairAp.getId());
        String[] ids = new String[rRepairItemDateList.size()];
        int i = 0;
        for (RRepairItemDate rRepairItemDate : rRepairItemDateList) {
            if (i <= rRepairItemDateList.size()) {
                ids[i] = rRepairItemDate.getId();
                i++;
            }
        }
        //??????????????????
        repairItemDateService.deleteRRepairItemDateByIds(ids);
        //????????????
        for (RRepairItemDate rRepairItemDate : rRepairAp.getRepairItemDate()) {
            rRepairItemDate.setId(UUID.randomUUID().toString());
            rRepairItemDate.setRepairId(rRepairAp.getId());
        }
        return toAjax(repairItemDateService.insertRRepairItemDateBatch(rRepairAp.getRepairItemDate()));
    }

    /***
     *
     * @param relationId
     * @return
     */
    @RequestMapping("/H01RecordForm")
    @ResponseBody
    @Log(title = "??????????????????????????????")
    public AjaxResult H01RecordForm(String relationId) {
        SysExamineTask sysExamineTask = new SysExamineTask();
        sysExamineTask.setRelationId(relationId);
        SysExamineTask sysExamineTaskList = repairItemDateService.queryRecord(sysExamineTask);
        Map<String, Object> map = new HashMap<>();
        map.put("reviewRecordList", sysExamineTaskList.getSysExamineUserTaskList());
        map.put("copyRecordList", sysExamineTaskList.getSysDuplicateUserTaskList());
        return AjaxResult.success(map);
    }

    /**
     * ?????????????????????
     */
    @RequestMapping("/H02Upload")
    @ResponseBody
    @Log(title = "?????????????????????")
    public AjaxResult H02Upload(@RequestBody RRepair rRepair) {
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        if (null == rRepairOne) {
            return AjaxResult.error("????????????????????????");
        }
        rRepairOne.setRepairDetialPhoto(rRepair.getRepairDetialPhoto());
        return toAjax(rRepairService.updateRRepair(rRepairOne));
    }

    @GetMapping("/H03AllListDetail/{repairId}")
    @ResponseBody
    public AjaxResult H03AllListDetail(@PathVariable("repairId") String repairId) {
        List<RRepairDetailed> rRepairDetailedList = repairDetailedService.selectRRepairDetailedAllByGarageId(repairId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (RRepairDetailed rRepairDetailed : rRepairDetailedList) {
            totalCost = totalCost.add(rRepairDetailed.getAmount());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("detailedList", rRepairDetailedList);
        map.put("totalCost", totalCost);
        return AjaxResult.success(map);
    }

}
