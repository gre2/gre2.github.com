package com.longfor.plm.project.dto.board.response;

import com.longfor.plm.project.util.Safes;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class C1IndexDashboardResponse {
    /**
     * 公司
     */

    private String company;

    private String companyCode;

    /**
     * 区域
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * 分期code
     */
    private String phaseCode;

    /**
     * 项目分期
     */
    private String phaseName;

    /**
     * 营销案名
     */
    private String marketingName;

    /**
     * 操盘类型
     */
    @ApiModelProperty(value = "操盘类型（1:龙湖操盘，2:研发/营销操盘，3:研发操盘，4:营销操盘，5:非操盘）")
    private String operateType;

    /**
     * 项目是否是复合航道
     */
    private Boolean compositeChannel;

    /**
     * 主要业态
     */
    private List<String> tzFormatCodeList;
    private List<String> dwFormatCodeList;
    private List<String> ggFormatCodeList;

    /**
     * 建设用地面积
     */
    private BigDecimal stageArea;

    /**
     * 规划容积率
     */
    private String scPlotR;

    /**
     * 总建筑面积
     */
    private BigDecimal dwTotalArea;
    private BigDecimal qdTotalArea;
    private BigDecimal ggTotalArea;

    /**
     * 地上总建筑面积
     */

    private BigDecimal dwBuildUpArea;

    private BigDecimal qdBuildDownArea;

    private BigDecimal ggBuildUpArea;

    /**
     * 地下总建筑面积
     */

    private BigDecimal dwBuildDownArea;

    private BigDecimal qdBuildUpArea;
    private BigDecimal ggBuildDownArea;

    /**
     * 地上可售产权面积
     */
    private BigDecimal dwSaleUpArea;
    private BigDecimal qdSaleUpArea;
    private BigDecimal ggSaleUpArea;

    /**
     * 地上住宅总建筑面积
     */
    private BigDecimal dwResidentialBuildUpArea;
    private BigDecimal qdResidentialBuildUpArea;
    private BigDecimal ggResidentialBuildUpArea;

    /**
     * 地上住宅可售产权面积
     */
    private BigDecimal dwResidentialSaleUpArea;
    private BigDecimal qdResidentialSaleUpArea;
    private BigDecimal ggResidentialSaleUpArea;

    /**
     * 地上可售住宅总户数 (户数)
     */
    private Integer dwResidentialUpSaleHouseholds;
    private Integer qdResidentialUpSaleHouseholds;
    private Integer ggResidentialUpSaleHouseholds;

    /**
     * 地上扣除面积
     */
    private BigDecimal dwAboveDeductedArea;
    private BigDecimal qdAboveDeductedArea;
    private BigDecimal ggAboveDeductedArea;

    /**
     * 地下扣除面积
     */
    private BigDecimal dwUnderDeductedArea;
    private BigDecimal qdUnderDeductedArea;
    private BigDecimal ggUnderDeductedArea;

    /**
     * 地上车位数量
     */
    private Integer dwAboveParkingNum;
    private Integer qdAboveParkingNum;
    private Integer ggAboveParkingNum;

    /**
     * 地下车位数量
     */
    private Integer dwUnderParkingNum;
    private Integer qdUnderParkingNum;
    private Integer ggUnderParkingNum;

    /**
     * 可售及赠送业主地下室面积
     */
    private BigDecimal dwGivingBasementArea;
    private BigDecimal qdGivingBasementArea;
    private BigDecimal ggGivingBasementArea;

    /**
     * 可售及赠送空间的地下停车数量
     */
    private Integer dwGivingParkingNum;
    private Integer qdGivingParkingNum;
    private Integer ggGivingParkingNum;

    /**
     * 建筑窗墙比（住宅部分）
     */
    private BigDecimal ggBuildingWindowRatio;

    /**
     * 建筑体形系数
     */
    private BigDecimal ggBuildingFormFactor;

    /**
     * 是否设置幼儿园
     */
    private Boolean hasKindergarten;

    /**
     * 是否有机械车位
     */
    private Boolean hasMechanicsParking;

    /**
     * 可售比
     */
    private BigDecimal tzAvailableProportion;
    private BigDecimal dwAvailableProportion;
    private BigDecimal qdAvailableProportion;
    private BigDecimal ggAvailableProportion;

    /**
     * 单车位指标1（报规全口径）
     */
    private BigDecimal tzSingleParkingOne;
    private BigDecimal dwSingleParkingOne;
    private BigDecimal qdSingleParkingOne;
    private BigDecimal ggSingleParkingOne;

    /**
     * 单车位指标2（研发控制口径）
     */
    private BigDecimal dwSingleParkingTwo;
    private BigDecimal qdSingleParkingTwo;
    private BigDecimal ggSingleParkingTwo;

    /**
     * 住宅户均面积
     */
    private BigDecimal qdAverageResidentialArea;
    private BigDecimal ggAverageResidentialArea;

    /**
     * 每百平米地上可售对应地下车位数量
     */
    private BigDecimal qdUnderParkingNumEvery;
    private BigDecimal ggUnderParkingNumEvery;

    /**
     * 是否做装配式
     */
    private Boolean qdHasAssembly;

    /**
     * 预制率
     */
    private BigDecimal qdPrefabricatedRate;

    /**
     * 装配率
     */
    private BigDecimal qdAssemblyRate;

    /**
     * 装配面积
     */
    private BigDecimal qdAssemblyArea;

    /**
     * 装配占比
     */
    private BigDecimal qdAssemblyAccounted;

    /**
     * 绿建星级
     */
    private Integer qdGreenBuildingStar;

    /**
     * 项目定档
     */
    private String projectArchives;

    /**
     * 投委会
     */
    private String tzHousePower;
    private String tzHardcoverArchives;
    private String tzLandscapeArchives;

    /**
     * 定位共创会
     */
    private String dwHousePower;
    private String dwHardcoverArchives;
    private String dwLandscapeArchives;

    /**
     * 启动会
     */
    private String qdHousePower;
    private String qdHardcoverArchives;
    private String qdLandscapeArchives;

    /**
     * 是否批量精装
     */
    private Boolean isBatchHardcover;
    /**
     * 批量精装范围
     */
    private List<String> batchHardcoverScope;

    /**
     * 正向BIM面积合计
     */
    private BigDecimal bimArea;

    /**
     * 正向BIM占比
     */
    private BigDecimal bimRate;

    /**
     * 项目经理
     */
    private String projectManager;

    /**
     * 设计供方
     */
    private String planProvider;
    private String blueprintProvider;
    private String bimProvider;

    /**
     * 完成时间
     */
    private String dwFinishedDate;
    private String qdFinishedDate;
    private String bimStartFinishedDate;
    private String bimMoveFinishedDate;
    private String ggFinishedDate;
    private String openSaleFinishedDate;

    /**
     * 拿地时间 yyyy-MM-dd HH:mm:ss
     */
    private String landTime;

    /**
     * 拿地年份
     */
    private Integer landYear;

    /**
     * 拿地月份
     */
    private Integer landMonth;

    /**
     * 是否获取工规证
     */
    private Boolean hadGgLicense;

    /**
     * 模一启动至模一移交时间
     */
    private Integer model1StartAndMoveDays;


    //merge数据用（plm的excel数据和指标数据）
    private boolean isCompleteTz = false;
    private boolean isCompleteDw = false;
    private boolean isCompleteQd = false;
    private boolean isCompleteGg = false;

    private List<String> tzFormatNameList;
    private List<String> dwFormatNameList;
    private List<String> ggFormatNameList;

    /**
     * 项目等级
     */
    private String level;

    /**
     * 6*6标签
     */
    private String tag6_6;
    /**
     * 6*6标签颜色
     */
    private String tagRgba;

    //主力产品
    private String mainProduct;

    public static C1IndexDashboardResponse mergeDesignIndexWithExcel(C1IndexDashboardResponse base, C1IndexDashboardResponse designIndex) {
        //基本信息
        base.setOperateType(designIndex.getOperateType());
        base.setStageArea(designIndex.getStageArea());
        base.setScPlotR(designIndex.getScPlotR());
        base.setBatchHardcoverScope(designIndex.getBatchHardcoverScope());
        if (StringUtils.isNotBlank(designIndex.getProjectArchives())) {
            base.setLevel(designIndex.getProjectArchives());
        }
        if (Objects.nonNull(designIndex.getBimArea())) {
            base.setBimArea(designIndex.getBimArea());
        }
        if (Objects.nonNull(designIndex.getBimRate())) {
            base.setBimRate(designIndex.getBimRate());
        }
        base.setHasKindergarten(designIndex.getHasKindergarten());
        base.setHasMechanicsParking(designIndex.getHasMechanicsParking());
        base.setIsBatchHardcover(designIndex.getIsBatchHardcover());
        base.setHadGgLicense(designIndex.getHadGgLicense());
        if(StringUtils.isNotEmpty(designIndex.getMainProduct())){
            base.setMainProduct(designIndex.getMainProduct());
        }
        //指标覆盖excel
        //建筑关键指标的数据单独处理，指标阶段到了，建筑关键指标没有数据，以底表为主
        if (designIndex.isCompleteTz()) {
            base.setTzFormatCodeList(designIndex.getTzFormatCodeList());
            if (Objects.nonNull(designIndex.getTzAvailableProportion())) {
                base.setTzAvailableProportion(designIndex.getTzAvailableProportion());
            }
            if (StringUtils.isNotEmpty(designIndex.getTzHardcoverArchives())) {
                base.setTzHardcoverArchives(designIndex.getTzHardcoverArchives());
            }
            if (StringUtils.isNotEmpty(designIndex.getTzHousePower())) {
                base.setTzHousePower(designIndex.getTzHousePower());
            }
            if (StringUtils.isNotEmpty(designIndex.getTzLandscapeArchives())) {
                base.setTzLandscapeArchives(designIndex.getTzLandscapeArchives());
            }
            if (Objects.nonNull(designIndex.getTzSingleParkingOne())) {
                base.setTzSingleParkingOne(designIndex.getTzSingleParkingOne());
            }
            base.setTzFormatNameList(Safes.of(designIndex.getTzFormatNameList()).stream().map(s -> StringUtils.substringBeforeLast(s, ".")).collect(Collectors.toList()));
        }
        if (designIndex.isCompleteDw()) {
            base.setDwFormatNameList(Safes.of(designIndex.getDwFormatNameList()).stream().map(s -> StringUtils.substringBeforeLast(s, ".")).collect(Collectors.toList()));
            base.setDwFormatCodeList(designIndex.getDwFormatCodeList());
            base.setDwTotalArea(designIndex.getDwTotalArea());
            base.setDwBuildUpArea(designIndex.getDwBuildUpArea());
            base.setDwBuildDownArea(designIndex.getDwBuildDownArea());
            base.setDwSaleUpArea(designIndex.getDwSaleUpArea());
            base.setDwResidentialBuildUpArea(designIndex.getDwResidentialBuildUpArea());
            base.setDwResidentialSaleUpArea(designIndex.getDwResidentialSaleUpArea());
            base.setDwResidentialUpSaleHouseholds(designIndex.getDwResidentialUpSaleHouseholds());
            if (Objects.nonNull(designIndex.getDwAboveDeductedArea())) {
                base.setDwAboveDeductedArea(designIndex.getDwAboveDeductedArea());
            }
            if (Objects.nonNull(designIndex.getDwUnderDeductedArea())) {
                base.setDwUnderDeductedArea(designIndex.getDwUnderDeductedArea());
            }
            if (Objects.nonNull(designIndex.getDwAboveParkingNum())) {
                base.setDwAboveParkingNum(designIndex.getDwAboveParkingNum());
            }
            if (Objects.nonNull(designIndex.getDwUnderParkingNum())) {
                base.setDwUnderParkingNum(designIndex.getDwUnderParkingNum());
            }
            if (Objects.nonNull(designIndex.getDwGivingBasementArea())) {
                base.setDwGivingBasementArea(designIndex.getDwGivingBasementArea());
            }
            if (Objects.nonNull(designIndex.getDwGivingParkingNum())) {
                base.setDwGivingParkingNum(designIndex.getDwGivingParkingNum());
            }
            if (Objects.nonNull(designIndex.getDwAvailableProportion())) {
                base.setDwAvailableProportion(designIndex.getDwAvailableProportion());
            }
            if (Objects.nonNull(designIndex.getDwSingleParkingOne())) {
                base.setDwSingleParkingOne(designIndex.getDwSingleParkingOne());
            }
            if (Objects.nonNull(designIndex.getDwSingleParkingTwo())) {
                base.setDwSingleParkingTwo(designIndex.getDwSingleParkingTwo());
            }
            if (StringUtils.isNotEmpty(designIndex.getDwHousePower())) {
                base.setDwHousePower(designIndex.getDwHousePower());
            }
            if (StringUtils.isNotEmpty(designIndex.getDwHardcoverArchives())) {
                base.setDwHardcoverArchives(designIndex.getDwHardcoverArchives());
            }
            if (StringUtils.isNotEmpty(designIndex.getDwLandscapeArchives())) {
                base.setDwLandscapeArchives(designIndex.getDwLandscapeArchives());
            }
        }
        if (designIndex.isCompleteQd()) {
            base.setQdTotalArea(designIndex.getQdTotalArea());
            base.setQdBuildUpArea(designIndex.getQdBuildUpArea());
            base.setQdBuildDownArea(designIndex.getQdBuildDownArea());
            base.setQdSaleUpArea(designIndex.getQdSaleUpArea());
            base.setQdResidentialBuildUpArea(designIndex.getQdResidentialBuildUpArea());
            base.setQdResidentialSaleUpArea(designIndex.getQdResidentialSaleUpArea());
            base.setQdResidentialUpSaleHouseholds(designIndex.getQdResidentialUpSaleHouseholds());
            if (Objects.nonNull(designIndex.getQdAboveDeductedArea())) {
                base.setQdAboveDeductedArea(designIndex.getQdAboveDeductedArea());
            }
            if (Objects.nonNull(designIndex.getQdUnderDeductedArea())) {
                base.setQdUnderDeductedArea(designIndex.getQdUnderDeductedArea());
            }
            if (Objects.nonNull(designIndex.getQdAboveParkingNum())) {
                base.setQdAboveParkingNum(designIndex.getQdAboveParkingNum());
            }
            if (Objects.nonNull(designIndex.getQdUnderParkingNum())) {
                base.setQdUnderParkingNum(designIndex.getQdUnderParkingNum());
            }
            if (Objects.nonNull(designIndex.getQdGivingBasementArea())) {
                base.setQdGivingBasementArea(designIndex.getQdGivingBasementArea());
            }
            if (Objects.nonNull(designIndex.getQdGivingParkingNum())) {
                base.setQdGivingParkingNum(designIndex.getQdGivingParkingNum());
            }
            if (Objects.nonNull(designIndex.getQdAvailableProportion())) {
                base.setQdAvailableProportion(designIndex.getQdAvailableProportion());
            }
            if (Objects.nonNull(designIndex.getQdSingleParkingOne())) {
                base.setQdSingleParkingOne(designIndex.getQdSingleParkingOne());
            }
            if (Objects.nonNull(designIndex.getQdSingleParkingTwo())) {
                base.setQdSingleParkingTwo(designIndex.getQdSingleParkingTwo());
            }
            if (StringUtils.isNotEmpty(designIndex.getQdHousePower())) {
                base.setQdHousePower(designIndex.getQdHousePower());
            }
            if (StringUtils.isNotEmpty(designIndex.getQdHardcoverArchives())) {
                base.setQdHardcoverArchives(designIndex.getQdHardcoverArchives());
            }
            if (StringUtils.isNotEmpty(designIndex.getQdLandscapeArchives())) {
                base.setQdLandscapeArchives(designIndex.getQdLandscapeArchives());
            }
            if (Objects.nonNull(designIndex.getQdPrefabricatedRate())) {
                base.setQdPrefabricatedRate(designIndex.getQdPrefabricatedRate());
            }
            if (Objects.nonNull(designIndex.getQdAssemblyRate())) {
                base.setQdAssemblyRate(designIndex.getQdAssemblyRate());
            }
            if (Objects.nonNull(designIndex.getQdAssemblyArea())) {
                base.setQdAssemblyArea(designIndex.getQdAssemblyArea());
            }
            if (Objects.nonNull(designIndex.getQdAssemblyAccounted())) {
                base.setQdAssemblyAccounted(designIndex.getQdAssemblyAccounted());
            }
            if (Objects.nonNull(designIndex.getQdGreenBuildingStar())) {
                base.setQdGreenBuildingStar(designIndex.getQdGreenBuildingStar());
            }
            if (Objects.nonNull(designIndex.getQdHasAssembly())) {
                base.setQdHasAssembly(designIndex.getQdHasAssembly());
            }
            if (Objects.nonNull(designIndex.getQdAverageResidentialArea())) {
                base.setQdAverageResidentialArea(designIndex.getQdAverageResidentialArea());
            }
        }
        if (designIndex.isCompleteGg()) {
            base.setGgFormatNameList(Safes.of(designIndex.getGgFormatNameList()).stream().map(s -> StringUtils.substringBeforeLast(s, ".")).collect(Collectors.toList()));
            base.setGgFormatCodeList(designIndex.getGgFormatCodeList());
            base.setGgTotalArea(designIndex.getGgTotalArea());
            base.setGgBuildUpArea(designIndex.getGgBuildUpArea());
            base.setGgBuildDownArea(designIndex.getGgBuildDownArea());
            base.setGgSaleUpArea(designIndex.getGgSaleUpArea());
            base.setGgResidentialBuildUpArea(designIndex.getGgResidentialBuildUpArea());
            base.setGgResidentialSaleUpArea(designIndex.getGgResidentialSaleUpArea());
            base.setGgResidentialUpSaleHouseholds(designIndex.getGgResidentialUpSaleHouseholds());
            if (Objects.nonNull(designIndex.getGgAboveDeductedArea())) {
                base.setGgAboveDeductedArea(designIndex.getGgAboveDeductedArea());
            }
            if (Objects.nonNull(designIndex.getGgUnderDeductedArea())) {
                base.setGgUnderDeductedArea(designIndex.getGgUnderDeductedArea());
            }
            if (Objects.nonNull(designIndex.getGgAboveParkingNum())) {
                base.setGgAboveParkingNum(designIndex.getGgAboveParkingNum());
            }
            if (Objects.nonNull(designIndex.getGgUnderParkingNum())) {
                base.setGgUnderParkingNum(designIndex.getGgUnderParkingNum());
            }
            if (Objects.nonNull(designIndex.getGgGivingBasementArea())) {
                base.setGgGivingBasementArea(designIndex.getGgGivingBasementArea());
            }
            if (Objects.nonNull(designIndex.getGgGivingParkingNum())) {
                base.setGgGivingParkingNum(designIndex.getGgGivingParkingNum());
            }
            if (Objects.nonNull(designIndex.getGgAvailableProportion())) {
                base.setGgAvailableProportion(designIndex.getGgAvailableProportion());
            }
            if (Objects.nonNull(designIndex.getGgSingleParkingOne())) {
                base.setGgSingleParkingOne(designIndex.getGgSingleParkingOne());
            }
            if (Objects.nonNull(designIndex.getGgSingleParkingTwo())) {
                base.setGgSingleParkingTwo(designIndex.getGgSingleParkingTwo());
            }
            if (Objects.nonNull(designIndex.getGgAverageResidentialArea())) {
                base.setGgAverageResidentialArea(designIndex.getGgAverageResidentialArea());
            }
            if (Objects.nonNull(designIndex.getGgUnderParkingNumEvery())) {
                base.setGgUnderParkingNumEvery(designIndex.getGgUnderParkingNumEvery());
            }
            if (Objects.nonNull(designIndex.getGgBuildingWindowRatio())) {
                base.setGgBuildingWindowRatio(designIndex.getGgBuildingWindowRatio());
            }
            if (Objects.nonNull(designIndex.getGgBuildingFormFactor())) {
                base.setGgBuildingFormFactor(designIndex.getGgBuildingFormFactor());
            }
        }
        return base;
    }

    /**
     * 获取最新阶段建面
     *
     * @return
     */
    public BigDecimal obtainLatestBuildingArea() {
        if (ggTotalArea != null) {
            return ggTotalArea;
        }
        if (qdTotalArea != null) {
            return qdTotalArea;
        }
        if (dwTotalArea != null) {
            return dwTotalArea;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 最新阶段的总面积
     *
     * @return
     */
    public BigDecimal obtainLatestHouseArea() {
        if (ggResidentialSaleUpArea != null) {
            return ggResidentialSaleUpArea;
        }
        if (qdResidentialSaleUpArea != null) {
            return qdResidentialSaleUpArea;
        }
        if (dwResidentialSaleUpArea != null) {
            return dwResidentialSaleUpArea;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 最新阶段的总户数
     *
     * @return
     */
    public Integer obtainLatestHouseNum() {
        if (ggResidentialUpSaleHouseholds != null) {
            return ggResidentialUpSaleHouseholds;
        }
        if (qdResidentialUpSaleHouseholds != null) {
            return qdResidentialUpSaleHouseholds;
        }
        if (dwResidentialUpSaleHouseholds != null) {
            return dwResidentialUpSaleHouseholds;
        }
        return 0;
    }

    /**
     * 建筑定档
     *
     * @return
     */
    public String obtainLatestPTags() {
        if (qdHousePower != null) {
            return qdHousePower;
        }
        if (dwHousePower != null) {
            return dwHousePower;
        }
        if (tzHousePower != null) {
            return tzHousePower;
        }
        return null;
    }

    /**
     * 获取最新精装定档
     *
     * @return
     */
    public String obtainLatestDTags() {
        if (qdHardcoverArchives != null) {
            return qdHardcoverArchives;
        }
        if (dwHardcoverArchives != null) {
            return dwHardcoverArchives;
        }
        if (tzHardcoverArchives != null) {
            return tzHardcoverArchives;
        }
        return null;
    }

    /**
     * 获取最新景观定档
     *
     * @return
     */
    public String obtainLatestSTags() {
        if (qdLandscapeArchives != null) {
            return qdLandscapeArchives;
        }
        if (dwLandscapeArchives != null) {
            return dwLandscapeArchives;
        }
        if (tzLandscapeArchives != null) {
            return tzLandscapeArchives;
        }
        return null;
    }

    /**
     * 最新阶段主要业态名
     *
     * @return
     */
    public List<String> obtainLatestFormats() {
        if (CollectionUtils.isNotEmpty(ggFormatNameList)) {
            return ggFormatNameList;
        }
        if (CollectionUtils.isNotEmpty(dwFormatNameList)) {
            return dwFormatNameList;
        }
        if (CollectionUtils.isNotEmpty(tzFormatNameList)) {
            return tzFormatNameList;
        }
        return null;
    }
}
