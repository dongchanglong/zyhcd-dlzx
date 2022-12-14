package com.dlzx.pfservercar.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.dlzx.common.core.exception.CustomException;
import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfservercar.client.SystemClient;
import com.dlzx.pfservercar.domain.AddRepairRecord;
import com.dlzx.pfservercar.domain.RGarageRepairItem;
import com.dlzx.pfservercar.domain.RRepairDetailed;
import com.dlzx.pfservercar.domain.RRepairItem;
import com.dlzx.pfservercar.domain.RRepairItemDate;
import com.dlzx.pfservercar.domain.RRepairOutExcel;
import com.dlzx.pfservercar.domain.ReviewSubmit;
import com.dlzx.pfservercar.domain.ReviewSubmitR;
import com.dlzx.pfservercar.service.IRGarageRepairItemService;
import com.dlzx.pfservercar.service.IRRepairItemDateService;
import com.dlzx.pfservercar.service.impl.RRepairDetailedServiceImpl;
import com.dlzx.system.api.model.LoginUser;
import org.springframework.beans.BeanUtils;
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
import com.dlzx.pfservercar.domain.RRepair;
import com.dlzx.pfservercar.service.IRRepairService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-???????????????Controller
 * 
 * @author yangluhe
 * @date 2020-12-04
 */
@RestController
@RequestMapping("/car/repair")
public class RRepairController extends BaseController
{
    @Autowired
    private IRRepairService rRepairService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RRepairDetailedServiceImpl rRepairDetailedService;
    @Autowired
    private IRGarageRepairItemService garageRepairItemService;
    @Autowired
    private IRRepairItemDateService repairItemDateService;
    @Autowired
    private SystemClient systemClient;


    /**
     * ??????????????????-?????????????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:repair:list")
    @PostMapping("/H01List")
    public TableDataInfo H01List(@RequestBody RRepair rRepair)
    {
        startPage(rRepair.getPageNum(),rRepair.getPageSize());
        List<RRepair> list = rRepairService.selectRRepairList(rRepair);
        return getDataTable(list);
    }


    /**
     * ??????????????????-???????????????????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:repair:info")
    @GetMapping(value = "/H03Info/{id}")
    public AjaxResult H03Info(@PathVariable("id") String id)
    {
        RRepair rRepair = rRepairService.selectRRepairById(id);
        return AjaxResult.success(rRepair);
    }


    /**
     * ?????????????????????-???????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:repair:reviewOnelevel")
    @Log(title = "????????????-???????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/H02ReviewOneLevel")
    public AjaxResult H02ReviewOneLevel(@RequestBody RRepair rRepair)
    {
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        if (rRepair.getStatus() == 1){
            rRepairOne.setStatus(Long.valueOf(2));
        }
        else {
            rRepairOne.setStatus(Long.valueOf(3));
        }
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairOne.setUpdateBy(userInfo.getUsername());
        rRepairOne.setReviewRemarks(rRepair.getReviewRemarks());
        rRepairOne.setGarageId(rRepair.getGarageId());
        rRepairOne.setGarageName(rRepair.getGarageName());
        return toAjax(rRepairService.updateRRepair(rRepairOne));
    }

    /**
     * ???????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:repair:reviewtwolevel")
    @Log(title = "???????????????",businessType = BusinessType.OTHER)
    @PostMapping("/H02ReviewTwoLevel")
    public AjaxResult H02ReviewTwoLevel(@RequestBody RRepair rRepair){
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairOne.setUpdateBy(userInfo.getUsername());
        rRepairOne.setReviewRemarks(rRepair.getReviewRemarks());
        rRepairOne.setGarageStatus(2L);
        rRepairOne.setFileUrls(rRepair.getFileUrls());

        // ??????????????????????????????????????????????????????????????????????????????
        systemClient.updateStationStatus(rRepair.getId());
        return toAjax(rRepairService.updateRRepairUpload(rRepairOne));
    }

    /**
     * ??????????????????????????????
     */
    @Log(title = "??????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repair:infodetailnotnewlist")
    @PostMapping("/H03InfoDetailList/{garageId}")
    public AjaxResult H03InfoDetailList(@PathVariable("garageId")String garageId){
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedNotNewList(garageId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (RRepairDetailed rRepairDetailed:rRepairDetailedList){
            totalCost = totalCost.add(rRepairDetailed.getAmount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("detailedList",rRepairDetailedList);
        map.put("totalCost",totalCost);
        return AjaxResult.success(map);
    }


    /**
     * ????????????????????????????????????list???
     */
    @Log(title = "????????????????????????????????????list???",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairstation:liststation")
    @PostMapping("/H09List")
    public TableDataInfo H09List(@RequestBody RRepair rRepair){
        startPage(rRepair.getPageNum(),rRepair.getPageSize());
        rRepair.setGarageId(tokenService.getLoginUser().getSysUser().getGarageId());
        List<RRepair> rRepairList = rRepairService.selectRRepairDriverPass(rRepair);
        return getDataTable(rRepairList);
    }

    /**
     * ?????????????????????????????????????????????
     */
    @Log(title = "?????????????????????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairgarage:list")
    @PostMapping("/H05List")
    public TableDataInfo H05List(@RequestBody RRepair rRepair){
        startPage(rRepair.getPageNum(),rRepair.getPageSize());
        rRepair.setGarageId(tokenService.getLoginUser().getSysUser().getGarageId());
        List<RRepair> rRepairList = rRepairService.selectRRepairStationReview(rRepair);
        return getDataTable(rRepairList);
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    @Log(title = "???????????????????????????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:updatestation")
    @PostMapping("/H05EditStation")
    public AjaxResult H05EditStation(@RequestBody RRepair rRepair){
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        if (null == rRepairOne){
            return AjaxResult.error("??????????????????");
        }
        if (rRepair.getGarageStatus() == 1){
            rRepairOne.setGarageStatus(Long.valueOf(3));
        }
        else {
            rRepairOne.setGarageStatus(Long.valueOf(4));
        }
        rRepairOne.setGarageReviewRemarks(rRepair.getGarageReviewRemarks());
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairOne.setUpdateBy(userInfo.getUsername());
        return toAjax(rRepairService.updateRRepair(rRepairOne));
    }

    /**
     * ??????(???????????????)????????????????????????
     */
    @Log(title = "??????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairgarage:infodetailisnewlist")
    @PostMapping("/H07InfoDetailList/{garageId}")
    public AjaxResult H07InfoDetailList(@PathVariable("garageId")String garageId){
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedNotNewList(garageId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (RRepairDetailed rRepairDetailed:rRepairDetailedList){
            totalCost = totalCost.add(rRepairDetailed.getAmount());
        }
        List<RRepairDetailed> rRepairDetailedListOne = rRepairDetailedService.selectRRepairDetailedIsNewList(garageId);
        for (RRepairDetailed rRepairDetailed:rRepairDetailedListOne){
            totalCost = totalCost.add(rRepairDetailed.getAmount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("detailedNotNewList",rRepairDetailedList);
        map.put("detailedIsNewList",rRepairDetailedListOne);
        map.put("totalCost",totalCost);
        return AjaxResult.success(map);
    }

    /**
     * (?????????????????????)???????????????????????????
     */
    @Log(title = "?????????????????????????????????????????????",businessType = BusinessType.ITEM)
    @PreAuthorize(hasPermi = "pfservercar:repair:info")
    @GetMapping("/H08DetailAllList/{repairId}")
    public AjaxResult H08DetailAllList(@PathVariable("repairId")String repairId){

        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedAllByGarageId(repairId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for(RRepairDetailed rRepairDetailed:rRepairDetailedList){
            totalCost = totalCost.add(rRepairDetailed.getAmount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("detailedList",rRepairDetailedList);
        map.put("totalCost",totalCost);
        return AjaxResult.success(map);
    }

    @Log(title = "??????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairisnew:infolist")
    @PostMapping("/H06AddList")
    public TableDataInfo H06AddList(@RequestBody AddRepairRecord addRepairRecord){
        startPage(addRepairRecord.getPageNum(),addRepairRecord.getPageSize());
        RRepairDetailed rRepairDetailed = new RRepairDetailed();
        rRepairDetailed.setCode(addRepairRecord.getCode());
        rRepairDetailed.setMainLicensePlate(addRepairRecord.getMainLicensePlate());
        rRepairDetailed.setHangLicensePlate(addRepairRecord.getHangLicensePlate());
        rRepairDetailed.setGarageName(addRepairRecord.getGarageName());
        rRepairDetailed.setName(addRepairRecord.getName());
        //??????????????????
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedAddListRecord(rRepairDetailed);
        //??????????????????
        List<AddRepairRecord> addRepairRecordList = new ArrayList<>();
        for (RRepairDetailed rRepairDetailedOne:rRepairDetailedList){
            AddRepairRecord addRepairRecordOne = new AddRepairRecord();
            addRepairRecordOne.setId(rRepairDetailedOne.getId());
            addRepairRecordOne.setCode(rRepairDetailedOne.getRepair().getCode());
            addRepairRecordOne.setGarageName(rRepairDetailedOne.getGarageName());
            addRepairRecordOne.setHangLicensePlate(rRepairDetailedOne.getRepair().getHangLicensePlate());
            addRepairRecordOne.setMainLicensePlate(rRepairDetailedOne.getRepair().getMainLicensePlate());
            addRepairRecordOne.setCompleteTime(rRepairDetailedOne.getRepair().getCompleteTime());
            addRepairRecordOne.setName(rRepairDetailedOne.getName());
            addRepairRecordOne.setUnit(rRepairDetailedOne.getUnit());
            addRepairRecordOne.setUnitPrice(rRepairDetailedOne.getUnitPrice());
            addRepairRecordOne.setDuration(rRepairDetailedOne.getDuration());
            addRepairRecordOne.setAmount(rRepairDetailedOne.getAmount());
            addRepairRecordOne.setNumber(rRepairDetailedOne.getNumber());
            addRepairRecordList.add(addRepairRecordOne);
        }
        return getDataTable(addRepairRecordList);
    }


    /**
     * ???????????????????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:repairisnew:infoexport")
    @Log(title = "???????????????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/H06Export")
    public void H06Export(HttpServletResponse response, AddRepairRecord addRepairRecord) throws IOException
    {
        RRepairDetailed rRepairDetailed = new RRepairDetailed();
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedAddListRecord(rRepairDetailed);
        List<AddRepairRecord> addRepairRecordList = new ArrayList<>();
        for (RRepairDetailed rRepairDetailedOne:rRepairDetailedList){
            AddRepairRecord addRepairRecordOne = new AddRepairRecord();
            addRepairRecordOne.setId(rRepairDetailedOne.getId());
            addRepairRecordOne.setCode(rRepairDetailedOne.getRepair().getCode());
            addRepairRecordOne.setGarageName(rRepairDetailedOne.getGarageName());
            addRepairRecordOne.setHangLicensePlate(rRepairDetailedOne.getRepair().getHangLicensePlate());
            addRepairRecordOne.setMainLicensePlate(rRepairDetailedOne.getRepair().getMainLicensePlate());
            addRepairRecordOne.setCompleteTime(rRepairDetailedOne.getRepair().getCompleteTime());
            addRepairRecordOne.setName(rRepairDetailedOne.getName());
            addRepairRecordOne.setUnit(rRepairDetailedOne.getUnit());
            addRepairRecordOne.setUnitPrice(rRepairDetailedOne.getUnitPrice());
            addRepairRecordOne.setDuration(rRepairDetailedOne.getDuration());
            addRepairRecordOne.setAmount(rRepairDetailedOne.getAmount());
            addRepairRecordOne.setNumber(rRepairDetailedOne.getNumber());
            addRepairRecordList.add(addRepairRecordOne);
        }
        ExcelUtil<AddRepairRecord> util = new ExcelUtil<AddRepairRecord>(AddRepairRecord.class);
        util.exportExcel(response, addRepairRecordList, "addRepairRecord");
    }


    /**
     * ?????????????????????????????????????????????
     */
    @Log(title = "?????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PreAuthorize(hasPermi = "pfservercar:repairstation:liststation")
    @PostMapping("/H10NotSubmittedReviewEdit")
    public AjaxResult H10NotSubmittedReviewEdit(@RequestBody RRepair rRepair){
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        if (null == rRepairOne){
            return AjaxResult.error("??????????????????");
        }
        rRepairOne.setGarageReviewRemarks(rRepair.getGarageReviewRemarks());
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairOne.setUpdateBy(userInfo.getUsername());
        rRepairOne.setGarageStatus(Long.valueOf(1));
        return toAjax(rRepairService.updateRRepair(rRepairOne));
    }

    /**
     * ???????????????????????????????????????????????????
     */
    @Log(title = "?????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PreAuthorize(hasPermi = "pfservercar:repairstation:liststationsubmit")
    @PostMapping("/H10SubmittedStation")
    public AjaxResult H10SubmittedStation(@RequestBody RRepair rRepair){
        RRepair rRepairOne = rRepairService.selectRRepairById(rRepair.getId());
        if (null == rRepairOne){
            return AjaxResult.error("??????????????????");
        }
        rRepairOne.setGarageStatus(Long.valueOf(2));
        return toAjax(rRepairService.updateRRepair(rRepairOne));
    }


    /**
     * ???????????????????????????????????????????????????????????????
     * @return
     */
    @Log(title = "???????????????????????????????????????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repair:info")
    @GetMapping("/H10NotSubmittedList/{repairId}")
    public AjaxResult H10NotSubmittedList(@PathVariable("repairId")String repairId){
        BigDecimal totalCost = BigDecimal.ZERO;
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedNotNewList(repairId);
        for (RRepairDetailed rRepairDetailedOne:rRepairDetailedList){
            totalCost = totalCost.add(rRepairDetailedOne.getAmount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("totalCost",totalCost);
        map.put("notSubmittedList",rRepairDetailedList);
        return AjaxResult.success(map);
    }

    /**
     * ???????????????????????????????????????????????????
     * @param id
     * @return rGarageRepairItem
     */
    @Log(title = "???????????????????????????????????????????????????????????????????????????",businessType = BusinessType.INSERT)
    @GetMapping(value = "/H10NotSubmittedAddInfo/{id}")
    public AjaxResult H10NotSubmittedAddInfo(@PathVariable("id")String id){
        RGarageRepairItem rGarageRepairItem = garageRepairItemService.selectRGarageRepairItemById(id);
        return AjaxResult.success(rGarageRepairItem);
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????",businessType = BusinessType.INSERT)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:notnewadd")
    @PostMapping("/H10NotSubmittedAdd")
    public AjaxResult H10NotSubmittedAdd(@RequestBody RRepairDetailed rRepairDetailed){
        if (StringUtils.isNull(rRepairDetailed.getUnitPrice())){
            return AjaxResult.error("??????????????????");
        }
        if (StringUtils.isNull(rRepairDetailed.getDuration())){
            return AjaxResult.error("?????????????????????");
        }
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairDetailed.setId(UUID.randomUUID().toString());
        rRepairDetailed.setCreateBy(userInfo.getUsername());
        rRepairDetailed.setIsNew(Long.valueOf(2));
        rRepairDetailed.setDeleteFlag(Long.valueOf(1));
        //????????????
        BigDecimal number = new BigDecimal(rRepairDetailed.getNumber());
        if (rRepairDetailed.getDuration()!= null && rRepairDetailed.getDuration().compareTo(BigDecimal.ZERO)>0){
            BigDecimal total = rRepairDetailed.getUnitPrice().multiply(rRepairDetailed.getDuration());
            BigDecimal amount = total.multiply(number);
            rRepairDetailed.setAmount(amount);
        }else {
            BigDecimal amount = rRepairDetailed.getUnitPrice().multiply(number);
            rRepairDetailed.setAmount(amount);
        }
        //???????????????
        RRepairDetailed repairDetailed = new RRepairDetailed();
        repairDetailed.setName(rRepairDetailed.getName());
        repairDetailed.setVehicleModel(rRepairDetailed.getVehicleModel());
        //????????????????????????/????????????
        RRepair rRepair = rRepairService.selectRRepairById(rRepairDetailed.getRepairId());
        String licensePlate = null;
        if(rRepair.getType() == 1){
            licensePlate = rRepair.getMainLicensePlate();
        }
        else {
            licensePlate = rRepair.getHangLicensePlate();
        }
        repairDetailed.setLicensePlate(licensePlate);


        // ?????????????????????
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.MONTH,1);
        Date finishTime = calendar.getTime();
        repairDetailed.setStartTime(startTime);
        repairDetailed.setFinshTime(finishTime);


        Long month = 1L;
        Long year = 1L;
        RRepairDetailed monthAndYearNumber = rRepairDetailedService.selectMonthNumber(repairDetailed);
        if (monthAndYearNumber == null){
            rRepairDetailed.setMonthNumber(1L);
            rRepairDetailed.setYearNumber(1L);
        }
        else {
            rRepairDetailed.setMonthNumber(monthAndYearNumber.getmNumber()+month);
            rRepairDetailed.setYearNumber(monthAndYearNumber.getyNumber()+year);
        }
        return toAjax(rRepairDetailedService.insertRRepairDetailed(rRepairDetailed));
    }


    @Log(title = "?????????????????????????????????????????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:notnewedit")
    @GetMapping(value = "/H10NotSubmittedEdit/{id}")
    public AjaxResult H10NotSubmittedEdit(@PathVariable("id") String id){
        RRepairDetailed rRepairDetailed = rRepairDetailedService.selectRRepairDetailedById(id);
        if (null == rRepairDetailed){
            return AjaxResult.error("??????????????????");
        }
        else {
            return AjaxResult.success(rRepairDetailed);
        }
    }


    @Log(title = "???????????????????????????????????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PostMapping("/H10NotSubmittedEditSave")
    public AjaxResult H10NotSubmittedEditSave(@RequestBody RRepairDetailed rRepairDetailed){
        RRepairDetailed rRepairDetailedOne = rRepairDetailedService.selectRRepairDetailedById(rRepairDetailed.getId());
        rRepairDetailedOne.setIsNew(Long.valueOf(2));
        rRepairDetailedOne.setGarageId(rRepairDetailed.getGarageId());
        rRepairDetailedOne.setGarageName(rRepairDetailed.getGarageName());
        rRepairDetailedOne.setDuration(rRepairDetailed.getDuration());
        rRepairDetailedOne.setVehicleModel(rRepairDetailed.getVehicleModel());
        rRepairDetailedOne.setVehicleModelName(rRepairDetailed.getVehicleModelName());
        rRepairDetailedOne.setUnit(rRepairDetailed.getUnit());
        rRepairDetailedOne.setNumber(rRepairDetailed.getNumber());
        rRepairDetailedOne.setName(rRepairDetailed.getName());
        rRepairDetailedOne.setUnitPrice(rRepairDetailed.getUnitPrice());
        rRepairDetailedOne.setMonthNumber(rRepairDetailed.getMonthNumber());
        rRepairDetailedOne.setYearNumber(rRepairDetailed.getYearNumber());
        rRepairDetailedOne.setRemarks(rRepairDetailed.getRemarks());
        //????????????????????????
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairDetailedOne.setUpdateBy(userInfo.getUsername());
        //????????????
        BigDecimal total = rRepairDetailed.getUnitPrice().multiply(rRepairDetailed.getDuration());
        BigDecimal number = new BigDecimal(rRepairDetailed.getNumber());
        BigDecimal amount = total.multiply(number);
        rRepairDetailedOne.setAmount(amount);
        return toAjax(rRepairDetailedService.updateRRepairDetailed(rRepairDetailedOne));
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????",businessType = BusinessType.DELETE)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:notnewdelete")
    @GetMapping(value = "/H10NotSubmittedDelete/{id}")
    public AjaxResult H10NotSubmittedDelete(@PathVariable("id")String id){
        RRepairDetailed rRepairDetailed = rRepairDetailedService.selectRRepairDetailedById(id);
        if (rRepairDetailed.getDeleteFlag() == 2){
            return AjaxResult.error("??????????????????");
        }
        else {
            rRepairDetailed.setDeleteFlag(Long.valueOf(2));
            return toAjax(rRepairDetailedService.updateRRepairDetailed(rRepairDetailed));
        }
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repair:info")
    @GetMapping(value = "/H10NotSubmittedIsNewList/{repairId}")
    public AjaxResult H10NotSubmittedIsNewList(@PathVariable("repairId")String repairId){
        List<RRepairDetailed> rRepairDetailedList = rRepairDetailedService.selectRRepairDetailedIsNewList(repairId);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (RRepairDetailed rRepairDetailedOne:rRepairDetailedList){
            totalCost = totalCost.add(rRepairDetailedOne.getAmount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("totalCost",totalCost);
        map.put("notSubmittedIsNewList",rRepairDetailedList);
        return AjaxResult.success(map);
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????",businessType = BusinessType.INSERT)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:isnewadd")
    @PostMapping("/H10NotSubmittedIsNewAdd")
    public AjaxResult H10NotSubmittedIsNewAdd(@RequestBody RRepairDetailed rRepairDetailed){
        rRepairDetailed.setDeleteFlag(Long.valueOf(1));
        rRepairDetailed.setIsNew(Long.valueOf(1));
        //????????????????????????
        rRepairDetailed.setId(UUID.randomUUID().toString());
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairDetailed.setCreateBy(userInfo.getUsername());
        //????????????
        BigDecimal total = rRepairDetailed.getUnitPrice().multiply(rRepairDetailed.getDuration());
        BigDecimal number = new BigDecimal(rRepairDetailed.getNumber());
        BigDecimal amount = total.multiply(number);
        rRepairDetailed.setAmount(amount);
        //???????????????????????????
//        RGarageRepairItem rGarageRepairItem = new RGarageRepairItem();
//        rGarageRepairItem.setId(rRepairDetailed.getId());
//        rGarageRepairItem.setGarageId(rRepairDetailed.getGarageId());
//        rGarageRepairItem.setVehicleModel(rRepairDetailed.getVehicleModel());
//        rGarageRepairItem.setVehicleModelName(rRepairDetailed.getVehicleModelName());
//        rGarageRepairItem.setName(rRepairDetailed.getName());
//        rGarageRepairItem.setUnit(rRepairDetailed.getUnit());
//        rGarageRepairItem.setUnitPrice(rRepairDetailed.getUnitPrice());
//        rGarageRepairItem.setDuration(rRepairDetailed.getDuration());
//        rGarageRepairItem.setDeleteFlag(Long.valueOf(1));
//        rGarageRepairItem.setCreateBy(userInfo.getUsername());
//        garageRepairItemService.insertRGarageRepairItem(rGarageRepairItem);
        return toAjax(rRepairDetailedService.insertRRepairDetailed(rRepairDetailed));
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:isnewedit")
    @GetMapping(value = "/H10NotSubmittedIsNewEdit/{id}")
    public AjaxResult H10NotSubmittedIsNewEdit(@PathVariable("id")String id){
        RRepairDetailed rRepairDetailed = rRepairDetailedService.selectRRepairDetailedById(id);
        if (null == rRepairDetailed){
            return AjaxResult.error("??????????????????");
        }
        else {
            return AjaxResult.success(rRepairDetailed);
        }
    }

    @Log(title = "???????????????????????????????????????????????????????????????????????????",businessType = BusinessType.UPDATE)
    @PostMapping("/H10NotSubmittedIsNewEditSave")
    public AjaxResult H10NotSubmittedIsNewEditSave(@RequestBody RRepairDetailed rRepairDetailed){
        RRepairDetailed rRepairDetailedOne = rRepairDetailedService.selectRRepairDetailedById(rRepairDetailed.getId());
        if (null == rRepairDetailedOne){
            return AjaxResult.error("??????????????????");
        }
        LoginUser userInfo = tokenService.getLoginUser();
        rRepairDetailedOne.setName(rRepairDetailed.getName());
        rRepairDetailedOne.setUnit(rRepairDetailed.getUnit());
        rRepairDetailedOne.setRemarks(rRepairDetailed.getRemarks());
        rRepairDetailedOne.setUnitPrice(rRepairDetailed.getUnitPrice());
        rRepairDetailedOne.setDuration(rRepairDetailed.getDuration());
        rRepairDetailedOne.setVehicleModelName(rRepairDetailed.getVehicleModelName());
        rRepairDetailedOne.setVehicleModel(rRepairDetailed.getVehicleModel());
        rRepairDetailedOne.setNumber(rRepairDetailed.getNumber());
        rRepairDetailedOne.setUpdateBy(userInfo.getUsername());
        //????????????
        BigDecimal total = rRepairDetailed.getUnitPrice().multiply(rRepairDetailed.getDuration());
        BigDecimal number = new BigDecimal(rRepairDetailed.getNumber());
        BigDecimal amount = total.multiply(number);
        rRepairDetailedOne.setAmount(amount);
        //????????????????????????
        /*RGarageRepairItem rGarageRepairItem = garageRepairItemService.selectRGarageRepairItemById(rRepairDetailedOne.getId());
        rGarageRepairItem.setGarageId(rRepairDetailed.getGarageId());
        rGarageRepairItem.setVehicleModel(rRepairDetailed.getVehicleModel());
        rGarageRepairItem.setVehicleModelName(rRepairDetailed.getVehicleModelName());
        rGarageRepairItem.setDuration(rRepairDetailed.getDuration());
        rGarageRepairItem.setUnit(rRepairDetailed.getUnit());
        rGarageRepairItem.setName(rRepairDetailed.getName());
        rGarageRepairItem.setUnitPrice(rRepairDetailed.getUnitPrice());
        rGarageRepairItem.setUpdateBy(userInfo.getUsername());
        garageRepairItemService.updateRGarageRepairItem(rGarageRepairItem);*/
        return toAjax(rRepairDetailedService.updateRRepairDetailed(rRepairDetailedOne));
    }

    @Log(title = "?????????????????????????????????????????????????????????????????????",businessType = BusinessType.DELETE)
    @PreAuthorize(hasPermi = "pfservercar:repairgaragestation:isnewdelete")
    @GetMapping("/H10NotSubmittedIsNewDelete/{id}")
    public AjaxResult H10NotSubmittedIsNewDelete(@PathVariable("id")String id){
        RRepairDetailed rRepairDetailed = rRepairDetailedService.selectRRepairDetailedById(id);
        if (null == rRepairDetailed){
            return AjaxResult.error("??????????????????");
        }
        else {
            rRepairDetailed.setDeleteFlag(Long.valueOf(2));
            /*RGarageRepairItem rGarageRepairItem = garageRepairItemService.selectRGarageRepairItemById(id);
            rGarageRepairItem.setDeleteFlag(Long.valueOf(2));
            garageRepairItemService.updateRGarageRepairItem(rGarageRepairItem);*/
            return toAjax(rRepairDetailedService.updateRRepairDetailed(rRepairDetailed));
        }
    }

    @Log(title = "??????????????????",businessType = BusinessType.OTHER)
    @PreAuthorize(hasPermi = "pfservercar:repairbook:list")
    @PostMapping("/H17List")
    public TableDataInfo H17List(@RequestBody RRepair rRepair){
        startPage(rRepair.getPageNum(),rRepair.getPageSize());
        if (rRepair.getStartTime() != null && rRepair.getFinshTime() != null){
            if (rRepair.getFinshTime().before(rRepair.getStartTime())){
                throw new CustomException("????????????????????????????????????");
            }
        }
        if (rRepair.getFinshTime()!= null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(rRepair.getFinshTime());
            calendar.add(Calendar.DAY_OF_MONTH,1);
            rRepair.setFinshTime(calendar.getTime());
        }
        rRepair.setGarageId(tokenService.getLoginUser().getSysUser().getGarageId());
        List<RRepair> rRepairList = rRepairService.selectRRepairBook(rRepair);
        return getDataTable(rRepairList);
    }

    @Log(title = "??????????????????",businessType = BusinessType.IMPORT)
    @PreAuthorize(hasPermi = "pfservercar:repairbook:export")
    @PostMapping("/H17Export")
    public void H17Import(HttpServletResponse response, RRepair rRepair)throws IOException{
        if (rRepair.getStartTime() != null && rRepair.getFinshTime() != null){
            if (rRepair.getFinshTime().before(rRepair.getStartTime())){
                throw new CustomException("????????????????????????????????????");
            }
        }
        if (rRepair.getFinshTime()!= null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(rRepair.getFinshTime());
            calendar.add(Calendar.DAY_OF_MONTH,1);
            rRepair.setFinshTime(calendar.getTime());
        }
        rRepair.setGarageId(tokenService.getLoginUser().getSysUser().getGarageId());
        List<RRepairOutExcel> rRepairList = rRepairService.queryRRepairImport(rRepair);

        // ?????? ??????
        Map<String,RRepairOutExcel> rRepairOutExcelMap = new LinkedHashMap<>();
        RRepairOutExcel rRepairData;
        for (RRepairOutExcel rRepairOutExcel : rRepairList) {
            rRepairData = rRepairOutExcelMap.get(rRepairOutExcel.getRepairId());
            if(rRepairData != null){
                rRepairData.setTotal(rRepairData.getTotal().add(rRepairOutExcel.getAmount()));
                rRepairOutExcel.setTotal(rRepairData.getTotal());
            }else {
                rRepairOutExcel.setTotal(rRepairOutExcel.getAmount());
                rRepairOutExcelMap.put(rRepairOutExcel.getRepairId(),rRepairOutExcel);
            }
        }
        ExcelUtil<RRepairOutExcel> util = new ExcelUtil<RRepairOutExcel>(RRepairOutExcel.class);
        util.exportExcel(response, rRepairList, "repairOutExcel");
    }

    /**
     * ????????????????????????
     */
    @Log(title = "????????????????????????")
    @PreAuthorize(hasPermi = "pfservercar:repair:check")
    @PostMapping("/H01Check")
    public AjaxResult H01Check(@Validated @RequestBody ReviewSubmitR reviewSubmit){
        if (reviewSubmit.getStatus()==2 && StringUtils.isEmpty(reviewSubmit.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        // ???????????????
        LoginUser userInfo = tokenService.getLoginUser();
        reviewSubmit.setUserId(userInfo.getUserid()+"");
        return rRepairService.checkRRepair(reviewSubmit);
    }

    /**
     * ???????????????
     */
    @Log(title = "???????????????????????????")
    @PreAuthorize(hasPermi = "pfservercar:repairstation:check")
    @PostMapping("/H07StationCheck")
    public AjaxResult H07StationCheck(@Validated @RequestBody ReviewSubmit reviewSubmit){
        if (reviewSubmit.getStatus()==2 && StringUtils.isEmpty(reviewSubmit.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        // ???????????????
        LoginUser userInfo = tokenService.getLoginUser();
        reviewSubmit.setUserId(userInfo.getUserid()+"");
        return rRepairService.checkGarageRRepair(reviewSubmit);
    }

    /**
     * ?????????????????????????????????????????????
     */
    @RequestMapping("/H02RecordList")
    @PreAuthorize(hasPermi = "pfservercar:repair:recordlist")
    public AjaxResult H02RecordList(Long type){
        return systemClient.queryExamineConfigProcessList(type);
    }

}
