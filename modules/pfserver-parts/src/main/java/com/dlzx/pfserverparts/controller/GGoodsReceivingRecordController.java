package com.dlzx.pfserverparts.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.dlzx.common.core.exception.CustomException;
import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.security.service.DriverTokenService;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfserverparts.client.SystemClient;
import com.dlzx.pfserverparts.domain.DDriver;
import com.dlzx.pfserverparts.domain.GGoods;
import com.dlzx.pfserverparts.domain.GGoodsCheck;
import com.dlzx.pfserverparts.domain.GGoodsCheckItem;
import com.dlzx.pfserverparts.domain.GGoodsReceivingRecordDate;
import com.dlzx.pfserverparts.domain.GGoodsType;
import com.dlzx.pfserverparts.domain.vm.GGoodsReceivingRecordVm;
import com.dlzx.pfserverparts.service.IGGoodsCheckItemService;
import com.dlzx.pfserverparts.service.IGGoodsCheckService;
import com.dlzx.pfserverparts.service.IGGoodsReceivingRecordDateService;
import com.dlzx.pfserverparts.service.IGGoodsService;
import com.dlzx.system.api.model.LoginDriver;
import com.dlzx.system.api.model.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.dlzx.common.log.annotation.Log;
import com.dlzx.common.log.enums.BusinessType;
import com.dlzx.common.security.annotation.PreAuthorize;
import com.dlzx.pfserverparts.domain.GGoodsReceivingRecord;
import com.dlzx.pfserverparts.service.IGGoodsReceivingRecordService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-??????????????????Controller
 * 
 * @author jijiawen
 * @date 2020-12-09
 */
@RestController
@RequestMapping("/parts/goodsReceivingRecord")
public class GGoodsReceivingRecordController extends BaseController
{
    @Autowired
    private IGGoodsReceivingRecordService gGoodsReceivingRecordService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private IGGoodsReceivingRecordDateService gGoodsReceivingRecordDateService;
    @Autowired
    private IGGoodsService goodsService;
    @Autowired
    private IGGoodsCheckItemService gGoodsCheckItemService;
    @Autowired
    private IGGoodsCheckService gGoodsCheckService;


    /**
     * ??????????????????-????????????????????????
     * K18List ???????????????(??????)
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @PostMapping(value = {"/K17List", "/K18List"
                        ,"/K20List", "/K21List", "/K22List"})
    public TableDataInfo K17List(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(gGoodsReceivingRecord);
        return getDataTable(list);
    }

    /**
     * ???????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @GetMapping(value = {"/K17InfoList"})
    public TableDataInfo K17InfoList()
    {
        List<GGoods> gGoods = goodsService.selectGGoodsList(new GGoods());
        return getDataTable(gGoods);
    }


    /**
     * ?????????????????????
     * @param id
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @GetMapping("/K19GetInfo/{id}")
    public TableDataInfo K19GetInfo(@PathVariable("id") String id)
    {
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordByGGRRId(id);
        List<String> list1 = new ArrayList<>();
        for (GGoodsReceivingRecord goodsReceivingRecord : list) {
            Date receiveDates = goodsReceivingRecord.getReceiveDates();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String format = simpleDateFormat.format(receiveDates);
            list1.add(format);
            goodsReceivingRecord.setReceiveDateLists(list1);
        }
        ArrayList<GGoodsReceivingRecord> collect = list.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(
                                GGoodsReceivingRecord::getId))), ArrayList::new));
        return getDataTable(collect);
    }

    /**
     * ???????????????????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @GetMapping("/K20GetInfo/{id}")
    public AjaxResult K20GetInfo(@PathVariable("id") String id)
    {
        GGoodsReceivingRecord gGoodsReceivingRecord = gGoodsReceivingRecordService.selectGGoodsReceivingRecordInfo(id);
        return AjaxResult.success(gGoodsReceivingRecord);
    }

    /**
     * ????????????
     * @param id
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @GetMapping("/K21GetInfo/{id}")
    public TableDataInfo K21GetInfo(@PathVariable("id") String id)
    {
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordByGGRRIId(id);
        List<Date> list1 = new ArrayList<>();
        for (GGoodsReceivingRecord goodsReceivingRecord : list) {
            Date receiveDates = goodsReceivingRecord.getReceiveDates();
            list1.add(receiveDates);
            goodsReceivingRecord.setReceiveDateList(list1);
        }
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????????????????
     * ????????????????????????
     */
    @RequestMapping(path = "/T01List",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TableDataInfo T01List(@RequestBody GGoodsReceivingRecord good)
    {
        startPage(good.getPageNum(), good.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(good);
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????????????????
     * ????????????????????????
     */
    @RequestMapping("/T02List")
    @ResponseBody
    public TableDataInfo T02List(@RequestBody GGoodsReceivingRecord good)
    {
        startPage();
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(good);
        List<Date> list1 = new ArrayList<>();
        for (GGoodsReceivingRecord goodsReceivingRecord : list) {
            list1.add(goodsReceivingRecord.getReceiveDates());
            goodsReceivingRecord.setReceiveDateList(list1);
        }
        return getDataTable(list);
    }


    /**
     * ??????????????????-????????????????????????
     * L09List L10List L11List L12List L13List ???????????????
     */
    @RequestMapping(path = "/L09List",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TableDataInfo L09List(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(gGoodsReceivingRecord);
        return getDataTable(list);
    }

    @RequestMapping(path = "/L12GetInfo",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TableDataInfo L12GetInfo(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(gGoodsReceivingRecord);
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:export")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GGoodsReceivingRecord gGoodsReceivingRecord) throws IOException
    {
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordList(gGoodsReceivingRecord);
        ExcelUtil<GGoodsReceivingRecord> util = new ExcelUtil<GGoodsReceivingRecord>(GGoodsReceivingRecord.class);
        util.exportExcel(response, list, "record");
    }

    /**
     * ??????????????????-??????????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @GetMapping("/K18GetInfo/{id}")
    public AjaxResult K18GetInfo(@PathVariable("id") String id)
    {
        GGoodsReceivingRecord gGoodsReceivingRecord = gGoodsReceivingRecordService.selectGGoodsReceivingRecordById(id);
        return AjaxResult.success(gGoodsReceivingRecord);
    }

    /**
     * ??????????????????-??????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:edit")
    @Log(title = "????????????-??????????????????-??????", businessType = BusinessType.UPDATE)
    @PostMapping("/K18EditSave")
    public AjaxResult K18EditSave(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        if (StringUtils.isNull(gGoodsReceivingRecord.getStatus())){
            return AjaxResult.error("?????????????????????");
        }
        if (gGoodsReceivingRecord.getStatus()==2 && StringUtils.isEmpty(gGoodsReceivingRecord.getReviewRemarks())){
            return AjaxResult.error("????????????????????????");
        }
        LoginUser userInfo = tokenService.getLoginUser();
        gGoodsReceivingRecord.setUserid(userInfo.getUserid().toString());
        return gGoodsReceivingRecordService.checkCInsuranceVehicle(gGoodsReceivingRecord);
    }

// ============== ?????????App  start =======
    /**
     * ??????L01?????????????????????
     * App
     */
    @RequestMapping("/L01GoodTypeList")
    public TableDataInfo L01GoodTypeList()
    {
        return getDataTable(gGoodsReceivingRecordService.queryGoodTypeList());
    }


    /**
     * ??????????????????-????????????????????????
     * App
     */
    @RequestMapping(path = "/L08List",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Log(title = "App ????????????????????????")
    public TableDataInfo L08List(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.queryGGoodsReceivingRecordList(gGoodsReceivingRecord);
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????????????????
     */
    @RequestMapping(path = "/L08GetInfo",method = RequestMethod.POST)
    public AjaxResult L08GetInfo(@RequestParam("id") String id)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.queryGGoodsReceivingRecordById(id));
    }

    /**
     * ??????????????????-??????????????????ID????????????(????????????)
     */
    @RequestMapping(path = "/L07GetGoodList",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public TableDataInfo L07GetInfo(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        GGoods gGoods = new GGoods();
        gGoods.setGoodsTypeId(gGoodsReceivingRecord.getGoodsTypeId());
        if (StringUtils.isNotEmpty(gGoodsReceivingRecord.getGoodsName())){
            gGoods.setName(gGoodsReceivingRecord.getGoodsName());
        }
        List<GGoods> gGoodsList = goodsService.selectGGoodsList(gGoods);
        return getDataTable(gGoodsList);
    }

    /**
     * ??????????????????-??????????????????app-??????
     */
    @Log(title = "????????????-??????????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/L02Add",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L02Add(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.insertGGoodsReceivingRecord(gGoodsReceivingRecord));
    }

    /**
     * ??????????????????-??????????????????app-?????????
     */
    @Log(title = "????????????-??????????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/L03Add",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L03Add(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        for (Date date : gGoodsReceivingRecord.getReceiveDate()) {
            if (StringUtils.isNull(date)){
                throw new CustomException("????????????????????????");
            }
        }
        return AjaxResult.success(gGoodsReceivingRecordService.insertGGoodsReceivingRecordDate(gGoodsReceivingRecord));
    }

    /**
     * ??????????????????-??????????????????app-?????? / ???????????????
     */
    @Log(title = "????????????-??????????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/L04Add",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L04Add(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.insertGGoodsReceivingRecordTyre(gGoodsReceivingRecord));
    }

    /**
     * ??????????????????-????????????
     */
    @Log(title = "????????????-????????????-????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/L06Add",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L06Add(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.insertGGoodsReceivingRecordItem(gGoodsReceivingRecord));
    }

    /**
     * ????????????app- ??????(????????????)
     */
    @Log(title = "????????????-??????????????????-??????", businessType = BusinessType.UPDATE)
    @RequestMapping(path = "/L12EditSave",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L12EditSave(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.submitGGoodsReceivingRecord(gGoodsReceivingRecord));
    }

    /**
     * ????????????app- ??????
     */
    @Log(title = "????????????-??????????????????-??????", businessType = BusinessType.UPDATE)
    @RequestMapping(path = "/L08Recall",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L08Recall(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.recallGGoodsReceivingRecord(gGoodsReceivingRecord));
    }


    /**
     * ????????????app- ????????????
     */
    @Log(title = "????????????-??????????????????-????????????", businessType = BusinessType.UPDATE)
    @RequestMapping(path = "/L10Affirm",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult L10Affirm(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.updateGGoodsReceivingRecord(gGoodsReceivingRecord));
    }

//  ========end======


    /**
     * ??????????????????-L03?????????????????????
     * App
     */
    @RequestMapping("/L03GetInfo/{goodsId}")
    public AjaxResult L03GetInfo(@PathVariable("goodsId") String goodsId)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.selectGGoodsReceivingRecordByGoodsIdOrDate(goodsId));
    }

    /**
     * ??????????????????-L04?????????????????????
     * L05?????????????????????
     * L02?????????????????????
     */
    @RequestMapping("/L04GetInfo/{goodsId}")
    public AjaxResult L04GetInfo(@PathVariable("goodsId") String goodsId)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.selectGGoodsReceivingRecordByByGoodsId(goodsId));
    }


    /**
     * ??????????????????-??????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:remove")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(gGoodsReceivingRecordService.deleteGGoodsReceivingRecordByIds(ids));
    }


    /**
     * ????????????
     * ????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:list")
    @PostMapping(value = {"/M10List", "/M11List"})
    public TableDataInfo M10List(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordAmount(gGoodsReceivingRecord);

        List<GGoodsReceivingRecordVm> recordVmList = new ArrayList<>();
        BigDecimal insuranceCostTotal = BigDecimal.ZERO;
        for (GGoodsReceivingRecord gGoodsReceivingRecord1 : list) {
            GGoodsReceivingRecordVm gGoodsReceivingRecordVm = new GGoodsReceivingRecordVm();
            BigDecimal unitPrice = gGoodsReceivingRecord1.getUnitPrice();
            BigDecimal number = new BigDecimal(gGoodsReceivingRecord1.getNumber());
            gGoodsReceivingRecord1.setInsuranceCostTotal(unitPrice.multiply(number));
            BeanUtils.copyProperties(gGoodsReceivingRecord1, gGoodsReceivingRecordVm);
            recordVmList.add(gGoodsReceivingRecordVm);
        }
        // ????????????
        GGoodsReceivingRecord data = new GGoodsReceivingRecord();
        data.setLicensePlate(gGoodsReceivingRecord.getLicensePlate());
        data.setStartTime(gGoodsReceivingRecord.getStartTime());
        data.setFinishTime(gGoodsReceivingRecord.getFinishTime());
        data.setGoodsType(gGoodsReceivingRecord.getGoodsType());
        List<GGoodsReceivingRecord> gGoodsReceivingRecords = gGoodsReceivingRecordService.selectGGoodsReceivingRecordAmount(data);

        for (GGoodsReceivingRecord goodsReceivingRecord : gGoodsReceivingRecords) {
            BigDecimal unitPrice = goodsReceivingRecord.getUnitPrice();
            BigDecimal number = new BigDecimal(goodsReceivingRecord.getNumber());
            goodsReceivingRecord.setInsuranceCostTotal(unitPrice.multiply(number));
            insuranceCostTotal = goodsReceivingRecord.getInsuranceCostTotal().add(insuranceCostTotal);
        }
        // ??????????????????
        List<Map<String,Object>> mapList = new ArrayList<>();
        // ?????????????????? ??????
        Map<String,Object> map = new HashMap<>();
        map.put("amountSum",insuranceCostTotal);
        map.put("list",list);
        mapList.add(map);
        return getDataTableMap(mapList,list);
    }

    /**
     * ????????????????????????
     * specification ??????
     */
    @GetMapping("/M11Info")
    public TableDataInfo M11Info()
    {
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordDrop();
        return getDataTable(list);
    }



    /**
     * ??????????????????-????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:export")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.EXPORT)
    @PostMapping(value = {"/M10Export", "/M11Export"})
    public void M10Export(HttpServletResponse response, GGoodsReceivingRecord gGoodsReceivingRecord) throws IOException
    {
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.selectGGoodsReceivingRecordAmount(gGoodsReceivingRecord);
        for (GGoodsReceivingRecord gGoodsReceivingRecord1 : list) {
            BigDecimal unitPrice = gGoodsReceivingRecord1.getUnitPrice();
            BigDecimal number = new BigDecimal(gGoodsReceivingRecord1.getNumber());
            gGoodsReceivingRecord1.setAmountSum(unitPrice.multiply(number));
        }
        ExcelUtil<GGoodsReceivingRecord> util = new ExcelUtil<GGoodsReceivingRecord>(GGoodsReceivingRecord.class);
        util.exportExcel(response, list, "record");
    }


    // =========== ?????????APP start =======

    /**
     * ????????????????????????????????????
     */
    @RequestMapping(path = "/T01GoodsList",method = RequestMethod.POST)
    public TableDataInfo T01GoodsList()
    {
        GGoods gGoods = new GGoods();
        List<GGoods> gGoodsList = goodsService.selectGGoodsList(gGoods);
        System.out.println("\t ");
        return getDataTable(gGoodsList);
    }

    /**
     * ???????????????????????????????????????
     */
    @RequestMapping(path = "/T01DriversList",method = RequestMethod.POST)
    public TableDataInfo T01DriversList()
    {
        List<DDriver> dDrivers = goodsService.selectDriversList();
        return getDataTable(dDrivers);
    }

    /**
     * ????????????-????????????????????????
     * App
     */
    @Log(title = "????????????????????????")
    @RequestMapping(path = "/T01AdminGGoodsReceivingList",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public TableDataInfo T01AdminGGoodsReceivingList(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        startPage(gGoodsReceivingRecord.getPageNum(), gGoodsReceivingRecord.getPageSize());
        // ????????????????????????
        gGoodsReceivingRecord.setStatus(4L);
        List<GGoodsReceivingRecord> list = gGoodsReceivingRecordService.queryAdminGGoodsReceivingRecordList(gGoodsReceivingRecord);
        return getDataTable(list);
    }

    /**
     * ????????????-????????????????????????
     */
    @Log(title = "????????????????????????")
    @RequestMapping(path = "/T01AdminGGoodsReceivingInfo",method = RequestMethod.POST)
    public AjaxResult T01AdminGGoodsReceivingInfo(@RequestBody GGoodsReceivingRecord gGoodsReceivingRecord)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.queryGGoodsReceivingRecordById(gGoodsReceivingRecord.getId()));
    }

    /**
     * ??????-??????????????????
     */
    @RequestMapping(path = "/T03AdminGGoodsCheckList",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Log(title = "??????????????????")
    public TableDataInfo T03AdminGGoodsCheckList(@RequestBody GGoodsCheck gGoodsCheck)
    {
        startPage(gGoodsCheck.getPageNum(), gGoodsCheck.getPageSize());
        List<GGoodsCheck> list = gGoodsReceivingRecordService.queryGGoodsCheckList(gGoodsCheck);
        return getDataTable(list);
    }

    /**
     * ??????-??????????????????
     */
    @Log(title = "??????????????????")
    @RequestMapping(path = "/T03AdminGGoodsCheckInfo",method = RequestMethod.POST)
    public AjaxResult T03AdminGGoodsCheckInfo(@RequestBody GGoodsCheck gGoodsCheck)
    {
        return AjaxResult.success(gGoodsReceivingRecordService.queryGGoodsCheckById(gGoodsCheck));
    }

    /**
     * ??????-????????????
     */
    @Log(title = "????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/T03AdminInsertCheck",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult T03AdminInsertCheck(@RequestBody GGoodsType gGoodsType)
    {
        return AjaxResult.success(gGoodsCheckService.submitGGoodsCheck(gGoodsType));
    }

    /**
     * ?????????-??????
     */
    @Log(title = "???????????????", businessType = BusinessType.INSERT)
    @RequestMapping(path = "/T04AdminSubmitCheckItem",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult T04AdminSubmitCheckItem(@RequestBody GGoodsCheck gGoodsCheck)
    {
        if (gGoodsCheck.getgGoodsCheckItems().size()<=0){
            return AjaxResult.error("???????????????");
        }
        for (GGoodsCheckItem gGoodsCheckItem : gGoodsCheck.getgGoodsCheckItems()) {
            if (StringUtils.isNull(gGoodsCheckItem.getDifference())){
                return AjaxResult.error("?????????????????????");
            }
            if (StringUtils.isNull(gGoodsCheckItem.getStatus())){
                return AjaxResult.error("?????????????????????");
            }
        }
        return toAjax(gGoodsCheckItemService.submitCheckItem(gGoodsCheck));
    }

    /**
     * ?????????-??????
     */
    @Log(title = "???????????????", businessType = BusinessType.UPDATE)
    @RequestMapping(path = "/T05AdminUpdateCheckItem",method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public AjaxResult T05AdminUpdateCheckItem(@RequestBody GGoodsCheckItem gGoodsCheckItem)
    {
        if (StringUtils.isNull(gGoodsCheckItem.getCheckQuantity())){
            return AjaxResult.error("????????????????????????");
        }
        return toAjax(gGoodsCheckItemService.updateCheckItem(gGoodsCheckItem));
    }

    // =========== end   =========

}
