package com.dlzx.pfservercar.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;

import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfservercar.client.SystemClient;
import com.dlzx.pfservercar.domain.CInsuranceVehicle;
import com.dlzx.pfservercar.domain.CInsuranceVehicleItem;
import com.dlzx.pfservercar.domain.CVehicle;
import com.dlzx.pfservercar.domain.SysFile;
import com.dlzx.pfservercar.mapper.CInsuranceVehicleItemMapper;
import com.dlzx.pfservercar.service.ICInsuranceVehicleItemService;
import com.dlzx.pfservercar.service.ICInsuranceVehicleService;
import com.dlzx.pfservercar.service.ICVehicleService;
import com.dlzx.pfservercar.service.impl.SysFileServiceImpl;
import com.dlzx.system.api.model.LoginUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dlzx.common.log.annotation.Log;
import com.dlzx.common.log.enums.BusinessType;
import com.dlzx.common.security.annotation.PreAuthorize;
import com.dlzx.pfservercar.domain.CInsuranceVehicleGoogds;
import com.dlzx.pfservercar.service.ICInsuranceVehicleGoogdsService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-????????????-??????Controller
 * 
 * @author dlzx
 * @date 2020-11-27
 */
@RestController
@RequestMapping("/car/googds")
public class CInsuranceVehicleGoogdsController extends BaseController
{
    @Autowired
    private ICInsuranceVehicleGoogdsService cInsuranceVehicleGoogdsService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private ICInsuranceVehicleService icInsuranceVehicleService;
    @Autowired
    private ICInsuranceVehicleItemService icInsuranceVehicleItemService;
    @Autowired
    private SystemClient systemClient;

    /**
     * ??????????????????-????????????-????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:list")
    @PostMapping
    public TableDataInfo list(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds)
    {
        startPage(cInsuranceVehicleGoogds.getPageNum(),cInsuranceVehicleGoogds.getPageSize());
        List<CInsuranceVehicleGoogds> list = cInsuranceVehicleGoogdsService.selectCInsuranceVehicleGoogdAndCIV(cInsuranceVehicleGoogds);
        return getDataTable(list);
    }

    /**
     * ????????????
     * @param civgd
     * @return
     */
    @GetMapping("/E16FileList")
    public TableDataInfo selectSysFileList(@RequestParam("civgd") String civgd) {
        List<SysFile> files = cInsuranceVehicleGoogdsService.selectSysFileList(civgd);
        return getDataTable(files);
    }

    /**
     * ??????????????????-????????????-????????????
     * "/E10List","/E11List", "/E16List", "/E17List"
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:list")
    @PostMapping(value = {"/E15List"})
    public TableDataInfo E10List(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds)
    {
        startPage(cInsuranceVehicleGoogds.getPageNum(),cInsuranceVehicleGoogds.getPageSize());
        List<CInsuranceVehicleGoogds> list = cInsuranceVehicleGoogdsService.selectCInsuranceVehicleGoogdAndCIV(cInsuranceVehicleGoogds);
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????-????????????
     * E10 E11 E15 E16 E17
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:list")
    @GetMapping(value = {"/E10GetInfo/{id}"})
    public AjaxResult E10GetInfo(@PathVariable("id") String id)
    {
        CInsuranceVehicleGoogds cInsuranceVehicleGoogds = cInsuranceVehicleGoogdsService.selectCInsuranceVehicleGoogdInfo(id);
        List<CInsuranceVehicleGoogds> list1 = new ArrayList<>();
//        // FileList????????????
//        for (CInsuranceVehicleGoogds insuranceVehicleGoogds : list) {
        CInsuranceVehicleGoogds cInsuranceVehicleGoogds1 = new CInsuranceVehicleGoogds();
//            // ??????ID
        cInsuranceVehicleGoogds1.setCIVGD(cInsuranceVehicleGoogds.getId());
//            // ????????????ID
        cInsuranceVehicleGoogds1.setVehicleId(cInsuranceVehicleGoogds.getVehicleId());
//            // ???????????????
        cInsuranceVehicleGoogds1.setLicensePlate(cInsuranceVehicleGoogds.getLicensePlate());
        list1.add(cInsuranceVehicleGoogds1);
        cInsuranceVehicleGoogds.setcVehicleList(list1);
//        }

        List<CInsuranceVehicleItem> list2 = new ArrayList<>();
//        for (CInsuranceVehicleGoogds cInsuranceVehicleItem : list) {
            CInsuranceVehicleItem cInsuranceVehicleItem1 = new CInsuranceVehicleItem();
            // ????????????ID
            cInsuranceVehicleItem1.setInsuranceId(cInsuranceVehicleGoogds.getInsuranceId());
            // ??????????????????ID
            cInsuranceVehicleItem1.setInsuranceTypeId(cInsuranceVehicleGoogds.getInsuranceTypeId());
            // ????????????????????????
            cInsuranceVehicleItem1.setInsuranceItemId(cInsuranceVehicleGoogds.getInsuranceItemId());
            // ???????????????ID
            cInsuranceVehicleItem1.setInsuranceItemId(cInsuranceVehicleGoogds.getInsuranceItemId());
            // ?????????????????????
            cInsuranceVehicleItem1.setInsuranceItemName(cInsuranceVehicleGoogds.getInsuranceItemName());
            // ????????????
            cInsuranceVehicleItem1.setInsuranceQuota(cInsuranceVehicleGoogds.getInsuranceQuota());
            // ????????????
            cInsuranceVehicleItem1.setInsuranceCost(cInsuranceVehicleGoogds.getInsuranceCost());
            list2.add(cInsuranceVehicleItem1);
        cInsuranceVehicleGoogds.setcInsuranceVehicleItemList(list2);
//        }
        return AjaxResult.success(cInsuranceVehicleGoogds);
    }

    /**
     * ??????????????????-????????????-????????????
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:export")
    @Log(title = "????????????-????????????-??????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CInsuranceVehicleGoogds cInsuranceVehicleGoogds) throws IOException
    {
        List<CInsuranceVehicleGoogds> list = cInsuranceVehicleGoogdsService.selectCInsuranceVehicleGoogdsList(cInsuranceVehicleGoogds);
        ExcelUtil<CInsuranceVehicleGoogds> util = new ExcelUtil<CInsuranceVehicleGoogds>(CInsuranceVehicleGoogds.class);
        util.exportExcel(response, list, "googds");
    }

    /**
     * ??????????????????-????????????-?????????????????? YLH
     */
    @Log(title = "??????????????????-????????????-??????????????????")
    @PreAuthorize(hasPermi = "pfservercar:googds:info")
    @GetMapping(value = "/E11Info/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(cInsuranceVehicleGoogdsService.selectCInsuranceVehicleGoogdsById(id));
    }

    /**
     * ??????????????????-????????????-??????
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:add")
    @Log(title = "??????????????????-??????", businessType = BusinessType.INSERT)
    @PostMapping("/E16Add")
    public AjaxResult E16Add(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds)
    {
        return toAjax(cInsuranceVehicleGoogdsService.insertCInsuranceVehicleGoogds(cInsuranceVehicleGoogds));
    }

    /**
     * YLH
     * @param cInsuranceVehicleGoogds YLH
     * @return
     */
    @ApiOperation("??????????????????")
    @PreAuthorize(hasPermi = "pfservercar:googds:add")
    @Log(title = "????????????", businessType = BusinessType.INSERT)
    @PostMapping("/E07VehicleAdd")
    public AjaxResult E07VehicleAdd(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds)
    {
        if (cInsuranceVehicleGoogds.getcVehicleList().size()==0){
            return AjaxResult.error("??????????????????");
        }
        if (cInsuranceVehicleGoogds.getcInsuranceVehicleItemList().size()==0){
            return AjaxResult.error("?????????????????????");
        }else {
            for (CInsuranceVehicleItem cInsuranceVehicleItem:cInsuranceVehicleGoogds.getcInsuranceVehicleItemList()){
                if (cInsuranceVehicleItem.getInsuranceCost()==null || cInsuranceVehicleItem.getInsuranceQuota()==null){
                    return AjaxResult.error("????????????????????????");
                }
            }
        }
        CInsuranceVehicle cInsuranceVehicle = new CInsuranceVehicle();
        // ??????ID c_insurance_vehicle
        cInsuranceVehicle.setId(UUID.randomUUID().toString());
        // ??????
        cInsuranceVehicle.setRemarks(cInsuranceVehicleGoogds.getRemarks());
        // ??????????????????
        cInsuranceVehicle.setCreateBy(tokenService.getLoginUser().getUsername());
        // type ???????????? 1???????????? 2???????????? 3????????????
        cInsuranceVehicle.setType(Long.valueOf(3));
        // ??????????????????
        cInsuranceVehicle.setDeleteFlag(Long.valueOf(1));
        // ???????????? ?????????
        cInsuranceVehicle.setStatus(Long.valueOf(1));
        cInsuranceVehicle.setTotalPremium(cInsuranceVehicleGoogds.getTotalPremium());

        //??????????????????
        int result = systemClient.adminSubmitCommon(14L,cInsuranceVehicle.getId(),"","");
        //??????????????????????????????
        if (result == 0){
            cInsuranceVehicle.setStatus(2L);
        }


//c_insurance_vehicle_googds
        if ( cInsuranceVehicleGoogds.getcVehicleList().size()>0){
            for (CInsuranceVehicleGoogds civg : cInsuranceVehicleGoogds.getcVehicleList()) {
                // ??????ID  c_insurance_vehicle_googds
                civg.setId(UUID.randomUUID().toString());
                // ??????ID
                civg.setInsuranceId(cInsuranceVehicle.getId());
            }
            //
            cInsuranceVehicleGoogdsService.insertBatch(cInsuranceVehicleGoogds.getcVehicleList());
        }
//c_insurance_vehicle_item
        if (cInsuranceVehicleGoogds.getcInsuranceVehicleItemList().size()>0){
            for (CInsuranceVehicleItem vehicleItem :cInsuranceVehicleGoogds.getcInsuranceVehicleItemList()){
                vehicleItem.setId(UUID.randomUUID().toString());
                vehicleItem.setInsuranceId(cInsuranceVehicle.getId());
                vehicleItem.setDeleteFlag(1L);
                vehicleItem.setCreateBy(tokenService.getLoginUser().getUsername());
            }
            icInsuranceVehicleItemService.insertBatch(cInsuranceVehicleGoogds.getcInsuranceVehicleItemList());
        }
        return toAjax(icInsuranceVehicleService.insertCInsuranceVehicle(cInsuranceVehicle));
    }

    /**
     * ??????????????????-????????????-??????
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:edit")
    @Log(title = "????????????-????????????-??????", businessType = BusinessType.UPDATE)
    @PostMapping("/E16SaveEdit")
    public AjaxResult E16SaveEdit(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds)
    {
        return toAjax(cInsuranceVehicleGoogdsService.updateCInsuranceVehicleGoogds(cInsuranceVehicleGoogds));
    }

    /**
     * ??????????????????-????????????-??????
     */
    @PreAuthorize(hasPermi = "pfservercar:googds:remove")
    @Log(title = "????????????-????????????-??????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(cInsuranceVehicleGoogdsService.deleteCInsuranceVehicleGoogdsByIds(ids));
    }

    @GetMapping("/H16Info/{id}")
//    @PreAuthorize(hasPermi = "pfservercar:googds:goodsinfo")
    @Log(title = "??????????????????")
    public AjaxResult H16Info(@PathVariable("id")String id){
         return AjaxResult.success(cInsuranceVehicleGoogdsService.selectCInVeGoodById(id));
    }

    @PostMapping("/H16EditSave")
    @PreAuthorize(hasPermi = "pfservercar:googds:goodsinfo")
    @Log(title = "????????????????????????")
    public AjaxResult H16EditSave(@RequestBody CInsuranceVehicleGoogds cInsuranceVehicleGoogds){
        return toAjax(cInsuranceVehicleGoogdsService.updateCIVG(cInsuranceVehicleGoogds));
    }
}
