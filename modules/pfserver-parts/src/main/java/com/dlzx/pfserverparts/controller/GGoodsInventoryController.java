package com.dlzx.pfserverparts.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;

import com.dlzx.common.core.utils.DateUtils;
import com.dlzx.common.core.utils.StringUtils;
import com.dlzx.common.security.service.TokenService;
import com.dlzx.pfserverparts.domain.GGoods;
import com.dlzx.pfserverparts.domain.GGoodsType;
import com.dlzx.pfserverparts.domain.excel.GGoodsInventoryDealExcel;
import com.dlzx.pfserverparts.domain.excel.GGoodsInventoryInExcel;
import com.dlzx.pfserverparts.domain.excel.GGoodsInventoryOutExcel;
import com.dlzx.pfserverparts.domain.excel.GGoodsInventoryScrapExcel;
import com.dlzx.pfserverparts.service.IGGoodsService;
import com.dlzx.pfserverparts.service.IGGoodsTypeService;
import com.dlzx.system.api.model.LoginUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.dlzx.pfserverparts.domain.GGoodsInventory;
import com.dlzx.pfserverparts.service.IGGoodsInventoryService;
import com.dlzx.common.core.web.controller.BaseController;
import com.dlzx.common.core.web.domain.AjaxResult;
import com.dlzx.common.core.utils.poi.ExcelUtil;
import com.dlzx.common.core.web.page.TableDataInfo;

/**
 * ????????????-??????Controller
 * 
 * @author dlzx
 * @date 2020-12-02
 */
@RestController
@RequestMapping("/parts/goodsInventory")
public class GGoodsInventoryController extends BaseController
{
    @Autowired
    private IGGoodsInventoryService gGoodsInventoryService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private IGGoodsService gGoodsService;
    @Autowired
    private IGGoodsTypeService goodsTypeService;


    /**
     * @describe ????????????-????????????????????????
     * @author DongCL
     * @date 2021-03-05 09:35
     * @param
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @PostMapping("/K06List")
    public TableDataInfo K06List(@RequestBody GGoodsInventory gGoodsInventory)
    {
        startPage(gGoodsInventory.getPageNum(), gGoodsInventory.getPageSize());
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsOutInventoryList(gGoodsInventory);
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @PostMapping(value = {"/K03List", "/K04AddGetInfo", "/K07AddGetInfo", "/K08EditGetInfo", "/K12AddGetInfo", "/K13EditGetInfo"})
    public TableDataInfo K03List(@RequestBody GGoodsInventory gGoodsInventory)
    {
        startPage(gGoodsInventory.getPageNum(), gGoodsInventory.getPageSize());
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryList(gGoodsInventory);
        return getDataTable(list);
    }


    /**
     * @describe ??????????????????
     * @author DongCL
     * @date 2021-03-05 13:23
     * @param
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:export")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/goodsInventoryInListExport")
    public void inExport(HttpServletResponse response, GGoodsInventory gGoodsInventory) throws IOException
    {
        // ?????????????????????
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryList(gGoodsInventory);

        List<GGoodsInventoryInExcel> gGoodsInventoryInExcels = new ArrayList<>();
        list.stream().forEach(gGoodsInventoryData -> {
            GGoodsInventoryInExcel gGoodsInventoryInExcel = new GGoodsInventoryInExcel();
            BeanUtils.copyProperties(gGoodsInventoryData,gGoodsInventoryInExcel);
            gGoodsInventoryInExcel.setTotalMoney(gGoodsInventoryInExcel.getUnitPrice().multiply(BigDecimal.valueOf(gGoodsInventoryInExcel.getInventoryNumber())));
            gGoodsInventoryInExcels.add(gGoodsInventoryInExcel);
        });

        ExcelUtil<GGoodsInventoryInExcel> util = new ExcelUtil<GGoodsInventoryInExcel>(GGoodsInventoryInExcel.class);
        util.exportExcel(response, gGoodsInventoryInExcels, "goodsInventory");
    }

    /**
     * @describe ??????????????????
     * @author DongCL
     * @date 2021-03-05 13:23
     * @param
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:export")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/goodsInventoryOutListExport")
    public void outExport(HttpServletResponse response, GGoodsInventory gGoodsInventory) throws IOException
    {
        // ?????????????????????
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsOutInventoryList(gGoodsInventory);

        List<GGoodsInventoryOutExcel> gGoodsInventoryOutExcels = new ArrayList<>();
        list.stream().forEach(gGoodsInventoryData -> {
            GGoodsInventoryOutExcel gGoodsInventoryOutExcel = new GGoodsInventoryOutExcel();
            BeanUtils.copyProperties(gGoodsInventoryData,gGoodsInventoryOutExcel);
            gGoodsInventoryOutExcel.setTotalMoney(gGoodsInventoryOutExcel.getUnitPrice().multiply(BigDecimal.valueOf(gGoodsInventoryOutExcel.getInventoryNumber())));
            gGoodsInventoryOutExcels.add(gGoodsInventoryOutExcel);
        });

        ExcelUtil<GGoodsInventoryOutExcel> util = new ExcelUtil<GGoodsInventoryOutExcel>(GGoodsInventoryOutExcel.class);
        util.exportExcel(response, gGoodsInventoryOutExcels, "goodsInventory");
    }

    /**
     * @describe ????????????????????????
     * @author DongCL
     * @date 2021-03-05 13:23
     * @param
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:export")
    @Log(title = "????????????-????????????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/goodsInventoryDealListExport")
    public void dealExport(HttpServletResponse response, GGoodsInventory gGoodsInventory) throws IOException
    {
        // ?????????????????????
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryDetailList(gGoodsInventory);

        List<GGoodsInventoryDealExcel> gGoodsInventoryDealExcels = new ArrayList<>();
        list.stream().forEach(gGoodsInventoryData -> {

            // ?????????
            if (gGoodsInventoryData.getType() == 2) {
                gGoodsInventoryData.setInventoryNumber(gGoodsInventoryData.getInventoryNumber() == null ? 0 : gGoodsInventoryData.getInventoryNumber());
                gGoodsInventoryData.setTotalMoney(gGoodsInventoryData.getUnitPrice().multiply(new BigDecimal(gGoodsInventoryData.getInventoryNumber())));
            }
            // ?????????
            if (gGoodsInventoryData.getType() == 1) {
                gGoodsInventoryData.setInventoryOutTotalQuantity(gGoodsInventoryData.getInventoryNumber() == null ? 0 : gGoodsInventoryData.getInventoryNumber().intValue());
                gGoodsInventoryData.setInventoryNumber(0L);
                gGoodsInventoryData.setTotalMoney(gGoodsInventoryData.getUnitPrice().multiply(new BigDecimal(gGoodsInventoryData.getInventoryOutTotalQuantity())));
            }

            GGoodsInventoryDealExcel gGoodsInventoryDealExcel = new GGoodsInventoryDealExcel();
            BeanUtils.copyProperties(gGoodsInventoryData, gGoodsInventoryDealExcel);
            gGoodsInventoryDealExcels.add(gGoodsInventoryDealExcel);
        });

        ExcelUtil<GGoodsInventoryDealExcel> util = new ExcelUtil<GGoodsInventoryDealExcel>(GGoodsInventoryDealExcel.class);
        util.exportExcel(response, gGoodsInventoryDealExcels, "goodsInventory");
    }

    /**
     * @describe ??????????????????
     * @author DongCL
     * @date 2021-03-05 13:23
     * @param
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:export")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.EXPORT)
    @PostMapping("/goodsInventoryScrapListExport")
    public void scrapExport(HttpServletResponse response, GGoodsInventory gGoodsInventory) throws IOException
    {
        // ?????????????????????
        gGoodsInventory.setInventoryType(6L);
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryList(gGoodsInventory);

        List<GGoodsInventoryScrapExcel> gGoodsInventoryScrapExcels = new ArrayList<>();
        list.stream().forEach(gGoodsInventoryData -> {
            GGoodsInventoryScrapExcel gGoodsInventoryScrapExcel = new GGoodsInventoryScrapExcel();
            BeanUtils.copyProperties(gGoodsInventoryData,gGoodsInventoryScrapExcel);
            gGoodsInventoryScrapExcel.setTotalMoney(gGoodsInventoryScrapExcel.getUnitPrice().multiply(BigDecimal.valueOf(gGoodsInventoryScrapExcel.getInventoryNumber())));
            gGoodsInventoryScrapExcels.add(gGoodsInventoryScrapExcel);
        });
        ExcelUtil<GGoodsInventoryScrapExcel> util = new ExcelUtil<GGoodsInventoryScrapExcel>(GGoodsInventoryScrapExcel.class);
        util.exportExcel(response, gGoodsInventoryScrapExcels, "goodsInventory");
    }

    /**
     * ????????????-?????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @GetMapping(value = {"/selectAllGoodsTypeNameRK"})
    public TableDataInfo selectAllGoodsTypeNameRK()
    {
        GGoodsType gGoodsType = new GGoodsType();
        List<GGoodsType> list = goodsTypeService.selectGGoodsTypeList(gGoodsType);
        return getDataTable(list);
    }

    /**
     * ????????????-?????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @GetMapping(value = {"/selectAllGoodsTypeNameCK"})
    public TableDataInfo selectAllGoodsTypeNameCK()
    {
        GGoodsType gGoodsType = new GGoodsType();
        List<GGoodsType> list = goodsTypeService.selectGGoodsTypeList(gGoodsType);
        return getDataTable(list);
    }

    /**
     * ????????????-?????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @GetMapping(value = {"/selectAllGoodsTypeNameKC"})
    public TableDataInfo selectAllGoodsTypeNameKC()
    {
        GGoodsType gGoodsType = new GGoodsType();
        List<GGoodsType> list = goodsTypeService.selectGGoodsTypeList(gGoodsType);
        return getDataTable(list);
    }

    /**
     * ??????-?????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @GetMapping(value = {"/selectAllGoodsTypeNameBF"})
    public TableDataInfo selectAllGoodsTypeNameBF()
    {
        GGoodsType gGoodsType = new GGoodsType();
        List<GGoodsType> list = goodsTypeService.selectGGoodsTypeList(gGoodsType);
        return getDataTable(list);
    }

    /**
     * ??????????????????-??????????????????????????????
     * k05 k08 k13 ????????????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsReceivingRecord:query")
    @GetMapping("/K05GetInfo/{id}")
    public AjaxResult K05GetInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(gGoodsInventoryService.selectGGoodsInventoryById(id));
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @PostMapping("/K09List")
    public TableDataInfo K09List(@RequestBody GGoods gGoods)
    {
        startPage(gGoods.getPageNum(), gGoods.getPageSize());
        List<GGoods> list = gGoodsService.selectGGoodsList(gGoods);
        return getDataTable(list);
    }

    /**
     * ????????????-??????????????????
     * @param gGoodsInventory
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @PostMapping(value = {"/K10List"})
    public TableDataInfo K10List(@RequestBody GGoodsInventory gGoodsInventory)
    {
        startPage(gGoodsInventory.getPageNum(), gGoodsInventory.getPageSize());
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryDetailList(gGoodsInventory);
        for (GGoodsInventory gGoodsInventory1 : list) {
            // ?????????
            if (gGoodsInventory1.getType() == 2) {
                gGoodsInventory1.setInventoryNumber(gGoodsInventory1.getInventoryNumber()==null?0:gGoodsInventory1.getInventoryNumber());
                gGoodsInventory1.setTotalMoney(gGoodsInventory1.getUnitPrice().multiply(new BigDecimal(gGoodsInventory1.getInventoryNumber())));
            }
            // ?????????
            if (gGoodsInventory1.getType() == 1) {
                gGoodsInventory1.setInventoryOutTotalQuantity(gGoodsInventory1.getInventoryNumber()==null?0:gGoodsInventory1.getInventoryNumber().intValue());
                gGoodsInventory1.setInventoryNumber(0L);
                gGoodsInventory1.setTotalMoney(gGoodsInventory1.getUnitPrice().multiply(new BigDecimal(gGoodsInventory1.getInventoryOutTotalQuantity())));
            }
        }
        return getDataTable(list);
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:export")
    @Log(title = "????????????-??????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GGoodsInventory gGoodsInventory) throws IOException
    {
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryList(gGoodsInventory);
        ExcelUtil<GGoodsInventory> util = new ExcelUtil<GGoodsInventory>(GGoodsInventory.class);
        util.exportExcel(response, list, "inventory");
    }

    /**
     * ??????????????????-??????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:query")
    @GetMapping(value = "/getInfo/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return AjaxResult.success(gGoodsInventoryService.selectGGoodsInventoryById(id));
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:add")
    @Log(title = "????????????-????????????", businessType = BusinessType.INSERT)
    @PostMapping("/K04Add")
    public AjaxResult K04Add(@RequestBody GGoodsInventory gGoodsInventory)
    {
        // ????????????????????????
        LoginUser loginUser = tokenService.getLoginUser();

        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }
        gGoodsInventory.setId(UUID.randomUUID().toString());
        gGoodsInventory.setCreateBy(loginUser.getUsername());
        // ??????
        gGoodsInventory.setType(2L);
        gGoodsInventory.setCode(DateUtils.getCode(gGoodsInventory.getType()));
        // ??????g_goods?????????
        GGoods goodData = gGoodsService.selectGGoodsById(gGoodsInventory.getGoodsId());
        // ??????
        goodData.setAmount(goodData.getAmount() + gGoodsInventory.getInventoryNumber());
        // ??????
        gGoodsService.updateGGoods(goodData);

        // ????????????
        gGoodsInventory.setInventoryType(1L);
        return toAjax(gGoodsInventoryService.insertGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????-??????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:edit")
    @Log(title = "????????????-????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/K05EditSave")
    public AjaxResult K05EditSave(@RequestBody GGoodsInventory gGoodsInventory)
    {
        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }
        GGoodsInventory gGoodsInventoryData = gGoodsInventoryService.selectGGoodsInventoryById(gGoodsInventory.getId());
        // ??????????????????
        long sub = gGoodsInventoryData.getInventoryNumber() - gGoodsInventory.getInventoryNumber();
        // ??????g_goods?????????
        GGoods gGoodsData = gGoodsService.selectGGoodsById(gGoodsInventory.getGoodsId());

        gGoodsData.setAmount(gGoodsData.getAmount() - sub);
        gGoodsService.updateGGoods(gGoodsData);

        LoginUser loginUser = tokenService.getLoginUser();
        gGoodsInventory.setUpdateBy(loginUser.getUsername());
        return toAjax(gGoodsInventoryService.updateGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????-??????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:remove")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.DELETE)
	@GetMapping(value = {"/K03Delete/{id}", "/K11Delete/{id}"})
    public AjaxResult K03Delete(@PathVariable String id)
    {
        GGoodsInventory gGoodsInventory = gGoodsInventoryService.selectGGoodsInventoryById(id);
        gGoodsInventory.setDeleteFlag(Long.valueOf(2));
        return toAjax(gGoodsInventoryService.updateGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????-??????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:remove")
    @Log(title = "????????????-??????????????????", businessType = BusinessType.DELETE)
    @GetMapping(value = {"/K06Delete/{id}"})
    public AjaxResult K06Delete(@PathVariable String id)
    {
        GGoodsInventory gGoodsInventory = gGoodsInventoryService.selectGGoodsInventoryById(id);
        Long inventoryType = gGoodsInventory.getInventoryType();
        if (inventoryType == 2L || inventoryType == 3L || inventoryType == 4L) {
            return AjaxResult.error("????????????????????????????????????");
        }
        gGoodsInventory.setDeleteFlag(Long.valueOf(2));
        return toAjax(gGoodsInventoryService.updateGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????-????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:add")
    @Log(title = "????????????-????????????", businessType = BusinessType.INSERT)
    @PostMapping("/K07Add")
    public AjaxResult K07Add(@RequestBody GGoodsInventory gGoodsInventory)
    {
        // ????????????????????????
        LoginUser loginUser = tokenService.getLoginUser();

        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }
        gGoodsInventory.setId(UUID.randomUUID().toString());
        gGoodsInventory.setCreateBy(loginUser.getUsername());
        // ??????
        gGoodsInventory.setType(1L);
        gGoodsInventory.setCode(DateUtils.getCode(gGoodsInventory.getType()));
        // ??????g_goods?????????
        GGoods goodData = gGoodsService.selectGGoodsById(gGoodsInventory.getGoodsId());
        if (goodData.getAmount() < gGoodsInventory.getInventoryNumber()) {
            return AjaxResult.error("??????????????????????????????????????????,??????????????????");
        }
        // ??????
        goodData.setAmount(goodData.getAmount() - gGoodsInventory.getInventoryNumber());
        // ??????
        gGoodsService.updateGGoods(goodData);
        // ??????
        gGoodsInventory.setInventoryType(3L);
        return toAjax(gGoodsInventoryService.insertGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????-??????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:edit")
    @Log(title = "????????????-????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/K08EditSave")
    public AjaxResult K08EditSave(@RequestBody GGoodsInventory gGoodsInventory)
    {
        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }

        GGoodsInventory gGoodsInventoryData = gGoodsInventoryService.selectGGoodsInventoryById(gGoodsInventory.getId());
        // ??????????????????
        long sub = gGoodsInventoryData.getInventoryNumber() - gGoodsInventory.getInventoryNumber();
        // ??????g_goods?????????
        GGoods gGoodsData = gGoodsService.selectGGoodsById(gGoodsInventory.getGoodsId());
        if ((gGoodsData.getAmount() + sub)<0) {
            return AjaxResult.error("??????????????????????????????????????????,??????????????????");
        }
        gGoodsData.setAmount(gGoodsData.getAmount() + sub);
        gGoodsService.updateGGoods(gGoodsData);

        LoginUser loginUser = tokenService.getLoginUser();
        gGoodsInventory.setUpdateBy(loginUser.getUsername());
        return toAjax(gGoodsInventoryService.updateGGoodsInventory(gGoodsInventory));
    }

    /**
     * ??????????????????
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:list")
    @PostMapping(value = {"/K11List"})
    public TableDataInfo K11List(@RequestBody GGoodsInventory gGoodsInventory)
    {
        startPage(gGoodsInventory.getPageNum(), gGoodsInventory.getPageSize());
        gGoodsInventory.setInventoryType(6L);
        List<GGoodsInventory> list = gGoodsInventoryService.selectGGoodsInventoryList(gGoodsInventory);
        return getDataTable(list);
    }

    /**
     * ????????????
     * @param gGoodsInventory
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:add")
    @Log(title = "????????????-????????????", businessType = BusinessType.INSERT)
    @PostMapping("/K12Add")
    public AjaxResult K12Add(@RequestBody GGoodsInventory gGoodsInventory)
    {
        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }
        // ??????ID
        gGoodsInventory.setId(UUID.randomUUID().toString());
        // code
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Random random = new Random();
        int i1 = 1000;
        int num = random.nextInt(8999)+ i1;
        gGoodsInventory.setCode("BF"+simpleDateFormat.format(date) + num);
        LoginUser loginUser = tokenService.getLoginUser();
        gGoodsInventory.setCreateBy(loginUser.getUsername());
        gGoodsInventory.setType(1L);
        gGoodsInventory.setInventoryType(6L);
        // ??????g_goods?????????
        GGoods gGoods = new GGoods();
        String goodsId = gGoodsInventory.getGoodsId();
        gGoods.setId(goodsId);
        Long i = null;
        Long j = null;
        List<GGoods> gGoods1 = gGoodsService.selectGGoodsList(gGoods);
        for (GGoods list : gGoods1) {
            i = list.getAmount();
            j = gGoodsInventory.getInventoryNumber();
        }
        Long k = i - j;
        if (i < j) {
            return AjaxResult.error("??????????????????????????????????????????,??????????????????");
        }

        // todo update by 2021.3.15 11.39 - dongchanglong ?????????????????????????????????????????????
//        gGoods.setAmount(k);
//        gGoodsService.updateGGoods(gGoods);
        return toAjax(gGoodsInventoryService.insertGGoodsInventory(gGoodsInventory));
    }

    /**
     * ????????????-????????????
     * @param gGoodsInventory
     * @return
     */
    @PreAuthorize(hasPermi = "parts:goodsInventory:edit")
    @Log(title = "????????????-????????????", businessType = BusinessType.UPDATE)
    @PostMapping("/K13EditSave")
    public AjaxResult K13EditSave(@RequestBody GGoodsInventory gGoodsInventory)
    {
        if (StringUtils.isNull(gGoodsInventory.getInventoryTime())){
            return AjaxResult.error("????????????????????????");
        }
        if (StringUtils.isNull(gGoodsInventory.getInventoryNumber())){
            return AjaxResult.error("????????????????????????");
        }
        if (gGoodsInventory.getInventoryNumber()<=0){
            return AjaxResult.error("????????????????????????0");
        }

        GGoodsInventory gGoodsInventoryData = gGoodsInventoryService.selectGGoodsInventoryById(gGoodsInventory.getId());
        // ??????????????????
        long sub = gGoodsInventoryData.getInventoryNumber() - gGoodsInventory.getInventoryNumber();
        // ??????g_goods?????????
        GGoods gGoodsData = gGoodsService.selectGGoodsById(gGoodsInventory.getGoodsId());
        if ((gGoodsData.getAmount() + sub)<0) {
            return AjaxResult.error("??????????????????????????????????????????,??????????????????");
        }

        // todo update by 2021.3.15 11.42 - dongchanglong ?????????????????????????????????????????????
//        gGoodsData.setAmount(gGoodsData.getAmount() + sub);
//        gGoodsService.updateGGoods(gGoodsData);

        LoginUser loginUser = tokenService.getLoginUser();
        gGoodsInventory.setUpdateBy(loginUser.getUsername());
        return toAjax(gGoodsInventoryService.updateGGoodsInventory(gGoodsInventory));
    }


}
